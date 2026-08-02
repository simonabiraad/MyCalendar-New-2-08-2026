package com.example.mycalendar2026sar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Single shared SQLite database for the app's transactions (Cash In / Cash Out).
 * All expense-related screens should read/write through this helper so there is
 * only ever one source of truth for transaction data.
 */
public class TransactionDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "mycalendar.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_TYPE = "type";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_ACCOUNT = "account";
    public static final String COL_NOTES = "notes";
    public static final String COL_VOICE_PATH = "voice_path";
    public static final String COL_BILLS = "bills";

    private static TransactionDbHelper instance;

    public static synchronized TransactionDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private TransactionDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_TYPE + " TEXT NOT NULL, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_ACCOUNT + " TEXT, " +
                COL_NOTES + " TEXT, " +
                COL_VOICE_PATH + " TEXT, " +
                COL_BILLS + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COL_NOTES + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COL_VOICE_PATH + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COL_BILLS + " TEXT");
        }
    }

    public long addTransaction(String title, double amount, String type, long timestamp, String account) {
        return addTransaction(title, amount, type, timestamp, account, "", "", "");
    }

    public long addTransaction(String title, double amount, String type, long timestamp, String account, String notes, String voicePath, String bills) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_AMOUNT, amount);
        values.put(COL_TYPE, type);
        values.put(COL_TIMESTAMP, timestamp);
        values.put(COL_ACCOUNT, account);
        values.put(COL_NOTES, notes);
        values.put(COL_VOICE_PATH, voicePath);
        values.put(COL_BILLS, bills);
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    public void deleteTransaction(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void addOrUpdateMonthlyIncome(double amount) {
        SQLiteDatabase db = getWritableDatabase();
        String title = "Monthly Income";
        
        // Check if "Monthly Income" exists
        Cursor c = db.query(TABLE_TRANSACTIONS, new String[]{COL_ID}, COL_TITLE + "=?", new String[]{title}, null, null, null);
        if (c != null && c.moveToFirst()) {
            long id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            c.close();
            
            // Update existing
            ContentValues values = new ContentValues();
            values.put(COL_AMOUNT, amount);
            values.put(COL_TIMESTAMP, System.currentTimeMillis()); // Keep it recent or keep original? User said "putted in all in one line", suggesting update.
            db.update(TABLE_TRANSACTIONS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        } else {
            if (c != null) c.close();
            // Add new
            addTransaction(title, amount, Transaction.TYPE_CASH_IN, System.currentTimeMillis(), "System");
        }
    }

    /** Returns every transaction, sorted oldest -> newest (used for running-balance math). */
    public List<Transaction> getAllTransactionsAscending() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTIONS, null, null, null, null, null, COL_TIMESTAMP + " ASC, " + COL_ID + " ASC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(new Transaction(
                        c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT)),
                        c.getString(c.getColumnIndexOrThrow(COL_TYPE)),
                        c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP)),
                        c.getString(c.getColumnIndexOrThrow(COL_ACCOUNT)),
                        c.getString(c.getColumnIndexOrThrow(COL_NOTES)),
                        c.getString(c.getColumnIndexOrThrow(COL_VOICE_PATH)),
                        c.getString(c.getColumnIndexOrThrow(COL_BILLS))
                ));
            }
            c.close();
        }
        return list;
    }
}
