package com.example.financetracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.financetracker.R;
import com.example.financetracker.models.CategorySummary;
import com.example.financetracker.utils.TransactionManager;
import com.example.financetracker.views.ChartLegendView;
import com.example.financetracker.views.PieChartView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpendingChartFragment extends BaseCachedFragment {

    private static final String TAG = "SpendingChartFragment";
    private static final String CACHE_KEY = "spending_chart_fragment";

    private static final String ARG_START_DATE = "arg_start_date";
    private static final String ARG_END_DATE = "arg_end_date";

    // Views
    private PieChartView pieChart;
    private ChartLegendView chartLegend;
    private TextView tvChartTitle;

    // Data
    private long startDateMillis;
    private long endDateMillis;
    private TransactionManager transactionManager;

    public SpendingChartFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY + "_" + startDateMillis + "_" + endDateMillis;
    }

    public static SpendingChartFragment newInstance(long startDateMillis, long endDateMillis) {
        SpendingChartFragment fragment = new SpendingChartFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_START_DATE, startDateMillis);
        args.putLong(ARG_END_DATE, endDateMillis);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: SpendingChartFragment");

        // Get arguments
        if (getArguments() != null) {
            startDateMillis = getArguments().getLong(ARG_START_DATE);
            endDateMillis = getArguments().getLong(ARG_END_DATE);
        }

        // Initialize TransactionManager
        transactionManager = TransactionManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spending_chart, container, false);
        Log.d(TAG, "onCreateView: SpendingChartFragment view created");

        // Initialize views
        pieChart = view.findViewById(R.id.pieChart);
        chartLegend = view.findViewById(R.id.chartLegend);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: SpendingChartFragment");

        // Set chart title
        setChartTitle();

        // Configure pie chart
        configurePieChart();

        // Load and display data
        loadChartData();
    }

    private void setChartTitle() {
        // Format dates for title
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String startDateStr = dateFormat.format(new Date(startDateMillis));
        String endDateStr = dateFormat.format(new Date(endDateMillis));

        // Set title with date range
        tvChartTitle.setText(String.format("Spending by Category (%s - %s)", startDateStr, endDateStr));
    }

    private void configurePieChart() {
        // Set up the pie chart
        pieChart.setNoDataText("No spending data available");
    }

    private void loadChartData() {
        // Get category summaries from TransactionManager
        List<CategorySummary> categorySummaries = transactionManager.getCategorySummariesBetweenDates(
                new Date(startDateMillis), new Date(endDateMillis));

        // Check if we have data
        if (categorySummaries.isEmpty()) {
            pieChart.setData(new ArrayList<>()); // Empty list will show "No data" message
            chartLegend.setEntries(new ArrayList<>()); // Empty legend
            return;
        }

        // Create pie chart entries
        List<PieChartView.PieEntry> pieEntries = new ArrayList<>();

        // Create legend entries
        List<ChartLegendView.LegendEntry> legendEntries = new ArrayList<>();

        // Convert category summaries to pie entries and legend entries
        for (CategorySummary categorySummary : categorySummaries) {
            if (categorySummary.getPercentage() > 0) {
                // Add to pie chart
                pieEntries.add(new PieChartView.PieEntry(
                        categorySummary.getTotalAmount(),
                        categorySummary.getCategoryName()
                ));

                // Add to legend
                legendEntries.add(new ChartLegendView.LegendEntry(
                        categorySummary.getCategoryName(),
                        categorySummary.getTotalAmount(),
                        categorySummary.getPercentage()
                ));
            }
        }

        // Set data to chart and legend
        pieChart.setData(pieEntries);
        chartLegend.setEntries(legendEntries);
    }

    @Override
    protected void saveState(Bundle outState) {
        // Store date range in case we need to restore it
        outState.putLong(ARG_START_DATE, startDateMillis);
        outState.putLong(ARG_END_DATE, endDateMillis);
    }

    @Override
    protected void restoreState(Bundle state) {
        // Restore date range
        startDateMillis = state.getLong(ARG_START_DATE);
        endDateMillis = state.getLong(ARG_END_DATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: SpendingChartFragment resumed");

        // Refresh data when fragment resumes
        loadChartData();
    }
}