package com.pos.controllers;

import com.pos.models.Category;
import com.pos.models.Product;
import com.pos.models.Sale;
import com.pos.models.SaleItem;
import com.pos.repository.CategoryRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.SaleRepository;
import com.pos.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SalesController implements Initializable {
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<Category> categoryFilter;
    
    @FXML
    private TableView<Product> productsTable;
    
    @FXML
    private TableColumn<Product, String> productNameColumn;
    
    @FXML
    private TableColumn<Product, String> productCategoryColumn;
    
    @FXML
    private TableColumn<Product, Double> productPriceColumn;
    
    @FXML
    private TableColumn<Product, Integer> productStockColumn;
    
    @FXML
    private TableColumn<Product, Button> addToCartColumn;
    
    @FXML
    private TableView<SaleItem> cartTable;
    
    @FXML
    private TableColumn<SaleItem, String> cartProductColumn;
    
    @FXML
    private TableColumn<SaleItem, Integer> cartQuantityColumn;
    
    @FXML
    private TableColumn<SaleItem, Double> cartPriceColumn;
    
    @FXML
    private TableColumn<SaleItem, Double> cartSubtotalColumn;
    
    @FXML
    private TableColumn<SaleItem, Button> removeFromCartColumn;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button searchBtn;
    
    @FXML
    private Button processSaleBtn;
    
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private SaleRepository saleRepository;
    private ObservableList<SaleItem> cartItems;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            productRepository = new ProductRepository();
            categoryRepository = new CategoryRepository();
            saleRepository = new SaleRepository();
            
            cartItems = FXCollections.observableArrayList();
            
            setupTableColumns();
            loadProducts();
            loadCategories();
            setupEventHandlers();
            
        } catch (Exception e) {
            AlertUtils.showException("Sales Controller Error", e);
        }
    }
    
    private void setupTableColumns() {
        // Products table
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Format price column
        productPriceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
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
        
        // Add to cart button column
        addToCartColumn.setCellFactory(column -> new TableCell<Product, Button>() {
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    Button addBtn = new Button("Add to Cart");
                    addBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-background-radius: 4;");
                    addBtn.setOnAction(e -> addToCart(product));
                    setGraphic(addBtn);
                }
            }
        });
        
        // Cart table
        cartProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        cartSubtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        
        // Format cart price columns
        cartPriceColumn.setCellFactory(column -> new TableCell<SaleItem, Double>() {
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
        
        cartSubtotalColumn.setCellFactory(column -> new TableCell<SaleItem, Double>() {
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
        
        // Remove from cart button column
        removeFromCartColumn.setCellFactory(column -> new TableCell<SaleItem, Button>() {
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SaleItem saleItem = getTableView().getItems().get(getIndex());
                    Button removeBtn = new Button("Remove");
                    removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4;");
                    removeBtn.setOnAction(e -> removeFromCart(saleItem));
                    setGraphic(removeBtn);
                }
            }
        });
        
        cartTable.setItems(cartItems);
    }
    
    private void setupEventHandlers() {
        searchBtn.setOnAction(e -> searchProducts());
        processSaleBtn.setOnAction(e -> processSale());
        
        // Search on Enter key
        searchField.setOnAction(e -> searchProducts());
        
        // Filter by category
        categoryFilter.setOnAction(e -> filterByCategory());
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productRepository.findAll();
            productsTable.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            AlertUtils.showException("Error loading products", e);
        }
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            categoryFilter.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            AlertUtils.showException("Error loading categories", e);
        }
    }
    
    private void searchProducts() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }
        
        try {
            List<Product> products = productRepository.searchByName(searchTerm);
            productsTable.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            AlertUtils.showException("Error searching products", e);
        }
    }
    
    private void filterByCategory() {
        Category selectedCategory = categoryFilter.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            loadProducts();
            return;
        }
        
        try {
            List<Product> products = productRepository.findByCategory(selectedCategory.getId());
            productsTable.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            AlertUtils.showException("Error filtering products", e);
        }
    }
    
    private void addToCart(Product product) {
        if (product.getStockQuantity() <= 0) {
            AlertUtils.showWarning("Out of Stock", "This product is out of stock.");
            return;
        }
        
        // Check if product is already in cart
        for (SaleItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                if (item.getQuantity() >= product.getStockQuantity()) {
                    AlertUtils.showWarning("Stock Limit", "Cannot add more items. Stock limit reached.");
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                updateTotal();
                return;
            }
        }
        
        // Add new item to cart
        SaleItem newItem = new SaleItem(product.getId(), product.getName(), 1, product.getPrice());
        cartItems.add(newItem);
        updateTotal();
    }
    
    private void removeFromCart(SaleItem item) {
        cartItems.remove(item);
        updateTotal();
    }
    
    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(SaleItem::getSubtotal).sum();
        totalLabel.setText("$" + String.format("%.2f", total));
    }
    
    private void processSale() {
        if (cartItems.isEmpty()) {
            AlertUtils.showWarning("Empty Cart", "Please add items to cart before processing sale.");
            return;
        }
        
        try {
            // Validate stock
            for (SaleItem item : cartItems) {
                Product product = productRepository.findById(item.getProductId());
                if (product == null) {
                    AlertUtils.showError("Product Not Found", "One or more products in cart no longer exist.");
                    return;
                }
                if (product.getStockQuantity() < item.getQuantity()) {
                    AlertUtils.showError("Insufficient Stock", 
                        "Insufficient stock for " + item.getProductName() + 
                        ". Available: " + product.getStockQuantity() + ", Required: " + item.getQuantity());
                    return;
                }
            }
            
            // Create sale
            double totalAmount = cartItems.stream().mapToDouble(SaleItem::getSubtotal).sum();
            Sale sale = new Sale(totalAmount);
            int saleId = saleRepository.save(sale);
            
            // Save sale items
            List<SaleItem> itemsToSave = new ArrayList<>();
            for (SaleItem item : cartItems) {
                item.setSaleId(saleId);
                itemsToSave.add(item);
            }
            saleRepository.saveSaleItems(saleId, itemsToSave);
            
            // Update stock
            for (SaleItem item : cartItems) {
                Product product = productRepository.findById(item.getProductId());
                int newStock = product.getStockQuantity() - item.getQuantity();
                productRepository.updateStock(item.getProductId(), newStock);
            }
            
            // Clear cart
            cartItems.clear();
            updateTotal();
            
            AlertUtils.showInfo("Sale Processed", "Sale completed successfully. Total: $" + String.format("%.2f", totalAmount));
            
        } catch (SQLException e) {
            AlertUtils.showException("Error processing sale", e);
        }
    }
}
