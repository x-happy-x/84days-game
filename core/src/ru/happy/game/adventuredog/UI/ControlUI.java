package ru.happy.game.adventuredog.UI;

import java.util.HashMap;
import java.util.Map;

public class ControlUI {
    Map<String, ControlElement> elements = new HashMap<>();
    public void draw(){
        elements.get(0).draw();
    }
}
