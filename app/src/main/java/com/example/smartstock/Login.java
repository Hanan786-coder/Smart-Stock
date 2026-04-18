package com.example.smartstock;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText e,p;
    private Button login;
    private TextView signup,forgot_password;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(Login.this,MainPage.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        EdgeToEdge.enable(this);

        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT); // Make nav bar transparent
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightNavigationBars(false); // Optional: makes nav buttons white if background is dark
        }

        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        e=findViewById(R.id.Email);
        p=findViewById(R.id.editTextText);
        login=findViewById(R.id.LoginButton);
        signup=findViewById(R.id.signup);
        forgot_password=findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.progressbar);

        signup.setOnClickListener(v->{
            Intent intent=new Intent(Login.this,registration_activity.class);
            startActivity(intent);
        });

        forgot_password.setOnClickListener(v->{
            Intent intent=new Intent(Login.this,forgotPassword.class);
            startActivity(intent);
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(Login.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                login.setEnabled(false);  // prevent multiple clicks

                String email = e.getText().toString().trim();
                String password = p.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    e.setError("Email cannot be empty");
                    e.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(!email.contains("@") || !email.contains(".")){
                    e.setError("Invalid Email format");
                    e.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    p.setError("Password cannot be empty");
                    p.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }


                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                login.setEnabled(true);
                                try {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                                                    if (documentSnapshot.exists()) {
                                                        String role = documentSnapshot.getString("role");
                                                        if (role.equals("Administrator")) {
                                                            Toast.makeText(Login.this, "Admin Login Successful.", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(Login.this, MainPage.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(Login.this, "Employee Login Successful.", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(Login.this, MainPage.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    } else {
                                                        Toast.makeText(Login.this, "Login failed. User not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(Login.this, "Error getting user data", Toast.LENGTH_SHORT).show();
                                                    System.out.println("Error getting user data" + e.getMessage());
                                                });
                                    } else {
                                        Toast.makeText(Login.this, "Login failed. Invalid Email or Password", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception ex) {
                                    Toast.makeText(Login.this, "Login Failed: "+ex.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });



    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}