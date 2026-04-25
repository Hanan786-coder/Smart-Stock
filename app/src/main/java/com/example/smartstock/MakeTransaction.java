package com.example.smartstock;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MakeTransaction extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private Stock stock;
    private User user1;
    private TextInputLayout trans_date_layout, quantity_layout, price_layout, handledBy_layout,trans_id_layout,customer_name_layout,customer_phone_layout;
    private EditText trans_date, quantity, price, handledBy,trans_id,customer_name,customer_phone;
    private Button makeTransactionButton;
    private FirebaseFirestore db;
    private AutoCompleteTextView stockIdDropdown;
    private AutoCompleteTextView transactionTypeDropdown;
    private List<StockDropdownItem> stockDropdownItems = new ArrayList<>();
    private String collectionPath;
    private final String[] transactionTypes = new String[]{"Sale"};
    private ProgressBar progressBar;

    private static class StockDropdownItem {
        int stockId;
        String itemName;

        StockDropdownItem(int stockId, String itemName) {
            this.stockId = stockId;
            this.itemName = itemName;
        }

        @Override
        public String toString() {
            return stockId + " (" + itemName + ")";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        stockIdDropdown = findViewById(R.id.stockIdDropdown);
        stockIdDropdown.setDropDownBackgroundResource(android.R.color.white);
        transactionTypeDropdown = findViewById(R.id.transactionTypeDropdown);
        transactionTypeDropdown.setDropDownBackgroundResource(android.R.color.white);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        trans_date_layout = findViewById(R.id.transactionDateInput);
        quantity_layout = findViewById(R.id.quantityInput);
        price_layout = findViewById(R.id.priceInput);
        handledBy_layout = findViewById(R.id.userIdInput);
        trans_id_layout = findViewById(R.id.transactionIdInput);
        makeTransactionButton = findViewById(R.id.makeTransactionButton);
        customer_name_layout = findViewById(R.id.CustomerNameLayout);
        customer_phone_layout = findViewById(R.id.CustomerPhoneLayout);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);

        trans_date = trans_date_layout.getEditText();
        quantity = quantity_layout.getEditText();
        price = price_layout.getEditText();
        handledBy = handledBy_layout.getEditText();
        trans_id = trans_id_layout.getEditText();
        customer_name = customer_name_layout.getEditText();
        customer_phone = customer_phone_layout.getEditText();

        trans_date.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        getUser();
        progressBar.setVisibility(View.GONE);

        ArrayAdapter<StockDropdownItem> stockAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, stockDropdownItems
        );
        stockIdDropdown.setAdapter(stockAdapter);

        ArrayAdapter<String> transactionTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, transactionTypes
        );
        transactionTypeDropdown.setAdapter(transactionTypeAdapter);

        quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int qty = Integer.parseInt(s.toString());
                    if (stock != null && stock.getItem() != null) {
                        double unitPrice = stock.getItem().getSellingPrice();
                        double totalPrice = qty * unitPrice;
                        price.setText(String.format("%.2f", totalPrice));
                        price.setEnabled(false);
                    }
                } catch (NumberFormatException e) {
                    price.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        progressBar.setVisibility(View.VISIBLE);
        fetchLastTransactionId();
        trans_id.setEnabled(false);
        progressBar.setVisibility(View.GONE);



        if (user != null) {
            progressBar.setVisibility(View.VISIBLE);
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    String collectionPath = "Administrator".equals(role) ? user.getUid() : documentSnapshot.getString("bossID");

                    db.collection("users").document(collectionPath).collection("stocks")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                stockDropdownItems.clear();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");

                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    stock = new Stock();
                                    stock.setStockID(doc.getLong("stockID").intValue());
                                    stock.setQuantity(doc.getLong("quantity").intValue());

                                    Timestamp date = doc.getTimestamp("lastUpdate");
                                    if (date != null) {
                                        stock.setLastUpdate(date);
                                    }

                                    Object itemObj = doc.get("item");
                                    if (itemObj instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> itemMap = (Map<String, Object>) itemObj;

                                        Item item = new Item();
                                        item.setItemID(((Long) itemMap.get("itemID")).intValue());
                                        item.setItemName((String) itemMap.get("itemName"));
                                        item.setItemDescription((String) itemMap.get("itemDescription"));
                                        item.setItemCategory((String) itemMap.get("itemCategory"));
                                        item.setItemCode((String) itemMap.get("itemCode"));
                                        item.setUnitWeight((Double) itemMap.get("unitWeight"));
                                        item.setCostPrice((Double) itemMap.get("costPrice"));
                                        item.setSellingPrice((Double) itemMap.get("sellingPrice"));

                                        Timestamp itemDate = (Timestamp) itemMap.get("date");
                                        if (itemDate != null) {
                                            item.setDate(itemDate.toDate());
                                        }

                                        stock.setItem(item);

                                        stockDropdownItems.add(new StockDropdownItem(
                                                stock.getStockID(),
                                                item.getItemName()
                                        ));
                                    }
                                }

                                stockAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error loading stocks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error getting user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        stockIdDropdown.setOnItemClickListener((parent, view, position, id) -> {
            StockDropdownItem selectedItem = (StockDropdownItem) parent.getItemAtPosition(position);
            int selectedStockId = selectedItem.stockId;

            // Find and set the corresponding stock object
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    String collectionPath = "Administrator".equals(role) ? user.getUid() : documentSnapshot.getString("bossID");

                    db.collection("users").document(collectionPath).collection("stocks")
                            .whereEqualTo("stockID", selectedStockId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                                    stock = new Stock();
                                    stock.setStockID(doc.getLong("stockID").intValue());
                                    stock.setQuantity(doc.getLong("quantity").intValue());

                                    Timestamp date = doc.getTimestamp("lastUpdate");
                                    if (date != null) {
                                        stock.setLastUpdate(date);
                                    }

                                    Object itemObj = doc.get("item");
                                    if (itemObj instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> itemMap = (Map<String, Object>) itemObj;

                                        Item item = new Item();
                                        item.setItemID(((Long) itemMap.get("itemID")).intValue());
                                        item.setItemName((String) itemMap.get("itemName"));
                                        item.setItemDescription((String) itemMap.get("itemDescription"));
                                        item.setItemCategory((String) itemMap.get("itemCategory"));
                                        item.setItemCode((String) itemMap.get("itemCode"));
                                        item.setUnitWeight((Double) itemMap.get("unitWeight"));
                                        item.setCostPrice((Double) itemMap.get("costPrice"));
                                        item.setSellingPrice((Double) itemMap.get("sellingPrice"));

                                        Timestamp itemDate = (Timestamp) itemMap.get("date");
                                        if (itemDate != null) {
                                            item.setDate(itemDate.toDate());
                                        }

                                        stock.setItem(item);
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error loading stock: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            });
        });

        makeTransactionButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if (!checkValidations()){ progressBar.setVisibility(View.GONE); return; }

            int transactionId = Integer.parseInt(trans_id.getText().toString());

            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    collectionPath = "Administrator".equals(role) ? user.getUid() : documentSnapshot.getString("bossID");

                    db.collection("users").document(collectionPath).collection("transactions")
                            .document(String.valueOf(transactionId)).get()
                            .addOnSuccessListener(transSnapshot -> {
                                if (transSnapshot.exists()) {
                                    trans_id.setError("Transaction ID already exists");
                                    trans_id.requestFocus();
                                } else {
                                    // Proceed with transaction creation
                                    Transaction transaction = new Transaction();
                                    transaction.setTransactionId(transactionId);
                                    transaction.setStock(stock);
                                    transaction.setHandledBy(user1);
                                    transaction.setQuantity(Integer.parseInt(quantity.getText().toString()));
                                    transaction.setPrice(stock.getItem().getSellingPrice() * transaction.getQuantity());
                                    transaction.setTransactionDate();
                                    transaction.setCustomerName(customer_name.getText().toString());
                                    transaction.setCustomerPhone(customer_phone.getText().toString());

                                    db.collection("users").document(collectionPath)
                                            .collection("transactions")
                                            .document(String.valueOf(transactionId))
                                            .set(transaction)
                                            .addOnSuccessListener(aVoid -> {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                                                clearFields();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(this, "Error saving transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });

                                    // Update stock
                                    stock.setQuantity(stock.getQuantity() - transaction.getQuantity());
                                    db.collection("users").document(collectionPath)
                                            .collection("stocks")
                                            .document(String.valueOf(stock.getStockID()))
                                            .set(stock);
                                }
                            });
                    progressBar.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
            });
            progressBar.setVisibility(View.GONE);
        });
        progressBar.setVisibility(View.GONE);

    }

    public void getUser(){
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
           if(documentSnapshot.exists()){
               String role = documentSnapshot.getString("role");
               if(role.equals("Administrator")){
                   user1 = new Admin();
                   user1.setUsername(documentSnapshot.getString("username"));
                   user1.setEmail(documentSnapshot.getString("email"));
                   user1.setRole(documentSnapshot.getString("role"));
                   user1.setPassword(documentSnapshot.getString("password"));
                   user1.setCompanyName(documentSnapshot.getString("companyName"));
               }
               else{
                   user1 = (Employee) user1;
                   user1 = new Employee();
                   user1.setUsername(documentSnapshot.getString("username"));
                   user1.setEmail(documentSnapshot.getString("email"));
                   user1.setRole(documentSnapshot.getString("role"));
                   user1.setPassword(documentSnapshot.getString("password"));
                   ((Employee) user1).setContactNumber(String.valueOf(documentSnapshot.getString("contactNumber")));
                   ((Employee) user1).setAddress(documentSnapshot.getString("address"));
                   ((Employee) user1).setBossID(documentSnapshot.getString("bossID"));
                   user1.setCompanyName(documentSnapshot.getString("companyName"));
                   user1.setSalary(documentSnapshot.getDouble("salary"));

               }
               handledBy.setText(user1.getUsername());
               handledBy.setEnabled(false);
               trans_date.setText(new Date().toString());
               progressBar.setVisibility(View.GONE);
           }
           else{
               progressBar.setVisibility(View.GONE);
               Toast.makeText(getApplicationContext(), "Error getting user data", Toast.LENGTH_SHORT).show();
           }
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error getting user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public int getSelectedStockId() {
        int position = stockIdDropdown.getListSelection();
        if (position >= 0 && position < stockDropdownItems.size()) {
            return stockDropdownItems.get(position).stockId;
        }
        return -1;
    }

    public boolean checkValidations(){
        if(trans_date.getText().toString().isEmpty()){
            trans_date.setError("Transaction date is required");
            trans_date.requestFocus();
            return false;
        }
        if(trans_id.getText().toString().isEmpty()){
            trans_id.setError("Transaction ID is required");
            trans_id.requestFocus();
            return false;
        }
        if(transactionTypeDropdown.getText().toString().isEmpty()){
            transactionTypeDropdown.setError("Transaction type is required");
            transactionTypeDropdown.requestFocus();
            return false;
        }
        if(stockIdDropdown.getText().toString().isEmpty()){
            stockIdDropdown.setError("Stock ID is required");
            stockIdDropdown.requestFocus();
            return false;
        }
        if(customer_name.getText().toString().isEmpty()){
            customer_name.setError("Customer name is required");
            customer_name.requestFocus();
            return false;
        }

        if(customer_phone.getText().toString().isEmpty()){
            customer_phone.setError("Customer phone is required");
            customer_phone.requestFocus();
            return false;
        }
        if(customer_phone.getText().toString().length() != 11){
            customer_phone.setError("Customer phone must be 11 digits");
            customer_phone.requestFocus();
            return false;
        }
        if(quantity.getText().toString().isEmpty()){
            quantity.setError("Quantity is required");
            quantity.requestFocus();
            return false;
        }
        if(Integer.parseInt(quantity.getText().toString()) <= 0){
            quantity.setError("Quantity must be greater than 0");
            quantity.requestFocus();
            return false;
        }

        if(stock.getQuantity() < Integer.parseInt(quantity.getText().toString())){
            quantity.setError("Quantity is greater than the stock available");
            quantity.requestFocus();
            return false;
        }

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                collectionPath = "Administrator".equals(role) ? user.getUid() : documentSnapshot.getString("bossID");

                db.collection("users").document(collectionPath).collection("transactions").document(trans_id.getText().toString()).get().addOnSuccessListener(documentSnapshot1 -> {
                    if(documentSnapshot1.exists()){
                        trans_id.setError("Transaction ID already exists");
                        trans_id.requestFocus();
                        return ;
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting transaction data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Error getting transaction data: " + e.getMessage());
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error getting user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        return true;
    }

    public void clearFields(){
        trans_date.setText("");
        quantity.setText("");
        price.setText("");
        handledBy.setText("");
        trans_id.setText("");
        stockIdDropdown.setText("");
        transactionTypeDropdown.setText("");
        customer_name.setText("");
        customer_phone.setText("");
    }
    private void fetchLastTransactionId() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String role = userDoc.getString("role");
                String collectionPath = "Administrator".equals(role) ? user.getUid() : userDoc.getString("bossID");

                // Query transactions and order by ID in descending order to get the latest
                db.collection("users").document(collectionPath).collection("transactions")
                        .orderBy("transactionId", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot lastTransDoc = querySnapshot.getDocuments().get(0);
                                int lastId = lastTransDoc.getLong("transactionId").intValue();
                                trans_id.setText(String.valueOf(lastId + 1));
                            } else {
                                trans_id.setText("1");
                            }
                            progressBar.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error fetching last transaction ID", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }
}
