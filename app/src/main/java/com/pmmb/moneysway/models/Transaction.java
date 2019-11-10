package com.pmmb.moneysway.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Transaction {

    public String id;
    public String timestamp;
    public String type;
    public String amount;
    public String description;
    public String memo;
    public String category;
    public String payment_method;

    public Transaction() {
        // Default constructor required for calls to DataSnapshot.getValue(Transaction.class)
    }

    public Transaction(int reqId) {
        // Default constructor for alert dialog
        this.type = "income";
        this.amount = "";
        this.description = "";
        this.memo = "";
        this.category = "Category : Select a category";
        this.payment_method = "Payment Method : Select a payment method";
    }

    public Transaction(String type, String amount, String description, String memo, String category, String payment_method) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.memo = memo;
        this.category = category;
        this.payment_method = payment_method;
    }

    public Transaction(String id, String timestamp, String type, String amount, String description, String memo, String category, String payment_method) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.memo = memo;
        this.category = category;
        this.payment_method = payment_method;
    }

    public String getId() {
        return id;
    }

    public void setId(String timestamp) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }
}