package com.example.musiccolab.instruments;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musiccolab.Client;
import com.example.musiccolab.R;

public class Piano extends AppCompatActivity implements View.OnClickListener{

    Button c, d, e, f, g, a, h, c2;
    Animation scaleUp, scaleDown;
    private SoundPool soundPool;
    private int sound_c, sound_d, sound_e, sound_f, sound_g, sound_a, sound_h, sound_c2;

    //handles the screen rotation
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.piano);

        c = (Button) findViewById(R.id.btnC);
        d = (Button) findViewById(R.id.btnD);
        e = (Button) findViewById(R.id.btnE);
        f = (Button) findViewById(R.id.btnF);
        g = (Button) findViewById(R.id.btnG);
        a = (Button) findViewById(R.id.btnA);
        h = (Button) findViewById(R.id.btnH);
        c2 = (Button) findViewById(R.id.btnC2);

        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder().setMaxStreams(5).build();
        }else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        //match sound to the buttons
        sound_c = soundPool.load(this, R.raw.p_c, 1);
        sound_d = soundPool.load(this, R.raw.p_d, 1);
        sound_e = soundPool.load(this, R.raw.p_e, 1);
        sound_f = soundPool.load(this, R.raw.p_f, 1);
        sound_g = soundPool.load(this, R.raw.p_g, 1);
        sound_a = soundPool.load(this, R.raw.p_a, 1);
        sound_h = soundPool.load(this, R.raw.p_h, 1);
        sound_c2 = soundPool.load(this, R.raw.p_c2, 1);


        Thread pianoThread = new Thread(()->Client.getInstance().run());
        pianoThread.start();
        Client.toneType= 1;
        Client.action=7;

        c.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        c.startAnimation(scaleDown);
                        soundPool.play(sound_c, 1, 1, 0, 0, 1);
                        System.out.println("c pressed");
                        Client.toneAction=1;
                        Client.toneData="c";
                        break;
                    case MotionEvent.ACTION_UP:
                        c.startAnimation(scaleUp);
                        soundPool.pause(sound_c);
                        System.out.println("c released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });

        d.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        d.startAnimation(scaleDown);
                        soundPool.play(sound_d, 1, 1, 0, 0, 1);
                        System.out.println("d pressed");
                        Client.toneAction=1;
                        Client.toneData="d";
                        break;
                    case MotionEvent.ACTION_UP:
                        d.startAnimation(scaleUp);
                        soundPool.pause(sound_d);
                        System.out.println("d released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });

        e.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        e.startAnimation(scaleDown);
                        soundPool.play(sound_e, 1, 1, 0, 0, 1);
                        System.out.println("e pressed");
                        Client.toneAction=1;
                        Client.toneData="e";
                        break;
                    case MotionEvent.ACTION_UP:
                        e.startAnimation(scaleUp);
                        soundPool.pause(sound_e);
                        System.out.println("e released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });

        f.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        f.startAnimation(scaleDown);
                        soundPool.play(sound_f, 1, 1, 0, 0, 1);
                        System.out.println("f pressed");
                        Client.toneAction=1;
                        Client.toneData="f";
                        break;
                    case MotionEvent.ACTION_UP:
                        f.startAnimation(scaleUp);
                        soundPool.pause(sound_f);
                        System.out.println("f released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });

        g.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        g.startAnimation(scaleDown);
                        soundPool.play(sound_g, 1, 1, 0, 0, 1);
                        System.out.println("g pressed");
                        Client.toneAction=1;
                        Client.toneData="g";
                        break;
                    case MotionEvent.ACTION_UP:
                        g.startAnimation(scaleUp);
                        soundPool.pause(sound_g);
                        System.out.println("g released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });

        a.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        a.startAnimation(scaleDown);
                        soundPool.play(sound_a, 1, 1, 0, 0, 1);
                        System.out.println("a pressed");
                        Client.toneAction=1;
                        Client.toneData="a";
                        Client.toneAction=2;
                        break;
                    case MotionEvent.ACTION_UP:
                        a.startAnimation(scaleUp);
                        soundPool.pause(sound_a);
                        System.out.println("a released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });

        h.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        h.startAnimation(scaleDown);
                        soundPool.play(sound_h, 1, 1, 0, 0, 1);
                        System.out.println("h pressed");
                        Client.toneAction=1;
                        Client.toneData="h";
                        break;
                    case MotionEvent.ACTION_UP:
                        h.startAnimation(scaleUp);
                        soundPool.pause(sound_h);
                        System.out.println("h released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });

        c2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        c2.startAnimation(scaleDown);
                        soundPool.play(sound_c2, 1, 1, 0, 0, 1);
                        System.out.println("c2 pressed");
                        Client.toneAction=1;
                        Client.toneData="c2";
                        break;
                    case MotionEvent.ACTION_UP:
                        c2.startAnimation(scaleUp);
                        soundPool.pause(sound_c2);
                        System.out.println("c2 released");
                        Client.toneAction=2;
                        break;
                    default:
                        System.out.println("no key pressed");
                        break;
                }
                return true;
            }
        });
    }
    @Override
    // In this function we will 'hear' for onClick events and according to
    // their IDs we will make the correct decision
    public void onClick(View view) {

    }
}
