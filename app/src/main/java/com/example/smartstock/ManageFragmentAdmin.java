package com.example.smartstock;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManageFragmentAdmin extends Fragment {

    private CardView add_view_item, add_view_employee, add_view_warehouse, add_view_supplier, remove_item, remove_warehouse, remove_supplier, remove_employee;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_admin, container, false);

        add_view_item = view.findViewById(R.id.add_item);
        add_view_employee = view.findViewById(R.id.add_employee);
        remove_item = view.findViewById(R.id.remove_item);
        remove_employee = view.findViewById(R.id.remove_employee);

        remove_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RemoveItem.class);
                startActivity(intent);
            }
        });

        add_view_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), View_Items_admin.class);
                    startActivity(intent);
            }
        });

        add_view_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewEmployee.class);
                startActivity(intent);
            }
        });

        remove_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RemoveEmployee.class);
                startActivity(intent);
            }
        });

        return view;
    }
}