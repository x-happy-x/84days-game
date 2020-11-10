package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;

public class PlayerSlider {

    //TextureRegion bg, btn;
    float maxValue, minValue, curValue;
    boolean selected, active, paused;
    float delta;
    Color textColor;
    Rectangle pos, rect, drawing;
    TextureRegion icon, icon2;
    ImageView bg, btn;

    public PlayerSlider(TextureRegion btn, TextureRegion bg, TextureRegion ic, TextureRegion ic2, Color textColor) {
        this.bg = new ImageView(bg);
        this.btn = new ImageView(btn);
        rect = new Rectangle();
        pos = new Rectangle();
        drawing = new Rectangle();
        active = true;
        selected = false;
        maxValue = 100;
        delta = curValue = minValue = 0;
        icon = ic;
        icon2 = ic2;
        this.textColor = textColor;
    }

    public void draw(MainGDX game, float _delta_) {
        drawing.setWidth(game.interpolation.apply(pos.width, pos.width * 1.2f, delta));
        drawing.setHeight(game.interpolation.apply(pos.height, pos.height * 1.2f, delta));
        drawing.setX(game.interpolation.apply(pos.x, pos.x - (pos.width * 1.2f - pos.width) / 2, delta));
        drawing.setY(game.interpolation.apply(pos.y, pos.y - (pos.height * 1.2f - pos.height) / 2, delta));
        int minutes;
        minutes = (int) (curValue / 60);
        String v = String.format("%02d:%02d", minutes, (int) (curValue - minutes * 60));
        game.world.setText(v, 1, rect.x - game.world.getTextSize(v, 1, GameWorld.FONTS.SMALL)[0], pos.y + pos.height / 2f, textColor, true, GameWorld.FONTS.SMALL);
        minutes = (int) (maxValue / 60);
        v = String.format("%02d:%02d", minutes, (int) (maxValue - minutes * 60));
        game.world.setText(v, 1, rect.x + rect.width + game.world.getTextSize(v, 1, GameWorld.FONTS.SMALL)[0], pos.y + pos.height / 2f, textColor, true, GameWorld.FONTS.SMALL);
        bg.setSize(rect.width, rect.height);
        bg.setPosition(rect.x, rect.y);
        bg.setRadius(rect.height / 2f);
        bg.draw(game);
        btn.setPosition(drawing.x, drawing.y);
        btn.setSize(drawing.width, drawing.height);
        btn.setRadius(drawing.width / 2f);
        btn.draw(game);
        game.getBatch().draw(isPaused() ? icon2 : icon, drawing.x + drawing.width * 0.25f, drawing.y + drawing.height * 0.25f, drawing.width * 0.5f, drawing.height * 0.5f);
        if (selected) {
            if (delta < 1f) delta += _delta_ * 3f;
        } else if (delta > 0f) {
            delta -= _delta_ * 3f;
        }
    }

    public void setSize(float w, float h) {
        rect.setSize(w, h);
        pos.setPosition(getPosForValue(maxValue), rect.y + rect.height / 2f - pos.height / 2f);
    }

    public void setPosition(float x, float y) {
        rect.setPosition(x, y);
        pos.setPosition(getPosForValue(maxValue), rect.y + rect.height / 2f - pos.height / 2f);
    }

    public void setSizeSlider(float w, float h) {
        pos.setSize(w, h);
        pos.setPosition(getPosForValue(maxValue), rect.y + rect.height / 2f - pos.height / 2f);
    }

    public void setValues(float min, float max) {
        maxValue = max;
        minValue = min;
    }

    public boolean isClicked(Vector2 v) {
        return isClicked(v.x, v.y);
    }

    public boolean isClicked(float x, float y) {
        return isActive() && pos.contains(x, y);
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSelected() {
        return selected;
    }

    private float getPosForValue(float value) {
        return rect.x + (rect.width - pos.width) * (1f / (maxValue - minValue) * (value - minValue));
    }

    public float getY() {
        return pos.getY();
    }

    public float getX() {
        return rect.getX();
    }

    public float getWidth() {
        return rect.getWidth();
    }

    public float getHeight() {
        return pos.getHeight();
    }

    public void setCursor(float x, float y) {
        if (selected && x > 0 && y > 0) {
            if (getPosForValue(minValue) >= x - pos.width / 2f) setValue(minValue);
            else if (getPosForValue(maxValue) <= x - pos.width / 2f) setValue(maxValue);
            else
                setValue((int) ((x - pos.width / 2f - rect.x) * ((maxValue - minValue + 1) / (rect.width - pos.width))));
        } else {
            Rectangle.tmp.set(getX(), getY(), getWidth(), getHeight());
            selected = active && Rectangle.tmp.contains(x, y);
        }
    }

    public void setCursor(Vector2 v) {
        setCursor(v.x, v.y);
    }

    public float getValue() {
        return curValue;
    }

    public void setValue(float value) {
        if (value <= maxValue && value >= minValue) {
            curValue = value;
            pos.setX(getPosForValue(value));
        }
    }
}
