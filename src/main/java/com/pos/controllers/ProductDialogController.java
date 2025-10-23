package com.pos.controllers;

import com.pos.models.Category;
import com.pos.models.Product;
import com.pos.repository.CategoryRepository;
import com.pos.repository.ProductRepository;
import com.pos.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ProductDialogController implements Initializable {
    
    @FXML
    private TextField nameField;
    
    @FXML
    private ComboBox<Category> categoryCombo;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private TextField stockField;
    
    @FXML
    private TextField imagePathField;
    
    @FXML
    private Button browseImageBtn;
    
    @FXML
    private Button saveBtn;
    
    @FXML
    private Button cancelBtn;
    
    private Product product;
    private ProductController productController;
    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            categoryRepository = new CategoryRepository();
            productRepository = new ProductRepository();
            
            loadCategories();
            setupEventHandlers();
            
        } catch (Exception e) {
            AlertUtils.showException("Product Dialog Error", e);
        }
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            AlertUtils.showException("Error loading categories", e);
        }
    }
    
    private void setupEventHandlers() {
        browseImageBtn.setOnAction(e -> browseImage());
        saveBtn.setOnAction(e -> saveProduct());
        cancelBtn.setOnAction(e -> closeDialog());
    }
    
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            stockField.setText(String.valueOf(product.getStockQuantity()));
            imagePathField.setText(product.getImagePath());
            
            // Set category
            for (Category category : categoryCombo.getItems()) {
                if (category.getId() == product.getCategoryId()) {
                    categoryCombo.getSelectionModel().select(category);
                    break;
                }
            }
        }
    }
    
    public void setProductController(ProductController productController) {
        this.productController = productController;
    }
    
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(browseImageBtn.getScene().getWindow());
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    private void saveProduct() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Validation Error", "Product name is required.");
                return;
            }
            
            if (categoryCombo.getSelectionModel().getSelectedItem() == null) {
                AlertUtils.showWarning("Validation Error", "Please select a category.");
                return;
            }
            
            double price;
            try {
                price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    AlertUtils.showWarning("Validation Error", "Price must be greater than 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtils.showWarning("Validation Error", "Please enter a valid price.");
                return;
            }
            
            int stock;
            try {
                stock = Integer.parseInt(stockField.getText());
                if (stock < 0) {
                    AlertUtils.showWarning("Validation Error", "Stock quantity cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtils.showWarning("Validation Error", "Please enter a valid stock quantity.");
                return;
            }
            
            // Create or update product
            if (product == null) {
                // Create new product
                Product newProduct = new Product(
                    nameField.getText().trim(),
                    categoryCombo.getSelectionModel().getSelectedItem().getId(),
                    price,
                    stock,
                    imagePathField.getText().trim()
                );
                
                productRepository.save(newProduct);
                AlertUtils.showInfo("Success", "Product created successfully.");
            } else {
                // Update existing product
                product.setName(nameField.getText().trim());
                product.setCategoryId(categoryCombo.getSelectionModel().getSelectedItem().getId());
                product.setPrice(price);
                product.setStockQuantity(stock);
                product.setImagePath(imagePathField.getText().trim());
                
                productRepository.update(product);
                AlertUtils.showInfo("Success", "Product updated successfully.");
            }
            
            if (productController != null) {
                productController.refreshProducts();
            }
            
            closeDialog();
            
        } catch (SQLException e) {
            AlertUtils.showException("Error saving product", e);
        }
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
