package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import ru.happy.game.adventuredog.MainGDX;


public class ImageView {

    protected TextureRegion region;
    protected Rectangle pos, rect, orig, newPos;
    protected float radius, deltaAction, finishAction;
    protected boolean actionRun;
    protected Button.Action action;

    public ImageView(TextureRegion region, Button.Action action) {
        this.region = region;
        this.pos = new Rectangle();
        this.orig = new Rectangle();
        this.newPos = new Rectangle();
        this.radius = 10;
        this.deltaAction = this.finishAction = 1f;
        this.actionRun = false;
        this.action = action;
        this.rect = new Rectangle(region.getRegionX(), region.getRegionY(), region.getRegionWidth(), region.getRegionHeight());
    }

    public ImageView(TextureRegion region) {
        this(region, null);
    }

    public ImageView setAction(Button.Action action) {
        this.action = action;
        return this;
    }

    public ImageView move(float x, float y, float w, float h, float duration) {
        orig.set(pos);
        newPos.set(x, y, w, h);
        actionRun = true;
        deltaAction = 0f;
        finishAction = duration;
        return this;
    }

    public ImageView move(Rectangle rect, float duration) {
        return move(rect.x, rect.y, rect.width, rect.height, duration);
    }

    public Rectangle getOrig() {
        return orig;
    }

    public float getMovingState() {
        return deltaAction / finishAction;
    }

    public void draw(MainGDX game) {
        if (actionRun) {
            pos.setPosition(game.interpolation.apply(getOrig().x, newPos.x, deltaAction / finishAction),
                    game.interpolation.apply(getOrig().y, newPos.y, deltaAction / finishAction));
            pos.setSize(game.interpolation.apply(getOrig().width, newPos.width, deltaAction / finishAction),
                    game.interpolation.apply(getOrig().height, newPos.height, deltaAction / finishAction));
            deltaAction += Gdx.graphics.getDeltaTime();
            if (finishAction <= deltaAction) {
                if (action != null) action.onCompletionAction();
                actionRun = false;
            }
        }
        setRegion(rect.x, rect.y, rect.width / 2f, rect.height / 2f);
        game.getBatch().draw(region, pos.x, pos.y + pos.height - radius, radius, radius);
        setRegion(rect.x + rect.width / 2f - 1, rect.y, 2, rect.height / 2f);
        game.getBatch().draw(region, pos.x + radius, pos.y + pos.height - radius, pos.width - radius * 2, radius);
        setRegion(rect.x + rect.width / 2, rect.y, rect.width / 2f, rect.height / 2f);
        game.getBatch().draw(region, pos.x + pos.width - radius, pos.y + pos.height - radius, radius, radius);

        setRegion(rect.x, rect.y + rect.height / 2f, rect.width / 2f, rect.height / 2f);
        game.getBatch().draw(region, pos.x, pos.y, radius, radius);
        setRegion(rect.x + rect.width / 2f - 1, rect.y + rect.height / 2f, 2, rect.height / 2f);
        game.getBatch().draw(region, pos.x + radius, pos.y, pos.width - radius * 2, radius);
        setRegion(rect.x + rect.width / 2, rect.y + rect.height / 2f, rect.width / 2f, rect.height / 2f);
        game.getBatch().draw(region, pos.x + pos.width - radius, pos.y, radius, radius);

        setRegion(rect.x, rect.y + rect.height / 2f - 1, rect.width / 2f, 2);
        game.getBatch().draw(region, pos.x, pos.y + radius, radius, pos.height - radius * 2);
        setRegion(rect.x + rect.width / 2f - 1, rect.y + rect.height / 2f - 1, 2, 2);
        game.getBatch().draw(region, pos.x + radius, pos.y + radius, pos.width - radius * 2, pos.height - radius * 2);
        setRegion(rect.x + rect.width / 2, rect.y + rect.height / 2f - 1, rect.width / 2f, 2);
        game.getBatch().draw(region, pos.x + pos.width - radius, pos.y + radius, radius, pos.height - radius * 2);
        setRegion(rect.x, rect.y, rect.width, rect.height);
    }

    public ImageView setPosition(float x, float y) {
        pos.setPosition(x, y);
        return this;
    }

    public ImageView setSize(float w, float h) {
        pos.setSize(w, h);
        return this;
    }

    public ImageView setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    private ImageView setRegion(float x, float y, float w, float h) {
        this.region.setRegion((int) x, (int) y, (int) w, (int) h);
        return this;
    }

    public ImageView setRect(float x, float y, float w, float h) {
        pos.set(x, y, w, h);
        return this;
    }

    public float getX() {
        return pos.getX();
    }

    public void setX(float x) {
        pos.setX(x);
    }

    public float getY() {
        return pos.getY();
    }

    public ImageView setY(float y) {
        pos.setY(y);
        return this;
    }

    public float getWidth() {
        return pos.getWidth();
    }

    public ImageView setWidth(float w) {
        pos.setWidth(w);
        return this;
    }

    public float getHeight() {
        return pos.getHeight();
    }

    public ImageView setHeight(float h) {
        pos.setHeight(h);
        return this;
    }

    public ImageView center(){
        setPosition((MainGDX.WIDTH-getWidth())/2f,(MainGDX.HEIGHT-getHeight())/2f);
        return this;
    }
}
