package zhang.hailing.btdemo;

import android.app.Application;

import zhang.hailing.btdemo.graph.Dagger_HardwareComponent;
import zhang.hailing.btdemo.graph.HardwareComponent;

/**
 * Created by zhanghailin on 25/3/15.
 */
public class DemoApplication extends Application {
    private HardwareComponent component;

    public HardwareComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = Dagger_HardwareComponent.builder()
                .build();
    }
}
