package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import ru.happy.game.adventuredog.MainGDX;


public class ImageView {
    TextureRegion region;
    Rectangle pos, rect;
    float radius;

    public ImageView(TextureRegion region) {
        this.region = region;
        this.pos = new Rectangle();
        this.radius = 10;
        this.rect = new Rectangle(region.getRegionX(), region.getRegionY(), region.getRegionWidth(), region.getRegionHeight());
    }

    public void draw(MainGDX game) {
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
        setRegion(rect.x,rect.y,rect.width,rect.height);
    }

    public void setPosition(float x, float y) {
        pos.setPosition(x, y);
    }

    public void setSize(float w, float h) {
        pos.setSize(w, h);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    private void setRegion(float x, float y, float w, float h) {
        this.region.setRegion((int) x, (int) y, (int) w, (int) h);
    }

    public void setRect(float x, float y, float w, float h) {
        pos.set(x, y, w, h);
    }

    public void setX(float x) {
        pos.setX(x);
    }

    public void setY(float y) {
        pos.setY(y);
    }

    public void setWidth(float w) {
        pos.setWidth(w);
    }

    public void setHeight(float h) {
        pos.setHeight(h);
    }

    public float getX() {
        return pos.getX();
    }

    public float getY() {
        return pos.getY();
    }

    public float getWidth() {
        return pos.getWidth();
    }

    public float getHeight() {
        return pos.getHeight();
    }
}
