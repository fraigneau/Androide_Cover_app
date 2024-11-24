package com.example.radiolucas;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The StorageManager class provides functionality to manage storage operations such as creating folders and files.
 */
public class StorageManager {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final MainActivity activity;

    /**
     * Constructs a new StorageManager instance.
     *
     * @param activity the main activity of the application
     */
    public StorageManager(MainActivity activity) {
        this.activity = activity;
    }

    /**
     * Creates a folder in the external files directory of the application.
     *
     * @param folderName the name of the folder to create
     * @return the created folder, or null if the folder could not be created
     */
    public File createAppFolder(String folderName) {
        File folder;
        folder = new File(activity.getExternalFilesDir(null), folderName);

        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            if (!success) {
                Log.e("StorageManager", "Erreur lors de la création du dossier");
                return null;
            }
        }
        Log.e("StorageManager", "Dossier créé avec succès : " + folder.getAbsolutePath());
        return folder;
    }

    /**
     * Creates a file in the specified folder with the given content.
     *
     * @param folder   the folder in which to create the file
     * @param fileName the name of the file to create
     * @param content  the content to write to the file
     * @return true if the file was created successfully, false otherwise
     */
    public boolean createFile(File folder, String fileName, String content) {
        try {
            File file = new File(folder, fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}