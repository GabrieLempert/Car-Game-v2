package com.example.cargame2;

import static com.example.cargame2.MainActivity.HARD_GAME;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.CarrierConfigManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

public class GameActivity extends AppCompatActivity {
    private GameDB myDB;
    private ImageButton[] buttons;
    private ImageView[] cars;
    private ImageView[][] obstacles;
    private ImageView[][] points;
    private ImageView[] booms;
    private ImageView[] hearts;
    private MediaPlayer explosionSound, gameOverSound, coinSound;
    private static final int DELAY = 1000;
    private Timer timer;
    private int clock;
    private int lifeCount = 2;
    private long score = 0;
    private TextView SCORE;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener accSensorEventListener;
    private LocationManager locationManager;
    private Location location;

    public enum DirectionAction {LEFT, RIGHT}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        initViews();
        initButtons();
        String fromJSON = MSPv3.getInstance(this).getStringSP("MY_DB", "");
        myDB = new Gson().fromJson(fromJSON, GameDB.class);
        if (myDB == null)
            myDB = new GameDB();
        Intent intent = getIntent();
        int number = intent.getIntExtra(HARD_GAME, 0);
        if (number == 1) {
            initSensors(true);
            accSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float x = event.values[0];
                    if (x <= -0.8) {
                        DirectionAction action = DirectionAction.LEFT;
                        moveCarBySensors(action);
                    } else if (x >= 0.8) {
                        DirectionAction action = DirectionAction.RIGHT;
                        moveCarBySensors(action);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            IntStream.range(0, buttons.length).forEach(i -> buttons[i].setVisibility(View.GONE));

        } else {
            IntStream.range(0, buttons.length).forEach(i -> buttons[i].setVisibility(View.VISIBLE));
            initSensors(false);
            moveCar(true);
        }


    }




    private void moveCarBySensors(DirectionAction action) {

        if (action == DirectionAction.LEFT) {
            int lane = checkCar();
            int newLane = lane - 1;
            if (newLane >= 0) {
                cars[newLane].setVisibility(View.VISIBLE);
                cars[lane].setVisibility(View.INVISIBLE);
            } else {
                cars[lane].setVisibility(View.VISIBLE);
            }
            checkBooms();
            checkCoins();
        } else if (action == DirectionAction.RIGHT) {
            int lane = checkCar();
            int newLane = lane + 1;
            if (newLane <= 4) {
                cars[newLane].setVisibility(View.VISIBLE);
                cars[lane].setVisibility(View.INVISIBLE);
            } else {
                cars[lane].setVisibility(View.VISIBLE);
            }
            checkBooms();
            checkCoins();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().getIntExtra(HARD_GAME,0 )==1){
            sensorManager.registerListener(accSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(getIntent().getIntExtra(HARD_GAME,0 )==1){
            sensorManager.unregisterListener(accSensorEventListener);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
     }

    @Override
    protected void onStart() {
        super.onStart();
        startTicker();
    }



    public void moveCar(boolean get){
        if(get==true) {
            buttons[0].setOnClickListener(e -> {
                int lane = checkCar();
                int newLane = lane + 1;
                if (newLane <= 4) {
                    cars[newLane].setVisibility(View.VISIBLE);
                    cars[lane].setVisibility(View.INVISIBLE);
                } else {
                    cars[lane].setVisibility(View.VISIBLE);
                }
            });
            buttons[1].setOnClickListener(e -> {
                int lane = checkCar();
                int newLane = lane - 1;
                if (newLane >= 0) {
                    cars[newLane].setVisibility(View.VISIBLE);
                    cars[lane].setVisibility(View.INVISIBLE);
                } else {
                    cars[lane].setVisibility(View.VISIBLE);
                }
            });
        }else{
            buttons[0].setOnClickListener(e->{
            });
            buttons[1].setOnClickListener(e->{
            });

        }

    }

    public void initViews(){
        cars=new ImageView[]{findViewById(R.id.car_lane1),findViewById(R.id.car_lane2),findViewById(R.id.car_lane3),findViewById(R.id.car_lane4),findViewById(R.id.car_lane5)
        };
        gameOverSound= MediaPlayer.create(this,R.raw.lose);
        explosionSound=MediaPlayer.create(this,R.raw.car_crash);
        coinSound=MediaPlayer.create(this,R.raw.coin_crash);
        obstacles = new ImageView[][]{
                {findViewById(R.id.barrier_Lane1_0), findViewById(R.id.barrier_Lane1_1), findViewById(R.id.barrier_Lane1_2), findViewById(R.id.barrier_Lane1_3), findViewById(R.id.barrier_Lane1_4)},//Lane 1;
                {findViewById(R.id.barrier_Lane2_0), findViewById(R.id.barrier_Lane2_1), findViewById(R.id.barrier_Lane2_2), findViewById(R.id.barrier_Lane2_3), findViewById(R.id.barrier_Lane2_4)},//Lane 2;
                {findViewById(R.id.barrier_Lane3_0), findViewById(R.id.barrier_Lane3_1), findViewById(R.id.barrier_Lane3_2), findViewById(R.id.barrier_Lane3_3), findViewById(R.id.barrier_Lane3_4)},//Lane 3;
                {findViewById(R.id.barrier_Lane4_0), findViewById(R.id.barrier_Lane4_1), findViewById(R.id.barrier_Lane4_2), findViewById(R.id.barrier_Lane4_3), findViewById(R.id.barrier_Lane4_4)},//Lane 4;
                {findViewById(R.id.barrier_Lane5_0), findViewById(R.id.barrier_Lane5_1), findViewById(R.id.barrier_Lane5_2), findViewById(R.id.barrier_Lane5_3), findViewById(R.id.barrier_Lane5_4)}//Lane 5;
        };
        points = new ImageView[][]{                                                                                                               //collision
                {findViewById(R.id.coin_lane1_0),findViewById(R.id.coin_lane1_1),findViewById(R.id.coin_lane1_2),findViewById(R.id.coin_lane1_3),findViewById(R.id.coin_lane1_4)},//Lane 1
                {findViewById(R.id.coin_lane2_0),findViewById(R.id.coin_lane2_1),findViewById(R.id.coin_lane2_2),findViewById(R.id.coin_lane2_3),findViewById(R.id.coin_lane2_4)},//Lane 2
                {findViewById(R.id.coin_lane3_0),findViewById(R.id.coin_lane3_1),findViewById(R.id.coin_lane3_2),findViewById(R.id.coin_lane3_3),findViewById(R.id.coin_lane3_4)},//Lane 3
                {findViewById(R.id.coin_lane4_0),findViewById(R.id.coin_lane4_1),findViewById(R.id.coin_lane4_2),findViewById(R.id.coin_lane4_3),findViewById(R.id.coin_lane4_4)},//Lane 4
                {findViewById(R.id.coin_lane5_0),findViewById(R.id.coin_lane5_1),findViewById(R.id.coin_lane5_2),findViewById(R.id.coin_lane5_3),findViewById(R.id.coin_lane5_4)},//Lane 5
        };

        booms=new ImageView[]{
                findViewById(R.id.Nuke_1),findViewById(R.id.Nuke_2),findViewById(R.id.Nuke_3),findViewById(R.id.Nuke_4),findViewById(R.id.Nuke_5)
        };
        hearts =new ImageView[]{
                findViewById(R.id.heart1),findViewById(R.id.heart2),findViewById(R.id.heart3)
        };
        SCORE=findViewById(R.id.SCORE);

    }

    public void initButtons(){
        buttons=new ImageButton[]{
                findViewById(R.id.RIGHT_BUTTON),findViewById(R.id.LEFT_BUTTON)
        };
    }

    public int checkCar(){
        int n = 0;
        for (int i=0;i<cars.length;i++){
            if(cars[i].getVisibility()==View.VISIBLE)
                n=i;
        }
        return n;
    }
    public void hideBooms(){
        for (int i = 0; i <5 ; i++) {
            booms[i].setVisibility(View.GONE);
        }
    }

    public void startTicker() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("timeTick","Time: "+clock+"On Thread: "+Thread.currentThread().getName());
                runOnUiThread(() -> {
                    Log.d("timeTick","Time: "+clock+"On Thread: "+Thread.currentThread().getName());
                    moveObstacles();
                });
            }
        },0, DELAY);
    }

    private void moveObstacles() {
        clock++;
        score++;
        SCORE.setText(new StringBuilder().append("Score: ").append(score).toString());
        hideBooms();
        for(int i=0;i<=4;i++){
            if (obstacles[i][4].getVisibility()==View.VISIBLE) {
                obstacles[i][4].setVisibility(View.GONE);
            }
            if(points[i][4].getVisibility()==View.VISIBLE)
                points[i][4].setVisibility(View.GONE);

            for (int j = 4; j >= 0; j--) {
                if (obstacles[i][j].getVisibility() == View.VISIBLE) {
                    obstacles[i][j].setVisibility(View.GONE);
                    obstacles[i][j + 1].setVisibility(View.VISIBLE);
                }
            }
            for (int j = 4; j >=0 ; j--) {
                if(points[i][j].getVisibility()==View.VISIBLE){
                    points[i][j].setVisibility(View.GONE);
                    points[i][j+1].setVisibility(View.VISIBLE);
                }
            }
        }
        int random1=(int) ((Math.random()*5+1));
        int random2=(int) ((Math.random()*5+1));
        while(random1==random2){
            random2=(int) ((Math.random()*5+1));
        }
        if(clock%2==0){
            initBoom(random1);
        }
        if(clock%5==0)
            initCoins(random2);

        getCarBack();
    }
    public int  checkBooms() {
        if (obstacles[0][4].getVisibility() == View.VISIBLE && cars[0].getVisibility() == View.VISIBLE) {
            cars[0].setVisibility(View.GONE);
            obstacles[0][4].setVisibility(View.GONE);
            booms[0].setVisibility(View.VISIBLE);
            hearts[lifeCount--].setVisibility(View.INVISIBLE);
            toast(true);
            vibrate();
            explosionSound.start();
            return 0;
        }  if (obstacles[1][4].getVisibility() == View.VISIBLE && cars[1].getVisibility() == View.VISIBLE) {
            cars[1].setVisibility(View.GONE);
            obstacles[1][4].setVisibility(View.GONE);
            booms[1].setVisibility(View.VISIBLE);
            hearts[lifeCount--].setVisibility(View.INVISIBLE);
            toast(true);
            vibrate();
            explosionSound.start();
            return 1;
        }  if (obstacles[2][4].getVisibility() == View.VISIBLE && cars[2].getVisibility() == View.VISIBLE) {
            cars[2].setVisibility(View.GONE);
            obstacles[2][4].setVisibility(View.GONE);
            booms[2].setVisibility(View.VISIBLE);
            hearts[lifeCount--].setVisibility(View.INVISIBLE);
            toast(true);
            vibrate();
            explosionSound.start();
            return 2;
        } if (obstacles[3][4].getVisibility() == View.VISIBLE && cars[3].getVisibility() == View.VISIBLE) {
            cars[3].setVisibility(View.GONE);
            obstacles[3][4].setVisibility(View.GONE);
            booms[3].setVisibility(View.VISIBLE);
            hearts[lifeCount--].setVisibility(View.INVISIBLE);
            toast(true);
            vibrate();
            explosionSound.start();
            return 3;
        } if (obstacles[4][4].getVisibility() == View.VISIBLE&& cars[4].getVisibility() == View.VISIBLE) {
            cars[4].setVisibility(View.GONE);
            obstacles[4][4].setVisibility(View.GONE);
            booms[4].setVisibility(View.VISIBLE);
            hearts[lifeCount--].setVisibility(View.INVISIBLE);
            toast(true);
            vibrate();
            explosionSound.start();
            return 4;
        }
        return -1;
    }


    private void toast(boolean dynamite) {
        if (dynamite) {
            switch (lifeCount) {
                case 1:
                    Toast.makeText(this, "1 Life as Gone!", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(this, "ARE YOU OUT OF YOUR MIND??", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(this, "###  GAME OVER  ### ", Toast.LENGTH_SHORT).show();
                    gameOver();
                    break;
            }
        } else
            Toast.makeText(this, " EXTRA 20 POINTS! ", Toast.LENGTH_SHORT).show();
    }

    private void gameOver() {
        gameOverSound.start();
        timer.cancel();
        moveCar(false);
        Record record=new Record();
        // Ask location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            return;
        }
        // Set location
     if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)==null) {
         location = new Location(LocationManager.NETWORK_PROVIDER);
     }
        // check if current score should enter to the highscore list
        if (myDB.getRecords().size() == 0) {
            record.setScore(score).setLat(location.getLatitude()).setLon(location.getLongitude());
            myDB.getRecords().add(record);
        }
        else if (myDB.getRecords().size() <= 10) {
            record.setScore(score).setLat(location.getLatitude()).setLon(location.getLongitude());
            myDB.getRecords().add(record);
        } else if (myDB.getRecords().get(myDB.getRecords().size() - 1).getScore() < score) {
            record.setScore(score).setLat(location.getLatitude()).setLon(location.getLongitude());
            myDB.getRecords().set(myDB.getRecords().size() - 1, record);
        }
        myDB.sortRecords();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(this, ScoresActivity.class);
        Bundle bundle = new Bundle();
        String json = new Gson().toJson(myDB);
        bundle.putString("myDB", json);
        intent.putExtra("myDB", bundle);
        MSPv3.getInstance(this).putStringSP("MY_DB", json);
        startActivity(intent);
    }

    public void initBoom(int random) {
        switch (random) {
            case 1: {
                obstacles[0][0].setVisibility(View.VISIBLE);
                break;
            }
            case 2: {
                obstacles[1][0].setVisibility(View.VISIBLE);
                break;
            }
            case 3: {
                obstacles[2][0].setVisibility(View.VISIBLE);
                break;
            }
            case 4: {
                obstacles[3][0].setVisibility(View.VISIBLE);
                break;
            }
            case 5: {
                obstacles[4][0].setVisibility(View.VISIBLE);
                break;
            }
        }
    }
    public void initCoins(int random){

        switch (random) {
            case 1: {
                points[0][0].setVisibility(View.VISIBLE);
                break;
            }
            case 2: {
                points[1][0].setVisibility(View.VISIBLE);
                break;
            }
            case 3: {
                points[2][0].setVisibility(View.VISIBLE);
                break;
            }
            case 4: {
                points[3][0].setVisibility(View.VISIBLE);
                break;
            }
            case 5: {
                points[4][0].setVisibility(View.VISIBLE);
                break;
            }
        }


    }
    public int checkCoins(){
        if (points[0][4].getVisibility() == View.VISIBLE && cars[0].getVisibility() == View.VISIBLE) {
            cars[0].setVisibility(View.GONE);
            points[0][4].setVisibility(View.GONE);
            toast(false);
            vibrate();
            score=score+20;
            coinSound.start();
            return 0;
        }
        if (points[1][4].getVisibility() == View.VISIBLE && cars[1].getVisibility() == View.VISIBLE) {
            cars[1].setVisibility(View.GONE);
            points[1][4].setVisibility(View.GONE);
            toast(false);
            vibrate();
            coinSound.start();
            score=score+20;
            return 1;
        }
        if (points[2][4].getVisibility() == View.VISIBLE && cars[2].getVisibility() == View.VISIBLE) {
            cars[2].setVisibility(View.GONE);
            points[2][4].setVisibility(View.GONE);
            toast(false);
            vibrate();
            score=score+20;
            coinSound.start();
            return 2;
        }
        if (points[3][4].getVisibility() == View.VISIBLE && cars[3].getVisibility() == View.VISIBLE) {
            cars[3].setVisibility(View.GONE);
            points[3][4].setVisibility(View.GONE);
            toast(false);
            vibrate();
            score += 20;
            coinSound.start();

            return 3;
        }
        if (points[4][4].getVisibility() == View.VISIBLE&& cars[4].getVisibility() == View.VISIBLE) {
            cars[4].setVisibility(View.GONE);
            points[4][4].setVisibility(View.GONE);
            toast(false);
            vibrate();
            score=score+20;
            coinSound.start();
            return 4;
        }
        return -1;
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    public void getCarBack(){
        int i=checkBooms();
        int j=checkCoins();
        if(j>=0)
            cars[j].setVisibility(View.VISIBLE);
        if(i>=0)
            cars[i].setVisibility(View.VISIBLE);
    }

    public void initSensors(boolean game){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }







}
