package com.lisss79.android.myperiodictasks;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleLoginActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<Intent> signInActivityResultLauncher;
    private int result = 4;
    final int AUTH_OK = 1;
    final int AUTH_FAILED = 2;
    final int SIGNIN_FAILED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlelogin);

        // лончер для залогинивания в гугле
        signInActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
        );

        //Initializing Auth
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        signIn();

    }

    public void signIn()
    {
        Intent signIn = mGoogleSignInClient.getSignInIntent();
        signInActivityResultLauncher.launch(signIn);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> myCompletedTask)
    {
        try {
            GoogleSignInAccount account = myCompletedTask.getResult(ApiException.class);
            FirebaseGoogleAuth(account);
        } catch (ApiException e) {
            Toast.makeText(GoogleLoginActivity.this,"Sign In Failed",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount myAccount)
    {
        //check if account is null
        if (myAccount != null)
        {
            AuthCredential myAuthCredential = GoogleAuthProvider.getCredential(myAccount.getIdToken(), null);
            firebaseAuth.signInWithCredential(myAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        result = AUTH_OK;
                        Toast.makeText(GoogleLoginActivity.this, "Authentication Succesful", Toast.LENGTH_SHORT).show();

                    } else {
                        result = AUTH_FAILED;
                        Toast.makeText(GoogleLoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        FirebaseUser mUser = firebaseAuth.getCurrentUser();
                        updateUI(mUser);
                    }
                    setAndFinish(myAccount);
                }
            });
        }
        else{
            result = SIGNIN_FAILED;
            setAndFinish(null);
        }
    }

    private void setAndFinish(GoogleSignInAccount myAccount) {
        App.getInstance().setAccount(myAccount);
        setResult(result);
        finish();
    }

    private void updateUI(FirebaseUser firebaseUser)
    {
        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (mAccount != null)
        {
            String mPersonName = mAccount.getDisplayName();
            String mPersonEmail = mAccount.getEmail();

        }

    }

}