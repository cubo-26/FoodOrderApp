package com.example.foodorderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.foodorderapp.R;
import com.example.foodorderapp.model.Food;
import com.example.foodorderapp.utils.DataManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.chip.Chip;
import java.util.List;

//FoodAdapter dùng để hiển thị danh sách các món ăn trong danh sách món ăn
public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<Food> foodList;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
        void onAddToCartClick(Food food);
    }

    public FoodAdapter(Context context, List<Food> foodList, OnFoodClickListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    public void updateList(List<Food> newList) {
        this.foodList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);

        holder.tvName.setText(food.getName());
        holder.tvPrice.setText(food.getFormattedPrice());
        holder.tvCategory.setText(food.getCategory());
        holder.tvRating.setText(String.valueOf(food.getRating()));
        holder.tvPrepTime.setText(food.getPrepTimeMinutes() + " phút");

        // Truncate description
        String desc = food.getDescription();
        holder.tvDescription.setText(desc.length() > 60 ? desc.substring(0, 60) + "..." : desc);

        // Load image
        Glide.with(context)
                .load(food.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.ivFood);

        // Availability overlay
        holder.viewUnavailable.setVisibility(food.isAvailable() ? View.GONE : View.VISIBLE);
        holder.tvUnavailable.setVisibility(food.isAvailable() ? View.GONE : View.VISIBLE);

        // Cart badge
        int cartQty = DataManager.getInstance().getCartQuantity(food.getId());
        if (cartQty > 0) {
            holder.tvCartBadge.setVisibility(View.VISIBLE);
            holder.tvCartBadge.setText(String.valueOf(cartQty));
        } else {
            holder.tvCartBadge.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) listener.onFoodClick(food);
        });

        holder.btnAddCart.setOnClickListener(v -> {
            if (food.isAvailable() && listener != null) {
                listener.onAddToCartClick(food);
            }
        });

        if (!food.isAvailable()) {
            holder.btnAddCart.setAlpha(0.4f);
            holder.btnAddCart.setEnabled(false);
        } else {
            holder.btnAddCart.setAlpha(1.0f);
            holder.btnAddCart.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() { return foodList.size(); }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivFood;
        TextView tvName, tvPrice, tvCategory, tvRating, tvPrepTime, tvDescription;
        TextView tvCartBadge, tvUnavailable;
        View viewUnavailable;
        View btnAddCart;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardFood);
            ivFood = itemView.findViewById(R.id.ivFood);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvCategory = itemView.findViewById(R.id.tvFoodCategory);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrepTime = itemView.findViewById(R.id.tvPrepTime);
            tvDescription = itemView.findViewById(R.id.tvFoodDescription);
            tvCartBadge = itemView.findViewById(R.id.tvCartBadge);
            tvUnavailable = itemView.findViewById(R.id.tvUnavailable);
            viewUnavailable = itemView.findViewById(R.id.viewUnavailable);
            btnAddCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
