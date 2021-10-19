package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Screen;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.Mario;
import ru.happy.game.adventuredog.Tools.AssetsManagerX;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.ValuesManager;
import ru.happy.game.adventuredog.UI.Layout;

public class NoMario implements Screen {

    // Кол-во ошибок
    private final int MAX_ERROR_COUNT = 3;

    // Глобальные объекты
    private final MainGDX game;
    private final Layout layout;
    private final GameWorld world;
    private final AssetsTool assets;
    private final ValuesManager managerV;
    private final AssetsManagerX managerX;

    Mario mario;

    public NoMario(MainGDX mainGDX) {
        this.game = mainGDX;
        this.layout = game.layout;
        this.world = game.world;
        this.assets = game.assets;
        this.managerV = game.values;
        this.managerX = game.manager;
        //this.mario = new Mario(mainGDX);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

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
