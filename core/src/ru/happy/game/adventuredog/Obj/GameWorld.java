package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.GamePrefs;
import ru.happy.game.adventuredog.Tools.NetTask;

public class GameWorld extends Stage {

    public static String FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя1234567890 .,:;_¡!¿?\"'+-*/()[]={}%@";
    public InputMultiplexer multiplexer;
    public int fontSize = 20;
    public String out;
    public boolean skipLevel, usedBonus, firstVisible, firstErrVisible, getBonus;
    public GamePrefs prefs;
    User user;
    NetTask task;
    OrthographicCamera camera;
    BitmapFont font, bigFont, smallFont, medFont, smedFont, custom;
    GlyphLayout glyphLayout;
    ShaderProgram fontShader;
    float aFloat = 1f;
    float[] sizes = new float[]{0, 0};
    private boolean fontShaderOn, synced, syncError, lockedAction;
    public GameWorld() {
        multiplexer = new InputMultiplexer();
        Gdx.input.setCatchBackKey(true);
        camera = new OrthographicCamera(MainGDX.WIDTH, MainGDX.HEIGHT);
        camera.setToOrtho(false, MainGDX.WIDTH, MainGDX.HEIGHT);
        setViewport(new FitViewport(MainGDX.WIDTH, MainGDX.HEIGHT));
        prefs = new GamePrefs("prefs");
        if (AssetsTool.isExists("shaders/font.vert")) {
            fontShader = new ShaderProgram(AssetsTool.getFileHandler("shaders/font.vert"), AssetsTool.getFileHandler("shaders/font.frag"));
            fontShaderOn = fontShader.isCompiled();
        }
    }

    public void setLockedAction(boolean lockedAction) {
        this.lockedAction = lockedAction;
    }

    public boolean isLockedAction() {
        return lockedAction;
    }

    public static String getEngCharset() {
        return FONT_CHARACTERS.substring(0, 52);
    }

    public static String getRusCharset() {
        return FONT_CHARACTERS.substring(52, 118);
    }

    public static String getNumCharset() {
        return FONT_CHARACTERS.substring(118, 128);
    }

    public static String getSymbolsCharset() {
        return FONT_CHARACTERS.substring(129);
    }

    public void LoadData() {
        user.setInWorld(prefs);
    }

    private void SyncData() {
        if (task == null) task = new NetTask(new NetTask.NetListener() {
            @Override
            public void onDownloadComplete(String filename) {
                User tmp;
                try {
                    tmp = new Json(JsonWriter.OutputType.json).fromJson(User.class, filename);
                } catch (SerializationException e) {
                    tmp = new User();
                    tmp.setSuccess(-1);
                    tmp.setMessage(e.getMessage());
                }
                //System.out.println(AssetsTool.encodePlatform(filename,true));
                tmp.setMessage(AssetsTool.encodePlatform(tmp.getMessage(), true));
                //User tmp = new Json(JsonWriter.OutputType.json).fromJson(User.class, isAndroid()?filename:AssetsTool.encodeString(filename,false));
                if (tmp.getSuccess() == 1) {
                    user.set(tmp);
                    LoadData();
                    prefs.putBoolean("sync", true);
                    prefs.flush();
                    synced = true;
                } else {
                    syncError = true;
                    out = tmp.getMessage();
                }
            }

            @Override
            public void onProgressUpdate(int progress) {

            }

            @Override
            public void onDownloadFailure(String msg) {
                out = msg;
                syncError = true;
            }
        });
        if (!synced) {
            task.API(NetTask.SYNCHRONIZATION, "mail", prefs.getString("name"), "pass", prefs.getString("pass"), "data", getLives() + "_" + getTicket() + "_" + getHelp() + "_" + prefs.getInteger("finished_level", 0) + "_" + prefs.getInteger("opened_level", 0) + "_" + MainGDX.VERSION);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (FONTS f : FONTS.values()) {
            if (getFont(f) != null) getFont(f).dispose();
        }
    }

    public void startSync() {
        if (MainGDX.OFFLINE) {
            synced = true;
        } else {
            synced = false;
            syncError = false;
            prefs.putBoolean("sync", false);
            prefs.flush();
            out = "Соединение с сервером...";
            SyncData();
        }
    }

    public boolean isSynced() {
        return synced;
    }

    public boolean isSyncError() {
        return syncError;
    }

    public void fontSetting() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/SimsRegular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = fontSize;
        parameter.characters = FONT_CHARACTERS;
        font = generator.generateFont(parameter);
        parameter.size = 15;
        smallFont = generator.generateFont(parameter);
        parameter.size = 20;
        smedFont = generator.generateFont(parameter);
        parameter.size = 40;
        medFont = generator.generateFont(parameter);
        parameter.size = 100;
        bigFont = generator.generateFont(parameter);
        glyphLayout = new GlyphLayout();
        generator.dispose();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        smedFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        smallFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        medFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bigFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        prefs.putBoolean("sync", false);
        prefs.flush();
        this.user = user;
    }

    public int getLives() {
        return user.getLives();
    }

    public void setLives(int lives) {
        user.setLives(lives);
        useLives(0);
    }

    public int getTicket() {
        return user.getTickets();
    }

    public void setTicket(int ticket) {
        user.setTickets(ticket);
        useTicket(0);
    }

    public int getHelp() {
        return user.getHelps();
    }

    public void setHelp(int help) {
        user.setHelps(help);
        useHelp(0);
    }

    public void useTicket() {
        useTicket(1);
    }

    public void useLives() {
        useLives(1);
    }

    public void useHelp() {
        useHelp(1);
    }

    public void useHelp(int c) {
        usedBonus = true;
        user.useHelp(c);
        prefs.putInteger("help", user.getHelps());
        prefs.flush();
    }

    public void useTicket(int c) {
        usedBonus = true;
        user.useTicket(c);
        prefs.putInteger("ticket", user.getTickets());
        prefs.flush();
    }

    public void useLives(int c) {
        user.useLives(c);
        prefs.putInteger("live", user.getLives());
        prefs.flush();
    }

    public void addTicket() {
        addTicket(1);
    }

    public void addLives() {
        addLives(1);
    }

    public void addHelp() {
        addHelp(1);
    }

    public void addTicket(int c) {
        useTicket(-c);
    }

    public void addLives(int c) {
        useLives(-c);
    }

    public void addHelp(int c) {
        useHelp(-c);
    }

    public void updateMultiplexer() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    public void resetMultiplexer() {
        multiplexer.clear();
        Gdx.input.setCatchBackKey(true);
        updateMultiplexer();
    }

    public void addProcessor(InputProcessor processor) {
        multiplexer.addProcessor(processor);
    }

    public float getAlpha() {
        return aFloat;
    }

    public void setAlpha(float alpha) {
        aFloat = alpha;
    }

    public void resize(float w, float h) {
        MainGDX.HEIGHT = (int) (h / w * MainGDX.WIDTH);
        camera.setToOrtho(false, MainGDX.WIDTH, MainGDX.HEIGHT);
        setViewport(new FitViewport(MainGDX.WIDTH, MainGDX.HEIGHT));
    }

    @Override
    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void setViewport(Viewport viewport) {
        super.setViewport(viewport);
    }

    @Override
    public Batch getBatch() {
        return super.getBatch();
    }

    @Override
    public void draw() {
        for (int i = getActors().size - 1; i >= 0; i--) {
            getActors(i).draw(getBatch(), getAlpha());
        }
    }

    public Actor getActors(int index) {
        return super.getActors().get(index);
    }

    public void setColor(float r, float g, float b, float a) {
        getBatch().setColor(r, g, b, a);
    }

    // Применение шейдера для шрифта
    public void fontSmooth(boolean b) {
        if (fontShaderOn) getBatch().setShader(b ? fontShader : null);
    }

    // Получить лайоут с текстом
    public GlyphLayout getGlyphLayout(String text, float size, Color color, FONTS font) {
        GlyphLayout gl = new GlyphLayout();
        getFont(font).setColor(color);
        getFont(font).getData().setScale(size);
        gl.setText(getFont(font), text);
        return gl;
    }

    // Получить и установить шрифт
    public BitmapFont getFont(FONTS ftype) {
        switch (ftype) {
            case SMALL:
                return smallFont;
            case MEDIAN:
                return medFont;
            case SMEDIAN:
                return smedFont;
            case BIG:
                return bigFont;
            case CUSTOM:
                return custom;
        }
        return font;
    }

    public void setFont(BitmapFont font) {
        this.font = font;
    }

    public void setFont(String font, int size, boolean external) {
        if (this.custom != null) custom.dispose();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(external ? AssetsTool.getFileHandler("fonts/" + font + ".ttf") : Gdx.files.internal("fonts/" + font + ".ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        fontSize = size;
        parameter.characters = FONT_CHARACTERS;
        this.custom = generator.generateFont(parameter);
        generator.dispose();
    }

    // Размеры текста
    public float[] getTextSize(String text, float size, FONTS ftype) {
        switch (ftype) {
            case CUR:
                font.getData().setScale(size);
                glyphLayout.setText(font, text);
                break;
            case SMALL:
                smallFont.getData().setScale(size);
                glyphLayout.setText(smallFont, text);
                break;
            case MEDIAN:
                medFont.getData().setScale(size);
                glyphLayout.setText(medFont, text);
                break;
            case SMEDIAN:
                smedFont.getData().setScale(size);
                glyphLayout.setText(smedFont, text);
                break;
            case BIG:
                bigFont.getData().setScale(size);
                glyphLayout.setText(bigFont, text);
                break;
        }
        sizes[0] = glyphLayout.width;
        sizes[1] = glyphLayout.height;
        return sizes;
    }

    // Размеры последнего текста
    public float[] getSizes() {
        return sizes;
    }

    // Вывод текста
    public void setText(String text, float size, float x, float y, Color color, boolean centered) {
        setText(text, size, x, y, color, centered, centered, font);
    }

    public void setText(String text, float size, float x, float y, Color color, boolean centered, FONTS ftype) {
        setText(text, size, x, y, color, centered, centered, ftype);
    }

    public void setText(GlyphLayout glyphLayout, float x, float y, boolean centerx, boolean centery, FONTS ftype) {
        if (fontShaderOn) getBatch().setShader(fontShader);
        getFont(ftype).draw(getBatch(), glyphLayout, centerx ? x - glyphLayout.width / 2 : x, centery ? y + glyphLayout.height / 2 : y);
        if (fontShaderOn) getBatch().setShader(null);
    }

    public void setText(String text, float size, float x, float y, Color color, Color stroke, boolean centered, FONTS ftype) {
        setText(text, size, x, y, color, stroke, centered, centered, ftype);
    }

    public void setText(String text, float size, float x, float y, Color color, boolean centerx, boolean centery, FONTS ftype) {
        setText(text, size, x, y, color, centerx, centery, getFont(ftype));
        sizes[0] = glyphLayout.width;
        sizes[1] = glyphLayout.height;
    }

    public void setText(String text, float size, float x, float y, Color color, boolean centerx, boolean centery, BitmapFont font) {
        font.setColor(color);
        font.getData().setScale(size);
        if (fontShaderOn) getBatch().setShader(fontShader);
        glyphLayout.setText(font, text);
        font.draw(getBatch(), glyphLayout, centerx ? x - glyphLayout.width / 2 : x, centery ? y + glyphLayout.height / 2 : y);
        if (fontShaderOn) getBatch().setShader(null);
    }

    public void setText(String text, float size, float x, float y, Color color, Color stroke, boolean centerx, boolean centery, FONTS ftype) {
        setText(text, size, x - 1, y - 1, stroke, centerx, centery, ftype);
        setText(text, size, x + 1, y + 1, stroke, centerx, centery, ftype);
        setText(text, size, x + 1, y - 1, stroke, centerx, centery, ftype);
        setText(text, size, x - 1, y + 1, stroke, centerx, centery, ftype);
        setText(text, size, x, y, color, centerx, centery, ftype);
    }

    public enum FONTS {CUR, SMALL, SMEDIAN, MEDIAN, BIG, CUSTOM}
}
