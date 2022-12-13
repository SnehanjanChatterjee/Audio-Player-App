package com.example.audioplayerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
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
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        this.requestUserStorageAccess();
    }

    private void requestUserStorageAccess() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Toast.makeText(MainActivity.this, "Ext. Storage permission given", Toast.LENGTH_SHORT).show();

                        readSongsFromExtDirectory();
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

        File rootDirectory = Environment.getExternalStorageDirectory();
        File customDirectory = new File(rootDirectory + "/Bollywood Songs");

        ArrayList<File> songsList = fetchSongs(customDirectory);

        String[] songNames = new String[songsList.size()];
        for (int i = 0; i < songsList.size(); i++) {
            songNames[i] = songsList.get(i).getName().replace(".mp3", "");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, songNames);
        listView.setAdapter(adapter);
    }

    private ArrayList<File> fetchSongs(File directory) {
        ArrayList<File> songsList = new ArrayList<File>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isHidden()) {
                    if (file.isDirectory()) {
                        songsList.addAll(fetchSongs(file));
                    } else if (file.getName().endsWith(".mp3") && !file.getName().startsWith(".")) {
                        songsList.add(file);
                    }
                }
            }
        }
        return songsList;
    }
}