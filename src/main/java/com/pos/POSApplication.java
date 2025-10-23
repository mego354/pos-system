package com.pos;

import com.pos.database.DatabaseManager;
import com.pos.models.*;
import com.pos.repository.*;
import com.pos.utils.AlertUtils;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class POSApplication extends Application {
    
    private Stage primaryStage;
    private BorderPane mainLayout;
    private StackPane contentArea;
    
    // Repositories
    private ProductRepository productRepo;
    private CategoryRepository categoryRepo;
    private SaleRepository saleRepo;
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        try {
            // Initialize database
            DatabaseManager.initialize();
            
            // Initialize repositories
            productRepo = new ProductRepository();
            categoryRepo = new CategoryRepository();
            saleRepo = new SaleRepository();
            
            // Create main layout
            mainLayout = new BorderPane();
            
            // Create sidebar
            VBox sidebar = createSidebar();
            mainLayout.setLeft(sidebar);
            
            // Create content area
            contentArea = new StackPane();
            mainLayout.setCenter(contentArea);
            
            // Load dashboard by default
            showDashboard();
            
            // Create scene
            Scene scene = new Scene(mainLayout, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            stage.setTitle("POS System - Complete Edition");
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();
            
        } catch (Exception e) {
            AlertUtils.showError("Startup Error", "Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.getStyleClass().add("sidebar");
        
        Label title = new Label("POS System");
        title.getStyleClass().add("sidebar-title");
        
        Button dashboardBtn = createSidebarButton("📊 Dashboard");
        Button productsBtn = createSidebarButton("📦 Products");
        Button categoriesBtn = createSidebarButton("📁 Categories");
        Button salesBtn = createSidebarButton("🛒 Sales");
        
        dashboardBtn.setOnAction(e -> showDashboard());
        productsBtn.setOnAction(e -> showProducts());
        categoriesBtn.setOnAction(e -> showCategories());
        salesBtn.setOnAction(e -> showSales());
        
        sidebar.getChildren().addAll(title, new Separator(), dashboardBtn, productsBtn, categoriesBtn, salesBtn);
        return sidebar;
    }
    
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-button");
        return btn;
    }
    
    // ==================== DASHBOARD ====================
    
    private void showDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));
        dashboard.getStyleClass().add("page-container");
        
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        
        // Statistics Grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        
        try {
            int totalProducts = productRepo.findAll().size();
            int totalCategories = categoryRepo.findAll().size();
            int totalSales = saleRepo.getTotalSalesCount();
            double totalRevenue = saleRepo.getTotalRevenue();
            
            statsGrid.add(createStatCard("Total Products", String.valueOf(totalProducts), "#0984e3"), 0, 0);
            statsGrid.add(createStatCard("Total Categories", String.valueOf(totalCategories), "#00b894"), 1, 0);
            statsGrid.add(createStatCard("Total Sales", String.valueOf(totalSales), "#fdcb6e"), 2, 0);
            statsGrid.add(createStatCard("Total Revenue", String.format("$%.2f", totalRevenue), "#e17055"), 3, 0);
            
            // Recent Sales Table
            Label recentLabel = new Label("Recent Sales");
            recentLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            TableView<Sale> recentSalesTable = new TableView<>();
            recentSalesTable.setMaxHeight(300);
            
            TableColumn<Sale, Integer> idCol = new TableColumn<>("Sale ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(80);
            
            TableColumn<Sale, String> amountCol = new TableColumn<>("Amount");
            amountCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotalAmount())));
            amountCol.setPrefWidth(100);
            
            TableColumn<Sale, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getSaleDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            dateCol.setPrefWidth(150);
            
            TableColumn<Sale, String> itemsCol = new TableColumn<>("Items");
            itemsCol.setCellValueFactory(cellData -> {
                try {
                    List<SaleItem> items = saleRepo.findSaleItems(cellData.getValue().getId());
                    return new SimpleStringProperty(String.valueOf(items.size()));
                } catch (SQLException e) {
                    return new SimpleStringProperty("0");
                }
            });
            itemsCol.setPrefWidth(60);
            
            TableColumn<Sale, Void> detailsCol = new TableColumn<>("Details");
            detailsCol.setPrefWidth(80);
            detailsCol.setCellFactory(col -> new TableCell<>() {
                private final Button detailsBtn = new Button("View");
                {
                    detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Sale sale = getTableView().getItems().get(getIndex());
                        detailsBtn.setOnAction(e -> showSaleDetails(sale));
                        setGraphic(detailsBtn);
                    }
                }
            });
            
            recentSalesTable.getColumns().addAll(idCol, amountCol, dateCol, itemsCol, detailsCol);
            
            List<Sale> sales = saleRepo.findAll();
            if (sales.size() > 10) sales = sales.subList(0, 10);
            recentSalesTable.setItems(FXCollections.observableArrayList(sales));
            
            dashboard.getChildren().addAll(title, statsGrid, recentLabel, recentSalesTable);
            
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load dashboard data");
            e.printStackTrace();
        }
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboard);
    }
    
    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("stat-card");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-label");
        
        card.getChildren().addAll(valueLabel, labelText);
        return card;
    }
    
    // ==================== PRODUCTS ====================
    
    private void showProducts() {
        VBox productsView = new VBox(15);
        productsView.setPadding(new Insets(20));
        productsView.getStyleClass().add("page-container");
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Products Management");
        title.getStyleClass().add("page-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("➕ Add Product");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> showAddProductDialog());
        
        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().add("secondary-button");
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().add("danger-button");
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("secondary-button");
        
        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, refreshBtn);
        
        // Products Table
        TableView<Product> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(150);
        
        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getPrice())));
        priceCol.setPrefWidth(100);
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        stockCol.setPrefWidth(80);
        
        TableColumn<Product, String> imageCol = new TableColumn<>("Image");
        imageCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getImagePath() != null ? "✓" : "✗"));
        imageCol.setPrefWidth(80);
        
        // Add image display column
        TableColumn<Product, Void> imageDisplayCol = new TableColumn<>("Preview");
        imageDisplayCol.setPrefWidth(100);
        imageDisplayCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                        try {
                            File imageFile = new File(product.getImagePath());
                            if (imageFile.exists()) {
                                Image image = new Image(imageFile.toURI().toString());
                                imageView.setImage(image);
                            } else {
                                imageView.setImage(null);
                            }
                        } catch (Exception e) {
                            imageView.setImage(null);
                        }
                    } else {
                        imageView.setImage(null);
                    }
                    setGraphic(imageView);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, categoryCol, priceCol, stockCol, imageCol, imageDisplayCol);
        
        // Load data
        try {
            table.setItems(FXCollections.observableArrayList(productRepo.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load products");
        }
        
        // Button actions
        editBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditProductDialog(selected, table);
            } else {
                AlertUtils.showWarning("No Selection", "Please select a product to edit");
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (AlertUtils.showConfirmation("Delete Product", 
                    "Are you sure you want to delete '" + selected.getName() + "'?")) {
                    try {
                        productRepo.delete(selected.getId());
                        table.setItems(FXCollections.observableArrayList(productRepo.findAll()));
                        AlertUtils.showInfo("Success", "Product deleted successfully");
                    } catch (SQLException ex) {
                        AlertUtils.showError("Error", "Failed to delete product");
                    }
                }
            } else {
                AlertUtils.showWarning("No Selection", "Please select a product to delete");
            }
        });
        
        refreshBtn.setOnAction(e -> {
            try {
                table.setItems(FXCollections.observableArrayList(productRepo.findAll()));
            } catch (SQLException ex) {
                AlertUtils.showError("Error", "Failed to refresh products");
            }
        });
        
        productsView.getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(productsView);
    }
    
    private void showAddProductDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Product");
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        
        Label categoryLabel = new Label("Category:");
        ComboBox<Category> categoryCombo = new ComboBox<>();
        try {
            categoryCombo.setItems(FXCollections.observableArrayList(categoryRepo.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Failed to load categories");
        }
        
        Label priceLabel = new Label("Price:");
        TextField priceField = new TextField();
        
        Label stockLabel = new Label("Stock:");
        TextField stockField = new TextField();
        
        Label imageLabel = new Label("Image:");
        TextField imageField = new TextField();
        imageField.setEditable(false);
        Button browseBtn = new Button("Browse...");
        HBox imageBox = new HBox(5, imageField, browseBtn);
        HBox.setHgrow(imageField, Priority.ALWAYS);
        
        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                imageField.setText(file.getAbsolutePath());
            }
        });
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(categoryLabel, 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(priceLabel, 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(stockLabel, 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(imageLabel, 0, 4);
        grid.add(imageBox, 1, 4);
        
        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("primary-button");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 0, 5, 2, 1);
        
        saveBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                Category category = categoryCombo.getValue();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String imagePath = imageField.getText();
                
                if (name.isEmpty() || category == null) {
                    AlertUtils.showWarning("Validation", "Please fill all required fields");
                    return;
                }
                
                if (price <= 0 || stock < 0) {
                    AlertUtils.showWarning("Validation", "Price must be > 0 and stock >= 0");
                    return;
                }
                
                Product product = new Product(name, category.getId(), price, stock, imagePath);
                productRepo.save(product);
                AlertUtils.showInfo("Success", "Product added successfully");
                dialog.close();
                showProducts(); // Refresh
                
            } catch (NumberFormatException ex) {
                AlertUtils.showWarning("Validation", "Invalid price or stock value");
            } catch (SQLException ex) {
                AlertUtils.showError("Error", "Failed to save product: " + ex.getMessage());
            }
        });
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        Scene scene = new Scene(grid, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void showEditProductDialog(Product product, TableView<Product> table) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Product");
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nameField = new TextField(product.getName());
        ComboBox<Category> categoryCombo = new ComboBox<>();
        TextField priceField = new TextField(String.valueOf(product.getPrice()));
        TextField stockField = new TextField(String.valueOf(product.getStockQuantity()));
        TextField imageField = new TextField(product.getImagePath() != null ? product.getImagePath() : "");
        imageField.setEditable(false);
        
        try {
            List<Category> categories = categoryRepo.findAll();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
            for (Category cat : categories) {
                if (cat.getId() == product.getCategoryId()) {
                    categoryCombo.setValue(cat);
                    break;
                }
            }
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Failed to load categories");
        }
        
        Button browseBtn = new Button("Browse...");
        HBox imageBox = new HBox(5, imageField, browseBtn);
        HBox.setHgrow(imageField, Priority.ALWAYS);
        
        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                imageField.setText(file.getAbsolutePath());
            }
        });
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Image:"), 0, 4);
        grid.add(imageBox, 1, 4);
        
        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("primary-button");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 0, 5, 2, 1);
        
        saveBtn.setOnAction(e -> {
            try {
                product.setName(nameField.getText().trim());
                product.setCategoryId(categoryCombo.getValue().getId());
                product.setPrice(Double.parseDouble(priceField.getText()));
                product.setStockQuantity(Integer.parseInt(stockField.getText()));
                product.setImagePath(imageField.getText());
                
                productRepo.update(product);
                AlertUtils.showInfo("Success", "Product updated successfully");
                dialog.close();
                table.setItems(FXCollections.observableArrayList(productRepo.findAll()));
                
            } catch (Exception ex) {
                AlertUtils.showError("Error", "Failed to update product: " + ex.getMessage());
            }
        });
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        Scene scene = new Scene(grid, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    // ==================== CATEGORIES ====================
    
    private void showCategories() {
        VBox categoriesView = new VBox(15);
        categoriesView.setPadding(new Insets(20));
        categoriesView.getStyleClass().add("page-container");
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Categories Management");
        title.getStyleClass().add("page-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("➕ Add Category");
        addBtn.getStyleClass().add("primary-button");
        
        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyleClass().add("secondary-button");
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyleClass().add("danger-button");
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("secondary-button");
        
        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, refreshBtn);
        
        // Categories Table
        TableView<Category> table = new TableView<>();
        table.getStyleClass().add("data-table");
        
        TableColumn<Category, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        TableColumn<Category, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Category, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(400);
        
        table.getColumns().addAll(idCol, nameCol, descCol);
        
        // Load data
        try {
            table.setItems(FXCollections.observableArrayList(categoryRepo.findAll()));
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load categories");
        }
        
        // Button actions
        addBtn.setOnAction(e -> showAddCategoryDialog(table));
        
        editBtn.setOnAction(e -> {
            Category selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditCategoryDialog(selected, table);
            } else {
                AlertUtils.showWarning("No Selection", "Please select a category to edit");
            }
        });
        
        deleteBtn.setOnAction(e -> {
            Category selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    if (categoryRepo.hasProducts(selected.getId())) {
                        AlertUtils.showWarning("Cannot Delete", 
                            "This category has products. Please reassign or delete them first.");
                        return;
                    }
                    
                    if (AlertUtils.showConfirmation("Delete Category", 
                        "Are you sure you want to delete '" + selected.getName() + "'?")) {
                        categoryRepo.delete(selected.getId());
                        table.setItems(FXCollections.observableArrayList(categoryRepo.findAll()));
                        AlertUtils.showInfo("Success", "Category deleted successfully");
                    }
                } catch (SQLException ex) {
                    AlertUtils.showError("Error", "Failed to delete category");
                }
            } else {
                AlertUtils.showWarning("No Selection", "Please select a category to delete");
            }
        });
        
        refreshBtn.setOnAction(e -> {
            try {
                table.setItems(FXCollections.observableArrayList(categoryRepo.findAll()));
            } catch (SQLException ex) {
                AlertUtils.showError("Error", "Failed to refresh categories");
            }
        });
        
        categoriesView.getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(categoriesView);
    }
    
    private void showAddCategoryDialog(TableView<Category> table) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Category");
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nameField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        
        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("primary-button");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 0, 2, 2, 1);
        
        saveBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    AlertUtils.showWarning("Validation", "Name is required");
                    return;
                }
                
                Category category = new Category(name, descField.getText().trim());
                categoryRepo.save(category);
                AlertUtils.showInfo("Success", "Category added successfully");
                dialog.close();
                table.setItems(FXCollections.observableArrayList(categoryRepo.findAll()));
                
            } catch (SQLException ex) {
                AlertUtils.showError("Error", "Failed to save category: " + ex.getMessage());
            }
        });
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        Scene scene = new Scene(grid, 400, 250);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void showEditCategoryDialog(Category category, TableView<Category> table) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Category");
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField nameField = new TextField(category.getName());
        TextArea descField = new TextArea(category.getDescription());
        descField.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        
        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("primary-button");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 0, 2, 2, 1);
        
        saveBtn.setOnAction(e -> {
            try {
                category.setName(nameField.getText().trim());
                category.setDescription(descField.getText().trim());
                
                categoryRepo.update(category);
                AlertUtils.showInfo("Success", "Category updated successfully");
                dialog.close();
                table.setItems(FXCollections.observableArrayList(categoryRepo.findAll()));
                
            } catch (SQLException ex) {
                AlertUtils.showError("Error", "Failed to update category: " + ex.getMessage());
            }
        });
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        Scene scene = new Scene(grid, 400, 250);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    // ==================== SALES ====================
    
    private void showSales() {
        HBox salesView = new HBox(20);
        salesView.setPadding(new Insets(20));
        
        // Left side - Products
        VBox leftSide = new VBox(15);
        leftSide.setPrefWidth(500);
        
        Label productsTitle = new Label("Products");
        productsTitle.getStyleClass().add("section-title");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search products...");
        searchField.getStyleClass().add("search-field");
        
        ComboBox<Category> categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("Filter by category");
        try {
            List<Category> categories = categoryRepo.findAll();
            categories.add(0, new Category(0, "All Categories", ""));
            categoryFilter.setItems(FXCollections.observableArrayList(categories));
            categoryFilter.setValue(categories.get(0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        TableView<Product> productsTable = new TableView<>();
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getPrice())));
        priceCol.setPrefWidth(80);
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        stockCol.setPrefWidth(60);
        
        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("Add to Cart");
            {
                addBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    addBtn.setOnAction(e -> addToCart(product));
                    setGraphic(addBtn);
                }
            }
        });
        
        productsTable.getColumns().addAll(nameCol, priceCol, stockCol, actionCol);
        
        // Load products
        try {
            productsTable.setItems(FXCollections.observableArrayList(productRepo.findAll()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Search functionality
        searchField.textProperty().addListener((obs, old, newVal) -> {
            try {
                if (newVal.trim().isEmpty()) {
                    productsTable.setItems(FXCollections.observableArrayList(productRepo.findAll()));
                } else {
                    productsTable.setItems(FXCollections.observableArrayList(
                        productRepo.searchByName(newVal)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        
        // Category filter
        categoryFilter.setOnAction(e -> {
            try {
                Category selected = categoryFilter.getValue();
                if (selected != null && selected.getId() == 0) {
                    productsTable.setItems(FXCollections.observableArrayList(productRepo.findAll()));
                } else if (selected != null) {
                    productsTable.setItems(FXCollections.observableArrayList(
                        productRepo.findByCategory(selected.getId())));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        
        leftSide.getChildren().addAll(productsTitle, searchField, categoryFilter, productsTable);
        VBox.setVgrow(productsTable, Priority.ALWAYS);
        
        // Right side - Cart
        VBox rightSide = new VBox(15);
        rightSide.setPrefWidth(400);
        rightSide.getStyleClass().add("cart-section");
        
        Label cartTitle = new Label("Shopping Cart");
        cartTitle.getStyleClass().add("section-title");
        
        TableView<SaleItem> cartTable = new TableView<>();
        
        TableColumn<SaleItem, String> cartNameCol = new TableColumn<>("Product");
        cartNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        cartNameCol.setPrefWidth(150);
        
        TableColumn<SaleItem, Void> qtyCol = new TableColumn<>("Qty");
        qtyCol.setPrefWidth(100);
        qtyCol.setCellFactory(col -> new TableCell<>() {
            private final HBox qtyBox = new HBox(5);
            private final Button minusBtn = new Button("-");
            private final Label qtyLabel = new Label();
            private final Button plusBtn = new Button("+");
            
            {
                minusBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-min-width: 25;");
                plusBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-min-width: 25;");
                qtyLabel.setMinWidth(30);
                qtyLabel.setAlignment(Pos.CENTER);
                qtyBox.setAlignment(Pos.CENTER);
                qtyBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SaleItem saleItem = getTableView().getItems().get(getIndex());
                    qtyLabel.setText(String.valueOf(saleItem.getQuantity()));
                    
                    minusBtn.setOnAction(e -> {
                        if (saleItem.getQuantity() > 1) {
                            saleItem.setQuantity(saleItem.getQuantity() - 1);
                            qtyLabel.setText(String.valueOf(saleItem.getQuantity()));
                            cartTable.refresh(); // Refresh table to update subtotal
                            updateTotal(null);
                        } else {
                            cartItems.remove(saleItem);
                            updateTotal(null);
                        }
                    });
                    
                    plusBtn.setOnAction(e -> {
                        // Check stock limit
                        try {
                            Product product = productRepo.findById(saleItem.getProductId());
                            if (product != null && saleItem.getQuantity() < product.getStockQuantity()) {
                                saleItem.setQuantity(saleItem.getQuantity() + 1);
                                qtyLabel.setText(String.valueOf(saleItem.getQuantity()));
                                cartTable.refresh(); // Refresh table to update subtotal
                                updateTotal(null);
                            } else {
                                AlertUtils.showWarning("Stock Limit", "Cannot add more. Stock limit reached.");
                            }
                        } catch (SQLException ex) {
                            AlertUtils.showError("Error", "Failed to check stock");
                        }
                    });
                    
                    setGraphic(qtyBox);
                }
            }
        });
        
        TableColumn<SaleItem, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(cellData -> {
            SaleItem item = cellData.getValue();
            return new SimpleStringProperty(String.format("$%.2f", item.getQuantity() * item.getUnitPrice()));
        });
        subtotalCol.setPrefWidth(80);
        
        Label totalLabel = new Label("Total: $0.00");
        totalLabel.getStyleClass().add("total-label");
        
        TableColumn<SaleItem, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setPrefWidth(80);
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("✖");
            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SaleItem saleItem = getTableView().getItems().get(getIndex());
                    removeBtn.setOnAction(e -> {
                        cartItems.remove(saleItem);
                        updateTotal(totalLabel);
                    });
                    setGraphic(removeBtn);
                }
            }
        });
        
        cartTable.getColumns().addAll(cartNameCol, qtyCol, subtotalCol, removeCol);
        cartTable.setItems(cartItems);
        
        // Add listener to refresh table when items change
        cartItems.addListener((javafx.collections.ListChangeListener<SaleItem>) change -> {
            while (change.next()) {
                if (change.wasUpdated()) {
                    // Refresh the table to show updated subtotals
                    cartTable.refresh();
                }
            }
        });
        
        Button processBtn = new Button("💳 Process Sale");
        processBtn.setMaxWidth(Double.MAX_VALUE);
        processBtn.getStyleClass().add("success-button");
        processBtn.setOnAction(e -> processSale(totalLabel));
        
        Button clearBtn = new Button("Clear Cart");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.getStyleClass().add("danger-button");
        clearBtn.setOnAction(e -> {
            cartItems.clear();
            updateTotal(totalLabel);
        });
        
        rightSide.getChildren().addAll(cartTitle, cartTable, totalLabel, processBtn, clearBtn);
        VBox.setVgrow(cartTable, Priority.ALWAYS);
        
        salesView.getChildren().addAll(leftSide, rightSide);
        HBox.setHgrow(leftSide, Priority.ALWAYS);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(salesView);
    }
    
    private ObservableList<SaleItem> cartItems = FXCollections.observableArrayList();
    
    private void addToCart(Product product) {
        if (product.getStockQuantity() <= 0) {
            AlertUtils.showWarning("Out of Stock", "This product is out of stock");
            return;
        }
        
        // Check if already in cart
        for (SaleItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                if (item.getQuantity() >= product.getStockQuantity()) {
                    AlertUtils.showWarning("Stock Limit", "Cannot add more. Stock limit reached.");
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                // Force table refresh to update subtotal
                cartItems.set(cartItems.indexOf(item), item);
                updateTotal(null);
                return;
            }
        }
        
        // Add new item
        SaleItem newItem = new SaleItem(product.getId(), product.getName(), 1, product.getPrice());
        cartItems.add(newItem);
        updateTotal(null);
    }
    
    private void updateTotal(Label totalLabel) {
        double total = cartItems.stream().mapToDouble(item -> item.getQuantity() * item.getUnitPrice()).sum();
        if (totalLabel != null) {
            totalLabel.setText(String.format("Total: $%.2f", total));
        }
        
        // Find and update any total label in the scene
        contentArea.lookupAll(".total-label").forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setText(String.format("Total: $%.2f", total));
            }
        });
    }
    
    private void showSaleDetails(Sale sale) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Sale Details - #" + sale.getId());
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Sale header
        HBox header = new HBox(20);
        Label saleIdLabel = new Label("Sale ID: #" + sale.getId());
        saleIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label dateLabel = new Label("Date: " + sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        Label totalLabel = new Label("Total: $" + String.format("%.2f", sale.getTotalAmount()));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        header.getChildren().addAll(saleIdLabel, dateLabel, totalLabel);
        
        // Items table
        TableView<SaleItem> itemsTable = new TableView<>();
        
        TableColumn<SaleItem, String> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productCol.setPrefWidth(200);
        
        TableColumn<SaleItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(60);
        
        TableColumn<SaleItem, String> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getUnitPrice())));
        priceCol.setPrefWidth(100);
        
        TableColumn<SaleItem, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getSubtotal())));
        subtotalCol.setPrefWidth(100);
        
        TableColumn<SaleItem, Void> imageCol = new TableColumn<>("Image");
        imageCol.setPrefWidth(80);
        imageCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SaleItem saleItem = getTableView().getItems().get(getIndex());
                    try {
                        Product product = productRepo.findById(saleItem.getProductId());
                        if (product != null && product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                            File imageFile = new File(product.getImagePath());
                            if (imageFile.exists()) {
                                Image image = new Image(imageFile.toURI().toString());
                                imageView.setImage(image);
                            } else {
                                imageView.setImage(null);
                            }
                        } else {
                            imageView.setImage(null);
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                    setGraphic(imageView);
                }
            }
        });
        
        itemsTable.getColumns().addAll(productCol, qtyCol, priceCol, subtotalCol, imageCol);
        
        try {
            List<SaleItem> items = saleRepo.findSaleItems(sale.getId());
            itemsTable.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Failed to load sale items");
        }
        
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("primary-button");
        closeBtn.setOnAction(e -> dialog.close());
        
        content.getChildren().addAll(header, itemsTable, closeBtn);
        VBox.setVgrow(itemsTable, Priority.ALWAYS);
        
        Scene scene = new Scene(content, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void processSale(Label totalLabel) {
        if (cartItems.isEmpty()) {
            AlertUtils.showWarning("Empty Cart", "Please add items to cart");
            return;
        }
        
        try {
            // Validate stock
            for (SaleItem item : cartItems) {
                Product product = productRepo.findById(item.getProductId());
                if (product == null || product.getStockQuantity() < item.getQuantity()) {
                    AlertUtils.showError("Insufficient Stock", 
                        "Not enough stock for " + item.getProductName());
                    return;
                }
            }
            
            // Create sale
            double totalAmount = cartItems.stream().mapToDouble(item -> item.getQuantity() * item.getUnitPrice()).sum();
            Sale sale = new Sale(totalAmount);
            int saleId = saleRepo.save(sale);
            
            // Save sale items and update stock
            List<SaleItem> itemsList = new ArrayList<>(cartItems);
            for (SaleItem item : itemsList) {
                item.setSaleId(saleId);
                item.setSubtotal(item.getQuantity() * item.getUnitPrice()); // Calculate subtotal
            }
            saleRepo.saveSaleItems(saleId, itemsList);
            
            // Update stock
            for (SaleItem item : cartItems) {
                Product product = productRepo.findById(item.getProductId());
                productRepo.updateStock(item.getProductId(), 
                    product.getStockQuantity() - item.getQuantity());
            }
            
            // Clear cart
            cartItems.clear();
            updateTotal(totalLabel);
            
            AlertUtils.showInfo("Sale Completed", 
                String.format("Sale #%d completed successfully!\nTotal: $%.2f", saleId, totalAmount));
            
        } catch (SQLException e) {
            AlertUtils.showError("Error", "Failed to process sale: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch();
    }
}

