package com.example.foodorderapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.adapter.CartAdapter;
import com.example.foodorderapp.model.CartItem;
import com.example.foodorderapp.model.Order;
import com.example.foodorderapp.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

//CartActivity dùng để hiển thị danh sách các món ăn trong giỏ hàng
//Chỉnh sửa những món ăn đã đặt (xóa)
//điền thông tin để đặt hàng (tên, số điện thoại, địa chỉ, ghi chú)
public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private TextView tvSubtotal, tvDelivery, tvTotal, tvItemCount, tvEmptyCart;
    private View layoutCartContent, layoutEmpty;
    private TextInputEditText etCustomerName, etCustomerPhone, etAddress, etOrderNote;
    private RadioGroup rgPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCart);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        initViews();
        setupDataManager();
        setupRecyclerView();
        setupOrderButton();
        updateSummary();
    }

    private void setupDataManager() {
        DataManager.getInstance().setDataChangedListener(new DataManager.OnDataChangedListener() {
            @Override
            public void onFoodDataChanged() {}

            @Override
            public void onCartDataChanged() {
                runOnUiThread(() -> refreshCart());
            }
        });
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvCartTotal);
        tvItemCount = findViewById(R.id.tvCartItemCount);
//        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        layoutCartContent = findViewById(R.id.layoutCartContent);
        layoutEmpty = findViewById(R.id.layoutCartEmpty);
        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        etAddress = findViewById(R.id.etDeliveryAddress);
        etOrderNote = findViewById(R.id.etOrderNote);
        rgPayment = findViewById(R.id.rgPaymentMethod);

        findViewById(R.id.btnClearCart).setOnClickListener(v -> {
            if (DataManager.getInstance().getCartItems().isEmpty()) return;
            new AlertDialog.Builder(this)
                .setTitle("Xóa giỏ hàng")
                .setMessage("Bạn có chắc muốn xóa tất cả món trong giỏ?")
                .setPositiveButton("Xóa", (d, w) -> {
                    DataManager.getInstance().clearCart();
                    refreshCart();
                    Toast.makeText(this, "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        List<CartItem> items = DataManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(this, items, this);
        rvCart.setAdapter(cartAdapter);
        updateEmptyState(items);
    }

    private void updateEmptyState(List<CartItem> items) {
        if (items.isEmpty()) {
            layoutCartContent.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutCartContent.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void updateSummary() {
        DataManager dm = DataManager.getInstance();
        double subtotal = dm.getCartTotal();
        double delivery = Order.DELIVERY_FEE;
        double total = subtotal + delivery;

        tvSubtotal.setText(String.format("%,.0f đ", subtotal));
        tvDelivery.setText(String.format("%,.0f đ", delivery));
        tvTotal.setText(String.format("%,.0f đ", total));
        tvItemCount.setText(dm.getCartCount() + " món");
    }

    private void refreshCart() {
        List<CartItem> items = DataManager.getInstance().getCartItems();
        cartAdapter.updateList(items);
        updateEmptyState(items);
        updateSummary();
    }

    private void setupOrderButton() {
        MaterialButton btnOrder = findViewById(R.id.btnPlaceOrder);
        btnOrder.setOnClickListener(v -> validateAndOrder());
    }

    private void validateAndOrder() {

        //check xem giỏ hàng có trống không nếu trống thì hiện ra "giỏ hàng trống"
        if (DataManager.getInstance().getCartItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etCustomerName.getText() != null ? etCustomerName.getText().toString().trim() : "";
        String phone = etCustomerPhone.getText() != null ? etCustomerPhone.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etCustomerName.setError("Vui lòng nhập tên");
            etCustomerName.requestFocus();
            return;
        }
        if (phone.isEmpty() || phone.length() < 10) {
            etCustomerPhone.setError("Vui lòng nhập số điện thoại hợp lệ");
            etCustomerPhone.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ giao hàng");
            etAddress.requestFocus();
            return;
        }

        // Build order
        Order order = new Order();
        order.setItems(DataManager.getInstance().getCartItems());
        order.setCustomerName(name);
        order.setCustomerPhone(phone);
        order.setDeliveryAddress(address);
        order.calculateTotals();

        // Lưu đơn hàng vào Firestore
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            order.setOrderId("ORD-" + System.currentTimeMillis()); // Đảm bảo ID duy nhất
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("orders")
                    .document(order.getOrderId())
                    .set(order)
                    .addOnSuccessListener(aVoid -> {
                        String note = etOrderNote.getText() != null ? etOrderNote.getText().toString() : "";
                        order.setNote(note);

                        int selectedPayment = rgPayment.getCheckedRadioButtonId();
                        if (selectedPayment == R.id.rbCash) {
                            order.setPaymentMethod("Tiền mặt khi nhận hàng");
                        } else if (selectedPayment == R.id.rbBankTransfer) {
                            order.setPaymentMethod("Chuyển khoản ngân hàng");
                        } else if (selectedPayment == R.id.rbMomo) {
                            order.setPaymentMethod("Ví MoMo");
                        } else {
                            order.setPaymentMethod("Tiền mặt khi nhận hàng");
                        }

                        Intent intent = new Intent(this, InvoiceActivity.class);
                        intent.putExtra("order", order);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQty) {
        DataManager.getInstance().updateCartItemQuantity(item.getFood().getId(), newQty);
        refreshCart();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        DataManager.getInstance().removeFromCart(item.getFood().getId());
        refreshCart();
        Toast.makeText(this, item.getFood().getName() + " đã bị xóa", Toast.LENGTH_SHORT).show();
    }
}
