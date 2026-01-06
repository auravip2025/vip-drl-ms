package com.example.drools.model;

/**
 * Order model class representing an order fact for Drools rules.
 */
public class Order {

    private String orderId;
    private String category;
    private double amount;
    private double discount;
    private String discountReason;

    public Order() {
    }

    public Order(String orderId, String category, double amount) {
        this.orderId = orderId;
        this.category = category;
        this.amount = amount;
        this.discount = 0.0;
        this.discountReason = "No discount applied";
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getDiscountReason() {
        return discountReason;
    }

    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }

    public double getFinalAmount() {
        return amount - (amount * discount / 100);
    }

    @Override
    public String toString() {
        return String.format("Order[id=%s, category=%s, amount=%.2f, discount=%.1f%%, reason='%s', final=%.2f]",
                orderId, category, amount, discount, discountReason, getFinalAmount());
    }
}
