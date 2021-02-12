package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.MTMap;
import ru.happy.game.adventuredog.Tools.AssetsManagerX;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.ValuesManager;
import ru.happy.game.adventuredog.UI.Layout;

public class ThreeInRow implements Screen {

    // Глобальные объекты
    private final MainGDX game;
    private final Layout layout;
    private final GameWorld world;
    private final AssetsTool assets;
    private final ValuesManager managerV;
    private final AssetsManagerX managerX;

    // Графические ресурсы
    private final TextureAtlas atlas;

    private MTMap map;
    Vector2 cursor;

    ThreeInRow(MainGDX gdx) {
        // Копирование ссылки на глобальные объекты
        game = gdx;
        world = game.world;
        layout = game.layout;
        assets = game.assets;
        managerV = game.values;
        managerX = game.manager;

        atlas = assets.get("graphic");
        map = new MTMap(assets.getLevelContent("maps/map_001.bin"),atlas);
        cursor = new Vector2();
        world.resetMultiplexer();
        world.addProcessor(map);
        world.updateMultiplexer();
        //for (int i = 0; i < map.rows(); i++) {
        //    for (int j = 0; j < map.columns(); j++) {
        //        map.check(i,j);
        //    }
        //}
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        game.draw();
        map.draw(game,delta);
        game.end();
    }

    @Override
    public void resize(int width, int height) {

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
