package com.example.financetracker.models;

import java.util.Date;

public class Transaction {
    private long id;
    private float amount;
    private String description;
    private long categoryId;
    private Date date;
    private boolean isRecurring;
    private String notes;
    private String firebaseKey; // For Firebase integration
    private boolean isIncome; // Added to track if this is income vs expense

    public Transaction(long id, float amount, String description, long categoryId, Date date, boolean isRecurring, String notes) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
        this.date = date;
        this.isRecurring = isRecurring;
        this.notes = notes;
        this.firebaseKey = null; // Default to null for new local transactions
        this.isIncome = false; // Default to expense (not income)
    }

    // Constructor that includes income flag
    public Transaction(long id, float amount, String description, long categoryId, Date date, boolean isRecurring, String notes, boolean isIncome) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
        this.date = date;
        this.isRecurring = isRecurring;
        this.notes = notes;
        this.firebaseKey = null; // Default to null for new local transactions
        this.isIncome = isIncome;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    // Get actual amount value (positive for income, negative for expense)
    public float getSignedAmount() {
        return isIncome ? amount : -amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public boolean isSyncedWithFirebase() {
        return firebaseKey != null && !firebaseKey.isEmpty();
    }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }
}