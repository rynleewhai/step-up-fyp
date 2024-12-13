package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.fyp.stepupfinal.challengeReminderWorker;

public class Challenge extends AppCompatActivity {
    private TextView tvChallengeTitle, tvChallengeDesc, tvChallengeDuration, tvChallengeXPGain, tvTimeRemaining, tvGoBack;
    private ImageView ivGoBack;
    private Button buttonChallenge;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_challenge);

        tvChallengeTitle = findViewById(R.id.textView_title);
        tvChallengeDesc = findViewById(R.id.textView_description);
        tvChallengeDuration = findViewById(R.id.textView_duration);
        tvChallengeXPGain = findViewById(R.id.textView_XPgain);
        tvTimeRemaining = findViewById(R.id.textView_timeRemaining);
        buttonChallenge = findViewById(R.id.button_challenge);
        tvGoBack = findViewById(R.id.textView_goBack);
        ivGoBack = findViewById(R.id.imageView_GoBack);


        Intent intent = getIntent();
        String title = intent.getStringExtra("Title");
        String description = intent.getStringExtra("Description");
        int challengePoints = intent.getIntExtra("Points", 0);
        int duration = intent.getIntExtra("Duration", 0);
        int daysRemaining = intent.getIntExtra("Timer",0);

        // Set the data to the views
        tvChallengeTitle.setText(title);
        tvChallengeDesc.setText(description);
        tvChallengeXPGain.setText(String.valueOf(challengePoints)+ " points");
        tvChallengeDuration.setText(String.valueOf(duration) + " days");
        tvTimeRemaining.setText(String.valueOf(daysRemaining) + " days");

        //state Manager for challenges
        challengeStateManager(title, challengePoints);

        // bottom navigation
        tvGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Challenge.this, HomePage.class));
                finish();

            }
        });
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Challenge.this, HomePage.class));
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void challengeStateManager(String challengeTitle, int challengePoints) {
        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();

        // now we find the challenge with that title and get that damn state
        db.collection("Registered Users").document(uid).collection("UserChallenges")
                .whereEqualTo("Title", challengeTitle).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot challengeDoc = task.getResult().getDocuments().get(0);
                        String state = challengeDoc.getString("State");
                        long daysRemaining = challengeDoc.getLong("Timer");

                        ButtonManager(state, daysRemaining);
                        buttonChallenge.setOnClickListener(view -> challengeStateImplementer(state, challengeDoc.getId(), daysRemaining, uid, challengePoints ));
                    }

                    else {
                        // Task failed to complete successfully
                        Exception e = task.getException();
                        if (e != null) {
                            Toast.makeText(this, "Error retrieving challenge: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Task failed without an exception.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // This method will update the button's text and enabled state based on the current challenge state
    private void ButtonManager(String state, long daysRemaining) {
        if (state.equals("new")) {
            buttonChallenge.setText("Let's do it!");
            buttonChallenge.setEnabled(true);
        } else if (state.equals("current") && daysRemaining > 0) {
            buttonChallenge.setText("Ongoing");
            buttonChallenge.setBackgroundColor(getResources().getColor(R.color.ongoing));
            buttonChallenge.setTextColor(ContextCompat.getColor(this, R.color.white));
            buttonChallenge.setEnabled(false);
        } else if (state.equals("current") && daysRemaining <= 0) {
            buttonChallenge.setText("Tap to Complete!");
            buttonChallenge.setBackgroundColor(getResources().getColor(R.color.maroon));
            buttonChallenge.setEnabled(true);
        } else if (state.equals("completed")) {
            buttonChallenge.setText("Re-do Challenge");
            buttonChallenge.setBackgroundColor(getResources().getColor(R.color.darkgreen));
            buttonChallenge.setEnabled(true);
        }
    }

    // schedule and use the challengeReminderWorker class every 24 hours
    private void scheduleChallengeReminderWorker() {
        WorkManager workManager = WorkManager.getInstance(this);
        PeriodicWorkRequest reminderRequest =
                new PeriodicWorkRequest.Builder(challengeReminderWorker.class, 24, TimeUnit.HOURS)
                        .addTag("challenge_reminder")
                        .build();
        workManager.enqueue(reminderRequest);
    }

    // This method handles the button click and changes the state accordingly
    private void challengeStateImplementer(String state, String challengeID, long daysRemaining, String uid, int challengePoints) {

        if (state.equals("new")) {
            // Start the challenge

            Map<String, Object> updates = new HashMap<>();
            updates.put("State", "current");
            updates.put("StartDate", new Date());
            updates.put("Timer", 30);

            db.collection("Registered Users").document(uid).collection("UserChallenges").document(challengeID)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Challenge.this, "Challenge started!", Toast.LENGTH_SHORT).show();

                        try {
                            // Try to schedule the WorkManager task
                            scheduleChallengeReminderWorker();
                            Log.d("ChallengeReminder", "successfully run");
                            Toast.makeText(Challenge.this, "Reminder Scheduled Successfully!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            // Handle the failure in scheduling the reminder worker
                            Log.e("WorkManagerError", "Failed to schedule reminder", e);
                            Toast.makeText(Challenge.this, "Failed to Schedule Reminder", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Challenge.this, "Failed to start Challenge!", Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreError", "Error starting challenge", e);
                    });

            buttonChallenge.setText("Ongoing");
            buttonChallenge.setBackgroundColor(getResources().getColor(R.color.ongoing));
            buttonChallenge.setTextColor(ContextCompat.getColor(this, R.color.white));
            buttonChallenge.setEnabled(false);

        } else if (state.equals("current") && daysRemaining <= 0) {
            buttonChallenge.setBackgroundColor(getResources().getColor(R.color.darkgreen));
            // Complete the challenge
            Map<String, Object> updates = new HashMap<>();
            updates.put("State", "completed");

            db.collection("Registered Users").document(uid)
                    .collection("UserChallenges").document(challengeID)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Challenge.this, "Congratulations! You completed the Challenge.", Toast.LENGTH_SHORT).show();

                        //increment number of challegens done
                        incrementCompletedChallenges();
                    });

            // update user XP points.
            // 1) retrieve user's current points
            // 2) increment user points by challenge points
            // 3) update user points

            db.collection("Registered Users").document(uid).get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            long currentUserXP = userDoc.getLong("Total User XP");
                            long updatedUserXP = currentUserXP + challengePoints;

                            db.collection("Registered Users").document(uid)
                                    .update("Total User XP", updatedUserXP)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "XP updated for user");
                                        Toast.makeText(Challenge.this, "XP Updated!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreError", "Error updating XP", e);
                                        Toast.makeText(Challenge.this, "Failed to update XP", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error retrieving user data", e);
                        Toast.makeText(Challenge.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                    });


            WorkManager.getInstance(this).cancelAllWorkByTag("challenge_reminder");

            buttonChallenge.setText("Re-do Challenge");
            buttonChallenge.setBackgroundColor(getResources().getColor(R.color.darkgreen));
            buttonChallenge.setEnabled(true);

        } else if (state.equals("completed") && ((buttonChallenge.getText()).equals("Re-do Challenge"))) {
            // Re-do the challenge

            // manually set time remaining to 30 days
            // this is not a very sophisticated fix but it doesnt matter since
            // a) default challenge time is 30
            // b) Timer in firestore will be set to 30 too, so its just a UI fix.
            // backend still checks out so dont worry

            tvTimeRemaining.setText(String.valueOf(30));

            Map<String, Object> updates = new HashMap<>();
            updates.put("State", "current");
            updates.put("StartDate", new Date());
            updates.put("Timer", 30);
            updates.put("CompletionDate", null);

            db.collection("Registered Users").document(uid).collection("UserChallenges").document(challengeID)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Challenge.this, "Challenge Re-started!", Toast.LENGTH_SHORT).show();

                        try {
                            // Try to schedule the WorkManager task
                            scheduleChallengeReminderWorker();
                            Log.d("ChallengeReminder", "successfully run");
                            Toast.makeText(Challenge.this, "Reminder Scheduled Successfully!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            // Handle the failure in scheduling the reminder worker
                            Log.e("WorkManagerError", "Failed to schedule reminder", e);
                            Toast.makeText(Challenge.this, "Failed to Schedule Reminder", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Challenge.this, "Failed to Restart Challenge!", Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreError", "Error restarting challenge", e);
                    });

            buttonChallenge.setText("Ongoing");
            buttonChallenge.setBackgroundColor(getResources().getColor(R.color.ongoing));
            buttonChallenge.setEnabled(false);
        }
    }

    private void incrementCompletedChallenges() {
        String uid = firebaseUser.getUid();

        db.collection("Registered Users").document(uid)
                .update("completedChallenges", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Log.d("UserProfile", "Completed challenges incremented."))
                .addOnFailureListener(e -> Log.w("UserProfile", "Failed to increment completed challenges.", e));
    }

}