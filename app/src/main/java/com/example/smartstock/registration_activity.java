package com.example.smartstock;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Map;

public class registration_activity extends AppCompatActivity {

    private EditText fn,e,p,c,cn;
    private TextView login;
    private Button register;
    private FirebaseAuth mAuth;
    private User u;
    private ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(registration_activity.this,MainPage.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT); // Make nav bar transparent
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightNavigationBars(false); // Optional: makes nav buttons white if background is dark
        }

        setContentView(R.layout.activity_registeration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        fn=findViewById(R.id.editTextText2);
        e=findViewById(R.id.editTextText4);
        p=findViewById(R.id.editTextText5);
        c=findViewById(R.id.editTextText3);
        cn=findViewById(R.id.companyName);
        login=findViewById(R.id.textView5);
        register=findViewById(R.id.RegisterButton);
        progressBar = findViewById(R.id.progressbar);

        login.setOnClickListener(v->{
            Intent intent=new Intent(registration_activity.this,Login.class);
            startActivity(intent);
            finish();
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(registration_activity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                String email,password,full_name,confirm_password,company_name;
                email=e.getText().toString();
                password=p.getText().toString();
                full_name=fn.getText().toString();
                confirm_password=c.getText().toString();
                company_name=cn.getText().toString();

                u = new Admin(email,company_name,password,full_name);

                if(email.isEmpty()){
                    e.setError("Email cannot be empty");
                    e.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(password.isEmpty()){
                    p.setError("Password cannot be empty");
                    p.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }
                //check if email format is correct
                if(!email.contains("@") || !email.contains(".")){
                    e.setError("Invalid Email format");
                    e.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(full_name.isEmpty()){
                    fn.setError("Full Name cannot be empty");
                    fn.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(company_name.isEmpty()){
                    cn.setError("Company Name cannot be empty");
                    cn.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(confirm_password.isEmpty()){
                    c.setError("Confirm Password cannot be empty");
                    c.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(p.getText().toString().length() != c.getText().toString().length()){
                    Toast.makeText(registration_activity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }

                if(p.getText().toString().length() < 6){
                    Toast.makeText(registration_activity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();


                                    db.collection("users").document(user.getUid()).set(u).addOnSuccessListener(aVoid -> {
                                        Toast.makeText(registration_activity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(registration_activity.this,Login.class);
                                        startActivity(intent);
                                        finish();
                                            });
                                } else {
                                    Toast.makeText(registration_activity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
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