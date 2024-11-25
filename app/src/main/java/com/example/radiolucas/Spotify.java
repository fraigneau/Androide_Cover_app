package com.example.radiolucas;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerState;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The Spotify class provides functionality to connect to the Spotify app remote and authenticate the user.
 */
public class Spotify {

    private static final String CLIENT_ID = "fc99f5a7950c4d11b44dca56a05bffc6"; // Replace with your client ID
    private static final String REDIRECT_URI = "https://google.com/";
    private static final int REQUEST_CODE = 1337;
    private final MainActivity activity;
    public SpotifyAppRemote mSpotifyAppRemote;
    public String Uri;

    /**
     * Constructs a new Spotify instance.
     *
     * @param context the main activity of the application
     */
    public Spotify(MainActivity context) {
        this.activity = context;
    }

    /**
     * Connects to the Spotify app remote using the provided access token.
     *
     * @param accessToken the access token for Spotify
     */
    public void connectToSpotifyRemote(String accessToken) {
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();

        SpotifyAppRemote.connect(activity, connectionParams, new Connector.ConnectionListener() {

            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                Log.e("SpotifyRemote", "Connected! Yay!");

                mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                    // Utilisation de MyThread pour récupérer Uri
                    MyThread myThread = new MyThread();
                    myThread.start();

                    new Thread(() -> {
                        // Attendre que le thread soit terminé
                        while (!myThread.isComplete()) {
                            try {
                                Thread.sleep(2000); // Vérification toutes les 100ms
                            } catch (InterruptedException e) {
                                Log.e("MyThread", "Thread interrupted", e);
                            }
                        }

                        // Récupérer la valeur de Uri une fois le thread terminé
                        Uri = myThread.getResultUri();
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, "Cover URI : " + Uri, Toast.LENGTH_LONG).show();
                            activity.setCoverUri(Uri);

                        });
                    }).start();
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("SpotifyRemote", "Failed to connect", throwable);
            }
        });
    }

    /**
     * Retrieves the player state with a timeout.
     *
     * @return the player state
     * @throws Exception if an error occurs while retrieving the player state
     */
    @NonNull
    private PlayerState getPlayerStateWithTimeout() throws Exception {
        long timeout = TimeUnit.SECONDS.toMillis(20);
        long startTime = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new TimeoutException("Timed out waiting for player state");
            }

            PlayerState playerState = mSpotifyAppRemote.getPlayerApi().getPlayerState().await().getData();
            if (playerState != null) {
                return playerState;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new Exception("Thread interrupted while waiting for player state", e);
            }
        }
    }

    /**
     * Authenticates the user with Spotify.
     */
    public void authenticateSpotify() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"app-remote-control", "user-read-currently-playing"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request);
    }

    /**
     * Classe MyThread pour récupérer ou traiter Uri
     */
    public class MyThread extends Thread {
        private String resultUri;
        private boolean isComplete = false;

        @Override
        public void run() {
            try {
                // Simulation d'un traitement ou récupération de l'URI
                PlayerState playerState = getPlayerStateWithTimeout();
                resultUri = playerState.track.imageUri.raw;
                isComplete = true;
            } catch (Exception e) {
                Log.e("MyThread", "Error in thread execution", e);
            }
        }

        public String getResultUri() {
            return resultUri;
        }

        public boolean isComplete() {
            return isComplete;
        }
    }
}
