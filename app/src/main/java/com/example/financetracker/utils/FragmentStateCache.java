package com.example.financetracker.utils;

import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;

/**
 * Cache for preserving fragment view states during navigation
 * Improves performance by avoiding unnecessary recreation of views
 */
public class FragmentStateCache {
    private static final String TAG = "FragmentStateCache";
    private static final int CACHE_SIZE = 10; // Number of fragment states to cache

    private static FragmentStateCache instance;
    private final LruCache<String, Bundle> viewStateCache;

    private FragmentStateCache() {
        viewStateCache = new LruCache<>(CACHE_SIZE);
    }

    public static synchronized FragmentStateCache getInstance() {
        if (instance == null) {
            instance = new FragmentStateCache();
        }
        return instance;
    }

    /**
     * Save a fragment's view state
     *
     * @param fragmentKey Unique identifier for the fragment
     * @param state Bundle containing the fragment's view state
     */
    public void saveState(String fragmentKey, Bundle state) {
        if (state != null) {
            viewStateCache.put(fragmentKey, state);
            Log.d(TAG, "Saved state for fragment: " + fragmentKey);
        }
    }

    /**
     * Retrieve a saved fragment view state
     *
     * @param fragmentKey Unique identifier for the fragment
     * @return The saved state Bundle or null if not found
     */
    public Bundle getState(String fragmentKey) {
        Bundle state = viewStateCache.get(fragmentKey);
        if (state != null) {
            Log.d(TAG, "Retrieved state for fragment: " + fragmentKey);
        }
        return state;
    }

    /**
     * Remove a fragment's state from the cache
     *
     * @param fragmentKey Unique identifier for the fragment
     */
    public void removeState(String fragmentKey) {
        viewStateCache.remove(fragmentKey);
        Log.d(TAG, "Removed state for fragment: " + fragmentKey);
    }

    /**
     * Clear all cached fragment states
     */
    public void clearAllStates() {
        viewStateCache.evictAll();
        Log.d(TAG, "Cleared all fragment states");
    }
}