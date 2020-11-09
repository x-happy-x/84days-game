package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;

import java.util.ArrayList;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;

import static java.lang.Math.max;

public class Layout {

    ArrayList<Color> colors;
    ColorAction[] rColors = new ColorAction[4];
    ColorAction pbCA = new ColorAction();
    Color stColor, pbColor;
    Rectangle pb = new Rectangle(), pause = new Rectangle();
    float timeDelta, animDelta = 2, pbMargin1 = 10, pbMargin2 = 10;

    public Layout() {}

    // Смена цветов градиента
    public void colorize() {
        timeDelta = 0;
        for (ColorAction rColor : rColors) {
            stColor = rColor.getColor();
            rColor.reset();
            rColor.setColor(stColor);
            rColor.setEndColor(getRandomColor());
        }
    }

    // Случайный цвет
    private Color getRandomColor() {
        return colors.get(MathUtils.random(0, colors.size() - 1));
    }

    // Настройка градиента
    public void ColorPrefs(ArrayList<Color> colors) {
        this.colors = colors;
        for (int i = 0; i < rColors.length; i++) {
            rColors[i] = new ColorAction();
            rColors[i].setColor(getRandomColor());
            rColors[i].setDuration(animDelta);
        }
        colorize();
    }

    public void ColorPrefs(ArrayList<Color> colors, float delta) {
        animDelta = delta;
        ColorPrefs(colors);
    }

    // Рисование окна
    public void drawRectangle(ShapeRenderer renderer, Color color) {
        drawRectangle(renderer, color, 0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, Rectangle rect) {
        drawRectangle(renderer, color, rect.x, rect.y, rect.width, rect.height);
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, Rectangle rect, float r) {
        drawRectangle(renderer, color, rect.x, rect.y, rect.width, rect.height, r);
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, float offset, Rectangle rect) {
        drawRectangle(renderer, color, rect.x - offset, rect.y - offset, rect.width + offset * 2, rect.height + offset * 2);
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, float offset, Rectangle rect, float r) {
        drawRectangle(renderer, color, rect.x - offset, rect.y - offset, rect.width + offset * 2, rect.height + offset * 2, r);
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, float x, float y, float w, float h) {
        renderer.setColor(color);
        renderer.rect(x, y, w, h);
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, float x, float y, float w, float h, float r) {
        renderer.setColor(color);
        if (r * 2f > h || r * 2f > w) r = Math.min(h, w) / 2f;
        renderer.circle(x + r, y + r, r);
        renderer.circle(x + w - r, y + r, r);
        renderer.circle(x + r, y + h - r, r);
        renderer.circle(x + w - r, y + h - r, r);
        drawRectangle(renderer, color, x, y + r, w, h - r * 2);
        drawRectangle(renderer, color, x + r, y, w - r * 2, h);
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, float x, float y, float w, float h, float bottomLeftRadius, float bottomRightRadius, float topLeftRadius, float topRightRadius) {
        renderer.setColor(color);
        /*if (r1*2f > h || r1*2f > w) r1 = Math.min(h, w)/2f;
        if (r2*2f > h || r2*2f > w) r2 = Math.min(h, w)/2f;
        if (r3*2f > h || r3*2f > w) r3 = Math.min(h, w)/2f;
        if (r4*2f > h || r4*2f > w) r4 = Math.min(h, w)/2f;*/
        renderer.circle(x + bottomLeftRadius, y + bottomLeftRadius, bottomLeftRadius); // Bottom left
        renderer.circle(x + w - bottomRightRadius, y + bottomRightRadius, bottomRightRadius); // Bottom right
        renderer.circle(x + topLeftRadius, y + h - topLeftRadius, topLeftRadius); // Top left
        renderer.circle(x + w - topRightRadius, y + h - topRightRadius, topRightRadius); // Top right
        drawRectangle(renderer, color, x + topLeftRadius, y + h - max(topLeftRadius, topRightRadius), w - topLeftRadius - topRightRadius, max(topLeftRadius, topRightRadius)); // top
        drawRectangle(renderer, color, x + bottomLeftRadius, y, w - bottomLeftRadius - bottomRightRadius, max(bottomLeftRadius, bottomRightRadius)); // bottom
        drawRectangle(renderer, color, x, y + bottomLeftRadius, max(bottomLeftRadius, topLeftRadius), h - bottomLeftRadius - topLeftRadius); // left
        drawRectangle(renderer, color, x + w - max(bottomRightRadius, topRightRadius), y + bottomRightRadius, max(bottomRightRadius, topRightRadius), h - bottomRightRadius - topRightRadius); // right
        drawRectangle(renderer, color, x + max(bottomLeftRadius, topLeftRadius), y + max(bottomLeftRadius, bottomRightRadius), w - max(bottomLeftRadius, topLeftRadius) - max(bottomRightRadius, topRightRadius), h - max(bottomLeftRadius, bottomRightRadius) - max(topLeftRadius, topRightRadius)); // center
    }

    public void drawRectangle(ShapeRenderer renderer, Color color, Rectangle r, float r1, float r2, float r3, float r4) {
        drawRectangle(renderer, color, r.x, r.y, r.width, r.height, r1, r2, r3, r4);
    }

    public void drawPause(ShapeRenderer renderer, float offset, float x, float y, float w, float h, float r, Color color1, Color color2) {
        drawRectangle(renderer, color2, x - offset, y - offset, w / 4 * 1.5f + offset * 2, h + offset * 2, r);
        drawRectangle(renderer, color1, x, y, w / 4 * 1.5f, h, r);
        drawRectangle(renderer, color2, x + w / 4 * 2.5f - offset, y - offset, w / 4 * 1.5f + offset * 2, h + offset * 2, r);
        drawRectangle(renderer, color1, x + w / 4 * 2.5f, y, w / 4 * 1.5f, h, r);
    }

    public void drawPause(ShapeRenderer renderer, float offset, float r, Rectangle rect, Color c1, Color c2) {
        drawPause(renderer, offset, rect.x, rect.y, rect.width, rect.height, r, c1, c2);
    }

    public Rectangle[] drawEnd(MainGDX game, String[] menuList, String title, float progress, Interpolation interpolation, boolean win, int selected, Color accent, Color bg) {
        String[] newMenuList;
        String _title_ = win ? "УРОВЕНЬ ПРОЙДЕН" : "УРОВЕНЬ ПРОВАЛЕН";
        int select = 0;
        if (win) {
            newMenuList = menuList;
            select = selected;
        } else {
            newMenuList = new String[menuList.length - 1];
            select = selected - 1;
            for (int i = 0; i < newMenuList.length; i++) {
                newMenuList[i] = menuList[i + 1];
            }
        }
        Rectangle[] m = drawMenu(game, _title_, newMenuList, progress, interpolation, accent, bg, select, true);
        if (win) {
            game.world.setText(title, 1f, MainGDX.WIDTH / 2f - game.world.getSizes()[0] / 2f, interpolation.apply(MainGDX.HEIGHT + MainGDX.HEIGHT / 12f - 30, MainGDX.HEIGHT * 5f / 6.5f + MainGDX.HEIGHT / 12f - 30, progress), Color.WHITE, false, true, GameWorld.FONTS.SMALL);
            if (game.world.getBonus) {
                game.world.setText("+1 ПОДСКАЗКА", 1f, MainGDX.WIDTH / 2f + game.world.getTextSize(title, 1, GameWorld.FONTS.MEDIAN)[0] / 2f - game.world.getTextSize("+1 ПОДСКАЗКА", 1, GameWorld.FONTS.SMALL)[0], interpolation.apply(MainGDX.HEIGHT + MainGDX.HEIGHT / 12f - 30, MainGDX.HEIGHT * 5f / 6.5f + MainGDX.HEIGHT / 12f - 30, progress), Color.WHITE, false, true, GameWorld.FONTS.SMALL);
            }
        }
        game.end();
        return m;
    }

    public Rectangle[] drawPause(MainGDX game, String[] menuList, float progress, Interpolation interpolation, int score, int good, int all, int selected, Color accent, Color bg) {
        Rectangle[] m = drawMenu(game, "ПАУЗА", menuList, progress, interpolation, accent, bg, selected, true);
        if (score >= 0)game.world.setText("УГАДАНО: " + score, 1f, MainGDX.WIDTH / 2f - game.world.getTextSize("ПАУЗАПАУЗАПАУЗАПАУЗА", 1, GameWorld.FONTS.MEDIAN)[0] / 2f, interpolation.apply(MainGDX.HEIGHT + MainGDX.HEIGHT / 12f - 30, MainGDX.HEIGHT * 5f / 6.5f + MainGDX.HEIGHT / 12f - 30, progress), Color.WHITE, false, true, GameWorld.FONTS.SMALL);
        if (good >= 0) game.world.setText("ХОРОШО: " + good, 1f, MainGDX.WIDTH / 2f, interpolation.apply(MainGDX.HEIGHT + MainGDX.HEIGHT / 12f - 30, MainGDX.HEIGHT * 5f / 6.5f + MainGDX.HEIGHT / 12f - 30, progress), Color.WHITE, true, GameWorld.FONTS.SMALL);
        if (all >= 0)  game.world.setText("ОСТАЛОСЬ: " + all, 1f, MainGDX.WIDTH / 2f + game.world.getTextSize("ПАУЗАПАУЗАПАУЗАПАУЗА", 1, GameWorld.FONTS.MEDIAN)[0] / 2f - game.world.getTextSize("ОСТАЛОСЬ: " + all, 1, GameWorld.FONTS.SMALL)[0], interpolation.apply(MainGDX.HEIGHT + MainGDX.HEIGHT / 12f - 30, MainGDX.HEIGHT * 5f / 6.5f + MainGDX.HEIGHT / 12f - 30, progress), Color.WHITE, false, true, GameWorld.FONTS.SMALL);
        game.end();
        return m;
    }

    public Rectangle[] drawMenu(MainGDX game, String title, String[] menuList, float progress, Interpolation interpolation, Color accent, Color bg, int selected, boolean addtxt) {
        Gdx.gl.glEnable(GL30.GL_BLEND);
        game.drawShape();
        drawRectangle(game.renderer, new Color(0, 0, 0, interpolation.apply(0, 0.5f, progress)));
        bg.sub(0.1f, 0.1f, 0.1f, 0);
        drawRectangle(game.renderer, bg, (MainGDX.WIDTH - MainGDX.WIDTH / 2.5f) / 2f - 10, interpolation.apply(MainGDX.HEIGHT, 0, progress), 10, MainGDX.HEIGHT);
        drawRectangle(game.renderer, bg, (MainGDX.WIDTH - MainGDX.WIDTH / 2.5f) / 2f + MainGDX.WIDTH / 2.5f, interpolation.apply(MainGDX.HEIGHT, 0, progress), 10, MainGDX.HEIGHT);
        drawRectangle(game.renderer, bg.add(0.1f, 0.1f, 0.1f, 0), (MainGDX.WIDTH - MainGDX.WIDTH / 2.5f) / 2f, interpolation.apply(MainGDX.HEIGHT, 0, progress), MainGDX.WIDTH / 2.5f, MainGDX.HEIGHT);
        Rectangle[] rects = new Rectangle[menuList.length];
        bg.sub(0.1f, 0.1f, 0.1f, 0);
        for (int i = 0; i < menuList.length; i++) {
            if (i == 0)
                rects[i] = new Rectangle((MainGDX.WIDTH - MainGDX.WIDTH / 2.5f) / 2f, interpolation.apply(MainGDX.HEIGHT + MainGDX.HEIGHT / 12f * 1.5f * menuList.length, MainGDX.HEIGHT * 5f / 8f, progress), MainGDX.WIDTH / 2.5f, MainGDX.HEIGHT / 12f);
            else
                rects[i] = new Rectangle(rects[i - 1].getX(), rects[i - 1].getY() - rects[i - 1].getHeight() * 1.5f, rects[i - 1].getWidth(), rects[i - 1].getHeight());
            drawRectangle(game.renderer, i == selected ? accent : bg, rects[i]);
        }
        game.endShape();
        Gdx.gl.glDisable(GL30.GL_BLEND);
        game.draw();
        for (int i = 0; i < menuList.length; i++) {
            game.world.setText(menuList[i], 1f, rects[i].x + rects[i].width / 2f, rects[i].y + rects[i].height / 2f, Color.WHITE, true, GameWorld.FONTS.SMEDIAN);
        }
        game.end();
        game.drawShape();
        drawRectangle(game.renderer, accent, 0, interpolation.apply(MainGDX.HEIGHT, MainGDX.HEIGHT * 5f / 6.5f, progress), MainGDX.WIDTH, MainGDX.HEIGHT / 6f);
        game.endShape();
        game.draw();
        String winText = title;
        game.world.setText(winText, 1f, MainGDX.WIDTH / 2f, interpolation.apply(MainGDX.HEIGHT + MainGDX.HEIGHT / 12f, MainGDX.HEIGHT * 5f / 6.5f + MainGDX.HEIGHT / 12f, progress), Color.WHITE, true, GameWorld.FONTS.MEDIAN);
        if (!addtxt) game.end();
        bg.add(0.1f, 0.1f, 0.1f, 0);
        return rects;
    }

    // Рисование градиентного окна
    public void drawColoredRectangle(ShapeRenderer renderer, float delta) {
        drawColoredRectangle(renderer, delta, 0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
    }

    public void drawColoredRectangle(ShapeRenderer renderer, float delta, float x, float y, float w, float h) {
        renderer.rect(x, y, w, h, rColors[0].getColor(), rColors[1].getColor(), rColors[2].getColor(), rColors[3].getColor());
        update(delta);
    }

    public void drawColoredRectangle(ShapeRenderer renderer, float delta, float x, float y, float w, float h, float r) {
        renderer.setColor(rColors[0].getColor());
        renderer.circle(x + r, y + r, r);
        renderer.setColor(rColors[1].getColor());
        renderer.circle(x + w - r, y + r, r);
        renderer.setColor(rColors[2].getColor());
        renderer.circle(x + r, y + h - r, r);
        renderer.setColor(rColors[3].getColor());
        renderer.circle(x + w - r, y + h - r, r);
        drawColoredRectangle(renderer, 0, x, y + r, w, h - r * 2);
        drawColoredRectangle(renderer, delta, x + r, y, w - r * 2, h);
    }

    // Настройка прогресс бара
    public Rectangle createProgressBar(float x, float y, float w, float h, Color color) {
        pb.set(x, y, w, h);
        pbColor = color;
        pbCA.setColor(color);
        pbCA.setDuration(animDelta);
        return pb;
    }

    public Rectangle createProgressBar(float x, float y, float w, float h, Color color, float pbM1, float pbM2) {
        pbMargin1 = pbM1;
        pbMargin2 = pbM2;
        return createProgressBar(x, y, w, h, color);
    }

    // Рисование линии прогресса
    private void drawProgressLine(ShapeRenderer renderer, float progress, float x, float y, float w, float h, Color color) {
        renderer.setColor(color);
        renderer.circle(x + h / 2f, y + h / 2f, h / 2f);
        renderer.circle(x + h / 2 + (w - h) * progress, y + h / 2f, h / 2f);
        renderer.rect(x + h / 2, y, (w - h) * progress, h);
    }

    // Рисование прогресс бара
    public void drawProgressBar(ShapeRenderer renderer, float progress) {
        drawProgressLine(renderer, 1f, pb.x - pbMargin1 - pbMargin2, pb.y - pbMargin1 - pbMargin2, pb.width + (pbMargin1 + pbMargin2) * 2, pb.height + (pbMargin1 + pbMargin2) * 2, pbColor);
        drawProgressLine(renderer, 1f, pb.x - pbMargin1, pb.y - pbMargin1, pb.width + pbMargin1 * 2, pb.height + pbMargin1 * 2, Color.WHITE);
        drawProgressLine(renderer, progress, pb.x, pb.y, pb.width, pb.height, pbColor);
    }

    // Обновление цветов для градиента
    public void update(float delta) {
        timeDelta += delta;
        if (timeDelta >= animDelta) {
            colorize();
        }
        for (ColorAction c : rColors) {
            c.act(delta);
        }
    }
}
