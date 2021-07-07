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
 * GUI / box for every instrument in the visual box in the Lobby
 */
public class InstrumentGUIBox {

    public static final float DRUMS_ROTATION_NORMAL = 0;
    private final List<Integer> pianoKeysIDs;
    private TextView textInCenter;
    private final List<Button> pianoKeys = new ArrayList<>();
    private Animation scaleDown;
    private final Lobby lobby;
    private final int textInCenterID;

    private ImageView image4Drums;
    private ImageView image4Theremin;
    private ImageView defaultBlack;

    private RotateAnimation rotateLeft, rotateVert, rotateRight;

    /**
     * @param lobby          the activity containing the InstrumentGUIBox
     * @param textInCenterID the id of the TextView displayed in the middle of the box
     * @param pianoKeysIDs   all ids for the Buttons of the piano keys
     */
    public InstrumentGUIBox(Lobby lobby, int textInCenterID, List<Integer> pianoKeysIDs) {
        this.lobby = lobby;
        this.textInCenterID = textInCenterID;
        this.pianoKeysIDs = pianoKeysIDs;
    }

    /**
     * @param scaleDown animation for the piano keys when pressed
     */
    public void setScaleDownAnimation(Animation scaleDown) {
        this.scaleDown = scaleDown;
    }

    /**
     * Initializes the InstrumentGUIBox by creating all necessary ImageViews and all piano keys
     */
    public void init() {
        textInCenter = lobby.findViewById(textInCenterID);
        image4Drums = lobby.findViewById(R.id.drumImageView);
        image4Theremin = lobby.findViewById(R.id.thereminView);
        defaultBlack = lobby.findViewById(R.id.blackView);
        createPianoKeys();
    }

    public void setTextInCenter(String txt) {
        textInCenter.setText(txt);
    }

    /**
     * Sets the Piano picture visible and all other pictures invisible
     */
    public void setPianoKeysVisible() {
        setPianoKeysVisibleIntern();
        setDrumsInvisible();
        setThereminInvisible();
    }

    /**
     * Sets the Drums picture visible and all other pictures invisible
     */
    public void setDrumsVisible() {
        setDrumsVisibleIntern();
        setPianoKeysInvisible();
        setThereminInvisible();
    }

    /**
     * Sets the Theremin picture visible and all other pictures invisible
     */
    public void setThereminVisible() {
        setThereminVisibleIntern();
        setPianoKeysInvisible();
        setDrumsInvisible();
    }

    /**
     * Rotates the picture for the drums.
     *
     * @param rotation the RotateAnimation
     */
    public void setDrumsRotate(int rotation) {
        if (rotation == Drums.DRUMS_IMAGE_ROTATION_LEFT) {
            image4Drums.startAnimation(rotateLeft);
        } else if (rotation == Drums.DRUMS_IMAGE_ROTATION_VERT) {
            image4Drums.startAnimation(rotateVert);
        } else if (rotation == Drums.DRUMS_IMAGE_ROTATION_RIGHT) {
            image4Drums.startAnimation(rotateRight);
        }
    }

    /**
     * Sets the rotation of the drums picture back to default position.
     */
    public void setDrumsNormal() {
        image4Drums.setRotationX(DRUMS_ROTATION_NORMAL);
    }

    /**
     * Sets the visibility of the black picture that is in front of Theremin.
     */
    public void setThereminAlpha(int alphaScale) {
        defaultBlack.setImageAlpha(alphaScale);
    }

    /**
     * @return a list containing all piano keys as Buttons
     */
    public List<Button> getPianoKeys() {
        return pianoKeys;
    }

    /**
     * @param index starts the scale down animation for the given piano key
     */
    public void startAnimationForPianoKey(int index) {
        Button btn = pianoKeys.get(index);
        btn.startAnimation(scaleDown);
    }

    /**
     * @param index clears all animations for the given piano key
     */
    public void clearAnimationForPianoKey(int index) {
        pianoKeys.get(index).clearAnimation();
    }

    /**
     * For testing purposes, this method should be filled with mocks of RotateAnimations
     *
     * @param rotateRight when null, it will create a new instance of RotateAnimation on its own (bad for testing)
     * @param rotateLeft  when null, it will create a new instance of RotateAnimation on its own (bad for testing)
     * @param rotateVert  when null, it will create a new instance of RotateAnimation on its own (bad for testing)
     */
    public void createRotateAnimations(RotateAnimation rotateRight, RotateAnimation rotateLeft, RotateAnimation rotateVert) {
        this.rotateRight = rotateRight;
        this.rotateRight.setDuration(500);
        this.rotateRight.setInterpolator(new LinearInterpolator());
        this.rotateLeft = rotateLeft;
        this.rotateLeft.setDuration(500);
        this.rotateLeft.setInterpolator(new LinearInterpolator());
        this.rotateVert = rotateVert;
        this.rotateVert.setDuration(500);
        this.rotateVert.setInterpolator(new LinearInterpolator());
    }

    private void createPianoKeys() {
        for (Integer id : pianoKeysIDs) {
            pianoKeys.add(lobby.findViewById(id));
        }
    }

    private void setThereminVisibleIntern() {
        image4Theremin.setImageResource(R.drawable.theremin);
        image4Theremin.setVisibility(View.VISIBLE);

        defaultBlack.setImageResource(R.drawable.black);
        defaultBlack.setVisibility(View.VISIBLE);
        defaultBlack.bringToFront();
    }

    private void setPianoKeysVisibleIntern() {
        for (Button btn : pianoKeys) {
            btn.setVisibility(View.VISIBLE);
        }
    }

    private void setDrumsVisibleIntern() {
        image4Drums.setImageResource(R.drawable.drums0);
        image4Drums.setVisibility(View.VISIBLE);
    }

    private void setThereminInvisible() {
        image4Theremin.setImageResource(R.drawable.theremin);
        image4Theremin.setVisibility(View.GONE);

        defaultBlack.setImageResource(R.drawable.black);
        defaultBlack.setVisibility(View.GONE);
    }

    private void setPianoKeysInvisible() {
        for (Button btn : pianoKeys) {
            btn.setVisibility(View.GONE);
        }
    }

    private void setDrumsInvisible() {
        image4Drums.setImageResource(R.drawable.drums0);
        image4Drums.setVisibility(View.GONE);
    }
}