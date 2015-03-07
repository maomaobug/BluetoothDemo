package zhang.hailing.btdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

import de.greenrobot.event.EventBus;
import zhang.hailing.btdemo.service.BaseEvent;

public class DeviceDialogFragment extends DialogFragment {
    private static final String NO_DEVICE_HINT = "No Devices";
    private ListView pairedList;
    private ListView discoveredList;
    private TextView discoverTitle;
    private ArrayAdapter<String> discoveredAdapter;

    private Activity activity;

    private BluetoothAdapter bluetoothAdapter;
    private View discoverButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(sysBluetoothBroadcastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        activity.registerReceiver(sysBluetoothBroadcastReceiver, filter);
    }

    @Override
    public void onDetach() {
        activity.unregisterReceiver(sysBluetoothBroadcastReceiver);
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Connect to wearable")
                .setView(initView())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
        ;

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private void discoverDevices() {
        discoverTitle.setText("Discovering...");
        discoverButton.setEnabled(false);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private View initView() {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_devices, null);

        discoverButton = rootView.findViewById(R.id.button_discover);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices();
            }
        });

        pairedList = (ListView) rootView.findViewById(R.id.paired_list);
        initPairedList();
        discoveredList = (ListView) rootView.findViewById(R.id.discovered_list);
        initDisCoveredList();
        discoverTitle = (TextView) rootView.findViewById(R.id.title_discover);
        return rootView;
    }

    private void initPairedList() {
        ArrayAdapter<String> pairedAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1);
        pairedList.setAdapter(pairedAdapter);
        pairedList.setOnItemClickListener(deviceClickListener);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                pairedAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedAdapter.add(NO_DEVICE_HINT);
        }
    }

    private final AdapterView.OnItemClickListener deviceClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothAdapter.cancelDiscovery();
            dismiss();

            String info = ((TextView) view).getText().toString();
            if (info.length() > 17) {
                // mac addresses are of 17 chars
                String deviceAddress = info.substring(info.length() - 17);
                connectDevice(deviceAddress);
            }
        }
    };

    private void connectDevice(String deviceAddress) {
        EventBus.getDefault().post(new DeviceAddressEvent(deviceAddress));
    }

    public static class DeviceAddressEvent extends BaseEvent {

        public DeviceAddressEvent(Object what) {
            super(what);
        }
    }

    private void initDisCoveredList() {
        discoveredAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1);
        discoveredList.setAdapter(discoveredAdapter);
        discoveredList.setOnItemClickListener(deviceClickListener);
    }

    private final BroadcastReceiver sysBluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredAdapter.getCount() == 0) {
                    discoveredAdapter.add(NO_DEVICE_HINT);
                }

                if (discoverTitle != null) {
                    discoverTitle.setText("Discovered Devices");
                }

                if (discoverButton != null) {
                    discoverButton.setEnabled(true);
                }
            }

        }
    };
}
