package com.example.foodorderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorderapp.model.CartItem;
import com.example.foodorderapp.model.Order;
import com.example.foodorderapp.utils.DataManager;
import com.google.android.material.button.MaterialButton;

//InvoiceActivity dùng để hiển thị hóa đơn của khách hàng
//khi khách hàng thanh toán hóa đơn thì nó sẽ tìm nội dung(text) được ghi trong hóa đơn lúc đặt và hiển thị lên hóa đơn đã in
//
public class InvoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order == null) { finish(); return; }

        bindInvoiceData(order);

        MaterialButton btnBackHome = findViewById(R.id.btnBackToHome);
        btnBackHome.setOnClickListener(v -> {
            DataManager.getInstance().clearCart();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        MaterialButton btnNewOrder = findViewById(R.id.btnNewOrder);
        btnNewOrder.setOnClickListener(v -> {
            DataManager.getInstance().clearCart();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void bindInvoiceData(Order order) {
        ((TextView) findViewById(R.id.tvInvoiceOrderId)).setText("# " + order.getOrderId());
        ((TextView) findViewById(R.id.tvInvoiceTime)).setText(order.getOrderTime());
        ((TextView) findViewById(R.id.tvInvoiceStatus)).setText(order.getStatus());
        ((TextView) findViewById(R.id.tvInvoiceCustomerName)).setText(order.getCustomerName());
        ((TextView) findViewById(R.id.tvInvoicePhone)).setText(order.getCustomerPhone());
        ((TextView) findViewById(R.id.tvInvoiceAddress)).setText(order.getDeliveryAddress());
        ((TextView) findViewById(R.id.tvInvoicePayment)).setText(order.getPaymentMethod());

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            findViewById(R.id.layoutInvoiceNote).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvInvoiceNote)).setText(order.getNote());
        } else {
            findViewById(R.id.layoutInvoiceNote).setVisibility(View.GONE);
        }

        // Build items list
        LinearLayout layoutItems = findViewById(R.id.layoutInvoiceItems);
        layoutItems.removeAllViews();

        for (CartItem item : order.getItems()) {
            View itemView = getLayoutInflater().inflate(R.layout.item_invoice_row, layoutItems, false);
            ((TextView) itemView.findViewById(R.id.tvInvoiceItemName)).setText(item.getFood().getName());
            ((TextView) itemView.findViewById(R.id.tvInvoiceItemQty)).setText("x" + item.getQuantity());
            ((TextView) itemView.findViewById(R.id.tvInvoiceItemPrice)).setText(item.getFormattedTotalPrice());
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                TextView tvNote = itemView.findViewById(R.id.tvInvoiceItemNote);
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText("Ghi chú: " + item.getNote());
            }
            layoutItems.addView(itemView);
        }

        // Summary
        ((TextView) findViewById(R.id.tvInvoiceSubtotal)).setText(order.getFormattedSubtotal());
        ((TextView) findViewById(R.id.tvInvoiceDelivery)).setText(order.getFormattedDeliveryFee());
        ((TextView) findViewById(R.id.tvInvoiceTotal)).setText(order.getFormattedTotal());
        ((TextView) findViewById(R.id.tvInvoiceTotalItems)).setText(order.getTotalItems() + " món");
    }
}
