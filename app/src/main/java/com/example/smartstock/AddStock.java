package com.example.smartstock;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddStock extends AppCompatActivity {

    private Item item = new Item();
    private TextInputLayout itemIDLayout, quantityLayout;
    private TextView availableQuan,Heading,subHeading;
    private EditText itemID, quantity;
    private Button addStockButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_stock);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        itemIDLayout = findViewById(R.id.itemIDInput);
        quantityLayout = findViewById(R.id.stockQuantityInput);
        Heading = findViewById(R.id.header);
        subHeading = findViewById(R.id.subHeader);
        itemID = itemIDLayout.getEditText();
        quantity = quantityLayout.getEditText();

        String itemIDString = getIntent().getStringExtra("itemID");
        itemID.setText(itemIDString);

        addStockButton = findViewById(R.id.addStockButton);
        availableQuan = findViewById(R.id.availableQuan);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            progressBar.setVisibility(View.VISIBLE);
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String userId;
                        if("Administrator".equals(role)) {
                            userId = user.getUid();
                        } else {
                            userId = documentSnapshot.getString("bossID");
                            addStockButton.setVisibility(View.GONE);
                            quantityLayout.setVisibility(View.GONE);
                            Heading.setText("View Stock");
                            subHeading.setText("View the available stock of the item");
                        }
                        db.collection("users").document(userId).collection("stocks").document(itemIDString).get().addOnSuccessListener(docSnapshot -> {
                            try {
                                availableQuan.setText(docSnapshot.getLong("quantity").toString());
                            } catch (Exception e) {
                                availableQuan.setText("N/A");
                            }
                            progressBar.setVisibility(View.GONE);
                        }).addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            availableQuan.setText("0");
                        });
                    }
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddStock.this, "Failed to retrieve user", Toast.LENGTH_SHORT).show();
                });

        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                itemIDLayout.setError(null);
                quantityLayout.setError(null);

                if (itemID.getText().toString().isEmpty()) {
                    itemIDLayout.setError("Item ID is required");
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (quantity.getText().toString().isEmpty()) {
                    quantityLayout.setError("Quantity is required");
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                int addedQuantity = Integer.parseInt(quantity.getText().toString());
                if (addedQuantity <= 0) {
                    quantityLayout.setError("Quantity must be greater than 0");
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String userId = "Administrator".equals(role) ? user.getUid() : documentSnapshot.getString("bossID");

                        db.collection("users").document(userId)
                                .collection("items").document(itemID.getText().toString())
                                .get()
                                .addOnSuccessListener(itemSnapshot -> {
                                    if (itemSnapshot.exists()) {
                                        item.setItemID(Integer.parseInt(itemID.getText().toString()));
                                        item.setItemName(itemSnapshot.getString("itemName"));
                                        item.setItemDescription(itemSnapshot.getString("itemDescription"));
                                        item.setItemCategory(itemSnapshot.getString("itemCategory"));
                                        item.setUnitWeight(itemSnapshot.getDouble("unitWeight"));
                                        item.setCostPrice(itemSnapshot.getDouble("costPrice"));
                                        item.setSellingPrice(itemSnapshot.getDouble("sellingPrice"));
                                        item.setItemCode(itemSnapshot.getString("itemCode"));

                                        //flexible date parsing
                                        Timestamp dateString = itemSnapshot.getTimestamp("date");
                                        if (dateString != null) {
                                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                                            item.setDate(dateString.toDate());
                                        }

                                        //get current stock and update
                                        db.collection("users").document(userId)
                                                .collection("stocks").document(itemID.getText().toString())
                                                .get()
                                                .addOnSuccessListener(stockSnapshot -> {
                                                    int existingQuantity = 0;
                                                    if (stockSnapshot.exists()) {
                                                        Long q = stockSnapshot.getLong("quantity");
                                                        if (q != null) existingQuantity = q.intValue();
                                                    }

                                                    Stock stock = new Stock();
                                                    stock.setItem(item);
                                                    stock.setStockID(item.getItemID());
                                                    stock.setQuantity(existingQuantity + addedQuantity);
                                                    stock.setLastUpdate();

                                                    db.collection("users").document(userId)
                                                            .collection("stocks").document(itemID.getText().toString())
                                                            .set(stock)
                                                            .addOnSuccessListener(docRef -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(AddStock.this, "Stock added successfully", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(AddStock.this, "Failed to add stock", Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(AddStock.this, "Failed to retrieve stock", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(AddStock.this, "Item not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(AddStock.this, "Failed to retrieve item", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddStock.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddStock.this, "Failed to retrieve user", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
