package com.example.foodorderapp;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorderapp.model.Food;
import com.example.foodorderapp.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

//ManageActivity dùng để quản lý danh sách các món ăn
//Quản lý các chức năng (thêm, xóa, sửa)
//Quản lý chi tiết khi thêm một món mới và các điều kiện ràng buộc, trạng thái
//lọc món ăn/uống

public class ManageActivity extends AppCompatActivity {

    private RecyclerView rvManage;
    private ManageFoodAdapter manageAdapter;
    private EditText etSearchManage;
    private String[] categories = {"Món chính", "Bánh mì", "Lẩu", "Đồ uống", "Tráng miệng", "Khác"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        MaterialToolbar toolbar = findViewById(R.id.toolbarManage);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý món ăn");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etSearchManage = findViewById(R.id.etSearchManage);
        rvManage = findViewById(R.id.rvManage);
        rvManage.setLayoutManager(new LinearLayoutManager(this));

        setupDataManager();
        loadFoods("");

        etSearchManage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { loadFoods(s.toString()); }
        });

        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAddFood);
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void setupDataManager() {
        DataManager.getInstance().setDataChangedListener(new DataManager.OnDataChangedListener() {
            @Override
            public void onFoodDataChanged() {
                runOnUiThread(() -> loadFoods(etSearchManage.getText().toString()));
            }

            @Override
            public void onCartDataChanged() {}
        });
    }

    private void loadFoods(String query) {
        List<Food> foods = query.isEmpty()
                ? DataManager.getInstance().getFoodList()
                : DataManager.getInstance().searchFood(query);
        manageAdapter = new ManageFoodAdapter(this, foods,
                food -> showAddEditDialog(food),
                food -> confirmDelete(food));
        rvManage.setAdapter(manageAdapter);
    }

    private void showAddEditDialog(Food existingFood) {
        boolean isEdit = existingFood != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogStyle);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_food, null);
        builder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etDlgName);
        TextInputEditText etDescription = view.findViewById(R.id.etDlgDescription);
        TextInputEditText etPrice = view.findViewById(R.id.etDlgPrice);
        TextInputEditText etImageUrl = view.findViewById(R.id.etDlgImageUrl);
        TextInputEditText etPrepTime = view.findViewById(R.id.etDlgPrepTime);
        TextInputEditText etRating = view.findViewById(R.id.etDlgRating);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        SwitchMaterial switchAvailable = view.findViewById(R.id.switchAvailable);
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);

        tvDialogTitle.setText(isEdit ? "✏️ Sửa món ăn" : "➕ Thêm món ăn mới");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        if (isEdit) {
            etName.setText(existingFood.getName());
            etDescription.setText(existingFood.getDescription());
            etPrice.setText(String.valueOf((int) existingFood.getPrice()));
            etImageUrl.setText(existingFood.getImageUrl());
            etPrepTime.setText(String.valueOf(existingFood.getPrepTimeMinutes()));
            etRating.setText(String.valueOf(existingFood.getRating()));
            switchAvailable.setChecked(existingFood.isAvailable());
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(existingFood.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        } else {
            switchAvailable.setChecked(true);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        view.findViewById(R.id.btnDlgCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnDlgSave).setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
            String imgUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";
            String prepStr = etPrepTime.getText() != null ? etPrepTime.getText().toString().trim() : "15";
            String ratingStr = etRating.getText() != null ? etRating.getText().toString().trim() : "4.5";
            String category = categories[spinnerCategory.getSelectedItemPosition()];

            if (name.isEmpty()) { etName.setError("Bắt buộc"); return; }
            if (priceStr.isEmpty()) { etPrice.setError("Bắt buộc"); return; }

            double price;
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) { etPrice.setError("Giá không hợp lệ"); return; }

            int prepTime = prepStr.isEmpty() ? 15 : Integer.parseInt(prepStr);
            float rating;
            try { rating = ratingStr.isEmpty() ? 4.5f : Float.parseFloat(ratingStr); }
            catch (NumberFormatException e) { rating = 4.5f; }

            Food food = isEdit ? existingFood : new Food();
            food.setName(name);
            food.setDescription(desc);
            food.setPrice(price);
            food.setCategory(category);
            food.setImageUrl(imgUrl);
            food.setPrepTimeMinutes(prepTime);
            food.setRating(Math.min(5.0f, Math.max(0f, rating)));
            food.setAvailable(switchAvailable.isChecked());

            if (isEdit) {
                DataManager.getInstance().updateFood(food);
                Toast.makeText(this, "✓ Đã cập nhật " + name, Toast.LENGTH_SHORT).show();
            } else {
                DataManager.getInstance().addFood(food);
                Toast.makeText(this, "✓ Đã thêm " + name, Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
            loadFoods(etSearchManage.getText().toString());
        });
    }

    private void confirmDelete(Food food) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa món ăn")
            .setMessage("Bạn có chắc muốn xóa \"" + food.getName() + "\"?")
            .setPositiveButton("Xóa", (d, w) -> {
                DataManager.getInstance().deleteFood(food.getId());
                Toast.makeText(this, "✓ Đã xóa " + food.getName(), Toast.LENGTH_SHORT).show();
                loadFoods(etSearchManage.getText().toString());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    // Inner Adapter for Manage screen
    static class ManageFoodAdapter extends RecyclerView.Adapter<ManageFoodAdapter.VH> {
        private final Context ctx;
        private final List<Food> foods;
        private final java.util.function.Consumer<Food> onEdit;
        private final java.util.function.Consumer<Food> onDelete;

        ManageFoodAdapter(Context ctx, List<Food> foods,
                          java.util.function.Consumer<Food> onEdit,
                          java.util.function.Consumer<Food> onDelete) {
            this.ctx = ctx; this.foods = foods;
            this.onEdit = onEdit; this.onDelete = onDelete;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.item_manage_food, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Food f = foods.get(pos);
            h.tvName.setText(f.getName());
            h.tvPrice.setText(f.getFormattedPrice());
            h.tvCategory.setText(f.getCategory());
            h.tvStatus.setText(f.isAvailable() ? "Còn hàng" : "Hết hàng");
            h.tvStatus.setTextColor(ctx.getColor(
                    f.isAvailable() ? R.color.success_green : R.color.error_red));
            Glide.with(ctx).load(f.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .centerCrop()
                    .into(h.ivThumb);
            h.btnEdit.setOnClickListener(v -> onEdit.accept(f));
            h.btnDelete.setOnClickListener(v -> onDelete.accept(f));
        }

        @Override public int getItemCount() { return foods.size(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            TextView tvName, tvPrice, tvCategory, tvStatus;
            View btnEdit, btnDelete;
            VH(@NonNull View v) {
                super(v);
                ivThumb = v.findViewById(R.id.ivManageThumb);
                tvName = v.findViewById(R.id.tvManageName);
                tvPrice = v.findViewById(R.id.tvManagePrice);
                tvCategory = v.findViewById(R.id.tvManageCategory);
                tvStatus = v.findViewById(R.id.tvManageStatus);
                btnEdit = v.findViewById(R.id.btnEditFood);
                btnDelete = v.findViewById(R.id.btnDeleteFood);
            }
        }
    }
}
