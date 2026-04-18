package com.example.smartstock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;

public class forgotPassword extends AppCompatActivity {

    private EditText e;
    private Button send_link_button;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT); // Make nav bar transparent
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightNavigationBars(false); // Optional: makes nav buttons white if background is dark
        }

        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Link UI elements
        e = findViewById(R.id.Email);
        send_link_button = findViewById(R.id.send_link);

        // Set click listener for send link button
        send_link_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = e.getText().toString().trim();

                // Check if email field is empty
                if (email.isEmpty()) {
                    e.setError("Email cannot be empty");
                    e.requestFocus();
                    return;
                }

                // Send password reset email
                sendPasswordResetEmail(email);
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        // Send password reset email using Firebase Auth
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Success - Inform the user
                        Toast.makeText(forgotPassword.this, "Reset link sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(forgotPassword.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Failure - Show error message
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(forgotPassword.this, "Failed to send reset link: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
           });
}
}
