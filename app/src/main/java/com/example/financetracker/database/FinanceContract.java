package com.example.financetracker.database;

import android.provider.BaseColumns;

public final class FinanceContract {

    // Private constructor to prevent accidental instantiation
    private FinanceContract() {}

    // TransactionEntry inner class defines constants for the transactions table
    public static class TransactionEntry implements BaseColumns {
        public static final String TABLE_NAME = "transactions";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_IS_RECURRING = "is_recurring";
        public static final String COLUMN_IS_INCOME = "is_income"; // Added isIncome column
        public static final String COLUMN_NOTES = "notes";
    }

    // CacheEntry inner class defines constants for the cache table
    public static class CacheEntry implements BaseColumns {
        public static final String TABLE_NAME = "cache";
        public static final String COLUMN_CACHE_KEY = "cache_key";
        public static final String COLUMN_CACHE_DATA = "cache_data";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}