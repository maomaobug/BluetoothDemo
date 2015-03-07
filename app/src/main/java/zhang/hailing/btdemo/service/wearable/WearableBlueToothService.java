package zhang.hailing.btdemo.service.wearable;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import de.greenrobot.event.EventBus;
import zhang.hailing.btdemo.service.BluetoothStateEvent;
import zhang.hailing.btdemo.service.Constants;
import zhang.hailing.btdemo.service.State;

public class WearableBlueToothService {
    private static final String TAG = WearableBlueToothService.class.getSimpleName();
    private State state;

    private BluetoothAdapter adapter;
    private ListeningThread listeningThread;
    private BluetoothSocket connectedClientSocket;

    public WearableBlueToothService() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        setConnectionState(State.STATE_IDLE);
    }

    public synchronized State getConnectionState() {
        return state;
    }

    public synchronized void setConnectionState(State state) {
        this.state = state;
        broadCastStateEvent(this.state);
    }

    private void broadCastStateEvent(State state) {
        EventBus.getDefault().post(new BluetoothStateEvent(state));
    }

    public synchronized void start() {
        setConnectionState(State.STATE_LISTENING);

        startListening();
    }

    public synchronized void stop() {
        stopListening();
        if (connectedClientSocket != null) {
            try {
                connectedClientSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "socket already closed");
            } finally {
                connectedClientSocket = null;
            }
        }
    }

    private void startListening() {
        if (listeningThread == null) {
            listeningThread = new ListeningThread();
            listeningThread.start();
        }
    }

    private void stopListening() {
        if (listeningThread != null) {
            listeningThread.cancel();
            listeningThread = null;
        }
    }

    private synchronized void onConnected(BluetoothSocket socket) {
        stopListening();
        connectedClientSocket = socket;
        setConnectionState(State.STATE_CONNECTED);
    }

    public void write(byte[] buffer) {
        if (connectedClientSocket != null) {
            try {
                OutputStream out = connectedClientSocket.getOutputStream();
                out.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCommand() {
        write(Constants.CMD.getBytes());
    }

    private class ListeningThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        private ListeningThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = adapter.listenUsingRfcommWithServiceRecord(
                        "WearableDemo", Constants.APP_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket clientSocket;
            while (!zhang.hailing.btdemo.service.State.STATE_CONNECTED.equals(getConnectionState())) {
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "error while accept");
                    break;
                }

                if (clientSocket != null) {
                    synchronized (WearableBlueToothService.this) {
                        switch (state) {
                            case STATE_LISTENING:
                                onConnected(clientSocket);
                                break;
                            case STATE_IDLE:
                            case STATE_CONNECTED:
                                try {
                                    clientSocket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "error while close client socket");
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                // no operation
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "error while close server socket");
            }
        }
    }

}
