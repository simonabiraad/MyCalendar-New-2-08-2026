package com.example.mycalendar2026sar;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTransactionLongClickListener {
        void onLongClick(Transaction transaction);
    }

    private List<TransactionListItem> items = new ArrayList<>();
    private final OnTransactionLongClickListener longClickListener;

    public TransactionAdapter(OnTransactionLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void updateItems(List<TransactionListItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TransactionListItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_transaction_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_transaction_row, parent, false);
            return new RowViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TransactionListItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerText.setText(item.getHeaderText());
        } else if (holder instanceof RowViewHolder) {
            RowViewHolder rowHolder = (RowViewHolder) holder;
            Transaction transaction = item.getTransaction();

            rowHolder.title.setText(transaction.getTitle());
            rowHolder.time.setText(DateFormat.format("hh:mm a", transaction.getTimestamp()));

            String formattedAmount = String.format(Locale.getDefault(), "%.2f", transaction.getAmount());

            if (transaction.isCashIn()) {
                rowHolder.cashIn.setText(formattedAmount);
                rowHolder.cashOut.setText("");
            } else {
                rowHolder.cashOut.setText(formattedAmount);
                rowHolder.cashIn.setText("");
            }

            rowHolder.balance.setText(String.format(Locale.getDefault(), "%.2f", item.getBalanceAfter()));

            rowHolder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLongClick(transaction);
                }
                return true;
            });
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerTitle);
        }
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        TextView cashIn;
        TextView cashOut;
        TextView balance;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.transactionTitle);
            time = itemView.findViewById(R.id.transactionTime);
            cashIn = itemView.findViewById(R.id.transactionCashIn);
            cashOut = itemView.findViewById(R.id.transactionCashOut);
            balance = itemView.findViewById(R.id.transactionBalance);
        }
    }
}
