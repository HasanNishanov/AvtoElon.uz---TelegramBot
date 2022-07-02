package com.company.database;

import com.company.model.Category;
import com.company.model.Customer;
import com.company.model.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public interface Database {
    List<Customer> customerList = new ArrayList<>();
    List<Category> categoryList = new ArrayList<>();
    List<Product> productList = new ArrayList<>();

    static Connection getConnection() {

        final String DB_USERNAME = "";
        final String DB_PASSWORD = "";
        final String DB_URL = "";

        Connection conn = null;
        try {

            Class.forName("org.postgresql.Driver");

            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            if (conn != null) {
                System.out.println("Connection worked");
            } else {
                System.out.println("Connection failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }
}
