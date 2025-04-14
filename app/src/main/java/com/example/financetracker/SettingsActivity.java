package com.example.financetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        EditText editBudget = findViewById(R.id.editBudget);
        EditText editCurrency = findViewById(R.id.editCurrency);
        Button btnSave = findViewById(R.id.btnSave);

        // SharedPreferences setup
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Load existing preferences
        editBudget.setText(prefs.getString("budget", ""));
        editCurrency.setText(prefs.getString("currency", ""));

        // Save preferences on button click
        btnSave.setOnClickListener(v -> {
            String budget = editBudget.getText().toString().trim();
            String currency = editCurrency.getText().toString().trim();

            if (!budget.isEmpty() && !currency.isEmpty()) {
                editor.putString("budget", budget);
                editor.putString("currency", currency);
                editor.apply();

                Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}