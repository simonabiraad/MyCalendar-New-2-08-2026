package com.example.mycalendar2026sar;

/**
 * A single row inside the transactions RecyclerView: either a date-group
 * header ("Today", "Yesterday", "24-07-2026"...) or an actual transaction.
 */
public class TransactionListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TRANSACTION = 1;

    private final int type;
    private final String headerText;
    private final Transaction transaction;
    private final double balanceAfter;

    private TransactionListItem(int type, String headerText, Transaction transaction, double balanceAfter) {
        this.type = type;
        this.headerText = headerText;
        this.transaction = transaction;
        this.balanceAfter = balanceAfter;
    }

    public static TransactionListItem header(String text) {
        return new TransactionListItem(TYPE_HEADER, text, null, 0);
    }

    public static TransactionListItem transaction(Transaction transaction, double balanceAfter) {
        return new TransactionListItem(TYPE_TRANSACTION, null, transaction, balanceAfter);
    }

    public int getType() {
        return type;
    }

    public String getHeaderText() {
        return headerText;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }
}
