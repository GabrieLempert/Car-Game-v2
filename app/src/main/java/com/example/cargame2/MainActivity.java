package com.example.cargame2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public static final String HARD_GAME = "HARD GAME";
    public static final String EASY_GAME ="EASY GAME";

    Button[] buttons;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startmenu);
        initButtons();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},101);
        }
        changeActivity();

    }


    public void initButtons(){
       buttons=new Button[]{findViewById(R.id.easy_game_button), findViewById(R.id.hard_game_button), findViewById(R.id.scores_button)};
    }

    public void changeActivity(){
        buttons[0].setOnClickListener(e->{
               EasyGame();
        });
        buttons[1].setOnClickListener(e->{
               HardGame();
        });
        buttons[2].setOnClickListener(e->{
            Scores();
        });


    }

    private void EasyGame() {
        Intent intent=new Intent(this,GameActivity.class);
        intent.putExtra(EASY_GAME,0);
        startActivity(intent);
    }
    private void HardGame(){
       Intent intent=new Intent(this,GameActivity.class);
       intent.putExtra(HARD_GAME,1);
       startActivity(intent);
    }
    public void Scores(){
        Intent intent=new Intent(this,ScoresActivity.class);
        startActivity(intent);
    }





}