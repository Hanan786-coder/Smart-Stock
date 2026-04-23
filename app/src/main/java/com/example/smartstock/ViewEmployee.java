package com.example.smartstock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ViewEmployee extends AppCompatActivity {

    private CardView addEmployeeCard;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private LinearLayout employeeContainer;
    private TextView Employees,empDet;
    private ProgressBar progressBar;

    //Reload when back
    @Override
    protected void onResume() {
        super.onResume();
        employeeContainer.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);
        loadEmployees();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_employee);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addEmployeeCard = findViewById(R.id.addEmployeeCard);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);

        addEmployeeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewEmployee.this, AddEmployee.class);
                startActivity(intent);
            }
        });
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        employeeContainer = findViewById(R.id.employeeContainer);
        Employees = findViewById(R.id.Employees);
        empDet = findViewById(R.id.empDet);

        employeeContainer.setVisibility(View.VISIBLE);
        Employees.setVisibility(View.VISIBLE);
        empDet.setVisibility(View.GONE);
    }

    private void loadEmployees() {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("employees")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Employee employee = doc.toObject(Employee.class);
                        addEmployeeCard(employee);
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ViewEmployee.this, "Failed to load employees", Toast.LENGTH_SHORT).show();
                });
    }

    private void addEmployeeCard(Employee employee) {
        View card = LayoutInflater.from(this).inflate(R.layout.employee_card, employeeContainer, false);
        //Toast.makeText(ViewEmployee.this, "Card Inflated", Toast.LENGTH_SHORT).show();
        TextView username = card.findViewById(R.id.usernameText);
        TextView email = card.findViewById(R.id.emailText);
        TextView company = card.findViewById(R.id.companyText);
        TextView role = card.findViewById(R.id.roleText);

        username.setText(employee.getUsername());
        email.setText(employee.getEmail());
        company.setText(employee.getCompanyName());
        role.setText(employee.getRole());
        employeeContainer.addView(card);

        //Toast.makeText(ViewEmployee.this, "Card Added", Toast.LENGTH_SHORT).show();
        card.setOnClickListener(v -> {
            // Prepare data
            Bundle bundle = new Bundle();
            bundle.putString("username", employee.getUsername());
            bundle.putString("email", employee.getEmail());
            bundle.putString("company", employee.getCompanyName());
            bundle.putString("role", employee.getRole());
            bundle.putString("address", employee.getAddress());
            bundle.putString("contact", employee.getContactNumber());
            bundle.putString("salary", String.valueOf(employee.getSalary()));
            bundle.putString("empId", employee.getEmpId());

            // Create fragment and set arguments
            EmpDetail empDetailFragment = new EmpDetail();
            empDetailFragment.setArguments(bundle);

            loadFragment(empDetailFragment);
        });

    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        employeeContainer.setVisibility(View.GONE);
        Employees.setVisibility(View.GONE);
        empDet.setVisibility(View.VISIBLE);
        findViewById(R.id.frameLayout).setVisibility(View.VISIBLE);
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            employeeContainer.setVisibility(View.VISIBLE);
            Employees.setVisibility(View.VISIBLE);
            empDet.setVisibility(View.GONE);
            findViewById(R.id.frameLayout).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

}