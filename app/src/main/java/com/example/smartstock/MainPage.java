package com.example.smartstock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String role;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT); // Make nav bar transparent
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightNavigationBars(false);
        }

        setContentView(R.layout.activity_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainFrameLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // No bottom padding
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        frameLayout = findViewById(R.id.mainFrameLayout);

        if (user == null) {
            Intent intent = new Intent(MainPage.this, Login.class);
            startActivity(intent);
            finish();
        }


        loadFragment(new TransactionFragment(),true);

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                role = documentSnapshot.getString("role");
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navHome) {
                    loadFragment(new TransactionFragment(),false);

                }
                else if (itemId == R.id.navProfile) {
                    loadFragment(new ProfileFragment(),false);

                }
                else if(itemId == R.id.navNotification){
                    loadFragment(new NotificationFragment(),false);

                }
                else {
                    if ("Administrator".equals(role)) {
                        loadFragment(new ManageFragmentAdmin(), false);
                    } else {
                        loadFragment(new ManageFragmentEmp(), false);
                    }
                }

                return true;
            }
        });



    }
    @Override
    public void onBackPressed() {
        // Find the current TransactionFragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (fragment instanceof TransactionFragment) {
            TransactionFragment tf = (TransactionFragment) fragment;

            // Check if it has a child fragment visible
            if (tf.getChildFragmentManager().getBackStackEntryCount() > 0) {
                tf.restoreTransactionView();
                return;
            }
        }

        // Default back behavior
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
    private void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (isAppInitialized) {
            fragmentTransaction.add(R.id.mainFrameLayout, fragment);
        } else {
            fragmentTransaction.replace(R.id.mainFrameLayout, fragment);
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }
}