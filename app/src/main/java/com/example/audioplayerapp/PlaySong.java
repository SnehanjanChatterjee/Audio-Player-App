package com.example.audioplayerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {
    private TextView txtView;
    private ImageView previous, play, pause, next;
    ArrayList<File> songsList;
    private MediaPlayer mediaPlayer;
    private String currentSongName;
    private Integer position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        initialize();
        playCurrentSong();
    }

    private void initialize() {
        this.txtView = findViewById(R.id.textView);
        this.previous = findViewById(R.id.previous);
        this.next = findViewById(R.id.next);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        this.songsList = (ArrayList) bundle.getParcelableArrayList(MainActivity.EXTRA_SONGS_LIST);
        this.currentSongName = intent.getStringExtra(MainActivity.EXTRA_CURRENT_SONG_NAME);
        this.position = intent.getIntExtra(MainActivity.EXTRA_POSITION, 0);

        this.txtView.setText(this.currentSongName);
    }

    private void playCurrentSong() {
        Uri uri = Uri.parse(songsList.get(position).toString());
        this.mediaPlayer = MediaPlayer.create(this, uri);
        this.mediaPlayer.start();
    }
}