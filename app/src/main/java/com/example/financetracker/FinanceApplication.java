package com.example.financetracker;

import android.app.Application;
import android.util.Log;

import com.example.financetracker.utils.DatabaseCache;

/**
 * Application class for initializing global components and performing app-wide operations.
 */
public class FinanceApplication extends Application {

    private static final String TAG = "FinanceApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application initialized");

        // Initialize the cache system
        initializeCache();
    }

    /**
     * Initialize and perform maintenance on the database cache
     */
    private void initializeCache() {
        // Get the database cache instance and clean up expired entries
        DatabaseCache cache = DatabaseCache.getInstance(this);
        cache.cleanupExpiredCache();
        Log.d(TAG, "Cache system initialized and cleaned up");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "Low memory condition detected");

        // Clear all caches to free up memory
        DatabaseCache cache = DatabaseCache.getInstance(this);
        cache.clearAllCaches();
    }
}