package com.example.movieapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.movieapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText firstNameET, lastNameET, emailET, passwordET, confirmPasswordET;
    Button signUpBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().hide();

        firstNameET = findViewById(R.id.FirstNameEditText);
        lastNameET = findViewById(R.id.LastNameText);
        emailET = findViewById(R.id.EmailText);
        passwordET = findViewById(R.id.PasswordText);
        confirmPasswordET = findViewById(R.id.ConfirmPasswordText);
        signUpBtn = findViewById(R.id.SignUpButton);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        signUpBtn.setOnClickListener(v -> {
            String email = emailET.getText().toString().trim();
            String password = passwordET.getText().toString().trim();
            String firstName = firstNameET.getText().toString();
            String lastName = lastNameET.getText().toString();
            String confirmPassword = confirmPasswordET.getText().toString().trim();

            if (CheckInputs()) {
                if (password.equals(confirmPassword)) {
                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser fUser = fAuth.getCurrentUser();
                            fUser.sendEmailVerification().addOnSuccessListener(aVoid ->
                                    Toast.makeText(RegistrationActivity.this, "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Log.d("TAG", "onFailure: Email not sent " + e.getMessage()));

                            Toast.makeText(RegistrationActivity.this, "User Created.", Toast.LENGTH_SHORT).show();

                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            List<Integer> watchListMovieIds = new ArrayList<>();
                            user.put("firstName", firstName);
                            user.put("lastName", lastName);
                            user.put("email", email);
                            user.put("watchListMovieIds", watchListMovieIds);
                            documentReference.set(user).addOnSuccessListener(aVoid ->
                                    Log.d("TAG", "onSuccess: user Profile is created for " + userID))
                                    .addOnFailureListener(e -> Log.d("TAG", "onFailure: " + e.toString()));
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(RegistrationActivity.this, "Error! Password and confirmation password is not matching.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(RegistrationActivity.this, "Error! The inputs can not be empty.", Toast.LENGTH_SHORT).show();
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
            passwordET.setError("Password is required!");
            inputValidation = false;
        }
        if (isEmpty(firstNameET)) {
            firstNameET.setError("First name is required!");
            inputValidation = false;
        }
        if (isEmpty(lastNameET)) {
            lastNameET.setError("Last name is required!");
            inputValidation = false;
        }
        if (isEmpty(confirmPasswordET)) {
            confirmPasswordET.setError("Confirm password is required!");
            inputValidation = false;
        }
        if (passwordET.equals(confirmPasswordET)) {
            confirmPasswordET.setError("Password and Confirm password does not match!");
            inputValidation = false;
        }
        return inputValidation;
    }
}
