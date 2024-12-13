package com.fyp.stepupfinal;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText etUserName, etDOB;
    private TextView etUserEmail;
    private ProgressBar progressBar;
    private ImageView updateProfile;
    private TextView tvSelectProfilePicture;
    private ImageView ProfilePicture;
    private FirebaseAuth authProfile;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);

        //initialise the data fields
        etUserName = findViewById(R.id.edittext_userName);
        etDOB = findViewById(R.id.edittext_DOB);
        etUserEmail = findViewById(R.id.edittext_userEmail);
        updateProfile = findViewById(R.id.imageView_updateProfile);
        ProfilePicture = findViewById(R.id.imageView_profile_pic);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        tvSelectProfilePicture = findViewById(R.id.change_profile_pic);

        //establish connection with firebase auth and firestore
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //connection to firebase storage
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures");

        Uri uri = firebaseUser.getPhotoUrl();

        //set user's current profile pic in imageView (if uploaded already)
        Picasso.get().load(uri).into(ProfilePicture);

        if (firebaseUser == null) {
            Toast.makeText(UpdateProfileActivity.this, "Something went wrong! User information not available.", Toast.LENGTH_LONG).show();
        } else {
            // Get the user data from Firestore
            loadUserProfile(firebaseUser);
        }

        //upload profile pic
        tvSelectProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        etDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // Date Picker Dialog
                DatePickerDialog picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        //update information button
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInformation(firebaseUser);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void uploadPicturetoFirebase() {
        if (uriImage != null) {
            // Make the progress bar visible before starting the upload
            progressBar.setVisibility(View.VISIBLE);

            // Save the image with the UID of the current user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() +
                    "." + getFileExtension(uriImage));

            // Upload the image to Firebase Storage
            fileReference.putFile(uriImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUri = uri;
                                    //firebaseUser = authProfile.getCurrentUser();

                                    // Finally set the display image of the user after upload
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(downloadUri).build();
                                    firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(UpdateProfileActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                                        }
                                        // Hide the progress bar once the profile update is complete
                                        progressBar.setVisibility(View.GONE);
                                    });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Hide the progress bar and show an error message if upload fails
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }
    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            ProfilePicture.setImageURI(uriImage);

            uploadPicturetoFirebase();
        }

    }

    private void loadUserProfile(FirebaseUser firebaseuser) {
        //obtain uid from user
        String uid = firebaseuser.getUid();

        //reference to the document in firestore
        db.collection("Registered Users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()){
                    //extract user information and populate textFields
                    String currentUserName = document.getString("Name");
                    String currentUserDOB = document.getString("Date of Birth");
                    String currentUserEmail = firebaseuser.getEmail().toString();

                    etUserName.setText(currentUserName);
                    etDOB.setText(currentUserDOB);
                    etUserEmail.setText(currentUserEmail);

                }
                else {
                    Toast.makeText(UpdateProfileActivity.this, "User profile not found.", Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(UpdateProfileActivity.this, "Failed to load profile. Please try again.", Toast.LENGTH_LONG).show();
            }
        });


    }
    private void updateUserInformation(FirebaseUser firebaseuser){
        String updatedName = etUserName.getText().toString();
        String updatedDOB = etDOB.getText().toString();
        String dobPattern = "\\b(?:[0-3]?\\d)\\/(?:[0-1]?\\d)\\/(?:\\d{4})\\b";
        String eightCharPattern = "^.{1,8}$";

        // Check if any fields are empty/ not of the correct format
        if (updatedName.isEmpty() || updatedDOB.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show();
        }
        else if (!updatedName.matches(eightCharPattern)){
            etUserName.setError("Max 8 characters");
            etUserName.requestFocus();
        }
        else if (!updatedDOB.matches(dobPattern)){
            etDOB.setError("Valid DOB is required");
            etDOB.requestFocus();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            Map<String, Object> updatedUserInformation = new HashMap<>();
            updatedUserInformation.put("Name", updatedName);
            updatedUserInformation.put("Date of Birth", updatedDOB);
            //updatedUserInformation.put("Email", updatedEmail);

            String uid = firebaseuser.getUid();
            db.collection("Registered Users").document(uid).update(updatedUserInformation)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(UpdateProfileActivity.this, UserProfile.class));
                            finish();
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, "Failed to update profile. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
            progressBar.setVisibility(View.GONE);
        }
    }
}