package ru.happy.game.adventuredog.Tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import ru.happy.game.adventuredog.MainGDX;

public class GraphicTool {
    private static final Vector2 v = new Vector2();

    public static Vector2 toLocal(Vector2 v) {
        return toLocal(v.x, v.y);
    }

    public static Vector2 toLocal(float x, float y) {
        return new Vector2(x / Gdx.graphics.getWidth() * MainGDX.WIDTH, y / Gdx.graphics.getHeight() * MainGDX.HEIGHT);
    }

    public static Vector2 fromLocal(Vector2 v) {
        return fromLocal(v.x, v.y);
    }

    public static Vector2 fromLocal(float x, float y) {
        return new Vector2(x / MainGDX.WIDTH * Gdx.graphics.getWidth(), y / MainGDX.HEIGHT * Gdx.graphics.getHeight());
    }

    public static void toLocal(Vector2 v, float x, float y) {
        v.set(x / Gdx.graphics.getWidth() * MainGDX.WIDTH, y / Gdx.graphics.getHeight() * MainGDX.HEIGHT);
    }

    public static Vector2 getClick() {
        return Gdx.input.isTouched() ? v.set((float) Gdx.input.getX() / Gdx.graphics.getWidth() * MainGDX.WIDTH, MainGDX.HEIGHT - (float) Gdx.input.getY() / Gdx.graphics.getHeight() * MainGDX.HEIGHT) : v.set(0, 0);
    }

    public static void addRectArea(Rectangle r, int x) {
        r.set(r.x - x, r.y - x, r.width + x * 2, r.height + x * 2);
    }
}
