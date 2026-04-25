package com.example.smartstock;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.progressindicator.BaseProgressIndicatorSpec;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TransactionFragment extends Fragment {

    private Button makeTransactionButton;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String collectionPath;
    private LinearLayout transactionsContainer;
    private TextView transaction_det, rt, filterLabel;
    private Spinner daysFilterSpinner;
    private int selectedDays = -1;
    private String formattedTime;
    private ProgressBar progressBar;

    @Override
    public void onResume() {
        super.onResume();
        restoreTransactionView();
        if (collectionPath != null) {
            progressBar.setVisibility(View.VISIBLE);
            loadTransactions();
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackButtonHandler(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        initializeViews(view);
        setupFirebase();
        setupFilterSpinner();
        setupTransactionButton();

        if (user != null) {
            getPath();
        } else {
            Toast.makeText(getActivity(), "Please sign in first", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void initializeViews(View view) {
        makeTransactionButton = view.findViewById(R.id.MakeTranactionButton);
        transactionsContainer = view.findViewById(R.id.transactionsContainer);
        transaction_det = view.findViewById(R.id.trans_details);
        rt = view.findViewById(R.id.rt);
        filterLabel = view.findViewById(R.id.filterLabel);
        daysFilterSpinner = view.findViewById(R.id.daysFilterSpinner);
        progressBar = view.findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);

        transaction_det.setVisibility(View.GONE);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    private void setupFilterSpinner() {
        daysFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                switch (selectedItem) {
                    case "Last 7 Days": selectedDays = 7; break;
                    case "Last 15 Days": selectedDays = 15; break;
                    case "Last 30 Days": selectedDays = 30; break;
                    default: selectedDays = -1;
                }
                if (collectionPath != null) loadTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDays = 7;
            }
        });
    }

    private void setupTransactionButton() {
        makeTransactionButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            Intent intent = new Intent(getActivity(), MakeTransaction.class);
            startActivity(intent);
        });
        progressBar.setVisibility(View.GONE);
    }

    private void setupBackButtonHandler(View view) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                getParentFragmentManager().popBackStack();
                return true;
            }
            return false;
        });
    }

    private void getPath() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        collectionPath = "Administrator".equals(role) ? user.getUid() : documentSnapshot.getString("bossID");

                        if (collectionPath != null) {
                            loadTransactions();
                        } else {
                            Toast.makeText(getActivity(), "Failed to determine collection path", Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e ->{
                    progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Failed to get user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void addTransactionCard(Transaction transaction) {
        if (getActivity() == null || transaction == null) return;

        View card = LayoutInflater.from(getActivity()).inflate(R.layout.transaction_card, transactionsContainer, false);

        TextView transactionId = card.findViewById(R.id.transactionIdText);
        TextView handledBy = card.findViewById(R.id.handledByText);
        TextView stock = card.findViewById(R.id.stockText);
        TextView date = card.findViewById(R.id.dateText);

        transactionId.setText(String.valueOf(transaction.getTransactionId()));
        handledBy.setText(transaction.getHandledBy() != null ? transaction.getHandledBy().getUsername() : "N/A");
        stock.setText(transaction.getStock() != null && transaction.getStock().getItem() != null
                ? transaction.getStock().getItem().getItemName() : "N/A");

        if (transaction.getTransactionDate() != null) {
            Date dateObj = transaction.getTransactionDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
            String formattedDate = dateFormat.format(dateObj);
            formattedTime = timeFormat.format(dateObj);
            date.setText(formattedDate);
        } else {
            date.setText("N/A");
        }

        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("transaction", transaction);
            bundle.putString("time", formattedTime);
            bundle.putString("ID", String.valueOf(transaction.getTransactionId()));

            ViewTransactionFragment fragment = new ViewTransactionFragment();
            fragment.setArguments(bundle);

            loadFragment(fragment);
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);

        transactionsContainer.addView(card);
    }

    private void loadTransactions() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(collectionPath).collection("transactions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactionsContainer.removeAllViews();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Transaction transaction = parseTransaction(doc);
                            if (shouldIncludeTransaction(transaction)) {
                                addTransactionCard(transaction);
                            }
                        } catch (Exception e) {
                            Log.e("TransactionFragment", "Error parsing transaction", e);
                            Toast.makeText(getActivity(), "Error parsing transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("TransactionFragment", "Failed to load transactions", e);
                    Toast.makeText(getActivity(), "Failed to load transactions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Transaction parseTransaction(DocumentSnapshot doc) {
        Transaction transaction = new Transaction();

        // Basic fields
        if (doc.contains("transactionId")) transaction.setTransactionId(doc.getLong("transactionId").intValue());
        if (doc.contains("quantity")) transaction.setQuantity(doc.getLong("quantity").intValue());
        if (doc.contains("price")) transaction.setPrice(doc.getDouble("price"));
        transaction.setCustomerName(doc.getString("customerName"));
        transaction.setCustomerPhone(doc.getString("customerPhone"));

        // Transaction date
        Timestamp timestamp = doc.getTimestamp("transactionDate");
        if (timestamp != null) transaction.setTransactionDate(timestamp.toDate());

        // Stock information
        Object stockObj = doc.get("stock");
        if (stockObj instanceof Map) {
            Map<String, Object> stockMap = (Map<String, Object>) stockObj;
            Stock stock = new Stock();

            if (stockMap.get("quantity") instanceof Long) stock.setQuantity(((Long) stockMap.get("quantity")).intValue());
            if (stockMap.get("stockID") instanceof Long) stock.setStockID(((Long) stockMap.get("stockID")).intValue());

            Object itemObj = stockMap.get("item");
            if (itemObj instanceof Map) {
                Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                Item item = new Item();

                if (itemMap.get("itemID") instanceof Long) item.setItemID(((Long) itemMap.get("itemID")).intValue());
                item.setItemName((String) itemMap.get("itemName"));
                item.setItemDescription((String) itemMap.get("itemDescription"));
                item.setItemCategory((String) itemMap.get("itemCategory"));
                if (itemMap.get("unitWeight") instanceof Double) item.setUnitWeight((Double) itemMap.get("unitWeight"));
                if (itemMap.get("costPrice") instanceof Double) item.setCostPrice((Double) itemMap.get("costPrice"));
                if (itemMap.get("sellingPrice") instanceof Double) item.setSellingPrice((Double) itemMap.get("sellingPrice"));
                item.setItemCode((String) itemMap.get("itemCode"));

                Object itemDateObj = itemMap.get("date");
                if (itemDateObj instanceof Timestamp) {
                    item.setDate(((Timestamp) itemDateObj).toDate());
                } else {
                    // Optional: log unexpected date type
                }

                stock.setItem(item);
            } else {
                // Optional: log unexpected stock.item structure
            }

            transaction.setStock(stock);
        }

        // User information
        Object handledByObj = doc.get("handledBy");
        if (handledByObj instanceof Map) {
            Map<String, Object> userMap = (Map<String, Object>) handledByObj;
            String role = (String) userMap.get("role");
            User u = "Employee".equals(role) ? new Employee() : new User();

            if (u instanceof Employee) {
                ((Employee) u).setContactNumber((String) userMap.get("contactNumber"));
            }

            u.setUsername((String) userMap.get("username"));
            u.setEmail((String) userMap.get("email"));
            u.setRole(role);

            transaction.setHandledBy(u);
        }

        return transaction;
    }


    private boolean shouldIncludeTransaction(Transaction transaction) {
        if (selectedDays == -1) return true;
        if (transaction.getTransactionDate() == null) return false;

        long millisInDay = 24 * 60 * 60 * 1000L;
        long diff = new Date().getTime() - transaction.getTransactionDate().getTime();
        long daysDiff = diff / millisInDay;

        return daysDiff <= selectedDays;
    }

    public void loadFragment(Fragment fragment) {
        // Clear any existing fragments in frameLayout11
        Fragment existingFragment = getChildFragmentManager().findFragmentById(R.id.frameLayout11);
        if (existingFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(existingFragment)
                    .commitNow();
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();


        // Hide transaction list UI elements
        transaction_det.setVisibility(View.VISIBLE);
        rt.setVisibility(View.GONE);
        makeTransactionButton.setVisibility(View.GONE);
        transactionsContainer.setVisibility(View.GONE);
        filterLabel.setVisibility(View.GONE);
        daysFilterSpinner.setVisibility(View.GONE);

        // Add to frameLayout11
        transaction.replace(R.id.frameLayout11, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void restoreTransactionView() {
        // Use commitNow() to ensure immediate execution
        getChildFragmentManager().popBackStackImmediate();

        // Restore main view visibility
        transaction_det.setVisibility(View.GONE);
        rt.setVisibility(View.VISIBLE);
        makeTransactionButton.setVisibility(View.VISIBLE);
        transactionsContainer.setVisibility(View.VISIBLE);
        filterLabel.setVisibility(View.VISIBLE);
        daysFilterSpinner.setVisibility(View.VISIBLE);
    }
    public boolean isViewTransactionVisible() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.frameLayout11);
        return fragment != null && fragment.isVisible();
    }
}