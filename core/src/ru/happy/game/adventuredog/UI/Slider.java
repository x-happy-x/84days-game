package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Screens.Auth;

public class Slider {
    int maxValue, minValue, curValue;
    boolean selected, active;
    float delta;
    Color textColor, accent, bgColor;
    Rectangle pos, rect, drawing;

    public Slider(Color accent, Color bg) {
        rect = new Rectangle();
        pos = new Rectangle();
        drawing = new Rectangle();
        delta = 0;
        active = true;
        selected = false;
        maxValue = 100;
        minValue = 0;
        curValue = minValue;
        textColor = Color.WHITE;
        this.accent = accent;
        bgColor = bg;
    }

    public void draw(MainGDX game, float _delta_) {
        drawing.setX(game.interpolation.apply(pos.x, pos.x - 10, delta));
        drawing.setY(game.interpolation.apply(pos.y, pos.y - 4, delta));
        drawing.setWidth(game.interpolation.apply(pos.width, pos.width + 20, delta));
        drawing.setHeight(game.interpolation.apply(pos.height, pos.height + 8, delta));
        boolean shapeOn = game.renderer.isDrawing(),
                batchOn = game.getBatch().isDrawing();
        if (batchOn) game.end();
        Gdx.gl.glEnable(GL30.GL_BLEND);
        if (!shapeOn) game.drawShape();
        bgColor.a = Auth.textColor.a;
        accent.a = Auth.textColor.a;
        game.layout.drawRectangle(game.renderer, bgColor, rect, rect.height);
        game.layout.drawRectangle(game.renderer, accent, drawing, rect.height);
        game.endShape();
        Gdx.gl.glDisable(GL30.GL_BLEND);
        game.draw();
        textColor.a = Auth.textColor.a;
        game.world.setText(curValue + "", 1f, pos.x + pos.width / 2f, pos.y + pos.height / 2f, textColor, true, GameWorld.FONTS.SMEDIAN);
        if (shapeOn) {
            game.end();
            game.drawShape();
        } else if (!batchOn) {
            game.end();
        }
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

    public void setValues(int min, int max) {
        maxValue = max;
        minValue = min;
    }

    private float getPosForValue(int value) {
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

    public int getValue() {
        return curValue;
    }

    public void setValue(int value) {
        if (value <= maxValue && value >= minValue) {
            curValue = value;
            pos.setX(getPosForValue(value));
        }
    }
}
