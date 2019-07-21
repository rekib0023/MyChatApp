package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mBtn;


    //private Toolbar mToolbar;
    private Toolbar mToolbar;

    //Progressdialog
    private ProgressDialog mSignProgress;

    // Firebase Auth
    private FirebaseAuth mAuth;

    // Database ref
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Toolbar set
        mToolbar = findViewById(R.id.sign_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        // Database

        // Initialize progress dialog
        mSignProgress = new ProgressDialog(this);

        mEmail = findViewById(R.id.sign_email);
        mPassword = findViewById(R.id.sign_password);
        mBtn = findViewById(R.id.sign_btn);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString().trim();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    mSignProgress.setTitle("Sign In progress");
                    mSignProgress.setMessage("Please wait while we check your credentials");
                    mSignProgress.show();
                    signin(email, password);
                }

            }
        });

    }

    private void signin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mSignProgress.dismiss();


                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {

                            String device_token = instanceIdResult.getToken();
                            String current_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user);


                            mRef.child("device_token").setValue(device_token)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent mainIntent = new Intent(SignInActivity.this, MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    });
                        }
                    });
                } else {
                    mSignProgress.hide();
                    Toast.makeText(SignInActivity.this, "Error", Toast.LENGTH_LONG).show();

                }
            }
        });
    }
}
