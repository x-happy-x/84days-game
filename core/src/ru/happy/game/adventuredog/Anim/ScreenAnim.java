package ru.happy.game.adventuredog.Anim;

import com.badlogic.gdx.graphics.Color;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.UI.Layout;

public class ScreenAnim {
    private static boolean fadeIn, startAnim;
    private static float alpha = 1.0f, delta = 0.05f;
    private static Layout bgTexture;
    private static final Color color = new Color();
    public static int level;

    public static void load() {
        fadeIn = false;
        bgTexture = new Layout();
    }

    public static void setDelta(float delta) {
        ScreenAnim.delta = delta;
    }

    public static float getAlpha() {
        return alpha;
    }

    public static void setOpen() {
        fadeIn = false;
        alpha = 1.0f;
    }

    public static void setClose() {
        fadeIn = true;
        alpha = 0.0f;
    }

    public static float getDelta() {
        return delta;
    }

    public static boolean getState() {
        return startAnim;
    }

    public static boolean isClosing() {
        return fadeIn;
    }

    public static void setState(boolean x) {
        startAnim = x;
    }

    public static boolean show(MainGDX mainGDX) {
        alpha += delta * (fadeIn ? 1 : -1);
        color.set(0, 0, 0, alpha > 1 || alpha < 0 ? (fadeIn ? 1 : 0) : alpha);
        bgTexture.drawRectangle(mainGDX.renderer, color);
        return alpha - delta > 1 || alpha + delta < 0;
    }
}
