package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.Map;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Tools.AssetsTool;

public class Dog {
    OrthographicCamera camera;
    Map<String, Map<String, String>> property;
    TextureAtlas atlas;
    Sprite sprite;
    String animState, repeatType;
    boolean repeating, animated, flipX, flipY;
    int minAnim, maxAnim, AnimDir, sizeW, sizeH;
    float moveX, moveY, curAnim;

    public Dog(MainGDX game, float x, float y, int h, int w, String objectName, OrthographicCamera cam) {
        atlas = (TextureAtlas) game.assets.get("graphic");
        String[] animList = AssetsTool.readFile("objects/" + objectName + "/graphic.prop").split("\n\n");
        property = new HashMap<>();
        for (String anim : animList) {
            if (animState == null) animState = anim.split("\n")[0];
            property.put(anim.split("\n")[0], AssetsTool.getParamFromFile(anim.substring(anim.indexOf("\n") + 1)));
        }
        sizeH = h;
        sizeW = w;
        sprite = new Sprite();
        setAnimation(animState);
        sprite.setPosition(x, y);
        camera = cam;
    }

    public void flip() {
        flipX = !flipX;
    }

    public void setAnimation(String animation) {
        animState = animation;
        minAnim = Integer.parseInt(property.get(animState).get("min"));
        maxAnim = Integer.parseInt(property.get(animState).get("max"));
        repeatType = property.get(animState).get("repeatType");
        AnimDir = 1;
        curAnim = minAnim;
        if (sizeW > 0) sprite.setSize(sizeW, getHeight() / getWidth() * sizeW);
        else sprite.setSize(getWidth() / getHeight() * sizeH, sizeH);
        moveX = (int) (Integer.parseInt(property.get(animState).get("moveX")) / getWidth() * sprite.getWidth());
        moveY = (int) (Integer.parseInt(property.get(animState).get("moveY")) / getHeight() * sprite.getHeight());
    }

    public float getY() {
        return sprite.getY();
    }

    public float getX() {
        return sprite.getX();
    }

    public float getHeight() {
        return Float.parseFloat(property.get(animState).get("height"));
    }

    public float getWidth() {
        return Float.parseFloat(property.get(animState).get("width"));
    }

    public Rectangle getRect() {
        return sprite.getBoundingRectangle();
    }

    public void startAnim(String anim, boolean repeat) {
        setAnimation(anim);
        startAnim(repeat);
    }

    public void startAnim(boolean repeat) {
        repeating = repeat;
        animated = true;
    }

    public void stopAnim() {
        animated = false;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void update() {
        if (animated) {
            float step = maxAnim / Float.parseFloat(property.get(animState).get("speed"));
            curAnim += AnimDir * step;
            if ((int) curAnim == maxAnim && !repeating) stopAnim();
            if ((int) curAnim == maxAnim && repeatType.equals("back")) AnimDir = -1;
            else if ((int) curAnim == minAnim) AnimDir = 1;
            else if ((int) curAnim == maxAnim && repeatType.equals("start")) curAnim = minAnim;
            float movingX = moveX / maxAnim * step, movingY = moveY / maxAnim * step;
            sprite.translate(movingX, movingY);
            if (sprite.getX() > MainGDX.WIDTH / 2f - getWidth()) camera.translate(movingX, movingY);
        }
    }

    public void draw(SpriteBatch batch) {
        sprite.setRegion(atlas.findRegion(animState + (int) curAnim));
        if (flipX || flipY) sprite.flip(flipX, flipY);
        sprite.draw(batch);
        update();
    }

    public void dispose() {
        atlas.dispose();
    }
}
