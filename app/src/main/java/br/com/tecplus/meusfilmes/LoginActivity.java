package br.com.tecplus.meusfilmes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginField;
    private EditText mLoginPasswordField;
    private TextView mLoginCriar;

    private Button mLoginBtn;
    private SignInButton mGoogleBtn;

    private Button mCreateAccount;

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;

    private DatabaseReference mDataBaseUsers;

    private static int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();




        mDataBaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataBaseUsers.keepSynced(true);

        mProgress = new ProgressDialog(this);
        mGoogleBtn = (SignInButton)findViewById(R.id.googleBtn);

        mLoginField = (EditText)findViewById(R.id.loginEmailField);
        mLoginPasswordField = (EditText)findViewById(R.id.loginpasswordField);
        mLoginCriar = (TextView)findViewById(R.id.link_signup);
        mLoginBtn = (Button)findViewById(R.id.loginBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkLogin();

            }
        });


        // ----------- GOOGLE SIGN IN -------
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {



                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        mLoginCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mainIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);

            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mProgress.setMessage("Entrando com sua conta GOOGLE");
            mProgress.show();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                mProgress.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {

                            mProgress.dismiss();

                            checkUserExist();
                        }

                        // ...
                    }
                });
    }

    private void checkLogin() {


        String email = mLoginField.getText().toString().trim();
        String password = mLoginPasswordField.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            mProgress.setMessage("Entrando");
            mProgress.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){
                        mProgress.dismiss();

                        checkUserExist();

                    }else {
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this, "Erro ao fazer login email ou senha incorretos",Toast.LENGTH_LONG).show();
                    }
                }
            });


        }
    }

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {


            final String user_id = mAuth.getCurrentUser().getUid();

            mDataBaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(user_id)) {

                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);

                    } else {

                        Intent setupIntent = new Intent(LoginActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
