package com.example.radiolucas;

import android.content.Context;
import android.util.Log;

import com.example.radiolucas.cover.CoverInfo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The Downloader class provides functionality to download files from a given URL.
 */
public class Downloader {

    /**
     * Downloads a file from the URL specified in the CoverInfo object and saves it to the device.
     *
     * @param coverInfo the CoverInfo object containing the URL and file name
     * @param context   the context of the application
     */
    public void downloadFile(CoverInfo coverInfo, Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(coverInfo.cover_url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e("Cover_Downloader", "Erreur serveur: " + connection.getResponseCode());
                        throw new RuntimeException("Erreur serveur: " + connection.getResponseCode());
                    }

                    int fileLength = connection.getContentLength();
                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    Log.e("Cover_Downloader", "Téléchargement en cours : ");

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int count;

                    while ((count = input.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, count);
                    }

                    byte[] downloadedData = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.close();
                    input.close();

                    SaveManager coverSaveManager = new SaveManager(context);
                    coverSaveManager.createCoverDirectories();

                    Log.e("Cover_Downloader", "Sauvegarde du fichier : " + coverInfo.cover_name + ".jpg");
                    File savedFile = coverSaveManager.saveFile(
                            downloadedData,
                            coverInfo.cover_name,
                            ".jpg",
                            SaveManager.StorageLocation.NATIVE
                    );

                    if (savedFile != null) {
                        Log.e("Cover_Downloader", "Téléchargement terminé : " + savedFile.getAbsolutePath());
                    } else {
                        Log.e("Cover_Downloader", "Échec de sauvegarde du fichier");
                    }
                    
                } catch (Exception e) {
                    Log.e("Cover_Downloader", "Erreur de téléchargement : " + e.getMessage(), e);
                }
            }
        }).start();
    }
}