package br.com.tecplus.meusfilmes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
    private Button mCreateAccount;

    private ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDataBaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDataBaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataBaseUsers.keepSynced(true);

        mProgress = new ProgressDialog(this);

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

        mLoginCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent mainIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);

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


        final String user_id = mAuth.getCurrentUser().getUid();

        mDataBaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(user_id)){

                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }else {

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
