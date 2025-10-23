package com.pos.controllers;

import com.pos.models.Category;
import com.pos.models.Product;
import com.pos.repository.CategoryRepository;
import com.pos.repository.ProductRepository;
import com.pos.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    
    @FXML
    private TableView<Product> productsTable;
    
    @FXML
    private TableColumn<Product, ImageView> imageColumn;
    
    @FXML
    private TableColumn<Product, String> nameColumn;
    
    @FXML
    private TableColumn<Product, String> categoryColumn;
    
    @FXML
    private TableColumn<Product, Double> priceColumn;
    
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    
    @FXML
    private Button addProductBtn;
    
    @FXML
    private Button editProductBtn;
    
    @FXML
    private Button deleteProductBtn;
    
    @FXML
    private Button refreshBtn;
    
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            productRepository = new ProductRepository();
            categoryRepository = new CategoryRepository();
            
            setupTableColumns();
            loadProducts();
            setupEventHandlers();
            
        } catch (Exception e) {
            AlertUtils.showException("Product Controller Error", e);
        }
    }
    
    private void setupTableColumns() {
        imageColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            ImageView imageView = new ImageView();
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);
            
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                try {
                    Image image = new Image("file:" + product.getImagePath());
                    imageView.setImage(image);
                } catch (Exception e) {
                    // Use default image or placeholder
                    imageView.setImage(new Image("/images/placeholder.png"));
                }
            }
            
            return new javafx.beans.property.SimpleObjectProperty<>(imageView);
        });
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.2f", item));
                }
            }
        });
    }
    
    private void setupEventHandlers() {
        addProductBtn.setOnAction(e -> showProductDialog(null));
        editProductBtn.setOnAction(e -> {
            Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                showProductDialog(selectedProduct);
            } else {
                AlertUtils.showWarning("No Selection", "Please select a product to edit.");
            }
        });
        
        deleteProductBtn.setOnAction(e -> {
            Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                if (AlertUtils.showConfirmation("Delete Product", 
                    "Are you sure you want to delete '" + selectedProduct.getName() + "'?")) {
                    deleteProduct(selectedProduct);
                }
            } else {
                AlertUtils.showWarning("No Selection", "Please select a product to delete.");
            }
        });
        
        refreshBtn.setOnAction(e -> loadProducts());
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productRepository.findAll();
            productsTable.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            AlertUtils.showException("Error loading products", e);
        }
    }
    
    private void showProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductDialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            ProductDialogController controller = loader.getController();
            controller.setProduct(product);
            controller.setProductController(this);
            
            Stage stage = new Stage();
            stage.setTitle(product == null ? "Add Product" : "Edit Product");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            AlertUtils.showException("Error opening product dialog", e);
        }
    }
    
    private void deleteProduct(Product product) {
        try {
            if (productRepository.delete(product.getId())) {
                AlertUtils.showInfo("Success", "Product deleted successfully.");
                loadProducts();
            } else {
                AlertUtils.showError("Error", "Failed to delete product.");
            }
        } catch (SQLException e) {
            AlertUtils.showException("Error deleting product", e);
        }
    }
    
    public void refreshProducts() {
        loadProducts();
    }
}
