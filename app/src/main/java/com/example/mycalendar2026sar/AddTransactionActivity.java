package com.example.mycalendar2026sar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView titleView, txtDate, txtTime;
    private Button btnCashIn, btnCashOut, btnSaveExit, btnSaveContinue;
    private EditText editAmount, editItems, editNotes;
    private ImageView btnCalculator, btnVoice, btnSelectCategory;
    
    private String currentType = Transaction.TYPE_CASH_IN;
    private Calendar selectedDateTime = Calendar.getInstance();
    private SimpleDateFormat dateSdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private final ActivityResultLauncher<Intent> categoryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String category = result.getData().getStringExtra("category");
                    if (category != null) {
                        editItems.setText(category);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addTransactionMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleView = findViewById(R.id.addTransactionTitle);
        btnCashIn = findViewById(R.id.btnCashIn);
        btnCashOut = findViewById(R.id.btnCashOut);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        editAmount = findViewById(R.id.editAmount);
        editItems = findViewById(R.id.editItems);
        editNotes = findViewById(R.id.editNotes);
        btnCalculator = findViewById(R.id.btnCalculator);
        btnVoice = findViewById(R.id.btnVoice);
        btnSelectCategory = findViewById(R.id.btnSelectCategory);
        btnSaveExit = findViewById(R.id.btnSaveExit);
        btnSaveContinue = findViewById(R.id.btnSaveContinue);

        String initialType = getIntent().getStringExtra("type");
        if (Transaction.TYPE_CASH_OUT.equals(initialType)) {
            setMode(Transaction.TYPE_CASH_OUT);
        } else {
            setMode(Transaction.TYPE_CASH_IN);
        }

        updateDateTimeLabels();

        btnCashIn.setOnClickListener(v -> setMode(Transaction.TYPE_CASH_IN));
        btnCashOut.setOnClickListener(v -> setMode(Transaction.TYPE_CASH_OUT));

        findViewById(R.id.datePickerBox).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.timePickerBox).setOnClickListener(v -> showTimePicker());

        btnCalculator.setOnClickListener(v -> {
            CalculatorDialogFragment calc = CalculatorDialogFragment.newInstance(result -> editAmount.setText(result));
            calc.show(getSupportFragmentManager(), "calculator");
        });

        btnVoice.setOnClickListener(v -> Toast.makeText(this, "Voice Recording coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnSelectCategory).setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.putExtra("selection_mode", true);
            intent.putExtra("is_expense", Transaction.TYPE_CASH_OUT.equals(currentType));
            categoryLauncher.launch(intent);
        });

        findViewById(R.id.btnAddBills).setOnClickListener(v -> showBillsOptions());

        btnSaveExit.setOnClickListener(v -> {
            if (saveTransaction()) {
                finish();
            }
        });

        btnSaveContinue.setOnClickListener(v -> {
            if (saveTransaction()) {
                editAmount.setText("");
                editItems.setText("");
                editNotes.setText("");
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setMode(String type) {
        currentType = type;
        int activeColor = ContextCompat.getColor(this, R.color.light_green);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray);

        if (Transaction.TYPE_CASH_IN.equals(type)) {
            titleView.setText("Cach in");
            btnCashIn.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            btnCashOut.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            editAmount.setHint("Cash in amount");
        } else {
            titleView.setText("Cach out");
            btnCashIn.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
            btnCashOut.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            editAmount.setHint("Cash out amount");
        }
    }

    private void updateDateTimeLabels() {
        txtDate.setText(dateSdf.format(selectedDateTime.getTime()));
        txtTime.setText(timeSdf.format(selectedDateTime.getTime()));
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeLabels();
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            updateDateTimeLabels();
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true).show();
    }

    private void showBillsOptions() {
        String[] options = {"Camera", "Gallery", "PDF"};
        int[] icons = {
                android.R.drawable.ic_menu_camera,
                android.R.drawable.ic_menu_gallery,
                android.R.drawable.ic_menu_save
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Add Bills");

        android.widget.ListAdapter adapter = new android.widget.ArrayAdapter<String>(this, R.layout.dialog_item_with_icon, R.id.itemText, options) {
            @androidx.annotation.NonNull
            @Override
            public android.view.View getView(int position, android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                ImageView icon = view.findViewById(R.id.itemIcon);
                icon.setImageResource(icons[position]);
                return view;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            switch (which) {
                case 0: // Camera
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(takePicture);
                    break;
                case 1: // Gallery
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivity(pickPhoto);
                    break;
                case 2: // PDF
                    Intent pickPdf = new Intent(Intent.ACTION_GET_CONTENT);
                    pickPdf.setType("application/pdf");
                    startActivity(pickPdf);
                    break;
            }
        });
        builder.show();
    }

    private boolean saveTransaction() {
        String amountStr = editAmount.getText().toString().trim();
        String itemTitle = editItems.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();
        
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (itemTitle.isEmpty()) {
            itemTitle = currentType.equals(Transaction.TYPE_CASH_IN) ? "Cash In" : "Cash Out";
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return false;
        }

        String account = getSharedPreferences("ExpensesPrefs", MODE_PRIVATE)
                .getString("ActiveAccount", "Expenses");
        
        TransactionDbHelper.getInstance(this).addTransaction(
                itemTitle,
                amount,
                currentType,
                selectedDateTime.getTimeInMillis(),
                account,
                notes,
                "", // voice path placeholder
                ""  // bills placeholder
        );
        
        return true;
    }
}
