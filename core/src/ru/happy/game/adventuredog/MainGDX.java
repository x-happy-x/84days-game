package ru.happy.game.adventuredog;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.User;
import ru.happy.game.adventuredog.Screens.Auth;
import ru.happy.game.adventuredog.Tools.ApplicationBundle;
import ru.happy.game.adventuredog.Tools.AssetsManagerX;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.LevelSwitcher;
import ru.happy.game.adventuredog.Tools.NetTask;
import ru.happy.game.adventuredog.Tools.SizeChangeListener;
import ru.happy.game.adventuredog.Tools.View;
import ru.happy.game.adventuredog.UI.Layout;

import static ru.happy.game.adventuredog.Tools.AssetsTool.encodePlatform;
import static ru.happy.game.adventuredog.Tools.AssetsTool.getFile;

public class MainGDX extends Game {
    // Параметры экрана
    public static int WIDTH = 1000, HEIGHT = 500, DISPLAY_CUTOUT_MODE;
    // Версия игры
    public static int VERSION;
    // Цвет при очистке экрана
    public Color clearBg;

    // Игровые обЪекты
    public ShapeRenderer renderer; // Рендер фигур
    public AssetsTool assets; // Управление ресурсами
    public AssetsManagerX manager; // Помощник тому что выше
    public GameWorld world; // Игровой мир
    public Layout layout; // Рисование сложных фигур
    private Sprite bg; // Фон загрузки игры
    Thread extraction; // Поток загрузки
    public User user; // Игрок
    public View view; // Объект для отслеживания сенсорной клавиатуры

    // Игровые параметры
    public Interpolation interpolation = Interpolation.exp5; // Вид анимации
    public Map<String, String> property; // Параметры уровней
    private boolean loaded, auth, error; // Статус загрузки игры
    public static Logger logger;
    private final float sync_time = 3 * 60; // Время синхронизации (сек)
    private final String[] states = new String[]{"Проверка обновлений",            // Названия стадий
            "Загрузка дополнительный файлов",
            "Распаковка скачанных файлов",
            "Авторизация",
            "Запуск игры"};
    Texture bgTexture;
    Color statColor = Color.valueOf("#ffffff"), statStroke = Color.valueOf("#000000");
    float[] olds;
    private int stateType; // Стадия загрузки
    private String state; // Название текущей стадии
    private float sync_delta; // Отсчёт времени синхронизации

    public MainGDX(ApplicationBundle bundle) {
        view = bundle.getView();
    }

    private float progressNow; // Прогресс загрузки

    // Конструкторы игры
    public MainGDX() {
    }

    public static void enableLogger(String text) {
        logger = Logger.getLogger("MyLog");
        FileHandler fh;
        try {
            fh = new FileHandler(getFile(text, false).getAbsolutePath());
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String text) {
        logger.info(text);
    }

    // Добавить слушатель при изменение размера экрана (при показе и скрытии клавиатуры на андроид)
    public void addSizeChangeListener(SizeChangeListener listener) {
        if (view == null) return;
        view.clear();
        view.addListener(listener);
    }

    @Override
    public void setScreen(Screen screen) {
        ScreenAnim.setOpen();
        ScreenAnim.setState(true);
        super.setScreen(screen);
    }

    private void waitState(int state) {
        while (true) {
            if (getProgress(state) == 2) break;
        }
    }

    @Override
    public void create() {
        enableLogger("log");
        // Установка дефолтных параметров
        HEIGHT = (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * WIDTH);
        clearBg = new Color(0, 0, 0, 1);
        loaded = error = auth = false;
        progressNow = 1f;

        // Создание и настройка объектов
        renderer = new ShapeRenderer();
        manager = new AssetsManagerX();
        assets = new AssetsTool();
        world = new GameWorld();
        layout = new Layout();
        user = new User();

        world.fontSetting();

        DISPLAY_CUTOUT_MODE = world.prefs.getInteger("cutoutMode", 0);
        world.resize(WIDTH, HEIGHT);

        // Настройка камеры
        getBatch().setProjectionMatrix(world.getCamera().combined);
        renderer.setProjectionMatrix(world.getCamera().combined);
        renderer.setAutoShapeType(true);
        renderer.updateMatrices();
        getCamera().update();

        // Настройка фона
        bg = new Sprite();
        bg.setBounds(0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
        bgTexture = new Texture("bg.jpg");
        bg.setRegion(bgTexture);
        float w = bg.getRegionWidth(), h = bg.getRegionHeight();
        if ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() > w / h) {
            bg.setRegion(0, 0, (int) w, (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * w));
            bg.setRegion(0, (int) ((h - bg.getRegionHeight()) / 2f), bg.getRegionWidth(), bg.getRegionHeight());
        } else {
            bg.setRegion(0, 0, (int) ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * h), (int) h);
            bg.setRegion((int) ((w - bg.getRegionWidth()) / 2f), 0, bg.getRegionWidth(), bg.getRegionHeight());
        }

        // Начало загрузки
        stateType = 0;
        state = states[stateType];
        load();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(clearBg.r, clearBg.g, clearBg.b, clearBg.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        renderer.setProjectionMatrix(world.getCamera().combined);
        getCamera().update();
        getBatch().setProjectionMatrix(world.getCamera().combined);
        super.render();
        if (!loaded) {
            if (stateType < 4 || assets.updating()) {
                draw();
                bg.draw(getBatch());
                String progressText = state + "... ";
                if (!error) {
                    switch (stateType) {
                        case 0:
                            break;
                        case 1:
                            progressText += "" + (int) (progressNow * 100) + "%";
                            break;
                        case 2:
                            progressText += "" + (int) (getProgress(extraction.isAlive() ? 1f / AssetsTool.allExtractCount * AssetsTool.extractCount : 1f) * 100) + "%";
                            break;
                    }
                }
                float y = MainGDX.HEIGHT / 4f;
                for (String s : progressText.split("_")) {
                    world.setText(s, 1f, MainGDX.WIDTH / 2f, y, Color.WHITE, true, GameWorld.FONTS.SMALL);
                    y -= world.getTextSize("1", 1f, GameWorld.FONTS.SMALL)[1] * 1.7f;
                }
                if (error) {
                    if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                        load();
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
                        Gdx.app.exit();
                    }
                }
                end();
            } else {
                ScreenAnim.load();
                loaded = true;
                bgTexture.dispose();
                if (auth) LevelSwitcher.setLevel(this, 0);
                else setScreen(new Auth(this));
            }
        }
        drawStat();
    }

    @Override
    public void resize(int width, int height) {
        if (world != null) world.resize(width, height);
        super.resize(width, height);
        if (!loaded) {
            bg.setRegion(bgTexture);
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

    void drawStat() {
        draw();
        if (loaded) {
            sync_delta += Gdx.graphics.getDeltaTime();
            if (sync_delta >= sync_time) {
                world.startSync();
                sync_delta = 0f;
            }
        }
        float H = Gdx.graphics.getHeight(), W = Gdx.graphics.getWidth(), D = Gdx.graphics.getDensity(), F = Gdx.graphics.getFramesPerSecond();
        if (olds == null || olds[0] != HEIGHT || olds[1] != WIDTH || olds[2] != H || olds[3] != W || olds[4] != D) {
            olds = new float[]{HEIGHT, WIDTH, H, W, D};
            write("GameSize: " + WIDTH + "x" + HEIGHT + " Screen: " + W + "x" + H + " DPI: " + D + " FPS: " + F);
        }
        world.setText((!world.isSynced() ? "НЕ " : "") + "СИНХРАНИЗИРОВАНО", 0.5f, MainGDX.WIDTH / 2f, MainGDX.HEIGHT - 5, statColor, statStroke, true, false, GameWorld.FONTS.SMALL);
        world.setText("GameSize: " + WIDTH + "x" + HEIGHT + " Screen: " + W + "x" + H + " DPI: " + D + " FPS: " + F, 0.8f, MainGDX.WIDTH / 2f, 15, statColor, statStroke, true, false, GameWorld.FONTS.SMALL);
        end();
    }

    @Override
    public void dispose() {
        getFile("log").copyTo(Gdx.files.external("game_log.txt"));
        world.startSync();
        world.dispose();
        super.dispose();
    }

    public void draw() {
        world.getBatch().begin();
    }

    public void end() {
        world.getBatch().end();
    }

    public Batch getBatch() {
        return world.getBatch();
    }

    public OrthographicCamera getCamera() {
        return world.getCamera();
    }

    public void drawShape() {
        renderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    public void endShape() {
        renderer.end();
    }

    // Плавный прогресс
    private float getProgress(float progressNew) {
        if (progressNow < 1 && progressNew >= progressNow)
            progressNow += Math.max(0.01f, (progressNew - progressNow) / 50);
        if (progressNow > 1) progressNow = 1f;
        return progressNow;
    }

    // Прогресс выполнения стадии загрузки
    private float getProgress(int stateChange) {
        if (progressNow == 1f) {
            progressNow = stateType < 4 ? 0f : 1f;
            stateType = stateChange;
            return 2;
        }
        return getProgress(1f);
    }

    // Загрузка
    private void load() {
        extraction = new Thread(() -> {
            manager.clear();
            boolean upd;
            upd = auth = error = false;
            progressNow = stateType = 0;
            NetTask task = new NetTask(new NetTask.NetListener() {
                @Override
                public void onDownloadComplete(String out) {}

                @Override
                public void onProgressUpdate(int progress) {
                    getProgress(progress / 100f);
                }

                @Override
                public void onDownloadFailure(String msg) {
                    state = msg;
                    error = true;
                }
            });

            // Проверка обновлений
            state = states[stateType];
            if (AssetsTool.getFile("menu/game.pref").exists()) {
                property = AssetsTool.getParamFromFile(AssetsTool.readFile("menu/game.pref"));
                MainGDX.VERSION = Integer.parseInt(property.get("version"));
                if (task.AsyncGET("updates/lastversion.php")) {
                    if (MainGDX.VERSION < Integer.parseInt(task.result.split("_")[0])) {
                        upd = true;
                        MainGDX.VERSION = Integer.parseInt(task.result);
                    }
                } else {
                    state = task.result + "_Нажмите на экран чтобы попробовать снова";
                    error = true;
                    return;
                }
            } else upd = true;

            // Обновление
            if (upd) {
                waitState(1);
                state = states[stateType];
                if (task._GET(NetTask.site+"updates/data.zip", AssetsTool.getFile("data.tmp", false))) {
                    waitState(2);
                    state = states[stateType];
                    AssetsTool.extractObb(AssetsTool.getFile("data.tmp", false));
                    getFile("data.tmp").delete();
                    property = AssetsTool.getParamFromFile(AssetsTool.readFile("menu/game.pref"));
                    if (MainGDX.VERSION == 0)
                        MainGDX.VERSION = Integer.parseInt(property.get("version"));
                    else {
                        property.put("version", "" + MainGDX.VERSION);
                        AssetsTool.setParamToFile("menu/game.pref", property);
                    }
                } else {
                    state = task.result + "_Нажмите на экран чтобы попробовать снова";
                    error = true;
                    return;
                }
            }

            // Авторизация
            waitState(3);
            if (world.prefs.contains("name")) {
                state = states[stateType];
                if (task.AsyncGET("", "mode","1","mail",world.prefs.getString("mail").equals("@") ? world.prefs.getString("mail") : world.prefs.getString("name"),"pass", world.prefs.getString("pass"))) {
                    try {
                        user.set(new Json(JsonWriter.OutputType.json).fromJson(User.class, task.result));
                        user.setMessage(encodePlatform(user.getMessage(),false));
                    } catch (SerializationException e) {
                        user.reset();
                        user.setSuccess(-1);
                    }
                    if (user.getSuccess() == 1) auth = true;
                }
            }

            // Зазгрузка начальных ресурсов
            if (auth) state = states[stateType+1];
            manager.setProperty("levels",property.get("levels"));
            Map<String,String> temp;
            for (int i = 0; i < manager.getInt("levels"); i++) {
                manager.setProperty(i,"path", property.get("level" + i + "Path"));
                temp = AssetsTool.getParamFromFile(AssetsTool.readFile(manager.getString(i,"path") + "/level.pref"));
                manager.setProperty(i,"hints",temp.get("hintCount"));
                //
                for (int j = 0; j < manager.getInt(i,"hints"); j++)
                    manager.setProperty(i,"hint"+j,temp.get("hint" + j));
                //
                for (int j = 0; j < Integer.parseInt(temp.get("loadFiles")); j++)
                    manager.add(i,temp.get("load" + j + "Name"),manager.getString(i,"path")+"/"+temp.get("load" + j + "Path"),temp.get("load" + j + "Type"));
            }
            assets.setManager(manager);

            // Не удалось авторизоваться
            if (!auth) {
                assets.fresh();
                assets.setLevel(-1);
                assets.bg = new AssetDescriptor<>("menu/reg_bg.png", Texture.class);
                assets.load();
            } else {
                // Синхронизация с миром
                user.setInWorld(world.prefs);
                world.setUser(user);
            }

            stateType++;
        });

        extraction.start();
    }
}
