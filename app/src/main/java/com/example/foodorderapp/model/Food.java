package com.example.foodorderapp.model;

import java.io.Serializable;

//Food là lớp đại diện cho một món ăn trong danh sách món ăn như: tên món, giá, số lượng, ghi chú
//
public class Food implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
    private boolean isAvailable;
    private float rating;
    private int prepTimeMinutes;

    public Food() {}

    public Food(String id, String name, String description, double price,
                String category, String imageUrl, boolean isAvailable,
                float rating, int prepTimeMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
        this.rating = rating;
        this.prepTimeMinutes = prepTimeMinutes;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }

    public String getFormattedPrice() {
        return String.format("%,.0f đ", price);
    }
}
