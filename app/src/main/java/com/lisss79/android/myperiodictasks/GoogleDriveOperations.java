package com.lisss79.android.myperiodictasks;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class GoogleDriveOperations {

    public Drive drive;
    public FileList fileList;
    public List<File> files;
    private Handler createFolderHandler;
    private File folder;
    private File file;
    private boolean result;
    private final String FOLDER = "application/vnd.google-apps.folder";
    private final String CONFIG_TYPE = "application/octet-stream";

    GoogleDriveOperations(Drive drive, FileList fileList, Context context) {
        this.drive = drive;
        this.fileList = fileList;
        files = fileList.getFiles();
    }


    public String getFileId(String fileName, String parentsName) {
        String currFileName;
        String currFileId;
        String resultFileId = null;
        String currParents;
        String parentsId;

        if(parentsName != null) parentsId = getFolderId(parentsName);
        else parentsId = null;

        for(int i = 0; i < files.size(); i++) {
            currFileName = files.get(i).getName();
            currFileId = files.get(i).getId();
            if(files.get(i).getParents() != null) currParents = files.get(i).getParents().get(0);
            else currParents = null;

            if(parentsId == null) {
                System.out.println(currFileName);
                if(currFileName.equals(fileName)) {
                    resultFileId = currFileId;
                    break;
                }
            }
            else {
                //System.out.println(files.get(i));
                if(currFileName.equals(fileName) && parentsId.equals(currParents)) {
                    resultFileId = currFileId;
                    break;
                }
            }

        }
        return resultFileId;
    }

    public String getFolderId(String fileName) {
        String currFileName;
        String currFileId;
        String currType;
        String resultFileId = null;
        for(int i = 0; i < files.size(); i++) {
            currFileName = files.get(i).getName();
            currFileId = files.get(i).getId();
            currType = files.get(i).getMimeType();
            if(currFileName.equals(fileName) && currType.equals(FOLDER)) {
                resultFileId = currFileId;
                break;
            }
        }
        return resultFileId;
    }

    public String createFolder(String folderName) {

        String id = getFolderId(folderName);
        if(id != null) return id;

        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(FOLDER);

        folder = null;
        Thread createFolderThread = new Thread(() -> {
            try {
                folder = drive.files().create(fileMetadata).setFields("id").execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        createFolderThread.start();

        for(int i = 0; i < 20; i++) {
            if(createFolderThread.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else break;
        }
        return (folder != null ? folder.getId() : null);
    }


    public boolean deleteFileId(String id) {
        result = false;
        Thread deleteFileThread = new Thread(() -> {
            try {
                drive.files().delete(id).execute();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        deleteFileThread.start();

        for(int i = 0; i < 20; i++) {
            if(deleteFileThread.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else break;
        }
        return result;
    }

    public boolean saveConfigFile(String folderId, String filesDir) {
        File fileMetadata = new File();
        fileMetadata.setName("taskslist");
        fileMetadata.setParents(Collections.singletonList(folderId));
        java.io.File filePath = new java.io.File(filesDir + "/taskslist");
        FileContent mediaContent = new FileContent(CONFIG_TYPE, filePath);
        file = null;
        result = false;
        Thread createFileThread = new Thread(() -> {
            try {
                file = drive.files().create(fileMetadata, mediaContent)
                        .setFields("id, parents")
                        .execute();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        createFileThread.start();

        for(int i = 0; i < 20; i++) {
            if(createFileThread.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else break;
        }
        return result;
    }

    public boolean loadConfigFile(String fileId, String filesDir) {

        result = false;

        Thread downloadFileThread = new Thread(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filesDir + "/taskslist");
                drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                outputStream.writeTo(fos);
                fos.close();
                result = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        downloadFileThread.start();
        for(int i = 0; i < 20; i++) {
            if(downloadFileThread.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else break;
        }

        return result;
    }

}
