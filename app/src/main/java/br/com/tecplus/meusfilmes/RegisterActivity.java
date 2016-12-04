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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameFeld;
    private EditText mEmailField;
    private EditText mPasswordField;

    private Button mRegisterBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDataBase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        mNameFeld = (EditText)findViewById(R.id.nameField);
        mEmailField = (EditText)findViewById(R.id.emailField);
        mPasswordField = (EditText)findViewById(R.id.passwordField);

        mRegisterBtn = (Button)findViewById(R.id.registerBtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });
    }

    private void startRegister() {


        final String name = mNameFeld.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String passoword = mPasswordField.getText().toString().trim();


        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(passoword)){

            mProgress.setMessage("Criando...");
            mProgress.show();

            mAuth.createUserWithEmailAndPassword(email,passoword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        String user_id = mAuth.getCurrentUser().getUid();

                        DatabaseReference current_user_db = mDataBase.child(user_id);

                        current_user_db.child("name").setValue(name);
                        current_user_db.child("image").setValue("default");

                        mProgress.dismiss();

                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });

        }
    }
}
