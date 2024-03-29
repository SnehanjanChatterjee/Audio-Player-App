package com.example.audioplayerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // CheckBox checkBoxRoot;
    CheckBox checkBoxDownload, checkBoxBollywood;
    final String DOWNLOAD = "Download";
    final String BOLLYWOOD = "Bollywood Songs";
    Button btn;
    private ListView listView;
    File rootDirectory, customDirectory;
    ArrayList<File> songsList;
    String[] songNames;
    public static final String EXTRA_SONGS_LIST = "com.example.audioplayerapp.songslist";
    public static final String EXTRA_CURRENT_SONG_NAME = "com.example.audioplayerapp.currentSongName";
    public static final String EXTRA_POSITION = "com.example.audioplayerapp.position";
    public static final String EXTRA_DIRECTORY = "com.example.audioplayerapp.directory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This disabled the default night theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.listView = findViewById(R.id.listView);
        this.checkBoxDownload = findViewById(R.id.checkBox);
        this.checkBoxBollywood = findViewById(R.id.checkBox2);
        this.btn = findViewById(R.id.button);

        this.requestUserStorageAccess();

        this.checkBoxDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkBoxDownload.isChecked()) {
                    checkBoxBollywood.setChecked(false);
                }
            }
        });

        this.checkBoxBollywood.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkBoxBollywood.isChecked()) {
                    checkBoxDownload.setChecked(false);
                }
            }
        });

        this.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxBollywood.isChecked() || checkBoxDownload.isChecked()) {
                    readSongsFromExtDirectory();
                }
            }
        });
    }

    private void requestUserStorageAccess() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        // Toast.makeText(MainActivity.this, "Ext. Storage permission given", Toast.LENGTH_SHORT).show();

                        // This line would automatically load songs on activity start
                        // readSongsFromExtDirectory();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Ext. Storage permission denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        // If permission denied, will ask for permission again next time app is launched
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void readSongsFromExtDirectory() {
        Log.d("extStorage", "Dir is: " + Environment.getExternalStorageDirectory());

        this.rootDirectory = Environment.getExternalStorageDirectory();
        // this.customDirectory = new File(rootDirectory + "/Bollywood Songs");

        if (this.checkBoxDownload.isChecked()) {
            this.checkBoxBollywood.setChecked(false);
            this.customDirectory = new File(rootDirectory + "/" + DOWNLOAD);
        } else if (this.checkBoxBollywood.isChecked()) {
            this.checkBoxDownload.setChecked(false);
            this.customDirectory = new File(rootDirectory + "/" + BOLLYWOOD);
        }

        this.songsList = fetchSongs(customDirectory);

        if(this.songsList.size() == 0) {
            listView.setAdapter(null);
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
        } else {
            this.songNames = new String[songsList.size()];
            for (int i = 0; i < songsList.size(); i++) {
                this.songNames[i] = songsList.get(i).getName().replace(".mp3", "");
            }
            displaySongs();
            setEventsOnListItem();
        }
    }


    private void displaySongs() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, this.songNames);
        listView.setAdapter(adapter);
    }

    private void setEventsOnListItem() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String currentSongName = listView.getItemAtPosition(i).toString();

                Intent intent = new Intent(MainActivity.this, PlaySong.class);
                intent.putExtra(EXTRA_SONGS_LIST, songsList);
                intent.putExtra(EXTRA_CURRENT_SONG_NAME, currentSongName);
                intent.putExtra(EXTRA_POSITION, i);

                startActivity(intent);
            }
        });
    }

    private ArrayList<File> fetchSongs(File directory) {
        ArrayList<File> songs = new ArrayList<File>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isHidden()) {
                    if (file.isDirectory()) {
                        songs.addAll(fetchSongs(file));
                    } else if (file.getName().endsWith(".mp3") && !file.getName().startsWith(".")) {
                        songs.add(file);
                    }
                }
            }
        }
        return songs;
    }
}