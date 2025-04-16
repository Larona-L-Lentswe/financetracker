package com.example.financetracker.models;

public class CategorySummary {
    private String categoryName;
    private float totalAmount;
    private float percentage;
    private int iconResourceId;

    public CategorySummary(String categoryName, float totalAmount, float percentage, int iconResourceId) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.iconResourceId = iconResourceId;
    }

    // Getters and setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }
}