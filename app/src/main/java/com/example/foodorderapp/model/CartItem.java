package com.example.foodorderapp.model;

import java.io.Serializable;

//CartItem là lớp đại diện cho một món ăn trong giỏ hàng
public class CartItem implements Serializable {
    private Food food;
    private int quantity;// Số lượng sản phẩm trong giỏ hàng
    private String note;//ghi chú

    public CartItem() {}

    public CartItem(Food food, int quantity) {
        this.food = food;
        this.quantity = quantity;
        this.note = "";
    }

    public CartItem(Food food, int quantity, String note) {
        this.food = food;
        this.quantity = quantity;
        this.note = note;
    }

    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public double getTotalPrice() {
        return food.getPrice() * quantity;
    }

    public String getFormattedTotalPrice() {
        return String.format("%,.0f đ", getTotalPrice());
    }
}
