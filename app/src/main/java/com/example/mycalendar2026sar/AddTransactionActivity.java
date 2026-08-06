package com.example.mycalendar2026sar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.View;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView titleView, txtDate, txtTime;
    private Button btnCashIn, btnCashOut, btnSaveExit, btnSaveContinue, btnDelete;
    private EditText editAmount, editItems, editNotes;
    private ImageView btnCalculator, btnVoice, btnSelectCategory;
    
    private String currentType = Transaction.TYPE_CASH_IN;
    private long editTransactionId = -1;
    private double originalAmount = 0;
    private String originalType = "";
    private String originalAccount = "";

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

    private final ActivityResultLauncher<Intent> voiceRecognitionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        String existingText = editNotes.getText().toString();
                        editNotes.setText(existingText.isEmpty() ? spokenText : existingText + " " + spokenText);
                    }
                }
            });

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
        btnDelete = findViewById(R.id.btnDelete);
        btnSaveExit = findViewById(R.id.btnSaveExit);
        btnSaveContinue = findViewById(R.id.btnSaveContinue);

        editTransactionId = getIntent().getLongExtra("transaction_id", -1);
        if (editTransactionId != -1) {
            setupEditMode();
        } else {
            String initialType = getIntent().getStringExtra("type");
            if (Transaction.TYPE_CASH_OUT.equals(initialType)) {
                setMode(Transaction.TYPE_CASH_OUT);
            } else {
                setMode(Transaction.TYPE_CASH_IN);
            }
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

        btnVoice.setOnClickListener(v -> startVoiceRecognition());

        findViewById(R.id.btnSelectCategory).setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryActivity.class);
            intent.putExtra("selection_mode", true);
            intent.putExtra("is_expense", Transaction.TYPE_CASH_OUT.equals(currentType));
            categoryLauncher.launch(intent);
        });

        findViewById(R.id.btnAddBills).setOnClickListener(v -> showBillsOptions());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete", (d, w) -> {
                        performDelete();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

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

    private void setupEditMode() {
        Transaction t = TransactionDbHelper.getInstance(this).getTransactionById(editTransactionId);
        if (t != null) {
            setMode(t.getType());
            editAmount.setText(String.format(Locale.getDefault(), "%.2f", t.getAmount()));
            editItems.setText(t.getTitle());
            editNotes.setText(t.getNotes());
            selectedDateTime.setTimeInMillis(t.getTimestamp());
            updateDateTimeLabels();

            btnDelete.setVisibility(View.VISIBLE);
            btnSaveContinue.setVisibility(View.GONE); // Usually not needed in edit mode

            // Store original values for balance correction
            originalAmount = t.getAmount();
            originalType = t.getType();
            originalAccount = t.getAccount();
        }
    }

    private void performDelete() {
        // 1. Reverse balance
        double delta = originalType.equals(Transaction.TYPE_CASH_IN) ? -originalAmount : originalAmount;
        BalanceManager.updateAccountBalance(this, originalAccount, delta);

        // 2. Delete from DB
        TransactionDbHelper.getInstance(this).deleteTransaction(editTransactionId);
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

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...");
        try {
            voiceRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBillsOptions() {
        String[] options = {"Camera", "Gallery", "PDF"};
        int[] icons = {
                R.drawable.ic_notif_camera_color,
                R.drawable.ic_notif_gallery_color,
                R.drawable.ic_pdf_logo
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
                icon.setImageTintList(null); // Remove default tinting
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
        
        String type = currentType;
        
        if (editTransactionId != -1) {
            // EDIT MODE logic
            
            // 1. Reverse original balance
            double reverseDelta = originalType.equals(Transaction.TYPE_CASH_IN) ? -originalAmount : originalAmount;
            BalanceManager.updateAccountBalance(this, originalAccount, reverseDelta);
            
            // 2. Update DB record
            TransactionDbHelper.getInstance(this).updateTransaction(
                    editTransactionId,
                    itemTitle,
                    amount,
                    type,
                    selectedDateTime.getTimeInMillis(),
                    account,
                    notes,
                    "", // voice path
                    ""  // bills
            );
        } else {
            // NEW RECORD logic
            TransactionDbHelper.getInstance(this).addTransaction(
                    itemTitle,
                    amount,
                    type,
                    selectedDateTime.getTimeInMillis(),
                    account,
                    notes,
                    "", // voice path placeholder
                    ""  // bills placeholder
            );
        }

        // Apply NEW balance (Shared logic for both new/edit)

        // 1. Sync with Account Balance
        double delta = type.equals(Transaction.TYPE_CASH_IN) ? amount : -amount;
        BalanceManager.updateAccountBalance(this, account, delta);

        return true;
    }
}
