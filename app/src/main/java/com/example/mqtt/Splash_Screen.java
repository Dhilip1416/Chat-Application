package com.example.mqtt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class Splash_Screen extends AppCompatActivity {
    private  static final int SPLASH_SCREEN =3000;
    LottieAnimationView lottieAnimationView;
    Animation bottom, top;
    TextView Mqtt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        Mqtt = findViewById(R.id.textView);
        lottieAnimationView = findViewById(R.id.imageView);

        bottom = AnimationUtils.loadAnimation(this, R.anim.bottom);
        top  =  AnimationUtils.loadAnimation(this, R.anim.top);

        Mqtt.setAnimation(bottom);
        lottieAnimationView.setAnimation(top);


        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash_Screen.this, MainActivity.class);
            startActivity(intent);
            finish();
        },SPLASH_SCREEN);
    }
}
