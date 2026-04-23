package com.example.smartstock;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RemoveEmployee extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseFirestore db;
    private LinearLayout employeeCheckboxContainer;
    private Button removeemployeeButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_remove_employee);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        employeeCheckboxContainer = findViewById(R.id.employeeCheckboxContainer);
        removeemployeeButton = findViewById(R.id.removeemployeeButton);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(user.getUid()).collection("employees").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String employeeName = doc.getString("username");
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(employeeName);
                checkBox.setTag(doc.getId());
                employeeCheckboxContainer.addView(checkBox);
            }
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to fetch employees", Toast.LENGTH_SHORT).show();
        });

        removeemployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                for (int i = 0; i < employeeCheckboxContainer.getChildCount(); i++) {
                    CheckBox checkBox = (CheckBox) employeeCheckboxContainer.getChildAt(i);
                    if (checkBox.isChecked()) {
                        String documentId = (String) checkBox.getTag();
                        db.collection("users").document(user.getUid()).collection("employees").document(documentId).delete();
                        db.collection("users").document(documentId).delete();
                    }
                }
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RemoveEmployee.this, "Employees removed successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        progressBar.setVisibility(View.GONE);
    }
}