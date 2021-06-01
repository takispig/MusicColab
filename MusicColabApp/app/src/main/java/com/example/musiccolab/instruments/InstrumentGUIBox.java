package com.example.musiccolab.instruments;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.musiccolab.Lobby;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI / box for every instrument in the "green box" in the Lobby
 */
public class InstrumentGUIBox {

    private final TextView textInCenter;
    private final List<Button> pianoKeys = new ArrayList<>();

    public InstrumentGUIBox(Lobby lobby, int textInCenterID, List<Integer> pianoKeysIDs) {
        textInCenter = lobby.findViewById(textInCenterID);
        for (Integer id : pianoKeysIDs) {
            pianoKeys.add(lobby.findViewById(id));
        }
        setPianoKeysInvisible();
    }

    private void setPianoKeysInvisible() {
        for (Button btn : pianoKeys) {
            btn.setVisibility(View.GONE);
        }
    }

    public void setTextInCenter(String txt) {
        textInCenter.setText(txt);
    }

    public void setPianoKeysVisible() {
        for (Button btn : pianoKeys) {
            btn.setVisibility(View.VISIBLE);
        }
    }

    public List<Button> getPianoKeys() {
        return pianoKeys;
    }
}