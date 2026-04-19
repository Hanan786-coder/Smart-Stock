package com.example.smartstock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class View_Items_admin extends AppCompatActivity {

    private LinearLayout itemsContainer;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CardView addItemCard;
    private TextView item, itemDetails;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private ProgressBar progressBar;

    @Override
    protected void onResume() {
        super.onResume();
        itemsContainer.removeAllViews();
        itemDetails.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        loadItems();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_items_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addItemCard = findViewById(R.id.addItemCard);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        itemsContainer = findViewById(R.id.itemsContainer);
        item = findViewById(R.id.Items);
        itemDetails = findViewById(R.id.header);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.GONE);

        itemDetails.setVisibility(View.GONE);

        addItemCard.setOnClickListener(v -> {

            Intent intent = new Intent(View_Items_admin.this, AddItems.class);
            startActivity(intent);
        });
    }

    private void loadItems() {
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE); // Show before loading
        itemsContainer.setVisibility(View.GONE);
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                String userId;

                if ("Administrator".equals(role)) {
                    userId = user.getUid();
                } else {
                    userId = documentSnapshot.getString("bossID");
                }

                db.collection("users")
                        .document(userId)
                        .collection("items")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            itemsContainer.removeAllViews();

                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Item item = new Item();
                                item.setItemID(document.getLong("itemID").intValue());
                                item.setItemName(document.getString("itemName"));
                                item.setItemDescription(document.getString("itemDescription"));
                                item.setItemCategory(document.getString("itemCategory"));
                                item.setUnitWeight(document.getDouble("unitWeight"));
                                item.setCostPrice(document.getDouble("costPrice"));
                                item.setSellingPrice(document.getDouble("sellingPrice"));
                                item.setItemCode(document.getString("itemCode"));

                                // Handle Timestamp directly
                                Timestamp timestamp = document.getTimestamp("date");
                                if (timestamp != null) {
                                    item.setDate(timestamp); // Now accepts Timestamp directly
                                }

                                @SuppressLint("InflateParams")
                                View cardView = getLayoutInflater().inflate(R.layout.item_card_layout, null);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 0, 0, 24);
                                cardView.setLayoutParams(params);

                                TextView itemName = cardView.findViewById(R.id.itemNameText);
                                TextView itemId = cardView.findViewById(R.id.itemIdText);
                                TextView salePrice = cardView.findViewById(R.id.salePriceText);
                                TextView dateText = cardView.findViewById(R.id.dateText);

                                itemName.setText(item.getItemName());
                                itemId.setText(String.valueOf(item.getItemID()));
                                salePrice.setText(String.format(Locale.getDefault(), "%.2f", item.getSellingPrice()));

                                // Format the date properly
                                if (item.getDate() != null) {
                                    dateText.setText(dateFormat.format(item.getDate().toDate()));
                                } else {
                                    dateText.setText("");
                                }

                                cardView.setOnClickListener(v -> {
                                    ItemDetail fragment = ItemDetail.newInstance(item);
                                    loadFragment(fragment);
                                    progressBar.setVisibility(View.GONE);
                                });

                                itemsContainer.addView(cardView);
                            }
                            progressBar.setVisibility(View.GONE); // Show before loading
                            itemsContainer.setVisibility(View.VISIBLE);

                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error getting user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        itemsContainer.setVisibility(View.GONE);
        item.setVisibility(View.GONE);
        itemDetails.setVisibility(View.VISIBLE);
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
            itemsContainer.setVisibility(View.VISIBLE);
            item.setVisibility(View.VISIBLE);
            itemDetails.setVisibility(View.GONE);
            findViewById(R.id.frameLayout).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}