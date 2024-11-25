package com.example.radiolucas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.radiolucas.cover.CoverInfo;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;

    StorageManager storageManager = new StorageManager(this);
    Spotify spotify = new Spotify(this);
    SaveManager coverSaveManager = new SaveManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spotify.authenticateSpotify();
        storageManager.createAppFolder("Cover");
        storageManager.createAppFolder("Resize");
        coverSaveManager.createCoverDirectories();
        //testresize();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    spotify.connectToSpotifyRemote(response.getAccessToken());
                    break;
                case ERROR:
                    Log.e("SpotifyAuth", "Auth error: " + response.getError());
                    break;
                default:
                    Log.e("SpotifyAuth", "Auth result: " + response.getType());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect from Spotify to avoid memory leaks
        if (spotify.mSpotifyAppRemote != null && spotify.mSpotifyAppRemote.isConnected()) {
            SpotifyAppRemote.disconnect(spotify.mSpotifyAppRemote);
        }
    }

    public void testresize() {
        Resize resize = new Resize(this);
        //CoverInfo coverInfo = new CoverInfo();
        resize.Image("/storage/emulated/0/Download/native/ab67616d0000b273a281c8c6cb1b0630d3a24bd7.jpg", "/storage/emulated/0/Download/resize/cover.jpg");
    }
}

//