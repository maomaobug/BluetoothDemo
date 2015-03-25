package zhang.hailing.btdemo.graph.module;

import dagger.Module;
import dagger.Provides;
import zhang.hailing.btdemo.service.app.AppBlueToothService;
import zhang.hailing.btdemo.service.wearable.WearableBlueToothService;

/**
 * Created by zhanghailin on 25/3/15.
 */
@Module
public class BlueToothModule {
    @Provides
    AppBlueToothService provideAppBlueToothService() {
        return new AppBlueToothService();
    }

    @Provides
    WearableBlueToothService provideWearableBlueToothService() {
        return new WearableBlueToothService();
    }
}
