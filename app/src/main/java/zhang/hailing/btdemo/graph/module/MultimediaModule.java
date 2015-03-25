package zhang.hailing.btdemo.graph.module;

import dagger.Module;
import dagger.Provides;
import zhang.hailing.btdemo.service.app.AudioPlayerService;
import zhang.hailing.btdemo.service.app.AudioRecorderService;

/**
 * Created by zhanghailin on 25/3/15.
 */
@Module
public class MultimediaModule {
    @Provides
    AudioPlayerService provideAudioPlayerService() {
        return new AudioPlayerService();
    }

    @Provides
    AudioRecorderService provideAudioRecorderService() {
        return new AudioRecorderService();
    }
}
