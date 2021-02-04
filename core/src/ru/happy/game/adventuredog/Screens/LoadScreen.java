package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Tools.AssetsTool;

public class LoadScreen implements Screen {

    MainGDX game;
    Sprite bg;
    TextureAtlas atlas;
    Rectangle pb;
    //Map<String, String> levelPref;
    Map<String, String>[] loadTxt;
    String showedHint = "";
    float tDelta;
    int pbShow, bgShow;

    public LoadScreen(MainGDX mainGDX) {
        game = mainGDX;
    }

    public static void setLevel(MainGDX game, int level) {
        if (game.world.getLives() <= 0) level = 0;
        game.world.startSync();
        game.clearBg.set(0, 0, 0, 1);
        game.world.skipLevel = false;
        game.world.usedBonus = false;
        game.world.getBonus = false;
        game.world.firstErrVisible = false;
        game.world.firstVisible = false;
        game.world.getActors().clear();
        game.world.resetMultiplexer();
        if (game.getScreen() != null)
            game.getScreen().dispose();
        game.assets.fresh();
        game.assets.setLevel(level);
        game.manager.setLevel(level);
        game.assets.load();
        game.setScreen(new LoadScreen(game));
    }

    public void runLevel() {
        switch (game.assets.getLevel()) {
            case 0:
                game.setScreen(new MainMenu(game));
                break;
            case 1:
                game.setScreen(new MainScreen(game));
                break;
            case 2:
                game.setScreen(new Guessing(game));
                break;
            case 3:
                game.setScreen(new MusicLevel(game));
                break;
            case 4:
                game.setScreen(new ThreeInRow(game));
                break;
        }
    }

    @Override
    public void show() {
        game.assets.finishLoad(game.manager.getGUI());
        pbShow = -1;
        bgShow = -1;
        if (game.assets.isLevelFile("load2.pref")) {
            String[] nTextures = game.assets.getLevelContent("load2.pref").split("\n\n");
            loadTxt = new HashMap[nTextures.length];
            for (int i = 0; i < nTextures.length; i++) {
                loadTxt[i] = AssetsTool.getParamFromFile(nTextures[i]);
                if (loadTxt[i].get("id").equals("progressbar")) pbShow = i;
                else if (loadTxt[i].get("id").equals("bg")) bgShow = i;
            }
        }
        if (game.assets.getLevel() > 0) {
            //Map<String, String> levelPref = game.assets.getLevelProp();
            showedHint = game.manager.getString(game.assets.getLevel(), "hint" + MathUtils.random(1, game.manager.getInt(game.assets.getLevel(), "hints")));
            if (showedHint == null) showedHint = "";
            if (Gdx.app.getType() == Application.ApplicationType.Desktop)
                showedHint = AssetsTool.encodeString(showedHint, false);
        }
        if (game.manager.get("load") != null) atlas = game.assets.get("load");
        if (atlas != null && atlas.findRegion("loadbg") != null) {
            bg = new Sprite();
            bgShow = -1;
            bg.setBounds(0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
            bg.setRegion(atlas.findRegion("loadbg"));
            float w = bg.getRegionWidth(), h = bg.getRegionHeight();
            if ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() > w / h) {
                bg.setRegion(0, 0, (int) w, (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * w));
                bg.setRegion(0, (int) ((h - bg.getRegionHeight()) / 2f), bg.getRegionWidth(), bg.getRegionHeight());
            } else {
                bg.setRegion(0, 0, (int) ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * h), (int) h);
                bg.setRegion((int) ((w - bg.getRegionWidth()) / 2f), 0, bg.getRegionWidth(), bg.getRegionHeight());
            }
        } else if (bgShow >= 0 && loadTxt[bgShow].get("bg").equals("animated gradient")) {
            ArrayList<Color> colors = new ArrayList<>();
            if (loadTxt[bgShow].containsKey("colors")) {
                for (String color : loadTxt[bgShow].get("colors").split(" ")) {
                    colors.add(Color.valueOf(color));
                }
            } else {
                colors.add(Color.BLUE);
                colors.add(Color.LIME);
                colors.add(Color.RED);
                colors.add(Color.ROYAL);
                colors.add(Color.CLEAR);
                colors.add(Color.CHARTREUSE);
                colors.add(Color.OLIVE);
                colors.add(Color.ORANGE);
                colors.add(Color.PURPLE);
                colors.add(Color.SLATE);
                colors.add(Color.SALMON);
                colors.add(Color.SCARLET);
                colors.add(Color.SCARLET);
                colors.add(Color.CORAL);
                colors.add(Color.CYAN);
                colors.add(Color.GREEN);
                colors.add(Color.GOLD);
            }
            game.layout.ColorPrefs(colors);
        }
        tDelta = 0;
        if (loadTxt != null && pbShow >= 0) {
            int[] r = getPosById(loadTxt[pbShow], MainGDX.WIDTH * 2f / 3, MainGDX.HEIGHT / 30f);
            pb = game.layout.createProgressBar(r[0], r[1], r[2], r[3], Color.valueOf(loadTxt[pbShow].get("color")), 3, 3);
        }
    }

    @Override
    public void render(float delta) {
        if (game.world.isSynced() && tDelta == 0 && game.assets.getProgress(game.assets.getLevel() > 0) == 1) {
            tDelta += ScreenAnim.getDelta();
            if (game.assets.getLevel() == 0) tDelta = 1;
            ScreenAnim.setClose();
            ScreenAnim.setState(true);
        } else if (tDelta > 0 && tDelta < 1) {
            tDelta += ScreenAnim.getDelta();
            if (tDelta > 1) tDelta = 1f;
        }
        if (!game.world.isSynced() || game.assets.updating() || tDelta < 1) {
            if (bgShow >= 0) {
                game.drawShape();
                if (loadTxt[bgShow].get("bg").equals("animated gradient"))
                    game.layout.drawColoredRectangle(game.renderer, delta);
                else {
                    game.renderer.setColor(Color.valueOf(loadTxt[bgShow].get("colors")));
                    game.renderer.rect(0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
                }
                game.endShape();
            }
            game.draw();
            if (bg != null) bg.draw(game.world.getBatch());
            if (loadTxt != null) {
                for (int i = 0; i < loadTxt.length; i++) {
                    if (i == pbShow || i == bgShow) continue;
                    boolean rightOf = loadTxt[i].containsKey("rightOf"), leftOf = loadTxt[i].containsKey("leftOf"),
                            bottomOf = loadTxt[i].containsKey("bottomOf"), topOf = loadTxt[i].containsKey("topOf");
                    if (leftOf || topOf || rightOf || bottomOf) {
                        String of = loadTxt[i].get(leftOf ? "leftOf" : topOf ? "topOf" : rightOf ? "rightOf" : "bottomOf");
                        for (int j = 1; j < i; j++) {
                            if (loadTxt[j].get("id").equals(of)) {
                                TextureRegion region = atlas.findRegion(loadTxt[i].get("id"));
                                int[] r = getPosById(loadTxt[j], atlas.findRegion(loadTxt[j].get("id")).getRegionWidth(), atlas.findRegion(loadTxt[j].get("id")).getRegionHeight());
                                int[] n = getSizeById(loadTxt[i], region.getRegionWidth(), region.getRegionHeight());
                                int endx = 0, endy = 0, offset = 0;
                                if (loadTxt[i].containsKey("offset"))
                                    offset = MainGDX.WIDTH / 100 * Integer.parseInt(loadTxt[i].get("offset"));
                                if (loadTxt[j].containsKey("endx"))
                                    endx = Integer.parseInt(loadTxt[j].get("endx"));
                                if (loadTxt[j].containsKey("endy"))
                                    endy = Integer.parseInt(loadTxt[j].get("endy"));
                                game.world.getBatch().draw(region, r[2] - offset + r[0] + (endx != 0 ? ((MainGDX.WIDTH - r[0]) / 100f * endx) * game.assets.getProgress() : 0), r[1] + r[3] / 2f - n[1] / 2f + (endy != 0 ? ((MainGDX.HEIGHT - r[1]) / 100f * endy) * game.assets.getProgress() : 0), n[0], n[1]);
                            }
                        }
                    } else {
                        TextureRegion region = atlas.findRegion(loadTxt[i].get("id"));
                        int[] r = getPosById(loadTxt[i], region.getRegionWidth(), region.getRegionHeight());
                        int endx = 0, endy = 0;
                        if (loadTxt[i].containsKey("endx"))
                            endx = Integer.parseInt(loadTxt[i].get("endx"));
                        if (loadTxt[i].containsKey("endy"))
                            endy = Integer.parseInt(loadTxt[i].get("endy"));
                        game.world.getBatch().draw(region, r[0] + (endx != 0 ? ((MainGDX.WIDTH - r[0]) / 100f * endx) * game.assets.getProgress() : 0), r[1] + (endy != 0 ? ((MainGDX.HEIGHT - r[1]) / 100f * endy) * game.assets.getProgress() : 0), r[2], r[3]);
                    }
                }
                game.end();
                game.drawShape();
                //if (bg == null) game.layout.drawColoredRectangle(game.renderer, delta);
                if (pbShow >= 0)
                    game.layout.drawProgressBar(game.renderer, game.assets.getProgress());
                game.endShape();
                game.draw();
                if (pbShow >= 0)
                    game.world.setText((int) (game.assets.getProgress() * 100) + "%", 1f, pb.x + pb.width / 2f, pb.y + pb.height / 2f, game.assets.getProgress() < 0.5f ? Color.valueOf(loadTxt[pbShow].get("color")) : Color.WHITE, true, GameWorld.FONTS.SMALL);
                int i = 0;
                float y = pb.y + pb.height / 2f - game.world.getSizes()[1] * 3f;
                for (String s : game.world.isSyncError() ? (game.world.out + "_Нажмите на экран для повторной попытки").split("_") : showedHint.split("_")) {
                    game.world.setText(s, 1f, pb.x + pb.width / 2f, y, Color.valueOf(pbShow >= 0 ? loadTxt[pbShow].get("color") : "000000"), true, GameWorld.FONTS.SMALL);
                    y -= game.world.getSizes()[1] * 1.7f;
                }
            } else if (game.world.isSyncError()) {
                int i = 0;
                float y = MainGDX.HEIGHT / 4f;
                for (String s : (game.world.out + "_Нажмите на экран для повторной попытки").split("_")) {
                    game.world.setText(s, 1f, MainGDX.WIDTH / 2f, y, Color.WHITE, true, GameWorld.FONTS.SMALL);
                    y -= game.world.getTextSize("1", 1f, GameWorld.FONTS.SMALL)[1] * 1.7f;
                }
            }
            if (game.world.isSyncError() && (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ENTER))) {
                game.world.startSync();
            } else if (game.world.isSyncError() && Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
                Gdx.app.exit();
            }
            game.end();
            if (ScreenAnim.getState()) {
                Gdx.gl.glEnable(GL30.GL_BLEND);
                game.drawShape();
                if (ScreenAnim.show(game)) {
                    ScreenAnim.setState(false);
                }
                game.endShape();
                Gdx.gl.glDisable(GL30.GL_BLEND);
            }
        } else {
            if (game.assets.getLevel() > 0 && atlas != null) atlas.dispose();
            runLevel();
        }
    }

    private int[] getPosById(Map<String, String> map, float w, float h) {
        int left = -1, right = -1, top = -1, bottom = -1, width = -1, height = -1;
        boolean centerx = map.containsKey("centerx"), centery = map.containsKey("centery");
        if (!centerx && map.containsKey("left"))
            left = Integer.parseInt(map.get("left"));
        else if (!centerx && map.containsKey("right"))
            right = Integer.parseInt(map.get("right"));
        if (!centery && map.containsKey("top"))
            top = Integer.parseInt(map.get("top"));
        else if (!centery && map.containsKey("bottom"))
            bottom = Integer.parseInt(map.get("bottom"));
        if (map.containsKey("width"))
            width = Integer.parseInt(map.get("width"));
        if (map.containsKey("height"))
            height = Integer.parseInt(map.get("height"));
        if (height >= 0) {
            height = (int) (MainGDX.HEIGHT / 100f * height);
            width = (int) (width >= 0 ? MainGDX.WIDTH / 100f * width : w / h * height);
        } else {
            width = (int) (MainGDX.WIDTH / 100f * width);
            height = (int) (h / w * width);
        }
        if (centerx)
            left = (int) (MainGDX.WIDTH / 2f - width / 2f);
        else if (left >= 0)
            left = (int) (MainGDX.WIDTH / 100f * left);
        else
            left = (int) (MainGDX.WIDTH - width - MainGDX.WIDTH / 100f * right);
        if (centery)
            top = (int) (MainGDX.HEIGHT / 2f - height / 2f);
        else if (bottom >= 0)
            top = (int) (MainGDX.HEIGHT / 100f * bottom);
        else
            top = (int) (MainGDX.HEIGHT - height - MainGDX.HEIGHT / 100f * top);
        return new int[]{left, top, width, height};
    }

    private int[] getSizeById(Map<String, String> map, float w, float h) {
        int height = -1, width = -1;
        if (map.containsKey("width"))
            width = Integer.parseInt(map.get("width"));
        if (map.containsKey("height"))
            height = Integer.parseInt(map.get("height"));
        if (height >= 0) {
            height = (int) (MainGDX.HEIGHT / 100f * height);
            width = (int) (width >= 0 ? MainGDX.WIDTH / 100f * width : w / h * height);
        } else {
            width = (int) (MainGDX.WIDTH / 100f * width);
            height = (int) (h / w * width);
        }
        return new int[]{width, height};
    }

    @Override
    public void resize(int width, int height) {
        if (bg != null) {
            float w = bg.getRegionWidth(), h = bg.getRegionHeight();
            if ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() > w / h) {
                bg.setRegion(0, 0, (int) w, (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * w));
                bg.setRegion(0, (int) ((h - bg.getRegionHeight()) / 2f), bg.getRegionWidth(), bg.getRegionHeight());
            } else {
                bg.setRegion(0, 0, (int) ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * h), (int) h);
                bg.setRegion((int) ((w - bg.getRegionWidth()) / 2f), 0, bg.getRegionWidth(), bg.getRegionHeight());
            }
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
