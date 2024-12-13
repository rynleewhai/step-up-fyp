package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.OAuthProvider;

public class ForgotPassword extends AppCompatActivity {

    private Button resetButton;
    private EditText etEmail;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final static String TAG = "ForgotPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        resetButton = findViewById(R.id.button_resetPassword);
        etEmail = findViewById(R.id.et_email);
        progressBar = findViewById(R.id.progressBarPasswordReset);
        progressBar.setVisibility(View.GONE);
        authProfile = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString();

                if(TextUtils.isEmpty(email)){
                    etEmail.setError("Email is required");
                    etEmail.requestFocus();
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_LONG).show();
                    etEmail.setError("Valid email is required");
                    etEmail.requestFocus();
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(email);
                }
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void resetPassword(String email) {
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "Please check your inbox for password reset link!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
                    // Clear stack to prevent user from coming back to UserProfileActivity on pressing back button after logging out
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();  // Close UserProfileActivity
                }
                else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        etEmail.setError("User does not exist or is no longer valid. Please register again.");
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}