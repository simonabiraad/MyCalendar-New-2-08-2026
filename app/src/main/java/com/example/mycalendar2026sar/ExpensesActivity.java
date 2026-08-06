package com.example.mycalendar2026sar;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpensesActivity extends AppCompatActivity {

    // Filter modes for the transaction list
    private static final int FILTER_ALL = 0;
    private static final int FILTER_DAILY = 1;
    private static final int FILTER_WEEKLY = 2;
    private static final int FILTER_MONTHLY = 3;
    private static final int FILTER_YEARLY = 4;

    private DrawerLayout drawerLayout;
    private List<Account> accountList = new ArrayList<>();
    private AccountAdapter adapter;
    private Button topExpensesButton, allButton, dailyButton, weeklyButton, monthlyButton, yearlyButton;

    private TransactionDbHelper transactionDbHelper;
    private TransactionAdapter transactionAdapter;
    private RecyclerView transactionsRecyclerView;
    private View emptyStateText;
    private TextView cashInTotalText;
    private TextView cashOutTotalText;
    private TextView balanceTotalText;
    private TextView previousBalanceTotalText;
    private TextView finalBalanceTotalText;
    private View previousBalanceRow, finalBalanceRow;

    private int currentFilter = FILTER_ALL;
    private String currentSearchQuery = "";
    private boolean isVoiceCommandMode = false;

    private final ActivityResultLauncher<Intent> voiceRecognitionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        if (isVoiceCommandMode) {
                            isVoiceCommandMode = false;
                            processVoiceCommand(spokenText);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expenses);

        loadAccounts();
        if (accountList.isEmpty()) {
            accountList.add(new Account("Expenses", 0.00));
            accountList.add(new Account("Cash", 500.00));
            accountList.add(new Account("Bank", 1500.00));
            saveAccounts();
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        topExpensesButton = findViewById(R.id.topExpensesButton);
        allButton = findViewById(R.id.allButton);
        dailyButton = findViewById(R.id.dailyButton);
        weeklyButton = findViewById(R.id.weeklyButton);
        monthlyButton = findViewById(R.id.monthlyButton);
        yearlyButton = findViewById(R.id.yearlyButton);
        NavigationView navigationView = findViewById(R.id.expensesNavigationView);
        
        // Disable icon tinting to show real colors
        navigationView.setItemIconTintList(null);

        // --- Transaction list setup ---
        transactionDbHelper = TransactionDbHelper.getInstance(this);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        cashInTotalText = findViewById(R.id.cashInTotalText);
        cashOutTotalText = findViewById(R.id.cashOutTotalText);
        balanceTotalText = findViewById(R.id.balanceTotalText);
        previousBalanceTotalText = findViewById(R.id.previousBalanceTotalText);
        finalBalanceTotalText = findViewById(R.id.finalBalanceTotalText);
        previousBalanceRow = findViewById(R.id.previousBalanceRow);
        finalBalanceRow = findViewById(R.id.finalBalanceRow);

        transactionAdapter = new TransactionAdapter(this::confirmDeleteTransaction);
        transactionAdapter.setOnTransactionClickListener(this::showTransactionNotePopup);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(transactionAdapter);

        refreshTransactionsList();
        
        // Persist default account
        saveActiveAccount(topExpensesButton.getText().toString());

        findViewById(R.id.expensesMenuButton).setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        findViewById(R.id.aiAssistantButton).setOnClickListener(v -> {
            isVoiceCommandMode = true;
            startVoiceRecognition();
        });

        topExpensesButton.setOnClickListener(v -> showAccountsDialog());

        findViewById(R.id.expensesOverflowButton).setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.menu_expenses_overflow, popup.getMenu());
            
            // Force icons to show
            try {
                java.lang.reflect.Field field = popup.getClass().getDeclaredField("mPopup");
                field.setAccessible(true);
                Object menuHelper = field.get(popup);
                Class<?> classPopupHelper = Class.forName(menuHelper.getClass().getName());
                java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuHelper, true);
            } catch (Exception ignored) {}

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                String title = String.valueOf(item.getTitle());
                if (id == R.id.action_date_asc || id == R.id.action_date_desc) {
                    item.setChecked(true);
                } else if (id == R.id.action_category) {
                    startActivity(new Intent(this, CategoryActivity.class));
                }
                Toast.makeText(this, title + " selected", Toast.LENGTH_SHORT).show();
                return true;
            });
            popup.show();
        });

        findViewById(R.id.expensesExportButton).setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.menu_expenses_export, popup.getMenu());

            // Force icons to show
            try {
                java.lang.reflect.Field field = popup.getClass().getDeclaredField("mPopup");
                field.setAccessible(true);
                Object menuHelper = field.get(popup);
                Class<?> classPopupHelper = Class.forName(menuHelper.getClass().getName());
                java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuHelper, true);
            } catch (Exception ignored) {}

            popup.setOnMenuItemClickListener(item -> {
                String title = String.valueOf(item.getTitle());
                Toast.makeText(this, "Exporting to " + title + "...", Toast.LENGTH_SHORT).show();
                return true;
            });
            popup.show();
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_remove_ads) {
                Toast.makeText(this, "Remove Ads feature coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_summary) {
                Toast.makeText(this, "Summary feature coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_account_summary) {
                Toast.makeText(this, "Account Summary feature coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_transaction_all) {
                Toast.makeText(this, "Transaction - All Accounts feature coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_accounts) {
                Toast.makeText(this, "Accounts management coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_transfer) {
                startActivity(new Intent(this, TransferActivity.class));
            } else if (id == R.id.nav_report_all) {
                Toast.makeText(this, "Reports feature coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_transaction_names) {
                Toast.makeText(this, "Transaction Names management coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_notebook) {
                Toast.makeText(this, "Notebook feature coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_calendar) {
                finish(); // Go back to MainActivity
            } else if (id == R.id.nav_cash_calculator) {
                Toast.makeText(this, "Cash Calculator coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_backup_restore) {
                Toast.makeText(this, "Backup & Restore coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_setting) {
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_deleted_transactions) {
                Toast.makeText(this, "Deleted Transactions folder coming soon", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_rate_us) {
                Toast.makeText(this, "Thank you for wanting to rate us!", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_recommend) {
                Toast.makeText(this, "Recommendations feature coming soon", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.expenses_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.dailyButton).setOnClickListener(v -> {
            View subDaily = findViewById(R.id.subDailyContainer);
            View subAll = findViewById(R.id.subExpensesContainer);
            View subWeekly = findViewById(R.id.subWeeklyContainer);
            View subMonthly = findViewById(R.id.subMonthlyContainer);
            View subYearly = findViewById(R.id.subYearlyContainer);
            subAll.setVisibility(View.GONE);
            subWeekly.setVisibility(View.GONE);
            subMonthly.setVisibility(View.GONE);
            subYearly.setVisibility(View.GONE);
            if (subDaily.getVisibility() == View.VISIBLE) {
                subDaily.setVisibility(View.GONE);
            } else {
                subDaily.setVisibility(View.VISIBLE);
            }
            currentFilter = FILTER_DAILY;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        Button subTodayButton = findViewById(R.id.subTodayButton);
        SimpleDateFormat dateSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String todayDate = dateSdf.format(new java.util.Date());
        subTodayButton.setText(todayDate);
        subTodayButton.setOnClickListener(v -> {
            if (subTodayButton.getText().toString().equals(todayDate)) {
                subTodayButton.setText("Today");
            } else {
                subTodayButton.setText(todayDate);
            }
            currentFilter = FILTER_DAILY;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        findViewById(R.id.weeklyButton).setOnClickListener(v -> {
            View subWeekly = findViewById(R.id.subWeeklyContainer);
            View subAll = findViewById(R.id.subExpensesContainer);
            View subDaily = findViewById(R.id.subDailyContainer);
            View subMonthly = findViewById(R.id.subMonthlyContainer);
            View subYearly = findViewById(R.id.subYearlyContainer);
            subAll.setVisibility(View.GONE);
            subDaily.setVisibility(View.GONE);
            subMonthly.setVisibility(View.GONE);
            subYearly.setVisibility(View.GONE);
            if (subWeekly.getVisibility() == View.VISIBLE) {
                subWeekly.setVisibility(View.GONE);
            } else {
                subWeekly.setVisibility(View.VISIBLE);
            }
            currentFilter = FILTER_WEEKLY;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        Button subWeeklyRangeButton = findViewById(R.id.subWeeklyRangeButton);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String startWeekly = sdf.format(calendar.getTime());
        calendar.add(Calendar.DATE, 6);
        String endWeekly = sdf.format(calendar.getTime());
        String weeklyRange = startWeekly + " to " + endWeekly;
        subWeeklyRangeButton.setText(weeklyRange);
        subWeeklyRangeButton.setOnClickListener(v -> {
            if (subWeeklyRangeButton.getText().toString().equals(weeklyRange)) {
                subWeeklyRangeButton.setText("Weekly");
            } else {
                subWeeklyRangeButton.setText(weeklyRange);
            }
            currentFilter = FILTER_WEEKLY;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        findViewById(R.id.monthlyButton).setOnClickListener(v -> {
            View subMonthly = findViewById(R.id.subMonthlyContainer);
            View subAll = findViewById(R.id.subExpensesContainer);
            View subDaily = findViewById(R.id.subDailyContainer);
            View subWeekly = findViewById(R.id.subWeeklyContainer);
            View subYearly = findViewById(R.id.subYearlyContainer);
            subAll.setVisibility(View.GONE);
            subDaily.setVisibility(View.GONE);
            subWeekly.setVisibility(View.GONE);
            subYearly.setVisibility(View.GONE);
            if (subMonthly.getVisibility() == View.VISIBLE) {
                subMonthly.setVisibility(View.GONE);
            } else {
                subMonthly.setVisibility(View.VISIBLE);
            }
            currentFilter = FILTER_MONTHLY;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        Button subMonthlyRangeButton = findViewById(R.id.subMonthlyRangeButton);
        Calendar monthCal = Calendar.getInstance();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);
        String startMonth = sdf.format(monthCal.getTime());
        monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endMonth = sdf.format(monthCal.getTime());
        String monthlyRange = startMonth + " to " + endMonth;
        subMonthlyRangeButton.setText(monthlyRange);
        subMonthlyRangeButton.setOnClickListener(v -> {
            if (subMonthlyRangeButton.getText().toString().equals(monthlyRange)) {
                subMonthlyRangeButton.setText("Monthly");
            } else {
                subMonthlyRangeButton.setText(monthlyRange);
            }
            currentFilter = FILTER_MONTHLY;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        findViewById(R.id.yearlyButton).setOnClickListener(v -> {
            View subYearly = findViewById(R.id.subYearlyContainer);
            View subAll = findViewById(R.id.subExpensesContainer);
            View subDaily = findViewById(R.id.subDailyContainer);
            View subWeekly = findViewById(R.id.subWeeklyContainer);
            View subMonthly = findViewById(R.id.subMonthlyContainer);
            subAll.setVisibility(View.GONE);
            subDaily.setVisibility(View.GONE);
            subWeekly.setVisibility(View.GONE);
            subMonthly.setVisibility(View.GONE);
            if (subYearly.getVisibility() == View.VISIBLE) {
                subYearly.setVisibility(View.GONE);
            } else {
                subYearly.setVisibility(View.VISIBLE);
            }
            currentFilter = FILTER_YEARLY;
            updateFilterButtonsUI();
        });

        Button subYearlyRangeButton = findViewById(R.id.subYearlyRangeButton);
        Calendar yearCal = Calendar.getInstance();
        yearCal.set(Calendar.DAY_OF_YEAR, 1);
        String startYear = sdf.format(yearCal.getTime());
        yearCal.set(Calendar.DAY_OF_YEAR, yearCal.getActualMaximum(Calendar.DAY_OF_YEAR));
        String endYear = sdf.format(yearCal.getTime());
        String yearlyRange = startYear + " to " + endYear;
        subYearlyRangeButton.setText(yearlyRange);
        subYearlyRangeButton.setOnClickListener(v -> {
            if (subYearlyRangeButton.getText().toString().equals(yearlyRange)) {
                subYearlyRangeButton.setText("Yearly");
            } else {
                subYearlyRangeButton.setText(yearlyRange);
            }
            currentFilter = FILTER_YEARLY;
            updateFilterButtonsUI();
            Toast.makeText(this, "Yearly Expenses view coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.allButton).setOnClickListener(v -> {
            View subAll = findViewById(R.id.subExpensesContainer);
            View subDaily = findViewById(R.id.subDailyContainer);
            View subWeekly = findViewById(R.id.subWeeklyContainer);
            View subMonthly = findViewById(R.id.subMonthlyContainer);
            View subYearly = findViewById(R.id.subYearlyContainer);
            subDaily.setVisibility(View.GONE);
            subWeekly.setVisibility(View.GONE);
            subMonthly.setVisibility(View.GONE);
            subYearly.setVisibility(View.GONE);
            if (subAll.getVisibility() == View.VISIBLE) {
                subAll.setVisibility(View.GONE);
            } else {
                subAll.setVisibility(View.VISIBLE);
            }
            currentFilter = FILTER_ALL;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        findViewById(R.id.subAllButton).setOnClickListener(v -> {
            currentFilter = FILTER_ALL;
            updateFilterButtonsUI();
            refreshTransactionsList();
        });

        findViewById(R.id.cashInButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("type", Transaction.TYPE_CASH_IN);
            startActivity(intent);
        });

        findViewById(R.id.cashOutButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("type", Transaction.TYPE_CASH_OUT);
            startActivity(intent);
        });

        SearchView searchView = findViewById(R.id.expensesSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                refreshTransactionsList();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                refreshTransactionsList();
                return true;
            }
        });
        
        updateFilterButtonsUI();
    }

    private void updateFilterButtonsUI() {
        int activeColor = ContextCompat.getColor(this, R.color.light_green);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray);

        allButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == FILTER_ALL ? activeColor : inactiveColor));
        dailyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == FILTER_DAILY ? activeColor : inactiveColor));
        weeklyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == FILTER_WEEKLY ? activeColor : inactiveColor));
        monthlyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == FILTER_MONTHLY ? activeColor : inactiveColor));
        yearlyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == FILTER_YEARLY ? activeColor : inactiveColor));
    }

    private void showAccountsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_accounts, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.accountsRecyclerView);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        
        adapter = new AccountAdapter(accountList, new AccountAdapter.OnAccountClickListener() {
            @Override
            public void onAccountClick(Account account) {
                topExpensesButton.setText(account.getName());
                saveActiveAccount(account.getName());
                dialog.dismiss();
            }

            @Override
            public void onDeleteClick(Account account, int position) {
                new androidx.appcompat.app.AlertDialog.Builder(ExpensesActivity.this)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete " + account.getName() + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            accountList.remove(account);
                            adapter.updateList(accountList);
                            saveAccounts();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onEditClick(Account account, int position) {
                View editView = getLayoutInflater().inflate(R.layout.dialog_add_account, null);
                android.widget.EditText nameInput = editView.findViewById(R.id.editAccountName);
                android.widget.EditText balanceInput = editView.findViewById(R.id.editAccountBalance);
                
                nameInput.setText(account.getName());
                balanceInput.setText(String.format(Locale.getDefault(), "%.2f", account.getBalance()));
                
                new androidx.appcompat.app.AlertDialog.Builder(ExpensesActivity.this)
                        .setTitle("Edit Account")
                        .setView(editView)
                        .setPositiveButton("Save", (d, w) -> {
                            String newName = nameInput.getText().toString();
                            String balanceStr = balanceInput.getText().toString();
                            if (!newName.isEmpty()) {
                                account.setName(newName);
                                if (!balanceStr.isEmpty()) {
                                    try {
                                        account.setBalance(Double.parseDouble(balanceStr));
                                    } catch (NumberFormatException ignored) {}
                                }
                                adapter.notifyItemChanged(position);
                                if (topExpensesButton.getText().toString().equals(account.getName())) {
                                    topExpensesButton.setText(newName);
                                }
                                saveAccounts();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onListChanged() {
                saveAccounts();
            }
        });
        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.editAccountsButton).setOnClickListener(v -> {
            adapter.setEditMode(!adapter.isEditMode());
        });

        androidx.appcompat.widget.SearchView searchView = dialogView.findViewById(R.id.accountsSearchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        dialogView.findViewById(R.id.addAccountButton).setOnClickListener(v -> {
            View addView = getLayoutInflater().inflate(R.layout.dialog_add_account, null);
            android.widget.EditText nameInput = addView.findViewById(R.id.editAccountName);
            android.widget.EditText balanceInput = addView.findViewById(R.id.editAccountBalance);
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Add New Account")
                    .setView(addView)
                    .setPositiveButton("Add", (d, w) -> {
                        String name = nameInput.getText().toString();
                        String balanceStr = balanceInput.getText().toString();
                        if (!name.isEmpty()) {
                            double balance = 0.0;
                            if (!balanceStr.isEmpty()) {
                                try {
                                    balance = Double.parseDouble(balanceStr);
                                } catch (NumberFormatException ignored) {}
                            }
                            accountList.add(new Account(name, balance));
                            adapter.updateList(accountList);
                            saveAccounts();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        dialog.show();
    }

    private void showAddTransactionDialog(String type) {
        boolean isCashIn = Transaction.TYPE_CASH_IN.equals(type);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText titleInput = dialogView.findViewById(R.id.transactionTitleInput);
        EditText amountInput = dialogView.findViewById(R.id.transactionAmountInput);
        RecyclerView categoryRecyclerView = dialogView.findViewById(R.id.categoryRecyclerView);
        
        dialogTitle.setText(isCashIn ? "Add Cash In" : "Add Cash Out");
        
        List<String> categories = new ArrayList<>();
        categories.add("Other"); // Add Other at the beginning
        if (isCashIn) {
            categories.add("Salary");
            categories.add("Bonus");
            categories.add("Business");
            categories.add("Investment Income");
            categories.add("Other Income");
        } else {
            categories.add("Food");
            categories.add("Groceries");
            categories.add("Rent");
            categories.add("Bills");
            categories.add("Fuel");
            categories.add("Transport");
            categories.add("Medicine");
            categories.add("Shopping");
            categories.add("Entertainment");
            categories.add("Mobile");
            categories.add("Internet");
            categories.add("Electricity");
            categories.add("Water");
            categories.add("Education");
            categories.add("Fitness");
            categories.add("Travel");
            categories.add("Insurance");
            categories.add("EMI");
            categories.add("Taxi");
            categories.add("Car");
            categories.add("Bike");
            categories.add("Gifts");
            categories.add("Other Expense");
        }

        categoryRecyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
        categoryRecyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
                View v = getLayoutInflater().inflate(R.layout.item_category_chip, parent, false);
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                View item = holder.itemView;
                TextView icon = item.findViewById(R.id.categoryIconText);
                TextView name = item.findViewById(R.id.categoryName);
                
                String cat = categories.get(position);
                icon.setText(getCategoryEmoji(cat));
                name.setText(cat);
                
                item.setOnClickListener(v -> {
                    if ("Other".equals(cat)) {
                        android.widget.EditText customInput = new android.widget.EditText(ExpensesActivity.this);
                        customInput.setHint("Enter Name");
                        new androidx.appcompat.app.AlertDialog.Builder(ExpensesActivity.this)
                                .setTitle("Custom Description")
                                .setView(customInput)
                                .setPositiveButton("OK", (d, w) -> {
                                    String customName = customInput.getText().toString().trim();
                                    if (!customName.isEmpty()) {
                                        titleInput.setText(customName);
                                        amountInput.requestFocus();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    } else {
                        titleInput.setText(cat);
                        amountInput.requestFocus();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return categories.size();
            }
        });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String title = titleInput.getText().toString().trim();
                    String amountStr = amountInput.getText().toString().trim();
                    if (title.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Please enter a title and amount", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (amount <= 0) {
                        Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String accountName = topExpensesButton.getText().toString();
                    transactionDbHelper.addTransaction(title, amount, type, System.currentTimeMillis(), accountName);
                    refreshTransactionsList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteTransaction(Transaction transaction) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Delete \"" + transaction.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    // Sync Balance back
                    double delta = transaction.isCashIn() ? -transaction.getAmount() : transaction.getAmount();
                    BalanceManager.updateAccountBalance(this, transaction.getAccount(), delta);

                    transactionDbHelper.deleteTransaction(transaction.getId());
                    refreshTransactionsList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTransactionNotePopup(Transaction transaction) {
        String note = transaction.getNotes();
        if (note == null || note.trim().isEmpty()) {
            return;
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialogTheme);
        builder.setTitle("Transaction Note");
        builder.setMessage(note);

        builder.setPositiveButton("Edit", (dialog, which) -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("transaction_id", transaction.getId());
            startActivity(intent);
        });

        builder.setNegativeButton("Delete", (dialog, which) -> confirmDeleteTransaction(transaction));

        builder.setNeutralButton("Update", (dialog, which) -> showQuickUpdateNoteDialog(transaction));

        builder.show();
    }

    private void showQuickUpdateNoteDialog(Transaction transaction) {
        final EditText input = new EditText(this);
        input.setText(transaction.getNotes());
        input.setPadding(40, 20, 40, 20);

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle("Update Note")
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newNote = input.getText().toString().trim();
                    transactionDbHelper.updateTransactionNote(transaction.getId(), newNote);
                    refreshTransactionsList();
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTransactionsList();
    }

    /**
     * Reloads transactions from the database, applies the current date filter and
     * search query, groups the results by date, and refreshes the totals footer.
     */
    private void refreshTransactionsList() {
        List<Transaction> allAscending = transactionDbHelper.getAllTransactionsAscending();

        // Running balance is computed across the FULL history (not just the filtered
        // view) so "Balance after" always reflects the true balance at that point in time.
        java.util.Map<Long, Double> balanceAfterById = new java.util.HashMap<>();
        double running = 0;
        for (Transaction t : allAscending) {
            running += t.getSignedAmount();
            balanceAfterById.put(t.getId(), running);
        }

        // Apply date filter + search, newest first
        List<Transaction> filtered = new ArrayList<>();
        Transaction monthlyIncome = null;
        for (int i = allAscending.size() - 1; i >= 0; i--) {
            Transaction t = allAscending.get(i);
            if (matchesFilter(t) && matchesSearch(t)) {
                if ("Monthly Income".equals(t.getTitle())) {
                    monthlyIncome = t;
                } else {
                    filtered.add(t);
                }
            }
        }

        // Always keep Monthly Income as the first line if it exists
        if (monthlyIncome != null) {
            filtered.add(0, monthlyIncome);
        }

        // Group by date ("Today" / "Yesterday" / actual date) and total up the totals
        List<TransactionListItem> grouped = new ArrayList<>();
        String lastGroupLabel = null;
        double cashIn = 0;
        double cashOut = 0;
        for (Transaction t : filtered) {
            String label = getDateGroupLabel(t.getTimestamp());
            if (!label.equals(lastGroupLabel)) {
                grouped.add(TransactionListItem.header(label));
                lastGroupLabel = label;
            }
            grouped.add(TransactionListItem.transaction(t, balanceAfterById.get(t.getId())));
            if (t.isCashIn()) {
                cashIn += t.getAmount();
            } else {
                cashOut += t.getAmount();
            }
        }

        transactionAdapter.updateItems(grouped);
        transactionsRecyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
        emptyStateText.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        cashInTotalText.setText(String.format(Locale.getDefault(), "%.2f", cashIn));
        cashOutTotalText.setText(String.format(Locale.getDefault(), "%.2f", cashOut));
        balanceTotalText.setText(String.format(Locale.getDefault(), "%.2f", cashIn - cashOut));

        // --- Period Balance Calculations ---
        if (currentFilter == FILTER_ALL) {
            previousBalanceRow.setVisibility(View.GONE);
            finalBalanceRow.setVisibility(View.GONE);
        } else {
            long periodStart = getPeriodStartMillis();
            double prevBalance = 0;
            double totalBalance = 0;
            for (Transaction t : allAscending) {
                totalBalance += t.getSignedAmount();
                if (t.getTimestamp() < periodStart) {
                    prevBalance += t.getSignedAmount();
                }
            }
            previousBalanceRow.setVisibility(View.VISIBLE);
            finalBalanceRow.setVisibility(View.VISIBLE);
            previousBalanceTotalText.setText(String.format(Locale.getDefault(), "%.2f", prevBalance));
            finalBalanceTotalText.setText(String.format(Locale.getDefault(), "%.2f", totalBalance));
        }
    }

    private boolean matchesFilter(Transaction t) {
        if (currentFilter == FILTER_ALL) return true;

        Calendar now = Calendar.getInstance();
        Calendar txCal = Calendar.getInstance();
        txCal.setTimeInMillis(t.getTimestamp());

        switch (currentFilter) {
            case FILTER_DAILY:
                return isSameDay(now, txCal);
            case FILTER_WEEKLY:
                return isSameWeek(now, txCal);
            case FILTER_MONTHLY:
                return now.get(Calendar.YEAR) == txCal.get(Calendar.YEAR)
                        && now.get(Calendar.MONTH) == txCal.get(Calendar.MONTH);
            case FILTER_YEARLY:
                return now.get(Calendar.YEAR) == txCal.get(Calendar.YEAR);
            default:
                return true;
        }
    }

    private boolean matchesSearch(Transaction t) {
        if (currentSearchQuery == null || currentSearchQuery.trim().isEmpty()) return true;
        return t.getTitle().toLowerCase(Locale.getDefault())
                .contains(currentSearchQuery.trim().toLowerCase(Locale.getDefault()));
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar a, Calendar b) {
        Calendar startOfWeek = (Calendar) a.clone();
        startOfWeek.setFirstDayOfWeek(Calendar.MONDAY);
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
        startOfWeek.set(Calendar.MINUTE, 0);
        startOfWeek.set(Calendar.SECOND, 0);
        startOfWeek.set(Calendar.MILLISECOND, 0);

        Calendar endOfWeek = (Calendar) startOfWeek.clone();
        endOfWeek.add(Calendar.DATE, 7);

        long time = b.getTimeInMillis();
        return time >= startOfWeek.getTimeInMillis() && time < endOfWeek.getTimeInMillis();
    }

    private String getDateGroupLabel(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        Calendar txCal = Calendar.getInstance();
        txCal.setTimeInMillis(timestamp);

        if (isSameDay(today, txCal)) {
            return "Today";
        } else if (isSameDay(yesterday, txCal)) {
            return "Yesterday";
        } else {
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(txCal.getTime());
        }
    }

    private void saveAccounts() {
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
            getSharedPreferences("ExpensesPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("AccountList", array.toString())
                    .apply();
            
            // Aggregated Monthly Income
            transactionDbHelper.addOrUpdateMonthlyIncome(totalBalance);
            refreshTransactionsList();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAccounts() {
        try {
            String json = getSharedPreferences("ExpensesPrefs", MODE_PRIVATE)
                    .getString("AccountList", null);
            if (json != null) {
                JSONArray array = new JSONArray(json);
                accountList.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    accountList.add(new Account(obj.getString("name"), obj.getDouble("balance")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getPeriodStartMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        switch (currentFilter) {
            case FILTER_DAILY:
                // Today 00:00:00
                break;
            case FILTER_WEEKLY:
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case FILTER_MONTHLY:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case FILTER_YEARLY:
                cal.set(Calendar.DAY_OF_YEAR, 1);
                break;
        }
        return cal.getTimeInMillis();
    }

    private void saveActiveAccount(String name) {
        getSharedPreferences("ExpensesPrefs", MODE_PRIVATE)
                .edit()
                .putString("ActiveAccount", name)
                .apply();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening for Expenses command...");
        try {
            voiceRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
            isVoiceCommandMode = false;
        }
    }

    private void processVoiceCommand(String command) {
        String cmd = command.toLowerCase().trim();
        Toast.makeText(this, "Command: " + command, Toast.LENGTH_SHORT).show();

        if (cmd.contains("cash in") || cmd.contains("add income")) {
            findViewById(R.id.cashInButton).performClick();
        } else if (cmd.contains("cash out") || cmd.contains("add expense")) {
            findViewById(R.id.cashOutButton).performClick();
        } else if (cmd.contains("category") || cmd.contains("categories")) {
            startActivity(new Intent(this, CategoryActivity.class));
        } else if (cmd.contains("account")) {
            showAccountsDialog();
        } else if (cmd.contains("transfer")) {
            startActivity(new Intent(this, TransferActivity.class));
        } else if (cmd.contains("back") || cmd.contains("calendar")) {
            finish();
        } else if (cmd.contains("all")) {
            findViewById(R.id.allButton).performClick();
        } else if (cmd.contains("today") || cmd.contains("daily")) {
            findViewById(R.id.dailyButton).performClick();
        } else if (cmd.contains("weekly")) {
            findViewById(R.id.weeklyButton).performClick();
        } else if (cmd.contains("monthly")) {
            findViewById(R.id.monthlyButton).performClick();
        } else if (cmd.contains("print")) {
            Toast.makeText(this, "Opening Print options", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Command not recognized: " + command, Toast.LENGTH_LONG).show();
        }
    }

    private String getCategoryEmoji(String categoryName) {
        switch (categoryName) {
            case "Other": return "➕";
            case "Salary": return "💰";
            case "Bonus": return "🎁";
            case "Business": return "💼";
            case "Investment Income": return "📈";
            case "Other Income": return "💵";
            case "Food": return "🍔";
            case "Groceries": return "🛒";
            case "Rent": return "🏠";
            case "Bills": return "🧾";
            case "Fuel": return "⛽";
            case "Transport": return "🚌";
            case "Medicine": return "💊";
            case "Shopping": return "🛍️";
            case "Entertainment": return "🎬";
            case "Mobile": return "📱";
            case "Internet": return "🌐";
            case "Electricity": return "💡";
            case "Water": return "💧";
            case "Education": return "📚";
            case "Fitness": return "🏋️";
            case "Travel": return "✈️";
            case "Insurance": return "🛡️";
            case "EMI": return "💳";
            case "Taxi": return "🚕";
            case "Car": return "🚗";
            case "Bike": return "🏍️";
            case "Gifts": return "🎁";
            case "Other Expense": return "💸";
            default: return "📝";
        }
    }
}
