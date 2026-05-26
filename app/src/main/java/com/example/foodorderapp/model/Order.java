package com.example.foodorderapp.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Oder là lớp đại diện cho một đơn hàng trong ứng dụng như: giá tiền, địa chỉ, số lượng, ghi chú
public class Order implements Serializable {
    private String orderId;
    private List<CartItem> items;
    private double subtotal;
    private double deliveryFee;
    private double discount;
    private double total;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String paymentMethod;
    private String orderTime;
    private String status;
    private String note;

    public static final double DELIVERY_FEE = 15000;

    public Order() {
        this.items = new ArrayList<>();
        this.orderTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());
        this.orderId = "ORD" + System.currentTimeMillis();
        this.status = "Đang xử lý";
    }

    public void calculateTotals() {
        this.subtotal = 0;
        for (CartItem item : items) {
            this.subtotal += item.getTotalPrice();
        }
        this.deliveryFee = DELIVERY_FEE;
        this.total = subtotal + deliveryFee - discount;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOrderTime() { return orderTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getFormattedSubtotal() { return String.format("%,.0f đ", subtotal); }
    public String getFormattedDeliveryFee() { return String.format("%,.0f đ", deliveryFee); }
    public String getFormattedDiscount() { return String.format("-%,.0f đ", discount); }
    public String getFormattedTotal() { return String.format("%,.0f đ", total); }

    public int getTotalItems() {
        int count = 0;
        for (CartItem item : items) count += item.getQuantity();
        return count;
    }
}
