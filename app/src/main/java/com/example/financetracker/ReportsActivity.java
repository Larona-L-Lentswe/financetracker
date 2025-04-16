package com.example.financetracker;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.adapters.CategorySummaryAdapter;
import com.example.financetracker.fragments.SpendingChartFragment;
import com.example.financetracker.models.CategorySummary;
import com.example.financetracker.utils.TransactionManager;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private static final String TAG = "ReportsActivity";

    // View elements
    private Button btnThisWeek, btnThisMonth, btnThisYear;
    private TextView tvTotalSpent, tvTopCategory, tvAverageSpend;
    private RecyclerView recyclerViewCategories;

    // Date range for reports
    private Date startDate;
    private Date endDate;
    private String currentPeriod = "month"; // Default period

    // Transaction manager
    private TransactionManager transactionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        Log.d(TAG, "onCreate: ReportsActivity started");

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Set up date range buttons
        setupDateRangeButtons();

        // Initialize TransactionManager
        transactionManager = TransactionManager.getInstance(this);

        // Set default date range to current month
        setDateRangeToCurrentMonth();

        // Load spending chart fragment
        if (savedInstanceState == null) {
            loadSpendingChartFragment();
        }
    }

    private void initViews() {
        btnThisWeek = findViewById(R.id.btnThisWeek);
        btnThisMonth = findViewById(R.id.btnThisMonth);
        btnThisYear = findViewById(R.id.btnThisYear);
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvTopCategory = findViewById(R.id.tvTopCategory);
        tvAverageSpend = findViewById(R.id.tvAverageSpend);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDateRangeButtons() {
        btnThisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateRangeToCurrentWeek();
                updateUI();
            }
        });

        btnThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateRangeToCurrentMonth();
                updateUI();
            }
        });

        btnThisYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateRangeToCurrentYear();
                updateUI();
            }
        });
    }

    private void setDateRangeToCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        endDate = calendar.getTime();

        currentPeriod = "week";
        updateButtonSelection();
    }

    private void setDateRangeToCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        endDate = calendar.getTime();

        currentPeriod = "month";
        updateButtonSelection();
    }

    private void setDateRangeToCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        startDate = calendar.getTime();

        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        endDate = calendar.getTime();

        currentPeriod = "year";
        updateButtonSelection();
    }

    private void updateButtonSelection() {
        // Reset all buttons
        btnThisWeek.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
        btnThisMonth.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
        btnThisYear.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));

        // Highlight selected button
        switch (currentPeriod) {
            case "week":
                btnThisWeek.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
                break;
            case "month":
                btnThisMonth.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
                break;
            case "year":
                btnThisYear.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
                break;
        }
    }

    private void loadSpendingChartFragment() {
        SpendingChartFragment chartFragment = SpendingChartFragment.newInstance(startDate.getTime(), endDate.getTime());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.chartFragmentContainer, chartFragment);
        transaction.commit();
    }

    private void updateUI() {
        // Update spending chart fragment
        loadSpendingChartFragment();

        // Load spending data
        float totalSpent = transactionManager.calculateTotalSpentBetweenDates(startDate, endDate);
        String topCategory = transactionManager.getTopCategoryBetweenDates(startDate, endDate);

        // Calculate average daily spend
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        long startMillis = calendar.getTimeInMillis();
        calendar.setTime(endDate);
        long endMillis = calendar.getTimeInMillis();
        int daysBetween = (int) ((endMillis - startMillis) / (1000 * 60 * 60 * 24)) + 1;
        float averageDailySpend = totalSpent / daysBetween;

        // Format currency values
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

        // Update TextViews
        tvTotalSpent.setText(currencyFormatter.format(totalSpent));
        tvTopCategory.setText(topCategory != null ? topCategory : "None");
        tvAverageSpend.setText(currencyFormatter.format(averageDailySpend));

        // Update category summary RecyclerView
        List<CategorySummary> categorySummaries = transactionManager.getCategorySummariesBetweenDates(startDate, endDate);
        CategorySummaryAdapter adapter = new CategorySummaryAdapter(categorySummaries);
        recyclerViewCategories.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ReportsActivity resumed");
        updateUI();
    }

    // Activity Lifecycle Methods with Logging

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ReportsActivity visible");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ReportsActivity paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ReportsActivity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ReportsActivity destroyed");
    }
}