package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Screens.Guessing;
import ru.happy.game.adventuredog.Tools.AssetsTool;

import static ru.happy.game.adventuredog.Tools.AssetsTool.isAndroid;

public class Person extends Actor {
    public int layer, scoreName, scoreMulti;
    private float x_orig;
    private float y_orig;
    private float h_orig;
    private float w_orig;
    boolean visible, action, guess, rotated, showed;
    String[] names;
    String multi;
    Sprite sprite;
    Rectangle rect;

    public Person(TextureAtlas.AtlasRegion region, int x, int y, int w, int h, String name, String multi, int layer) {
        rotated = region.rotate;
        sprite = new Sprite();
        rect = new Rectangle();
        setHeight(h);
        setWidth(w);
        setX(x);
        setY(y);
        this.names = name.split(",");
        this.multi = multi.trim();
        visible = true;
        action = true;
        this.layer = layer;
        setTexture(region);
    }

    public void setTexture(TextureAtlas.AtlasRegion region) {
        sprite.setRegion(region);
        sprite.setRotation(rotated ? -90 : 0);
    }

    public void setX(int x) {
        this.x_orig = x;
        rect.x = x;
    }

    public void setY(int y) {
        this.y_orig = y;
        rect.y = y;
    }

    public void setHeight(int h) {
        this.h_orig = h;
        rect.height = h;
    }

    public void setWidth(int w) {
        this.w_orig = w;
        rect.width = w;
    }

    //public boolean isRotated() {
    //    return rotated;
    //}

    public void setRegion(float x, float y, float w, float h) {
        float w1 = w_orig / w * MainGDX.WIDTH;
        float h1 = h_orig / h * MainGDX.HEIGHT;
        float x1 = x_orig - x;
        float y1 = y_orig - y;
        sprite.setBounds(x1 / w * MainGDX.WIDTH, MainGDX.HEIGHT - (!rotated ? h1 : 0) - y1 / h * MainGDX.HEIGHT, rotated ? h1 : w1, !rotated ? h1 : w1);
    }

    public Rectangle getRect() {
        return rect;
    }

    public Rectangle getScreenRect() {
        return sprite.getBoundingRectangle();
    }

    public boolean collide(float x, float y) {
        if (!showed) return false;
        return visible && sprite.getBoundingRectangle().contains(x, y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        showed = false;
        if (Door.opened.contains(layer)) {
            if (visible) {
                sprite.draw(batch);
                showed = true;
            } else if (action) {
                sprite.setAlpha((160 - Guessing.step) / 60f);
                sprite.draw(batch);
                if ((160 - Guessing.step) / 60f == 0f) {
                    action = false;
                }
            }
        }
    }

    public boolean isShowed() {
        return showed;
    }

    public void drawToCenter(Batch batch, float delta, float y) {
        sprite.setAlpha(delta < 1f ? 1f : 2 - delta);
        sprite.setSize(!rotated ? w_orig / h_orig * MainGDX.HEIGHT / 2f : MainGDX.HEIGHT / 2f, !rotated ? MainGDX.HEIGHT / 2f : w_orig / h_orig * MainGDX.HEIGHT / 2f);
        sprite.setPosition(MainGDX.WIDTH / 2f - (!rotated ? sprite.getWidth() : sprite.getHeight()) / 2f, y - MainGDX.HEIGHT / 25f + (rotated ? sprite.getWidth() : 0));
        sprite.draw(batch);
    }

    public boolean checkName(String x) {
        scoreName = 100;
        x = x.replace("\n", "").replace("ё", "е").replace("Ё", "Е");
        if (!isAndroid()) {
            String xx = AssetsTool.encodeString(x, true).trim();
            for (String s : names) {
                String name = AssetsTool.encodeString(AssetsTool.encodeString(s.trim(), false).replace('ё', 'е').replace('Ё', 'Е'), true);
                if (name.equals(xx)) return true;
                scoreName -= 15;
                if (name.equalsIgnoreCase(xx)) return true;
                scoreName -= 10;
                for (String j : ".,:;_¡!¿?\"'+-*/()[]={}%".split("")) {
                    name = name.replace(j, "");
                    xx = xx.replace(j, "");
                }
                if (name.equals(xx)) return true;
                scoreName -= 10;
                if (name.equalsIgnoreCase(xx)) return true;
                scoreName += 35 - 50 / names.length;
            }
            return false;
        }
        for (String s : names) {
            String name = s.trim().replace('ё', 'е').replace('Ё', 'Е');
            if (name.equals(x.trim())) return true;
            scoreName -= 15;
            if (name.equalsIgnoreCase(x.trim())) return true;
            scoreName -= 10;
            for (String j : ".,:;_¡!¿?\"'+-*/()[]={}%".split("")) {
                name = name.replace(j, "");
                x = x.replace(j, "");
            }
            if (name.equals(x.trim())) return true;
            scoreName -= 10;
            if (name.equalsIgnoreCase(x.trim())) return true;
            scoreName += 35 - 50 / names.length;
        }
        return false;
    }

    public boolean checkMulti(String x) {
        scoreMulti = 100;
        x = x.replace("\n", "").trim().replace('ё', 'е').replace('Ё', 'Е');
        if (!isAndroid()) {
            String multi = AssetsTool.encodeString(AssetsTool.encodeString(this.multi.trim(), false).replace('ё', 'е').replace('Ё', 'Е'), true);
            String xx = AssetsTool.encodeString(x, true).trim();
            if (multi.equals(xx)) return true;
            scoreMulti -= 15;
            if (multi.equalsIgnoreCase(xx)) return true;
            scoreMulti -= 10;
            for (String j : ".,:;_¡!¿?\"'+-*/()[]={}%".split("")) {
                multi = multi.replace(j, "");
                xx = xx.replace(j, "");
            }
            if (multi.equals(xx)) return true;
            scoreMulti -= 10;
            if (multi.equalsIgnoreCase(xx)) return true;
            scoreMulti -= 15;
            return false;
        }
        String multi = this.multi.trim().replace('ё', 'е').replace('Ё', 'Е');
        if (multi.equals(x)) return true;
        scoreMulti -= 15;
        if (multi.equalsIgnoreCase(x)) return true;
        scoreMulti -= 10;
        for (String j : ".,:;_¡!¿?\"'+-*/()[]={}%".split("")) {
            multi = multi.replace(j, "");
            x = x.replace(j, "");
        }
        if (multi.equals(x)) return true;
        scoreMulti -= 10;
        if (multi.equalsIgnoreCase(x)) return true;
        scoreMulti -= 15;
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setGuess(boolean guess) {
        this.guess = guess;
    }

    public int getGuessed() {
        return guess ? 1 : 0;
    }

    @Override
    public String getName() {
        if (!isAndroid()) {
            return AssetsTool.encodeString(names[0], false).trim();
        }
        return names[0].trim();
    }

    public String getMulti() {
        if (!isAndroid()) {
            return AssetsTool.encodeString(multi, false).trim();
        }
        return multi.trim();
    }
}