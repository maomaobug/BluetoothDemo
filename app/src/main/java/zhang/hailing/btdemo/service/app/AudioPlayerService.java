package zhang.hailing.btdemo.service.app;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioPlayerService implements MediaPlayer.OnCompletionListener {
    private static final String TAG = AudioPlayerService.class.getSimpleName();
    private boolean playing;
    private MediaPlayer mediaPlayer;
    private AudioPlayerStateObserver observer;

    public void setObserver(AudioPlayerStateObserver observer) {
        if (this.observer != null && !this.observer.equals(observer)) {
            stop();
        }

        this.observer = observer;
        feedObserver();
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        feedObserver();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;

            setPlaying(false);
        }
    }

    private void feedObserver() {
        if (observer != null) {
            observer.onAudioPlayerState(playing ? "Playing" : "Idle");
        }
    }

    public void start(String fileName) {
        mediaPlayer = new MediaPlayer();
        try {

            mediaPlayer.setDataSource(fileName);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            setPlaying(true);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed");
        }
    }

    public void onCommand(String fileName) {
        if (playing) {
            stop();
        } else {
            start(fileName);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setPlaying(false);
    }

    public static interface AudioPlayerStateObserver {
        void onAudioPlayerState(String state);
    }

}
