package com.example.foodorderapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.adapter.FoodAdapter;
import com.example.foodorderapp.model.Food;
import com.example.foodorderapp.utils.DataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

//MainActivity dùng để hiển thị danh sách các món ăn và các chức năng chính
public class MainActivity extends AppCompatActivity implements FoodAdapter.OnFoodClickListener {

    private RecyclerView rvFoods;
    private FoodAdapter foodAdapter;
    private EditText etSearch;
    private ChipGroup chipGroupCategories;
    private TextView tvEmptyState;
    private TextView tvCartBadge;
    private String currentCategory = "Tất cả";
    private TextView tvUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra đăng nhập
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        updateUserInfo(user);
        setupDataManager();
        setupChipCategories();
        setupRecyclerView();
        setupSearch();
        setupBottomNav();
    }

    private void setupDataManager() {
        DataManager.getInstance().setDataChangedListener(new DataManager.OnDataChangedListener() {
            @Override
            public void onFoodDataChanged() {
                runOnUiThread(() -> {
                    setupChipCategories();
                    loadFoods(etSearch.getText().toString().trim());
                });
            }

            @Override
            public void onCartDataChanged() {
                runOnUiThread(() -> updateCartBadge());
            }

            @Override
            public void onUserProfileChanged(com.example.foodorderapp.model.User user) {
                runOnUiThread(() -> {
                    updateUserInfo(FirebaseAuth.getInstance().getCurrentUser());
                    checkAdminAccess();
                });
            }
        });
    }

    private void checkAdminAccess() {
        View btnManage = findViewById(R.id.btnManage);
        if (btnManage != null) {
            if (DataManager.getInstance().isAdmin()) {
                btnManage.setVisibility(View.VISIBLE);
            } else {
                btnManage.setVisibility(View.GONE);
            }
        }
    }

    private void initViews() {
        rvFoods = findViewById(R.id.rvFoods);
        etSearch = findViewById(R.id.etSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        tvUserName = findViewById(R.id.tvUserName); // Need to add this ID to layout

        findViewById(R.id.btnCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));
        
        View btnManage = findViewById(R.id.btnManage);
        btnManage.setOnClickListener(v ->
                startActivity(new Intent(this, ManageActivity.class)));
        
        checkAdminAccess();
        
        // Logout on long click for now
        tvUserName.setOnLongClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        });
    }

    private void updateUserInfo(FirebaseUser user) {
        if (tvUserName != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            if (name == null || name.isEmpty()) {
                name = "Người dùng ẩn danh";
            }
            tvUserName.setText(name);
        }
    }

    private void setupChipCategories() {
        chipGroupCategories.removeAllViews();
        List<String> categories = DataManager.getInstance().getCategories();
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector));
            chip.setChecked(cat.equals(currentCategory));
            chip.setOnClickListener(v -> {
                currentCategory = cat;
                loadFoods(etSearch.getText().toString().trim());
            });
            chipGroupCategories.addView(chip);
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvFoods.setLayoutManager(layoutManager);
        List<Food> foods = DataManager.getInstance().getFoodByCategory(currentCategory);
        foodAdapter = new FoodAdapter(this, foods, this);
        rvFoods.setAdapter(foodAdapter);
        updateEmptyState(foods);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                loadFoods(s.toString().trim());
            }
        });
    }

    private void loadFoods(String query) {
        List<Food> foods;
        if (query.isEmpty()) {
            foods = DataManager.getInstance().getFoodByCategory(currentCategory);
        } else {
            foods = DataManager.getInstance().searchFood(query);
        }
        foodAdapter.updateList(foods);
        updateEmptyState(foods);
    }

    private void updateEmptyState(List<Food> foods) {
        if (foods.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvFoods.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvFoods.setVisibility(View.VISIBLE);
        }
    }

    private void updateCartBadge() {
        int count = DataManager.getInstance().getCartCount();
        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        // Bottom bar handled by buttons in this layout
    }

    @Override
    public void onFoodClick(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra("food_id", food.getId());
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Food food) {
        DataManager.getInstance().addToCart(food, 1, "");
        Toast.makeText(this, "✓ Đã thêm " + food.getName() + " vào giỏ!", Toast.LENGTH_SHORT).show();
        updateCartBadge();
        foodAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        setupChipCategories();
        loadFoods(etSearch.getText().toString().trim());
    }
}
