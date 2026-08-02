package com.example.mycalendar2026sar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

public class CalculatorDialogFragment extends DialogFragment {

    public interface OnCalculatorResultListener {
        void onResult(String result);
    }

    private OnCalculatorResultListener listener;
    private TextView display;
    private String currentInput = "";
    private double firstOperand = Double.NaN;
    private String currentOperator = "";
    private boolean isOperatorJustPressed = false;

    public static CalculatorDialogFragment newInstance(OnCalculatorResultListener listener) {
        CalculatorDialogFragment fragment = new CalculatorDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        display = view.findViewById(R.id.calcDisplay);

        int[] digitIds = {
                R.id.btnCalc0, R.id.btnCalc1, R.id.btnCalc2, R.id.btnCalc3,
                R.id.btnCalc4, R.id.btnCalc5, R.id.btnCalc6, R.id.btnCalc7,
                R.id.btnCalc8, R.id.btnCalc9, R.id.btnCalcDot
        };

        View.OnClickListener digitListener = v -> {
            Button b = (Button) v;
            String text = b.getText().toString();
            if (isOperatorJustPressed) {
                currentInput = "";
                isOperatorJustPressed = false;
            }
            if (".".equals(text) && currentInput.contains(".")) return;
            currentInput += text;
            updateDisplay();
        };

        for (int id : digitIds) {
            view.findViewById(id).setOnClickListener(digitListener);
        }

        view.findViewById(R.id.btnCalcAdd).setOnClickListener(v -> setOperator("+"));
        view.findViewById(R.id.btnCalcSub).setOnClickListener(v -> setOperator("-"));
        view.findViewById(R.id.btnCalcMult).setOnClickListener(v -> setOperator("*"));
        view.findViewById(R.id.btnCalcDiv).setOnClickListener(v -> setOperator("/"));

        view.findViewById(R.id.btnCalcEqual).setOnClickListener(v -> calculate());
        view.findViewById(R.id.btnCalcClear).setOnClickListener(v -> clear());
        view.findViewById(R.id.btnCalcBack).setOnClickListener(v -> backspace());

        view.findViewById(R.id.btnCalcCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnCalcOk).setOnClickListener(v -> {
            if (listener != null) {
                listener.onResult(display.getText().toString());
            }
            dismiss();
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void updateDisplay() {
        if (currentInput.isEmpty()) {
            display.setText("0");
        } else {
            display.setText(currentInput);
        }
    }

    private void setOperator(String op) {
        if (!Double.isNaN(firstOperand) && !currentInput.isEmpty() && !isOperatorJustPressed) {
            calculate();
        }
        if (!currentInput.isEmpty()) {
            firstOperand = Double.parseDouble(currentInput);
        }
        currentOperator = op;
        isOperatorJustPressed = true;
    }

    private void calculate() {
        if (Double.isNaN(firstOperand) || currentInput.isEmpty() || currentOperator.isEmpty()) return;

        double secondOperand = Double.parseDouble(currentInput);
        double result = 0;

        switch (currentOperator) {
            case "+": result = firstOperand + secondOperand; break;
            case "-": result = firstOperand - secondOperand; break;
            case "*": result = firstOperand * secondOperand; break;
            case "/":
                if (secondOperand != 0) {
                    result = firstOperand / secondOperand;
                } else {
                    display.setText("Error");
                    clear();
                    return;
                }
                break;
        }

        currentInput = formatResult(result);
        updateDisplay();
        firstOperand = Double.NaN;
        currentOperator = "";
    }

    private String formatResult(double d) {
        if (d == (long) d)
            return String.format(Locale.getDefault(), "%d", (long) d);
        else
            return String.format(Locale.getDefault(), "%.2f", d);
    }

    private void clear() {
        currentInput = "";
        firstOperand = Double.NaN;
        currentOperator = "";
        isOperatorJustPressed = false;
        updateDisplay();
    }

    private void backspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            updateDisplay();
        }
    }
}
