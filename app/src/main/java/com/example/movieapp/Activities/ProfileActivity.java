package com.example.movieapp.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.movieapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {
    TextView emailTW, nameTW;
    ImageView profileImage;
    Button logOutBtn, changePasswordBtn;
    ProgressBar loadProgressBar;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userId;

    private static final int RESULT_LOAD_IMAGE = 1;
    protected DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        emailTW = findViewById(R.id.Email);
        nameTW = findViewById(R.id.Name);
        profileImage = findViewById(R.id.ProfileImage);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        logOutBtn = findViewById(R.id.LogOutButton);
        changePasswordBtn = findViewById(R.id.ChangePasswordButton);
        loadProgressBar = findViewById(R.id.progressBar);
        loadProgressBar.setVisibility(View.VISIBLE);

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        storageReference.child(userId).getDownloadUrl().addOnSuccessListener(uri -> {
            Log.e("Tuts+", "uri: " + uri.toString());
            Glide.with(getApplicationContext()).load(uri).into(profileImage);
            loadProgressBar.setVisibility(View.GONE);
        });

        documentReference = fStore.collection("users").document(userId);
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fName = documentSnapshot.getString("firstName");
                        String lName = documentSnapshot.getString("lastName");
                        String email = documentSnapshot.getString("email");

                        nameTW.setText(fName + " " + lName);
                        emailTW.setText(email);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Document does not exists", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {

                });

        profileImage.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, RESULT_LOAD_IMAGE);
        });

        changePasswordBtn.setOnClickListener(v -> {
            final View viewDialog = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.new_password, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setView(viewDialog);
            builder.setPositiveButton("Save", (dialog, which) -> {
                EditText currentPasswordET, newPasswordET, confirmNewPasswordET;
                currentPasswordET = viewDialog.findViewById(R.id.CurrentPassword);
                newPasswordET = viewDialog.findViewById(R.id.NewPassword);
                confirmNewPasswordET = viewDialog.findViewById(R.id.ConfirmNewPassword);
                String currentPassword, newPassword, confirmNewPassword;
                currentPassword = currentPasswordET.getText().toString();
                confirmNewPassword = confirmNewPasswordET.getText().toString();
                newPassword = newPasswordET.getText().toString();

                if (newPassword.equals(confirmNewPassword)) {
                    FirebaseUser user = fAuth.getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.d("TAG", "Password updated");
                                } else {
                                    Log.d("TAG", "Error password not updated");
                                }
                            });
                        }
                    });
                }
            });
            builder.create().show();
        });

        logOutBtn.setOnClickListener(v -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    break;
                case R.id.nav_profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    break;
                case R.id.nav_watchList:
                    startActivity(new Intent(getApplicationContext(), WatchListActivity.class));
                    break;
            }
            return true;
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri contentUri = data.getData();
            profileImage.setImageURI(contentUri);

            uploadImageToFirebase(contentUri);
        }
    }

    private void uploadImageToFirebase(Uri contentUri) {
        loadProgressBar.setVisibility(View.VISIBLE);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        userId = FirebaseAuth.getInstance().getUid();
        final StorageReference image = storageReference.child(userId);
        image.putFile(contentUri).addOnSuccessListener(taskSnapshot -> {
            image.getDownloadUrl().addOnSuccessListener(uri -> Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString()));
            loadProgressBar.setVisibility(View.GONE);
            Toast.makeText(ProfileActivity.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Upload is Failed.", Toast.LENGTH_SHORT).show());
    }
}