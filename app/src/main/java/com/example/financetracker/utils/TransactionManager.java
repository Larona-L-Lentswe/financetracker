package com.example.financetracker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.financetracker.database.FinanceDbHelper;
import com.example.financetracker.database.FinanceContract.TransactionEntry;
import com.example.financetracker.firebase.FirebaseAuthManager;
import com.example.financetracker.firebase.FirebaseDatabaseManager;
import com.example.financetracker.models.CategorySummary;
import com.example.financetracker.models.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TransactionManager {

    private static final String TAG = "TransactionManager";

    private static TransactionManager instance;
    private final FinanceDbHelper dbHelper;
    private final CategoryManager categoryManager;
    private final DatabaseCache databaseCache;
    private final FirebaseDatabaseManager firebaseManager;
    private final FirebaseAuthManager authManager;

    // Flag to indicate if we should use Firebase
    private boolean useFirebase;

    private TransactionManager(Context context) {
        Context context1 = context.getApplicationContext();
        dbHelper = new FinanceDbHelper(context1);
        categoryManager = CategoryManager.getInstance(context1);
        databaseCache = DatabaseCache.getInstance(context1);
        firebaseManager = FirebaseDatabaseManager.getInstance();
        authManager = FirebaseAuthManager.getInstance();

        // Initialize Firebase usage based on user login status
        useFirebase = authManager.isUserSignedIn();
    }

    public static synchronized TransactionManager getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Set whether to use Firebase for data storage
     * @param useFirebase True to use Firebase, false to use local SQLite only
     */
    public void setUseFirebase(boolean useFirebase) {
        this.useFirebase = useFirebase;
    }

    // Add a new transaction to the database
    public long addTransaction(Transaction transaction) {
        // Always add to local SQLite database first
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_AMOUNT, transaction.getAmount());
        values.put(TransactionEntry.COLUMN_DESCRIPTION, transaction.getDescription());
        values.put(TransactionEntry.COLUMN_CATEGORY_ID, transaction.getCategoryId());
        values.put(TransactionEntry.COLUMN_DATE, transaction.getDate().getTime());
        values.put(TransactionEntry.COLUMN_IS_RECURRING, transaction.isRecurring() ? 1 : 0);
        values.put(TransactionEntry.COLUMN_IS_INCOME, transaction.isIncome() ? 1 : 0); // Save income status
        values.put(TransactionEntry.COLUMN_NOTES, transaction.getNotes());

        long id = db.insert(TransactionEntry.TABLE_NAME, null, values);
        Log.d(TAG, "addTransaction: Transaction added to local DB with ID: " + id);

        // Set the local ID for the transaction
        transaction.setId(id);

        // If Firebase is enabled and user is signed in, also add to Firebase
        if (useFirebase && authManager.isUserSignedIn()) {
            try {
                String firebaseKey = firebaseManager.addTransaction(transaction).get();

                // Update the local database with the Firebase key
                if (firebaseKey != null) {
                    ContentValues updateValues = new ContentValues();
                    updateValues.put("firebase_key", firebaseKey);

                    db.update(
                            TransactionEntry.TABLE_NAME,
                            updateValues,
                            TransactionEntry._ID + "=?",
                            new String[]{String.valueOf(id)}
                    );

                    Log.d(TAG, "addTransaction: Updated local transaction with Firebase key: " + firebaseKey);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "Error adding transaction to Firebase", e);
            }
        }

        // Invalidate all transaction caches since we've modified the data
        invalidateAllTransactionCaches();

        return id;
    }

    // Get all transactions with cache support
    public List<Transaction> getAllTransactions() {
        // If Firebase is enabled and user is signed in, try to get from Firebase first
        if (useFirebase && authManager.isUserSignedIn()) {
            try {
                List<Transaction> firebaseTransactions = firebaseManager.getAllTransactions().get();
                Log.d(TAG, "getAllTransactions: Retrieved " + firebaseTransactions.size() + " transactions from Firebase");

                // Update local database with Firebase data
                syncFirebaseTransactionsToLocal(firebaseTransactions);

                return firebaseTransactions;
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "Error getting transactions from Firebase, falling back to local DB", e);
                // Fall back to local database
            }
        }

        // Try to get data from cache first
        String cacheKey = "all_transactions";
        List<Transaction> cachedTransactions = databaseCache.getCachedTransactions(cacheKey);

        if (cachedTransactions != null) {
            Log.d(TAG, "getAllTransactions: Returning " + cachedTransactions.size() + " transactions from cache");
            return cachedTransactions;
        }

        // If not in cache or expired, query from local database
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                TransactionEntry._ID,
                TransactionEntry.COLUMN_AMOUNT,
                TransactionEntry.COLUMN_DESCRIPTION,
                TransactionEntry.COLUMN_CATEGORY_ID,
                TransactionEntry.COLUMN_DATE,
                TransactionEntry.COLUMN_IS_RECURRING,
                TransactionEntry.COLUMN_IS_INCOME, // Include income status
                TransactionEntry.COLUMN_NOTES,
                "firebase_key" // Add Firebase key to projection
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

            // Get income status - default to 0 (expense) if column doesn't exist
            boolean isIncome = false;
            int isIncomeIndex = cursor.getColumnIndex(TransactionEntry.COLUMN_IS_INCOME);
            if (isIncomeIndex != -1) {
                isIncome = cursor.getInt(isIncomeIndex) == 1;
            }

            String notes = cursor.getString(cursor.getColumnIndexOrThrow(TransactionEntry.COLUMN_NOTES));

            Transaction transaction = new Transaction(id, amount, description, categoryId,
                    new Date(dateMillis), isRecurring, notes, isIncome);

            // Set Firebase key if available
            int firebaseKeyIndex = cursor.getColumnIndex("firebase_key");
            if (firebaseKeyIndex != -1 && !cursor.isNull(firebaseKeyIndex)) {
                transaction.setFirebaseKey(cursor.getString(firebaseKeyIndex));
            }

            transactions.add(transaction);
        }

        cursor.close();

        // Cache the results for future use
        databaseCache.cacheTransactions(cacheKey, transactions);

        Log.d(TAG, "getAllTransactions: Returning " + transactions.size() + " transactions from local database");
        return transactions;
    }

    // Calculate total income
    public float calculateTotalIncome() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(" + TransactionEntry.COLUMN_AMOUNT + ") FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_IS_INCOME + "=1";

        Cursor cursor = db.rawQuery(query, null);

        float totalIncome = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalIncome = cursor.getFloat(0);
        }

        cursor.close();
        return totalIncome;
    }

    // Calculate total expenses
    public float calculateTotalExpenses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(" + TransactionEntry.COLUMN_AMOUNT + ") FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_IS_INCOME + "=0";

        Cursor cursor = db.rawQuery(query, null);

        float totalExpenses = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalExpenses = cursor.getFloat(0);
        }

        cursor.close();
        return totalExpenses;
    }

    // Calculate total expenses between dates
    public float calculateTotalExpensesBetweenDates(Date startDate, Date endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(" + TransactionEntry.COLUMN_AMOUNT + ") FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_IS_INCOME + "=0" +
                " AND " + TransactionEntry.COLUMN_DATE + " >= " + startDate.getTime() +
                " AND " + TransactionEntry.COLUMN_DATE + " <= " + endDate.getTime();

        Cursor cursor = db.rawQuery(query, null);

        float totalExpenses = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalExpenses = cursor.getFloat(0);
        }

        cursor.close();
        return totalExpenses;
    }

    // Calculate total spent amount between dates (for backward compatibility)
    public float calculateTotalSpentBetweenDates(Date startDate, Date endDate) {
        return calculateTotalExpensesBetweenDates(startDate, endDate);
    }

    // Get the top spending category between dates
    public String getTopCategoryBetweenDates(Date startDate, Date endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + TransactionEntry.COLUMN_CATEGORY_ID + ", SUM(" + TransactionEntry.COLUMN_AMOUNT + ") as total" +
                " FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_IS_INCOME + "=0" + // Only for expenses
                " AND " + TransactionEntry.COLUMN_DATE + " >= " + startDate.getTime() +
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
        // Only include expenses for category summary
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get total spent during the period
        float totalSpent = calculateTotalExpensesBetweenDates(startDate, endDate);

        // If no spending, return empty list
        if (totalSpent == 0) {
            return new ArrayList<>();
        }

        // Query to get spending by category
        String query = "SELECT " + TransactionEntry.COLUMN_CATEGORY_ID + ", SUM(" + TransactionEntry.COLUMN_AMOUNT + ") as total" +
                " FROM " + TransactionEntry.TABLE_NAME +
                " WHERE " + TransactionEntry.COLUMN_IS_INCOME + "=0" + // Only for expenses
                " AND " + TransactionEntry.COLUMN_DATE + " >= " + startDate.getTime() +
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

    /**
     * Synchronize Firebase transactions to local database
     * This ensures the local database matches the Firebase database
     */
    private void syncFirebaseTransactionsToLocal(List<Transaction> firebaseTransactions) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // First, get all local transactions with Firebase keys
            Map<String, Long> localFirebaseKeyToId = new HashMap<>();
            Cursor cursor = db.query(
                    TransactionEntry.TABLE_NAME,
                    new String[]{TransactionEntry._ID, "firebase_key"},
                    "firebase_key IS NOT NULL",
                    null, null, null, null
            );

            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String firebaseKey = cursor.getString(1);
                localFirebaseKeyToId.put(firebaseKey, id);
            }
            cursor.close();

            // Now process each Firebase transaction
            for (Transaction transaction : firebaseTransactions) {
                String firebaseKey = transaction.getFirebaseKey();
                if (firebaseKey == null) continue;

                ContentValues values = new ContentValues();
                values.put(TransactionEntry.COLUMN_AMOUNT, transaction.getAmount());
                values.put(TransactionEntry.COLUMN_DESCRIPTION, transaction.getDescription());
                values.put(TransactionEntry.COLUMN_CATEGORY_ID, transaction.getCategoryId());
                values.put(TransactionEntry.COLUMN_DATE, transaction.getDate().getTime());
                values.put(TransactionEntry.COLUMN_IS_RECURRING, transaction.isRecurring() ? 1 : 0);
                values.put(TransactionEntry.COLUMN_IS_INCOME, transaction.isIncome() ? 1 : 0);
                values.put(TransactionEntry.COLUMN_NOTES, transaction.getNotes());
                values.put("firebase_key", firebaseKey);

                // Check if this transaction exists locally
                if (localFirebaseKeyToId.containsKey(firebaseKey)) {
                    // Update existing transaction
                    long localId = localFirebaseKeyToId.get(firebaseKey);
                    transaction.setId(localId); // Set local ID for reference

                    db.update(
                            TransactionEntry.TABLE_NAME,
                            values,
                            TransactionEntry._ID + "=?",
                            new String[]{String.valueOf(localId)}
                    );

                    // Remove from map to track which ones we've processed
                    localFirebaseKeyToId.remove(firebaseKey);
                } else {
                    // Insert new transaction
                    long id = db.insert(TransactionEntry.TABLE_NAME, null, values);
                    transaction.setId(id); // Set local ID for reference
                }
            }

            // Any remaining entries in localFirebaseKeyToId are transactions that exist locally
            // but not in Firebase - they were probably deleted in Firebase, so delete them locally too
            for (Long localId : localFirebaseKeyToId.values()) {
                db.delete(
                        TransactionEntry.TABLE_NAME,
                        TransactionEntry._ID + "=?",
                        new String[]{String.valueOf(localId)}
                );
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // Invalidate caches after sync
        invalidateAllTransactionCaches();
    }
}