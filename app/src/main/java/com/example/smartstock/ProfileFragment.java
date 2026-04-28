package com.example.smartstock;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private Button logout;
    private TextView n,c,e,r,title,s;
    private ImageView salary_icon;
    private FirebaseUser u;
    private FirebaseFirestore db;
    private User user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        logout = view.findViewById(R.id.LogoutButton);
        n = view.findViewById(R.id.name);
        c = view.findViewById(R.id.companyName);
        e = view.findViewById(R.id.email);
        r = view.findViewById(R.id.role);
        s = view.findViewById(R.id.salary);
        salary_icon = view.findViewById(R.id.salary_icon);
        title = view.findViewById(R.id.user_name);

        u = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (u != null) {
            db.collection("users").document(u.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if("Administrator".equals(documentSnapshot.getString("role"))){
                                user = new Admin();
                                s.setVisibility(View.GONE);
                                salary_icon.setVisibility(View.GONE);
                                user = documentSnapshot.toObject(Admin.class);
                            }
                            else{
                                user = new Employee();
                                s.setVisibility(View.VISIBLE);
                                salary_icon.setVisibility(View.VISIBLE);
                                user = documentSnapshot.toObject(Admin.class);
                                s.setText(String.valueOf(user.getSalary()) != null ? String.valueOf(user.getSalary()) : "N/A");
                            }

                            n.setText(user.getUsername() != null ? user.getUsername() : "N/A");
                            c.setText(user.getCompanyName() != null ? user.getCompanyName() : "N/A");
                            e.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                            r.setText(user.getRole() != null ? user.getRole() : "N/A");
                            title.setText(user.getUsername() != null ? user.getUsername() : "N/A");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Error getting user data", Toast.LENGTH_SHORT).show();
                    });
        }


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
            }
        });

        return view;

    }

}