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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.Interfaces.VideoPlayer;
import ru.happy.game.adventuredog.Interfaces.View;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.User;
import ru.happy.game.adventuredog.Screens.Auth;
import ru.happy.game.adventuredog.Screens.LoadScreen;
import ru.happy.game.adventuredog.Tools.ApplicationBundle;
import ru.happy.game.adventuredog.Tools.AssetsManagerX;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.NetTask;
import ru.happy.game.adventuredog.Tools.ValuesManager;
import ru.happy.game.adventuredog.UI.Layout;

import static ru.happy.game.adventuredog.Tools.AssetsTool.encodePlatform;
import static ru.happy.game.adventuredog.Tools.AssetsTool.getFile;

public class MainGDX extends Game {

    // Параметры экрана
    public static final int APP_VERSION = 27;
    private static final int START_LEVEL = 3;
    public static int WIDTH = 1000,
            HEIGHT = 500,
            DISPLAY_CUTOUT_MODE,
            uID,
            VERSION;
    public static String APP_LOG_TAG = "UNNAMED_GAME";
    private final String[] states = new String[]{"Проверка обновлений", "Загрузка файлов", "Проверка файлов", "Авторизация", "Запуск игры"};
    public static boolean OFFLINE = false;

    // Цвет при очистке экрана
    public Color clearBg;

    // Игровые обЪекты
    public ShapeRenderer renderer; // Рендер фигур
    public AssetsTool assets; // Управление ресурсами
    public AssetsManagerX manager; // Помощник тому что выше
    public GameWorld world; // Игровой мир
    public Layout layout; // Рисование сложных фигур
    public User user; // Игрок
    public View view; // Объект для отслеживания сенсорной клавиатуры

    // Игровые параметры
    public Interpolation interpolation = Interpolation.exp5; // Вид анимации
    public Map<String, String> property; // Параметры уровней
    public VideoPlayer video;
    public ValuesManager values;

    private NetTask task;
    Thread extraction; // Поток загрузки
    private boolean loaded, auth, error; // Статус загрузки игры
    Texture bgTexture;
    Color statColor = Color.valueOf("#ffffff"), statStroke = Color.valueOf("#000000");
    float[] olds;
    private Sprite bg;                   // Фон загрузки игры
    private int stateType;               // Стадия загрузки
    private String state, openURL;       // Название текущей стадии
    private float sync_delta;            // Отсчёт времени синхронизации
    private float progressNow;           // Прогресс загрузки

    public MainGDX(ApplicationBundle bundle) {
        this();
        view = bundle.getView();
        video = bundle.getVideo();
        //video.loadVideo(NetTask.site + "video/1 (1).mp4", "test", "test");
    }

    // Конструкторы игры
    public MainGDX() {
    }

    private static Process logger;
    public static void enableLogger() {
        //Gdx.app.setApplicationLogger();
        try {
            logger = Runtime.getRuntime().exec(new String[]{"logcat", "-f", getFile("log.txt").getAbsolutePath()});//, APP_LOG_TAG + ":V", "*:S"});
            write("- LOG START -");
        } catch (IOException e) {
            write(e.getLocalizedMessage());
        }
    }
    public static void clearLogger(){
        /*if (logger == null) return;
        try {
            logger.destroy();
            getFile("log.txt").delete();
            MainGDX.write(getFile("log.txt").exists()+"");
            getFile("log.txt").createNewFile();
            enableLogger();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
    public static void write(String text) {
        Gdx.app.log(APP_LOG_TAG, AssetsTool.encodePlatform(text, true));
    }


    // Добавить слушатель при изменение размера экрана (при показе и скрытии клавиатуры на андроид)
    public void addSizeChangeListener(View.SizeChangeListener listener) {
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
        enableLogger();

        // Установка дефолтных параметров
        HEIGHT = (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * WIDTH);
        clearBg = new Color(0, 0, 0, 1);
        loaded = error = auth = false;
        progressNow = 1f;
        uID = -1;
        VERSION = 0;

        // Создание и настройка объектов
        renderer = new ShapeRenderer();
        manager = new AssetsManagerX();
        assets = new AssetsTool();
        world = new GameWorld();
        layout = new Layout();
        user = new User();
        task = new NetTask(new NetTask.NetListener() {
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

        world.fontSetting();

        DISPLAY_CUTOUT_MODE = world.prefs.getInteger("cutoutMode", 0);
        world.resize(WIDTH, HEIGHT);

        // Настройка камеры
        getBatch().setProjectionMatrix(world.getCamera().combined);
        renderer.setProjectionMatrix(world.getCamera().combined);
        renderer.setAutoShapeType(true);
        renderer.updateMatrices();
        getCamera().update();
        layout.createProgressBar(MainGDX.WIDTH / 4f, 35, MainGDX.WIDTH / 2f, 10, Color.valueOf("#6E5C7F"), 3, 3);

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
                String progressText = state;
                float y = MainGDX.HEIGHT / 4f;
                for (String s : progressText.split("_")) {
                    world.setText(s, 1f, MainGDX.WIDTH / 2f, y, Color.WHITE, true, GameWorld.FONTS.SMALL);
                    y -= world.getTextSize("1", 1f, GameWorld.FONTS.SMALL)[1] * 1.7f;
                }
                if (stateType == 1 || stateType == 2) {
                    end();
                    drawShape();
                    layout.getPb().setY(y - layout.getPb().height * 2);
                    layout.drawProgressBar(renderer, stateType == 1 ? progressNow : getProgress(extraction.isAlive() ? 1f / AssetsTool.ZIP_UNPACK_ALL * AssetsTool.ZIP_UNPACK_COUNT : 1f));
                    endShape();
                    draw();
                    if (task.max_progress > 0)
                        world.setText(stateType == 1 ? AssetsTool.formatSize(task.cur_progress) + " / " + AssetsTool.formatSize(task.max_progress) : AssetsTool.ZIP_UNPACK_COUNT + " / " + AssetsTool.ZIP_UNPACK_ALL,
                                0.8f, layout.getPb().x + layout.getPb().width / 2f, layout.getPb().y + layout.getPb().height / 2f, Color.WHITE, layout.getPbColor(),
                                true, GameWorld.FONTS.SMALL);
                }
                if (error) {
                    if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                        if (openURL != null && openURL.length() > 0) Gdx.net.openURI(openURL);
                        else load();
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
                        Gdx.app.exit();
                    }
                }
                end();
            } else {
                ScreenAnim.load();
                values = new ValuesManager();
                loaded = true;
                bgTexture.dispose();
                if (auth) LoadScreen.setLevel(this, START_LEVEL);
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
            layout.createProgressBar(MainGDX.WIDTH / 4f, 35, MainGDX.WIDTH / 2f, 10, Color.valueOf("#6E5C7F"), 3, 3);
        }
    }

    void drawStat() {
        draw();
        if (uID > 0 && loaded) {
            sync_delta += Gdx.graphics.getDeltaTime();
            // Время синхронизации (сек)
            float sync_time = 2 * 60;
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
        world.setText("UID: " + (uID == -1 ? "not authorized" : uID) + "        FPS: " + F, 0.8f, MainGDX.WIDTH / 2f, 15, statColor, statStroke, true, false, GameWorld.FONTS.SMALL);
        end();
    }

    @Override
    public void dispose() {
        //getFile("log").copyTo(Gdx.files.external("game_log.txt"));
        if (auth) world.startSync();
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
            Map<String, String> info;
            manager.clear();
            boolean upd;
            openURL = null;
            upd = auth = error = false;
            progressNow = stateType = 0;
            // Проверка обновлений
            state = states[stateType];
            if (AssetsTool.getFileHandler("menu/game.pref").exists()) {
                property = AssetsTool.getParamFromFile(AssetsTool.readFile("menu/game.pref"));
                VERSION = Integer.parseInt(property.get("version"));
            }

            // Оффлайн режим
            if (OFFLINE) {
                property = AssetsTool.getParamFromFile(AssetsTool.readFile("menu/game.pref"));
                property.put("version", "" + VERSION);
                auth = true;
                AssetsTool.setParamToFile("menu/game.pref", property);
                manager.setProperty("levels", property.get("levels"));
                Map<String, String> temp;
                for (int i = 0; i <= manager.getInt("levels"); i++) {
                    manager.setProperty(i, "path", property.get("level" + i + "Path"));
                    temp = AssetsTool.getParamFromFile(AssetsTool.readFile(manager.getString(i, "path") + "/level.pref"));
                    manager.setProperty(i, "hints", temp.get("hintCount"));
                    //
                    for (int j = 0; j < manager.getInt(i, "hints"); j++)
                        manager.setProperty(i, "hint" + j, temp.get("hint" + j));
                    //
                    for (int j = 0; j < Integer.parseInt(temp.get("loadFiles")); j++)
                        manager.add(i, temp.get("load" + j + "Name"), manager.getString(i, "path") + "/" + temp.get("load" + j + "Path"), temp.get("load" + j + "Type"));
                }
                assets.setManager(manager);
                user.setName("Оффлайн режим");
                user.setLives(999);
                user.setId(99999999);
                user.setTickets(99999);
                user.setHelps(99999);
                user.setAge(20);
                user.setSuccess(1);
                user.setMail("offline-test@mail.ru");
                user.setPass("1234");
                user.setInWorld(world.prefs);
                world.setUser(user);
                stateType = 4;
                return;
            }

            if (task.SYNC_GET("updates/status.php?version=" + VERSION + "&app=" + APP_VERSION + "&user=" + world.prefs.getInteger("uid", -1))) {
                info = AssetsTool.getParamFromFile(task.result);
                if (info.containsKey("LAST_VERSION")) {
                    write("UPDATE SUCCESS:\n" + task.result);
                    int ver = Integer.parseInt(info.get("LAST_VERSION"));
                    if (VERSION < ver) {
                        upd = true;
                        VERSION = ver;
                    }
                } else {
                    write("UPDATE ERROR: " + task.result);
                    state = task.result + "_Нажмите на экран чтобы попробовать снова";
                    error = true;
                    return;
                }
            } else {
                write("UPDATE ERROR: " + task.result);
                state = task.result + "_Нажмите на экран чтобы попробовать снова";
                error = true;
                return;
            }
            if (info.containsKey("STATUS")) {
                switch (Integer.parseInt(info.get("STATUS"))) {
                    case -1:
                        write("SERVER ERROR: " + info.get("MESSAGE"));
                        error = true;
                        state = info.get("MESSAGE");
                        break;
                    case 0:
                        write("SERVER WARNING: " + info.get("MESSAGE"));
                        error = true;
                        state = info.get("MESSAGE");
                        openURL = info.get("URL");
                        break;
                }
                if (error) return;
            }
            // Обновление
            if (upd) {
                waitState(1);
                state = states[stateType];
                String upd_path = NetTask.SITE + "updates/full_pack.zip", load_path = "data.tmp";
                if (info.containsKey("UPDATE_PACK")) {
                    upd_path = info.get("UPDATE_PACK");
                }
                if (info.containsKey("UPDATE_NAME")) {
                    load_path = info.get("UPDATE_NAME");
                }
                task.loadingFASize = 0;
                task.loadingFLSize = 0;

                for (String upd_path_part : upd_path.split(",")) {
                    int i = task.getFileSize(upd_path_part);
                    if (i > 0) {
                        task.loadingFASize += i;
                    } else {
                        error = true;
                        state = "Ошибка при загрузке обновления_Нажмите на экран чтобы попробовать снова";
                        return;
                    }
                }

                ArrayList<File> upd_files = new ArrayList<>();
                for (int i = 0; i < upd_path.split(",").length; i++) {
                    String url_pack = upd_path.split(",")[i];
                    File file_pack = AssetsTool.getFile(load_path.split(",")[i]);
                    if (!task.SYNC_GET(url_pack, true, file_pack)) {
                        state = task.result + "_Нажмите на экран чтобы попробовать снова";
                        error = true;
                        return;
                    } else {
                        upd_files.add(file_pack);
                    }
                }
                waitState(2);
                state = states[stateType];
                for (File f : upd_files) {
                    if (!AssetsTool.extractObb(f) || !f.delete()) {
                        state = "Ошибка при проверке целостности данных";
                        error = true;
                        return;
                    }
                    //if (f.delete()) write("UPDATING: Delete temp files");
                }
                property = AssetsTool.getParamFromFile(AssetsTool.readFile("menu/game.pref"));
                property.put("version", "" + VERSION);
                AssetsTool.setParamToFile("menu/game.pref", property);
            }

            // Авторизация
            waitState(3);
            if (world.prefs.contains("name")) {
                state = states[stateType];
                if (task.SYNC_GET(null, "mode", "" + NetTask.SIGN_IN, "mail", world.prefs.getString("mail").equals("@") ? world.prefs.getString("mail") : world.prefs.getString("name"), "pass", world.prefs.getString("pass"))) {
                    try {
                        user.set(new Json(JsonWriter.OutputType.json).fromJson(User.class, task.result));
                        user.setMessage(encodePlatform(user.getMessage(), false));
                    } catch (SerializationException e) {
                        user.reset();
                        user.setSuccess(-1);
                    }
                    if (user.getSuccess() == 1) auth = true;
                }
            }

            // Зазгрузка начальных ресурсов
            if (auth) state = states[stateType + 1];
            manager.setProperty("levels", property.get("levels"));
            Map<String, String> temp;
            for (int i = 0; i <= manager.getInt("levels"); i++) {
                manager.setProperty(i, "path", property.get("level" + i + "Path"));
                temp = AssetsTool.getParamFromFile(AssetsTool.readFile(manager.getString(i, "path") + "/level.pref"));
                manager.setProperty(i, "hints", temp.get("hintCount"));
                manager.setProperty(i, "classLoad", temp.get("classLoad"));
                //
                for (int j = 0; j < manager.getInt(i, "hints"); j++)
                    manager.setProperty(i, "hint" + j, temp.get("hint" + j));
                //
                for (int j = 0; j < Integer.parseInt(temp.get("loadFiles")); j++)
                    manager.add(i, temp.get("load" + j + "Name"), manager.getString(i, "path") + "/" + temp.get("load" + j + "Path"), temp.get("load" + j + "Type"));
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
