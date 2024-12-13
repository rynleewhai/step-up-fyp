package com.fyp.stepupfinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private TextView tvChangePassword, tvDeleteAccount, tvChangeEmail, tvLogOut;
    private ImageView ivBackToHome, ivBackToProfile;
    private CardView ChangeEmail, ChangePassword, LogOut;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        tvChangePassword = findViewById(R.id.textView_change_password);
        tvDeleteAccount = findViewById(R.id.textView_delete_account);
        ivBackToHome = findViewById(R.id.imageViewHome);
        tvChangeEmail = findViewById(R.id.textView_changeEmail);
        ivBackToProfile = findViewById(R.id.imageViewProfile);
        tvLogOut = findViewById(R.id.textView_logOut);
        ChangePassword = findViewById(R.id.cardView);
        ChangeEmail = findViewById(R.id.cardView2);
        LogOut = findViewById(R.id.cardView3);

        authProfile = FirebaseAuth.getInstance();

        ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ChangePassword.class));
                finish();
            }
        });

        ChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ChangeEmail.class));
                finish();
            }
        });

        tvChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ChangeEmail.class));
                finish();
            }
        });
        ivBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, HomePage.class));
                finish();
            }
        });
        ivBackToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, UserProfile.class));
                finish();
            }
        });
        tvDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, areYouSureDeleteAccount.class));
                finish();
            }
        });
        tvChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ChangePassword.class));
                finish();
            }
        });

        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authProfile.signOut();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }
        });

        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authProfile.signOut();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}