package com.lisss79.android.myperiodictasks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collection;

public class GoogleDriveFilesActivity extends AppCompatActivity {

    private GoogleSignInAccount account;
    private Drive googleDriveService;
    private FileList fileList;
    private ActivityResultLauncher<Intent> userRecoverableAuthIOExceptionResultLauncher;
    private Handler filesHandler;
    private Handler createFolderHandler;
    private Collection<String> scopes;

    private final int END_CODE_OK = 1;
    private final int END_CODE_CANCEL = 2;
    private int mResult;
    private File folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlelogin);

        // лончер на случай отсутствия прав доступа
        userRecoverableAuthIOExceptionResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_CANCELED) {
                            fileList = null;
                            mResult = RESULT_OK;
                            filesHandler.sendEmptyMessage(END_CODE_CANCEL);
                        }
                        else getFiles();
                    }
                }
        );

        account = App.getInstance().getAccount();
        scopes = App.getInstance().getScopes();
        System.out.println("Account got: " + account);
        googleDriveService = getDriveService(account, scopes);
        System.out.println("GoogleDriveService got: " + googleDriveService);

        getFiles();

        filesHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what == END_CODE_OK) {
                    setAndFinish();
                }
                else {
                    System.out.println("File haven't got");
                    setAndFinish();
                }
            }
        };

    }

    private void getFiles() {
        Thread filesThread = new Thread(() -> {
            try {
                fileList = googleDriveService.files().list()
                        .setFields("files(id, name, mimeType, parents, owners(me), capabilities(canDelete))").execute();
                mResult = RESULT_OK;
                filesHandler.sendEmptyMessage(END_CODE_OK);
            } catch (UserRecoverableAuthIOException e) {
                Log.i("Error", "UserRecoverableAuthIOException");
                userRecoverableAuthIOExceptionResultLauncher.launch(e.getIntent());
            } catch (IOException e) {
                e.printStackTrace();
                fileList = null;
                mResult = RESULT_OK;
                filesHandler.sendEmptyMessage(END_CODE_CANCEL);
            }
        });
        filesThread.start();
    }

    private void setAndFinish() {
        App.getInstance().setDrive(googleDriveService);
        App.getInstance().setFileList(fileList);
        App.getInstance().setGoogleDriveFilesActivity(this);
        setResult(mResult);
        finish();
    }


    private Drive getDriveService(GoogleSignInAccount myAccount, Collection<String> myScopes) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2
                (GoogleDriveFilesActivity.this, myScopes);
        credential.setSelectedAccount(myAccount.getAccount());
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                AndroidJsonFactory.getDefaultInstance(),
                credential).setApplicationName(getString(R.string.app_name)).build();
    }
}