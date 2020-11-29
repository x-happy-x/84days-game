package ru.happy.game.adventuredog.Interfaces;

import com.badlogic.gdx.math.Vector2;

import ru.happy.game.adventuredog.MainGDX;

public interface ElementsUI {
    // Привязка для текста
    enum ALIGN {LEFT, CENTER, RIGHT, TOP, BOTTOM, CUSTOM}

    class Action {
        public void isClick() {
        }

        public void isSelected() {
        }

        public void isInput(String text) {
        }

        public void onCompletionAction() {
        }
    }

    void draw(MainGDX game, float delta);
    boolean isClick(float x, float y);
    boolean isClick(Vector2 v);
    void setCursor(float x, float y);
    void setCursor(Vector2 v);
    boolean isActive();
}
