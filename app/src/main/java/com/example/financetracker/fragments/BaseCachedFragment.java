package com.example.financetracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.financetracker.utils.FragmentStateCache;

/**
 * Base Fragment class that handles view state caching
 * All fragments in the app should extend this class to benefit from state caching
 */
public abstract class BaseCachedFragment extends Fragment {

    // Unique identifier for this fragment's state in the cache
    private String cacheKey;

    /**
     * Get the cache key for this fragment
     * Override this method to provide a unique key for each fragment type
     *
     * @return A string key for caching the fragment's state
     */
    protected abstract String getCacheKey();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the cache key
        cacheKey = getCacheKey();

        // Try to restore state from cache if we don't have saved instance state
        if (savedInstanceState == null) {
            Bundle cachedState = FragmentStateCache.getInstance().getState(cacheKey);
            if (cachedState != null) {
                // Restore state from cache
                restoreState(cachedState);
                Log.d(getClass().getSimpleName(), "Restored state from cache");
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If we had saved instance state, restore from that instead of cache
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
            Log.d(getClass().getSimpleName(), "Restored state from saved instance");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the fragment's state into the bundle
        saveState(outState);

        // Also save to our cache
        FragmentStateCache.getInstance().saveState(cacheKey, outState);
        Log.d(getClass().getSimpleName(), "Saved state to cache");
    }

    @Override
    public void onDestroyView() {
        // Before destroying the view, save current state to cache
        Bundle state = new Bundle();
        saveState(state);
        FragmentStateCache.getInstance().saveState(cacheKey, state);
        Log.d(getClass().getSimpleName(), "Saved state to cache on view destroy");

        super.onDestroyView();
    }

    /**
     * Save the fragment's state to the provided bundle
     * Override this method in subclasses to save specific state
     *
     * @param outState Bundle to save state into
     */
    protected void saveState(Bundle outState) {
        // Base implementation does nothing
        // Subclasses should override to save specific state
    }

    /**
     * Restore the fragment's state from the provided bundle
     * Override this method in subclasses to restore specific state
     *
     * @param state Bundle containing saved state
     */
    protected void restoreState(Bundle state) {
        // Base implementation does nothing
        // Subclasses should override to restore specific state
    }
}