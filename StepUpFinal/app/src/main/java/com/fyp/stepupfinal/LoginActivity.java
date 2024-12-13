package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextLoginEmail, editTextLoginPassword;
    private TextView tvForgotPassword;
    private ImageView imageViewShowHidePW;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        editTextLoginEmail = findViewById(R.id.et_login_email);
        editTextLoginPassword = findViewById(R.id.et_login_password);
        tvForgotPassword = findViewById(R.id.tv_ForgotPassword);
        progressBar = findViewById(R.id.progressBarLogin);
        progressBar.setVisibility(View.GONE);

        authProfile = FirebaseAuth.getInstance();

        //forgot password
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });
        //show hide password using icon
        imageViewShowHidePW = findViewById(R.id.imageView_show_hide_password);
        imageViewShowHidePW.setImageResource(R.drawable.ic_show_pwd);
        imageViewShowHidePW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save current cursor position
                int cursorPosition = editTextLoginPassword.getSelectionStart();

                if (!editTextLoginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    // If the password is not visible, make it visible
                    editTextLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePW.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    // If the password is visible, hide it
                    editTextLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHidePW.setImageResource(R.drawable.ic_show_pwd);
                }

                // Restore the cursor position
                editTextLoginPassword.setSelection(cursorPosition);
            }
        });


        Button LoginButton = findViewById(R.id.button_login);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPassword = editTextLoginPassword.getText().toString();

                if (TextUtils.isEmpty(textEmail)){
                    //Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Email is required");
                    editTextLoginEmail.requestFocus();
                }
                else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    //Toast.makeText(RegisterActivity.this, "Please enter a valid email", Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Valid email is required");
                    editTextLoginEmail.requestFocus();
                }
                else if (TextUtils.isEmpty(textPassword)){
                    //Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_LONG).show();
                    editTextLoginPassword.setError("Password is required");
                    editTextLoginPassword.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail,textPassword);
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loginUser(String textEmail, String textPassword) {
        authProfile.signInWithEmailAndPassword(textEmail, textPassword)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login success.",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "successfully signed in");
                            startActivity(new Intent(LoginActivity.this, HomePage.class));
                            finish();
                        }
                        else
                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException e) {
                                editTextLoginEmail.setError("User does not exist or is no longer valid. Please try again.");
                                editTextLoginEmail.requestFocus();
                            }
                            catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextLoginEmail.setError("Invalid credentials. Kindly check and re-enter.");
                                editTextLoginEmail.requestFocus();
                            }
                            catch (Exception e) {
                                Log.e(TAG, "failed to sign in", task.getException());
                                Toast.makeText(LoginActivity.this, "Login failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
    //check if user is logged in already
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
            startActivity(new Intent(LoginActivity.this, HomePage.class));
            finish();
        }
    }

}