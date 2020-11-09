package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.Door;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.Person;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.UI.Button;
import ru.happy.game.adventuredog.UI.ImageButton;
import ru.happy.game.adventuredog.UI.ImageView;
import ru.happy.game.adventuredog.UI.Layout;
import ru.happy.game.adventuredog.UI.TextEditor;

import static ru.happy.game.adventuredog.Tools.GraphicTool.addRectArea;
import static ru.happy.game.adventuredog.Tools.GraphicTool.getClick;
import static ru.happy.game.adventuredog.Tools.GraphicTool.toLocal;
import static ru.happy.game.adventuredog.Tools.LevelSwitcher.setLevel;

public class Guessing implements Screen {

    //Objects
    MainGDX game;
    GameWorld world;
    Door doors;
    Layout layout;
    Sprite bgRoom;
    Interpolation interpolation = Interpolation.exp5;

    //Static parameters
    public static int step;
    public static int[] roomSize;

    //Params
    int h, w, h0, w0, h_max, w_max, select, gcount, allcount,
            showedC, errCount, selectedItemMenu = 0, score, errorType;

    float ratioRoom, ratioDisplay, errTimer, pauseTime, textInMenuTime;

    boolean isPan, selected, getError, getWin, leftMenu, backspacePressed,
            usedHelp, multiplexerLevel, paused, pauseShow, isTextInMenu;

    String //inputTextN, inputTextM,
            errorText, textInMenu, textInMenu2;
    String[] menulist, errList = new String[]{};

    ArrayList<Person> persons;
    Map<String, String[]> texts;

    Vector2 point1, point_orig, center;

    Rectangle[] menu;
    Rectangle //inputNR, inputMR, okBTN, cancelBTN, helpBTN,
            //ticketBTN, tmpRect, lives, helps, tickets,
            guesses, pauseBTN, persCoord, bgCoord, clickTmp;

    //Resources
    TextureAtlas atlas, menuAtlas;
    //TextureAtlas.AtlasRegion livesR, helpsR, ticketsR;

    public Guessing(MainGDX mainGDX) {
        // Инициализация пустых переменных
        gcount = 0;
        allcount = 0;
        errCount = 0;
        step = 0;
        score = 0;
        texts = new HashMap<>();
        point1 = new Vector2();
        point_orig = new Vector2();
        center = new Vector2();
        persCoord = new Rectangle();
        bgCoord = new Rectangle();
        // Сохраняем ссылки к объектам игры и мира
        game = mainGDX;
        world = game.world;
        layout = game.layout;
        // Грузим ресурсы
        atlas = (TextureAtlas) game.assets.get("graphic");
        menuAtlas = (TextureAtlas) game.assets.get(game.manager.getGUI());
        // Настраиваем фон
        bgRoom = new Sprite();
        bgRoom.setRegion((Texture) game.assets.get("bg"));
        bgRoom.setPosition(0, 0);
        bgRoom.setSize(MainGDX.WIDTH, MainGDX.HEIGHT);
        // Определяем размеры фона
        roomSize = new int[]{bgRoom.getRegionWidth(), bgRoom.getRegionHeight()};
        w = roomSize[0];
        h = roomSize[1];
        // Находим соотношение для фона и экрана
        ratioDisplay = Gdx.graphics.getWidth() * 1f / Gdx.graphics.getHeight();
        ratioRoom = roomSize[0] * 1f / roomSize[1];
        // Подстаиваем фон под разрешение экрана
        if (ratioDisplay > ratioRoom) {
            bgRoom.setRegion(0, 0, roomSize[0], (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * roomSize[0]));
        } else {
            bgRoom.setRegion(0, 0, (int) ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * roomSize[1]), roomSize[1]);
        }
        // Максимальные размеры фона для текущего разрешения экрана
        h_max = bgRoom.getRegionHeight();
        w_max = bgRoom.getRegionWidth();
        // Подгружаем надписи
        String textTmp = game.assets.getLevelFile("texts.pref");
        if (!AssetsTool.isAndroid()) textTmp = AssetsTool.encodeString(textTmp, false);
        for (String i : textTmp.split("\n\n")) {
            texts.put(i.substring(i.indexOf("[") + 1, i.indexOf("]")), i.substring(i.indexOf("\n") + 1).split("\n"));
        }
        menulist = texts.get("END");
        // Добавляем персонажей
        persons = new ArrayList<>();
        textTmp = game.assets.getLevelFile("persons.prop");
        for (String person : textTmp.split("\n\n")) {
            Map<String, String> params = AssetsTool.getParamFromFile(person);
            persons.add(new Person(atlas.findRegion(params.get("pic")), Integer.parseInt(params.get("x")), Integer.parseInt(params.get("y")),
                    Integer.parseInt(params.get("w")), Integer.parseInt(params.get("h")), params.get("name"), params.get("mult"), Integer.parseInt(params.get("layer"))));
            allcount++;
        }
        Collections.reverse(persons);
        // Добавляем двери
        doors = new Door();
        textTmp = game.assets.getLevelFile("doors.prop");
        for (String anim : textTmp.split("\n\n")) {
            Map<String, String> params = AssetsTool.getParamFromFile(anim);
            doors.append(params, atlas);
        }
        // Включаем обработку ввода
        setInputLevel();
        // Инициализируем необходимые элементы для метода drawLayout()
        createLayout();
    }

    private void setInputLevel() {
        multiplexerLevel = true;
        world.resetMultiplexer();
        world.addProcessor(new GestureDetector(new GestureDetector.GestureListener() {
            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                point_orig.set(bgRoom.getRegionX(), bgRoom.getRegionY());
                w = bgRoom.getRegionWidth();
                h = bgRoom.getRegionHeight();
                h0 = h;
                w0 = w;
                point1.set(x, y);
                return false;
            }

            @Override
            public boolean tap(float x, float y, int count, int button) {
                if (getWin) return true;
                Vector2 v = toLocal(x, y);
                v.y = MainGDX.HEIGHT - v.y;
                if (!selected) {
                    if (gcount >= showedC) {
                        String nDoor = doors.isClicked(v.x, v.y);
                        if (nDoor != null) {
                            doors.open(nDoor);
                            return true;
                        }
                    }
                    for (int i = persons.size() - 1; i >= 0; i--) {
                        if (persons.get(i).collide(v.x, v.y)) {
                            usedHelp = false;
                            getError = false;
                            selected = true;
                            select = i;
                            if (persons.get(select).getName().equalsIgnoreCase("none"))
                                name.setText(persons.get(select).getName(), game);
                            break;
                        }
                    }
                } else {
                    cancel_btn.isClick(v, 10);
                    ok_btn.isClick(v, 10);
                    help_btn.isClick(v, 10);
                    ticket_btn.isClick(v, 10);
                    if (!multi.isClick(v, 10)) {
                        multi.setEdit(false);
                        if (!name.isEdit()) Gdx.input.setOnscreenKeyboardVisible(false);
                    }
                    if (!name.isClick(v, 10)) {
                        name.setEdit(false);
                        if (!multi.isEdit()) Gdx.input.setOnscreenKeyboardVisible(false);
                    }
                    if (addArea(pauseBTN, 50).contains(v)) paused = true;
                    Gdx.input.setOnscreenKeyboardVisible(name.isEdit() || multi.isEdit());
                }
                return false;
            }

            @Override
            public boolean longPress(float x, float y) {
                return false;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                return false;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                if (getWin) return true;
                if (!selected) {
                    if (!isPan) {
                        point1.set(x, y);
                        point_orig.set(bgRoom.getRegionX(), bgRoom.getRegionY());
                        isPan = true;
                    }
                    int xx = (int) (point_orig.x + point1.x - x);
                    int yy = (int) (point_orig.y + point1.y - y);
                    bgRoom.setRegion(xx > 0 ? xx + w < roomSize[0] ? xx : roomSize[0] - w : 0,
                            yy > 0 ? yy + h < roomSize[1] ? yy : roomSize[1] - h : 0, w, h);
                }
                return false;
            }

            @Override
            public boolean panStop(float x, float y, int pointer, int button) {
                isPan = false;
                return false;
            }

            @Override
            public boolean zoom(float initialDistance, float distance) {
                return false;
            }

            @Override
            public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
                if (!selected) {
                    float initDist = (float) Math.sqrt(Math.pow(initialPointer2.x - initialPointer1.x, 2) + Math.pow(initialPointer2.y - initialPointer1.y, 2));
                    float dist = (float) Math.sqrt(Math.pow(pointer2.x - pointer1.x, 2) + Math.pow(pointer2.y - pointer1.y, 2));
                    float ratio = initDist / dist;
                    center.set(point_orig.x + w0 / 2, point_orig.y + h0 / 2);
                    //new Vector2(point_orig.x+(pointer1.x+pointer2.x)/2,point_orig.y+(pointer1.y+pointer2.y)/2);
                    h = (int) (h0 * ratio);
                    w = (int) (w0 * ratio);
                    h = h > h_max ? h_max : Math.max(h, h_max / 5);
                    w = w > w_max ? w_max : Math.max(w, w_max / 5);
                    center.set(center.x + w / 2 < roomSize[0] ? center.x - w / 2 >= 0 ? center.x - w / 2 : 0 : roomSize[0] - w, center.y + h / 2 < roomSize[1] ? center.y - h / 2 >= 0 ? center.y - h / 2 : 0 : roomSize[1] - h);
                    bgRoom.setRegion((int) (center.x), (int) (center.y), w, h);
                }
                return false;
            }

            @Override
            public void pinchStop() {
                if (!selected) {
                    point_orig.set(bgRoom.getRegionX(), bgRoom.getRegionY());
                }
            }
        }));
        world.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                backspacePressed = keycode == Input.Keys.BACKSPACE;
                if (backspacePressed) {
                    if (multi.isEdit()) multi.removeLast(game);
                    else if (name.isEdit()) name.removeLast(game);
                } else {
                    if (name.isEdit() || multi.isEdit()) {
                        switch (keycode) {
                            case Input.Keys.ENTER:
                                if (multi.isEdit() && !name.getText().equalsIgnoreCase("none")) {
                                    name.setEdit(true);
                                    multi.setEdit(false);
                                } else {
                                    Gdx.input.setOnscreenKeyboardVisible(false);
                                }
                                break;
                            case Input.Keys.ESCAPE:
                                Gdx.input.setOnscreenKeyboardVisible(false);
                                break;
                        }
                    } else {
                        switch (keycode) {
                            case Input.Keys.BACK:
                            case Input.Keys.ESCAPE:
                                if (selected && step == 60) {
                                    step = 100;
                                    getError = false;
                                    multi.setText("", game);
                                    name.setText("", game);
                                } else {
                                    paused = !paused;
                                }
                                break;
                            case Input.Keys.MINUS:
                                center.set(bgRoom.getRegionX() + bgRoom.getRegionWidth() / 2f, bgRoom.getRegionY() + bgRoom.getRegionHeight() / 2f);
                                h = (int) (bgRoom.getRegionHeight() * 1.2f);
                                w = (int) (bgRoom.getRegionWidth() * 1.2f);
                                h = h > h_max ? h_max : Math.max(h, h_max / 5);
                                w = w > w_max ? w_max : Math.max(w, w_max / 5);
                                center.set(center.x + w / 2 < roomSize[0] ? center.x - w / 2 >= 0 ? center.x - w / 2 : 0 : roomSize[0] - w, center.y + h / 2 < roomSize[1] ? center.y - h / 2 >= 0 ? center.y - h / 2 : 0 : roomSize[1] - h);
                                bgRoom.setRegion((int) (center.x), (int) (center.y), w, h);
                                break;
                            case Input.Keys.PLUS:
                                center.set(bgRoom.getRegionX() + bgRoom.getRegionWidth() / 2f, bgRoom.getRegionY() + bgRoom.getRegionHeight() / 2f);
                                h = (int) (bgRoom.getRegionHeight() * 0.8f);
                                w = (int) (bgRoom.getRegionWidth() * 0.8f);
                                h = h > h_max ? h_max : Math.max(h, h_max / 5);
                                w = w > w_max ? w_max : Math.max(w, w_max / 5);
                                center.set(center.x + w / 2 < roomSize[0] ? center.x - w / 2 >= 0 ? center.x - w / 2 : 0 : roomSize[0] - w, center.y + h / 2 < roomSize[1] ? center.y - h / 2 >= 0 ? center.y - h / 2 : 0 : roomSize[1] - h);
                                bgRoom.setRegion((int) (center.x), (int) (center.y), w, h);
                                break;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (GameWorld.FONT_CHARACTERS.contains(String.valueOf(character))) {
                    if (usedHelp) usedHelp = false;
                    if (multi.isEdit()) multi.add(character, game);
                    else if (name.isEdit()) name.add(character, game);
                }
                return false;
            }
        });
        world.updateMultiplexer();
    }

    private void setInputMenu() {
        selectedItemMenu = 0;
        multiplexerLevel = false;
        isTextInMenu = false;
        world.resetMultiplexer();
        world.addProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                Vector2 v = toLocal(screenX, screenY);
                for (int i = menulist.length - menu.length; i < menulist.length; i++) {
                    addRectArea(menu[i - (menulist.length - menu.length)], 8);
                    if (menu[i - (menulist.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                        selectedItemMenu = i;
                        break;
                    }
                }
                return false;
            }
        });
        world.addProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int button) {
                Vector2 v = toLocal(x, y);
                for (int i = menulist.length - menu.length; i < menulist.length; i++) {
                    addRectArea(menu[i - (menulist.length - menu.length)], 8);
                    if (menu[i - (menulist.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                        selectedItemMenu = i;
                        switch (menulist[i].toLowerCase()) {
                            case "продолжить":
                                paused = false;
                                break;
                            case "пропустить уровень":
                                isTextInMenu = true;
                                textInMenu = "Для этого требуется 10 пропусков";
                                textInMenu2 = "Нажмите и держите чтобы использовать";
                                textInMenuTime = 0;
                                break;
                            case "далее":
                                if (Integer.parseInt(game.property.get("levels")) >= game.assets.getLevel()) {
                                    ScreenAnim.setState(true);
                                    ScreenAnim.setClose();
                                    ScreenAnim.level = game.assets.getLevel() + 1;
                                } else {
                                    isTextInMenu = true;
                                    textInMenu = "У нас больше не осталось уровней :(";
                                    textInMenu2 = "Не волнуйтесь, мы уже работаем над этим :)";
                                    textInMenuTime = 0;
                                }
                                break;
                            case "начать сначало":
                                paused = false;
                                if (world.getLives() > 0) {
                                    ScreenAnim.setState(true);
                                    ScreenAnim.setClose();
                                    ScreenAnim.level = game.assets.getLevel();
                                } else {
                                    isTextInMenu = true;
                                    textInMenu = "У вас закончились все жизни :(";
                                    textInMenu2 = "Не волнуйтесь, отдохните и завтра продолжим :)";
                                    textInMenuTime = 0;
                                }
                                break;
                            case "главное меню":
                                paused = false;
                                ScreenAnim.setState(true);
                                ScreenAnim.setClose();
                                ScreenAnim.level = 0;
                                break;
                        }
                        break;
                    }
                }
                return false;
            }

            @Override
            public boolean longPress(float x, float y) {
                Vector2 v = toLocal(x, y);
                for (int i = menulist.length - menu.length; i < menulist.length; i++) {
                    addRectArea(menu[i - (menulist.length - menu.length)], 8);
                    if (menu[i - (menulist.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                        selectedItemMenu = i;
                        switch (menulist[i].toLowerCase()) {
                            case "пропустить уровень":
                                if (world.getTicket() < 10) {
                                    isTextInMenu = true;
                                    textInMenu = "У вас не достаточно пропусков";
                                    textInMenu2 = "";
                                    textInMenuTime = 0;
                                } else {
                                    paused = false;
                                    world.useTicket(10);
                                    world.skipLevel = true;
                                    step = 100;
                                    select = allcount - 1;
                                }
                                break;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                Vector2 v = toLocal(x, y);
                for (int i = menulist.length - menu.length; i < menulist.length; i++) {
                    addRectArea(menu[i - (menulist.length - menu.length)], 8);
                    if (menu[i - (menulist.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                        selectedItemMenu = i;
                        break;
                    }
                }
                return false;
            }
        }));
        world.updateMultiplexer();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (getError) {
            errTimer += delta;
            if (errTimer > 3) {
                getError = false;
            }
        }
        game.draw();
        if (selected) {
            if (step == 0) {
                Rectangle r = persons.get(select).getRect();
                float height, width;
                if (r.height > r.width) {
                    height = r.height;
                    width = r.height * ratioDisplay;
                } else {
                    width = r.width * 2f;
                    height = width * (float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
                }
                persCoord.set(r.x - (r.height > r.width ? (width * 1.1f / 2f - r.width) / 2f : width * .05f), r.y - (!(r.height > r.width) ? (height * 1.1f - r.height) / 2f : height * .05f), width * 1.1f, height * 1.1f);
                leftMenu = !(persCoord.x + persCoord.width > roomSize[0]);
                if (!leftMenu) {
                    persCoord.setPosition(persCoord.x - persCoord.width / 2f, persCoord.y);
                }
                bgCoord.set(bgRoom.getRegionX(), bgRoom.getRegionY(), bgRoom.getRegionWidth(), bgRoom.getRegionHeight());
            }
            if (step < 60) {
                bgRoom.setRegion((int) interpolation.apply(bgCoord.x, persCoord.x, step / 58f), (int) interpolation.apply(bgCoord.y, persCoord.y, step / 58f),
                        (int) interpolation.apply(bgCoord.width, persCoord.width, step / 58f), (int) interpolation.apply(bgCoord.height, persCoord.height, step / 58f));
                bgFix();
                step += 2;
            } else if (step >= 100 && step < 160) {
                bgRoom.setRegion((int) interpolation.apply(bgCoord.x, persCoord.x, 1 - (step - 100) / 60f), (int) interpolation.apply(bgCoord.y, persCoord.y, 1 - (step - 100) / 60f),
                        (int) interpolation.apply(bgCoord.width, persCoord.width, 1 - (step - 100) / 60f), (int) interpolation.apply(bgCoord.height, persCoord.height, 1 - (step - 100) / 60f));
                bgFix();
                step += 2;
            } else if (step == 160) {
                selected = false;
                step = 0;
            }
        }
        bgRoom.draw(world.getBatch());
        doors.setRegion(bgRoom.getRegionX(), bgRoom.getRegionY(), bgRoom.getRegionWidth(), bgRoom.getRegionHeight());
        doors.draw(world.getBatch());
        for (Person i : persons) {
            i.setRegion(bgRoom.getRegionX(), bgRoom.getRegionY(), bgRoom.getRegionWidth(), bgRoom.getRegionHeight());
            Rectangle r = i.getScreenRect();
            if (r.x + r.width > 0 && r.x < MainGDX.WIDTH && r.y < MainGDX.HEIGHT && r.y + r.height > 0)
                i.draw(world.getBatch(), 1f);
        }
        if (showedC == 0) for (Person i : persons) if (i.isShowed()) showedC++;
        if (selected) {
            drawLayout();
        } else {
            //world.getBatch().draw(menuAtlas.findRegion("live"),0,0,0,0,50,50,100,100,90);
        }
        if (getWin) {
            errTimer += delta;
            if (errTimer > 2) {
                getWin = false;
                errTimer = 0;
            } else {
                game.end();
                Color tmp = new Color(0, 0, 0, 2.1f - errTimer);
                Gdx.gl.glEnable(GL30.GL_BLEND);
                game.drawShape();
                layout.drawRectangle(game.renderer, tmp, 0, MainGDX.HEIGHT / 6f, MainGDX.WIDTH, MainGDX.HEIGHT / 8f);
                game.endShape();
                Gdx.gl.glDisable(GL30.GL_BLEND);
                game.draw();
                tmp.set(1, 1, 1, tmp.a);
                world.setText(errorText, 1f, MainGDX.WIDTH / 20f + (MainGDX.WIDTH - MainGDX.WIDTH / 10f) / 2f, MainGDX.HEIGHT / 6f + MainGDX.HEIGHT / 16f, tmp, true, GameWorld.FONTS.SMEDIAN);
                persons.get(select).drawToCenter(world.getBatch(), errTimer, MainGDX.HEIGHT / 6f + MainGDX.HEIGHT / 8f);
            }
        } else if (!selected && world.skipLevel && allcount != gcount && step == 0) {
            isTextInMenu = false;
            world.resetMultiplexer();
            selected = false;
            Person p = persons.get(select--);
            if (!Door.opened.contains(p.layer)) {
                doors.open("door" + (p.layer - 1));
            }
            if (p.getGuessed() == 0) {
                p.scoreName = 0;
                p.scoreMulti = 0;
                p.setVisible(false);
                p.setGuess(true);
                if (p.scoreName + p.scoreMulti == 200) score++;
                gcount++;
                step = 100;
            }
        } else if (!selected && world.skipLevel) {
            step += 20;
            if (step > 160) step = 0;
        }
        game.end();
        if (ScreenAnim.getState()) {
            Gdx.gl.glEnable(GL30.GL_BLEND);
            game.drawShape();
            if (ScreenAnim.show(game)) {
                if (ScreenAnim.isClosing())
                    setLevel(game, ScreenAnim.level);
                ScreenAnim.setState(false);
            }
            game.endShape();
            Gdx.gl.glDisable(GL30.GL_BLEND);
        }
        if (paused) {
            if (!pauseShow) {
                menulist = texts.get("PAUSE");
                setInputMenu();
                pauseTime = 0;
                pauseShow = true;
            }
            if (pauseTime < 1) pauseTime += Gdx.graphics.getDeltaTime();
            if (pauseTime > 1) pauseTime = 1;
            menu = layout.drawPause(game, menulist, pauseTime, interpolation, gcount, score, allcount - gcount, selectedItemMenu, pauseC, mainBG);
            drawTextInMenu();
        } else if (pauseShow) {
            if (!ScreenAnim.getState() && pauseTime > 0) pauseTime -= Gdx.graphics.getDeltaTime();
            else if (ScreenAnim.getState()) pauseTime = 1 - ScreenAnim.getAlpha();
            if (pauseTime < 0) {
                pauseShow = false;
                menulist = texts.get("END");
                setInputLevel();
                pauseTime = 0;
            }
            menu = layout.drawPause(game, menulist, pauseTime, interpolation, gcount, score, allcount - gcount, selectedItemMenu, pauseC, mainBG);
            drawTextInMenu();
        }
        if ((gcount == allcount || world.getLives() <= 0) && !getWin) {
            if (world.prefs.getInteger("finished_level", 0) == 0) {
                world.prefs.putInteger("finished_level", game.assets.getLevel());
                world.prefs.flush();
            }
            if (score == allcount && !world.usedBonus && !world.prefs.getBoolean("bonusLevel" + game.assets.getLevel(), false)) {
                world.prefs.putBoolean("bonusLevel" + game.assets.getLevel(), true);
                world.addHelp();
                world.getBonus = true;
            } else {
                world.prefs.putBoolean("bonusLevel" + game.assets.getLevel(), true);
            }
            if (multiplexerLevel) setInputMenu();
            if (!ScreenAnim.getState() && errTimer < 1) errTimer += Gdx.graphics.getDeltaTime();
            else if (ScreenAnim.getState()) errTimer = 1 - ScreenAnim.getAlpha();
            if (errTimer > 1) errTimer = 1;
            else if (errTimer < 0) errTimer = 0;
            menu = layout.drawEnd(game, menulist, "ОТЛИЧНО УГАДАНО: " + score, errTimer, interpolation, world.getLives() > 0, selectedItemMenu, world.getLives() > 0 ? win : lose, mainBG);
            drawTextInMenu();
        }
    }

    private void drawTextInMenu() {
        if (isTextInMenu) {
            textInMenuTime += Gdx.graphics.getDeltaTime();
            if (textInMenuTime > 3) {
                isTextInMenu = false;
                textInMenuTime = 0;
            } else {
                game.draw();
                world.setText(textInMenu, 1f, menu[menu.length - 1].x + menu[menu.length - 1].width / 2f, menu[menu.length - 1].y - menu[menu.length - 1].height / 2f, Color.WHITE, true, GameWorld.FONTS.SMALL);
                world.setText(textInMenu2, 1f, menu[menu.length - 1].x + menu[menu.length - 1].width / 2f, menu[menu.length - 1].y - menu[menu.length - 1].height / 2f - world.getSizes()[1] * 1.4f, Color.WHITE, true, GameWorld.FONTS.SMALL);
                game.end();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        ratioDisplay = (float) width / height;
        ratioRoom = roomSize[0] * 1f / roomSize[1];
        if (ratioDisplay > ratioRoom) {
            bgRoom.setRegion(0, 0, roomSize[0], (int) ((float) height / width * roomSize[0]));
        } else {
            bgRoom.setRegion(0, 0, (int) (ratioDisplay * roomSize[1]), roomSize[1]);
        }
        h_max = bgRoom.getRegionHeight();
        w_max = bgRoom.getRegionWidth();
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

    Color mainBG, darker, win, lose, pauseC;

    private void createLayout() {
        mainBG = Color.valueOf("#204051");
        win = Color.valueOf("#6D74FF");
        lose = Color.valueOf("#ff3300");
        pauseC = Color.valueOf("#f57d00");
        darker = new Color(0.1f, 0.1f, 0.1f, 0);

        menu_bg = new ImageView(menuAtlas.findRegion("blueL_btn"));
        live_bar = new ImageButton(world.getLives() + "", menuAtlas.findRegion("white_btn"), menuAtlas.findRegion("live"), world);
        help_bar = new ImageButton(world.getLives() + "", menuAtlas.findRegion("white_btn"), menuAtlas.findRegion("help"), world);
        ticket_bar = new ImageButton(world.getLives() + "", menuAtlas.findRegion("white_btn"), menuAtlas.findRegion("ticket"), world);
        help_btn = new Button("ПОДСКАЗКА", menuAtlas.findRegion("orange_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (!usedHelp && world.getHelp() > 0) {
                    Person p = persons.get(select);
                    name.setText(formatterString(p.getName()), game);
                    multi.setText(formatterString(p.getMulti()), game);
                    world.useHelp();
                    usedHelp = true;
                    errList = texts.get("HELP")[MathUtils.random(texts.get("HELP").length - 1)].split("#");
                } else {
                    errList = texts.get(!usedHelp ? "NOT HELP" : "USED HELP")[MathUtils.random(texts.get(!usedHelp ? "NOT HELP" : "USED HELP").length - 1)].split("#");
                }
                getError = true;
                errorType = 1;
                errTimer = 0;
            }

            @Override
            public void isSelected() {

            }
        });
        ticket_btn = new Button("ПРОПУСТИТЬ", menuAtlas.findRegion("orange_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (world.getTicket() > 0) {
                    Person p = persons.get(select);
                    multi.setText("", game);
                    name.setText("", game);
                    p.scoreName = 100;
                    p.scoreMulti = 100;
                    p.setVisible(false);
                    p.setGuess(true);
                    if (p.scoreName + p.scoreMulti == 200) score++;
                    gcount++;
                    world.useTicket();
                    getWin = true;
                    errCount = 0;
                    step = 100;
                    errTimer = 3;
                } else {
                    errList = texts.get("NOT HELP")[MathUtils.random(texts.get("NOT HELP").length - 1)].split("#");
                    getError = true;
                    errorType = 1;
                    errTimer = 0;
                }
            }

            @Override
            public void isSelected() {

            }
        });
        ok_btn = new Button("ПРОВЕРИТЬ", menuAtlas.findRegion("green_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (multi.getText().length() > 0 && name.getText().length() > 0) {
                    Person p = persons.get(select);
                    if (p.checkName(name.getText()) && p.checkMulti(multi.getText())) {
                        multi.setText("", game);
                        name.setText("", game);
                        int s = p.scoreName + p.scoreMulti;
                        errorText = texts.get(!usedHelp ? "WIN" : "WIN_HELP")[usedHelp ? MathUtils.random(texts.get("WIN_HELP").length - 1) : s == 200 ? 0 : s >= 185 ? 1 : s >= 170 ? 2 : s >= 150 ? 3 : 4];
                        p.setVisible(false);
                        p.setGuess(true);
                        if (p.scoreName + p.scoreMulti == 200) score++;
                        gcount++;
                        getWin = true;
                        errCount = 0;
                        step = 100;
                        usedHelp = false;
                    } else {
                        errCount++;
                        errorType = 2;
                        String errList2 = texts.get("ERROR")[MathUtils.random(texts.get("ERROR").length - 1)];
                        if (!world.firstErrVisible) {
                            world.firstErrVisible = true;
                            errList2 += "#" + texts.get("ERRCOUNT")[0];
                        } else if (errCount < 5) {
                            errList2 += "#" + texts.get("ERRCOUNT")[1].replace("{0}", "" + (5 - errCount));
                        } else {
                            errList2 += "#" + texts.get("ERRCOUNT")[2].replace("{0}", world.getLives() + " жизн" + (world.getLives() == 1 ? "ь" : "и")).replace("{1}", "" + (world.getLives() - 1));
                        }
                        errList = errList2.split("#");
                        if (errCount == 5) {
                            errCount = 0;
                            world.useLives();
                        }
                        getError = true;
                        if (world.getLives() == 0) {
                            getError = false;
                            errTimer = 0;
                        }
                    }
                } else if (multi.getText().length() > 0) {
                    errList = new String[]{"Вы не ввели имя персонажа"};
                    getError = true;
                    errorType = 0;
                } else {
                    errList = new String[]{"Вы не ввели название мульфильма"};
                    getError = true;
                    errorType = 0;
                }
                errTimer = 0;
            }

            @Override
            public void isSelected() {

            }
        });
        cancel_btn = new Button("ОТМЕНА", menuAtlas.findRegion("red_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                multi.setText("", game);
                name.setText("", game);
                getError = false;
                usedHelp = false;
                step = 100;
            }

            @Override
            public void isSelected() {

            }
        });
        name = new TextEditor("Имя персонажа", menuAtlas.findRegion("white_btn"), menuAtlas.findRegion("editIcon"), world, Color.BLACK, new TextEditor.Action() {
            @Override
            public void isInput(String text) {
                usedHelp = false;
            }

            @Override
            public void isClick() {
                name.setEdit(true);
                multi.setEdit(false);
                Gdx.input.setOnscreenKeyboardVisible(true);
            }

            @Override
            public void isSelected() {

            }
        });
        multi = new TextEditor("Название мультфильма", menuAtlas.findRegion("white_btn"), menuAtlas.findRegion("editIcon"), world, Color.BLACK, new TextEditor.Action() {
            @Override
            public void isInput(String text) {
                usedHelp = false;
            }

            @Override
            public void isClick() {
                name.setEdit(false);
                multi.setEdit(true);
                Gdx.input.setOnscreenKeyboardVisible(true);
            }

            @Override
            public void isSelected() {

            }
        });
        menu_bg.setRect(0, MainGDX.WIDTH / 200f, MainGDX.WIDTH / 2f - MainGDX.WIDTH / 50f, MainGDX.HEIGHT - MainGDX.WIDTH / 100f);
        multi.setRect(0, MainGDX.HEIGHT - MainGDX.WIDTH / 10f, menu_bg.getWidth() - MainGDX.WIDTH / 20f, multi.getHeight() * 1.2f);
        name.setRect(0, multi.getY() - multi.getHeight() * 1.7f, multi.getWidth(), multi.getHeight());
        ok_btn.setRect(0, MainGDX.HEIGHT / 15f, name.getWidth() / 2f - MainGDX.WIDTH / 40f, name.getHeight());
        ticket_btn.setRect(0, ok_btn.getY() + ok_btn.getHeight() * 1.7f, ok_btn.getWidth(), ok_btn.getHeight());
        cancel_btn.setRect(0, ok_btn.getY(), ok_btn.getWidth(), ok_btn.getHeight());
        help_btn.setRect(0, cancel_btn.getY() + cancel_btn.getHeight() * 1.7f, cancel_btn.getWidth(), cancel_btn.getHeight());
        live_bar.setY(live_bar.getHeight() / 3f);
        help_bar.setY(help_bar.getHeight() / 3f);
        ticket_bar.setY(ticket_bar.getHeight() / 3f);
        menu_bg.setRadius(name.getHeight() / 2f);
        name.setMaxLength(40);
        multi.setMaxLength(40);
        guesses = new Rectangle(0, MainGDX.HEIGHT - MainGDX.HEIGHT / 40f - ticket_bar.getHeight(), MainGDX.WIDTH / 6f, ticket_bar.getHeight() / 1.2f);
        pauseBTN = new Rectangle(0, MainGDX.HEIGHT - ticket_bar.getHeight() - MainGDX.HEIGHT / 40f, 25, ticket_bar.getHeight() / 1.2f);
        clickTmp = new Rectangle();
    }

    private Rectangle addArea(Rectangle r, int x) {
        return clickTmp.set(r.x - x, r.y - x, r.width + x * 2, r.height + x * 2);
    }

    TextEditor name, multi;
    Button ok_btn, cancel_btn, help_btn, ticket_btn;
    ImageButton live_bar, help_bar, ticket_bar;
    ImageView menu_bg;

    private void drawLayout() {
        // Меню с кнопками
        float xx = interpolation.apply(!leftMenu ? (MainGDX.DISPLAY_CUTOUT_MODE == 1 ? 35 : 0) + MainGDX.WIDTH / 100f : MainGDX.WIDTH / 2f + MainGDX.WIDTH / 100f,
                !leftMenu ? -MainGDX.WIDTH / 2f : MainGDX.WIDTH, (step <= 60 ? 60 - step : step - 100) / 60f);
        float xxx = MainGDX.WIDTH * 0.5f - xx + (leftMenu ? MainGDX.HEIGHT / 40f : (MainGDX.DISPLAY_CUTOUT_MODE == 1 ? 60 : 0));
        Vector2 v = getClick();

        menu_bg.setX(xx);
        multi.setX(menu_bg.getX() + MainGDX.WIDTH / 40f);
        name.setX(multi.getX());
        ok_btn.setX(name.getX() + name.getWidth() / 2f + MainGDX.WIDTH / 40f);
        cancel_btn.setX(multi.getX());
        ticket_btn.setX(ok_btn.getX());
        help_btn.setX(multi.getX());

        live_bar.setX(xxx + MainGDX.HEIGHT / 20f);
        help_bar.setX(xxx + MainGDX.WIDTH / 4f - live_bar.getWidth() / 2f);
        ticket_bar.setX(xxx + MainGDX.WIDTH / 2f - help_bar.getWidth() - MainGDX.WIDTH / 50f);
        live_bar.setText(world.getLives() + "", game);
        help_bar.setText(world.getHelp() + "", game);
        ticket_bar.setText(world.getTicket() + "", game);

        live_bar.setCursor(v);
        help_bar.setCursor(v);
        ticket_bar.setCursor(v);
        help_btn.setCursor(v);
        ticket_btn.setCursor(v);
        ok_btn.setCursor(v);
        cancel_btn.setCursor(v);
        name.setCursor(v);
        multi.setCursor(v);
        ok_btn.setActive(multi.getText().length() > 0 && name.getText().length() > 0);
        menu_bg.draw(game);
        live_bar.draw(game, Gdx.graphics.getDeltaTime());
        help_bar.draw(game, Gdx.graphics.getDeltaTime());
        ticket_bar.draw(game, Gdx.graphics.getDeltaTime());
        ok_btn.draw(game, Gdx.graphics.getDeltaTime());
        cancel_btn.draw(game, Gdx.graphics.getDeltaTime());
        help_btn.draw(game, Gdx.graphics.getDeltaTime());
        ticket_btn.draw(game, Gdx.graphics.getDeltaTime());
        if (!persons.get(select).getName().equalsIgnoreCase("none"))
            name.draw(game, Gdx.graphics.getDeltaTime());
        multi.draw(game, Gdx.graphics.getDeltaTime());
        game.end();

        game.drawShape();
        guesses.setX(leftMenu ? xxx + MainGDX.WIDTH / 2f - MainGDX.HEIGHT / 20f - MainGDX.WIDTH / 6f : xxx + MainGDX.HEIGHT / 20f);
        layout.drawRectangle(game.renderer, mainBG.sub(darker), guesses, 5);
        if (gcount > 0)
            layout.drawRectangle(game.renderer, win, guesses.x, guesses.y, guesses.width / allcount * gcount, guesses.height, 5);
        // Пауза
        pauseBTN.setX(leftMenu ? xxx + MainGDX.HEIGHT / 20f : xxx + MainGDX.WIDTH / 2f - MainGDX.HEIGHT / 20f - pauseBTN.width);
        layout.drawPause(game.renderer, 1, 4, pauseBTN, Color.WHITE, mainBG);
        mainBG.add(darker);
        game.endShape();
        // Текст и картинки
        game.draw();
        if (getError) {
            float y = (persons.get(select).getName().equalsIgnoreCase("none") ? multi.getY() : name.getY()) - MainGDX.HEIGHT / 40f;
            GameWorld.FONTS f = GameWorld.FONTS.SMEDIAN;
            for (String errText : errList) {
                //world.setText(errText, 1f, inputMR.x + inputMR.width / 2f + 1,  y- 1, Color.valueOf("#000000"), true, false, f);
                world.setText(errText, 1f, multi.getX() + multi.getWidth() / 2f, y, lose, darker, true, false, f);
                y -= world.getSizes()[1] * 2f;
                f = GameWorld.FONTS.SMALL;
            }
        }

        world.setText(gcount + "", 1f, guesses.getX() + guesses.getWidth() * 0.9f - world.getTextSize(gcount + "", 1f, GameWorld.FONTS.SMEDIAN)[0], guesses.getY() + guesses.getHeight() / 2f, Color.WHITE, false, true, GameWorld.FONTS.SMEDIAN);
        world.setText("Угадано:", 1f, guesses.getX() + MainGDX.HEIGHT / 40f, guesses.getY() + guesses.getHeight() / 2f, Color.WHITE, false, true, GameWorld.FONTS.SMEDIAN);

        world.setText("Название мультфильма:", 1f, multi.getX() + MainGDX.WIDTH / 100f, multi.getY() + multi.getHeight() * 1.35f, Color.WHITE, false, GameWorld.FONTS.SMALL);
        if (!persons.get(select).getName().equalsIgnoreCase("none"))
            world.setText("Имя персонажа:", 1f, name.getX() + MainGDX.WIDTH / 100f, name.getY() + name.getHeight() * 1.35f, Color.WHITE, false, GameWorld.FONTS.SMALL);
    }

    private String formatterString(String i) {
        String fstr = formatterString(i, " ");
        if (world.getTextSize(fstr, 1f, GameWorld.FONTS.SMEDIAN)[0] > name.getWidth() * 0.95f)
            fstr = formatterString(i, "");
        return fstr;
    }

    private String formatterString(String i, String j) {
        String n = "";
        String m = "";
        for (String s : i.split(j)) {
            if (world.getTextSize(n + s, 1f, GameWorld.FONTS.SMEDIAN)[0] > name.getWidth() * 0.95f) {
                m += n + "\n";
                n = "";
            }
            n += s + j;
        }
        m += n.trim();
        return m;
    }

    private void bgFix() {
        int x = bgRoom.getRegionX(), y = bgRoom.getRegionY(), w = bgRoom.getRegionWidth(), h = bgRoom.getRegionHeight();
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (w > w_max) {
            w = h_max;
            h = w * (h_max / w_max);
        }
        if (h > h_max) {
            h = h_max;
            w = h * (w_max / h_max);
        }
        if (x + w > roomSize[0]) {
            x -= x + w - roomSize[0];
        }
        if (y + h > roomSize[1]) {
            y -= y + h - roomSize[1];
        }
        bgRoom.setRegion(x, y, w, h);
    }
}
