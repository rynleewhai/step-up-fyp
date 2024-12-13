package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.widget.Toast;

public class confirmDeleteAccount extends AppCompatActivity {
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_delete_account);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //establish connection to firebase
        authProfile = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        //get user
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //begin deletion process
        deleteUserAccount(firebaseUser);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void deleteUserAccount(FirebaseUser firebaseUser) {
        if (firebaseUser != null){
                    // First, delete user data from Firestore
                    deleteSubCollection(firebaseUser);
                    deleteUserFromFireStore(firebaseUser);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // Then, delete the user's profile picture from Firebase Storage (if exists)
                    deleteProfilePicture(firebaseUser);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // Finally, delete the user account from Firebase Authentication
                    firebaseUser.delete().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(confirmDeleteAccount.this, "Account deleted successfully.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(confirmDeleteAccount.this, MainActivity.class));
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(confirmDeleteAccount.this, "Failed to delete account. Try again.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(confirmDeleteAccount.this, areYouSureDeleteAccount.class));
                            finish();
                        }
                    });
                }

    }

    private void deleteUserFromFireStore(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        db.collection("Registered Users").document(uid).delete()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(confirmDeleteAccount.this, "User data deleted from Firestore.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(confirmDeleteAccount.this, "Failed to delete Firestore data.", Toast.LENGTH_SHORT).show());
    }

    private void deleteProfilePicture(FirebaseUser firebaseUser) {
        // Delete profile picture from Firebase Storage
        StorageReference profilePicRef = storageReference.child("ProfilePictures/" + firebaseUser.getUid() + ".jpg");
        profilePicRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(confirmDeleteAccount.this, "Profile picture deleted from Firebase Storage.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(confirmDeleteAccount.this, "No profile picture found or deletion failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteSubCollection(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        // Reference to the user's main document
        db.collection("Registered Users").document(uid)
                .collection("UserChallenges")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Track deletion success/failure for each challenge
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("DeleteAccount", "Challenge " + document.getId() + " deleted"))
                                    .addOnFailureListener(e ->
                                            Log.w("DeleteAccount", "Failed to delete challenge " + document.getId(), e));
                        }
                    } else {
                        Log.e("confirmDeleteAccount", "Failed to retrieve UserChallenges");
                        Toast.makeText(confirmDeleteAccount.this, "Failed to retrieve challenges.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(confirmDeleteAccount.this, "Error retrieving subcollection.", Toast.LENGTH_SHORT).show());
    }

}