package com.example.financetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "FinanceTrackerPrefs";

    // Preference keys
    private static final String PREF_MONTHLY_BUDGET = "monthly_budget";
    private static final String PREF_CURRENCY = "currency";
    private static final String PREF_SHOW_REMINDERS = "show_reminders";

    // View elements
    private EditText etMonthlyBudget;
    private Spinner spinnerCurrency;
    private CheckBox checkboxShowReminders;
    private Button btnSaveBudget;
    private Button btnSaveSettings;

    // Currencies for the spinner
    private String[] currencies = {"USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "CAD ($)", "AUD ($)"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, "onCreate: SettingsActivity started");

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Set up currency spinner
        setupCurrencySpinner();

        // Load saved preferences
        loadPreferences();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initViews() {
        etMonthlyBudget = findViewById(R.id.etMonthlyBudget);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        checkboxShowReminders = findViewById(R.id.checkboxShowReminders);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
    }

    private void setupCurrencySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load monthly budget
        float monthlyBudget = prefs.getFloat(PREF_MONTHLY_BUDGET, 0.0f);
        if (monthlyBudget > 0) {
            etMonthlyBudget.setText(String.valueOf(monthlyBudget));
        }

        // Load currency preference
        String currency = prefs.getString(PREF_CURRENCY, currencies[0]);
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currency)) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }

        // Load reminders preference
        boolean showReminders = prefs.getBoolean(PREF_SHOW_REMINDERS, true);
        checkboxShowReminders.setChecked(showReminders);
    }

    private void setupButtonListeners() {
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMonthlyBudget();
            }
        });

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAllSettings();
            }
        });
    }

    private void saveMonthlyBudget() {
        try {
            String budgetStr = etMonthlyBudget.getText().toString().trim();
            if (budgetStr.isEmpty()) {
                Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show();
                return;
            }

            float budget = Float.parseFloat(budgetStr);
            if (budget <= 0) {
                Toast.makeText(this, "Budget must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putFloat(PREF_MONTHLY_BUDGET, budget);
            editor.apply();

            Toast.makeText(this, "Budget saved: " + budgetStr, Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid budget format", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveMonthlyBudget: Error parsing budget", e);
        }
    }

    private void saveAllSettings() {
        // Save budget
        try {
            String budgetStr = etMonthlyBudget.getText().toString().trim();
            float budget = 0.0f;
            if (!budgetStr.isEmpty()) {
                budget = Float.parseFloat(budgetStr);
            }

            // Get currency selection
            String currency = currencies[spinnerCurrency.getSelectedItemPosition()];

            // Get reminders preference
            boolean showReminders = checkboxShowReminders.isChecked();

            // Save all to SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

            if (budget > 0) {
                editor.putFloat(PREF_MONTHLY_BUDGET, budget);
            }

            editor.putString(PREF_CURRENCY, currency);
            editor.putBoolean(PREF_SHOW_REMINDERS, showReminders);
            editor.apply();

            Toast.makeText(this, "All settings saved", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid budget format", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveAllSettings: Error parsing budget", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Activity Lifecycle Methods with Logging

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: SettingsActivity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: SettingsActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: SettingsActivity paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: SettingsActivity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: SettingsActivity destroyed");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: Saving SettingsActivity state");
    }
}