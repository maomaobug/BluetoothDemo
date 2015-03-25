package zhang.hailing.btdemo.graph;

import dagger.Component;
import zhang.hailing.btdemo.graph.module.BlueToothModule;
import zhang.hailing.btdemo.graph.module.MultimediaModule;
import zhang.hailing.btdemo.service.app.AppBlueToothService;
import zhang.hailing.btdemo.service.app.AudioPlayerService;
import zhang.hailing.btdemo.service.app.AudioRecorderService;
import zhang.hailing.btdemo.service.wearable.WearableBlueToothService;

/**
 * Created by zhanghailin on 25/3/15.
 */
@Component(
        modules = {
                BlueToothModule.class,
                MultimediaModule.class
        }
)
public interface HardwareComponent {
    AppBlueToothService appBlueToothService();
    AudioPlayerService audioPlayerService();
    AudioRecorderService audioRecorderService();
    WearableBlueToothService wearableBlueToothService();
}
