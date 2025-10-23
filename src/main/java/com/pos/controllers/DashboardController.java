package com.pos.controllers;

import com.pos.models.Sale;
import com.pos.repository.CategoryRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.SaleRepository;
import com.pos.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    
    @FXML
    private Label totalProductsLabel;
    
    @FXML
    private Label totalCategoriesLabel;
    
    @FXML
    private Label totalSalesLabel;
    
    @FXML
    private Label totalRevenueLabel;
    
    @FXML
    private TableView<Sale> recentSalesTable;
    
    @FXML
    private TableColumn<Sale, Integer> saleIdColumn;
    
    @FXML
    private TableColumn<Sale, Double> amountColumn;
    
    @FXML
    private TableColumn<Sale, String> dateColumn;
    
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private SaleRepository saleRepository;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            productRepository = new ProductRepository();
            categoryRepository = new CategoryRepository();
            saleRepository = new SaleRepository();
            
            setupTableColumns();
            loadDashboardData();
            
        } catch (Exception e) {
            AlertUtils.showException("Dashboard Error", e);
        }
    }
    
    private void setupTableColumns() {
        saleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        dateColumn.setCellValueFactory(cellData -> {
            Sale sale = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
        });
    }
    
    private void loadDashboardData() {
        try {
            // Load statistics
            int totalProducts = productRepository.findAll().size();
            int totalCategories = categoryRepository.findAll().size();
            int totalSales = saleRepository.getTotalSalesCount();
            double totalRevenue = saleRepository.getTotalRevenue();
            
            totalProductsLabel.setText(String.valueOf(totalProducts));
            totalCategoriesLabel.setText(String.valueOf(totalCategories));
            totalSalesLabel.setText(String.valueOf(totalSales));
            totalRevenueLabel.setText("$" + String.format("%.2f", totalRevenue));
            
            // Load recent sales
            List<Sale> recentSales = saleRepository.findAll();
            if (recentSales.size() > 10) {
                recentSales = recentSales.subList(0, 10);
            }
            recentSalesTable.setItems(FXCollections.observableArrayList(recentSales));
            
        } catch (SQLException e) {
            AlertUtils.showException("Error loading dashboard data", e);
        }
    }
}
