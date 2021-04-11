package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;

import java.io.File;
import java.util.ArrayList;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.Interfaces.ElementsUI;
import ru.happy.game.adventuredog.Interfaces.VideoPlayer;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.LoadedMusic;
import ru.happy.game.adventuredog.Tools.AssetsManagerX;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.GraphicTool;
import ru.happy.game.adventuredog.Tools.NetTask;
import ru.happy.game.adventuredog.Tools.ValuesManager;
import ru.happy.game.adventuredog.UI.Button;
import ru.happy.game.adventuredog.UI.Dialog;
import ru.happy.game.adventuredog.UI.ImageButton;
import ru.happy.game.adventuredog.UI.ImageView;
import ru.happy.game.adventuredog.UI.Layout;
import ru.happy.game.adventuredog.UI.PlayerSlider;
import ru.happy.game.adventuredog.UI.TextEditor;
import ru.happy.game.adventuredog.UI.TextView;

import static ru.happy.game.adventuredog.Tools.AssetsTool.getFile;
import static ru.happy.game.adventuredog.Tools.GraphicTool.addRectArea;
import static ru.happy.game.adventuredog.Tools.GraphicTool.toLocal;

public class MusicLevel implements Screen {

    // Кол-во ошибок
    private final int MAX_ERROR_COUNT = 3;

    // Глобальные объекты
    private final MainGDX game;
    private final Layout layout;
    private final GameWorld world;
    private final AssetsTool assets;
    private final ValuesManager managerV;
    private final AssetsManagerX managerX;

    // Графические элементы
    private final ArrayList<Button> buttons;
    private final ImageButton ticket_btn;
    private final ImageButton help_btn;
    private final ImageButton live_btn;
    private final PlayerSlider slider;
    private final ImageView fragment;
    private final ImageView live_bg;
    private final ImageView pic_bg;
    private final ImageView title;
    private final TextEditor input;
    private final Dialog dialog;
    private final Button exit;
    private final Button next;
    private final Button ok;
    //
    private final Rectangle fragment_pos;
    private final NetTask netTask;
    LoadedMusic musics;
    String titleText, INFO_TEXT, menuText;
    Vector2 cursor;
    ArrayList<Float> rewindList;
    ArrayList<String[]> quest;
    String[] menuList;
    Rectangle[] menu;
    Color mainBG, pauseC, lose, win;
    // Ресурсы
    TextureAtlas texture;
    Music music, cutting;
    private NetTask task2;
    private int musicN;
    private int guessed;
    private int musicCount;
    private int selectedQuest;
    private int selectedMenu;
    InputAdapter menuInputAdapter = new InputAdapter() {
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
    private int newQuest;
    private int errors;
    private int musicL;
    private float musicP;
    private float pause_delta;
    private float INFO_TIME;
    private float end_delta;
    private float menuText_delta;
    // Установить слушатели для меню
    // Состояния элементов
    private int VIDEO_LOAD_STATE, MUSIC_LOAD_STATE, MUSIC_CUT_STATE, SCREEN_STATE;
    private boolean infoLoaded;
    private boolean isGuessed;
    private boolean levelMultiplexer;
    private boolean isChanging;
    private boolean errorLoading;
    private boolean typeSelected;
    // Слушатель жестов
    GestureDetector levelGestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
        @Override
        public boolean longPress(float x, float y) {
            return super.longPress(x, y);
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            Vector2 v = toLocal(x, y);
            v.set(v.x, MainGDX.HEIGHT - v.y);
            if (typeSelected) {
                if (input.isEdit()) {
                    if (ok.isActive()) ok.isClick(v);
                    if (input.isClick(v)) {
                        input.setEdit(true);
                        Gdx.input.setOnscreenKeyboardVisible(true);
                    } else {
                        if (game.view != null) Gdx.input.setOnscreenKeyboardVisible(false);
                        else input.setEdit(false);
                    }
                } else if (!dialog.isClick(v)) {
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
                    if (slider.isClicked(v)) slider.setPaused(!slider.isPaused());
                }
            } else {
                if (next.isActive()) next.isClick(v);
                if (buttons != null) for (Button b : buttons) if (b.isActive()) b.isClick(v);
            }
            return false;
        }
    });
    private boolean isPause;
    // Слушатели ввода
    InputAdapter levelInputAdapter = new InputAdapter() {
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
            } else if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                if (input.isEdit()) {
                    if (keycode == Input.Keys.ENTER) {
                        if (game.view != null) Gdx.input.setOnscreenKeyboardVisible(false);
                        else input.setEdit(false);
                    }
                } else if (input.getText().length() > 0 && keycode == Input.Keys.ENTER) {
                    ok.getAction().isClick();
                } else if (music.isPlaying()) {
                    music.pause();
                    slider.setPaused(true);
                } else {
                    music.play();
                    slider.setPaused(false);
                }
            }
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            if (GameWorld.FONT_CHARACTERS.contains(String.valueOf(character)) && input.isEdit()) {
                input.add(character, game);
            }
            return false;
        }
    };
    private boolean isEnd;
    private boolean isWin;
    private boolean nextLoading;
    private boolean usedHelp;
    private boolean showedWarning;
    GestureDetector menuGestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {
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
                                if (music != null) {
                                    slider.setPaused(true);
                                    music.setVolume(0);
                                    music.pause();
                                }
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
                                    if (music != null) {
                                        slider.setPaused(true);
                                        music.pause();
                                        music.setVolume(0);
                                    }
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
                                if (music != null) {
                                    slider.setPaused(true);
                                    music.setVolume(0);
                                    music.pause();
                                }
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
    private boolean video_showing;

    public MusicLevel(MainGDX mainGDX) {
        // Копирование ссылки на глобальные объекты
        game = mainGDX;
        world = game.world;
        layout = game.layout;
        assets = game.assets;
        managerV = game.values;
        managerX = game.manager;

        // Параметры игры
        cursor = new Vector2();
        netTask = new NetTask();
        rewindList = new ArrayList<>();
        quest = new ArrayList<>();
        musics = new LoadedMusic();
        buttons = new ArrayList<>();
        ScreenAnim.level = musicN = -1;
        nextLoading = infoLoaded = isGuessed = typeSelected = isEnd = isWin = isPause
                = usedHelp = showedWarning = false;
        INFO_TIME = menuText_delta = pause_delta = end_delta = musicCount = guessed = 0;

        // Установка состояний по умолчанию
        VIDEO_LOAD_STATE = MUSIC_LOAD_STATE = MUSIC_CUT_STATE = SCREEN_STATE = 0;

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
        layout.createProgressBar(MainGDX.WIDTH / 4f, 15, MainGDX.WIDTH / 2f, 10, Color.valueOf("#6E5C7F"));

        // Загрузка ресурсов
        texture = assets.get(managerX.getGUI());
        cutting = assets.get("sound_cut");
        cutting.setLooping(false);

        // Кнопка далее
        ok = new Button("Далее", texture.findRegion("blue_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (MUSIC_LOAD_STATE == 2) {
                    if (musicCheck(input.getText())) {
                        isGuessed = true;
                        MUSIC_LOAD_STATE = 3;
                        errors = 0;
                        showMessage(managerV.find("WIN", (scoreMusic / 20) + ""), 3f);
                        guessed++;
                    } else {
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
        ok.setWidth(100);
        ok.setPosition(MainGDX.WIDTH - 40 - ok.getWidth(), 30);
        ok.setUseGL(false);

        // Кнопка next
        next = new Button("НАЧАТЬ СЕЙЧАС", texture.findRegion("square_darkgray_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (musicL > 0) {
                    musicLoad(0);
                    ScreenAnim.setClose();
                    ScreenAnim.setState(true);
                }
            }
        });
        next.setPosition(MainGDX.WIDTH - 20 - next.getWidth(), 15);
        next.setUseGL(false);

        // Кнопка выход
        exit = new Button("Меню", texture.findRegion("red_btn"), world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                isPause = true;
                pause_delta = 0;
            }
        });
        exit.setPosition(40, 30);
        exit.setWidth(100);
        exit.setUseGL(false);

        // Поле для ввода названия
        input = new TextEditor("Введите название песни...", texture.findRegion("white_btn"), texture.findRegion("editIcon"), world, Color.valueOf("#000000"), Color.valueOf("#333333"));
        input.addIcon(texture.findRegion("done"), texture.findRegion("close"), texture.findRegion("loadblue"));
        input.setPosition(exit.getX() + exit.getWidth() + 20, exit.getY());
        input.setWidth(MainGDX.WIDTH - input.getX() - ok.getWidth() - 60);
        input.setMaxLength(60);
        input.setUseGL(false);

        // Слайдер для музыки
        slider = new PlayerSlider(texture.findRegion("play_bg"), texture.findRegion("white_btn"), texture.findRegion("play2"), texture.findRegion("pause2"), Color.valueOf("#444444"));
        slider.setSize(MainGDX.WIDTH * 0.75f, 10);
        slider.setPosition(MainGDX.WIDTH * 0.125f, input.getY() + input.getHeight() * 1.8f);
        slider.setSizeSlider(50, 50);
        slider.setActive(false);
        slider.setValue(0);

        // Основной фон для информационного табло
        pic_bg = new ImageView(texture.findRegion("gray1_btn"));
        pic_bg.setRadius(20);
        pic_bg.setPosition(MainGDX.WIDTH * 0.7f, MainGDX.HEIGHT * 0.4f - 20);
        pic_bg.setSize(MainGDX.WIDTH - pic_bg.getX() - exit.getX(), MainGDX.HEIGHT * 0.6f);

        // Побочный фон для информационного табло
        live_bg = new ImageView(texture.findRegion("gray1_btn"));
        live_bg.setRadius(18);
        live_bg.setPosition(pic_bg.getX() + 5, pic_bg.getY() + 20);
        live_bg.setSize(pic_bg.getWidth() - 10, pic_bg.getHeight() * 0.6f);

        // Фон для постера
        fragment = new ImageView(texture.findRegion("gray4_btn"));
        fragment.setRadius(20);
        fragment.setPosition(exit.getX(), pic_bg.getY());
        fragment.setSize(MainGDX.WIDTH - exit.getX() * 2f - pic_bg.getWidth() - 20, pic_bg.getHeight());
        fragment_pos = new Rectangle(fragment.getX(), fragment.getY(), fragment.getWidth(), fragment.getHeight());

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
                if (MUSIC_LOAD_STATE == 2) {
                    if (world.getTicket() > 0) {
                        world.useTicket();
                        errors = 0;
                        showMessage(managerV.find("USE TICKET", assets.getLevel() + ""), 3f);
                        isGuessed = true;
                        MUSIC_LOAD_STATE = 3;
                        VIDEO_LOAD_STATE = -1;
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
                if (MUSIC_LOAD_STATE == 2) {
                    if (world.getHelp() <= 0) {
                        showMessage(managerV.getRandString("NO HELP"), 2f);
                        return;
                    }
                    if (!usedHelp) dialog.open();
                    else showMessage(managerV.getRandString("USED HELP"), 2f);
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

        // Фон для заголовка при опросе
        title = new ImageView(texture.findRegion("square_darkgray_btn"));
        title.setRadius(50);
        title.setSize(MainGDX.WIDTH + 50, MainGDX.WIDTH / 10f);
        title.setPosition(-25, -20);

        // Переход к первому вопросу
        changeQuest(0);

        // Установка слушателей
        setInputLevel();
        game.addSizeChangeListener((w1, h1) -> {
            Vector2 visibleArea = toLocal(w1, h1);
            if (visibleArea.y < MainGDX.HEIGHT / 1.1) {
                if (input.isEdit()) {
                    input.move(exit.getX(), visibleArea.y + input.getHeight(), MainGDX.WIDTH - exit.getX() - ok.getWidth() - 60, input.getHeight(), 0.3f);
                    ok.move(MainGDX.WIDTH - 40 - ok.getWidth(), visibleArea.y + ok.getHeight(), ok.getWidth(), ok.getHeight(), 0.3f);
                }
            } else if ((int) visibleArea.y > MainGDX.HEIGHT - 10) {
                if (input.isEdit()) {
                    input.setEdit(false);
                    input.move(40 + exit.getWidth() + 20, 30, MainGDX.WIDTH - (exit.getX() + exit.getWidth() + 20) - ok.getWidth() - 60, input.getHeight(), 0.3f);
                    ok.move(MainGDX.WIDTH - 40 - ok.getWidth(), 30, ok.getWidth(), ok.getHeight(), 0.3f);
                }
            }
        });
        netTask.setListener(new NetTask.NetListener() {
            @Override
            public void onDownloadComplete(File result) {
                if (infoLoaded) {
                    nextLoading = false;
                    musics.musics.get(musicL).success = 1;
                    musicL++;
                    managerX.add(assets.getLevel(), result.getName(), AssetsTool.removeDataPath(result.getPath()), "M");
                    if (!typeSelected && musics.musics.get(musics.musics.size() - 1).path.contains(result.getName())) {
                        musicLoad(0);
                        ScreenAnim.setClose();
                        ScreenAnim.setState(true);
                    }
                }
            }

            @Override
            public void onDownloadComplete(String msg) {
                if (!infoLoaded) {
                    try {
                        musics = new Json().fromJson(LoadedMusic.class, msg);
                    } catch (SerializationException e) {
                        musics.success = 0;
                        musics.message = msg;
                    }
                    infoLoaded = !(musics.success == 0 || musics.musics == null || musics.musics.size() == 0);
                    if (!infoLoaded) {
                        titleText = "Не удалось загрузить";
                        errorLoading = true;
                    } else {
                        ArrayList<File> mFiles = new ArrayList<>();
                        musicCount = 0;
                        for (int i = 0; i < musics.musics.size(); i++) {
                            mFiles.add(AssetsTool.getFile(musics.musics.get(i).path));
                            musicCount++;
                        }
                        netTask.hardRun = true;
                        netTask.loadFiles(musics.path, "cache", mFiles.toArray());
                    }
                }
            }

            @Override
            public void onDownloadFailure(String msg) {
                showMessage("Не удалось загрузить музыку", 5f);
            }
        });
        if (game.video != null)
            game.video.setListener(new VideoPlayer.PlayerListener() {
                @Override
                public void onStop() {
                    isGuessed = false;
                    video_showing = false;
                    VIDEO_LOAD_STATE = 0;
                    isPause = false;
                    pause_delta = 0f;
                    slider.setPaused(true);
                    slider.setValue(slider.getMinValue());
                    SCREEN_STATE = 0;
                    setInputLevel();
                    fragment.move(fragment_pos, 1f);
                    fragment.setAction(new Button.Action() {
                        @Override
                        public void onCompletionAction() {
                            MainGDX.write("VIDEO STOP: " + musics.musics.get(musicN).video + " (" + musics.musics.get(musicN).artist + " - " + musics.musics.get(musicN).title + ")");
                            new Thread(() -> musicLoad(musicN + 1)).start();
                            fragment.setAction(null);
                        }
                    });
                }

                @Override
                public void onStart() {
                    video_showing = true;
                    MainGDX.write("VIDEO START: " + musics.musics.get(musicN).video + " (" + musics.musics.get(musicN).artist + " - " + musics.musics.get(musicN).title + ")");
                    Gdx.input.setOnscreenKeyboardVisible(false);
                    input.setEdit(false);
                    input.setText("", game);
                }

                @Override
                public void onError(String error) {
                    super.onError(error);
                    MainGDX.write("VIDEO ERROR: " + musics.musics.get(musicN).video + " (" + musics.musics.get(musicN).artist + " - " + musics.musics.get(musicN).title + ") " + error);
                    showMessage("Не удалось загрузить видео\n" + error, 2f);
                    onStop();
                }
            });
        TextureRegion bg = texture.findRegion("white_btn"),
                button = texture.findRegion("green_btn");
        Color buttonColor = Color.WHITE, textColor = Color.BLACK;
        dialog = (Dialog) new Dialog(bg, new Button.Action() {
        }).setSize(MainGDX.WIDTH / 2.4f, 310).center();
        // Диалог
        {
            dialog.addElement(
                    // Заголовок
                    new TextView(
                            game,
                            "Выберите подсказку",
                            dialog.getX() + dialog.getWidth() / 2f,
                            dialog.getY() + dialog.getHeight() - 15,
                            ElementsUI.ALIGN.CENTER,
                            ElementsUI.ALIGN.TOP,
                            GameWorld.FONTS.SMEDIAN,
                            textColor)
                            .setScale(1.3f)
                            .setMaxWidth(dialog.getWidth() * 0.9f))
                    // Подзаголовок
                    .addElement(new TextView(
                            game,
                            "Учтите что подсказку можно использовать лишь раз за один раунд",
                            dialog.getX() + dialog.getWidth() / 2f,
                            dialog.getY() + dialog.getHeight() - 50,
                            ElementsUI.ALIGN.CENTER,
                            ElementsUI.ALIGN.TOP,
                            GameWorld.FONTS.SMEDIAN,
                            textColor)
                            .setScale(0.8f)
                            .setMaxWidth(dialog.getWidth() * 0.9f))

                    // Кнопка 1
                    .addElement(new Button(
                            "Узнать название песни",
                            button,
                            game.world,
                            buttonColor,
                            new ElementsUI.Action() {
                                @Override
                                public void isClick() {
                                    useHelp(1);
                                }
                            })
                            .setPosition(dialog.getX() + 15, dialog.getY() + 165)
                            .setSize(dialog.getWidth() - 30, 50)
                            .setUseGL(false))
                    // Кнопка 2
                    .addElement(new Button(
                            "Узнать автора",
                            button,
                            game.world,
                            buttonColor,
                            new ElementsUI.Action() {
                                @Override
                                public void isClick() {
                                    useHelp(2);
                                }
                            })
                            .setPosition(dialog.getX() + 15, dialog.getY() + 95)
                            .setSize(dialog.getWidth() - 30, 50)
                            .setUseGL(false))
                    // Кнопка 3
                    .addElement(new Button(
                            "Хотя бы что-нибудь",
                            button,
                            game.world,
                            buttonColor,
                            new ElementsUI.Action() {
                                @Override
                                public void isClick() {
                                    useHelp(3);
                                }
                            })
                            .setPosition(dialog.getX() + 15, dialog.getY() + 25)
                            .setSize(dialog.getWidth() - 30, 50)
                            .setUseGL(false))

                    // Текст по кнопкой 1
                    .addElement(new TextView(
                            game,
                            "Стоимость: 2 подсказки",
                            dialog.getX() + 25,
                            dialog.getY() + 160,
                            ElementsUI.ALIGN.LEFT,
                            ElementsUI.ALIGN.TOP,
                            GameWorld.FONTS.SMALL,
                            textColor)
                            .setScale(0.9f))
                    // Текст по кнопкой 2
                    .addElement(new TextView(
                            game,
                            "Стоимость: 2 подсказки",
                            dialog.getX() + 25,
                            dialog.getY() + 90,
                            ElementsUI.ALIGN.LEFT,
                            ElementsUI.ALIGN.TOP,
                            GameWorld.FONTS.SMALL,
                            textColor)
                            .setScale(0.9f))
                    // Текст по кнопкой 3
                    .addElement(new TextView(
                            game,
                            "Стоимость: 1 подсказка",
                            dialog.getX() + 25,
                            dialog.getY() + 20,
                            ElementsUI.ALIGN.LEFT,
                            ElementsUI.ALIGN.TOP,
                            GameWorld.FONTS.SMALL,
                            textColor)
                            .setScale(0.9f));
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (SCREEN_STATE == 1 && levelMultiplexer) {
            setInputMenu();
            MainGDX.write("SCREEN STATE: 1");
        } else if (SCREEN_STATE == 0 && !levelMultiplexer) {
            setInputLevel();
            MainGDX.write("SCREEN STATE: 2");
        } else if (video_showing) {
            world.resetMultiplexer();
            return;
        }

        world.setLockedAction(dialog.isOpened() || video_showing);
        cursor = GraphicTool.getClick();
        game.drawShape();
        layout.drawColoredRectangle(game.renderer, delta);
        game.endShape();
        game.draw();
        if (typeSelected) playerUpdate(delta);
        else drawQuest(delta);
        //if (music != null)
        //    world.setText(musics.musics.get(musicN).artist, 1f, MainGDX.WIDTH / 2f, 40, Color.BLUE, true);
        if (infoLoaded && !typeSelected && musicL > 0) {
            next.setCursor(cursor);
            next.draw(game, delta);
        }
        game.end();
        if (infoLoaded && !typeSelected && netTask.max_progress > 0) {
            game.drawShape();
            layout.drawProgressBar(game.renderer, (float) netTask.cur_progress / netTask.max_progress);
            game.endShape();
            game.draw();
            world.setText(
                    AssetsTool.formatSize(netTask.cur_progress) + " / " + AssetsTool.formatSize(netTask.max_progress),
                    0.8f, MainGDX.WIDTH / 2f, 20, Color.WHITE, game.layout.getPbColor(),
                    true, GameWorld.FONTS.SMALL);
            game.end();
        }
        if (isPause) {
            if (levelMultiplexer) {
                menuList = managerV.getStrings("MENU PAUSE");
                SCREEN_STATE = 1;
            }
            if (pause_delta > 0.5f)
                slider.setPaused(true);
            if (pause_delta < 1f)
                pause_delta += delta;
            else
                pause_delta = 1f;
        } else if (pause_delta != 0) {
            if (slider.isPaused()) {
                SCREEN_STATE = 0;
                showedWarning = false;
                slider.setPaused(false);
            }
            if (pause_delta > 0f)
                pause_delta -= delta;
            else
                pause_delta = 0f;
        }
        if (isEnd) {
            if (levelMultiplexer) {
                SCREEN_STATE = 1;
                menuList = managerV.getStrings("MENU END");
                if (!world.usedBonus) {
                    world.getBonus = true;
                    world.addHelp();
                }
            }
            if (end_delta < 1f) end_delta += delta;
            else if (end_delta > 1f) {
                end_delta = 1f;
                //music.pause();
                //slider.setPaused(true);
            }
        }
        if (end_delta > 0) {
            if (music != null) music.setVolume(Math.max(1 - end_delta, 0.2f));
            menu = layout.drawEnd(game, menuList, isWin ? "Поздравляшки :)" : "Печалька :(", end_delta, game.interpolation, isWin, selectedMenu, isWin ? win : lose, mainBG);
            drawTextInMenu();
        } else if (pause_delta > 0) {
            if (music != null) music.setVolume(1 - pause_delta);
            menu = layout.drawPause(game, menuList, pause_delta, game.interpolation, guessed, -1, musicCount - guessed, selectedMenu, pauseC, mainBG);
            drawTextInMenu();
        }
        dialog.draw(game, delta, cursor);
        if (ScreenAnim.getState()) {
            game.drawShape();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (ScreenAnim.show(game)) {
                if (ScreenAnim.isClosing()) {
                    if (ScreenAnim.level >= 0)
                        LoadScreen.setLevel(game, ScreenAnim.level);
                    else {
                        ScreenAnim.setOpen();
                        typeSelected = true;
                    }
                } else {
                    ScreenAnim.setState(false);
                }
            }
            game.endShape();
            if (music != null && music.isPlaying()) music.setVolume(1 - ScreenAnim.getAlpha());
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

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

    @Override
    public void pause() {
        if (SCREEN_STATE != 2) {
            menuList = managerV.getStrings("MENU PAUSE");
            isPause = true;
            pause_delta = 1f;
        }
    }

    @Override
    public void resume() {

    }


    @Override
    public void hide() {
        pause();
    }

    @Override
    public void dispose() {
        netTask.kill();
        if (music != null) {
            music.stop();
            music.dispose();
        }
        AssetsTool.delete(AssetsTool.getFile("cache"));
        if (musics != null && musics.musics != null && musics.musics.size() > 0) {
            for (int i = 0; i < musics.musics.size(); i++) {
                //AssetsTool.getFileHandler("cache/" + musics.musics.get(i).path).delete();
                game.manager.delete(musics.musics.get(i).path);
            }
        }
    }

    // Перейти к другому вопросу
    private void changeQuest(int qq) {
        if (quest.size() == 0) {
            for (String question : AssetsTool.encodePlatform(assets.getLevelContent("list menu.pref")).split("\n\n")) {
                quest.add(question.split("\n"));
            }
        }
        selectedQuest = qq;
        String[] q = quest.get(selectedQuest);
        isChanging = false;
        titleText = q[0];
        buttons.clear();
        for (int i = 1; i < q.length; i++) {
            final int finalI = i;
            buttons.add(new Button(q[i].split("-")[0], texture.findRegion("square_darkgray_btn"), world, Color.WHITE, new Button.Action() {
                @Override
                public void isClick() {
                    String s = q[finalI].split("-")[1];
                    if (s.length() > 2) {
                        titleText = "Загрузка...";
                        for (Button b : buttons) {
                            b.setActive(false);
                            b.move(b.getX(), -b.getY(), b.getWidth(), b.getHeight(), 1);
                        }
                        netTask.API(NetTask.LEVEL_GUESS_MUSIC, "type", s.substring(4), "age", "" + game.user.getAge());
                    } else {
                        newQuest = Integer.parseInt(s);
                        isChanging = true;
                    }
                }

                @Override
                public void isSelected() {

                }
            }));
            buttons.get(i - 1).setWidth(MainGDX.WIDTH / (q.length > 6 ? 3f : 2f));
            if (q.length > 6) {
                buttons.get(i - 1).setPosition(MainGDX.WIDTH / (i <= q.length / 2f ? 3f : 1.5f) - buttons.get(i - 1).getWidth() / 2f, MainGDX.HEIGHT / 2f - (q.length / 2f - 1) * (buttons.get(i - 1).getHeight() + 10) / 2f + (i <= q.length / 2f ? i - 1 : q.length - i - 1) * (buttons.get(i - 1).getHeight() + 10));
            } else {
                buttons.get(i - 1).setPosition(MainGDX.WIDTH / 2f - buttons.get(i - 1).getWidth() / 2f, MainGDX.HEIGHT / 2f - (q.length - 1) * (buttons.get(i - 1).getHeight() + 10) / 2f + (i - 1) * (buttons.get(i - 1).getHeight() + 10));
            }
        }
    }

    // Нарисовать экран с вопросом
    public void drawQuest(float delta) {
        for (Button b : buttons) {
            if (levelMultiplexer) b.setCursor(cursor);
            b.draw(game, delta);
        }
        title.draw(game);
        world.setText(titleText, 1f, MainGDX.WIDTH / 2f, title.getHeight() / 2f, Color.WHITE, true, GameWorld.FONTS.SMEDIAN);
        if (errorLoading) {
            world.setText("Нажмите на экран чтоб вернуться обратно", 1f, MainGDX.WIDTH / 2f, title.getHeight() / 2f - world.getSizes()[1] * 1.3f, Color.WHITE, true, GameWorld.FONTS.SMALL);
            if (Gdx.input.isTouched()) {
                isChanging = true;
                errorLoading = false;
            }
        }
        if (isChanging) changeQuest(newQuest);
    }

    public void setInputLevel() {
        world.resetMultiplexer();
        world.addProcessor(levelInputAdapter);
        world.addProcessor(levelGestureDetector);
        world.updateMultiplexer();
        levelMultiplexer = true;
    }

    public void setInputMenu() {
        world.resetMultiplexer();
        world.addProcessor(menuInputAdapter);
        world.addProcessor(menuGestureDetector);
        world.updateMultiplexer();
        levelMultiplexer = false;
    }

    // Перемотка музыки
    private boolean cutCheck(float start, float end) {
        if (musicP >= start && musicP < end - 0.1) {
            music.pause();
            MUSIC_CUT_STATE = 1;
            slider.setActive(false);
            cutting.setOnCompletionListener(m -> {
                music.play();
                slider.setActive(true);
                music.setPosition(end);
                MUSIC_CUT_STATE = 0;
            });
            cutting.play();
        }
        return MUSIC_CUT_STATE == 1;
    }

    // Обновление музыки
    private void musicUpdate() {
        if (!assets.updating() && MUSIC_LOAD_STATE == 1) {
            if (music != null) {
                if (!music.isPlaying()) music.play();
                music.stop();
                music.dispose();
            }
            music = assets.get(musics.musics.get(musicN).path);
            music.play();
            slider.setPaused(false);
            slider.setActive(true);
            MUSIC_LOAD_STATE = 2;
        } else if (MUSIC_LOAD_STATE == -1) {
            showMessage("Не удалось загрузить песню", 3f);
        }
        if (music != null) {
            if (slider.isPaused()) {
                music.pause();
            } else if (MUSIC_CUT_STATE != 1 && music.isPlaying()) {
                musicP = music.getPosition();
                if (MUSIC_CUT_STATE != -1)
                    for (int i = 0; i < rewindList.size(); i++)
                        if (cutCheck(rewindList.get(i++), rewindList.get(i))) break;
                if (slider.isSelected()) music.pause();
                slider.setValue(musicP);
            } else if (MUSIC_CUT_STATE != 1 && !slider.isSelected()) {
                if ((int) slider.getMaxValue() <= (int) musicP + 1) {
                    slider.setPaused(true);
                    musicP = 0;
                } else {
                    musicP = slider.getValue();
                    music.play();
                    music.setPosition(musicP);
                }
            }
        }
    }

    // Загрузка музыки
    private void musicLoad(int N) {
        VIDEO_LOAD_STATE = 0;
        if (N == musics.musics.size()) {
            isWin = true;
            isEnd = true;
            return;
        } else if (N == musicL) {
            showMessage("Следующая песня ещё не загружена\n" + AssetsTool.formatSize(netTask.cur_progress) + " / " + AssetsTool.formatSize(netTask.max_progress), 1f);
            nextLoading = true;
            return;
        }
        musicN = N;
        //usedHelp = false;
        buttons.clear();
        assets.load(musics.musics.get(N).path);
        slider.setValues(0, (int) musics.musics.get(N).len);
        input.setText("", game);
        slider.setValue(0);
        slider.setPaused(true);
        rewindList.clear();
        String rewindTimeList = musics.musics.get(N).cut;
        if (!rewindTimeList.equalsIgnoreCase("0")) {
            for (String s : rewindTimeList.split(" ")) {
                for (String ss : s.split("-")) {
                    rewindList.add(Float.parseFloat(ss));
                }
            }
        }
        MUSIC_LOAD_STATE = 1;
        MUSIC_CUT_STATE = 0;
    }

    // Проверка названия
    public boolean musicCheck(String x) {
        x = AssetsTool.replace(x.toLowerCase().replace("\n", "").replace("ё", "е"),
                GameWorld.getSymbolsCharset(), " ", true).trim();
        String[] titles = AssetsTool.replace(musics.musics.get(musicN).title.toLowerCase().replace("ё", "е"),
                GameWorld.getSymbolsCharset(), " ", true).split("\n");
        String[] artists = AssetsTool.replace(musics.musics.get(musicN).artist.toLowerCase().replace("ё", "е"),
                GameWorld.getSymbolsCharset(), " ", true).split("\n");
        //String[] names = (selectedQuest > 2 ? musics.musics.get(musicN).title : musics.musics.get(musicN).artist).split("\n");
        scoreMusic = 100;
        if (selectedQuest > 2) {
            for (String artist : artists) {
                artist = artist.trim();
                MainGDX.write("Artist check: " + artist + " - " + x);
                if (x.contains(artist)) {
                    int i = x.indexOf(artist);
                    x = x.substring(0, i) + x.substring(i + artist.length());
                    break;
                }
                scoreMusic -= 50 / artists.length;
            }
        }
        for (String title : selectedQuest > 2 ? titles : artists) {
            title = title.trim();
            MainGDX.write("Title check: " + title + " - " + x);
            if (title.equalsIgnoreCase(x.trim()))
                return true;
            scoreMusic -= 50 / (selectedQuest > 2 ? titles : artists).length;
        }
        return false;
    }

    // Обновление плейера
    private void playerUpdate(float delta) {
        pic_bg.draw(game);
        live_bg.draw(game);

        world.setText("Угадано:", 1.3f, pic_bg.getX() + 20, pic_bg.getY() + pic_bg.getHeight() - 20, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        world.setText("Всего:", 1.3f, pic_bg.getX() + 20, pic_bg.getY() + pic_bg.getHeight() - 20 - world.getSizes()[1] * 2f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);

        world.setText("" + guessed, 1.3f, pic_bg.getX() + pic_bg.getWidth() - 20 - world.getTextSize("" + guessed, 1.3f, GameWorld.FONTS.SMEDIAN)[0], pic_bg.getY() + pic_bg.getHeight() - 20, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        world.setText("" + musicCount, 1.3f, pic_bg.getX() + pic_bg.getWidth() - 20 - world.getTextSize("" + musicCount, 1.3f, GameWorld.FONTS.SMEDIAN)[0], pic_bg.getY() + pic_bg.getHeight() - 20 - world.getSizes()[1] * 2f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);

        world.setText("Чтобы использовать подсказку нажмите на неё", 0.5f, pic_bg.getX() + pic_bg.getWidth() / 2f, (live_bg.getY() + pic_bg.getY()) / 2f + 2, Color.DARK_GRAY, true, GameWorld.FONTS.SMALL);
        if (SCREEN_STATE == 0 && !world.isLockedAction()) {
            ok.setCursor(cursor);
            exit.setCursor(cursor);
            input.setCursor(cursor);
            slider.setCursor(cursor);
            live_btn.setCursor(cursor);
            help_btn.setCursor(cursor);
            ticket_btn.setCursor(cursor);
        }
        live_btn.setText("" + world.getLives(), game);
        help_btn.setText("" + world.getHelp(), game);
        ticket_btn.setText("" + world.getTicket(), game);
        slider.draw(game, delta);
        live_btn.draw(game, delta);
        help_btn.draw(game, delta);
        ticket_btn.draw(game, delta);

        if (!input.isEdit()) {
            ok.draw(game, delta);
            exit.draw(game, delta);
            input.draw(game, delta);
        }

        game.getBatch().setColor(0, 0, 0, 1);
        fragment.draw(game);
        for (Button btn : buttons) {
            btn.setCursor(cursor);
            btn.draw(game, delta);
        }

        float height = MainGDX.HEIGHT / 3f;
        game.getBatch().setColor(1, 1, 1, VIDEO_LOAD_STATE > 0 ? 1 - fragment.getMovingState() : fragment.getMovingState());
        if (VIDEO_LOAD_STATE > 0) music.setVolume(1 - fragment.getMovingState());
        game.getBatch().draw(texture.findRegion("ask"), fragment.getX() + fragment.getWidth() / 2f - height / 5 * 2, fragment.getY() + fragment.getHeight() / 2f - height / 2f, height / 5 * 4, height);
        game.getBatch().setColor(1, 1, 1, 1);
        if (INFO_TIME > 0f) {
            if (!nextLoading) INFO_TIME -= delta;
            String[] ss = INFO_TEXT.replace("_", "\n").split("\n");
            for (int i = 0; i < ss.length; i++) {
                world.setText(ss[ss.length - 1 - i], 1f, fragment.getX() + fragment.getWidth() / 2f, fragment.getY() + MainGDX.HEIGHT / 20f + world.getSizes()[1] * 1.5f * i, Color.WHITE, Color.BLACK, true, GameWorld.FONTS.SMEDIAN);
            }
        } else if (isGuessed) {
            if (VIDEO_LOAD_STATE != -1) {
                input.setText("", game);
                if (game.video != null) {
                    videoLoad();
                } else {
                    showMessage("На вашем устройстве\nне возможно воспроизвести видео", 2f);
                    new Thread(() -> musicLoad(musicN + 1)).start();
                }
            } else {
                new Thread(() -> musicLoad(musicN + 1)).start();
            }
            isGuessed = false;
        }
        if (nextLoading) new Thread(() -> musicLoad(musicN + 1)).start();
        if (input.isEdit()) {
            ok.draw(game, delta);
            exit.draw(game, delta);
            input.draw(game, delta);
        }
        musicUpdate();
    }


    // Вывод текста
    private void showMessage(String text, float time) {
        INFO_TEXT = text;
        INFO_TIME = time;
    }

    // Загрузка видео
    private void videoLoad() {
        VIDEO_LOAD_STATE = 1;
        fragment.move(-10, -10, MainGDX.WIDTH + 20, MainGDX.HEIGHT + 20, 1f);
        fragment.setAction(new Button.Action() {
            @Override
            public void onCompletionAction() {
                music.stop();
                slider.setPaused(true);
                game.video.loadVideo(musics.musics.get(musicN).video,
                        (selectedQuest > 2 ? musics.musics.get(musicN).title : musics.musics.get(musicN).artist).split("\n")[0],
                        (selectedQuest > 2 ? musics.musics.get(musicN).artist : musics.musics.get(musicN).title).split("\n")[0]);
            }
        });
        //Vector2 v1 = fromLocal(fragment.getX(), fragment.getY());
        //Vector2 v2 = fromLocal(fragment.getWidth(), fragment.getHeight());
        //game.video.setBounds(v1.x,Gdx.graphics.getHeight()-v1.y-v2.y,v2.x,v2.y);
    }

    private void useHelp(int variant) {
        switch (variant) {
            case 1:
                if (world.getHelp() > 1) {
                    showMessage(musics.musics.get(musicN).title.split("\n")[0], 5f);
                    world.useHelp(2);
                } else showMessage("У вас не достаточно подсказок", 5f);
                break;
            case 2:
                if (world.getHelp() > 1) {
                    showMessage(musics.musics.get(musicN).artist.split("\n")[0], 5f);
                    world.useHelp(2);
                } else showMessage("У вас не достаточно подсказок", 5f);
                break;
            case 3:
                showMessage((MathUtils.random(1) == 0 ? musics.musics.get(musicN).title : musics.musics.get(musicN).artist).split("\n")[0], 5f);
                world.useHelp(1);
                break;
            case 4:
                showMessage(managerV.find("HELP", assets.getLevel() + ""), 5f);
                MUSIC_CUT_STATE = -1;
                world.useHelp(1);
                break;
        }
        usedHelp = true;
    }
}
