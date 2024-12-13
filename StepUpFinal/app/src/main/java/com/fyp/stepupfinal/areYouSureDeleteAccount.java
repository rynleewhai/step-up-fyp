package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;

public class areYouSureDeleteAccount extends AppCompatActivity {

    private Button wishToDeleteAccount, authenticateUser;
    private EditText inputPassword;
    private FirebaseAuth authProfile;
    private static final String TAG = "areYouSureDeleteAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_are_you_sure_delete_account);
        wishToDeleteAccount = findViewById(R.id.button_delete_account);
        wishToDeleteAccount.setEnabled(false);
        authenticateUser = findViewById(R.id.button_authenticate);
        inputPassword = findViewById(R.id.editTextPassword);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();



        authenticateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get text from editText
                String userInput = inputPassword.getText().toString();

                if (TextUtils.isEmpty(userInput)){
                    inputPassword.setError("enter password");
                    inputPassword.requestFocus();
                }
                else{
                    authenticateCredential(firebaseUser, userInput);
                }
            }
        });

    }

    private void authenticateCredential(FirebaseUser firebaseUser, String userEnteredPassword) {
        String userEmail = firebaseUser.getEmail();
        String userPassword = userEnteredPassword;

        Log.d(TAG, "Firestore error: "+userEmail+userPassword);

        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, userPassword);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(areYouSureDeleteAccount.this, "Authentication successful", Toast.LENGTH_LONG).show();
                authenticateUser.setEnabled(false);
                inputPassword.setEnabled(false);
                wishToDeleteAccount.setEnabled(true);
                wishToDeleteAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        redirectToDeleteAccount();
                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "Authentication failed, please enter password again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void redirectToDeleteAccount() {
        Intent intent = new Intent(areYouSureDeleteAccount.this, confirmDeleteAccount.class);
        startActivity(intent);
        finish();
    }
}