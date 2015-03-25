package zhang.hailing.btdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import zhang.hailing.btdemo.graph.HardwareComponent;
import zhang.hailing.btdemo.service.BluetoothStateEvent;
import zhang.hailing.btdemo.service.app.AppBlueToothService;
import zhang.hailing.btdemo.service.app.AudioRecorderService;

/**
 * Mimic the application.
 */
public class AppFragment extends Fragment implements AudioRecorderService.AudioRecorderStateObserver, AppBlueToothService.CommandExecutor {
    private TextView stateView;
    @Inject
    AppBlueToothService appBlueToothService;
    @Inject
    AudioRecorderService audioRecorderService;
    private Button audioControlView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        HardwareComponent component = ((DemoApplication) getActivity().getApplication()).getComponent();
        audioRecorderService = component.audioRecorderService();
        appBlueToothService = component.appBlueToothService();

        appBlueToothService.setCommandExecutor(this);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        audioControlView = (Button) view.findViewById(R.id.play_record);
        audioRecorderService.setStateObserver(this);
        audioControlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandRecorder();
            }
        });

        stateView = (TextView) view.findViewById(R.id.app_state);
        showBluetoothSate(appBlueToothService.getState().toString());

        View connectButton = view.findViewById(R.id.show_devices);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment connectDeviceFragment = new DeviceDialogFragment();
                connectDeviceFragment.show(getFragmentManager(), null);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_playlist:
                Intent toShowPlaylist = new Intent(getActivity(), PlayListActivity.class);
                getActivity().startActivity(toShowPlaylist);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_app, menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        appBlueToothService.stop();
    }

    private void showBluetoothSate(String state) {
        if (stateView != null) {
            stateView.setText("BluetoothState: " + state);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DeviceDialogFragment.DeviceAddressEvent event) {
        Toast.makeText(getActivity(), event.toString(), Toast.LENGTH_LONG).show();
        appBlueToothService.connect(event.toString());
        Log.e("addr", event.toString());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(BluetoothStateEvent event) {
        showBluetoothSate(event.toString());
    }

    @Override
    public void onAudioRecorderState(String state) {
        if (audioControlView != null) {
            audioControlView.setText("Recorder:" + state);
        }
    }

    @Override
    public void onCommand() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                commandRecorder();
            }
        });
    }

    private void commandRecorder() {
        if (audioRecorderService != null) {
            audioRecorderService.onCommand();
        }
    }

    private Handler mainThreadHandler = new Handler();
}
