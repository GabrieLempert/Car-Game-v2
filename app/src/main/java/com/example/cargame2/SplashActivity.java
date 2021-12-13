package com.example.cargame2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity{
    private ImageView logo;
    private TextView textLogo;
    Animation topAnim,bottomAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        initView();
        animation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAnimation();
            }
        },3000);
    }

    public void initView(){
        logo=findViewById(R.id.Logo);
        textLogo=findViewById(R.id.textLogo);
    };




    public void animation() {
        topAnim=AnimationUtils.loadAnimation(this,R.anim.top_animation);
        topAnim.setDuration(2000);
        bottomAnim=AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        bottomAnim.setDuration(2000);

        logo.setAnimation(topAnim);
        textLogo.setAnimation(bottomAnim);

    }

    public void stopAnimation(){

        Intent intent =new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
