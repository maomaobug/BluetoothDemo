package zhang.hailing.btdemo.service.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import de.greenrobot.event.EventBus;
import zhang.hailing.btdemo.service.BluetoothStateEvent;
import zhang.hailing.btdemo.service.Constants;
import zhang.hailing.btdemo.service.State;

public class AppBlueToothService {
    private static final String TAG = AppBlueToothService.class.getSimpleName();
    private ConnectThread connectThread;
    private BluetoothAdapter bluetoothAdapter;
    private State state;
    private CommandExecutor commandExecutor;

    public AppBlueToothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setState(State.STATE_IDLE);
    }

    public synchronized void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void setState(State state) {
        this.state = state;
        stateChanged(state);
    }

    public void connect(String address) {
        if (connectThread != null) {
            connectThread.cancel();
        }

        connectThread = new ConnectThread(address);
        setState(State.STATE_CONNECTING);
        connectThread.start();
    }

    public void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    private void stateChanged(State state) {
        EventBus.getDefault().post(new BluetoothStateEvent(state));
    }

    public static interface CommandExecutor {
        void onCommand();
    }

    private class ConnectThread extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket connectSocket;


        public ConnectThread(String address) {
            this.device = bluetoothAdapter.getRemoteDevice(address);

            try {
                connectSocket = device.createRfcommSocketToServiceRecord(Constants.APP_UUID);
            } catch (IOException e) {

                Log.e(TAG, "error when creating socket");
            }

        }

        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                connectSocket.connect();

                synchronized (AppBlueToothService.this) {
                    setState(zhang.hailing.btdemo.service.State.STATE_CONNECTED);
                }

                InputStream inputStream = connectSocket.getInputStream();
                byte[] cmd = new byte[Constants.CMD.getBytes().length];

                while (true) {
                    try {
                        if (inputStream.read(cmd) > 0) {
                            synchronized (AppBlueToothService.this) {
                                commandExecutor.onCommand();
                            }
                        }
                    } catch (IOException e) {
                        inputStream.close();
                        break;
                    }
                }
            } catch (IOException e) {

                try {
                    connectSocket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "error when close socket after connection failure");
                }

                setState(zhang.hailing.btdemo.service.State.STATE_CONNECTION_FAILED);
            }
        }

        public void cancel() {
            Log.e(TAG, "cancellllllll");
            try {
                if (connectSocket != null) {
                    connectSocket.close();
                    connectSocket = null;
                }
            } catch (IOException e) {
                Log.e(TAG, "error when cancel connecting");
            }
        }
    }
}
