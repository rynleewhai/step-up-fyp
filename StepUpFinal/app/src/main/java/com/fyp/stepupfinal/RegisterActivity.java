package com.fyp.stepupfinal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Button;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.net.Uri;
import com.squareup.picasso.Picasso;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etRePassword, etDOB;
    private ProgressBar progressBar;
    private StorageReference storageReference;
    private ImageView registerAccount;
    private Uri uriImage;
    private static final String TAG = "RegisterActivity";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etDOB = findViewById(R.id.et_dob);
        etRePassword = findViewById(R.id.et_reenter_password);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        etDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                // Date Picker Dialog
                DatePickerDialog picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        etDOB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        registerAccount = findViewById(R.id.imageView_RegisterAccount);
        registerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textName = etName.getText().toString();
                String textEmail = etEmail.getText().toString();
                String textPassword = etPassword.getText().toString();
                String textRePassword = etRePassword.getText().toString();
                String textDOB = etDOB.getText().toString();
                String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&+={*}]).{6,}$";
                String eightCharPattern = "^.{1,8}$";

                if (TextUtils.isEmpty(textName)){
                    //Toast.makeText(RegisterActivity.this, "Please enter your full name", Toast.LENGTH_LONG).show();
                    etName.setError("name is required");
                    etName.requestFocus();
                }
                else if (!textName.matches(eightCharPattern)){
                    Toast.makeText(RegisterActivity.this, "Username cannot be longer than 8 Characters", Toast.LENGTH_LONG).show();
                    etName.setError("Username too long");
                    etName.requestFocus();
                }
                else if (TextUtils.isEmpty(textDOB)){
                    //Toast.makeText(RegisterActivity.this, "Please enter your DOB", Toast.LENGTH_LONG).show();
                    etDOB.setError("DOB is required");
                    etDOB.requestFocus();
                }
                else if (!isValidDOB(textDOB)) {
                    Toast.makeText(RegisterActivity.this, "Minimum age 13.", Toast.LENGTH_LONG).show();
                    etDOB.setError("You must be at least 13 years old");
                    etDOB.requestFocus();
                }

                else if (TextUtils.isEmpty(textEmail)){
                    //Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    etEmail.setError("Email is required");
                    etEmail.requestFocus();
                }

                else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    //Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_LONG).show();
                    etEmail.setError("Valid email is required");
                    etEmail.requestFocus();
                }
                else if (TextUtils.isEmpty(textPassword)){
                    //Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_LONG).show();
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                }
                else if (textPassword.length() < 6){
                    Toast.makeText(RegisterActivity.this, "Password must be more than 6 characters", Toast.LENGTH_LONG).show();
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                }
                else if (!textPassword.matches(passwordPattern)){
                    Toast.makeText(RegisterActivity.this, "Password must be alphanumeric and contain a special symbol !@#$%^&+={*}", Toast.LENGTH_LONG).show();
                    etPassword.setError("Password not secure enough");
                    etPassword.requestFocus();
                }
                else if (TextUtils.isEmpty(textRePassword)){
                    //Toast.makeText(RegisterActivity.this, "Please re-enter your password", Toast.LENGTH_LONG).show();
                    etRePassword.setError("password confirmation is required");
                    etRePassword.requestFocus();
                }
                else if (!textPassword.equals(textRePassword)){
                    //Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_LONG).show();
                    etRePassword.setError("Passwords do not match");
                    etRePassword.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    // custom method for registering a new user account
                    registerUser(textName, textDOB, textEmail, textPassword, textRePassword);
                }

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // calculate age of user and make sure its not less than 13
    private boolean isValidDOB(String dob) {
        // set date format using SimpleDateFormat class
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.ENGLISH);
        // ensure only valid dates eg not allowing dates like 33/14/2002
        dateFormat.setLenient(false);

        try {
            // set dob string as a Date object instance
            Date dateOfBirth = dateFormat.parse(dob);
            Calendar dobCalendar = Calendar.getInstance();
            //set dob as a calendar object instance
            dobCalendar.setTime(dateOfBirth);

            // Calculate the minimum date allowed (13 years ago from today)
            Calendar minDateforAgeRequirement = Calendar.getInstance();
            minDateforAgeRequirement.add(Calendar.YEAR, -13); // Set it to 13 years before today

            // Check if the date of birth is before the minimum age date
            return !dobCalendar.after(minDateforAgeRequirement); // Return true if 13 or older, false otherwise

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Register a user
    private void registerUser(String textName, String textDOB, String textEmail, String textPassword, String textRePassword){

        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String uid = firebaseUser.getUid();


                    //create map to store new user data
                    Map<String, Object> userInformation = new HashMap<>();
                    userInformation.put("Name", textName);
                    userInformation.put("Date of Birth", textDOB);
                    userInformation.put("Total User XP", 0);
                    userInformation.put("completedChallenges", 0);

                    db.collection("Registered Users").document(uid)
                            .set(userInformation)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Send verification email
                                        firebaseUser.sendEmailVerification();
                                        Toast.makeText(RegisterActivity.this, "User registered successfully.", Toast.LENGTH_LONG).show();

                                        // add personalized challenges to user
                                        initializeChallengeForUser(uid);

                                        //add default pic to user
                                        uploadDefaultProfilePic(uid);

                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(RegisterActivity.this, "User registration failed. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Firestore error: " + e.getMessage());
                                Toast.makeText(RegisterActivity.this, "Failed to add user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            });
                }
                else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        etPassword.setError("Your password is too weak. Kindly use a mix of alphabets, numbers, and symbols.");
                        etPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        etEmail.setError("Your email is invalid or already in use. Kindly re-enter.");
                        etEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        etEmail.setError("This email is already in use.");
                        etEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });



    }

    private void initializeChallengeForUser(String uid){
        //retrieve challenges from Challenges collection
        db.collection("Challenges").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    //iterate through each challenge and add to hashmap
                    for (DocumentSnapshot document : task.getResult()) {
                        // Get challenge data
                        Map<String, Object> challengeData = new HashMap<>();
                        challengeData.put("Title", document.getString("Title"));
                        challengeData.put("Description", document.getString("Description"));
                        challengeData.put("Points", document.getLong("Points"));
                        challengeData.put("Duration", document.getLong("Duration"));
                        challengeData.put("State", "new");  //initialise state to NEW
                        challengeData.put("StartDate", null); // No start date yet
                        challengeData.put("CompletionDate", null); // No completion date yet
                        challengeData.put("Timer", document.getLong("Duration"));  // Timer is the same as duration

                        //add challengedata into registered users according to uid
                        db.collection("Registered Users").document(uid).collection("UserChallenges").
                                document(document.getId()).set(challengeData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firestore", "Challenge successfully added for user: " + uid);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("Firestore", "Error adding challenge: ", e);
                                    }
                                });
                    }

                } else {
                    Log.w("Firestore", "Error getting challenges.", task.getException());
                }
            }
        });
    }

    private void uploadDefaultProfilePic(String uid){
        //make a reference to firebase storage
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures");
        //convert drawable resource to uri
        Uri defaultPictureUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.profile_circle_512x512);

        //make reference to user's profile pic storage
        StorageReference fileReference = storageReference.child(uid +
                ".jpg");

        //upload pic to usser's firebase storage
        fileReference.putFile(defaultPictureUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Set the default profile picture URL for the new user in Firebase Auth
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri).build();
                        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Default profile picture set successfully.");
                            }
                        });
                    }
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload default profile picture: " + e.getMessage()));
    }
}