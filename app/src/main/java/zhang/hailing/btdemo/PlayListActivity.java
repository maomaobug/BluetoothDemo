package zhang.hailing.btdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;

import zhang.hailing.btdemo.service.app.AudioPlayerService;
import zhang.hailing.btdemo.service.app.AudioRecorderService;

public class PlayListActivity extends Activity {
    private AudioPlayerService audioPlayerService;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioPlayerService.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);
        ListView listView = (ListView) findViewById(R.id.play_list);
        listView.setEmptyView(findViewById(R.id.empty_view));
        listView.setAdapter(new PlayListAdapter(this));

        audioPlayerService = new AudioPlayerService();
    }

    public class PlayListAdapter extends BaseAdapter {
        private Context context;
        private String[] audioFiles;


        public PlayListAdapter(Context context) {
            this.context = context;
            File directory = new File(AudioRecorderService.getDirectory());
            audioFiles = directory.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".3gp");
                }
            });
        }

        @Override
        public int getCount() {
            return audioFiles == null ? 0 : audioFiles.length;
        }

        @Override
        public String getItem(int position) {
            return audioFiles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_play_list, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.bindView(convertView);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.fileNameText.setText(getItem(position));

            return convertView;
        }

        public class ViewHolder implements AudioPlayerService.AudioPlayerStateObserver{
            Button playButton;
            TextView fileNameText;

            public void bindView(View view) {
                playButton = (Button) view.findViewById(R.id.btn_play_item);
                fileNameText = (TextView) view.findViewById(R.id.fileName_play_item);

                playButton.setText("IDLE");

                view.setTag(this);

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String fileName = AudioRecorderService.getDirectory() + "/" + fileNameText.getText().toString();
                        audioPlayerService.setObserver(ViewHolder.this);
                        audioPlayerService.onCommand(fileName);
                    }
                });
            }

            @Override
            public void onAudioPlayerState(String state) {
                if (playButton != null) {
                    playButton.setText(state);
                }
            }
        }
    }
}
