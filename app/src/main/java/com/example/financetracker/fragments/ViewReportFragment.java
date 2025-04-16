package com.example.financetracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.adapters.CategorySummaryAdapter;
import com.example.financetracker.models.CategorySummary;
import com.example.financetracker.utils.TransactionManager;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewReportFragment extends Fragment {

    private static final String TAG = "ViewReportFragment";

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

    public ViewReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ViewReportFragment");

        // Initialize TransactionManager
        transactionManager = TransactionManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_report, container, false);

        // Initialize views
        initViews(view);

        // Set up date range buttons
        setupDateRangeButtons();

        // Set default date range to current month
        setDateRangeToCurrentMonth();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load spending chart fragment
        loadSpendingChartFragment();

        // Update UI with data
        updateUI();
    }

    private void initViews(View view) {
        btnThisWeek = view.findViewById(R.id.btnThisWeek);
        btnThisMonth = view.findViewById(R.id.btnThisMonth);
        btnThisYear = view.findViewById(R.id.btnThisYear);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvTopCategory = view.findViewById(R.id.tvTopCategory);
        tvAverageSpend = view.findViewById(R.id.tvAverageSpend);
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
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
        btnThisWeek.setBackgroundColor(requireContext().getColor(R.color.colorPrimary));
        btnThisMonth.setBackgroundColor(requireContext().getColor(R.color.colorPrimary));
        btnThisYear.setBackgroundColor(requireContext().getColor(R.color.colorPrimary));

        // Highlight selected button
        switch (currentPeriod) {
            case "week":
                btnThisWeek.setBackgroundColor(requireContext().getColor(R.color.colorAccent));
                break;
            case "month":
                btnThisMonth.setBackgroundColor(requireContext().getColor(R.color.colorAccent));
                break;
            case "year":
                btnThisYear.setBackgroundColor(requireContext().getColor(R.color.colorAccent));
                break;
        }
    }

    private void loadSpendingChartFragment() {
        SpendingChartFragment chartFragment = SpendingChartFragment.newInstance(startDate.getTime(), endDate.getTime());
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ViewReportFragment resumed");
        updateUI();
    }
}