package com.example.foodorderapp.utils;

import android.util.Log;
import com.example.foodorderapp.model.CartItem;
import com.example.foodorderapp.model.Food;
import com.example.foodorderapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * DataManager handles data using Firebase Firestore and Authentication.
 * It provides real-time updates for food items and cart items.
 */
public class DataManager {
    private static final String TAG = "DataManager";
    private static DataManager instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final CollectionReference foodsRef;
    
    private List<Food> foodList = new ArrayList<>();
    private List<CartItem> cartItems = new ArrayList<>();
    
    private ListenerRegistration foodListener;
    private ListenerRegistration cartListener;
    private OnDataChangedListener dataChangedListener;
    private User currentUserProfile;

    public interface OnDataChangedListener {
        void onFoodDataChanged();
        void onCartDataChanged();
        default void onUserProfileChanged(User user) {}
    }

    private DataManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        foodsRef = db.collection("foods");
        
        startListening();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void signOut() {
        auth.signOut();
        foodList.clear();
        cartItems.clear();
        if (foodListener != null) foodListener.remove();
        if (cartListener != null) cartListener.remove();
    }

    public void setDataChangedListener(OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }

    private void signInAnonymously() {
        // Method kept for backward compatibility or future use, but logic moved to LoginActivity
    }

    private void startListening() {
        listenToFoods();
        listenToCart();
        listenToUserProfile();
    }

    private void listenToUserProfile() {
        FirebaseUser fbUser = auth.getCurrentUser();
        if (fbUser == null) {
            currentUserProfile = null;
            return;
        }

        db.collection("users").document(fbUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    currentUserProfile = value.toObject(User.class);
                    if (dataChangedListener != null) {
                        dataChangedListener.onUserProfileChanged(currentUserProfile);
                    }
                });
    }

    public boolean isAdmin() {
        return currentUserProfile != null && "admin".equals(currentUserProfile.getRole());
    }

    public User getCurrentUserProfile() {
        return currentUserProfile;
    }

    private void listenToFoods() {
        if (foodListener != null) foodListener.remove();
        foodListener = foodsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed.", error);
                return;
            }
            if (value != null) {
                List<Food> newList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    Food food = doc.toObject(Food.class);
                    food.setId(doc.getId());
                    newList.add(food);
                }
                foodList = newList;
                if (foodList.isEmpty()) {
                    loadSampleData();
                }
                if (dataChangedListener != null) dataChangedListener.onFoodDataChanged();
            }
        });
    }

    private void listenToCart() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        if (cartListener != null) cartListener.remove();
        cartListener = db.collection("carts").document(user.getUid())
                .collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Cart listen failed.", error);
                        return;
                    }
                    if (value != null) {
                        List<CartItem> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            newList.add(doc.toObject(CartItem.class));
                        }
                        cartItems = newList;
                        if (dataChangedListener != null) dataChangedListener.onCartDataChanged();
                    }
                });
    }

    private void loadSampleData() {
        Log.d(TAG, "Loading sample data to Firestore...");
        List<Food> samples = new ArrayList<>();
        samples.add(new Food(null, "Phở Bò Đặc Biệt", "Phở bò truyền thống với nước dùng hầm xương 12 tiếng, thịt bò tươi, rau thơm và gia vị đặc trưng.", 65000, "Món chính", "https://images.unsplash.com/photo-1555126634-323283e090fa?w=400", true, 4.8f, 15));
        samples.add(new Food(null, "Bún Bò Huế", "Bún bò Huế cay nồng với chả, giò heo, rau sống và mắm ruốc Huế chính hiệu.", 60000, "Món chính", "https://images.unsplash.com/photo-1607330289024-1535c6b4e1c1?w=400", true, 4.7f, 15));
        samples.add(new Food(null, "Cơm Tấm Sườn Bì", "Cơm tấm với sườn nướng than hoa, bì heo, trứng ốp la, mỡ hành và nước mắm pha.", 55000, "Món chính", "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=400", true, 4.6f, 20));
        samples.add(new Food(null, "Bún Chả Hà Nội", "Bún chả đặc trưng Hà Nội với chả viên nướng than, chả miếng, bún tươi, rau sống và nước chấm chua ngọt.", 58000, "Món chính", "https://images.unsplash.com/photo-1511910849309-0dffb8785146?w=400", true, 4.9f, 20));
        samples.add(new Food(null, "Mì Quảng", "Mì Quảng đặc sản miền Trung với tôm thịt, trứng cút, bánh tráng mè và rau sống tươi.", 52000, "Món chính", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400", true, 4.5f, 15));
        samples.add(new Food(null, "Bánh Mì Thịt Nướng", "Bánh mì giòn với thịt nướng thơm lừng, pate, rau thơm và tương ớt đặc biệt.", 35000, "Bánh mì", "https://images.unsplash.com/photo-1559847844-5315695dadae?w=400", true, 4.7f, 5));
        samples.add(new Food(null, "Gà Nướng Mật Ong", "Gà ta nướng mật ong giòn da, ăn kèm rau sống, dưa chuột và tương chili.", 120000, "Món chính", "https://images.unsplash.com/photo-1598514982901-f62da36d8a36?w=400", true, 4.8f, 30));
        samples.add(new Food(null, "Lẩu Thái Hải Sản", "Lẩu Thái chua cay với hải sản tươi ngon: tôm, mực, nghêu, cá và rau đa dạng.", 280000, "Lẩu", "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400", true, 4.9f, 25));
        samples.add(new Food(null, "Cà Phê Sữa Đá", "Cà phê phin đặc trưng Việt Nam, pha với sữa đặc và đá viên.", 29000, "Đồ uống", "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400", true, 4.8f, 5));
        samples.add(new Food(null, "Trà Sữa Trân Châu", "Trà sữa thơm ngon với trân châu đen dai, có thể chọn độ ngọt và đá.", 45000, "Đồ uống", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400", true, 4.6f, 5));
        samples.add(new Food(null, "Sinh Tố Bơ", "Sinh tố bơ sánh mịn, ngọt béo, bổ dưỡng với sữa đặc và đá bào.", 42000, "Đồ uống", "https://images.unsplash.com/photo-1623065422902-30a2d299bbe4?w=400", true, 4.7f, 5));
        samples.add(new Food(null, "Chè Ba Màu", "Chè ba màu với đậu xanh, đậu đỏ, thạch và nước cốt dừa béo ngậy.", 30000, "Tráng miệng", "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?w=400", true, 4.5f, 5));
        samples.add(new Food(null, "Bánh Flan Caramen", "Bánh flan mềm mịn với caramen đắng ngọt hài hòa, kèm đá bào.", 28000, "Tráng miệng", "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400", true, 4.6f, 5));

        for (Food f : samples) {
            foodsRef.add(f);
        }
    }

    // --- Food CRUD ---
    public List<Food> getFoodList() { return new ArrayList<>(foodList); }

    public List<Food> getFoodByCategory(String category) {
        if (category.equals("Tất cả")) return getFoodList();
        List<Food> result = new ArrayList<>();
        for (Food f : foodList) {
            if (f.getCategory().equals(category)) result.add(f);
        }
        return result;
    }

    public List<Food> searchFood(String query) {
        List<Food> result = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Food f : foodList) {
            if (f.getName().toLowerCase().contains(q) ||
                f.getCategory().toLowerCase().contains(q) ||
                f.getDescription().toLowerCase().contains(q)) {
                result.add(f);
            }
        }
        return result;
    }

    public void addFood(Food food) {
        foodsRef.add(food);
    }

    public void updateFood(Food updatedFood) {
        if (updatedFood.getId() != null) {
            foodsRef.document(updatedFood.getId()).set(updatedFood);
        }
    }

    public void deleteFood(String foodId) {
        foodsRef.document(foodId).delete();
        // Also remove from cart if exists
        removeFromCart(foodId);
    }

    public Food getFoodById(String id) {
        for (Food f : foodList) {
            if (f.getId().equals(id)) return f;
        }
        return null;
    }

    // --- Cart ---
    public List<CartItem> getCartItems() { return cartItems; }

    private CollectionReference getCartItemsRef() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return null;
        return db.collection("carts").document(user.getUid()).collection("items");
    }

    public void addToCart(Food food, int quantity, String note) {
        CollectionReference cartRef = getCartItemsRef();
        if (cartRef == null) {
            Log.e(TAG, "Cannot add to cart: User not logged in");
            return;
        }

        Log.d(TAG, "Adding to cart: " + food.getName() + ", qty: " + quantity);

        cartRef.document(food.getId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                CartItem existing = documentSnapshot.toObject(CartItem.class);
                if (existing != null) {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    if (note != null && !note.isEmpty()) existing.setNote(note);
                    cartRef.document(food.getId()).set(existing)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated cart item successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update cart item", e));
                }
            } else {
                cartRef.document(food.getId()).set(new CartItem(food, quantity, note))
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Added new cart item successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add cart item", e));
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error checking existing cart item", e);
        });
    }

    public void updateCartItemQuantity(String foodId, int quantity) {
        CollectionReference cartRef = getCartItemsRef();
        if (cartRef == null) return;

        if (quantity <= 0) {
            cartRef.document(foodId).delete();
        } else {
            cartRef.document(foodId).update("quantity", quantity);
        }
    }

    public void removeFromCart(String foodId) {
        CollectionReference cartRef = getCartItemsRef();
        if (cartRef != null) {
            cartRef.document(foodId).delete();
        }
    }

    public void clearCart() {
        CollectionReference cartRef = getCartItemsRef();
        if (cartRef == null) return;
        
        cartRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                doc.getReference().delete();
            }
        });
    }

    public int getCartCount() {
        int count = 0;
        for (CartItem item : cartItems) count += item.getQuantity();
        return count;
    }

    public double getCartTotal() {
        double total = 0;
        for (CartItem item : cartItems) total += item.getTotalPrice();
        return total;
    }

    public boolean isInCart(String foodId) {
        for (CartItem item : cartItems) {
            if (item.getFood().getId().equals(foodId)) return true;
        }
        return false;
    }

    public int getCartQuantity(String foodId) {
        for (CartItem item : cartItems) {
            if (item.getFood().getId().equals(foodId)) return item.getQuantity();
        }
        return 0;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Tất cả");
        for (Food f : foodList) {
            if (!categories.contains(f.getCategory())) {
                categories.add(f.getCategory());
            }
        }
        return categories;
    }
}
