package com.example.financetracker.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.financetracker.R;
import com.example.financetracker.models.Category;
import com.example.financetracker.models.Transaction;
import com.example.financetracker.utils.CategoryManager;
import com.example.financetracker.utils.TransactionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionFragment extends BaseCachedFragment {

    private static final String TAG = "AddTransactionFragment";
    private static final String CACHE_KEY = "add_transaction_fragment";

    // State keys for cache
    private static final String STATE_AMOUNT = "amount";
    private static final String STATE_DESCRIPTION = "description";
    private static final String STATE_CATEGORY_POS = "category_position";
    private static final String STATE_DATE = "date";
    private static final String STATE_RECURRING = "recurring";
    private static final String STATE_NOTES = "notes";
    private static final String STATE_IS_INCOME = "is_income";

    // View elements
    private RadioGroup radioGroupTransactionType;
    private RadioButton radioExpense;
    private RadioButton radioIncome;
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

    // Managers
    private TransactionManager transactionManager;
    private CategoryManager categoryManager;

    public AddTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getCacheKey() {
        return CACHE_KEY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: AddTransactionFragment");

        // Initialize managers
        transactionManager = TransactionManager.getInstance(requireContext());
        categoryManager = CategoryManager.getInstance(requireContext());

        // Initialize date variables
        selectedDate = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        // Initialize views
        initViews(view);

        // Set up the date picker
        setupDatePicker();

        // Set up category spinner
        setupCategorySpinner();

        // Set up save button click listener
        setupSaveButton();

        return view;
    }

    private void initViews(View view) {
        radioGroupTransactionType = view.findViewById(R.id.radioGroupTransactionType);
        radioExpense = view.findViewById(R.id.radioExpense);
        radioIncome = view.findViewById(R.id.radioIncome);
        etAmount = view.findViewById(R.id.etAmount);
        etDescription = view.findViewById(R.id.etDescription);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        tvDate = view.findViewById(R.id.tvDate);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        checkboxRecurring = view.findViewById(R.id.checkboxRecurring);
        etNotes = view.findViewById(R.id.etNotes);
        btnSaveTransaction = view.findViewById(R.id.btnSaveTransaction);
    }

    private void setupDatePicker() {
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
                requireContext(),
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
        // Get categories
        List<Category> categories = categoryManager.getAllCategories();

        // Create adapter for spinner
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to spinner
        spinnerCategory.setAdapter(categoryAdapter);
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
            boolean isIncome = radioIncome.isChecked(); // Get income status from radio button

            // Create transaction object with income flag
            Transaction transaction = new Transaction(
                    0, // ID will be assigned by database
                    amount,
                    description,
                    category.getId(),
                    date,
                    isRecurring,
                    notes,
                    isIncome // Pass income status to constructor
            );

            // Save transaction using TransactionManager
            long transactionId = transactionManager.addTransaction(transaction);

            if (transactionId > 0) {
                Toast.makeText(requireContext(), "Transaction saved successfully", Toast.LENGTH_SHORT).show();

                // Clear form fields
                clearFormFields();

                // Return to home fragment
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(requireContext(), "Failed to save transaction", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount format");
            Log.e(TAG, "saveTransaction: Invalid amount format", e);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveTransaction: Error saving transaction", e);
        }
    }

    private void clearFormFields() {
        radioExpense.setChecked(true); // Reset to expense
        etAmount.setText("");
        etDescription.setText("");
        spinnerCategory.setSelection(0);
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
        checkboxRecurring.setChecked(false);
        etNotes.setText("");
    }

    @Override
    protected void saveState(Bundle outState) {
        // Save form field values
        outState.putString(STATE_AMOUNT, etAmount.getText().toString());
        outState.putString(STATE_DESCRIPTION, etDescription.getText().toString());
        outState.putInt(STATE_CATEGORY_POS, spinnerCategory.getSelectedItemPosition());
        outState.putLong(STATE_DATE, selectedDate.getTimeInMillis());
        outState.putBoolean(STATE_RECURRING, checkboxRecurring.isChecked());
        outState.putString(STATE_NOTES, etNotes.getText().toString());
        outState.putBoolean(STATE_IS_INCOME, radioIncome.isChecked()); // Save income selection
    }

    @Override
    protected void restoreState(Bundle state) {
        // Note: This will be called before the views are created in onCreateView
        if (radioGroupTransactionType != null) {
            if (state.getBoolean(STATE_IS_INCOME, false)) {
                radioIncome.setChecked(true);
            } else {
                radioExpense.setChecked(true);
            }
        }

        if (etAmount != null) {
            etAmount.setText(state.getString(STATE_AMOUNT, ""));
        }

        if (etDescription != null) {
            etDescription.setText(state.getString(STATE_DESCRIPTION, ""));
        }

        if (spinnerCategory != null && spinnerCategory.getAdapter() != null) {
            spinnerCategory.setSelection(state.getInt(STATE_CATEGORY_POS, 0));
        }

        if (selectedDate != null) {
            long dateMillis = state.getLong(STATE_DATE, Calendar.getInstance().getTimeInMillis());
            selectedDate.setTimeInMillis(dateMillis);
            if (tvDate != null) {
                updateDateDisplay();
            }
        }

        if (checkboxRecurring != null) {
            checkboxRecurring.setChecked(state.getBoolean(STATE_RECURRING, false));
        }

        if (etNotes != null) {
            etNotes.setText(state.getString(STATE_NOTES, ""));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Now that the views are initialized, apply the restored state again
        // This is needed because restoreState() might be called before views are created
        if (savedInstanceState != null) {
            // Set income/expense selection
            if (savedInstanceState.getBoolean(STATE_IS_INCOME, false)) {
                radioIncome.setChecked(true);
            } else {
                radioExpense.setChecked(true);
            }

            etAmount.setText(savedInstanceState.getString(STATE_AMOUNT, ""));
            etDescription.setText(savedInstanceState.getString(STATE_DESCRIPTION, ""));
            spinnerCategory.setSelection(savedInstanceState.getInt(STATE_CATEGORY_POS, 0));
            long dateMillis = savedInstanceState.getLong(STATE_DATE, Calendar.getInstance().getTimeInMillis());
            selectedDate.setTimeInMillis(dateMillis);
            updateDateDisplay();
            checkboxRecurring.setChecked(savedInstanceState.getBoolean(STATE_RECURRING, false));
            etNotes.setText(savedInstanceState.getString(STATE_NOTES, ""));
        }
    }
}