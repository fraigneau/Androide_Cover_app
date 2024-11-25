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
    public static String Uri_Spotify;
    Spotify spotify = new Spotify(this);
    StorageManager storageManager = new StorageManager(this);
    SaveManager coverSaveManager = new SaveManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Authentification Spotify et préparation des répertoires
        spotify.authenticateSpotify();
        coverSaveManager.createCoverDirectories();
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
            }

            if (Uri_Spotify != null) {
                CoverInfo coverInfo = new CoverInfo(Uri_Spotify);
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (spotify.mSpotifyAppRemote != null && spotify.mSpotifyAppRemote.isConnected()) {
            SpotifyAppRemote.disconnect(spotify.mSpotifyAppRemote);
        }
    }

    public void setCoverUri(String uri_spotify) {
        Uri_Spotify = uri_spotify;
        CoverInfo coverInfo = new CoverInfo(Uri_Spotify);
        Log.e("CoverUri", "Cover URI : " + uri_spotify);

    }
}
