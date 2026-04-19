package com.example.smartstock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ItemDetail extends Fragment {

    private static final String ARG_ITEM = "item_object";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;

    private Button addStockButton,editItemButton;

    private Item item;

    private TextView itemIDText, itemNameText, itemDescriptionText, itemCategoryText,itemCodeText,
            unitWeightText, costPriceText, sellingPriceText;


    public ItemDetail() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            // Re-fetch updated item details using item ID or update the existing item
            String updatedItemID = data.getStringExtra("itemID");
            fetchUpdatedItemDetails(updatedItemID);
        }
    }

    public static ItemDetail newInstance(Item item) {
        ItemDetail fragment = new ItemDetail();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemIDText = view.findViewById(R.id.itemIDText);
        itemNameText = view.findViewById(R.id.itemNameText);
        itemDescriptionText = view.findViewById(R.id.itemDescriptionText);
        itemCategoryText = view.findViewById(R.id.itemCategoryText);
        unitWeightText = view.findViewById(R.id.unitWeightText);
        costPriceText = view.findViewById(R.id.costPriceText);
        sellingPriceText = view.findViewById(R.id.sellingPriceText);
        itemCodeText = view.findViewById(R.id.itemCodeText);
        addStockButton = view.findViewById(R.id.addStockButton);
        editItemButton = view.findViewById(R.id.editItemButton);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (getArguments() != null) {
            item = getArguments().getParcelable(ARG_ITEM);
            if (item != null) {
                populateItemDetails(item);
            } else {
                showError("Item not found");
            }
        } else {
            showError("No item provided");
        }

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                if (role != null && role.equals("Administrator")) {
                    editItemButton.setVisibility(View.VISIBLE);
                }
                else{
                    editItemButton.setVisibility(View.GONE);
                    addStockButton.setText("View Stock");
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to fetch user role", Toast.LENGTH_SHORT).show();
        });

        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddStock.class);
                intent.putExtra("itemID", itemIDText.getText().toString());
                startActivity(intent);
                //End the fragment
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangeItemDetails.class);
                intent.putExtras(makeBundle());
                startActivityForResult(intent, 1001);
                //End the fragment
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });


    }

    private void populateItemDetails(Item item) {
        itemIDText.setText(String.valueOf(item.getItemID()));
        itemNameText.setText(item.getItemName());
        itemDescriptionText.setText(item.getItemDescription());
        itemCategoryText.setText(item.getItemCategory());
        unitWeightText.setText(String.valueOf(item.getUnitWeight()));
        costPriceText.setText(String.valueOf(item.getCostPrice()));
        sellingPriceText.setText(String.valueOf(item.getSellingPrice()));
        itemCodeText.setText(String.valueOf(item.getItemCode()));
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private Bundle makeBundle(){
        Bundle bundle = new Bundle();
        //Get text from textViews and add to bundle
        bundle.putString("itemID", itemIDText.getText().toString());
        bundle.putString("itemName", itemNameText.getText().toString());
        bundle.putString("itemDescription", itemDescriptionText.getText().toString());
        bundle.putString("itemCategory", itemCategoryText.getText().toString());
        bundle.putString("itemCode", itemCodeText.getText().toString());
        bundle.putString("unitWeight", unitWeightText.getText().toString());
        bundle.putString("costPrice", costPriceText.getText().toString());
        bundle.putString("sellingPrice", sellingPriceText.getText().toString());
        return bundle;
    }

    private void fetchUpdatedItemDetails(String itemID) {
        db.collection("users").document(user.getUid())
                .collection("items").document(itemID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item updatedItem = documentSnapshot.toObject(Item.class);
                        if (updatedItem != null) {
                            this.item = updatedItem;
                            populateItemDetails(updatedItem);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch updated item", Toast.LENGTH_SHORT).show();
                });
    }

}
