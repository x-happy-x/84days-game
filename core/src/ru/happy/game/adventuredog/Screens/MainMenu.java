package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.LevelSwitcher;
import ru.happy.game.adventuredog.UI.Button;
import ru.happy.game.adventuredog.UI.ImageButton;

import static java.lang.Math.abs;
import static ru.happy.game.adventuredog.Tools.GraphicTool.getClick;
import static ru.happy.game.adventuredog.Tools.GraphicTool.toLocal;

public class MainMenu implements Screen {

    GameWorld world;
    MainGDX game;
    ArrayList<Vector2> vectors;
    ArrayList<Texture> bgs;
    Sprite bg;
    TextureAtlas texture;
    Vector2 clickedPos, click, _size_, cursor;
    boolean clicked, scroll, centered, selected, opened;
    int selectedLvl, openedLvl, levels, finishedLvl;
    float cc = 1f, ccc = 0.3f, padding, deltaT;
    TextureRegion tmp;

    Rectangle play_sizes;
    float play_delta = 0, play_step = 5f;
    ImageButton live_btn, ticket_btn, help_btn, pic_btn;
    Button play_btn, shop_btn, exit_btn, user_btn, curLevel;
    SimpleDateFormat format;
    Thread updateTime;
    String liveTime = "", helpTime = "", ticketTime = "";
    double timer;

    public MainMenu(MainGDX mainGDX) {
        game = mainGDX;
        world = game.world;
        selected = true;
        finishedLvl = world.prefs.getInteger("finished_level", 0);
        selectedLvl = openedLvl = world.prefs.getInteger("opened_level", 0);
        opened = openedLvl == finishedLvl;
        if (!opened) {
            openedLvl++;
            selectedLvl++;
        }
        deltaT = 0;
        vectors = new ArrayList<>();
        bgs = new ArrayList<>();
        texture = game.assets.get("graphic");
        bg = new Sprite();
        bg.setBounds(0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
        bg.setTexture(game.assets.get("menu_bg"));
        float w = bg.getTexture().getWidth(), h = bg.getTexture().getHeight();
        if ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() > w / h) {
            bg.setRegion(0, 0, (int) w, (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * w));
            bg.setRegion(0, (int) ((h - bg.getRegionHeight()) / 2f), bg.getRegionWidth(), bg.getRegionHeight());
        } else {
            bg.setRegion(0, 0, (int) ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * h), (int) h);
            bg.setRegion((int) ((w - bg.getRegionWidth()) / 2f), 0, bg.getRegionWidth(), bg.getRegionHeight());
        }
        click = new Vector2(0, 0);
        padding = MainGDX.HEIGHT / 40f;
        _size_ = new Vector2(MainGDX.WIDTH / 2.5f - padding, MainGDX.HEIGHT / 2f - padding);
        _size_.x = _size_.y * 2;
        levels = game.manager.getInt("levels");
        for (int i = 1; i <= levels + 1; i++) {
            vectors.add(new Vector2(i * (_size_.x + padding), _size_.y));
            if (i <= levels)
                bgs.add(new Texture(AssetsTool.getFile(AssetsTool.isExists("menu/levels/" + i + ".png") ? "menu/levels/" + i + ".png" : "menu/levels/0.png")));
        }
        cursor = new Vector2();
        world.resetMultiplexer();
        world.addProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int b) {
                toLocal(cursor, x, y);
                cursor.y = MainGDX.HEIGHT - cursor.y;
                play_btn.isClick(cursor);
                shop_btn.isClick(cursor);
                exit_btn.isClick(cursor);
                live_btn.isClick(cursor);
                ticket_btn.isClick(cursor);
                help_btn.isClick(cursor);
                return false;
            }
        }));
        world.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.LEFT && selectedLvl > 0) selectedLvl--;
                else if (keycode == Input.Keys.RIGHT && selectedLvl < vectors.size() - 1)
                    selectedLvl++;
                else if (keycode == Input.Keys.ENTER) {
                    play_btn.getAction().isClick();
                } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    Gdx.app.exit();
                }
                return false;
            }
        });
        timer = 0;
    }

    @Override
    public void show() {
        initButtons();
        //initButtonsProfile();
    }

    @Override
    public void render(float delta) {
        if (timer == 0) {
            Date now = new Date();
            liveTime = getTime(world.getUser().getLivedate(), now);
            helpTime = getTime(world.getUser().getHelpdate(), now);
            ticketTime = getTime(world.getUser().getTicketdate(), now);
            timer = 0.01;
        } else {
            timer += delta;
            if (timer > 60) timer = 0;
        }
        game.draw();
        cursor.set(getClick());
        bg.draw(world.getBatch());
        //drawProfile(delta);
        drawButtons(delta);
        drawLevels(delta);
        scrollSystem();
        game.end();
        if (ScreenAnim.getState()) {
            game.drawShape();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (ScreenAnim.show(game)) {
                if (ScreenAnim.isClosing()) LevelSwitcher.setLevel(game, selectedLvl + 1);
                else ScreenAnim.setState(false);
            }
            game.endShape();
            Gdx.gl.glDisable(GL20.GL_BLEND);
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
        for (Texture t : bgs) {
            if (t != null) t.dispose();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    ImageButton pName, pMail, pPass, pSet;

    void initButtonsProfile() {
        pMail = new ImageButton("", texture.findRegion("white_btn"), texture.findRegion("editIcon"), world);
        pMail.setSize(MainGDX.HEIGHT / 6f, MainGDX.HEIGHT / 6f);
        pMail.setPosition(MainGDX.WIDTH / 3f, -pMail.getHeight());
        pMail.setAlignI(Button.ALIGN.CENTER, Button.ALIGN.CENTER);
        pMail.setIconSize(pic_btn.getWidth() * 0.75f);
        pic_btn.move(MainGDX.WIDTH / 3f, MainGDX.HEIGHT / 2.8f, MainGDX.WIDTH / 3f, MainGDX.WIDTH / 3f, 1);
        user_btn.move(MainGDX.WIDTH / 4f, MainGDX.HEIGHT / 3f, MainGDX.WIDTH / 2f, user_btn.getHeight(), 1);
        pMail.move(pMail.getX(), MainGDX.HEIGHT / 4.5f, pMail.getWidth(), pMail.getHeight(), 1);
    }
    void drawProfile(float delta) {
        pic_btn.setCursor(cursor);
        user_btn.setCursor(cursor);
        pic_btn.draw(game, delta);
        user_btn.draw(game, delta);
        pMail.draw(game, delta);
    }

    void scrollSystem() {
        if (Gdx.input.isTouched()) {
            if (!clicked) {
                clickedPos = toLocal(Gdx.input.getX(), Gdx.input.getY());
                clicked = true;
            }
            click = toLocal(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            click.add(clickedPos.x * -1, click.y * -1);
            if (click.x > 0 && vectors.get(0).x < MainGDX.WIDTH / 2f || click.x < 0 && vectors.get(vectors.size() - 1).x > MainGDX.WIDTH / 2f) {
                for (Vector2 v : vectors) {
                    v.add(click.x * cc, click.y * cc);
                }
                selected = false;
                centered = false;
                scroll = true;
            } else {
                scroll = false;
            }
            clickedPos = toLocal(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        } else {
            if (clicked) clicked = false;
            if (scroll) {
                click.add(click.x * -ccc, 0);
                for (Vector2 v : vectors) {
                    v.add(click.x * 3f, click.y * cc);
                }
                if (abs(click.x) < ccc) scroll = false;
            } else if (!centered) {
                if (!selected) {
                    selectedLvl = 0;
                    for (int i = 0; i < vectors.size(); i++) {
                        if (abs(vectors.get(selectedLvl).x - MainGDX.WIDTH / 2f) > abs(vectors.get(i).x - MainGDX.WIDTH / 2f))
                            selectedLvl = i;
                    }
                    selected = true;
                }
                click.set(vectors.get(selectedLvl));
                if (click.x + 10 > MainGDX.WIDTH / 2f || click.x - 10 < MainGDX.WIDTH / 2f) {
                    float tmp = (MainGDX.WIDTH / 2f - click.x) / 10f;
                    for (Vector2 v : vectors) {
                        v.add(tmp, 0);
                    }
                } else {
                    centered = true;
                }
            }
        }
    }

    void drawLevels(float delta) {
        if (!opened && deltaT < 2f) deltaT += delta;
        for (int i = 0; i < vectors.size(); i++) {
            Vector2 v = vectors.get(i);
            float scalar = (MainGDX.WIDTH - abs(v.x - MainGDX.WIDTH / 2f)) / MainGDX.WIDTH;
            Vector2 sizes = new Vector2(_size_.x * scalar, _size_.y * scalar);
            if (v.x + sizes.x / 2f < 0 || v.x - sizes.x / 2f > MainGDX.WIDTH) continue;
            if (openedLvl < i || !opened && openedLvl == i && deltaT < 1.55f || i == levels)
                world.getBatch().setColor(0.1f, 0.1f, 0.1f, 1);
            else if (!opened && openedLvl == i && deltaT >= 1.55f)
                world.getBatch().setColor((1 - 2 * (2f - deltaT)), (1 - 2 * (2f - deltaT)), (1 - 2 * (2f - deltaT)), 1);
            if (i < levels)
                world.getBatch().draw(bgs.get(i), v.x - sizes.x / 2f, v.y - sizes.y / 2f, sizes.x, sizes.y);
            curLevel.setText(i == levels ? "СКОРО" : (openedLvl < i + (opened ? 0 : 1) ? "ЗАКРЫТО" : AssetsTool.encodePlatform(game.property.get("level" + (i + 1) + "Name"))), game);
            curLevel.setSize(i == levels ? sizes.y : sizes.x / 1.5f, i == levels ? sizes.y : sizes.y / 6f);
            curLevel.setTextColor(openedLvl < i + (opened ? 0 : 1) || i == levels ? Color.WHITE : Color.BLACK);
            curLevel.draw(game, delta, scalar, v.x - curLevel.getWidth() / 2f, v.y - sizes.y / 2f - (i == levels ? 0 : curLevel.getHeight() / 2f));
            if (openedLvl <= i) {
                if (openedLvl == i && deltaT >= 1.5f)
                    world.getBatch().setColor(1, 1, 1, 2 * (2f - deltaT));
                else world.getBatch().setColor(1, 1, 1, 1);
                if (!opened || openedLvl != i) {
                    if (i < levels) {
                        float height;
                        tmp = texture.findRegion("locker");
                        height = (float) tmp.getRegionHeight() / tmp.getRegionWidth() * (sizes.x / 12f);
                        world.getBatch().draw(tmp, v.x - sizes.x / 24f, v.y + (openedLvl == i && deltaT >= 1f ? height * (1 - (2 - deltaT)) : 0), sizes.x / 12f, height);
                        tmp = texture.findRegion("lock");
                        height = (float) tmp.getRegionHeight() / tmp.getRegionWidth() * (sizes.x / 8f);
                        world.getBatch().draw(tmp, v.x - sizes.x / 16f, v.y - height / 1.5f - (openedLvl == i && deltaT >= 1f ? height / 2f * (1 - (2 - deltaT)) : 0), sizes.x / 8f, height);
                    }
                    if (openedLvl == i && deltaT >= 1.5f) world.getBatch().setColor(1, 1, 1, 1);
                    if (!opened && deltaT >= 2f) {
                        opened = finishedLvl == openedLvl;
                        openedLvl += opened ? 0 : 1;
                        world.prefs.putInteger("opened_level", openedLvl);
                        world.prefs.flush();
                        if (!opened) selectedLvl++;
                        deltaT = 0f;
                    }
                }
            }
        }
    }

    void initButtons() {
        format = new SimpleDateFormat("HH:mm");
        play_btn = new Button("ИГРАТЬ", texture.findRegion("green_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (world.getLives() > 0 && selectedLvl <= openedLvl && selectedLvl < levels) {
                    ScreenAnim.setState(true);
                    ScreenAnim.setClose();
                }
            }

            @Override
            public void isSelected() {

            }
        });
        play_btn.setWidth(MainGDX.WIDTH / 3f);
        play_btn.setPosition(MainGDX.WIDTH / 2f - play_btn.getWidth() / 2f, MainGDX.HEIGHT / 20f);
        play_sizes = new Rectangle(play_btn.getX(), play_btn.getY(), play_btn.getWidth(), play_btn.getHeight());

        shop_btn = new Button("МАГАЗИН", texture.findRegion("orange_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {

            }

            @Override
            public void isSelected() {

            }
        });
        shop_btn.setWidth(MainGDX.WIDTH / 6f);
        shop_btn.setPosition(MainGDX.WIDTH - shop_btn.getWidth() - shop_btn.getHeight(), play_btn.getY());

        exit_btn = new Button("ВЫХОД", texture.findRegion("red_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                Gdx.app.exit();
            }

            @Override
            public void isSelected() {

            }
        });
        exit_btn.setWidth(MainGDX.WIDTH / 6f);
        exit_btn.setPosition(exit_btn.getHeight(), play_btn.getY());

        curLevel = new Button("", texture.findRegion("white_btn"), world, Color.BLACK);
        curLevel.setWidth(MainGDX.WIDTH / 6f);
        curLevel.setUseGL(false);

        live_btn = new ImageButton(world.getLives() + "", texture.findRegion("white_btn"), texture.findRegion("live"), world);
        live_btn.setPosition(exit_btn.getX(), MainGDX.HEIGHT - MainGDX.HEIGHT / 8f);

        ticket_btn = new ImageButton(world.getTicket() + "", texture.findRegion("white_btn"), texture.findRegion("ticket"), world);
        ticket_btn.setPosition(live_btn.getX() + live_btn.getWidth() + MainGDX.HEIGHT / 20f, live_btn.getY());

        help_btn = new ImageButton(world.getHelp() + "", texture.findRegion("white_btn"), texture.findRegion("help"), world);
        help_btn.setPosition(ticket_btn.getX() + ticket_btn.getWidth() + MainGDX.HEIGHT / 20f, ticket_btn.getY());

        user_btn = new Button(world.getUser().getName(), texture.findRegion("white_btn"), world, Color.BLACK);
        user_btn.setSize(Math.max(MainGDX.WIDTH / 5f, user_btn.getWidth()), live_btn.getHeight());
        user_btn.setOffsetX(20);
        user_btn.setAlignT(Button.ALIGN.LEFT, Button.ALIGN.CENTER);
        user_btn.setPosition(MainGDX.WIDTH - user_btn.getWidth() - exit_btn.getX(), live_btn.getY());

        help_btn.setIconSize(help_btn.getHeight() - help_btn.getOffsetIY() * 2f);
        live_btn.setIconSize(help_btn.getHeight() - help_btn.getOffsetIY() * 2f);
        ticket_btn.setIconSize(help_btn.getHeight() - help_btn.getOffsetIY() * 2f);

        pic_btn = new ImageButton("", texture.findRegion("white_btn"), texture.findRegion("live"), world);
        pic_btn.setSize(user_btn.getHeight() * 2, user_btn.getHeight() * 2);
        pic_btn.setPosition(MainGDX.WIDTH - pic_btn.getWidth() - exit_btn.getX() * 0.8f, MainGDX.HEIGHT - MainGDX.HEIGHT / 50f - pic_btn.getHeight());
        pic_btn.setAlignI(Button.ALIGN.CENTER, Button.ALIGN.CENTER);
        pic_btn.setIconSize(pic_btn.getWidth() * 0.75f);
    }

    void drawButtons(float delta) {
        play_delta += delta * play_step;
        if (play_delta <= 0 || play_delta >= 3) play_step *= -1;
        play_btn.setPosition(play_sizes.x - play_delta * 3, play_sizes.y - play_delta * 1.1f);
        play_btn.setSize(play_sizes.width + play_delta * 6, play_sizes.height + play_delta * 2.2f);
        world.getTextSize("1", 1f, GameWorld.FONTS.SMEDIAN);
        world.setText(liveTime, 0.7f, live_btn.getX() + live_btn.getWidth() / 2f, live_btn.getY() - MainGDX.HEIGHT / 35f / 2f, Color.BLACK, true, GameWorld.FONTS.SMALL);
        world.setText(ticketTime, 0.7f, ticket_btn.getX() + ticket_btn.getWidth() / 2f, ticket_btn.getY() - MainGDX.HEIGHT / 35f / 2f, Color.BLACK, true, GameWorld.FONTS.SMALL);
        world.setText(helpTime, 0.7f, help_btn.getX() + help_btn.getWidth() / 2f, help_btn.getY() - MainGDX.HEIGHT / 35f / 2f, Color.BLACK, true, GameWorld.FONTS.SMALL);
        user_btn.draw(game, delta);
        //pic_btn.draw(game, delta);
        //world.getBatch().draw(texture.findRegion("white_btn"),MainGDX.WIDTH-MainGDX.HEIGHT/6f-exit_btn.getX(),MainGDX.HEIGHT-MainGDX.HEIGHT/6f-exit_btn.getX()/3f,MainGDX.HEIGHT/6f,MainGDX.HEIGHT/6f);
        play_btn.setCursor(cursor);
        live_btn.setCursor(cursor);
        ticket_btn.setCursor(cursor);
        help_btn.setCursor(cursor);
        exit_btn.setCursor(cursor);
        shop_btn.setCursor(cursor);
        play_btn.draw(game, delta);
        live_btn.draw(game, delta);
        ticket_btn.draw(game, delta);
        help_btn.draw(game, delta);
        exit_btn.draw(game, delta);
        shop_btn.draw(game, delta);
    }

    Date getDate(Date time, Date now) {
        return new Date(time.getTime() - now.getTime() - 3 * 60 * 60 * 1000);
    }

    String getTime(Date time, Date now) {
        String date;
        Date t = getDate(time, now);
        int days = t.getDate() - 1;
        if (days == 0) date = "";
        else if (days == 1) date = "1 день ";
        else if (days < 5) date = days + " дня ";
        else date = days + " дней ";
        return date + format.format(t);
    }
}
