package com.example.mycalendar2026sar;

/**
 * Represents a single Cash In / Cash Out transaction.
 */
public class Transaction {

    public static final String TYPE_CASH_IN = "IN";
    public static final String TYPE_CASH_OUT = "OUT";

    private long id;
    private String title;
    private double amount;
    private String type;      // TYPE_CASH_IN or TYPE_CASH_OUT
    private long timestamp;   // millis since epoch
    private String account;   // account name this transaction belongs to
    private String notes;
    private String voiceNotePath;
    private String billAttachments; // JSON string of paths

    public Transaction(long id, String title, double amount, String type, long timestamp, String account) {
        this(id, title, amount, type, timestamp, account, "", "", "");
    }

    public Transaction(long id, String title, double amount, String type, long timestamp, String account, String notes, String voiceNotePath, String billAttachments) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.account = account;
        this.notes = notes;
        this.voiceNotePath = voiceNotePath;
        this.billAttachments = billAttachments;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public boolean isCashIn() {
        return TYPE_CASH_IN.equals(type);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAccount() {
        return account;
    }

    public String getNotes() {
        return notes;
    }

    public String getVoiceNotePath() {
        return voiceNotePath;
    }

    public String getBillAttachments() {
        return billAttachments;
    }

    /** Signed amount: positive for Cash In, negative for Cash Out. */
    public double getSignedAmount() {
        return isCashIn() ? amount : -amount;
    }
}
