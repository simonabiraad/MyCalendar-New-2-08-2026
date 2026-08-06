package com.example.mycalendar2026sar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button expenseToggle, incomeToggle;
    private boolean isExpenseView = true;
    private boolean isSelectionMode = false;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        isSelectionMode = getIntent().getBooleanExtra("selection_mode", false);
        isExpenseView = getIntent().getBooleanExtra("is_expense", true);

        recyclerView = findViewById(R.id.categoryRecyclerView);
        expenseToggle = findViewById(R.id.categoryExpenseToggle);
        incomeToggle = findViewById(R.id.categoryIncomeToggle);
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.categorySearchView);

        findViewById(R.id.categoryBackButton).setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateList();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filter(newText);
                return true;
            }
        });

        expenseToggle.setOnClickListener(v -> {
            if (!isExpenseView) {
                isExpenseView = true;
                updateToggleStyles();
                updateList();
            }
        });

        incomeToggle.setOnClickListener(v -> {
            if (isExpenseView) {
                isExpenseView = false;
                updateToggleStyles();
                updateList();
            }
        });
    }

    private void updateToggleStyles() {
        if (isExpenseView) {
            expenseToggle.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.light_green));
            incomeToggle.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
        } else {
            expenseToggle.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
            incomeToggle.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.light_green));
        }
    }

    private void updateList() {
        List<CategoryItem> items = new ArrayList<>();
        items.add(new CategoryItem("Other", R.drawable.ic_cat_other_color)); // Add Other at top
        if (isExpenseView) {
            addExpenseItems(items);
        } else {
            addIncomeItems(items);
        }
        adapter = new CategoryAdapter(items, item -> {
            String finalTitle = item.name;
            if ("Other".equals(item.name)) {
                android.widget.EditText nameInput = new android.widget.EditText(this);
                nameInput.setHint("Enter Name");
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Custom Category")
                        .setView(nameInput)
                        .setPositiveButton("Next", (d, w) -> {
                            String customName = nameInput.getText().toString().trim();
                            if (!customName.isEmpty()) {
                                if (isSelectionMode) {
                                    returnResult(customName);
                                } else {
                                    showAmountDialog(customName);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                if (isSelectionMode) {
                    returnResult(finalTitle);
                } else {
                    showAmountDialog(finalTitle);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void returnResult(String category) {
        Intent data = new Intent();
        data.putExtra("category", category);
        setResult(RESULT_OK, data);
        finish();
    }

    private void showAmountDialog(String title) {
        android.widget.EditText amountInput = new android.widget.EditText(this);
        amountInput.setHint("0.00");
        amountInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Enter Amount for " + title)
                .setView(amountInput)
                .setPositiveButton("Save", (d, w) -> {
                    String amountStr = amountInput.getText().toString().trim();
                    if (!amountStr.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            String account = getSharedPreferences("ExpensesPrefs", MODE_PRIVATE)
                                    .getString("ActiveAccount", "Expenses");
                            String type = isExpenseView ? Transaction.TYPE_CASH_OUT : Transaction.TYPE_CASH_IN;
                            
                            TransactionDbHelper.getInstance(this).addTransaction(
                                    title, amount, type, System.currentTimeMillis(), account);
                            
                            android.widget.Toast.makeText(this, "Saved to " + account, android.widget.Toast.LENGTH_SHORT).show();
                            finish(); // Return to all view
                        } catch (NumberFormatException ignored) {}
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addExpenseItems(List<CategoryItem> items) {
        int[] resIds = {
            R.string.cat_air_tickets, R.string.cat_auto_rickshaw, R.string.cat_bike, R.string.cat_bills,
            R.string.cat_cable_tv, R.string.cat_car, R.string.cat_car_insurance, R.string.cat_card_fee,
            R.string.cat_cigarette, R.string.cat_clothes, R.string.cat_drinks, R.string.cat_driver,
            R.string.cat_durables, R.string.cat_education, R.string.cat_electricity, R.string.cat_emi,
            R.string.cat_entertainment, R.string.cat_fast_food, R.string.cat_festivals, R.string.cat_fitness,
            R.string.cat_food, R.string.cat_fruits_vegetables, R.string.cat_fuel, R.string.cat_furniture,
            R.string.cat_gas, R.string.cat_gifts, R.string.cat_groceries, R.string.cat_health,
            R.string.cat_health_insurance, R.string.cat_hobby, R.string.cat_home_insurance,
            R.string.cat_house_hold_expenses, R.string.cat_insurance, R.string.cat_internet,
            R.string.cat_investment_expense, R.string.cat_kids, R.string.cat_laundry, R.string.cat_maid_servant,
            R.string.cat_medicine, R.string.cat_milk, R.string.cat_mobile, R.string.cat_other_expense,
            R.string.cat_parking, R.string.cat_party, R.string.cat_personal_grooming, R.string.cat_pet,
            R.string.cat_rent, R.string.cat_repair_maintenance, R.string.cat_restaurant_hotel,
            R.string.cat_savings, R.string.cat_shopping, R.string.cat_social, R.string.cat_stationery,
            R.string.cat_taxes, R.string.cat_taxi, R.string.cat_toiletries, R.string.cat_toll,
            R.string.cat_toys, R.string.cat_transportation, R.string.cat_vacation, R.string.cat_water
        };
        for (int id : resIds) {
            String name = getString(id);
            items.add(new CategoryItem(name, getCategoryLogo(name)));
        }
    }

    private void addIncomeItems(List<CategoryItem> items) {
        int[] resIds = {
            R.string.cat_salary, R.string.cat_bonus, R.string.cat_business,
            R.string.cat_investment_income, R.string.cat_other_income
        };
        for (int id : resIds) {
            String name = getString(id);
            items.add(new CategoryItem(name, getCategoryLogo(name)));
        }
    }

    private int getCategoryLogo(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("salary") || lower.contains("bonus") || lower.contains("income") || lower.contains("investment")) 
            return R.drawable.ic_cat_money_color;
        if (lower.contains("business")) return R.drawable.ic_cat_business_color;
        if (lower.contains("food") || lower.contains("fast food") || lower.contains("drinks") || lower.contains("fruits") || lower.contains("restaurant") || lower.contains("milk")) 
            return R.drawable.ic_cat_food_color;
        if (lower.contains("groceries")) return R.drawable.ic_cat_grocery_color;
        if (lower.contains("rent") || lower.contains("home") || lower.contains("house")) return R.drawable.ic_cat_rent_color;
        if (lower.contains("bills") || lower.contains("taxes") || lower.contains("toll") || lower.contains("emi") || lower.contains("card") || lower.contains("insurance")) 
            return R.drawable.ic_cat_bill_color;
        if (lower.contains("fuel") || lower.contains("gas")) return R.drawable.ic_cat_fuel_color;
        if (lower.contains("transport") || lower.contains("taxi") || lower.contains("car") || lower.contains("bike") || lower.contains("rickshaw") || lower.contains("driver")) 
            return R.drawable.ic_cat_transport_color;
        if (lower.contains("medicine") || lower.contains("health") || lower.contains("fitness")) return R.drawable.ic_cat_health_color;
        if (lower.contains("shopping") || lower.contains("clothes") || lower.contains("gifts") || lower.contains("stationery") || lower.contains("grooming") || lower.contains("pet")) 
            return R.drawable.ic_cat_shop_color;
        if (lower.contains("entertainment") || lower.contains("hobby") || lower.contains("festivals") || lower.contains("party") || lower.contains("social")) 
            return R.drawable.ic_cat_ent_color;
        if (lower.contains("mobile") || lower.contains("internet") || lower.contains("cable") || lower.contains("phone")) 
            return R.drawable.ic_cat_mobile_color;
        if (lower.contains("electricity") || lower.contains("water") || lower.contains("repair") || lower.contains("maintenance") || lower.contains("utility")) 
            return R.drawable.ic_cat_utility_color;
        if (lower.contains("education") || lower.contains("kids") || lower.contains("toys") || lower.contains("baby")) 
            return R.drawable.ic_cat_edu_color;
        if (lower.contains("travel") || lower.contains("vacation") || lower.contains("tickets") || lower.contains("hotel") || lower.contains("air")) 
            return R.drawable.ic_cat_travel_color;
        
        return R.drawable.ic_cat_other_color;
    }

    private static class CategoryItem {
        String name;
        int logoRes;

        CategoryItem(String name, int logoRes) {
            this.name = name;
            this.logoRes = logoRes;
        }
    }

    private static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private final List<CategoryItem> allItems;
        private List<CategoryItem> filteredItems;
        private final OnCategoryClickListener listener;

        interface OnCategoryClickListener {
            void onCategoryClick(CategoryItem item);
        }

        CategoryAdapter(List<CategoryItem> items, OnCategoryClickListener listener) {
            this.allItems = items;
            this.filteredItems = new ArrayList<>(items);
            this.listener = listener;
        }

        void filter(String query) {
            filteredItems = new ArrayList<>();
            if (query.isEmpty()) {
                filteredItems.addAll(allItems);
            } else {
                for (CategoryItem item : allItems) {
                    if (item.name.toLowerCase().contains(query.toLowerCase())) {
                        filteredItems.add(item);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoryItem item = filteredItems.get(position);
            holder.nameText.setText(item.name);
            holder.logoImg.setImageResource(item.logoRes);
            holder.logoImg.setImageTintList(null); // Ensure real colors are shown
            holder.itemView.setOnClickListener(v -> listener.onCategoryClick(item));
        }

        @Override
        public int getItemCount() {
            return filteredItems.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            ImageView logoImg;

            ViewHolder(View view) {
                super(view);
                nameText = view.findViewById(R.id.categoryName);
                logoImg = view.findViewById(R.id.categoryIconImg);
            }
        }
    }
}
