package com.example.financetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.financetracker.database.FinanceContract.TransactionEntry;
import com.example.financetracker.database.FinanceContract.CacheEntry;

public class FinanceDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "FinanceDbHelper";

    // Database information
    private static final String DATABASE_NAME = "finance.db";
    private static final int DATABASE_VERSION = 3; // Incrementing because we're adding a column

    // SQL for creating the transactions table
    private static final String SQL_CREATE_TRANSACTIONS_TABLE =
            "CREATE TABLE " + TransactionEntry.TABLE_NAME + " (" +
                    TransactionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TransactionEntry.COLUMN_AMOUNT + " REAL NOT NULL," +
                    TransactionEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                    TransactionEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL," +
                    TransactionEntry.COLUMN_DATE + " INTEGER NOT NULL," +
                    TransactionEntry.COLUMN_IS_RECURRING + " INTEGER NOT NULL," +
                    TransactionEntry.COLUMN_IS_INCOME + " INTEGER NOT NULL DEFAULT 0," + // Adding isIncome column
                    TransactionEntry.COLUMN_NOTES + " TEXT," +
                    "firebase_key TEXT)"; // Add Firebase key column

    // SQL for creating the cache table
    private static final String SQL_CREATE_CACHE_TABLE =
            "CREATE TABLE " + CacheEntry.TABLE_NAME + " (" +
                    CacheEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CacheEntry.COLUMN_CACHE_KEY + " TEXT UNIQUE NOT NULL," +
                    CacheEntry.COLUMN_CACHE_DATA + " TEXT NOT NULL," +
                    CacheEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL)";

    // SQL for deleting the transactions table
    private static final String SQL_DELETE_TRANSACTIONS_TABLE =
            "DROP TABLE IF EXISTS " + TransactionEntry.TABLE_NAME;

    // SQL for deleting the cache table
    private static final String SQL_DELETE_CACHE_TABLE =
            "DROP TABLE IF EXISTS " + CacheEntry.TABLE_NAME;

    // SQL for adding the isIncome column if upgrading from earlier version
    private static final String SQL_ADD_IS_INCOME_COLUMN =
            "ALTER TABLE " + TransactionEntry.TABLE_NAME +
                    " ADD COLUMN " + TransactionEntry.COLUMN_IS_INCOME + " INTEGER NOT NULL DEFAULT 0";

    public FinanceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Creating database tables");
        db.execSQL(SQL_CREATE_TRANSACTIONS_TABLE);
        db.execSQL(SQL_CREATE_CACHE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2) {
            // Add cache table if upgrading from version 1
            db.execSQL(SQL_CREATE_CACHE_TABLE);
        }

        if (oldVersion < 3) {
            // Add isIncome column if upgrading from version 2 or earlier
            try {
                db.execSQL(SQL_ADD_IS_INCOME_COLUMN);
                Log.d(TAG, "onUpgrade: Added isIncome column");
            } catch (Exception e) {
                Log.e(TAG, "onUpgrade: Error adding isIncome column", e);
                // If column already exists or other error, handle gracefully
            }
        }

        // For more severe upgrades we might use:
        // db.execSQL(SQL_DELETE_TRANSACTIONS_TABLE);
        // db.execSQL(SQL_DELETE_CACHE_TABLE);
        // onCreate(db);
    }
}