package com.fyp.stepupfinal;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class challengeReminderWorker extends Worker {

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
        // perform background task to check challenge time and send notifications
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            db.collection("Registered Users").document(uid).collection("UserChallenges")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // iterates through UserChallenges and calculates start time and end time
                            for (DocumentSnapshot challengeDoc : task.getResult()) {
                                long startDateInMillis = challengeDoc.getDate("StartDate").getTime();
                                long currentTimeInMillis = System.currentTimeMillis();
                                long daysPassed = TimeUnit.MILLISECONDS.toDays(currentTimeInMillis - startDateInMillis);
                                long actualDaysRemaining = 30 - daysPassed;

                                long currentMinutesRemaining = challengeDoc.getLong("Timer");

                                if (currentMinutesRemaining != actualDaysRemaining){
                                    db.collection("Registered Users").document(uid)
                                            .collection("UserChallenges").document(challengeDoc.getId())
                                            .update("Timer", actualDaysRemaining)
                                            .addOnSuccessListener(aVoid -> {
                                                // Log the success of the Timer update
                                                Log.d("ChallengeReminderWorker", challengeDoc.getId()+ " timer successfully updated to: " + actualDaysRemaining);
                                            })
                                            .addOnFailureListener(e -> {
                                                // Log the error
                                                Log.e("ChallengeReminderWorker", "Error updating Timer: ", e);
                                            });
                                }

                                // If there are 3 days left, send a notification
                                if (actualDaysRemaining == 3) {
                                    sendNotification("Challenge Reminder", "You have 3 days left to complete your challenge!");
                                    Log.d("ChallengeReminderWorker","3 days left");
                                }

                                // If the challenge time has ended, mark it as completed and notify the user
                                if (actualDaysRemaining <= 0) {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("CompletionDate", new Date());

                                    db.collection("Registered Users").document(uid)
                                            .collection("UserChallenges").document(challengeDoc.getId())
                                            .update(updates)
                                            .addOnSuccessListener(aVoid -> {
                                                sendNotification("Challenge Completed", "Your challenge has ended!");
                                                Log.d("ChallengeReminderWorker", "Challenge marked as completed with completion date.");
                                            })
                                            .addOnFailureListener(e -> Log.e("ChallengeReminderWorker", "Error marking challenge as completed: ", e));
                                }
                            }
                        }
                    });
        }
        return Result.success();
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
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

