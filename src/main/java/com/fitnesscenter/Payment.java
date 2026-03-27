package com.fitnesscenter;

public class Payment {
    private String receiptId;
    private String memberId;
    private String amount;
    private String date;

    // Extra fields JUST for displaying in the UI (Not saved to the text file)
    private String planName;
    private int planPrice;
    private int balance;

    public Payment(String receiptId, String memberId, String amount, String date) {
        this.receiptId = receiptId;
        this.memberId = memberId;
        this.amount = amount;
        this.date = date;
    }

    // Getters for File Saving
    public String getReceiptId() { return receiptId; }
    public String getMemberId() { return memberId; }
    public String getAmount() { return amount; }
    public String getDate() { return date; }

    // Getters and Setters for the UI Table
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public int getPlanPrice() { return planPrice; }
    public void setPlanPrice(int planPrice) { this.planPrice = planPrice; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }

    // Formats data to save into the text file (Only saves the core 4 pieces of data!)
    public String toFileString() {
        return receiptId + "," + memberId + "," + amount + "," + date;
    }
}