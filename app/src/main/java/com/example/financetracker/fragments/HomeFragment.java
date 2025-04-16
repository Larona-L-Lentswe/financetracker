package com.example.financetracker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.financetracker.R;
import com.example.financetracker.utils.TransactionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends BaseCachedFragment {

    private static final String TAG = "HomeFragment";
    private static final String CACHE_KEY = "home_fragment";
    private static final String PREFS_NAME = "FinanceTrackerPrefs";

    // State keys for cache
    private static final String STATE_BUDGET = "budget";
    private static final String STATE_INCOME = "income";
    private static final String STATE_EXPENSES = "expenses";
    private static final String STATE_REMAINING = "remaining";
    private static final String STATE_BALANCE = "balance";

    // UI elements
    private TextView tvIncome, tvExpenses, tvBudget, tvRemaining, tvNetBalance;
    private TransactionManager transactionManager;

    // Cached values
    private float cachedBudget = 0.0f;
    private float cachedIncome = 0.0f;
    private float cachedExpenses = 0.0f;
    private float cachedRemaining = 0.0f;
    private float cachedNetBalance = 0.0f;
    private boolean dataLoaded = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: HomeFragment");

        // Initialize transaction manager
        transactionManager = TransactionManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpenses = view.findViewById(R.id.tvExpenses);
        tvBudget = view.findViewById(R.id.tvBudget);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvNetBalance = view.findViewById(R.id.tvNetBalance);

        // Handle FAB click
        FloatingActionButton fab = view.findViewById(R.id.fabAddTransaction);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                // Switch to AddTransactionFragment
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddTransactionFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If we have cached data, display immediately
        if (dataLoaded) {
            updateUI();
        }

        // Load the transaction list fragment
        loadTransactionListFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: HomeFragment resumed");

        // Always update data when fragment resumes
        // This ensures we have fresh data after adding transactions
        updateSummary();
    }

    private void loadTransactionListFragment() {
        TransactionListFragment transactionListFragment = new TransactionListFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.transactionListContainer, transactionListFragment);
        transaction.commit();
    }

    private void updateSummary() {
        // Get budget from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cachedBudget = prefs.getFloat("monthly_budget", 0.0f);

        // Get income and expenses
        cachedIncome = transactionManager.calculateTotalIncome();
        cachedExpenses = transactionManager.calculateTotalExpenses();

        // Calculate net balance (income - expenses)
        cachedNetBalance = cachedIncome - cachedExpenses;

        // Calculate remaining budget (budget - expenses)
        cachedRemaining = cachedBudget - cachedExpenses;

        // Mark data as loaded
        dataLoaded = true;

        // Update UI
        updateUI();
    }

    private void updateUI() {
        // Format currency values
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

        // Update TextView values
        tvIncome.setText(currencyFormatter.format(cachedIncome));
        tvExpenses.setText(currencyFormatter.format(cachedExpenses));
        tvBudget.setText(currencyFormatter.format(cachedBudget));
        tvRemaining.setText(currencyFormatter.format(cachedRemaining));
        tvNetBalance.setText(currencyFormatter.format(cachedNetBalance));

        // Change color of remaining amount if over budget
        if (cachedRemaining < 0) {
            tvRemaining.setTextColor(requireContext().getColor(R.color.colorError));
        } else {
            tvRemaining.setTextColor(requireContext().getColor(R.color.colorSuccess));
        }

        // Change color of net balance based on positive/negative
        if (cachedNetBalance < 0) {
            tvNetBalance.setTextColor(requireContext().getColor(R.color.colorError));
        } else {
            tvNetBalance.setTextColor(requireContext().getColor(R.color.colorSuccess));
        }
    }

    @Override
    protected void saveState(Bundle outState) {
        // Save our cached values to the bundle
        outState.putFloat(STATE_BUDGET, cachedBudget);
        outState.putFloat(STATE_INCOME, cachedIncome);
        outState.putFloat(STATE_EXPENSES, cachedExpenses);
        outState.putFloat(STATE_REMAINING, cachedRemaining);
        outState.putFloat(STATE_BALANCE, cachedNetBalance);
        outState.putBoolean("data_loaded", dataLoaded);
    }

    @Override
    protected void restoreState(Bundle state) {
        // Restore our cached values from the bundle
        cachedBudget = state.getFloat(STATE_BUDGET, 0.0f);
        cachedIncome = state.getFloat(STATE_INCOME, 0.0f);
        cachedExpenses = state.getFloat(STATE_EXPENSES, 0.0f);
        cachedRemaining = state.getFloat(STATE_REMAINING, 0.0f);
        cachedNetBalance = state.getFloat(STATE_BALANCE, 0.0f);
        dataLoaded = state.getBoolean("data_loaded", false);
    }
}