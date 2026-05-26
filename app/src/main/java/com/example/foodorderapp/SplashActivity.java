package com.example.foodorderapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

//SplashActivity dùng để hiển thị màn hình chào mừng trong vài giây
//Gọi Layout activity_splash cùng với các id của layout sau đó gắn hiệu ứng và cuối cùng là gắn sự kiện handler để chuyển hướng từ SplashActivity sang main sau khoảng 2 giây
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.ivSplashLogo);
        TextView tvAppName = findViewById(R.id.tvSplashName);
        TextView tvTagline = findViewById(R.id.tvSplashTagline);
        TextView tvTagUIS = findViewById(R.id.tvSplashTagUIS);


        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        ivLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(slideUp);
        tvTagline.startAnimation(slideUp);
        tvTagUIS.startAnimation(slideUp);

        new Handler().postDelayed(() -> {
            Intent intent;
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Log.d("SplashActivity", "User is logged in: " + user.getUid());
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                Log.d("SplashActivity", "No user logged in, going to LoginActivity");
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2200);
    }
}
