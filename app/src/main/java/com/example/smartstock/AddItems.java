package com.example.smartstock;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class AddItems extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseFirestore db;
    private TextInputLayout itemIDLayout, itemNameLayout, itemDescriptionLayout, itemCategoryLayout, unitWeightLayout, costPriceLayout, sellingPriceLayout, itemCodeLayout;
    private EditText itemIDInput, itemNameInput, itemDescriptionInput, itemCategoryInput, unitWeightInput, costPriceInput, sellingPriceInput, itemCodeInput;
    private ProgressBar progressBar;
    private MaterialButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_items);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {

            user = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();

            itemIDLayout = findViewById(R.id.itemIDInput);
            itemNameLayout = findViewById(R.id.itemNameInput);
            itemDescriptionLayout = findViewById(R.id.itemDescriptionInput);
            itemCategoryLayout = findViewById(R.id.itemCategoryInput);
            unitWeightLayout = findViewById(R.id.unitWeightInput);
            costPriceLayout = findViewById(R.id.costPriceInput);
            sellingPriceLayout = findViewById(R.id.sellingPriceInput);
            itemCodeLayout = findViewById(R.id.itemCodeInput);

            itemIDInput = itemIDLayout.getEditText();
            itemNameInput = itemNameLayout.getEditText();
            itemDescriptionInput = itemDescriptionLayout.getEditText();
            itemCategoryInput = itemCategoryLayout.getEditText();
            unitWeightInput = unitWeightLayout.getEditText();
            costPriceInput = costPriceLayout.getEditText();
            sellingPriceInput = sellingPriceLayout.getEditText();
            itemCodeInput = itemCodeLayout.getEditText();
            addButton = findViewById(R.id.addItemButton);
            progressBar = findViewById(R.id.pgbar);
            progressBar.setVisibility(View.GONE);


            addButton.setOnClickListener(v -> validateAndAddItem());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void validateAndAddItem() {
        progressBar.setVisibility(View.VISIBLE);
        try {
            if (!isNetworkAvailable()) {
                Toast.makeText(AddItems.this, "No internet connection", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if (TextUtils.isEmpty(itemIDInput.getText())) {
                itemIDInput.setError("Item ID is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(itemNameInput.getText())) {
                itemNameInput.setError("Item name is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(itemDescriptionInput.getText())) {
                itemDescriptionInput.setError("Item description is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(itemCategoryInput.getText())) {
                itemCategoryInput.setError("Item category is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(unitWeightInput.getText())) {
                unitWeightInput.setError("Unit weight is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(costPriceInput.getText())) {
                costPriceInput.setError("Cost price is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(sellingPriceInput.getText())) {
                sellingPriceInput.setError("Selling price is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(itemCodeInput.getText())) {
                itemCodeInput.setError("Item code is required");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (Double.parseDouble(unitWeightInput.getText().toString()) <= 0) {
                unitWeightInput.setError("Unit weight must be greater than 0");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (Double.parseDouble(costPriceInput.getText().toString()) <= 0) {
                costPriceInput.setError("Cost price must be greater than 0");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (Double.parseDouble(sellingPriceInput.getText().toString()) <= 0) {
                sellingPriceInput.setError("Selling price must be greater than 0");
                progressBar.setVisibility(View.GONE);
                return;
            }
            if (Double.parseDouble(itemCodeInput.getText().toString()) <= 0) {
                itemCodeInput.setError("Item code must be greater than 0");
                progressBar.setVisibility(View.GONE);
                return;
            }

            db.collection("users").document(user.getUid()).collection("items").document(itemIDInput.getText().toString()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    itemIDInput.setError("Item ID already exists");
                    itemIDInput.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                } else {


                    Item item = new Item(
                            parseInt(itemIDInput.getText().toString()),
                            itemNameInput.getText().toString(),
                            itemDescriptionInput.getText().toString(),
                            itemCategoryInput.getText().toString(),
                            Double.parseDouble(unitWeightInput.getText().toString()),
                            Double.parseDouble(costPriceInput.getText().toString()),
                            Double.parseDouble(sellingPriceInput.getText().toString()),
                            itemCodeInput.getText().toString()
                    );
                    item.setDate(Timestamp.now());

                    db.collection("users").document(user.getUid())
                            .collection("items").document(itemIDInput.getText().toString())
                            .set(item)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item_added, null);
                                AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                                        .setView(dialogView)
                                        .setCancelable(false)
                                        .create();

                                dialog.show();

                                Button yesBtn = dialogView.findViewById(R.id.yesBtn);
                                Button noBtn = dialogView.findViewById(R.id.noBtn);

                                yesBtn.setOnClickListener(v -> {
                                    dialog.dismiss();
                                    progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(this, AddStock.class);
                                    intent.putExtra("itemID", itemIDInput.getText().toString());
                                    startActivity(intent);
                                });

                                noBtn.setOnClickListener(v -> {
                                    dialog.dismiss();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error adding item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error checking item ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } catch (NumberFormatException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void clearFields() {
        itemIDInput.setText("");
        itemNameInput.setText("");
        itemDescriptionInput.setText("");
        itemCategoryInput.setText("");
        unitWeightInput.setText("");
        costPriceInput.setText("");
        sellingPriceInput.setText("");
        itemCodeInput.setText("");
        progressBar.setVisibility(View.GONE);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}