package com.example.musiccolab.instruments;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.R;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI / box for every instrument in the "green box" in the Lobby
 */
public class InstrumentGUIBox {

    public static int instrumentType;
    private final TextView textInCenter;
    private final List<Button> pianoKeys = new ArrayList<>();

    private ImageView image4Drums;
    private ImageView image4Theremin;
    private ImageView defaultBlack;

    //constructor for the piano
    public InstrumentGUIBox(Lobby lobby, int textInCenterID, List<Integer> pianoKeysIDs) {
        textInCenter = lobby.findViewById(textInCenterID);

        //init
        image4Drums = lobby.findViewById(R.id.drumImageView);
        image4Theremin = (ImageView) lobby.findViewById(R.id.thereminView);
        defaultBlack = (ImageView) lobby.findViewById(R.id.blackView);

        if (instrumentType == 0) { //PIANO
            this.setDrumsInvisible();
            this.setThereminInvisible();

            for (Integer id : pianoKeysIDs) {
                pianoKeys.add(lobby.findViewById(id));
            }
            setPianoKeysInvisible();
        } else if (instrumentType == 1) { //DRUMS
            this.setPianoKeysInvisible();
            this.setThereminInvisible();

            image4Drums = lobby.findViewById(R.id.drumImageView);
            image4Drums.setVisibility(View.VISIBLE);
        } else if (instrumentType == 2) {//THEREMIN
            this.setPianoKeysInvisible();
            this.setDrumsInvisible();

            setPianoKeysInvisible();

            image4Theremin = (ImageView) lobby.findViewById(R.id.thereminView);
            defaultBlack = (ImageView) lobby.findViewById(R.id.blackView);

            image4Theremin.setVisibility(View.VISIBLE);
            defaultBlack.setVisibility(View.VISIBLE);
        }
    }

    public void setPianoKeysVisible() {
        for (Button btn : pianoKeys) {
            btn.setVisibility(View.VISIBLE);
        }
    }

    private void setPianoKeysInvisible() {
        for (Button btn : pianoKeys) {
            btn.setVisibility(View.GONE);
        }
    }

    public void setTextInCenter(String txt) {
        textInCenter.setText(txt);
    }

    public void setDrumsVisible() {
        image4Drums.setImageResource(R.drawable.drums0);
        image4Drums.setVisibility(View.VISIBLE);
        setPianoKeysInvisible();
        setThereminInvisible();
    }

    public void setDrumsInvisible() {
        image4Drums.setImageResource(R.drawable.drums0);
        image4Drums.setVisibility(View.GONE);
    }

    public void setDrumsRotateRight() {
        RotateAnimation rotate = new RotateAnimation(40, 45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setInterpolator(new LinearInterpolator());
        image4Drums.startAnimation(rotate);
    }

    public void setDrumsRotateLeft() {
        RotateAnimation rotate = new RotateAnimation(-40, -45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setInterpolator(new LinearInterpolator());
        image4Drums.startAnimation(rotate);
    }

    public void setDrumsRotateVert() {
        RotateAnimation rotate = new RotateAnimation(0, 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setInterpolator(new LinearInterpolator());
        image4Drums.startAnimation(rotate);
    }

    public void setDrumsNormal() {
        image4Drums.setRotationX(0);
    }

    public void setThereminVisible() {
        image4Theremin.setImageResource(R.drawable.theremin);
        image4Theremin.setVisibility(View.VISIBLE);

        defaultBlack.setImageResource(R.drawable.black);
        defaultBlack.setVisibility(View.VISIBLE);
        defaultBlack.bringToFront();
        setPianoKeysInvisible();
    }

    public void setThereminInvisible() {
        image4Theremin.setImageResource(R.drawable.theremin);
        image4Theremin.setVisibility(View.GONE);

        defaultBlack.setImageResource(R.drawable.black);
        defaultBlack.setVisibility(View.GONE);
        setPianoKeysInvisible();
    }

    //sets the visibility of the black picture that is in front of the theremin
    public void setThereminAlpha(int alphaScale) {
        defaultBlack.setImageAlpha(alphaScale);
    }

    public List<Button> getPianoKeys() {
        return pianoKeys;
    }
}