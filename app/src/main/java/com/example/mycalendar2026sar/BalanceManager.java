package com.example.mycalendar2026sar;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BalanceManager {

    private static final String PREFS_NAME = "ExpensesPrefs";
    private static final String KEY_ACCOUNT_LIST = "AccountList";

    public static void updateAccountBalance(Context context, String accountName, double delta) {
        List<Account> accounts = loadAccounts(context);
        boolean found = false;
        for (Account account : accounts) {
            if (account.getName().equals(accountName)) {
                account.setBalance(account.getBalance() + delta);
                found = true;
                break;
            }
        }
        if (found) {
            saveAccounts(context, accounts);
        }
    }

    public static List<Account> loadAccounts(Context context) {
        List<Account> accountList = new ArrayList<>();
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_ACCOUNT_LIST, null);
            if (json != null) {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    accountList.add(new Account(obj.getString("name"), obj.getDouble("balance")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }

    public static void saveAccounts(Context context, List<Account> accountList) {
        try {
            double totalBalance = 0;
            JSONArray array = new JSONArray();
            for (Account account : accountList) {
                totalBalance += account.getBalance();
                JSONObject obj = new JSONObject();
                obj.put("name", account.getName());
                obj.put("balance", account.getBalance());
                array.put(obj);
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_ACCOUNT_LIST, array.toString())
                    .apply();

            // Update System Monthly Income in DB
            TransactionDbHelper.getInstance(context).addOrUpdateMonthlyIncome(totalBalance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
