package com.lisss79.android.myperiodictasks;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

import java.lang.ref.WeakReference;
import java.util.Collection;

public class App extends Application {

    private static App instance;
    private Drive drive;
    private FileList fileList;
    private GoogleSignInAccount account;
    private Collection<String> scopes;
    private GoogleDriveFilesActivity googleDriveFilesActivity;
    private WeakReference<MainActivity> mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    public Drive getDrive() {
        return drive;
    }

    public void setDrive(Drive drive) {
        this.drive = drive;
    }

    public FileList getFileList() {
        return fileList;
    }

    public void setFileList(FileList fileList) {
        this.fileList = fileList;
    }

    public GoogleSignInAccount getAccount() {
        return account;
    }

    public void setAccount(GoogleSignInAccount account) {
        this.account = account;
    }

    public Collection<String> getScopes() {
        return scopes;
    }

    public void setScopes(Collection<String> scopes) {
        this.scopes = scopes;
    }

    public GoogleDriveFilesActivity getGoogleDriveFilesActivity() {
        return googleDriveFilesActivity;
    }

    public void setGoogleDriveFilesActivity(GoogleDriveFilesActivity googleDriveFilesActivity) {
        this.googleDriveFilesActivity = googleDriveFilesActivity;
    }

    public MainActivity getMainActivity() {
        return mainActivity.get();
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = new WeakReference<>(mainActivity);
    }
}
