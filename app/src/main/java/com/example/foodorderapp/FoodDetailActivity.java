package com.example.foodorderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.foodorderapp.model.Food;
import com.example.foodorderapp.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

//FoodDetailActivity dùng để hiển thị chi tiết và trạng thái của một món ăn
//Gọi dữ liệu và bind nó vào activity_food_detail.xml như: hình ảnh, tên món, đánh giá, thời gian làm, giá cả, mô tả, text để khách hàng ghi chú
public class FoodDetailActivity extends AppCompatActivity {

    private Food food;
    private int quantity = 1;
    private TextView tvQuantity, tvTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        String foodId = getIntent().getStringExtra("food_id");
        food = DataManager.getInstance().getFoodById(foodId);

        if (food == null) { finish(); return; }

        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        bindFoodData();
        setupQuantityControls();
        setupButtons();
    }

    private void bindFoodData() {
        ImageView ivFood = findViewById(R.id.ivDetailFood);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvRating = findViewById(R.id.tvDetailRating);
        TextView tvPrepTime = findViewById(R.id.tvDetailPrepTime);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvAvailability = findViewById(R.id.tvDetailAvailability);
        tvQuantity = findViewById(R.id.tvDetailQuantity);
        tvTotalPrice = findViewById(R.id.tvDetailTotalPrice);

        Glide.with(this).load(food.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .centerCrop()
                .into(ivFood);

        tvName.setText(food.getName());
        tvPrice.setText(food.getFormattedPrice());
        tvCategory.setText(food.getCategory());
        tvRating.setText(String.valueOf(food.getRating()));
        tvPrepTime.setText("⏱ " + food.getPrepTimeMinutes() + " phút");
        tvDescription.setText(food.getDescription());

        if (food.isAvailable()) {
            tvAvailability.setText("✓ Còn hàng");
            tvAvailability.setTextColor(getColor(R.color.success_green));
        } else {
            tvAvailability.setText("✗ Hết hàng");
            tvAvailability.setTextColor(getColor(R.color.error_red));
        }

        updateTotalPrice();

        // Show current cart quantity
        int cartQty = DataManager.getInstance().getCartQuantity(food.getId());
        if (cartQty > 0) {
            quantity = cartQty;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotalPrice();
        }
    }

    private void setupQuantityControls() {
        tvQuantity = findViewById(R.id.tvDetailQuantity);
        tvQuantity.setText(String.valueOf(quantity));

        findViewById(R.id.btnDetailIncrease).setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        findViewById(R.id.btnDetailDecrease).setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });
    }

    private void updateTotalPrice() {
        double total = food.getPrice() * quantity;
        tvTotalPrice = findViewById(R.id.tvDetailTotalPrice);
        if (tvTotalPrice != null)
            tvTotalPrice.setText(String.format("%,.0f đ", total));
    }

    private void setupButtons() {
        MaterialButton btnAddToCart = findViewById(R.id.btnDetailAddToCart);
        MaterialButton btnOrderNow = findViewById(R.id.btnDetailOrderNow);

        if (!food.isAvailable()) {
            btnAddToCart.setEnabled(false);
            btnOrderNow.setEnabled(false);
            btnAddToCart.setAlpha(0.5f);
            btnOrderNow.setAlpha(0.5f);
        }

        btnAddToCart.setOnClickListener(v -> {
            TextInputEditText etNote = findViewById(R.id.etDetailNote);
            String note = etNote.getText() != null ? etNote.getText().toString() : "";
            DataManager.getInstance().addToCart(food, quantity, note);
            Toast.makeText(this, "✓ Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            // Animate button
            btnAddToCart.setIconResource(R.drawable.ic_check);
            btnAddToCart.postDelayed(() ->
                btnAddToCart.setIconResource(R.drawable.ic_cart), 1500);
        });

        btnOrderNow.setOnClickListener(v -> {
            TextInputEditText etNote = findViewById(R.id.etDetailNote);
            String note = etNote.getText() != null ? etNote.getText().toString() : "";
            DataManager.getInstance().addToCart(food, quantity, note);
            startActivity(new Intent(this, CartActivity.class));
        });
    }
}
