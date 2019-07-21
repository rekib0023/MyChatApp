package com.example.mychatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    // Buttons
    private Button signBtn;
    private Button regBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        signBtn = findViewById(R.id.start_signin_btn);
        regBtn =  findViewById(R.id.start_reg_btn);

        // Sign In actions
        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent signIntent = new Intent(StartActivity.this, SignInActivity.class);
                startActivity(signIntent);
                //finish();
            }
        });

        // Register actions
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(regIntent);
                //finish();
            }
        });


    }
}
