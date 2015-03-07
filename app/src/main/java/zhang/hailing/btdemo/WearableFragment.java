package zhang.hailing.btdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import zhang.hailing.btdemo.service.BluetoothStateEvent;
import zhang.hailing.btdemo.service.State;
import zhang.hailing.btdemo.service.wearable.WearableBlueToothService;

/**
 * Mimic a wearable.
 */
public class WearableFragment extends Fragment {
    private static final int REQ_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private WearableBlueToothService service;

    private TextView stateView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "No Bluetooth", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        if (!bluetoothAdapter.isEnabled()) {
            Intent toEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(toEnableBluetooth, REQ_ENABLE_BT);
        } else if (service == null) {
            setUpService();
        }
    }

    private void setUpService() {
        service = new WearableBlueToothService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wearable, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.stateView = (TextView) view.findViewById(R.id.wearable_state);

        View send = view.findViewById(R.id.wearable_instruction);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!State.STATE_CONNECTED.equals(service.getConnectionState())) {
                    ensureDiscoverable();
                } else {
                    service.sendCommand();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (service != null
                && State.STATE_IDLE.equals(service.getConnectionState())) {
            service.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);

        if (service != null) {
            service.stop();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("wf", "enabled");
                }
        }
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent toBeDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            toBeDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(toBeDiscoverable);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(BluetoothStateEvent event) {
        this.stateView.setText(event.toString());
    }
}
