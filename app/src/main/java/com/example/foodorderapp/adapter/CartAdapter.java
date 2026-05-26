package com.example.foodorderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.model.CartItem;
import java.util.List;

//CartAdapter dùng để hiển thị danh sách các món ăn trong giỏ hàng
public class  CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onQuantityChanged(CartItem item, int newQty);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateList(List<CartItem> newList) {
        this.cartItems = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getFood().getName());
        holder.tvPrice.setText(item.getFood().getFormattedPrice());
        holder.tvTotal.setText(item.getFormattedTotalPrice());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        if (item.getNote() != null && !item.getNote().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText("Ghi chú: " + item.getNote());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(item.getFood().getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .centerCrop()
                .into(holder.ivFood);

        holder.btnIncrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            if (listener != null) listener.onQuantityChanged(item, newQty);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            if (newQty <= 0) {
                if (listener != null) listener.onItemRemoved(item);
            } else {
                if (listener != null) listener.onQuantityChanged(item, newQty);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onItemRemoved(item);
        });
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvName, tvPrice, tvTotal, tvQuantity, tvNote;
        View btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.ivCartFood);
            tvName = itemView.findViewById(R.id.tvCartFoodName);
            tvPrice = itemView.findViewById(R.id.tvCartFoodPrice);
            tvTotal = itemView.findViewById(R.id.tvCartItemTotal);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvNote = itemView.findViewById(R.id.tvCartNote);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }
}
