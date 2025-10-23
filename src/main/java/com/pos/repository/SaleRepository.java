package com.pos.repository;

import com.pos.database.DatabaseManager;
import com.pos.models.Sale;
import com.pos.models.SaleItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleRepository {
    
    public List<Sale> findAll() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                sales.add(new Sale(
                    rs.getInt("id"),
                    rs.getDouble("total_amount"),
                    rs.getTimestamp("sale_date").toLocalDateTime()
                ));
            }
        }
        
        return sales;
    }
    
    public Sale findById(int id) throws SQLException {
        String sql = "SELECT * FROM sales WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = new Sale(
                        rs.getInt("id"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("sale_date").toLocalDateTime()
                    );
                    sale.setItems(findSaleItems(id));
                    return sale;
                }
            }
        }
        
        return null;
    }
    
    public List<SaleItem> findSaleItems(int saleId) throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        String sql = """
            SELECT si.*, p.name as product_name 
            FROM sale_items si 
            JOIN products p ON si.product_id = p.id 
            WHERE si.sale_id = ?
        """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new SaleItem(
                        rs.getInt("id"),
                        rs.getInt("sale_id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getDouble("subtotal")
                    ));
                }
            }
        }
        
        return items;
    }
    
    public int save(Sale sale) throws SQLException {
        String sql = "INSERT INTO sales (total_amount, sale_date) VALUES (?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setDouble(1, sale.getTotalAmount());
            stmt.setTimestamp(2, Timestamp.valueOf(sale.getSaleDate()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating sale failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating sale failed, no ID obtained.");
                }
            }
        }
    }
    
    public void saveSaleItems(int saleId, List<SaleItem> items) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (SaleItem item : items) {
                stmt.setInt(1, saleId);
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getUnitPrice());
                stmt.setDouble(5, item.getSubtotal());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    public int getTotalSalesCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM sales";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }
    
    public List<Sale> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(new Sale(
                        rs.getInt("id"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("sale_date").toLocalDateTime()
                    ));
                }
            }
        }
        
        return sales;
    }
}
