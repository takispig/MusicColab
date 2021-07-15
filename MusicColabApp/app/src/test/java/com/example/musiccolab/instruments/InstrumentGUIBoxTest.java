package com.example.musiccolab.instruments;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.musiccolab.Lobby;
import com.example.musiccolab.R;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.musiccolab.instruments.InstrumentGUIBox.DRUMS_ROTATION_NORMAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstrumentGUIBoxTest {

    private List<Integer> pianoKeyIDs;
    private Button btn;
    private final int randomInt = new Random().nextInt();
    private final String randomString = new Random().doubles().toString();
    private ImageView defaultBlack;
    private ImageView image4Drums;
    private ImageView image4Theremin;
    private TextView textInCenter;

    private InstrumentGUIBox createInstrumentGUIBox() {
        pianoKeyIDs = new ArrayList<>();
        Lobby lobby = mock(Lobby.class);
        btn = mock(Button.class);
        pianoKeyIDs.add(R.id.btnC);
        when(lobby.findViewById(R.id.btnC)).thenReturn(btn);
        pianoKeyIDs.add(R.id.btnD);
        when(lobby.findViewById(R.id.btnD)).thenReturn(btn);
        pianoKeyIDs.add(R.id.btnE);
        when(lobby.findViewById(R.id.btnE)).thenReturn(btn);
        pianoKeyIDs.add(R.id.btnF);
        when(lobby.findViewById(R.id.btnF)).thenReturn(btn);
        pianoKeyIDs.add(R.id.btnG);
        when(lobby.findViewById(R.id.btnG)).thenReturn(btn);
        pianoKeyIDs.add(R.id.btnA);
        when(lobby.findViewById(R.id.btnA)).thenReturn(btn);
        pianoKeyIDs.add(R.id.btnH);
        when(lobby.findViewById(R.id.btnH)).thenReturn(btn);
        pianoKeyIDs.add(R.id.btnC2);
        when(lobby.findViewById(R.id.btnC2)).thenReturn(btn);
        int textInCenterID = R.id.iva_text_1;
        textInCenter = mock(TextView.class);
        when(lobby.findViewById(textInCenterID)).thenReturn(textInCenter);
        image4Drums = mock(ImageView.class);
        when(lobby.findViewById(R.id.drumImageView)).thenReturn(image4Drums);
        image4Theremin = mock(ImageView.class);
        when(lobby.findViewById(R.id.thereminView)).thenReturn(image4Theremin);
        defaultBlack = mock(ImageView.class);
        when(lobby.findViewById(R.id.blackView)).thenReturn(defaultBlack);
        return new InstrumentGUIBox(lobby, textInCenterID, pianoKeyIDs);
    }

    @Test
    public void test_init_correctSizeOfPianoKeysList() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();

        // pre assert
        assertEquals(0, guiBox.getPianoKeys().size());

        // act
        guiBox.init();

        // assert
        assertEquals(pianoKeyIDs.size(), guiBox.getPianoKeys().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    public void test_startAnimationForPianoKey(int indices) {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();
        Animation animation = mock(Animation.class);

        // act
        guiBox.setScaleDownAnimation(animation);
        guiBox.startAnimationForPianoKey(indices);

        // assert
        verify(btn, times(1)).startAnimation(animation);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    public void test_clearAnimationForPianoKey(int indices) {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();
        Animation animation = mock(Animation.class);

        // act
        guiBox.setScaleDownAnimation(animation);
        guiBox.clearAnimationForPianoKey(indices);

        // assert
        verify(btn, times(1)).clearAnimation();
    }

    @Test
    public void test_setThereminAlpha() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();

        // act
        guiBox.setThereminAlpha(randomInt);

        // assert
        verify(defaultBlack, times(1)).setImageAlpha(randomInt);
    }

    @Test
    public void test_setDrumsNormal() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();

        // act
        guiBox.setDrumsNormal();

        // assert
        verify(image4Drums, times(1)).setRotationX(DRUMS_ROTATION_NORMAL);
    }

    @Test
    public void test_setTextInCenter() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();

        // act
        guiBox.setTextInCenter(randomString);

        // assert
        verify(textInCenter, times(1)).setText(randomString);
    }

    @Test
    public void test_setDrumsRotateLeft() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();
        RotateAnimation rotate = mock(RotateAnimation.class);
        guiBox.createRotateAnimations(rotate, rotate, rotate);

        // act
        guiBox.setDrumsRotate(Drums.DRUMS_IMAGE_ROTATION_LEFT);

        // assert
        verify(image4Drums, times(1)).startAnimation(rotate);
    }

    @Test
    public void test_setDrumsRotateVert() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();
        RotateAnimation rotate = mock(RotateAnimation.class);
        guiBox.createRotateAnimations(rotate, rotate, rotate);

        // act
        guiBox.setDrumsRotate(Drums.DRUMS_IMAGE_ROTATION_VERT);

        // assert
        verify(image4Drums, times(1)).startAnimation(rotate);
    }

    @Test
    public void test_setDrumsRotateRight() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();
        RotateAnimation rotate = mock(RotateAnimation.class);
        guiBox.createRotateAnimations(rotate, rotate, rotate);

        // act
        guiBox.setDrumsRotate(Drums.DRUMS_IMAGE_ROTATION_RIGHT);

        // assert
        verify(image4Drums, times(1)).startAnimation(rotate);
    }

    @Test
    public void test_setPianoKeysVisible() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();

        // act
        guiBox.setPianoKeysVisible();

        // assert
        verify(btn, times(8)).setVisibility(View.VISIBLE);
        verify(image4Theremin, times(1)).setVisibility(View.GONE);
        verify(image4Drums, times(1)).setVisibility(View.GONE);
    }

    @Test
    public void test_setDrumsVisible() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();

        // act
        guiBox.setDrumsVisible();

        // assert
        verify(image4Drums, times(1)).setVisibility(View.VISIBLE);
        verify(btn, times(8)).setVisibility(View.GONE);
        verify(image4Theremin, times(1)).setVisibility(View.GONE);
    }

    @Test
    public void test_setThereminVisible() {
        // arrange
        InstrumentGUIBox guiBox = createInstrumentGUIBox();
        guiBox.init();

        // act
        guiBox.setThereminVisible();

        // assert
        verify(image4Theremin, times(1)).setVisibility(View.VISIBLE);
        verify(image4Drums, times(1)).setVisibility(View.GONE);
        verify(btn, times(8)).setVisibility(View.GONE);
    }
}