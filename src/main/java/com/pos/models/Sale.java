package com.pos.models;

import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    private int id;
    private double totalAmount;
    private LocalDateTime saleDate;
    private List<SaleItem> items;
    
    public Sale() {}
    
    public Sale(double totalAmount) {
        this.totalAmount = totalAmount;
        this.saleDate = LocalDateTime.now();
    }
    
    public Sale(int id, double totalAmount, LocalDateTime saleDate) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getSaleDate() {
        return saleDate;
    }
    
    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }
    
    public List<SaleItem> getItems() {
        return items;
    }
    
    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "Sale #" + id + " - $" + String.format("%.2f", totalAmount) + " - " + saleDate;
    }
}
