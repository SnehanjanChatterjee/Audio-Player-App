package com.example.audioplayerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {
    private TextView txtView;
    private TextView artistInfo;
    private ImageView imageView;
    private ImageView previous, pause, next;
    private MediaPlayer mediaPlayer;
    private String currentSongName;
    private String songDirectory;
    private Integer position;
    ArrayList<File> songsList;
    SeekBar seekBar;
    Thread updateSeekBar;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMediaPlayer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        initialize();
        playCurrentSong();
        updateSeekbarMovement();
        songControls();
    }

    private void initialize() {
        this.txtView = findViewById(R.id.textView);
        this.artistInfo = findViewById(R.id.artistInfo);
        this.imageView = findViewById(R.id.imageView);
        this.previous = findViewById(R.id.previous);
        this.next = findViewById(R.id.next);
        this.pause = findViewById(R.id.pause);
        this.seekBar = findViewById(R.id.seekBar);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.songsList = (ArrayList) bundle.getParcelableArrayList(MainActivity.EXTRA_SONGS_LIST);
        this.currentSongName = intent.getStringExtra(MainActivity.EXTRA_CURRENT_SONG_NAME);
        this.position = intent.getIntExtra(MainActivity.EXTRA_POSITION, 0);
        // this.songDirectory = this.songsList.get(this.position).getAbsolutePath();
    }

    private void playCurrentSong() {
        Uri uri = Uri.parse(songsList.get(position).toString());
        this.mediaPlayer = MediaPlayer.create(this, uri);
        this.mediaPlayer.start();
        this.seekBar.setMax(this.mediaPlayer.getDuration());
        updateDisplay();
    }

    private void updateDisplay() {
        String songName = this.songsList.get(this.position).getName().toString().replace(".mp3", "");
        this.txtView.setText(songName);
        this.txtView.setSelected(true);
        this.pause.setImageResource(R.drawable.pause);

        this.songDirectory = this.songsList.get(this.position).getAbsolutePath();
        try {
            String artist = this.getArtist(this.songDirectory);
            if (!artist.isEmpty()) {
                this.artistInfo.setText(artist);
            }

            Bitmap bm = this.loadThumbnail(this.songDirectory);
            if (bm != null) {
                this.imageView.setImageBitmap(bm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMediaPlayer() {
        this.mediaPlayer.stop();
        this.mediaPlayer.release();
        this.updateSeekBar.interrupt();
    }

    private synchronized void updateSeekbarMovement() {
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        this.updateSeekBar = new Thread() {
            @Override
            public void run() {
                int curPosition = 0;
                try {
                    while (curPosition < mediaPlayer.getDuration()) {
                        curPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(curPosition);
                        sleep(800);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        this.updateSeekBar.start();
    }

    private void songControls() {
        this.pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    pause.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                } else {
                    pause.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                }
            }
        });

        this.previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMediaPlayer();
                if (position != 0) {
                    position = position - 1;
                } else {
                    position = songsList.size() - 1;
                }
                playCurrentSong();
            }
        });

        this.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMediaPlayer();
                if (position != songsList.size() - 1) {
                    position = position + 1;
                } else {
                    position = 0;
                }
                playCurrentSong();
            }
        });

    }

    public Bitmap loadThumbnail(String absolutePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(absolutePath);
        byte[] data = retriever.getEmbeddedPicture();
        retriever.release();
        if (data == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public String getArtist(String absolutePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(absolutePath);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        retriever.release();
        return artist;
    }
}