package com.example.financetracker.utils;

import android.content.Context;

import com.example.financetracker.R;
import com.example.financetracker.models.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManager {

    private static CategoryManager instance;
    private final Map<Long, Category> categoriesMap;

    private CategoryManager(Context context) {
        categoriesMap = new HashMap<>();
        initializeCategories();
    }

    public static synchronized CategoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryManager(context.getApplicationContext());
        }
        return instance;
    }

    private void initializeCategories() {
        // In a real app, these would be loaded from a database
        addCategory(new Category(1, "Food & Dining", R.drawable.ic_category_food));
        addCategory(new Category(2, "Transportation", R.drawable.ic_category_transport));
        addCategory(new Category(3, "Shopping", R.drawable.ic_category_shopping));
        addCategory(new Category(4, "Entertainment", R.drawable.ic_category_entertainment));
        addCategory(new Category(5, "Bills & Utilities", R.drawable.ic_category_bills));
        addCategory(new Category(6, "Healthcare", R.drawable.ic_category_healthcare));
        addCategory(new Category(7, "Other", R.drawable.ic_category_other));
    }

    private void addCategory(Category category) {
        categoriesMap.put(category.getId(), category);
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categoriesMap.values());
    }

    public Category getCategory(long categoryId) {
        return categoriesMap.get(categoryId);
    }

    public String getCategoryName(long categoryId) {
        Category category = categoriesMap.get(categoryId);
        return category != null ? category.getName() : "Unknown Category";
    }

    public int getCategoryIcon(long categoryId) {
        Category category = categoriesMap.get(categoryId);
        return category != null ? category.getIconResourceId() : R.drawable.ic_category_other;
    }
}