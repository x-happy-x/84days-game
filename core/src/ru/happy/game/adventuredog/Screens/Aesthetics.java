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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;

import java.io.File;
import java.util.ArrayList;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.AestheticsObject;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Tools.AssetsManagerX;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.GraphicTool;
import ru.happy.game.adventuredog.Tools.NetTask;
import ru.happy.game.adventuredog.Tools.ValuesManager;
import ru.happy.game.adventuredog.UI.Button;
import ru.happy.game.adventuredog.UI.ImageButton;
import ru.happy.game.adventuredog.UI.ImageView;
import ru.happy.game.adventuredog.UI.Layout;
import ru.happy.game.adventuredog.UI.TextEditor;

import static ru.happy.game.adventuredog.Tools.AssetsTool.getFile;
import static ru.happy.game.adventuredog.Tools.GraphicTool.addRectArea;
import static ru.happy.game.adventuredog.Tools.GraphicTool.toLocal;

public class Aesthetics implements Screen {

    // Глобальные объекты
    private final MainGDX game;
    private final Layout layout;
    private final GameWorld world;
    private final AssetsTool assets;
    private final ValuesManager managerV;
    private final AssetsManagerX managerX;
    private final ImageView pic_bg;
    private final ImageView live_bg;

    // Графические элементы
    private final ImageButton ticket_btn;
    private final ImageButton help_btn;
    private final ImageButton live_btn;
    private final Sprite ImageSprite;
    private final TextEditor input;
    private final Button exit;
    private final Button ok;
    private final Sprite bg;

    // Дополнительные объекты на экране
    private final Rectangle fragment_pos;
    private final ArrayList<HideSquare> hideSquares;

    // Цвета
    private final Color mainBG;
    private final Color pauseC;
    private final Color lose;
    private final Color win;
    private final NetTask netTask;
    private final int MAX_ERROR_COUNT = 3;
    /**
     * Обработка жестов уровня
     */
    private final GestureDetector levelGestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
        @Override
        public boolean longPress(float x, float y) {
            return super.longPress(x, y);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            Vector2 v = toLocal(x, y);
            v.set(v.x, MainGDX.HEIGHT - v.y);
            if (input.isEdit()) {
                if (ok.isActive()) ok.isClick(v);
                if (input.isClick(v)) {
                    input.setEdit(true);
                    Gdx.input.setOnscreenKeyboardVisible(true);
                } else {
                    if (game.view != null) Gdx.input.setOnscreenKeyboardVisible(false);
                    else input.setEdit(false);
                }
            } else {
                if (input.isClick(v)) {
                    input.setEdit(true);
                    Gdx.input.setOnscreenKeyboardVisible(true);
                } else if (input.isEdit()) {
                    if (game.view != null) Gdx.input.setOnscreenKeyboardVisible(false);
                    else input.setEdit(false);
                }
                if (ticket_btn.isActive()) ticket_btn.isClick(v);
                if (help_btn.isActive()) help_btn.isClick(v);
                if (ok.isActive()) ok.isClick(v);
                if (exit.isActive()) exit.isClick(v);
            }
            return false;
        }
    });
    // Ресурсы
    Texture image;
    TextureAtlas texture;
    // Сеть
    private AestheticsObject objAesthetic;
    private NetTask task2;
    // Параметры уровня
    private Rectangle[] menu;
    private Vector2 cursor;
    private String titleText, INFO_TEXT, menuText;
    private String[] menuList;
    private float menuText_delta;
    private float pause_delta;
    private float end_delta;
    private float INFO_TIME;
    private int CURRENT_IMAGE_NUMBER;
    private int COUNT_LOADED_IMAGES;
    private int LOADING_STATE_IMAGE;
    private int countAesthetics;
    private int selectedMenu;
    /**
     * Обработка нажатий клавиш и управления мышой в меню
     */
    private final InputAdapter menuInputAdapter = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            return super.keyDown(keycode);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            Vector2 v = toLocal(screenX, screenY);
            for (int i = menuList.length - menu.length; i < menuList.length; i++) {
                addRectArea(menu[i - (menuList.length - menu.length)], 8);
                if (menu[i - (menuList.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                    selectedMenu = i;
                    break;
                }
            }
            return false;
        }
    };
    private int scoreMusic;
    private int allErrors;
    private int guessed;
    private int errors;
    // Состояния элементов
    private boolean levelMultiplexer;
    private boolean isGuessed;
    private boolean infoLoaded;
    private boolean isPause;
    /**
     * Обработка нажатий клавиш в уровне
     */
    private final InputAdapter levelInputAdapter = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACKSPACE) {
                if (input.isEdit()) input.removeLast(game);
            } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                if (input.isEdit()) {
                    if (game.view != null) Gdx.input.setOnscreenKeyboardVisible(false);
                    else input.setEdit(false);
                } else {
                    isPause = true;
                    pause_delta = 0;
                }
            } else if (keycode == Input.Keys.ENTER) {
                if (input.isEdit()) {
                    if (game.view != null) Gdx.input.setOnscreenKeyboardVisible(false);
                    else input.setEdit(false);
                }
                ok.getAction().isClick();
            }
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            if (GameWorld.FONT_CHARACTERS.contains(String.valueOf(character)) && input.isEdit())
                input.add(character, game);
            return false;
        }
    };
    private boolean isEnd;
    private boolean isWin;
    private boolean nextLoading;
    private boolean showedWarning;
    /**
     * Обработка жестов в меню
     */
    private final GestureDetector menuGestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
        @Override
        public boolean tap(float x, float y, int count, int button) {
            Vector2 v = toLocal(x, y);
            for (int i = menuList.length - menu.length; i < menuList.length; i++) {
                addRectArea(menu[i - (menuList.length - menu.length)], 8);
                if (menu[i - (menuList.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                    selectedMenu = i;
                    switch (menuList[i].toLowerCase()) {
                        case "продолжить":
                            isPause = false;
                            pause_delta = 1;
                            break;
                        case "пропустить уровень":
                            menuText_delta = 5f;
                            menuText = world.getTicket() < 10 ? "У вас не достаточно пропусков" : "Нажмите и удерживайте чтобы пропустить";
                            break;
                        case "отчёт об ошибке":
                            if (task2 == null)
                                task2 = new NetTask(new NetTask.NetListener() {
                                    @Override
                                    public void onDownloadComplete(String msg) {
                                        menuText = "Отчёт отправлен";
                                        menuText_delta = 3f;
                                        MainGDX.clearLogger();
                                        MainGDX.write("LOG SEND");
                                    }

                                    @Override
                                    public void onDownloadFailure(String msg) {
                                        menuText_delta = 3f;
                                        menuText = "Не удалось отправить_" + msg;
                                    }
                                });
                            if (!task2.isAlive()) {
                                task2.uploadFile(game.user.getName(), game.user.getPass(), getFile("log.txt"));
                                menuText = "Отчёт отправляется...";
                                menuText_delta = 100f;
                            }
                            break;
                        case "далее":
                            if (managerX.getInt("levels") >= assets.getLevel()) {
                                ScreenAnim.setState(true);
                                ScreenAnim.setClose();
                                ScreenAnim.level = assets.getLevel() + 1;
                            } else {
                                menuText = managerV.getRandString("NO LEVEL");
                                menuText_delta = 3f;
                            }
                            break;
                        case "начать сначало":
                            if (showedWarning || isEnd) {
                                isPause = false;
                                if (!isEnd) world.useLives();
                                if (world.getLives() > 0) {
                                    ScreenAnim.setState(true);
                                    ScreenAnim.setClose();
                                    ScreenAnim.level = assets.getLevel();
                                } else {
                                    menuText = managerV.getRandString("NO LIVE");
                                    menuText_delta = 3f;
                                }
                            } else {
                                menuText = "Если вы выйдите не закончив игру,_то потеряете жизнь";
                                menuText_delta = 3f;
                                showedWarning = true;
                            }
                            break;
                        case "главное меню":
                            if (showedWarning || isEnd) {
                                isPause = false;
                                if (!isEnd) world.useLives();
                                ScreenAnim.setState(true);
                                ScreenAnim.setClose();
                                ScreenAnim.level = 0;
                            } else {
                                menuText = "Если вы выйдите не закончив игру,_то потеряете жизнь";
                                menuText_delta = 3f;
                                showedWarning = true;
                            }
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
            for (int i = menuList.length - menu.length; i < menuList.length; i++) {
                addRectArea(menu[i - (menuList.length - menu.length)], 8);
                if (menu[i - (menuList.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                    selectedMenu = i;
                    if ("пропустить уровень".equals(menuList[i].toLowerCase())) {
                        if (world.getTicket() < 10) {
                            menuText_delta = 5f;
                            menuText = "У вас не достаточно пропусков";
                        } else {
                            isPause = false;
                            world.useTicket(10);
                            world.skipLevel = true;
                            isWin = true;
                            isEnd = true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            Vector2 v = toLocal(x, y);
            for (int i = menuList.length - menu.length; i < menuList.length; i++) {
                addRectArea(menu[i - (menuList.length - menu.length)], 8);
                if (menu[i - (menuList.length - menu.length)].contains(v.x, MainGDX.HEIGHT - v.y)) {
                    selectedMenu = i;
                    break;
                }
            }
            return false;
        }
    });
    private boolean errorLoading;

    /**
     * Уровень с угадыванием эстетики
     *
     * @param mainGDX Главный игровой объект
     */
    public Aesthetics(MainGDX mainGDX) {
        // Копирование ссылки на глобальные объекты
        game = mainGDX;
        world = game.world;
        layout = game.layout;
        assets = game.assets;
        managerV = game.values;
        managerX = game.manager;

        //Получение цветов
        mainBG = Color.valueOf("#204051");
        win = Color.valueOf("#6D74FF");
        lose = Color.valueOf("#ff3300");
        pauseC = Color.valueOf("#f57d00");
        ArrayList<Color> colors = new ArrayList<>();
        for (String s : assets.getLevelContent("colors.pref").split(" "))
            colors.add(Color.valueOf("#" + s));
        // Настройка фона
        layout.ColorPrefs(colors);
        // Настройка прогресса загрузки
        layout.createProgressBar(MainGDX.WIDTH / 4f, 15, MainGDX.WIDTH / 2f, 10,
                Color.valueOf("#6E5C7F"));

        // Загрузка текстур интерфейса
        texture = assets.get(managerX.getGUI());

        // Настройка фона
        bg = new Sprite();
        bg.setBounds(0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
        bg.setRegion((Texture) assets.get("background"));
        float w = bg.getRegionWidth(), h = bg.getRegionHeight();
        if ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() > w / h) {
            bg.setRegion(0, 0, (int) w, (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * w));
            bg.setRegion(0, (int) ((h - bg.getRegionHeight()) / 2f), bg.getRegionWidth(), bg.getRegionHeight());
        } else {
            bg.setRegion(0, 0, (int) ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * h), (int) h);
            bg.setRegion((int) ((w - bg.getRegionWidth()) / 2f), 0, bg.getRegionWidth(), bg.getRegionHeight());
        }

        // Основной фон для информационного табло
        pic_bg = new ImageView(texture.findRegion("white_btn"));
        pic_bg.setRadius(10);
        pic_bg.setSize(MainGDX.WIDTH * 0.25f, MainGDX.HEIGHT * 0.95f);
        pic_bg.setPosition(MainGDX.WIDTH * 0.985f - pic_bg.getWidth(), MainGDX.HEIGHT * 0.025f);

        // Побочный фон для информационного табло
        live_bg = new ImageView(texture.findRegion("white_btn"));
        live_bg.setRadius(8);
        live_bg.setSize(pic_bg.getWidth() - pic_bg.getY() * 2, pic_bg.getHeight() * 0.35f);
        live_bg.setPosition(pic_bg.getX() + pic_bg.getY(), pic_bg.getY() + pic_bg.getHeight() - 150 - live_bg.getHeight());

        // Кнопка выход
        exit = new Button("Меню", texture.findRegion("red_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                isPause = true;
                pause_delta = 0;
            }
        });
        exit.setPosition(live_bg.getX(), pic_bg.getY() * 2);
        exit.setWidth((live_bg.getWidth() - pic_bg.getY()) / 2f);
        exit.setUseGL(false);

        // Кнопка далее
        ok = new Button("Далее", texture.findRegion("blue_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (input.getText().length() == 0) {
                    showMessage(managerV.getRandString("NO TEXT"), 3f);
                    return;
                }
                if (LOADING_STATE_IMAGE == 2) {
                    if (InputCheck(input.getText())) {
                        isGuessed = true;
                        LOADING_STATE_IMAGE = 3;
                        errors = 0;
                        showMessage(managerV.find("WIN", (scoreMusic / 20) + ""), 2f);
                        guessed++;
                    } else {
                        allErrors++;
                        errors++;
                        world.usedBonus = true;
                        int live = world.getLives();
                        showMessage(managerV.findWithParams("ERROR COUNT",
                                world.firstErrVisible ? (errors == MAX_ERROR_COUNT ? "END" : "OTHER") : "FIRST",
                                MAX_ERROR_COUNT + " провала",
                                (MAX_ERROR_COUNT - errors) + (MAX_ERROR_COUNT - errors > 1 ? " не верных ответа" : " промах"),
                                live + (live > 4 ? " жизней" : live > 1 ? " жизни" : " жизнь"),
                                (live - 1) + ""), 3f);
                        if (!world.firstErrVisible)
                            world.firstErrVisible = true;
                        if (errors == MAX_ERROR_COUNT) {
                            world.useLives();
                            if (world.getLives() == 0)
                                isEnd = true;
                            errors = 0;
                        }
                    }
                }
            }
        });
        ok.setWidth(exit.getWidth());
        ok.setPosition(exit.getX() + exit.getWidth() + pic_bg.getY() / 2f, exit.getY());
        ok.setUseGL(false);

        // Поле для ввода названия
        input = new TextEditor("Угадай с каким мультфильмом ассоциируются картинки...", texture.findRegion("white_btn"), texture.findRegion("editIcon"), world, Color.valueOf("#000000"), Color.valueOf("#333333"));
        input.addIcon(texture.findRegion("done"), texture.findRegion("close"), texture.findRegion("loadblue"));
        input.setPosition(MainGDX.HEIGHT * 0.025f, MainGDX.HEIGHT * 0.025f);
        input.setWidth(MainGDX.WIDTH - input.getX() * 3 - pic_bg.getWidth());
        input.setMaxLength(60);
        input.setUseGL(false);

        // Место для отображения изображения
        fragment_pos = new Rectangle();
        fragment_pos.setPosition(pic_bg.getY(), pic_bg.getY() * 2f + input.getHeight());
        fragment_pos.setSize(MainGDX.WIDTH - pic_bg.getY() * 2f - pic_bg.getWidth() - pic_bg.getY(),
                MainGDX.HEIGHT - pic_bg.getY() - fragment_pos.getY());

        // Изображение эстетики
        ImageSprite = new Sprite();
        ImageSprite.setRotation(0);
        ImageSprite.setBounds(fragment_pos.x, fragment_pos.y, fragment_pos.width, fragment_pos.height);

        // Сведения о жизнях
        live_btn = new ImageButton(world.getLives() + "", texture.findRegion("gray2_btn"), texture.findRegion("live"), world, Color.BLACK);
        live_btn.setIconSize(live_btn.getHeight() - live_btn.getOffsetIY() * 2f);
        live_btn.setPosition(live_bg.getX() + 5, live_bg.getY() + 10);
        live_btn.setWidth(live_bg.getWidth() - 10);
        live_btn.setUseGL(false);

        // Сведения о пропусках
        ticket_btn = new ImageButton(world.getTicket() + "", texture.findRegion("gray2_btn"), texture.findRegion("ticket"), world, Color.BLACK);
        ticket_btn.setPosition(live_btn.getX(), live_bg.getY() + live_bg.getHeight() - ticket_btn.getHeight() - 10);
        ticket_btn.setIconSize(ticket_btn.getHeight() - ticket_btn.getOffsetIY() * 2f);
        ticket_btn.setWidth(live_btn.getWidth());
        ticket_btn.setAction(new Button.Action() {
            @Override
            public void isClick() {
                if (LOADING_STATE_IMAGE == 2) {
                    if (world.getTicket() > 0) {
                        world.useTicket();
                        errors = 0;
                        showMessage(managerV.find("USE TICKET", managerX.getString(assets.getLevel(), "classLoad")), 2f);
                        isGuessed = true;
                        LOADING_STATE_IMAGE = 3;
                        guessed++;
                    } else {
                        showMessage(managerV.getRandString("NO TICKET"), 3f);
                    }
                }
            }
        });
        ticket_btn.setUseGL(false);

        // Сведения о подсказках
        help_btn = new ImageButton(world.getHelp() + "", texture.findRegion("gray2_btn"), texture.findRegion("help"), world, Color.BLACK);
        help_btn.setPosition(live_btn.getX(), live_bg.getY() + live_bg.getHeight() / 2f - help_btn.getHeight() / 2f);
        help_btn.setIconSize(help_btn.getHeight() - help_btn.getOffsetIY() * 2f);
        help_btn.setWidth(live_btn.getWidth());
        help_btn.setAction(new Button.Action() {
            @Override
            public void isClick() {
                if (LOADING_STATE_IMAGE == 2) {
                    if (world.getHelp() <= 0) {
                        showMessage(managerV.getRandString("NO HELP"), 2f);
                        return;
                    }
                    int i = 1;
                    for (HideSquare h : hideSquares) {
                        if (h.hide) {
                            showMessage(managerV.find("USE HELP", managerX.getString(assets.getLevel(), "classLoad")), 2f);
                            world.useHelp(i);
                            h.show();
                            return;
                        }
                        i++;
                    }
                    //if (!usedHelp) dialog.open();
                    //else
                    showMessage(managerV.getRandString("USED HELP"), 2f);
                    /*if (usedHelp) {
                        showText_delta = 2f;
                        showedText = "Вы уже воспользовались подсказкой";
                    } else if (game.world.getHelp() > 0) {
                        game.world.useHelp();
                        help_btn.setText("" + game.world.getHelp(), game);
                        showText_delta = 2f;
                        showedText = "Теперь вы можете слушать всю песню";
                        needCut = false;
                        usedHelp = true;
                    } else {
                        showText_delta = 3f;
                        showedText = "Ой ой у вас нет подсказок";
                    }*/
                }
            }
        });
        help_btn.setUseGL(false);

        // Другие параметры
        infoLoaded = nextLoading = isGuessed = isEnd = isWin = isPause = showedWarning = false;
        objAesthetic = new AestheticsObject();
        hideSquares = new ArrayList<>();
        hideSquares.add(new HideSquare());
        hideSquares.add(new HideSquare());
        cursor = new Vector2();

        // Настойка слушателей ввода и загрузки
        setInputLevel();
        game.addSizeChangeListener((w1, h1) -> {
            Vector2 visibleArea = toLocal(w1, h1);
            if (visibleArea.y < MainGDX.HEIGHT / 1.1) {
                if (input.isEdit()) {
                    input.move(input.getX(), visibleArea.y + input.getHeight(), input.getWidth(), input.getHeight(), 0.3f);
                    ok.move(exit.getX(), visibleArea.y + input.getHeight(), exit.getWidth() + ok.getWidth() + pic_bg.getY() / 2f, ok.getHeight(), 0.3f);
                }
            } else if ((int) visibleArea.y > MainGDX.HEIGHT - 10) {
                if (input.isEdit()) {
                    input.setEdit(false);
                    input.move(input.getX(), pic_bg.getY(), input.getWidth(), input.getHeight(), 0.3f);
                    ok.move(exit.getX() + exit.getWidth() + pic_bg.getY() / 2f, exit.getY(), exit.getWidth(), ok.getHeight(), 0.3f);
                }
            }
        });
        netTask = new NetTask(new NetTask.NetListener() {
            @Override
            public void onDownloadComplete(File result) {
                if (infoLoaded) {
                    nextLoading = false;
                    objAesthetic.aesthetics.get(COUNT_LOADED_IMAGES).success = 1;
                    COUNT_LOADED_IMAGES++;
                    managerX.add(assets.getLevel(), result.getName(), AssetsTool.removeDataPath(result.getPath()), "T");
                    if (COUNT_LOADED_IMAGES == 1) new Thread(() -> ImageLoad(0)).start();
                }
            }

            @Override
            public void onDownloadComplete(String msg) {
                if (!infoLoaded) {
                    try {
                        objAesthetic = new Json().fromJson(AestheticsObject.class, msg);
                    } catch (SerializationException e) {
                        objAesthetic.success = 0;
                        objAesthetic.message = msg;
                    }
                    if (objAesthetic.success == 0 || objAesthetic.aesthetics == null || objAesthetic.aesthetics.size() == 0) {
                        titleText = "Не удалось загрузить\n" + objAesthetic.message;
                        errorLoading = true;
                        MainGDX.write(titleText);
                    } else {
                        infoLoaded = true;
                        ArrayList<File> mFiles = new ArrayList<>();
                        countAesthetics = 0;
                        for (int i = 0; i < objAesthetic.aesthetics.size(); i++) {
                            mFiles.add(AssetsTool.getFile(objAesthetic.aesthetics.get(i).path));
                            countAesthetics++;
                        }
                        MainGDX.write(objAesthetic.message + "");
                        netTask.hardRun = true;
                        netTask.loadFiles(objAesthetic.path, "cache", mFiles.toArray());
                    }
                }
            }

            @Override
            public void onDownloadFailure(String msg) {
                MainGDX.write(msg);
            }
        });
    }

    /**
     * Первый показ окна
     */
    @Override
    public void show() {
        LOADING_STATE_IMAGE = COUNT_LOADED_IMAGES = 0;
        ScreenAnim.level = CURRENT_IMAGE_NUMBER = -1;
        netTask.API(NetTask.LEVEL_GUESS_AESTHETIC, "id", game.user.getId() + "");
    }

    /**
     * Отрисовка всего содержимого окна
     *
     * @param delta Время с последней отрисовки
     */
    @Override
    public void render(float delta) {
        cursor = GraphicTool.getClick();
        game.draw();
        bg.draw(game.getBatch());
        if (errorLoading) {
            if (Gdx.input.isTouched()) {
                ScreenAnim.setState(true);
                ScreenAnim.setClose();
                ScreenAnim.level = assets.getLevel();
                showMessage("Перезапуск уровня", 2f);
                errorLoading = false;
            } else showMessage(titleText + "\nНажмите на экран чтобы перезагрузить уровень", 3f);
        } else
            draw(delta);
        game.end();
        if (isPause) {
            if (levelMultiplexer) {
                menuList = managerV.getStrings("MENU PAUSE");
                setInputMenu();
            }
            if (pause_delta < 1f)
                pause_delta += delta;
            else
                pause_delta = 1f;
        } else if (pause_delta != 0) {
            setInputLevel();
            showedWarning = false;
            if (pause_delta > 0f)
                pause_delta -= delta;
            else
                pause_delta = 0f;
        }
        if (isEnd) {
            if (levelMultiplexer) {
                setInputMenu();
                menuList = managerV.getStrings("MENU END");
                if (!world.usedBonus) {
                    world.getBonus = true;
                    world.addHelp();
                }
            }
            if (end_delta < 1f) end_delta += delta;
            else if (end_delta > 1f) {
                end_delta = 1f;
            }
        }
        if (end_delta > 0) {
            menu = layout.drawEnd(game, menuList, isWin ? "Поздравляшки :)" : "Печалька :(", end_delta, game.interpolation, isWin, selectedMenu, isWin ? win : lose, mainBG);
            drawTextInMenu();
        } else if (pause_delta > 0) {
            menu = layout.drawPause(game, menuList, pause_delta, game.interpolation, guessed, -1, countAesthetics - guessed, selectedMenu, pauseC, mainBG);
            drawTextInMenu();
        }
        if (ScreenAnim.getState()) {
            game.drawShape();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (ScreenAnim.show(game)) {
                if (ScreenAnim.isClosing()) {
                    if (ScreenAnim.level >= 0)
                        LoadScreen.setLevel(game, ScreenAnim.level);
                    else {
                        ScreenAnim.setOpen();
                    }
                } else {
                    ScreenAnim.setState(false);
                }
            }
            game.endShape();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    /**
     * Изменение размеров окна игры
     *
     * @param width  Ширина
     * @param height Высота
     */
    @Override
    public void resize(int width, int height) {

    }

    /**
     * Пауза
     */
    @Override
    public void pause() {
        menuList = managerV.getStrings("MENU PAUSE");
        isPause = true;
        pause_delta = 1f;
    }

    /**
     * Получение фокуса игрой
     */
    @Override
    public void resume() {

    }

    /**
     * Потеря фокуса игрой
     */
    @Override
    public void hide() {
        pause();
    }

    /**
     * Уничтожение объекта с выгрузкой всех графических ресурсов и удалением кэша
     */
    @Override
    public void dispose() {
        netTask.kill();
        if (objAesthetic != null && image != null) {
            image.dispose();
        }
        AssetsTool.delete(AssetsTool.getFile("cache"));
        if (objAesthetic != null && objAesthetic.aesthetics != null && objAesthetic.aesthetics.size() > 0) {
            for (int i = 0; i < objAesthetic.aesthetics.size(); i++) {
                //AssetsTool.getFileHandler("cache/" + musics.musics.get(i).path).delete();
                game.manager.delete(objAesthetic.aesthetics.get(i).path);
            }
        }
    }

    /**
     * Отрисовка содержимого уровня
     *
     * @param delta Время с прошлой отрисовки
     */
    private void draw(float delta) {
        pic_bg.draw(game);
        live_bg.draw(game);

        world.setText("Угадано:", 1.3f, pic_bg.getX() + 20, pic_bg.getY() + pic_bg.getHeight() - 20, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        world.setText("Всего:", 1.3f, pic_bg.getX() + 20, pic_bg.getY() + pic_bg.getHeight() - 20 - world.getSizes()[1] * 2f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        world.setText("Ошибок:", 1.3f, pic_bg.getX() + 20, pic_bg.getY() + pic_bg.getHeight() - 20 - world.getSizes()[1] * 4f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);

        world.setText("" + guessed, 1.3f, pic_bg.getX() + pic_bg.getWidth() - 20 - world.getTextSize("" + guessed, 1.3f, GameWorld.FONTS.SMEDIAN)[0], pic_bg.getY() + pic_bg.getHeight() - 20, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        world.setText("" + countAesthetics, 1.3f, pic_bg.getX() + pic_bg.getWidth() - 20 - world.getTextSize("" + countAesthetics, 1.3f, GameWorld.FONTS.SMEDIAN)[0], pic_bg.getY() + pic_bg.getHeight() - 20 - world.getSizes()[1] * 2f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        world.setText("" + allErrors, 1.3f, pic_bg.getX() + pic_bg.getWidth() - 20 - world.getTextSize("" + allErrors, 1.3f, GameWorld.FONTS.SMEDIAN)[0], pic_bg.getY() + pic_bg.getHeight() - 20 - world.getSizes()[1] * 4f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);

        world.setText("Чтобы использовать подсказку нажмите на неё", 0.5f, pic_bg.getX() + pic_bg.getWidth() / 2f, live_bg.getY() - 20, Color.DARK_GRAY, true, GameWorld.FONTS.SMALL);
        if (!world.isLockedAction()) {
            ok.setCursor(cursor);
            exit.setCursor(cursor);
            input.setCursor(cursor);
            live_btn.setCursor(cursor);
            help_btn.setCursor(cursor);
            ticket_btn.setCursor(cursor);
        }
        live_btn.setText("" + world.getLives(), game);
        help_btn.setText("" + world.getHelp(), game);
        ticket_btn.setText("" + world.getTicket(), game);

        live_btn.draw(game, delta);
        help_btn.draw(game, delta);
        ticket_btn.draw(game, delta);

        if (!input.isEdit()) {
            ok.draw(game, delta);
            exit.draw(game, delta);
            input.draw(game, delta);
        }

        if (!assets.updating() && LOADING_STATE_IMAGE == 1) {
            if (image != null) {
                image.dispose();
            }
            image = assets.get(objAesthetic.aesthetics.get(CURRENT_IMAGE_NUMBER).path);
            ImageSprite.setRegion(image);
            ImageSprite.setRotation(0);
            float x, y, w, h;
            if ((float) image.getWidth() / image.getHeight() > fragment_pos.getWidth() / fragment_pos.getHeight()) {
                w = fragment_pos.getWidth();
                h = (float) image.getHeight() / image.getWidth() * w;
            } else {
                h = fragment_pos.getHeight();
                w = (float) image.getWidth() / image.getHeight() * h;
            }
            x = fragment_pos.x + (fragment_pos.width - w) / 2f;
            y = fragment_pos.y + (fragment_pos.height - h) / 2f;
            ImageSprite.setBounds(x, y, w, h);
            LOADING_STATE_IMAGE = 2;
        } else if (LOADING_STATE_IMAGE == -1) {
            showMessage("Не удалось загрузить песню", 3f);
        }
        if (image != null) {
            ImageSprite.draw(world.getBatch());
            for (HideSquare h : hideSquares)
                h.draw();
        }
        if (INFO_TIME > 0f) {
            if (!nextLoading) INFO_TIME -= delta;
            String[] ss = INFO_TEXT.replace("_", "\n").split("\n");
            for (int i = 0; i < ss.length; i++) {
                world.setText(ss[ss.length - 1 - i],
                        1f,
                        fragment_pos.x + fragment_pos.width / 2f,
                        fragment_pos.y + MainGDX.HEIGHT / 20f + world.getSizes()[1] * 1.5f * i,
                        Color.WHITE,
                        Color.BLACK,
                        true,
                        GameWorld.FONTS.SMEDIAN);
            }
        } else if (isGuessed) {
            isGuessed = false;
            if (!nextLoading)
                new Thread(() -> ImageLoad(CURRENT_IMAGE_NUMBER + 1)).start();
        }
        if (input.isEdit()) {
            ok.draw(game, delta);
            exit.draw(game, delta);
            input.draw(game, delta);
        }
    }

    /**
     * Включить обработку ввода для уровня
     */
    public void setInputLevel() {
        if (!levelMultiplexer) {
            world.resetMultiplexer();
            world.addProcessor(levelInputAdapter);
            world.addProcessor(levelGestureDetector);
            world.updateMultiplexer();
            levelMultiplexer = true;
        }
    }

    /**
     * Включить обработку ввода для меню паузы
     */
    public void setInputMenu() {
        if (levelMultiplexer) {
            world.resetMultiplexer();
            world.addProcessor(menuInputAdapter);
            world.addProcessor(menuGestureDetector);
            world.updateMultiplexer();
            levelMultiplexer = false;
        }
    }

    /**
     * Показ текста в меню паузы
     */
    private void drawTextInMenu() {
        if (menuText_delta > 0) {
            menuText_delta -= Gdx.graphics.getDeltaTime();
            game.draw();
            String[] ss = menuText.split("_");
            for (int i = 0; i < ss.length; i++) {
                world.setText(ss[i], 1f, menu[menu.length - 1].x + menu[menu.length - 1].width / 2f, menu[menu.length - 1].y - menu[menu.length - 1].height / 2f - world.getSizes()[1] * i, Color.WHITE, Color.BLACK, true, GameWorld.FONTS.SMALL);
            }
            game.end();
        }
    }

    /**
     * Проверка ввода
     *
     * @param x Введённый текст
     * @return Значение, верно-ли введён текст
     */
    public boolean InputCheck(String x) {
        x = AssetsTool.replace(x.toLowerCase().replace("\n", "").replace("ё", "е"),
                GameWorld.getSymbolsCharset(), " ", true).trim();
        String[] names = AssetsTool.replace(objAesthetic.aesthetics.get(CURRENT_IMAGE_NUMBER).name.toLowerCase().replace("ё", "е"),
                GameWorld.getSymbolsCharset(), " ", true).split("\n");
        scoreMusic = 100;
        for (String name : names) {
            name = name.trim();
            MainGDX.write("Name check: " + name + " - " + x);
            if (name.equalsIgnoreCase(x.trim()))
                return true;
            scoreMusic -= 50 / names.length;
        }
        return false;
    }

    /**
     * Загрузка изображения эстетики в буффер
     *
     * @param N Номер эстетики
     */
    private void ImageLoad(int N) {
        if (N == objAesthetic.aesthetics.size()) {
            isWin = true;
            isEnd = true;
            return;
        } else if (N == COUNT_LOADED_IMAGES) {
            showMessage("Следующая песня ещё не загружена\n" + AssetsTool.formatSize(netTask.cur_progress) + " / " + AssetsTool.formatSize(netTask.max_progress), 1f);
            nextLoading = true;
            return;
        }
        CURRENT_IMAGE_NUMBER = N;
        assets.load(objAesthetic.aesthetics.get(N).path);
        input.setText("", game);
        String HS = objAesthetic.aesthetics.get(N).hide;
        if (HS != null && HS.length() > 0 && HS.contains("-") && HS.contains(":")) {
            hideSquares.get(0).set(HS.split(" ")[0], texture.findRegion("locked_block"));
            hideSquares.get(1).set(HS.split(" ")[1], texture.findRegion("locked_block"));
        } else {
            hideSquares.get(0).hide = false;
            hideSquares.get(1).hide = false;
        }
        LOADING_STATE_IMAGE = 1;
    }

    /**
     * Вывод текста на картинке снизу
     *
     * @param text Текст
     * @param time Время его отображения в секундах
     */
    private void showMessage(String text, float time) {
        INFO_TEXT = text;
        INFO_TIME = time;
    }

    /**
     * Прямоугольник который закрывает последние 2 картинки
     */
    class HideSquare {
        public int x1, y1, x2, y2;
        public boolean hide;
        private TextureRegion region;

        /**
         * Прямоугольник который закрывает последние 2 картинки, без инициализации
         */
        public HideSquare() {
        }

        /**
         * Установка новых координат и текстуры
         *
         * @param value  Значение типа: x1:y1-x2:y2 для построения прямоугольника
         * @param region Текстура которая будет отображаться в этом прямоугольнике
         */
        public void set(String value, TextureRegion region) {
            this.region = region;
            hide = true;
            x1 = Integer.parseInt(value.split("-")[0].split(":")[0]);
            x2 = Integer.parseInt(value.split("-")[1].split(":")[0]);
            y1 = Integer.parseInt(value.split("-")[0].split(":")[1]);
            y2 = Integer.parseInt(value.split("-")[1].split(":")[1]);
        }

        /**
         * Показать закрытую область
         */
        public void show() {
            hide = false;
        }

        /**
         * Нарисовать прямоугольник с текстом
         */
        public void draw() {
            if (hide) {
                float x = x1, y = image.getHeight() - y2, w = x2 - x1, h = y2 - y1;
                w *= ImageSprite.getWidth() / image.getWidth();
                h *= ImageSprite.getHeight() / image.getHeight();
                x *= ImageSprite.getWidth() / image.getWidth();
                y *= ImageSprite.getHeight() / image.getHeight();
                world.getBatch().draw(region, ImageSprite.getX() + x, ImageSprite.getY() + y, w, h);
                world.setText("СКРЫТО", 0.7f, ImageSprite.getX() + x + w / 2f, ImageSprite.getY() + y + h / 2f, Color.WHITE, true, GameWorld.FONTS.SMALL);
            }
        }
    }
}