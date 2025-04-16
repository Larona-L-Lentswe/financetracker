package com.example.financetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.financetracker.models.Category;
import com.example.financetracker.models.Transaction;
import com.example.financetracker.utils.TransactionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String TAG = "AddTransactionActivity";

    // View elements
    private EditText etAmount;
    private EditText etDescription;
    private Spinner spinnerCategory;
    private TextView tvDate;
    private Button btnPickDate;
    private CheckBox checkboxRecurring;
    private EditText etNotes;
    private Button btnSaveTransaction;

    // Date related variables
    private Calendar selectedDate;
    private SimpleDateFormat dateFormatter;

    // State keys for saving instance state
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CATEGORY_POS = "category_position";
    private static final String KEY_DATE = "date";
    private static final String KEY_RECURRING = "recurring";
    private static final String KEY_NOTES = "notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        Log.d(TAG, "onCreate: AddTransactionActivity started");

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Set up the date picker
        setupDatePicker();

        // Set up category spinner
        setupCategorySpinner();

        // Set up save button click listener
        setupSaveButton();

        // Restore saved instance state if available
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvDate = findViewById(R.id.tvDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        checkboxRecurring = findViewById(R.id.checkboxRecurring);
        etNotes = findViewById(R.id.etNotes);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);
    }

    private void setupDatePicker() {
        // Initialize date variables
        selectedDate = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Set current date to TextView
        updateDateDisplay();

        // Set up date picker button
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        // Make the TextView also clickable to show date picker
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplay();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        tvDate.setText(dateFormatter.format(selectedDate.getTime()));
    }

    private void setupCategorySpinner() {
        // Get categories (in a real app, these would come from a database)
        List<Category> categories = getCategoryList();

        // Create adapter for spinner
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to spinner
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private List<Category> getCategoryList() {
        // In a real app, these would be loaded from a database
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, "Food & Dining", R.drawable.ic_category_food));
        categories.add(new Category(2, "Transportation", R.drawable.ic_category_transport));
        categories.add(new Category(3, "Shopping", R.drawable.ic_category_shopping));
        categories.add(new Category(4, "Entertainment", R.drawable.ic_category_entertainment));
        categories.add(new Category(5, "Bills & Utilities", R.drawable.ic_category_bills));
        categories.add(new Category(6, "Healthcare", R.drawable.ic_category_healthcare));
        categories.add(new Category(7, "Other", R.drawable.ic_category_other));
        return categories;
    }

    private void setupSaveButton() {
        btnSaveTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    saveTransaction();
                }
            }
        });
    }

    private boolean validateInput() {
        // Check if amount is valid
        if (etAmount.getText().toString().trim().isEmpty()) {
            etAmount.setError("Amount is required");
            return false;
        }

        // Check if description is valid
        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Description is required");
            return false;
        }

        return true;
    }

    private void saveTransaction() {
        try {
            // Get input values
            float amount = Float.parseFloat(etAmount.getText().toString().trim());
            String description = etDescription.getText().toString().trim();
            Category category = (Category) spinnerCategory.getSelectedItem();
            Date date = selectedDate.getTime();
            boolean isRecurring = checkboxRecurring.isChecked();
            String notes = etNotes.getText().toString().trim();

            // Create transaction object
            Transaction transaction = new Transaction(
                    0, // ID will be assigned by database
                    amount,
                    description,
                    category.getId(),
                    date,
                    isRecurring,
                    notes
            );

            // Save transaction using TransactionManager
            TransactionManager transactionManager = TransactionManager.getInstance(this);
            long transactionId = transactionManager.addTransaction(transaction);

            if (transactionId > 0) {
                Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and return to previous screen
            } else {
                Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount format");
            Log.e(TAG, "saveTransaction: Invalid amount format", e);
        } catch (Exception e) {
            Toast.makeText(this, "Error saving transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveTransaction: Error saving transaction", e);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: Saving AddTransactionActivity state");

        // Save current input values
        outState.putString(KEY_AMOUNT, etAmount.getText().toString());
        outState.putString(KEY_DESCRIPTION, etDescription.getText().toString());
        outState.putInt(KEY_CATEGORY_POS, spinnerCategory.getSelectedItemPosition());
        outState.putLong(KEY_DATE, selectedDate.getTimeInMillis());
        outState.putBoolean(KEY_RECURRING, checkboxRecurring.isChecked());
        outState.putString(KEY_NOTES, etNotes.getText().toString());
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreInstanceState: Restoring AddTransactionActivity state");

        // Restore input values
        etAmount.setText(savedInstanceState.getString(KEY_AMOUNT, ""));
        etDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION, ""));
        spinnerCategory.setSelection(savedInstanceState.getInt(KEY_CATEGORY_POS, 0));

        // Restore date
        long timeInMillis = savedInstanceState.getLong(KEY_DATE, Calendar.getInstance().getTimeInMillis());
        selectedDate.setTimeInMillis(timeInMillis);
        updateDateDisplay();

        // Restore other fields
        checkboxRecurring.setChecked(savedInstanceState.getBoolean(KEY_RECURRING, false));
        etNotes.setText(savedInstanceState.getString(KEY_NOTES, ""));
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
        Log.d(TAG, "onStart: AddTransactionActivity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: AddTransactionActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: AddTransactionActivity paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: AddTransactionActivity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: AddTransactionActivity destroyed");
    }
}