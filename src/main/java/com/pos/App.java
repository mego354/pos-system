package com.pos;

import com.pos.database.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class App {
    @FXML
    private StackPane contentArea;
    
    @FXML
    private Button dashboardBtn;
    
    @FXML
    private Button productsBtn;
    
    @FXML
    private Button categoriesBtn;
    
    @FXML
    private Button salesBtn;
    
    @FXML
    private void initialize() {
        try {
            // Initialize database
            DatabaseManager.initialize();
            
            // Load dashboard by default
            loadDashboard();
            
            // Set up navigation buttons
            setupNavigation();
            
        } catch (Exception e) {
            System.err.println("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupNavigation() {
        dashboardBtn.setOnAction(e -> loadDashboard());
        productsBtn.setOnAction(e -> loadProducts());
        categoriesBtn.setOnAction(e -> loadCategories());
        salesBtn.setOnAction(e -> loadSales());
    }
    
    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Products.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Categories.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadSales() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Sales.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
        } catch (IOException e) {
            System.err.println("Error loading sales: " + e.getMessage());
            e.printStackTrace();
        }
    }
}