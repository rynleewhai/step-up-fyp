package com.fyp.stepupfinal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

public class UserProfile extends AppCompatActivity {

    private TextView tvTotalPoints, tvName, tvDOB, tvEmail, tvCompletedChallenges;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private FirebaseFirestore db;
    private ImageView ProfilePicture;
    private FirebaseUser firebaseUser;
    private ImageView ImageViewSettings, ImageViewUpdateProfile;
    private Uri uri;
    private StorageReference storageReference;
    private ImageView homeIcon, userProfileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        tvTotalPoints = findViewById(R.id.textView_totalPoints);
        tvName = findViewById(R.id.textview_userName);
        tvDOB = findViewById(R.id.textView_DOB);
        tvEmail = findViewById(R.id.textview_userEmail);
        progressBar = findViewById(R.id.progressBar);
        ImageViewSettings = findViewById(R.id.imageView_profile_pic);
        ImageViewSettings = findViewById(R.id.imageView_settings);
        ImageViewUpdateProfile = findViewById(R.id.imageView_updateProfile);
        ProfilePicture = findViewById(R.id.imageView_profile_pic);
        tvCompletedChallenges = findViewById(R.id.textView_completedChallenges);

        progressBar.setVisibility(View.GONE);

        //establish connection with firebase auth
        authProfile = FirebaseAuth.getInstance();

        //establish conection with firestore
        db = FirebaseFirestore.getInstance();

        //connection to firebase Storage for photos
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures");

        //get current user from firebase auth
        firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser == null){
            Toast.makeText(UserProfile.this, "Something went wrong! User information not available.", Toast.LENGTH_LONG).show();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }

        ImageViewUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfile.this, UpdateProfileActivity.class));
            }
        });
        ImageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfile.this, SettingsActivity.class));
            }
        });
        homeIcon = findViewById(R.id.imageViewHome);
        userProfileIcon = findViewById(R.id.imageViewProfile);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfile.this, HomePage.class));
                finish();
            }
        });

        userProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserProfile.this, UserProfile.class));
                finish();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail().toString();

        //set user profile pic
        uri = firebaseUser.getPhotoUrl();
        //use picasso to load image from firebase Storage
        //cant use setImage cuz image is retrieved from an external source and not the local machine
        Picasso.get().load(uri).into(ProfilePicture);

        //establish db connection
        db.collection("Registered Users").document(uid).get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               //retrieve document
               DocumentSnapshot document = task.getResult();
               //if uid exists, extract data of user
               if (document.exists()){
                   String name = document.getString("Name");
                   String dob = document.getString("Date of Birth");
                   Long userXP = document.getLong("Total User XP");
                   Long userNumChallenges = document.getLong("completedChallenges");

                   //print data onto textViews
                   tvName.setText(name);
                   tvDOB.setText(dob);
                   tvEmail.setText(email);
                   tvTotalPoints.setText(userXP.toString() + " Points");
                   tvCompletedChallenges.setText(userNumChallenges.toString());
               }
               else{
                   //document doesnt exist
                   Toast.makeText(UserProfile.this, "Something went wrong! User profile not found.", Toast.LENGTH_LONG).show();
               }

           }
           else{
               //task unsuccessful, failed to extract document
               Toast.makeText(UserProfile.this, "Failed to load user profile, please try again!", Toast.LENGTH_LONG).show();
           }
            progressBar.setVisibility(View.GONE);
        });



    }

    public static class challengeReminderWorker extends Worker {

        // we need WorkManager to run the timer as a background task, even when app is closed

        private FirebaseFirestore db;
        private FirebaseUser firebaseUser;

        public challengeReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            db = FirebaseFirestore.getInstance();
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        @NonNull
        @Override
        public Result doWork() {
            // Perform background task to check the challenge time and send notifications
            if (firebaseUser != null) {
                String uid = firebaseUser.getUid();
                // access and retrieve firestore UserChallenges
                db.collection("Registered Users").document(uid).collection("UserChallenges")
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // go through each user challenge
                                for (DocumentSnapshot challengeDoc : task.getResult()) {
                                    // get start date and current time to calculate days remaining.
                                    long startDateInMillis = challengeDoc.getDate("StartDate").getTime();
                                    long currentTimeInMillis = System.currentTimeMillis();
                                    long daysPassed = TimeUnit.MILLISECONDS.toDays(currentTimeInMillis - startDateInMillis);
                                    long actualDaysRemaining = 30 - daysPassed;

                                    long currentDaysRemaining = challengeDoc.getLong("Timer");

                                    if (currentDaysRemaining != actualDaysRemaining){
                                        db.collection("Registered Users").document(uid).collection("UserChallenges").
                                                document(challengeDoc.getId()).update("Timer", actualDaysRemaining)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("ChallengeReminderWorker", "Timer successfully updated to: " + actualDaysRemaining);
                                                })
                                                .addOnFailureListener(e ->{
                                                    Log.e("ChallengeReminderWorker", "Error updating Timer: ", e);
                                                });
                                    }

                                    // If there are 3 days left, send a notification
                                    if (actualDaysRemaining == 3) {
                                        sendNotification("Challenge Reminder", "You have 3 days left till your challenge is complete!");
                                    }

                                    // If the challenge time has ended, mark it as completed and notify the user
                                    if (actualDaysRemaining <= 0) {
                                        db.collection("Registered Users").document(uid)
                                                .collection("UserChallenges").document(challengeDoc.getId())
                                                .update("State", "completed")
                                                .addOnSuccessListener(aVoid -> sendNotification("Challenge Completed", "Your challenge has ended!"));

                                    }
                                }
                            }
                        });
            }
            return Result.success();
        }

        private void sendNotification(String title, String message) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            String channelId = "challenge_reminder_channel";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Challenge Reminders", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.drawable.exclamation_circle_512x512)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            //notificationManager.notify(1, builder.build());
        }
    }
}