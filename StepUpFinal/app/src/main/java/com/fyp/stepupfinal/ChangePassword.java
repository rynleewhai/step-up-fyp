package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private EditText editText_CurrentPassword, editText_NewPassword, editText_ConfirmNewPassword;
    private Button buttonAuthenticate, buttonChangePassword;
    private ProgressBar progressBar;
    private ImageView PeekAtPassword;
    private String CurrentUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        editText_CurrentPassword = findViewById(R.id.editText_CurrentPassword);
        editText_NewPassword = findViewById(R.id.editText_NewPassword);
        editText_ConfirmNewPassword = findViewById(R.id.editText_ConfirmNewPassword);
        buttonAuthenticate = findViewById(R.id.button_authenticate);
        buttonChangePassword = findViewById(R.id.button_change_password);
        PeekAtPassword = findViewById((R.id.imageView_eye));
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //disable components for New password flow
        editText_NewPassword.setEnabled(false);
        editText_ConfirmNewPassword.setEnabled(false);
        buttonChangePassword.setEnabled(false);

        PeekAtPassword.setImageResource(R.drawable.ic_show_pwd);
        PeekAtPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save current cursor position
                int cursorPosition = editText_NewPassword.getSelectionStart();

                if (!editText_NewPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    // If the password is not visible, make it visible
                    editText_NewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    PeekAtPassword.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    // If the password is visible, hide it
                    editText_NewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    PeekAtPassword.setImageResource(R.drawable.ic_show_pwd);
                }

                // Restore the cursor position
                editText_NewPassword.setSelection(cursorPosition);
            }
        });

        if(firebaseUser == null){
            Toast.makeText(ChangePassword.this, "Something went wrong! User information not available.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePassword.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            reAuthenticateUSer(firebaseUser);
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // verify user before changing password
    private void reAuthenticateUSer(FirebaseUser firebaseUser) {
        buttonAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrentUserPassword =editText_CurrentPassword.getText().toString();
                if(TextUtils.isEmpty(CurrentUserPassword)){
                    Toast.makeText(ChangePassword.this, "Password is required!", Toast.LENGTH_LONG).show();
                    editText_CurrentPassword.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);

                    //verify user here
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),CurrentUserPassword);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                editText_CurrentPassword.setEnabled(false);
                                buttonAuthenticate.setEnabled(false);
                                editText_NewPassword.setEnabled(true);
                                editText_ConfirmNewPassword.setEnabled(true);
                                buttonChangePassword.setEnabled(true);
                                Toast.makeText(ChangePassword.this, "User verified.", Toast.LENGTH_LONG).show();

                                buttonChangePassword.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        changeUserPassword(firebaseUser, CurrentUserPassword);
                                    }
                                });
                            }
                            //verification failed
                            else {
                                try{
                                    throw task.getException();

                                } catch (Exception e){
                                    Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changeUserPassword(FirebaseUser firebaseUser, String CurrentUserPassword) {
        String NewPassword = editText_NewPassword.getText().toString();
        String ConfirmNewPassword = editText_ConfirmNewPassword.getText().toString();
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&+={*}]).{6,}$";

        if(TextUtils.isEmpty(NewPassword)){
            Toast.makeText(ChangePassword.this, "Enter New Password", Toast.LENGTH_LONG).show();
            editText_NewPassword.requestFocus();
        }
        else if(TextUtils.isEmpty(ConfirmNewPassword)){
            Toast.makeText(ChangePassword.this, "Re-Enter New Password", Toast.LENGTH_LONG).show();
            editText_ConfirmNewPassword.requestFocus();
        }
        else if(!NewPassword.matches(ConfirmNewPassword)){
            Toast.makeText(ChangePassword.this, "Passwords do not match, please re-enter!", Toast.LENGTH_LONG).show();
            editText_NewPassword.setError("Password do not match");
            editText_ConfirmNewPassword.requestFocus();
        }
        else if(NewPassword.matches(CurrentUserPassword)){
            Toast.makeText(ChangePassword.this, "New Password cannot be the \nsame as old Password", Toast.LENGTH_LONG).show();
            editText_NewPassword.setError("Change Password");
            editText_NewPassword.requestFocus();
        }
        else if (NewPassword.length() < 6){
            Toast.makeText(ChangePassword.this, "Password must be more than 6 characters", Toast.LENGTH_LONG).show();
            editText_NewPassword.setError("Password too short");
            editText_NewPassword.requestFocus();
        }
        else if (!NewPassword.matches(passwordPattern)) {
            Toast.makeText(ChangePassword.this, "Password must be alphanumeric and contain a special symbol !@#$%^&+={*}", Toast.LENGTH_LONG).show();
            editText_NewPassword.setError("Password not secure enough");
            editText_NewPassword.requestFocus();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            firebaseUser.updatePassword(NewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePassword.this, "Password Successfully updated", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ChangePassword.this, UserProfile.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        try{
                            throw task.getException();

                        } catch (Exception e){
                            Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}