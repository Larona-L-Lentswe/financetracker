package com.example.financetracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // includes BottomNavigationView

        BottomNavigationView bottomNav = findViewById(R.id.bottonNavigationView);

        // Load the default fragment
        loadFragment(new HomeFragment());

        // Set listener using if statements
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.mi_home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.mi_addtransaction) {
                loadFragment(new AddTransactionFragment());
            } else if (id == R.id.mi_viewreport) {
                loadFragment(new ViewReportFragment());
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
    }
}
