package com.sssameeri.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView timeTxt;
    private Button startBtn;
    private SeekBar timeSeekBar;
    private boolean isPressed;
    private int defaultInterval;
    private CountDownTimer myTimer;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        timeTxt = findViewById(R.id.timeTxt);
        startBtn = findViewById(R.id.startBtn);
        timeSeekBar = findViewById(R.id.timerSeekBar);
        timeSeekBar.setMax(600);
        setIntervalFromSharedPreferences(sharedPreferences);

        isPressed = false;

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setTimeTxt(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    private void setTimeTxt(int progress) {

        int minutes = progress / 60;
        int seconds = progress - (minutes * 60);

        String minutesString = "", secondsString = "";

        if (minutes < 10)
            minutesString = "0" + minutes;
        else
            minutesString = String.valueOf(minutes);

        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = String.valueOf(seconds);

        timeTxt.setText(minutesString + ":" + secondsString);
    }

    public void startTimer(View view) {

        if (!isPressed) {
            startBtn.setText(R.string.stop);
            timeSeekBar.setEnabled(false);

            myTimer = new CountDownTimer(timeSeekBar.getProgress() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    setTimeTxt((int) millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {

                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    if(sharedPreferences.getBoolean("enable_sound", true)) {

                        String melodyName = sharedPreferences.getString("timer_melody", "bell");
                        MediaPlayer mediaPlayer;
                        switch (melodyName)
                        {
                            case "bell":
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                                mediaPlayer.start();
                                break;
                            case "alarm_siren":
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                                mediaPlayer.start();
                                break;
                            case "bip":
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                                mediaPlayer.start();
                                break;
                        }
                    }
                    whenTimerStop();
                }
            }.start();

            isPressed = true;
        } else {
            myTimer.cancel();
            whenTimerStop();
        }
    }

    private void whenTimerStop() {
        startBtn.setText(R.string.start);
        setIntervalFromSharedPreferences(sharedPreferences);
        timeSeekBar.setEnabled(true);
        isPressed = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.action_color:
                return false;
            case R.id.action_about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                return true;
        }
        //return super.onOptionsItemSelected(item);
        return true;
    }

    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences) {

            defaultInterval = Integer.valueOf(sharedPreferences.getString("timer_default_interval", "30"));
            setTimeTxt(defaultInterval);
            timeSeekBar.setProgress(defaultInterval);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("timer_default_interval"))
        {
            setIntervalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
