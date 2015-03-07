package zhang.hailing.btdemo.service.app;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorderService {
    private static final String TAG = AudioRecorderService.class.getSimpleName();
    private boolean recording;
    private MediaRecorder mediaRecorder;
    private AudioRecorderStateObserver stateObserver;

    public static String getDirectory() {
        String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BTDemo/";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        return directoryPath;
    }

    public void setRecordingState(boolean recording) {
        this.recording = recording;
        feedObserver();
    }

    private void feedObserver() {
        if (stateObserver != null) {
            stateObserver.onAudioRecorderState(recording ? "Recoding" : "Idle");
        }
    }

    public void setStateObserver(AudioRecorderStateObserver stateObserver) {
        this.stateObserver = stateObserver;
        feedObserver();
    }

    public void onCommand() {
        if (recording) {
            stop();
        } else {
            start();
        }
    }

    private void stop() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        setRecordingState(false);
    }

    private void start() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(generateFileName());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            setRecordingState(true);
        } catch (IOException e) {
            Log.e(TAG, "prepare failed");
        }
    }

    private String generateFileName() {
        String filePath = getDirectory() + System.currentTimeMillis() + ".3gp";

        Log.d(TAG, filePath);
        return filePath;
    }

    public static interface AudioRecorderStateObserver {
        void onAudioRecorderState(String state);
    }

}
