package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Screens.Auth;


public class Button {

    // Привязка для текста
    public enum ALIGN {LEFT, CENTER, RIGHT, TOP, BOTTOM, CUSTOM}
    // Параметры
    protected float tmp_w, delta, dark, deltaAction, finishAction, offsetX, offsetY, offset;
    protected boolean click, active, selected, actionRun, useGL;
    protected Action action;
    protected Rectangle rect, pos, drawing, orig, newPos;
    protected ALIGN alignTV, alignTH;
    protected TextureRegion tmp;
    protected GlyphLayout text;
    protected String _text_;
    protected Color textColor;

    public boolean OnAutoSize;

    protected Button() {}

    public Button(String text, TextureRegion region, GameWorld world, Color color, Action action, ALIGN textGravityV, ALIGN textGravityH) {
        this._text_ = text;
        this.textColor = color;
        this.tmp = region;
        this.delta = this.deltaAction = this.tmp_w = this.finishAction = 0;
        this.dark = 0.1f;
        this.orig = new Rectangle();
        this.drawing = new Rectangle();
        this.newPos = new Rectangle();
        this.pos = new Rectangle();
        this.rect = new Rectangle(tmp.getRegionX(), tmp.getRegionY(), tmp.getRegionWidth(), tmp.getRegionHeight());
        this.text = world.getGlyphLayout(text, 1f, textColor, GameWorld.FONTS.SMEDIAN);
        this.OnAutoSize = active = useGL = true;
        this.actionRun = selected = click = false;
        this.action = action;
        this.alignTH = textGravityH;
        this.alignTV = textGravityV;
        this.offsetY = offsetX = 8;
        this.offset = 0.05f;
        this.setAutoSize();
    }

    public Button(String text, TextureRegion region, GameWorld world, Color color, Action action) {
        this(text, region, world, color, action, ALIGN.CENTER, ALIGN.CENTER);
    }

    public Button(String text, TextureRegion region, GameWorld world, Color color) {
        this(text, region, world, color, null, ALIGN.CENTER, ALIGN.CENTER);
    }

    public void setUseGL(boolean useGL) {
        this.useGL = useGL;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setRect(float x, float y, float w, float h) {
        setPosition(x, y);
        setSize(w, h);
    }

    void setAutoSize() {
        setSize(this.text.width * 1.2f, this.text.height * 3f);
    }

    public void setText(String text, MainGDX game) {
        _text_ = text;
        game.world.getFont(GameWorld.FONTS.SMEDIAN).setColor(textColor.r, textColor.g, textColor.g, Auth.textColor.a);
        this.text.setText(game.world.getFont(GameWorld.FONTS.SMEDIAN), text);
        if (this.text.width > pos.width && OnAutoSize) {
            setAutoSize();
        }
    }

    public Action getAction() {
        return action;
    }

    public String getText() {
        return _text_;
    }

    public void setWidth(float w) {
        pos.width = w;
    }

    public void setHeight(float h) {
        pos.height = h;
    }

    public void setX(float x) {
        pos.x = x;
    }

    public void setY(float y) {
        pos.y = y;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public void setPosition(float x, float y) {
        pos.setPosition(x, y);
        orig.set(pos);
    }

    public void setSize(float w, float h) {
        pos.setSize(w, h);
        tmp_w = tmp.getRegionWidth() / 2f * pos.height / tmp.getRegionHeight();
        orig.set(pos);
    }

    public Rectangle getOrig() {
        return orig;
    }

    public void draw(MainGDX game, float _delta_, float scale, GameWorld.FONTS font) {
        if (actionRun) {
            pos.setPosition(game.interpolation.apply(orig.x, newPos.x, deltaAction / finishAction), game.interpolation.apply(orig.y, newPos.y, deltaAction / finishAction));
            pos.setSize(game.interpolation.apply(orig.width, newPos.width, deltaAction / finishAction), game.interpolation.apply(orig.height, newPos.height, deltaAction / finishAction));
            tmp_w = tmp.getRegionWidth() / 2f * pos.height / tmp.getRegionHeight();
            deltaAction += _delta_;
            if (finishAction <= deltaAction) actionRun = false;
        }
        drawing.setX(game.interpolation.apply(pos.x, pos.x - pos.width * offset, delta));
        drawing.setY(game.interpolation.apply(pos.y, pos.y - pos.height * offset, delta));
        drawing.setWidth(game.interpolation.apply(pos.width, pos.width + pos.width * offset * 2, delta));
        drawing.setHeight(game.interpolation.apply(pos.height, pos.height + pos.height * offset * 2, delta));
        Color sbColor = game.getBatch().getColor();
        float darker = active ? game.interpolation.apply(0, dark, delta) : 0.4f;
        game.getBatch().setColor(sbColor.r - darker, sbColor.g - darker, sbColor.b - darker, Auth.textColor.a);
        tmp.setRegion((int) rect.x, (int) rect.y, (int) rect.width / 2, (int) rect.height);
        game.getBatch().draw(tmp, drawing.x, drawing.y, tmp_w, drawing.height);
        tmp.setRegion((int) (rect.x + rect.width / 2f - 1), (int) rect.y, 2, (int) rect.height);
        game.getBatch().draw(tmp, drawing.x + tmp_w, drawing.y, drawing.width - tmp_w * 2, drawing.height);
        tmp.setRegion((int) (rect.x + rect.width / 2), (int) rect.y, (int) rect.width / 2, (int) rect.height);
        game.getBatch().draw(tmp, drawing.x + drawing.width - tmp_w, drawing.y, tmp_w, drawing.height);
        game.getBatch().setColor(sbColor.r + darker, sbColor.g + darker, sbColor.b + darker, Auth.textColor.a);
        if (useGL && Auth.textColor.a != 1f) setText(getText(), game);
        float textX, textY;
        boolean centerX, centerY;
        switch (alignTH) {
            case TOP:
            case LEFT:
                textX = drawing.x + offsetX;
                centerX = false;
                break;
            case BOTTOM:
            case RIGHT:
                textX = drawing.x + drawing.width - text.width - offsetX;
                centerX = false;
                break;
            case CUSTOM:
                textX = drawing.x + drawing.width / 2f + offsetX;
                centerX = true;
                break;
            case CENTER:
            default:
                textX = drawing.x + drawing.width / 2f;
                centerX = true;
        }
        switch (alignTV) {
            case LEFT:
            case TOP:
                textY = drawing.y + drawing.height - offsetY;
                centerY = false;
                break;
            case RIGHT:
            case BOTTOM:
                textY = drawing.y + text.height + offsetY;
                centerY = false;
                break;
            case CUSTOM:
                textY = drawing.y + drawing.height / 2f + offsetX;
                centerY = true;
                break;
            case CENTER:
            default:
                textY = drawing.y + drawing.height / 2f;
                centerY = true;
        }
        if (OnAutoSize || text.width < getWidth() / 1.2f) {
            if (useGL) game.world.setText(text, textX, textY, centerX, centerY, font);
            else
                game.world.setText(_text_, scale, textX, textY, Color.GOLDENROD.set(textColor.r, textColor.g, textColor.b, Auth.textColor.a), centerX, centerY, font);
        } else {
            game.world.setText(_text_, (getWidth() / 1.2f) / text.width, textX, textY, Color.GOLDENROD.set(textColor.r, textColor.g, textColor.b, Auth.textColor.a), centerX, centerY, font);
        }
        game.getBatch().setColor(1, 1, 1, 1);
        tmp.setRegion((int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height);
        if (getAction() != null) {
            if (click) {
                getAction().isClick();
                click = false;
            } else if (selected) {
                getAction().isSelected();
            }
        }
        if (selected) {
            if (delta < 1f) delta += _delta_ * 3f;
        } else if (delta > 0f) {
            delta -= _delta_ * 3f;
        }
    }

    public void move(float x, float y, float w, float h, float duration, boolean saveOrig) {
        if (saveOrig) orig.set(pos);
        newPos.set(x, y, w, h);
        actionRun = true;
        deltaAction = 0f;
        finishAction = duration;
    }

    public void move(float x, float y, float w, float h, float duration) {
        move(x, y, w, h, duration, true);
    }

    public void draw(MainGDX game, float delta, float x, float y) {
        setPosition(x, y);
        draw(game, delta);
    }

    public void draw(MainGDX game, float delta, float scale, float x, float y) {
        setPosition(x, y);
        draw(game, delta, scale);
    }

    public void draw(MainGDX game, float delta) {
        draw(game, delta, 1f, GameWorld.FONTS.SMEDIAN);
    }

    public void draw(MainGDX game, float delta, float scale) {
        draw(game, delta, scale, GameWorld.FONTS.SMEDIAN);
    }

    public Rectangle getRect() {
        return pos;
    }

    public void setDark(float dark) {
        this.dark = dark;
    }

    public void draw(MainGDX game, float delta, float x, float y, float w, float h) {
        setSize(w, h);
        draw(game, delta, x, y);
    }

    public boolean isClick(float x, float y) {
        click = pos.contains(x, y);
        return click;
    }

    public boolean isClick(float x, float y, float r) {
        pos.set(pos.x - r, pos.y - r, pos.width + r * 2, pos.height + r * 2);
        click = isClick(x, y);
        pos.set(pos.x + r, pos.y + r, pos.width - r * 2, pos.height - r * 2);
        return click;
    }

    public boolean isClick(Vector2 v) {
        return isClick(v.x, v.y);
    }

    public boolean isClick(Vector2 v, float r) {
        return isClick(v.x, v.y, r);
    }

    public void setCursor(float x, float y) {
        selected = active && pos.contains(x, y);
    }

    public void setCursor(Vector2 v) {
        setCursor(v.x, v.y);
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isActive() {
        return active;
    }

    public float getX() {
        return pos.x;
    }

    public float getY() {
        return pos.y;
    }

    public float getWidth() {
        return pos.width;
    }

    public float getHeight() {
        return pos.height;
    }

    public interface Action {
        void isClick();

        void isSelected();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTexture(TextureRegion tmp) {
        this.tmp = tmp;
        rect.set(tmp.getRegionX(), tmp.getRegionY(), tmp.getRegionWidth(), tmp.getRegionHeight());
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setAlignT(ALIGN alignTH, ALIGN alignTV) {
        this.alignTH = alignTH;
        this.alignTV = alignTV;
    }
    /*public Button copy(){
        Button copy = new Button();
        copy._text_ = _text_;
        copy.textColor = textColor;
        copy.tmp = tmp;
        copy.delta = delta;
        copy.dark = dark;
        copy.orig = orig;
        copy.selected = selected;
        copy.active = active;
        copy.click = click;
        copy.drawing = drawing;
        copy.newPos = newPos;
        copy.rect = rect;
        copy.text = text;
        copy.pos = pos;
        copy.setAutoSize();
        copy.action = action;
        copy.alignTH = alignTH;
        copy.alignTV = alignTH;
        copy.offsetX = 8;
        return copy;
    }*/
}
