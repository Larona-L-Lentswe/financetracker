package com.example.financetracker.models;

public class Category {
    private long id;
    private String name;
    private int iconResourceId;

    public Category(long id, String name, int iconResourceId) {
        this.id = id;
        this.name = name;
        this.iconResourceId = iconResourceId;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    @Override
    public String toString() {
        return name; // For displaying in Spinner
    }
}