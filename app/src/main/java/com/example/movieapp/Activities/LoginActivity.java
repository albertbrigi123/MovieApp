package com.example.movieapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.movieapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailET, passwordET;
    Button loginBtn, registerBtn;
    FirebaseAuth fAuth;
    CheckBox rememberMeCHB;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        emailET = findViewById(R.id.EmailText);
        passwordET = findViewById(R.id.PasswordText);
        loginBtn = findViewById(R.id.LoginButton);
        registerBtn = findViewById(R.id.SignUpButton);
        rememberMeCHB = findViewById(R.id.RememberMeCheckBox);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        fAuth = FirebaseAuth.getInstance();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        if (preferences.contains("checked") && preferences.getBoolean("checked", false)) {
            rememberMeCHB.setChecked(true);
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);

        } else {
            rememberMeCHB.setChecked(false);
        }

        loginBtn.setOnClickListener(v -> {
            String email = emailET.getText().toString().trim();
            String password = passwordET.getText().toString().trim();
            progressBar.setVisibility(View.VISIBLE);

            if (CheckInputs()) {
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));

                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
            } else {
                Toast.makeText(LoginActivity.this, "Error! The inputs can not be empty.", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        });

        registerBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RegistrationActivity.class)));

        rememberMeCHB.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                editor.putBoolean("checked", true);
                editor.apply();
            } else {
                editor.putBoolean("checked", false);
                editor.apply();
            }
        });
    }

    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    public boolean CheckInputs() {
        boolean inputValidation = true;
        if (isEmpty(emailET)) {
            emailET.setError("Email is required!");
            inputValidation = false;
        }
        if (isEmpty(passwordET)) {
            passwordET.setError("Password is required");
            inputValidation = false;
        }
        return inputValidation;
    }
}