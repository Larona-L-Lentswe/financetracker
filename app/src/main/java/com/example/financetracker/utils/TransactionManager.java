package com.example.financetracker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.financetracker.database.FinanceDbHelper;
import com.example.financetracker.database.FinanceContract.TransactionEntry;
import com.example.financetracker.models.CategorySummary;
import com.example.financetracker.models.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionManager {

    private static final String TAG = "TransactionManager";

    private static TransactionManager instance;
    private final FinanceDbHelper dbHelper;
    private final CategoryManager categoryManager;
    private final DatabaseCache databaseCache;

    private TransactionManager(Context context) {
        dbHelper = new FinanceDbHelper(context);
        categoryManager = CategoryManager.getInstance(context);
        databaseCache = DatabaseCache.getInstance(context);
    }

    public static synchronized TransactionManager getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionManager(context.getApplicationContext());
        }
        return instance;
    }

    // Add a new transaction to the database
    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_AMOUNT, transaction.getAmount());
        values.put(TransactionEntry.COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(TransactionEntry.COLUMN_CATEGORY_ID, transaction.getCategoryId());
        values.put(TransactionEntry.COLUMN_DATE, transaction.getDate().getTime());
        values.put(TransactionEntry.COLUMN_IS_RECURRING, transaction.isRecurring() ? 1 : 0);
        values.put(TransactionEntry.COLUMN_NOTES, transaction.getNotes());

        long id = db.insert(TransactionEntry.TABLE_NAME, null, values);
        Log.d(TAG, "addTransaction: Transaction added with ID: " + id);

        // Invalidate all transaction caches since we've modified the data
        invalidateAllTransactionCaches();

        return id;
    }

    // Get all transactions with cache support
    public List<Transaction> getAllTransactions() {
        // Try to get data from cache first
        String cacheKey = "all_transactions";
        List<Transaction> cachedTransactions = databaseCache.getCachedTransactions(cacheKey);

        if (cachedTransactions != null) {
            Log.d(TAG, "getAllTransactions: Returning " + cachedTransactions.size() + " transactions from cache");
            return cachedTransactions;
        }

        // If not in cache or expired, query from database
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                TransactionEntry._ID,
                TransactionEntry.COLUMN_AMOUNT,
                TransactionEntry.COLUMN_DESCRIPTION,
                TransactionEntry.COLUMN_CATEGORY_ID,
                TransactionEntry.COLUMN_DATE,
                TransactionEntry.COLUMN_IS_RECURRING,
                TransactionEntry.COLUMN_NOTES
        };

        String sortOrder = TransactionEntry.COLUMN_DATE + " DESC";

        Cursor cursor = db.query(
                TransactionEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(TransactionEntry._ID));
            float amount = cursor.getFloat(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_AMOUNT));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_DESCRIPTION));
            long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_CATEGORY_ID));
            long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_DATE));
            boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_IS_RECURRING)) == 1;
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_NOTES));

            Transaction transaction = new Transaction(id, amount, description, categoryId, new Date(dateMillis), isRecurring, notes);
            transactions.add(transaction);
        }

        cursor.close();

        // Cache the results for future use
        databaseCache.cacheTransactions(cacheKey, transactions);

        Log.d(TAG, "getAllTransactions: Returning " + transactions.size() + " transactions from database");
        return transactions;
    }

    // Get transactions between two dates with cache support
    public List<Transaction> getTransactionsBetweenDates(Date startDate, Date endDate) {
        // Create a unique cache key for this date range
        String cacheKey = "transactions_" + startDate.getTime() + "_" + endDate.getTime();
        List<Transaction> cachedTransactions = databaseCache.getCachedTransactions(cacheKey);

        if (cachedTransactions != null) {
            Log.d(TAG, "getTransactionsBetweenDates: Returning " + cachedTransactions.size() + " transactions from cache");
            return cachedTransactions;
        }

        // If not in cache or expired, query from database
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                TransactionEntry._ID,
                TransactionEntry.COLUMN_AMOUNT,
                TransactionEntry.COLUMN_DESCRIPTION,
                TransactionEntry.COLUMN_CATEGORY_ID,
                TransactionEntry.COLUMN_DATE,
                TransactionEntry.COLUMN_IS_RECURRING,
                TransactionEntry.COLUMN_NOTES
        };

        String selection = TransactionEntry.COLUMN_DATE + " >= ? AND " + TransactionEntry.COLUMN_DATE + " <= ?";
        String[] selectionArgs = {
                String.valueOf(startDate.getTime()),
                String.valueOf(endDate.getTime())
        };

        String sortOrder = TransactionEntry.COLUMN_DATE + " DESC";

        Cursor cursor = db.query(
                TransactionEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(TransactionEntry._ID));
            float amount = cursor.getFloat(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_AMOUNT));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_DESCRIPTION));
            long categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_CATEGORY_ID));
            long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_DATE));
            boolean isRecurring = cursor.getInt(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_IS_RECURRING)) == 1;
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_NOTES));

            Transaction transaction = new Transaction(id, amount, description, categoryId, new Date(dateMillis), isRecurring, notes);
            transactions.add(transaction);
        }

        cursor.close();

        // Cache the results for future use
        databaseCache.cacheTransactions(cacheKey, transactions);

        return transactions;
    }

    // Calculate total spent amount with caching
    public float calculateTotalSpent() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(" + TransactionEntry.COLUMN_AMOUNT + ") FROM " + TransactionEntry.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        float totalSpent = 0;
        if (cursor.moveToFirst()) {
            totalSpent = cursor.getFloat(0);
        }

        cursor.close();
        return totalSpent;
    }

    // Calculate total spent amount between dates
    public float calculateTotalSpentBetweenDates(Date startDate, Date endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(" + TransactionEntry.COLUMN_AMOUNT + ") FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_DATE + " >= " + startDate.getTime() +
                " AND " + TransactionEntry.COLUMN_DATE + " <= " + endDate.getTime();

        Cursor cursor = db.rawQuery(query, null);

        float totalSpent = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalSpent = cursor.getFloat(0);
        }

        cursor.close();
        return totalSpent;
    }

    // Get the top spending category between dates
    public String getTopCategoryBetweenDates(Date startDate, Date endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + TransactionEntry.COLUMN_CATEGORY_ID + ", SUM(" + TransactionEntry.COLUMN_AMOUNT + ") as total" +
                " FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_DATE + " >= " + startDate.getTime() +
                " AND " + TransactionEntry.COLUMN_DATE + " <= " + endDate.getTime() +
                " GROUP BY " + TransactionEntry.COLUMN_CATEGORY_ID +
                " ORDER BY total DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        String topCategory = null;
        if (cursor.moveToFirst()) {
            long categoryId = cursor.getLong(0);
            topCategory = categoryManager.getCategoryName(categoryId);
        }

        cursor.close();
        return topCategory;
    }

    // Get category summaries for spending chart with cache support
    public List<CategorySummary> getCategorySummariesBetweenDates(Date startDate, Date endDate) {
        // Create a unique cache key for this request
        String cacheKey = "category_summary_" + startDate.getTime() + "_" + endDate.getTime();

        // For this method, we'll implement a simpler cache approach
        // We won't use the DatabaseCache directly since the CategorySummary objects
        // would need special handling in the JSON conversion

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get total spent during the period
        float totalSpent = calculateTotalSpentBetweenDates(startDate, endDate);

        // If no spending, return empty list
        if (totalSpent == 0) {
            return new ArrayList<>();
        }

        // Query to get spending by category
        String query = "SELECT " + TransactionEntry.COLUMN_CATEGORY_ID + ", SUM(" + TransactionEntry.COLUMN_AMOUNT + ") as total" +
                " FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_DATE + " >= " + startDate.getTime() +
                " AND " + TransactionEntry.COLUMN_DATE + " <= " + endDate.getTime() +
                " GROUP BY " + TransactionEntry.COLUMN_CATEGORY_ID +
                " ORDER BY total DESC";

        Cursor cursor = db.rawQuery(query, null);

        List<CategorySummary> categorySummaries = new ArrayList<>();

        while (cursor.moveToNext()) {
            long categoryId = cursor.getLong(0);
            float amount = cursor.getFloat(1);
            float percentage = (amount / totalSpent) * 100;

            String categoryName = categoryManager.getCategoryName(categoryId);
            int iconResourceId = categoryManager.getCategoryIcon(categoryId);

            CategorySummary summarySummary = new CategorySummary(categoryName, amount, percentage, iconResourceId);
            categorySummaries.add(summarySummary);
        }

        cursor.close();
        return categorySummaries;
    }

    /**
     * Invalidate all transaction-related caches
     */
    private void invalidateAllTransactionCaches() {
        // Remove the "all transactions" cache
        databaseCache.invalidateCache("all_transactions");

        // We could also selectively invalidate other date-specific caches,
        // but for simplicity we'll clean up expired caches
        databaseCache.cleanupExpiredCache();
    }
}