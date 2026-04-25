package com.example.smartstock;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewTransactionFragment extends Fragment {

    private Transaction transaction;
    private TextView transactionIDText, typeText, dateText, timeText, itemNameText, quantityText,
            customerNameText, customerPhoneText, userNameText, userPhoneText,
            userEmailText, userRoleText, totalPriceText, profitText;
    private Button downloadPdfButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String com;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                // Get the parent fragment
                Fragment parent = getParentFragment();
                if (parent instanceof TransactionFragment) {
                    ((TransactionFragment) parent).restoreTransactionView();
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_transaction, container, false);

        initializeViews(view);
        getCompanyString();
        loadTransactionData();
        setupPdfButton();

        return view;
    }

    private void initializeViews(View view) {
        transactionIDText = view.findViewById(R.id.transactionIDText);
        typeText = view.findViewById(R.id.typeText);
        dateText = view.findViewById(R.id.dateText);
        timeText = view.findViewById(R.id.timeText);
        itemNameText = view.findViewById(R.id.itemNameText);
        quantityText = view.findViewById(R.id.quantityText);
        customerNameText = view.findViewById(R.id.customerNameText);
        customerPhoneText = view.findViewById(R.id.customerPhoneText);
        userNameText = view.findViewById(R.id.userNameText);
        userPhoneText = view.findViewById(R.id.userPhoneText);
        userEmailText = view.findViewById(R.id.userEmailText);
        userRoleText = view.findViewById(R.id.userRoleText);
        totalPriceText = view.findViewById(R.id.priceText);
        profitText = view.findViewById(R.id.profitText);
        downloadPdfButton = view.findViewById(R.id.downloadPdfButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    private void loadTransactionData() {
        if (getArguments() != null) {
            transaction = getArguments().getSerializable("transaction", Transaction.class);
        }

        if (transaction == null) {
            Toast.makeText(getActivity(), "Transaction data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set transaction data
        transactionIDText.setText(getArguments() != null ? getArguments().getString("ID", "N/A") : "N/A");
        typeText.setText("Sale");
        timeText.setText(getArguments() != null ? getArguments().getString("time", "N/A") : "N/A");

        // Transaction date
        if (transaction.getTransactionDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            dateText.setText(sdf.format(transaction.getTransactionDate()));
        } else {
            dateText.setText("N/A");
        }

        // Stock and item info
        if (transaction.getStock() != null && transaction.getStock().getItem() != null) {
            itemNameText.setText(transaction.getStock().getItem().getItemName());
        } else {
            itemNameText.setText("N/A");
        }

        totalPriceText.setText(transaction.getPrice() != 0 ? String.valueOf(transaction.getPrice()) : "N/A");
        profitText.setText(transaction.calculateProfit() != 0 ? String.valueOf(transaction.calculateProfit()) : "N/A");
        quantityText.setText(String.valueOf(transaction.getQuantity()));
        customerNameText.setText(transaction.getCustomerName() != null ? transaction.getCustomerName() : "N/A");
        customerPhoneText.setText(transaction.getCustomerPhone() != null ? transaction.getCustomerPhone() : "N/A");

        // Handle user info
        if (transaction.getHandledBy() != null) {
            User user = transaction.getHandledBy();
            userNameText.setText(user.getUsername() != null ? user.getUsername() : "N/A");
            userEmailText.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            userRoleText.setText(user.getRole() != null ? user.getRole() : "N/A");

            if ("Employee".equals(user.getRole()) && user instanceof Employee) {
                Employee employee = (Employee) user;
                userPhoneText.setText(employee.getContactNumber() != null ? employee.getContactNumber() : "N/A");
            } else {
                userPhoneText.setText("N/A");
            }
        } else {
            userNameText.setText("N/A");
            userEmailText.setText("N/A");
            userRoleText.setText("N/A");
            userPhoneText.setText("N/A");
            Toast.makeText(getActivity(), "User information not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPdfButton() {
        downloadPdfButton.setOnClickListener(v -> generateAndDownloadPdf());
    }

    private void generateAndDownloadPdf() {
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            int x = 40;
            int y = 50;

            // Title
            paint.setColor(Color.BLACK);
            paint.setTextSize(20f);
            paint.setFakeBoldText(true);
            canvas.drawText(String.valueOf(com), x, y, paint);
            y += 30;

            paint.setTextSize(14f);
            paint.setFakeBoldText(false);
            canvas.drawText("Transaction Receipt", x, y, paint);
            y += 25;

            // Divider
            paint.setStrokeWidth(1);
            canvas.drawLine(x, y, x + 500, y, paint);
            y += 20;

            // Transaction Info
            paint.setTextSize(12f);
            drawLabelValue(canvas, paint, "Transaction ID", transactionIDText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Type", typeText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Date", dateText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Time", timeText.getText().toString(), x, y);
            y += 30;

            // Item Info
            paint.setFakeBoldText(true);
            canvas.drawText("Item Information", x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;

            drawLabelValue(canvas, paint, "Item Name", itemNameText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Quantity", quantityText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Total Price", totalPriceText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Profit", profitText.getText().toString(), x, y);
            y += 30;

            // Customer Info
            paint.setFakeBoldText(true);
            canvas.drawText("Customer Details", x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;

            drawLabelValue(canvas, paint, "Name", customerNameText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Phone", customerPhoneText.getText().toString(), x, y);
            y += 30;

            // Handled By Info
            paint.setFakeBoldText(true);
            canvas.drawText("Handled By", x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;

            drawLabelValue(canvas, paint, "Name", userNameText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Phone", userPhoneText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Email", userEmailText.getText().toString(), x, y);
            y += 20;
            drawLabelValue(canvas, paint, "Role", userRoleText.getText().toString(), x, y);

            y += 30;
            canvas.drawLine(x, y, x + 500, y, paint);
            y += 20;

            paint.setTextSize(10f);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Thank you for your purchase!", x + 500, y, paint);
            paint.setTextAlign(Paint.Align.LEFT);

            document.finishPage(page);

            // Save file
            String fileName = "Transaction_" + transactionIDText.getText() + ".pdf";
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            document.writeTo(new FileOutputStream(file));
            document.close();

            Toast.makeText(getActivity(), "PDF saved to Downloads folder", Toast.LENGTH_SHORT).show();
            openPdfFile(file);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "No PDF viewer installed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void drawLabelValue(Canvas canvas, Paint paint, String label, String value, int x, int y) {
        paint.setTextSize(12f);
        canvas.drawText(label + ": ", x, y, paint);
        canvas.drawText(value, x + 150, y, paint);
    }

    private String getCompanyString(){
        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
           com = documentSnapshot.getString("companyName");
        }).addOnFailureListener(e -> {
            com = "Company Name Unavailable";
        });

        return com;
    }

}