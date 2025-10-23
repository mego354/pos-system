package com.pos.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:pos_system.db";
    private static Connection connection;
    
    public static void initialize() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTables();
        seedData();
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    
    private static void createTables() throws SQLException {
        String createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                description TEXT
            )
        """;
        
        String createProductsTable = """
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category_id INTEGER NOT NULL,
                price REAL NOT NULL CHECK(price > 0),
                stock_quantity INTEGER NOT NULL CHECK(stock_quantity >= 0),
                image_path TEXT,
                FOREIGN KEY (category_id) REFERENCES categories(id)
            )
        """;
        
        String createSalesTable = """
            CREATE TABLE IF NOT EXISTS sales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                total_amount REAL NOT NULL,
                sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String createSaleItemsTable = """
            CREATE TABLE IF NOT EXISTS sale_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sale_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                unit_price REAL NOT NULL CHECK(unit_price > 0),
                subtotal REAL NOT NULL CHECK(subtotal > 0),
                FOREIGN KEY (sale_id) REFERENCES sales(id),
                FOREIGN KEY (product_id) REFERENCES products(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCategoriesTable);
            stmt.execute(createProductsTable);
            stmt.execute(createSalesTable);
            stmt.execute(createSaleItemsTable);
        }
    }
    
    private static void seedData() throws SQLException {
        // Check if data already exists
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Data already exists
            }
        }
        
        // Insert sample categories
        String insertCategories = """
            INSERT INTO categories (name, description) VALUES
            ('Electronics', 'Electronic devices and gadgets'),
            ('Clothing', 'Apparel and fashion items'),
            ('Books', 'Books and educational materials'),
            ('Food & Beverages', 'Food and drink items'),
            ('Home & Garden', 'Home improvement and garden supplies')
        """;
        
        // Insert sample products
        String insertProducts = """
            INSERT INTO products (name, category_id, price, stock_quantity, image_path) VALUES
            ('Laptop Computer', 1, 999.99, 10, 'src/main/resources/images/placeholder.png'),
            ('Smartphone', 1, 599.99, 25, 'src/main/resources/images/placeholder.png'),
            ('T-Shirt', 2, 19.99, 50, 'src/main/resources/images/placeholder.png'),
            ('Jeans', 2, 49.99, 30, 'src/main/resources/images/placeholder.png'),
            ('Programming Book', 3, 39.99, 15, 'src/main/resources/images/placeholder.png'),
            ('Coffee', 4, 4.99, 100, 'src/main/resources/images/placeholder.png'),
            ('Garden Tools Set', 5, 79.99, 8, 'src/main/resources/images/placeholder.png')
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(insertCategories);
            stmt.execute(insertProducts);
        }
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
