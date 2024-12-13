package com.fyp.stepupfinal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity {
    private ImageView homeIcon, userProfileIcon, refreshChallengeList;
    private TextView tvGreetingUser;
    private RecyclerView recyclerView;
    private ArrayList<ChallengeModel> challengeList;
    private RadioGroup radioGroup;
    private RadioButton RadioButtonNew, RadioButtonComplete, RadioButtonCurrent;
    private ProgressDialog progressDialog;
    private Adapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        homeIcon = findViewById(R.id.imageViewHome);
        userProfileIcon = findViewById(R.id.imageViewProfile);
        recyclerView = findViewById(R.id.challengeRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvGreetingUser = findViewById(R.id.greetingTextView);
        radioGroup = findViewById(R.id.radioGroupFilter);
        RadioButtonNew = findViewById(R.id.radioButtonNew);
        RadioButtonComplete = findViewById(R.id.radioButtonCompleted);
        RadioButtonCurrent = findViewById(R.id.radioButtonCurrent);
        refreshChallengeList = findViewById(R.id.imageView2);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        String uid = firebaseUser.getUid();

        //DISPLAY USE'S NAME
        displayUserName(uid);

        //CHALLENGE FILTER BY STATE
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String stateFilter = null;

                if (checkedId == R.id.radioButtonCurrent) {
                    stateFilter = "current";
                } else if (checkedId == R.id.radioButtonNew) {
                    stateFilter = "new";
                } else if (checkedId == R.id.radioButtonCompleted) {
                    stateFilter = "completed";
                }

                if (stateFilter != null) {
                    filterChallengesByState(stateFilter, uid);
                }

            }
        });


        //INVOKE ADAPTER CLASS
        challengeList = new ArrayList<>();
        adapter = new Adapter(HomePage.this, challengeList);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching data");
        progressDialog.show();

        //DISPLAY CHALLENGES
        retrieveAndDisplayChallenges(uid);

        // listens to any new challenges or updates to existing challenges
        //backgroundListenerForNewChallenges();
        refreshChallengeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backgroundListenerForNewChallenges();
                startActivity(new Intent(HomePage.this, HomePage.class));
                finish();
            }
        });

        // bottom navigation
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //backgroundListenerForNewChallenges();
                startActivity(new Intent(HomePage.this, HomePage.class));
                finish();
            }
        });
        userProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomePage.this, UserProfile.class));
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void displayUserName(String uid){
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Registered Users").document(uid);
        // Fetch the user's name from Firestore
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Get the user's name from the document
                    String name = documentSnapshot.getString("Name");
                    tvGreetingUser.setText("Hello " + name);
                } else {
                    Toast.makeText(getApplicationContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterChallengesByState(String selectedState, String uid){
        // Show progress dialog
        progressDialog.show();
        // Clear the current list
        challengeList.clear();

        // Query Firestore for challenges with the selected state
        db.collection("Registered Users").document(uid)
                .collection("UserChallenges")
                .whereEqualTo("State", selectedState)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            ChallengeModel challengeModel = doc.toObject(ChallengeModel.class);
                            challengeList.add(challengeModel);
                        }
                        adapter.notifyDataSetChanged(); // Update the RecyclerView
                    } else {
                        Toast.makeText(HomePage.this, "Error retrieving challenges", Toast.LENGTH_SHORT).show();
                    }

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomePage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("State filter error: ",e.getMessage());
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
    }

    private void retrieveAndDisplayChallenges(String userUID) {

        db.collection("Registered Users").document(userUID)
                .collection("UserChallenges").orderBy("Title", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(error!=null){
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Log.e("firestore error: ", error.getMessage());
                    return;
                }

                if (value!=null){
                    for (DocumentSnapshot doc : value.getDocuments()){
                        ChallengeModel challengeModel = doc.toObject(ChallengeModel.class);
                        challengeList.add(challengeModel);
                    }
                }

                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                adapter.notifyDataSetChanged();

            }
        });
    }

    //To implement!! basically add new challenges to from the firestore to the user's new challenges
    private void backgroundListenerForNewChallenges(){
        db = FirebaseFirestore.getInstance();
        // Listening to changes in the "Challenges" collection
        db.collection("Challenges").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error);
                    return;
                }

                if (value != null) {
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String challengeID = doc.getId();
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            String userUID = firebaseUser.getUid();

                            // Check if the challenge already exists in the user's "UserChallenges"
                            db.collection("Registered Users").document(userUID)
                                    .collection("UserChallenges").document(challengeID)
                                    .get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot userChallengeDoc = task.getResult();
                                            boolean needsUpdate = false;

                                            if (userChallengeDoc.exists()) {
                                                // Check for discrepancies between global and user challenge
                                                if (!doc.getString("Title").equals(userChallengeDoc.getString("Title")) ||
                                                        !doc.getString("Description").equals(userChallengeDoc.getString("Description")) ||
                                                        !doc.getLong("Points").equals(userChallengeDoc.getLong("Points")) ||
                                                        !doc.getLong("Duration").equals(userChallengeDoc.getLong("Duration"))) {

                                                    needsUpdate = true;  // Mark the challenge as needing an update
                                                }
                                            } else {
                                                // Challenge doesn't exist, so add it as new
                                                needsUpdate = true;
                                            }

                                            if (needsUpdate) {
                                                // Add or update the user's challenge with the latest details from "Challenges"
                                                Map<String, Object> challengeData = new HashMap<>();
                                                challengeData.put("Title", doc.getString("Title"));
                                                challengeData.put("Description", doc.getString("Description"));
                                                challengeData.put("Points", doc.getLong("Points"));
                                                challengeData.put("Duration", doc.getLong("Duration"));
                                                challengeData.put("State", userChallengeDoc.exists() ? userChallengeDoc.getString("State") : "new");
                                                challengeData.put("StartDate", userChallengeDoc.exists() ? userChallengeDoc.getString("StartDate") : null);
                                                challengeData.put("CompletionDate", userChallengeDoc.exists() ? userChallengeDoc.getString("CompletionDate") : null);
                                                challengeData.put("Timer", userChallengeDoc.exists() ? userChallengeDoc.getLong("Timer") : 30);

                                                db.collection("Registered Users").document(userUID)
                                                        .collection("UserChallenges").document(challengeID)
                                                        .set(challengeData)
                                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Challenge updated/added"))
                                                        .addOnFailureListener(e -> Log.w("Firestore", "Error updating/adding challenge", e));
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });

    }

}

