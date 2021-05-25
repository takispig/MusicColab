package com.example.musiccolab;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.security.Provider;

public class LightsensorTestActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
  TextView light;
  TextView note;
  SensorManager sensorManager;
  Sensor sensor;
  MediaPlayer c,d,e,f,g,a,h,c2;
  float current=0;
  float max=0;
  String toServer="0 Theremin";



  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.lightsensor);
    light = (TextView) findViewById(R.id.sensor);
    note = (TextView) findViewById(R.id.note);
    sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
    sensor= sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    Button cal = (Button) findViewById(R.id.cal);
    cal.setOnClickListener(this);
    c =MediaPlayer.create(this,R.raw.c);
    d =MediaPlayer.create(this,R.raw.d);
    e =MediaPlayer.create(this,R.raw.e);
    f =MediaPlayer.create(this,R.raw.f);
    g =MediaPlayer.create(this,R.raw.g);
    a =MediaPlayer.create(this,R.raw.a);
    h =MediaPlayer.create(this,R.raw.h);
    c2 =MediaPlayer.create(this,R.raw.c2);
  }

  @Override
  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if(event.sensor.getType()==Sensor.TYPE_LIGHT){
      current=event.values[0];
      if(max!=0){
        light.setText("Light intensity:"+event.values[0]+" ("+max+")");
        note.setText("Current Note: "+toServer);
        float x= (max-5)/8;
        if(event.values[0]<x){
          c.start();
          toServer="c Theremin";
        }
        else if(event.values[0]<2*x){
          d.start();
          toServer="d Theremin";
        }
        else if(event.values[0]<3*x){
          e.start();
          toServer="e Theremin";
        }
        else if(event.values[0]<4*x){
          f.start();
          toServer="f Theremin";
        }
        else if(event.values[0]<5*x){
          g.start();
          toServer="g Theremin";
        }
        else if(event.values[0]<6*x){
          a.start();
          toServer="a Theremin";
        }
        else if(event.values[0]<7*x){
          h.start();
          toServer="h Theremin";
        }
        else if(event.values[0]<8*x){
          c2.start();
          toServer="c2 Theremin";
        }else{
          toServer="0 Theremin";
        }

      };
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {

  }

  @Override
  public void onClick(View v) {
    if(v.getId()==R.id.cal){
      max=current;
    }

  }
}
