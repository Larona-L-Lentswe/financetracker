package com.example.financetracker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.financetracker.database.FinanceDbHelper;
import com.example.financetracker.database.FinanceContract.CacheEntry;
import com.example.financetracker.models.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to handle SQLite caching for improved performance and offline functionality.
 */
public class DatabaseCache {
    private static final String TAG = "DatabaseCache";

    // Cache expiration time (24 hours in milliseconds)
    private static final long CACHE_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(24);

    private static DatabaseCache instance;
    private final FinanceDbHelper dbHelper;

    private DatabaseCache(Context context) {
        dbHelper = new FinanceDbHelper(context);
    }

    public static synchronized DatabaseCache getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseCache(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Caches a list of transactions with the current timestamp
     *
     * @param cacheKey Unique identifier for this cache entry
     * @param transactions List of transactions to cache
     * @return true if cache operation was successful
     */
    public boolean cacheTransactions(String cacheKey, List<Transaction> transactions) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Convert transactions to JSON
            JSONArray jsonArray = new JSONArray();
            for (Transaction transaction : transactions) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", transaction.getId());
                jsonObject.put("amount", transaction.getAmount());
                jsonObject.put("description", transaction.getDescription());
                jsonObject.put("categoryId", transaction.getCategoryId());
                jsonObject.put("date", transaction.getDate().getTime());
                jsonObject.put("isRecurring", transaction.isRecurring());
                jsonObject.put("notes", transaction.getNotes());
                jsonArray.put(jsonObject);
            }

            // Create content values
            ContentValues values = new ContentValues();
            values.put(CacheEntry.COLUMN_CACHE_KEY, cacheKey);
            values.put(CacheEntry.COLUMN_CACHE_DATA, jsonArray.toString());
            values.put(CacheEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());

            // Insert or update cache entry
            db.beginTransaction();
            try {
                // Delete existing cache with same key if exists
                db.delete(CacheEntry.TABLE_NAME,
                        CacheEntry.COLUMN_CACHE_KEY + "=?",
                        new String[]{cacheKey});

                // Insert new cache
                long id = db.insert(CacheEntry.TABLE_NAME, null, values);
                db.setTransactionSuccessful();

                Log.d(TAG, "Cache saved with ID: " + id);
                return id != -1;
            } finally {
                db.endTransaction();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error caching transactions", e);
            return false;
        }
    }

    /**
     * Retrieves cached transactions if they exist and haven't expired
     *
     * @param cacheKey Unique identifier for this cache entry
     * @return List of cached transactions or null if cache miss or expired
     */
    public List<Transaction> getCachedTransactions(String cacheKey) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                CacheEntry.COLUMN_CACHE_DATA,
                CacheEntry.COLUMN_TIMESTAMP
        };

        String selection = CacheEntry.COLUMN_CACHE_KEY + "=?";
        String[] selectionArgs = {cacheKey};

        Cursor cursor = db.query(
                CacheEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                // Check if cache is expired
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(CacheEntry.COLUMN_TIMESTAMP));
                if (System.currentTimeMillis() - timestamp > CACHE_EXPIRATION_TIME) {
                    Log.d(TAG, "Cache expired for key: " + cacheKey);
                    return null;
                }

                // Get cached data
                String cacheData = cursor.getString(cursor.getColumnIndexOrThrow(CacheEntry.COLUMN_CACHE_DATA));
                return parseTransactionsFromJson(cacheData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving cache", e);
        } finally {
            cursor.close();
        }

        return null;
    }

    /**
     * Invalidates a specific cache entry
     *
     * @param cacheKey Unique identifier for the cache entry to invalidate
     */
    public void invalidateCache(String cacheKey) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CacheEntry.TABLE_NAME,
                CacheEntry.COLUMN_CACHE_KEY + "=?",
                new String[]{cacheKey});
        Log.d(TAG, "Cache invalidated for key: " + cacheKey);
    }

    /**
     * Clears all cached data
     */
    public void clearAllCaches() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CacheEntry.TABLE_NAME, null, null);
        Log.d(TAG, "All caches cleared");
    }

    /**
     * Removes expired cache entries
     */
    public void cleanupExpiredCache() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long expiryThreshold = System.currentTimeMillis() - CACHE_EXPIRATION_TIME;

        int deleted = db.delete(CacheEntry.TABLE_NAME,
                CacheEntry.COLUMN_TIMESTAMP + "<?",
                new String[]{String.valueOf(expiryThreshold)});

        Log.d(TAG, "Cleaned up " + deleted + " expired cache entries");
    }

    /**
     * Parse transaction list from JSON string
     */
    private List<Transaction> parseTransactionsFromJson(String jsonData) {
        List<Transaction> transactions = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                long id = jsonObject.getLong("id");
                float amount = (float) jsonObject.getDouble("amount");
                String description = jsonObject.getString("description");
                long categoryId = jsonObject.getLong("categoryId");
                long dateMillis = jsonObject.getLong("date");
                boolean isRecurring = jsonObject.getBoolean("isRecurring");
                String notes = jsonObject.getString("notes");

                Transaction transaction = new Transaction(
                        id, amount, description, categoryId,
                        new Date(dateMillis), isRecurring, notes);

                transactions.add(transaction);
            }

            return transactions;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON cache data", e);
            return null;
        }
    }
}