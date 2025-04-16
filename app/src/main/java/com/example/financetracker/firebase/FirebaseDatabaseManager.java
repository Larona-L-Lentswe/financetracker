package com.example.financetracker.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.financetracker.models.Transaction;
import com.example.financetracker.utils.DatabaseCache;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manager for Firebase Realtime Database operations
 */
public class FirebaseDatabaseManager {
    private static final String TAG = "FirebaseDatabaseManager";

    // Singleton instance
    private static FirebaseDatabaseManager instance;

    // Firebase references
    private final FirebaseDatabase database;
    private final DatabaseReference transactionsRef;
    private final FirebaseAuth auth;

    // Local cache
    private final DatabaseCache localCache;

    private FirebaseDatabaseManager() {
        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize local cache
        localCache = DatabaseCache.getInstance(null);

        // Get user-specific database reference
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            transactionsRef = database.getReference("users")
                    .child(currentUser.getUid())
                    .child("transactions");
        } else {
            // If no user is signed in, use a temporary reference
            // This will be updated when a user signs in
            transactionsRef = database.getReference("transactions_temp");
        }

        // Set up offline persistence
        database.setPersistenceEnabled(true);
        transactionsRef.keepSynced(true);
    }

    public static synchronized FirebaseDatabaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseManager();
        }
        return instance;
    }

    /**
     * Update the database reference after user authentication
     */
    public void updateUserReference() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference newRef = database.getReference("users")
                    .child(currentUser.getUid())
                    .child("transactions");

            // Keep synced for offline access
            newRef.keepSynced(true);

            Log.d(TAG, "Database reference updated for user: " + currentUser.getUid());
        }
    }

    /**
     * Add a transaction to Firebase
     *
     * @param transaction The transaction to add
     * @return A future that completes with the transaction ID when the operation is successful
     */
    public CompletableFuture<String> addTransaction(Transaction transaction) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Check if user is authenticated
        if (auth.getCurrentUser() == null) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        // Generate a new unique key
        String key = transactionsRef.push().getKey();
        if (key == null) {
            future.completeExceptionally(new Exception("Failed to generate transaction key"));
            return future;
        }

        // Convert transaction to Firebase map
        Map<String, Object> transactionValues = transactionToMap(transaction);

        // Add the transaction
        transactionsRef.child(key).setValue(transactionValues)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Transaction added with ID: " + key);
                            future.complete(key);

                            // Invalidate the local cache
                            localCache.invalidateCache("all_transactions");
                        } else {
                            Log.e(TAG, "Failed to add transaction", task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                });

        return future;
    }

    /**
     * Update an existing transaction
     *
     * @param transactionId The Firebase ID of the transaction
     * @param transaction The updated transaction object
     * @return A future that completes when the operation is successful
     */
    public CompletableFuture<Void> updateTransaction(String transactionId, Transaction transaction) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Check if user is authenticated
        if (auth.getCurrentUser() == null) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        // Convert transaction to Firebase map
        Map<String, Object> transactionValues = transactionToMap(transaction);

        // Update the transaction
        transactionsRef.child(transactionId).updateChildren(transactionValues)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Transaction updated: " + transactionId);
                            future.complete(null);

                            // Invalidate the local cache
                            localCache.invalidateCache("all_transactions");
                        } else {
                            Log.e(TAG, "Failed to update transaction", task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                });

        return future;
    }

    /**
     * Delete a transaction
     *
     * @param transactionId The Firebase ID of the transaction to delete
     * @return A future that completes when the operation is successful
     */
    public CompletableFuture<Void> deleteTransaction(String transactionId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Check if user is authenticated
        if (auth.getCurrentUser() == null) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        // Delete the transaction
        transactionsRef.child(transactionId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Transaction deleted: " + transactionId);
                            future.complete(null);

                            // Invalidate the local cache
                            localCache.invalidateCache("all_transactions");
                        } else {
                            Log.e(TAG, "Failed to delete transaction", task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                });

        return future;
    }

    /**
     * Get all transactions
     *
     * @return A future that completes with the list of transactions
     */
    public CompletableFuture<List<Transaction>> getAllTransactions() {
        CompletableFuture<List<Transaction>> future = new CompletableFuture<>();

        // Check if user is authenticated
        if (auth.getCurrentUser() == null) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        // Query all transactions
        transactionsRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Transaction> transactions = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Transaction transaction = mapToTransaction(snapshot.getKey(), snapshot);
                        transactions.add(transaction);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing transaction", e);
                    }
                }

                Log.d(TAG, "Retrieved " + transactions.size() + " transactions");
                future.complete(transactions);

                // Cache the transactions locally
                localCache.cacheTransactions("all_transactions", transactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to get transactions", databaseError.toException());
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    /**
     * Get transactions within a date range
     *
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @return A future that completes with the filtered list of transactions
     */
    public CompletableFuture<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        CompletableFuture<List<Transaction>> future = new CompletableFuture<>();

        // Check if user is authenticated
        if (auth.getCurrentUser() == null) {
            future.completeExceptionally(new Exception("User not authenticated"));
            return future;
        }

        // Create cache key for this date range
        String cacheKey = "transactions_" + startDate.getTime() + "_" + endDate.getTime();

        // Try to get from cache first
        List<Transaction> cachedTransactions = localCache.getCachedTransactions(cacheKey);
        if (cachedTransactions != null) {
            Log.d(TAG, "Returning " + cachedTransactions.size() + " transactions from cache");
            future.complete(cachedTransactions);
            return future;
        }

        // Query transactions in the date range
        Query query = transactionsRef
                .orderByChild("date")
                .startAt(startDate.getTime())
                .endAt(endDate.getTime());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Transaction> transactions = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Transaction transaction = mapToTransaction(snapshot.getKey(), snapshot);
                        transactions.add(transaction);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing transaction", e);
                    }
                }

                Log.d(TAG, "Retrieved " + transactions.size() + " transactions in date range");
                future.complete(transactions);

                // Cache the transactions locally
                localCache.cacheTransactions(cacheKey, transactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to get transactions", databaseError.toException());
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    /**
     * Convert a Transaction object to a Map for Firebase storage
     */
    private Map<String, Object> transactionToMap(Transaction transaction) {
        Map<String, Object> map = new HashMap<>();
        map.put("amount", transaction.getAmount());
        map.put("description", transaction.getDescription());
        map.put("categoryId", transaction.getCategoryId());
        map.put("date", transaction.getDate().getTime());
        map.put("isRecurring", transaction.isRecurring());
        map.put("notes", transaction.getNotes());
        return map;
    }

    /**
     * Convert Firebase data to a Transaction object
     */
    private Transaction mapToTransaction(String key, DataSnapshot snapshot) {
        long id = 0; // Use local ID for Room DB if needed
        float amount = snapshot.child("amount").getValue(Double.class).floatValue();
        String description = snapshot.child("description").getValue(String.class);
        long categoryId = snapshot.child("categoryId").getValue(Long.class);
        long dateMillis = snapshot.child("date").getValue(Long.class);
        boolean isRecurring = snapshot.child("isRecurring").getValue(Boolean.class);
        String notes = snapshot.child("notes").getValue(String.class);

        // Create a new Transaction object
        Transaction transaction = new Transaction(id, amount, description, categoryId,
                new Date(dateMillis), isRecurring, notes);

        // Add the Firebase key as metadata
        transaction.setFirebaseKey(key);

        return transaction;
    }
}