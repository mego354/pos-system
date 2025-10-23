package com.pos.controllers;

import com.pos.models.Category;
import com.pos.repository.CategoryRepository;
import com.pos.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {
    
    @FXML
    private TableView<Category> categoriesTable;
    
    @FXML
    private TableColumn<Category, Integer> idColumn;
    
    @FXML
    private TableColumn<Category, String> nameColumn;
    
    @FXML
    private TableColumn<Category, String> descriptionColumn;
    
    @FXML
    private Button addCategoryBtn;
    
    @FXML
    private Button editCategoryBtn;
    
    @FXML
    private Button deleteCategoryBtn;
    
    @FXML
    private Button refreshBtn;
    
    private CategoryRepository categoryRepository;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            categoryRepository = new CategoryRepository();
            
            setupTableColumns();
            loadCategories();
            setupEventHandlers();
            
        } catch (Exception e) {
            AlertUtils.showException("Category Controller Error", e);
        }
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }
    
    private void setupEventHandlers() {
        addCategoryBtn.setOnAction(e -> showCategoryDialog(null));
        editCategoryBtn.setOnAction(e -> {
            Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                showCategoryDialog(selectedCategory);
            } else {
                AlertUtils.showWarning("No Selection", "Please select a category to edit.");
            }
        });
        
        deleteCategoryBtn.setOnAction(e -> {
            Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                if (AlertUtils.showConfirmation("Delete Category", 
                    "Are you sure you want to delete '" + selectedCategory.getName() + "'?")) {
                    deleteCategory(selectedCategory);
                }
            } else {
                AlertUtils.showWarning("No Selection", "Please select a category to delete.");
            }
        });
        
        refreshBtn.setOnAction(e -> loadCategories());
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            categoriesTable.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            AlertUtils.showException("Error loading categories", e);
        }
    }
    
    private void showCategoryDialog(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryDialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            CategoryDialogController controller = loader.getController();
            controller.setCategory(category);
            controller.setCategoryController(this);
            
            Stage stage = new Stage();
            stage.setTitle(category == null ? "Add Category" : "Edit Category");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            AlertUtils.showException("Error opening category dialog", e);
        }
    }
    
    private void deleteCategory(Category category) {
        try {
            if (categoryRepository.hasProducts(category.getId())) {
                AlertUtils.showWarning("Cannot Delete", 
                    "This category has products associated with it. Please remove or reassign the products first.");
                return;
            }
            
            if (categoryRepository.delete(category.getId())) {
                AlertUtils.showInfo("Success", "Category deleted successfully.");
                loadCategories();
            } else {
                AlertUtils.showError("Error", "Failed to delete category.");
            }
        } catch (SQLException e) {
            AlertUtils.showException("Error deleting category", e);
        }
    }
    
    public void refreshCategories() {
        loadCategories();
    }
}
