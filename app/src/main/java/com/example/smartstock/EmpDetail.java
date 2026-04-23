package com.example.smartstock;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class EmpDetail extends Fragment {

    private TextView name, email, company,id, address, contact, salary;
    private Button editEmpButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emp_detail, container, false);

        id = view.findViewById(R.id.employeeIDText);
        name = view.findViewById(R.id.employeeNameText);
        email = view.findViewById(R.id.employeeEmailText);
        company = view.findViewById(R.id.employeeCompanyText);
        address = view.findViewById(R.id.employeeAddressText);
        contact = view.findViewById(R.id.employeeContactText);
        salary = view.findViewById(R.id.employeeSalaryText);
        editEmpButton = view.findViewById(R.id.editEmpButton);

        editEmpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditEmployee.class);
                intent.putExtras(makeBundle());
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        if (getArguments() != null) {
            name.setText(getArguments().getString("username"));
            email.setText(getArguments().getString("email"));
            company.setText(getArguments().getString("company"));
            address.setText(getArguments().getString("address"));
            contact.setText(getArguments().getString("contact"));
            salary.setText(getArguments().getString("salary"));
            id.setText(getArguments().getString("empId"));
        }

        return view;
    }

    private Bundle makeBundle(){
        Bundle bundle = new Bundle();
        //Get text from textViews and add to bundle
        bundle.putString("username",name.getText().toString());
        bundle.putString("email",email.getText().toString());
        bundle.putString("company",company.getText().toString());
        bundle.putString("address",address.getText().toString());
        bundle.putString("contact",contact.getText().toString());
        bundle.putString("salary",salary.getText().toString());
        bundle.putString("empId",id.getText().toString());

        return bundle;
    }
}