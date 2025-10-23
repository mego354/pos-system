package com.pos.controllers;

import com.pos.models.Category;
import com.pos.repository.CategoryRepository;
import com.pos.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CategoryDialogController implements Initializable {
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextArea descriptionField;
    
    @FXML
    private Button saveBtn;
    
    @FXML
    private Button cancelBtn;
    
    private Category category;
    private CategoryController categoryController;
    private CategoryRepository categoryRepository;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            categoryRepository = new CategoryRepository();
            setupEventHandlers();
            
        } catch (Exception e) {
            AlertUtils.showException("Category Dialog Error", e);
        }
    }
    
    private void setupEventHandlers() {
        saveBtn.setOnAction(e -> saveCategory());
        cancelBtn.setOnAction(e -> closeDialog());
    }
    
    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            nameField.setText(category.getName());
            descriptionField.setText(category.getDescription());
        }
    }
    
    public void setCategoryController(CategoryController categoryController) {
        this.categoryController = categoryController;
    }
    
    private void saveCategory() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Validation Error", "Category name is required.");
                return;
            }
            
            // Check if category name already exists (for new categories)
            if (category == null) {
                Category existingCategory = categoryRepository.findByName(nameField.getText().trim());
                if (existingCategory != null) {
                    AlertUtils.showWarning("Validation Error", "A category with this name already exists.");
                    return;
                }
            }
            
            // Create or update category
            if (category == null) {
                // Create new category
                Category newCategory = new Category(
                    nameField.getText().trim(),
                    descriptionField.getText().trim()
                );
                
                categoryRepository.save(newCategory);
                AlertUtils.showInfo("Success", "Category created successfully.");
            } else {
                // Update existing category
                category.setName(nameField.getText().trim());
                category.setDescription(descriptionField.getText().trim());
                
                categoryRepository.update(category);
                AlertUtils.showInfo("Success", "Category updated successfully.");
            }
            
            if (categoryController != null) {
                categoryController.refreshCategories();
            }
            
            closeDialog();
            
        } catch (SQLException e) {
            AlertUtils.showException("Error saving category", e);
        }
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
