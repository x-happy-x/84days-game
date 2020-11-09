package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.Dog;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Tools.LevelSwitcher;

public class MainScreen implements Screen {
    Texture bg;
    MainGDX game;
    GameWorld world;
    Music music;
    Dog dog;
    boolean touch;

    public MainScreen(MainGDX mainGDX) {
        game = mainGDX;
        world = game.world;
        dog = new Dog(mainGDX, 0, MainGDX.HEIGHT / 10f, 0, MainGDX.WIDTH / 4, "Dog", world.getCamera());
        world.resetMultiplexer();
        world.addProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int button) {
                return false;
            }
        }));
        world.addProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACK) {
                    ScreenAnim.level = 0;
                    ScreenAnim.setState(true);
                    ScreenAnim.setClose();
                }
                return false;
            }
        });
        //bg = new Texture(Gdx.files.internal("background.jpg"));
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        world.getBatch().begin();
        //world.getBatch().draw(bg, 0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
        world.setText("FPS: " + Gdx.graphics.getFramesPerSecond(), 2f, MainGDX.WIDTH / 2f, 20f, new Color(1, 1, 1, 1), true);
        dog.draw((SpriteBatch) world.getBatch());
        world.draw();
        if (ScreenAnim.getState()) {
            Gdx.gl.glEnable(GL30.GL_BLEND);
            game.drawShape();
            if (ScreenAnim.show(game)) {
                if (ScreenAnim.isClosing()) {
                    LevelSwitcher.setLevel(game, ScreenAnim.level);
                }
                ScreenAnim.setState(false);
            }
            game.endShape();
            Gdx.gl.glDisable(GL30.GL_BLEND);
        }
        world.getBatch().end();
        if (!touch && (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Input.Keys.ENTER))) {
            touch = true;
            dog.startAnim(!dog.isAnimated());
        } else if (!Gdx.input.isTouched()) {
            touch = false;
        }
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
