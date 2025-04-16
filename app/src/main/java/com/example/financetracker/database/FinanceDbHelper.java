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
    private static final int DATABASE_VERSION = 2; // Incremented version for adding cache table

    // SQL for creating the transactions table
    private static final String SQL_CREATE_TRANSACTIONS_TABLE =
            "CREATE TABLE " + TransactionEntry.TABLE_NAME + " (" +
                    TransactionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    TransactionEntry.COLUMN_AMOUNT + " REAL NOT NULL," +
                    TransactionEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                    TransactionEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL," +
                    TransactionEntry.COLUMN_DATE + " INTEGER NOT NULL," +
                    TransactionEntry.COLUMN_IS_RECURRING + " INTEGER NOT NULL," +
                    TransactionEntry.COLUMN_NOTES + " TEXT)";

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

        // For more significant changes we might use:
        // db.execSQL(SQL_DELETE_TRANSACTIONS_TABLE);
        // db.execSQL(SQL_DELETE_CACHE_TABLE);
        // onCreate(db);
    }
}