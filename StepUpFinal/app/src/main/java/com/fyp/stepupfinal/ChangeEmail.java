package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeEmail extends AppCompatActivity {
    private EditText etCurrentEmail, etPassword, etNewEmail;
    private Button buttonAuthenticate, buttonUpdate;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private String userCurrentEmail, userNewEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_email);

        etCurrentEmail = findViewById(R.id.editText_currentEmail);
        etPassword = findViewById(R.id.editText_password);
        etNewEmail = findViewById(R.id.editText_newEmail);
        buttonAuthenticate = findViewById(R.id.button_authenticate);
        buttonUpdate = findViewById(R.id.button_update);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        //disable update email functionality first
        buttonUpdate.setEnabled(false);
        etNewEmail.setEnabled(false);

        //firebase stuff
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        //display current email of user in editText
        userCurrentEmail = firebaseUser.getEmail();
        etCurrentEmail.setText(userCurrentEmail);

        if(firebaseUser == null){
            Toast.makeText(ChangeEmail.this, "Something went wrong! User information not available.", Toast.LENGTH_LONG).show();
        }
        else{
            reAuthenticateUser(firebaseUser, userCurrentEmail);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //verify user before updating email
    private void reAuthenticateUser(FirebaseUser firebaseUser, String userCurrentEmail){
        buttonAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //obtain password from editText
                String userPassword = etPassword.getText().toString();

                if (TextUtils.isEmpty(userPassword)) {
                    //Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_LONG).show();
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    AuthCredential credential = EmailAuthProvider.getCredential(userCurrentEmail, userPassword);
                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               progressBar.setVisibility(View.GONE);
                               Toast.makeText(ChangeEmail.this, "Password verified. Please proceed to update email.", Toast.LENGTH_LONG).show();

                               buttonAuthenticate.setEnabled(false);
                               buttonUpdate.setEnabled(true);
                               etPassword.setEnabled(false);
                               etNewEmail.setEnabled(true);

                               buttonUpdate.setBackgroundTintList(ContextCompat.getColorStateList(ChangeEmail.this, R.color.blue ));
                               buttonUpdate.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       userNewEmail = etNewEmail.getText().toString();
                                       if(TextUtils.isEmpty(userNewEmail)){
                                           etNewEmail.setError("Please enter email.");
                                           etNewEmail.requestFocus();
                                       }
                                       else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()){
                                           //Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_LONG).show();
                                           etNewEmail.setError("Valid email is required");
                                           etNewEmail.requestFocus();
                                       }
                                       else if (userNewEmail.matches(userCurrentEmail)){
                                           //Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_LONG).show();
                                           etNewEmail.setError("Please enter a new email.");
                                           etNewEmail.requestFocus();
                                       }
                                       else{
                                           progressBar.setVisibility(View.VISIBLE);
                                           updateEmail(firebaseUser);
                                       }
                                   }
                               });
                           }
                           else{
                               progressBar.setVisibility(View.GONE);
                               try{
                                   throw task.getException();
                               } catch (Exception e){
                                   Toast.makeText(ChangeEmail.this, e.getMessage(), Toast.LENGTH_LONG).show();
                               }
                           }
                        }
                    });
                }
            }
        });
    }
    private void updateEmail(FirebaseUser firebaseUser) {
        // Replace updateEmail with verifyBeforeUpdateEmail
        firebaseUser.verifyBeforeUpdateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Inform the user to check their new email for verification
                    Toast.makeText(ChangeEmail.this, "A verification email has been sent to your new email address. Please verify to complete the update.", Toast.LENGTH_LONG).show();

                    // Redirect to the user profile after sending verification email
                    Intent intent = new Intent(ChangeEmail.this, UserProfile.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(ChangeEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}