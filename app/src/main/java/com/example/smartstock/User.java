package com.example.smartstock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    protected String email;
    protected String companyName;
    protected String username;
    protected String role;
    protected String password;

    protected double salary;

    User() {

    }

    User(String email, String companyName, String password, String username) {
        this.email = email;
        this.companyName = companyName;
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getSalary(){
        return salary;
    }
    public void setSalary(double salary){
        this.salary = salary;
    }
}
    class Admin extends com.example.smartstock.User {
        public Admin(){
            super();
            this.role = "Administrator";
        }
        public Admin(String email, String companyName, String password, String username) {
            super(email, companyName, password, username);
            this.role = "Administrator";
        }

    }

    class Employee extends com.example.smartstock.User {

        private String empId;
        private String contactNumber;
        private String address;
        private String bossID;
        public Employee() {
            super();
            this.role = "Employee";
        }

        public Employee(String email, String companyName, String password, String username,String contactNumber,String address,double salary) {
            super(email, companyName, password, username);
            this.role = "Employee";
            this.contactNumber = contactNumber;
            this.address = address;
            this.salary = salary;
        }
        public Employee(String employeeId, String username) {
            this.empId = employeeId;
            this.username = username;
        }
        public String getEmpId(){
            return empId;
        }
        public void setEmpId(String empId) {
            this.empId = empId;
        }
        public String getBossID(){
            return bossID;
        }
        public double getSalary(){
            return salary;
        }
        public void setSalary(double salary){
            this.salary = salary;
        }
        public void setBossID(String bossID) {
            this.bossID = bossID;
        }
        public String getContactNumber(){
            return contactNumber;
        }
        public void setContactNumber(String contactNumber){
            this.contactNumber = contactNumber;
        }
        public String getAddress(){
            return address;
        }
        public void setAddress(String address){
            this.address = address;
        }
    }

