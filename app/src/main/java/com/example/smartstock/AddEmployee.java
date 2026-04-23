package com.example.smartstock;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddEmployee extends AppCompatActivity {

    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout, contactNumberLayout, addressLayout, salaryInputLayout;
    private EditText username, email, password, confirmPassword, contactNumber, address, salary;
    private Button addEmployeeButton;
    private Employee employee;
    private User user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser u;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_employee);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameLayout = findViewById(R.id.usernameInput);
        emailLayout = findViewById(R.id.emailInput);
        passwordLayout = findViewById(R.id.passwordInput);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordInput);
        contactNumberLayout = findViewById(R.id.contactNumberInput);
        addressLayout = findViewById(R.id.addressInput);
        salaryInputLayout = findViewById(R.id.salaryInput);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);

        addEmployeeButton = findViewById(R.id.addEmployeeButton);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        u = mAuth.getCurrentUser();

        username = usernameLayout.getEditText();
        email = emailLayout.getEditText();
        password = passwordLayout.getEditText();
        confirmPassword = confirmPasswordLayout.getEditText();
        contactNumber = contactNumberLayout.getEditText();
        address = addressLayout.getEditText();
        salary = salaryInputLayout.getEditText();


        db.collection("users").document(u.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                user = documentSnapshot.toObject(User.class);
            } else {
                Toast.makeText(AddEmployee.this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(AddEmployee.this, "Error getting user data", Toast.LENGTH_SHORT).show();
        });

        addEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (!isNetworkAvailable()) {
                    Toast.makeText(AddEmployee.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(username.getText().toString().isEmpty()) {
                    username.setError("Username cannot be empty");
                    username.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(email.getText().toString().isEmpty()) {
                    email.setError("Email cannot be empty");
                    email.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(password.getText().toString().length() < 6){
                    password.setError("Password must be at least 6 characters long");
                    password.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(password.getText().toString().isEmpty()) {
                    password.setError("Password cannot be empty");
                    password.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(confirmPassword.getText().toString().isEmpty()) {
                    confirmPassword.setError("Confirm password cannot be empty");
                    confirmPassword.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(!password.getText().toString().equals(confirmPassword.getText().toString())){
                    confirmPassword.setError("Passwords do not match");
                    confirmPassword.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(contactNumber.getText().toString().isEmpty()) {
                    contactNumber.setError("Contact number cannot be empty");
                    contactNumber.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(address.getText().toString().isEmpty()) {
                    address.setError("Address cannot be empty");
                    address.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(salary.getText().toString().isEmpty()) {
                    salary.setError("Salary cannot be empty");
                    salary.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(salary.getText().toString().equals("0")) {
                    salary.setError("Salary cannot be 0");
                    salary.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(salary.getText().toString().equals("0.0")) {
                    salary.setError("Salary cannot be 0.0");
                    salary.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(Double.parseDouble((salary.getText().toString()))<0){
                    salary.setError("Salary cannot be negative");
                    salary.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }


                String USERNAME = username.getText().toString();
                String EMAIL = email.getText().toString();
                String CONTACT_NUMBER = contactNumber.getText().toString();
                String ADDRESS = address.getText().toString();
                String PASSWORD = password.getText().toString();
                double SALARY = Double.parseDouble(salary.getText().toString());

                employee = new Employee(EMAIL, user.getCompanyName(), PASSWORD, USERNAME, CONTACT_NUMBER, ADDRESS,SALARY);
                employee.setBossID(u.getUid());

                Toast.makeText(AddEmployee.this, "Adding employee...", Toast.LENGTH_SHORT).show();
                mAuth.createUserWithEmailAndPassword(EMAIL, PASSWORD)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser newUser = mAuth.getCurrentUser();
                                    String newUserId = newUser.getUid();
                                    employee.setEmpId(newUserId);

                                    db.collection("users").document(newUserId).set(employee)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(AddEmployee.this, "Employee registered successfully", Toast.LENGTH_SHORT).show();

                                                db.collection("users").document(u.getUid())
                                                        .collection("employees")
                                                        .document(newUserId)
                                                        .set(employee)
                                                        .addOnSuccessListener(documentReference -> {
                                                            Toast.makeText(AddEmployee.this, "Employee information stored!", Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.GONE);
                                                            mAuth.signOut();
                                                            mAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword());
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            progressBar.setVisibility(View.GONE);
                                                            Toast.makeText(AddEmployee.this, "Employee info storage failed!", Toast.LENGTH_SHORT).show();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(AddEmployee.this, "Failed to save employee data in users collection", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(AddEmployee.this, "Employee registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
