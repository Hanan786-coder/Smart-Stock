package com.example.smartstock;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AddWarehouse extends AppCompatActivity {

    private TextInputLayout warehouseIdInput, warehouseNameInput, warehouseLocationInput;
    private EditText warehouseIdEditText, warehouseNameEditText, warehouseLocationEditText;
    private RadioGroup employeeRadioGroup;
    private ProgressBar progressBar;
    private LinearLayout stockContainer;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private List<Employee> employeesList = new ArrayList<>();
    private List<Stock> availableStocks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_warehouse);

        // Initialize views
        initializeViews();

        // Initialize Firebase
        initializeFirebase();
        progressBar.setVisibility(View.VISIBLE);
        // Load data
        loadData();
        progressBar.setVisibility(View.GONE);

        // Set click listener for add button
        progressBar.setVisibility(View.VISIBLE);
        setupAddButton();
        progressBar.setVisibility(View.GONE);
    }

    private void initializeViews() {
        warehouseIdInput = findViewById(R.id.warehouseIdInput);
        warehouseNameInput = findViewById(R.id.warehouseNameInput);
        warehouseLocationInput = findViewById(R.id.warehouseLocationInput);
        employeeRadioGroup = findViewById(R.id.employeeContainer);
        stockContainer = findViewById(R.id.stockContainer);
        warehouseIdEditText = warehouseIdInput.getEditText();
        warehouseNameEditText = warehouseNameInput.getEditText();
        warehouseLocationEditText = warehouseLocationInput.getEditText();
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadData() {
        showToast("Loading data...");
        loadEmployees();
        loadAvailableStocks();
    }

    private void setupAddButton() {
        findViewById(R.id.addWarehouseButton).setOnClickListener(v -> {
            showToast("Validating inputs...");
            validateAndAddWarehouse();
        });
    }

    private void loadEmployees() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(user.getUid()).collection("employees")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    employeesList.clear();
                    employeeRadioGroup.removeAllViews();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Employee employee = doc.toObject(Employee.class);
                        if (employee != null) {
                            employeesList.add(employee);

                            RadioButton radioButton = new RadioButton(this);
                            radioButton.setText(employee.getUsername());
                            radioButton.setTag(employee.getEmpId());
                            employeeRadioGroup.addView(radioButton);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    showToast("Employees loaded successfully");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to load employees");
                    logError("Error loading employees", e);
                });
    }

    private void loadAvailableStocks() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(user.getUid()).collection("stocks")
                .whereEqualTo("addedToWarehouse", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    availableStocks.clear();
                    stockContainer.removeAllViews();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Stock stock = doc.toObject(Stock.class);
                        if (stock != null && stock.getItem() != null) {
                            availableStocks.add(stock);

                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setText(stock.getItem().getItemName() + " (Qty: " + stock.getQuantity() + ")");
                            checkBox.setTag(doc.getId());
                            stockContainer.addView(checkBox);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    showToast("Stocks loaded successfully");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to load stocks");
                    logError("Error loading stocks", e);
                });
    }

    private void validateAndAddWarehouse() {
        progressBar.setVisibility(View.VISIBLE);

        // Get input values
        String idStr = warehouseIdEditText.getText().toString().trim();
        String name = warehouseNameEditText.getText().toString().trim();
        String location = warehouseLocationEditText.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(idStr, name, location)) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        int warehouseId = Integer.parseInt(idStr);

        // Check if manager is selected
        Employee manager = getSelectedManager();
        if (manager == null) {
            showToast("Please select a manager");
            return;
        }

        // Get selected stocks
        List<Stock> selectedStocks = getSelectedStocks();
        List<String> selectedStockDocIds = getSelectedStockDocIds(selectedStocks);

        // Check warehouse ID uniqueness
        checkWarehouseIdUniqueness(warehouseId, name, location, manager, selectedStocks, selectedStockDocIds);
    }

    private boolean validateInputs(String idStr, String name, String location) {
        warehouseIdInput.setError(null);
        warehouseNameInput.setError(null);
        warehouseLocationInput.setError(null);

        if (idStr.isEmpty()) {
            warehouseIdInput.setError("Warehouse ID is required");
            return false;
        }

        try {
            Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            warehouseIdInput.setError("Invalid warehouse ID");
            return false;
        }

        if (name.isEmpty()) {
            warehouseNameInput.setError("Warehouse name is required");
            return false;
        }

        if (location.isEmpty()) {
            warehouseLocationInput.setError("Location is required");
            return false;
        }

        return true;
    }

    private Employee getSelectedManager() {
        int selectedEmployeeId = employeeRadioGroup.getCheckedRadioButtonId();
        if (selectedEmployeeId == -1) {
            return null;
        }

        RadioButton selectedRadio = employeeRadioGroup.findViewById(selectedEmployeeId);
        String empId = (String) selectedRadio.getTag();
        return findEmployeeById(empId);
    }

    private List<Stock> getSelectedStocks() {
        List<Stock> selectedStocks = new ArrayList<>();
        for (int i = 0; i < stockContainer.getChildCount(); i++) {
            View view = stockContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    String docId = (String) checkBox.getTag();
                    Stock stock = findStockByDocId(docId);
                    if (stock != null) {
                        selectedStocks.add(stock);
                    }
                }
            }
        }
        return selectedStocks;
    }

    private List<String> getSelectedStockDocIds(List<Stock> selectedStocks) {
        List<String> docIds = new ArrayList<>();
        for (Stock stock : selectedStocks) {
            docIds.add(String.valueOf(stock.getStockID()));
        }
        return docIds;
    }

    private void checkWarehouseIdUniqueness(int warehouseId, String name, String location,
                                            Employee manager, List<Stock> selectedStocks,
                                            List<String> selectedStockDocIds) {
        showToast("Checking warehouse ID...");

        db.collection("users").document(user.getUid()).collection("warehouses")
                .whereEqualTo("warehouseId", warehouseId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        warehouseIdInput.setError("Warehouse ID already exists");
                        showToast("Warehouse ID already in use");
                    } else {
                        showToast("Creating warehouse...");
                        createWarehouse(warehouseId, name, location, manager, selectedStocks, selectedStockDocIds);
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error checking warehouse ID");
                    logError("Error checking warehouse ID", e);
                });
    }

    private void createWarehouse(int warehouseId, String name, String location,
                                 Employee manager, List<Stock> selectedStocks,
                                 List<String> selectedStockDocIds) {
        Warehouse warehouse = new Warehouse(
                warehouseId,
                name,
                location,
                manager,
                new ArrayList<>(selectedStocks)
        );

        db.collection("users").document(user.getUid())
                .collection("warehouses")
                .document(String.valueOf(warehouseId))
                .set(warehouse)
                .addOnSuccessListener(aVoid -> {
                    if (!selectedStockDocIds.isEmpty()) {
                        showToast("Updating stock statuses...");
                        updateStockStatuses(selectedStockDocIds);
                    } else {
                        showToast("Warehouse created successfully!");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to create warehouse");
                    logError("Error creating warehouse", e);
                });
    }

    private void updateStockStatuses(List<String> stockDocIds) {
        AtomicInteger successCount = new AtomicInteger(0);
        int totalStocks = stockDocIds.size();

        for (String docId : stockDocIds) {
            db.collection("users").document(user.getUid())
                    .collection("stocks")
                    .document(docId)
                    .update("addedToWarehouse", true)
                    .addOnSuccessListener(aVoid -> {
                        int completed = successCount.incrementAndGet();
                        if (completed == totalStocks) {
                            showToast("Warehouse created with " + totalStocks + " stocks!");
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Error updating some stocks");
                        logError("Error updating stock status for ID: " + docId, e);
                        if (successCount.incrementAndGet() == totalStocks) {
                            showToast("Warehouse created with some stock updates failing");
                            finish();
                        }
                    });
        }
    }

    private Employee findEmployeeById(String empId) {
        for (Employee employee : employeesList) {
            if (employee.getEmpId() != null && employee.getEmpId().equals(empId)) {
                return employee;
            }
        }
        return null;
    }

    private Stock findStockByDocId(String docId) {
        for (Stock stock : availableStocks) {
            if (String.valueOf(stock.getStockID()).equals(docId)) {
                return stock;
            }
        }
        return null;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void logError(String message, Exception e) {
        Log.e("AddWarehouse", message, e);
    }
}