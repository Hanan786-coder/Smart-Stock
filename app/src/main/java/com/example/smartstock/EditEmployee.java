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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditEmployee extends AppCompatActivity {

    private TextInputLayout usernameLayout, contactNumberLayout, addressLayout, salaryInputLayout;
    private EditText username, contactNumber, address, salary;
    private Button updateEmployeeButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String employeeId;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_employee);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // View bindings
        usernameLayout = findViewById(R.id.usernameInput);
        contactNumberLayout = findViewById(R.id.contactNumberInput);
        addressLayout = findViewById(R.id.addressInput);
        salaryInputLayout = findViewById(R.id.salaryInput);
        updateEmployeeButton = findViewById(R.id.addEmployeeButton);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);// Button reused

        username = usernameLayout.getEditText();
        contactNumber = contactNumberLayout.getEditText();
        address = addressLayout.getEditText();
        salary = salaryInputLayout.getEditText();

        // Get employeeId from Intent extras
        employeeId = getIntent().getStringExtra("empId");
        if (employeeId == null || employeeId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid employee ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pre-fill employee info
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .document(currentUser.getUid())
                .collection("employees")
                .document(employeeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Employee existingEmployee = documentSnapshot.toObject(Employee.class);
                        if (existingEmployee != null) {
                            username.setText(existingEmployee.getUsername());
                            contactNumber.setText(existingEmployee.getContactNumber());
                            address.setText(existingEmployee.getAddress());
                            salary.setText(String.valueOf(existingEmployee.getSalary()));
                        }
                        progressBar.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, "Employee not found", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load employee data", Toast.LENGTH_SHORT).show();
                });

        // Handle update click
        updateEmployeeButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = username.getText().toString().trim();
            String contact = contactNumber.getText().toString().trim();
            String addr = address.getText().toString().trim();
            String salStr = salary.getText().toString().trim();

            if (name.isEmpty()) {
                username.setError("Username cannot be empty");
                username.requestFocus();
                return;
            }

            if (contact.isEmpty()) {
                contactNumber.setError("Contact number cannot be empty");
                contactNumber.requestFocus();
                return;
            }

            if (addr.isEmpty()) {
                address.setError("Address cannot be empty");
                address.requestFocus();
                return;
            }

            if (salStr.isEmpty()) {
                salary.setError("Salary cannot be empty");
                salary.requestFocus();
                return;
            }

            double salaryValue;
            try {
                salaryValue = Double.parseDouble(salStr);
                if (salaryValue <= 0) {
                    salary.setError("Salary must be greater than zero");
                    salary.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                salary.setError("Invalid salary format");
                salary.requestFocus();
                return;
            }

            // Update only relevant fields
            db.collection("users")
                    .document(currentUser.getUid())
                    .collection("employees")
                    .document(employeeId)
                    .update(
                            "username", name,
                            "contactNumber", contact,
                            "address", addr,
                            "salary", salaryValue
                    )
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to update employee", Toast.LENGTH_SHORT).show();
                    });
            progressBar.setVisibility(View.VISIBLE);
            db.collection("users")
                    .document(employeeId)
                    .update(
                            "username", name,
                            "contactNumber", contact,
                            "address", addr,
                            "salary", salaryValue
                    )
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Employee updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to update employee", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
