package com.example.movieapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.movieapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {
    TextView emailTW, nameTW;
    ImageView profileImage;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userId;
    Button logOutBtn, changePasswordBtn;
    ProgressBar loadProgressBar;

    private static final int RESULT_LOAD_IMAGE=1;
    private DocumentReference documentReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        emailTW = findViewById(R.id.Email);
        nameTW = findViewById(R.id.Name);
        profileImage = findViewById(R.id.profile_image);
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        logOutBtn = findViewById(R.id.LogOutButton);
        changePasswordBtn = findViewById(R.id.ChangePasswordButton);
        loadProgressBar = findViewById(R.id.progressBar);
        loadProgressBar.setVisibility(View.VISIBLE);

        FirebaseStorage firebaseStorage= FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        storageReference.child(userId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.e("Tuts+", "uri: " + uri.toString());
                Glide.with(getApplicationContext()).load(uri).into(profileImage);
                loadProgressBar.setVisibility(View.GONE);
            }
        });



        documentReference = fStore.collection("users").document(userId);
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists())
                        {
                            String fName = documentSnapshot.getString("firstName");
                            String lName = documentSnapshot.getString("lastName");
                            String email = documentSnapshot.getString("email");

                            nameTW.setText(fName + " " + lName);
                            emailTW.setText(email);
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "Document does not exists", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

       profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery,RESULT_LOAD_IMAGE);
            }
        });

       changePasswordBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               final View viewDialog = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.new_password, null);
               final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
               builder.setView(viewDialog);
               builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       EditText currentPasswordET, newPasswordET, confirmNewPasswordET;
                       currentPasswordET = viewDialog.findViewById(R.id.CurrentPassword);
                       newPasswordET = viewDialog.findViewById(R.id.NewPassword);
                       confirmNewPasswordET = viewDialog.findViewById(R.id.ConfirmNewPassword);
                       String currentPassword, newPassword, confirmNewPassword;
                       currentPassword = currentPasswordET.getText().toString();
                       confirmNewPassword = confirmNewPasswordET.getText().toString();
                       newPassword = newPasswordET.getText().toString();

                       if(newPassword.equals(confirmNewPassword))
                       {
                           FirebaseUser user = fAuth.getCurrentUser();
                           AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

                           user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if(task.isSuccessful()){
                                       user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()) {
                                                   Log.d("TAG", "Password updated");
                                               } else {
                                                   Log.d("TAG", "Error password not updated");
                                               }
                                           }
                                       });
                                   }
                               }
                           });
                       }
                   }
               });
               builder.create().show();
           }
       });

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                        break;
                    case R.id.nav_profile :
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        break;
                    case R.id.nav_watchList:
                        startActivity(new Intent(getApplicationContext(),WatchListActivity.class));
                        break;
                }
                return true;
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data!=null)
        {
            Uri contentUri = data.getData();
            String path = contentUri.toString();
            profileImage.setImageURI(contentUri);

            uploadImageToFirebase(contentUri);
        }
    }

    private void uploadImageToFirebase(Uri contentUri) {
        loadProgressBar.setVisibility(View.VISIBLE);
        FirebaseStorage firebaseStorage= FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        userId = FirebaseAuth.getInstance().getUid();
        final StorageReference image = storageReference.child(userId);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());
                    }
                });
                loadProgressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Upload Failled.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}