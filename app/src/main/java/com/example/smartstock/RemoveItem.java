package com.example.smartstock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RemoveItem extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseFirestore db;
    private LinearLayout itemCheckboxContainer;
    private Button removeItemsButton;
    private String collectionPath;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_remove_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        itemCheckboxContainer = findViewById(R.id.itemCheckboxContainer);
        removeItemsButton = findViewById(R.id.removeItemsButton);
        progressBar = findViewById(R.id.pgbar);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                if(role.equals("Administrator")){
                    collectionPath = user.getUid();
                }
                else{
                    collectionPath = documentSnapshot.getString("bossID");
                }

                db.collection("users").document(collectionPath).collection("items").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if(!queryDocumentSnapshots.isEmpty()){
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String itemName = doc.getString("itemName");
                            int itemId = doc.getLong("itemID").intValue();
                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setText(itemId +". "+ itemName);
                            checkBox.setTag(doc.getId());
                            itemCheckboxContainer.addView(checkBox);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
        });

        //Delete selected items
        removeItemsButton.setOnClickListener(new View.OnClickListener() {
            //fix it
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if(itemCheckboxContainer.getChildCount() == 0){
                    Toast.makeText(RemoveItem.this, "No items selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                View dialogView = LayoutInflater.from(RemoveItem.this).inflate(R.layout.dialog_remove_item, null);
                AlertDialog dialog = new MaterialAlertDialogBuilder(RemoveItem.this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

                dialog.show();

                Button yesBtn = dialogView.findViewById(R.id.yesBtn);
                Button noBtn = dialogView.findViewById(R.id.noBtn);


                noBtn.setOnClickListener(v1 -> {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                    for (int i = 0; i < itemCheckboxContainer.getChildCount(); i++) {
                        CheckBox checkBox = (CheckBox) itemCheckboxContainer.getChildAt(i);
                        checkBox.setChecked(false);
                    }
                    return;
                });
                yesBtn.setOnClickListener(v1 -> {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();

                for (int i = 0; i < itemCheckboxContainer.getChildCount(); i++) {
                    CheckBox checkBox = (CheckBox) itemCheckboxContainer.getChildAt(i);
                    if (checkBox.isChecked()) {
                        String documentId = (String) checkBox.getTag();
                        db.collection("users").document(collectionPath).collection("items").document(documentId).delete();
                        try {
                            db.collection("users").document(collectionPath).collection("stocks").document(documentId).delete();
                        }catch(Exception e){
                            continue;
                        }
                    }
                }
                Toast.makeText(RemoveItem.this, "Items removed successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
            }
        });

    }
}