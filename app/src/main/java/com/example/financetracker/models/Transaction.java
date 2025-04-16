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

    public Transaction(long id, float amount, String description, long categoryId, Date date, boolean isRecurring, String notes) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.categoryId = categoryId;
        this.date = date;
        this.isRecurring = isRecurring;
        this.notes = notes;
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
}