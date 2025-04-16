package com.example.financetracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.adapters.TransactionAdapter;
import com.example.financetracker.models.Transaction;
import com.example.financetracker.utils.TransactionManager;

import java.util.List;

public class TransactionListFragment extends Fragment {

    private static final String TAG = "TransactionListFragment";

    // Views
    private RecyclerView recyclerViewTransactions;
    private TextView tvNoTransactions;

    // Adapter
    private TransactionAdapter transactionAdapter;

    // Data
    private TransactionManager transactionManager;

    public TransactionListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: TransactionListFragment");

        // Initialize TransactionManager
        transactionManager = TransactionManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        Log.d(TAG, "onCreateView: TransactionListFragment view created");

        // Initialize views
        recyclerViewTransactions = view.findViewById(R.id.recyclerViewTransactions);
        tvNoTransactions = view.findViewById(R.id.tvNoTransactions);

        // Set up RecyclerView
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: TransactionListFragment");

        // Load transactions
        loadTransactions();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: TransactionListFragment");

        // Refresh transactions when fragment resumes
        loadTransactions();
    }

    private void loadTransactions() {
        List<Transaction> transactions = transactionManager.getAllTransactions();

        if (transactions.isEmpty()) {
            recyclerViewTransactions.setVisibility(View.GONE);
            tvNoTransactions.setVisibility(View.VISIBLE);
        } else {
            recyclerViewTransactions.setVisibility(View.VISIBLE);
            tvNoTransactions.setVisibility(View.GONE);

            // Create adapter if it doesn't exist
            if (transactionAdapter == null) {
                transactionAdapter = new TransactionAdapter(requireContext(), transactions);
                recyclerViewTransactions.setAdapter(transactionAdapter);
            } else {
                // Update data if adapter already exists
                transactionAdapter.updateData(transactions);
            }
        }
    }

    // Fragment Lifecycle Methods with Logging

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: TransactionListFragment visible");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: TransactionListFragment paused");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: TransactionListFragment stopped");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: TransactionListFragment view destroyed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: TransactionListFragment destroyed");
    }
}