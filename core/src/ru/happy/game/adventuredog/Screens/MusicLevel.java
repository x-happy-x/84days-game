package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;

import java.io.File;
import java.util.ArrayList;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.LoadedMusic;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.GraphicTool;
import ru.happy.game.adventuredog.Tools.LevelSwitcher;
import ru.happy.game.adventuredog.Tools.NetTask;
import ru.happy.game.adventuredog.UI.Button;
import ru.happy.game.adventuredog.UI.ImageButton;
import ru.happy.game.adventuredog.UI.ImageView;
import ru.happy.game.adventuredog.UI.PlayerSlider;
import ru.happy.game.adventuredog.UI.TextEditor;

import static ru.happy.game.adventuredog.Tools.AssetsTool.isAndroid;
import static ru.happy.game.adventuredog.Tools.GraphicTool.addRectArea;
import static ru.happy.game.adventuredog.Tools.GraphicTool.toLocal;

public class MusicLevel implements Screen {

    boolean musicLoaded, infoLoaded, isCutting, isGuessed, levelMultiplexer, isChanging,
            typeSelected, isPause, isEnd, isWin, nextLoading;
    int musicN, guessed, musicCount, selectedQuest, selectedMenu, scoreMusic, newQuest,
            errors, musicL;
    float musicP, currentProgress, downloadProgress, pause_delta, showText_delta, end_delta;
    LoadedMusic musics;
    String titleText, showedText;
    NetTask netTask;
    Vector2 cursor;
    MainGDX game;

    ArrayList<Float> cut;
    ArrayList<String[]> quest;
    String[] pause_list = new String[]{"ПРОДОЛЖИТЬ", "ПРОПУСТИТЬ УРОВЕНЬ", "НАЧАТЬ СНАЧАЛА", "ПАРАМЕТРЫ", "ГЛАВНОЕ МЕНЮ"},
            end_list = new String[]{"СЛЕДУЮЩИЙ УРОВЕНЬ", "НАЧАТЬ СНАЧАЛА", "ПАРАМЕТРЫ", "ГЛАВНОЕ МЕНЮ"}, menuList;
    Rectangle[] menu;
    Sprite poster;
    Color mainBG, pauseC, lose, win;

    // Ресурсы
    TextureAtlas texture;
    Music music, cutting;

    // Графические элементы
    ImageView pic_bg, live_bg, fragment, title;
    ImageButton live_btn, ticket_btn, help_btn;
    ArrayList<Button> buttons;
    PlayerSlider slider;
    TextEditor input;
    Button ok, exit, next;

    public MusicLevel(MainGDX mainGDX) {
        // Объект игры
        game = mainGDX;

        // Параметры игры
        cursor = new Vector2();
        netTask = new NetTask();
        cut = new ArrayList<>();
        quest = new ArrayList<>();
        musics = new LoadedMusic();
        buttons = new ArrayList<>();
        ScreenAnim.level = musicN = -1;
        nextLoading = infoLoaded = musicLoaded = isGuessed = typeSelected = isEnd = isWin = isPause = false;
        showText_delta = pause_delta = end_delta = currentProgress = downloadProgress = musicCount = guessed = 0;

        //Получение цветов
        mainBG = Color.valueOf("#204051");
        win = Color.valueOf("#6D74FF");
        lose = Color.valueOf("#ff3300");
        pauseC = Color.valueOf("#f57d00");
        ArrayList<Color> colors = new ArrayList<>();
        for (String s : game.assets.getLevelFile("colors.pref").split(" "))
            colors.add(Color.valueOf("#" + s));
        // Настройка фона
        game.layout.ColorPrefs(colors);
        // Настройка прогресса загрузки
        game.layout.createProgressBar(MainGDX.WIDTH / 4f, 15, MainGDX.WIDTH / 2f, 10, Color.valueOf("#6E5C7F"));

        // Загрузка ресурсов
        texture = game.assets.get(game.manager.getGUI());
        cutting = game.assets.get("sound_cut");
        cutting.setLooping(false);

        // Кнопка далее
        ok = new Button("Далее", texture.findRegion("blue_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                showText_delta = 3f;
                if (musicCheck(input.getText())) {
                    isGuessed = true;
                    showedText = "Урааа вы угадали";
                    guessed++;
                } else {
                    errors++;
                    if (errors == 3){
                        game.world.useLives();
                        if (game.world.getLives() == 0) isEnd = true;
                        live_btn.setText(game.world.getLives()+"",game);
                        errors = 0;
                        showedText = "Ууупс, вы потеряли одну жизнь";
                    } else {
                        showedText = "Уже "+errors+" неверных попыток_Ещё "+(3-errors)+" и будет сюрприз";
                    }
                }
            }

            @Override
            public void isSelected() {

            }
        });
        ok.setWidth(100);
        ok.setPosition(MainGDX.WIDTH - 40 - ok.getWidth(), 30);
        ok.setUseGL(false);

        // Кнопка next
        next = new Button("НАЧАТЬ СЕЙЧАС", texture.findRegion("square_darkgray_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (musicL > 0) {
                    musicLoad(0);
                    ScreenAnim.setClose();
                    ScreenAnim.setState(true);
                }
            }

            @Override
            public void isSelected() {

            }
        });
        next.setPosition(MainGDX.WIDTH - 20 - next.getWidth(), 15);
        next.setUseGL(false);

        // Кнопка выход
        exit = new Button("Меню", texture.findRegion("red_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                isPause = true;
            }

            @Override
            public void isSelected() {

            }
        });
        exit.setPosition(40, 30);
        exit.setWidth(100);
        exit.setUseGL(false);

        // Поле для ввода названия
        input = new TextEditor("Введите название песни...", texture.findRegion("white_btn"), texture.findRegion("editIcon"), game.world, Color.valueOf("#000000"), Color.valueOf("#333333"));
        input.addIcon(texture.findRegion("done"), texture.findRegion("close"), texture.findRegion("loadblue"));
        input.setPosition(exit.getX() + exit.getWidth() + 20, exit.getY());
        input.setWidth(MainGDX.WIDTH - input.getX() - ok.getWidth() - 60);
        input.setMaxLength(60);
        input.setUseGL(false);
        /*input.setAction(new TextEditor.Action() {
            @Override
            public void isInput(String text) {
                if (musicCheck(text)) input.setDone(true);
                else input.setError(text.length() > 0);
            }

            @Override
            public void isClick() {

            }

            @Override
            public void isSelected() {

            }
        });*/

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

        // Постер
        poster = new Sprite();
        poster.setBounds(fragment.getX() + 10, fragment.getY() + 10, fragment.getWidth() - 20, fragment.getHeight() - 20);

        // Сведения о жизнях
        live_btn = new ImageButton(game.world.getLives() + "", texture.findRegion("gray2_btn"), texture.findRegion("live"), game.world, Color.BLACK);
        live_btn.setIconSize(live_btn.getHeight() - live_btn.getOffsetIY() * 2f);
        live_btn.setPosition(live_bg.getX() + 5, live_bg.getY() + 10);
        live_btn.setWidth(live_bg.getWidth() - 10);
        live_btn.setUseGL(false);

        // Сведения о пропусках
        ticket_btn = new ImageButton(game.world.getTicket() + "", texture.findRegion("gray2_btn"), texture.findRegion("ticket"), game.world, Color.BLACK);
        ticket_btn.setPosition(live_btn.getX(), live_bg.getY() + live_bg.getHeight() - ticket_btn.getHeight() - 10);
        ticket_btn.setIconSize(ticket_btn.getHeight() - ticket_btn.getOffsetIY() * 2f);
        ticket_btn.setWidth(live_btn.getWidth());
        ticket_btn.setAction(new Button.Action() {
            @Override
            public void isClick() {
                if (game.world.getTicket() > 0) {
                    game.world.useTicket();
                    ticket_btn.setText(game.world.getTicket()+"",game);
                    showedText = (selectedQuest>2?musics.musics.get(musicN).title:musics.musics.get(musicN).artist).split("__")[0];
                    showText_delta = 3f;
                    isGuessed = true;
                } else {
                    showText_delta = 3f;
                    showedText = "Уууупс у вас закончились подсказки";
                }
            }

            @Override
            public void isSelected() {

            }
        });
        ticket_btn.setUseGL(false);

        // Сведения о подсказках
        help_btn = new ImageButton(game.world.getHelp() + "", texture.findRegion("gray2_btn"), texture.findRegion("help"), game.world, Color.BLACK);
        help_btn.setPosition(live_btn.getX(), live_bg.getY() + live_bg.getHeight() / 2f - help_btn.getHeight() / 2f);
        help_btn.setIconSize(help_btn.getHeight() - help_btn.getOffsetIY() * 2f);
        help_btn.setWidth(live_btn.getWidth());
        help_btn.setAction(new Button.Action() {
            @Override
            public void isClick() {
                if (game.world.getHelp() > 0){
                    game.world.useHelp();
                    help_btn.setText(""+game.world.getHelp(),game);
                    showText_delta = 5f;
                    String name = (selectedQuest>2?musics.musics.get(musicN).title:musics.musics.get(musicN).artist).split("__")[0];
                    showedText = "Название песни:_"+name.substring(0,name.length()/2);
                    for (String s: name.substring(name.length()/2).split(" ")){
                        for (int i = 0; i < s.length(); i++) showedText += "*";
                        showedText += " ";
                    }
                } else {
                    showText_delta = 3f;
                    showedText = "Ой ой у вас нет подсказок";
                }
            }

            @Override
            public void isSelected() {

            }
        });
        help_btn.setUseGL(false);

        // Фон для заголовка при опросе
        title = new ImageView(texture.findRegion("square_darkgray_btn"));
        title.setRadius(50);
        title.setSize(MainGDX.WIDTH+50, MainGDX.WIDTH / 10f);
        title.setPosition(-25, -20);

        // Переход к первому вопросу
        changeQuest(0);

        // Установка слушателей
        setInputLevel();
        game.addSizeChangeListener((w1, h1) -> {
            Vector2 visibleArea = toLocal(w1, h1);
            if (visibleArea.y < MainGDX.HEIGHT / 1.1) {
                if (input.isEdit()) {
                    input.move(input.getX(), visibleArea.y + input.getHeight(), input.getWidth(), input.getHeight(), 0.3f);
                    ok.move(MainGDX.WIDTH - 40 - ok.getWidth(), visibleArea.y + ok.getHeight(),ok.getWidth(), ok.getHeight(), 0.3f);
                    exit.move(40, visibleArea.y + exit.getHeight(),exit.getWidth(), exit.getHeight(), 0.3f);
                }
            } else if ((int) visibleArea.y > MainGDX.HEIGHT - 10) {
                if (input.isEdit()) {
                    input.setEdit(false);
                    input.move(40 + exit.getWidth() + 20, 30,input.getWidth(),input.getHeight(), 0.3f);
                    ok.move(MainGDX.WIDTH - 40 - ok.getWidth(), 30,ok.getWidth(), ok.getHeight(), 0.3f);
                    exit.move(40, 30,exit.getWidth(), exit.getHeight(), 0.3f);
                }
            }
        });
        netTask.setListener(new NetTask.NetListener() {
            @Override
            public void onDownloadComplete(String msg) {
                if (infoLoaded) {
                    boolean isMusic = msg.endsWith(".mp3");
                    if (isMusic){
                        nextLoading = false;
                        musicL++;
                    }
                    game.manager.add(game.assets.getLevel(), msg,msg, isMusic? "M" : "T");
                    if (!typeSelected && musics.musics.get(musics.musics.size()-1).path.contains(msg)) {
                        musicLoad(0);
                        ScreenAnim.setClose();
                        ScreenAnim.setState(true);
                    }
                } else {
                    try {
                        musics = new Json().fromJson(LoadedMusic.class, msg);
                    } catch (SerializationException e) {
                        musics.success = 0;
                        musics.message = msg;
                    }
                    if (musics.success == 0 || musics.musics == null || musics.musics.size() == 0) {
                        System.out.println(11);
                    } else {
                        ArrayList<File> mFiles = new ArrayList<>();
                        //File[] mFiles = new File[musics.musics.size()*2];
                        musicCount = 0;
                        for (int i = 0; i < musics.musics.size(); i++) {
                            //musics.musics.get(i).image = "cache/"+musics.musics.get(i).image;
                            //musics.musics.get(i).path = "cache/"+musics.musics.get(i).path;
                            mFiles.add(AssetsTool.getFile(musics.musics.get(i).image, false));
                            mFiles.add(AssetsTool.getFile(musics.musics.get(i).path, false));
                            musicCount++;
                            //mFiles[i++] = AssetsTool.getFile(musics.musics.get(i).image, false);
                            //mFiles[i] = AssetsTool.getFile(musics.musics.get(i).path, false);
                        }
                        infoLoaded = true;
                        netTask.hardRun = true;
                        netTask.loadFiles("musics/", mFiles.toArray());
                    }
                }
                //game.manager.add(game.assets.getLevel(),"music1");
            }

            @Override
            public void onProgressUpdate(int progress) {
                if (infoLoaded) {
                    if (progress == 100) {
                        downloadProgress++;
                        currentProgress = 0;
                    } else {
                        currentProgress = progress / 100f;
                    }
                }
            }

            @Override
            public void onDownloadFailure(String msg) {
                showedText = "Не удалось загрузить";
            }
        });
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        cursor = GraphicTool.getClick();
        game.drawShape();
        game.layout.drawColoredRectangle(game.renderer, delta);
        game.endShape();
        game.draw();
        if (typeSelected) playerUpdate(delta);
        else drawQuest(delta);
        //if (music != null)
        //    game.world.setText(musics.musics.get(musicN).artist, 1f, MainGDX.WIDTH / 2f, 40, Color.BLUE, true);
        if (infoLoaded && !typeSelected && musicL > 0) {
            next.setCursor(cursor);
            next.draw(game,delta);
        }
        game.end();
        if (infoLoaded && !typeSelected) {
            game.drawShape();
            game.layout.drawProgressBar(game.renderer, (downloadProgress + currentProgress) / (musics.musics.size() * 2));
            game.endShape();
        }
        if (isPause) {
            if (levelMultiplexer) {
                menuList = pause_list;
                setInputMenu();
            }
            if (pause_delta < 1f) pause_delta += delta;
            else if (pause_delta > 1f) {
                pause_delta = 1f;
                if (music != null) music.pause();
                slider.setPaused(true);
            }
        } else if (pause_delta != 0) {
            if (slider.isPaused())
            if (!levelMultiplexer) setInputLevel();
            if (pause_delta > 0f) pause_delta -= delta;
            else if (pause_delta < 0f) pause_delta = 0f;
        }
        if (isEnd) {
            if (levelMultiplexer) {
                menuList = end_list;
                setInputMenu();
            }
            if (end_delta < 1f) end_delta += delta;
            else if (end_delta > 1f) {
                end_delta = 1f;
                //music.pause();
                //slider.setPaused(true);
            }
        }
        if (end_delta > 0) {
            if (music != null) music.setVolume(Math.max(1 - end_delta,0.2f));
            menu = game.layout.drawEnd(game, menuList, isWin?"Поздравляшки :)":"Печалька :(", end_delta, game.interpolation, isWin, selectedMenu, isWin?win:lose, mainBG);
        } else if (pause_delta > 0) {
            if (music != null) music.setVolume(1 - pause_delta);
            menu = game.layout.drawPause(game, menuList, pause_delta, game.interpolation, guessed, -1, musicCount, selectedMenu, pauseC, mainBG);
        }
        if (ScreenAnim.getState()) {
            game.drawShape();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (ScreenAnim.show(game)) {
                if (ScreenAnim.isClosing()) {
                    if (ScreenAnim.level >= 0)
                        LevelSwitcher.setLevel(game, ScreenAnim.level);
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
        if (music != null) {
            music.stop();
            music.dispose();
        }
        if (musics != null && musics.musics != null && musics.musics.size() > 0) {
            for (int i = 0; i < musics.musics.size(); i++) {
                AssetsTool.getFile(musics.musics.get(i).image).delete();
                AssetsTool.getFile(musics.musics.get(i).path).delete();
                game.manager.delete(musics.musics.get(i).image);
                game.manager.delete(musics.musics.get(i).path);
            }
        }
    }

    // Перейти к другому вопросу
    private void changeQuest(int qq) {
        if (quest.size() == 0) {
            for (String question : AssetsTool.encodePlatform(game.assets.getLevelFile("list menu.pref")).split("\n\n")) {
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
            buttons.add(new Button(q[i].split("-")[0], texture.findRegion("square_darkgray_btn"), game.world, Color.WHITE, new Button.Action() {
                @Override
                public void isClick() {
                    String s = q[finalI].split("-")[1];
                    if (s.length() > 2) {
                        titleText = "Загрузка...";
                        for (Button b : buttons) {
                            b.setActive(false);
                            b.move(b.getX(), -b.getY(), b.getWidth(), b.getHeight(), 1);
                        }
                        netTask.GET("", "mode", "5", "type", s.substring(4));
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
        game.world.setText(titleText, 1f, MainGDX.WIDTH / 2f, title.getHeight() / 2f, Color.WHITE, true, GameWorld.FONTS.SMEDIAN);

        if (isChanging) changeQuest(newQuest);
    }

    // Установить слушатели для уровня
    public void setInputLevel() {
        levelMultiplexer = true;
        game.world.resetMultiplexer();
        game.world.addProcessor(new InputAdapter() {
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
        });
        game.world.addProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int button) {
                Vector2 v = toLocal(x, y);
                v.set(v.x, MainGDX.HEIGHT - v.y);
                if (typeSelected) {
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
                } else {
                    if (next.isActive()) next.isClick(v);
                    if (buttons != null) for (Button b : buttons) if (b.isActive()) b.isClick(v);
                }
                return false;
            }
        }));
        game.world.updateMultiplexer();
        game.world.updateMultiplexer();
    }
    // Установить слушатели для меню
    public void setInputMenu() {
        levelMultiplexer = false;
        game.world.resetMultiplexer();
        game.world.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK)
                    isPause = false;
                return false;
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
        });
        game.world.addProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
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
                                break;
                            case "пропустить уровень":
                                if (game.world.getTicket() < 10){
                                    System.out.println(10);
                                }
                                break;
                            case "следующий уровень":
                                if (game.manager.getInt("levels") >= game.assets.getLevel()) {
                                    ScreenAnim.setState(true);
                                    ScreenAnim.setClose();
                                    ScreenAnim.level = game.assets.getLevel() + 1;
                                    if (music != null) music.pause();
                                }
                                break;
                            case "начать сначала":
                                isPause = false;
                                if (game.world.getLives() > 0) {
                                    ScreenAnim.setState(true);
                                    ScreenAnim.setClose();
                                    ScreenAnim.level = game.assets.getLevel();
                                    if (music != null) music.pause();
                                }
                                break;
                            case "главное меню":
                                isPause = false;
                                ScreenAnim.setState(true);
                                ScreenAnim.setClose();
                                ScreenAnim.level = 0;
                                if (music != null) music.pause();
                                slider.setPaused(true);
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
                            if (game.world.getTicket() < 10) {
                                System.out.println(10);
                            } else {
                                isPause = false;
                                game.world.useTicket(10);
                                game.world.skipLevel = true;
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
        }));
        game.world.updateMultiplexer();
        game.world.updateMultiplexer();
    }

    // Перемотка музыки
    private boolean cutCheck(float start, float end) {
        if (musicP >= start && musicP < end - 0.1) {
            music.pause();
            isCutting = true;
            slider.setActive(false);
            cutting.setOnCompletionListener(m -> {
                music.play();
                slider.setActive(true);
                music.setPosition(end);
                isCutting = false;
            });
            cutting.play();
        }
        return isCutting;
    }

    // Обновление музыки
    private void musicUpdate() {
        if (!game.assets.updating() && musicLoaded) {
            if (music != null) {
                music.stop();
                music.dispose();
            }
            music = game.assets.get(musics.musics.get(musicN).path);
            poster.setRegion((Texture) game.assets.get(musics.musics.get(musicN).image));
            float   w = poster.getRegionWidth(), h = poster.getRegionHeight(),
                    ow = poster.getWidth(), oh = poster.getHeight();
            if (ow / oh > w / h) {
                poster.setRegion(0, 0, (int) w, (int) (oh / ow * w));
                poster.setRegion(0, (int) ((h - poster.getRegionHeight()) / 2f), poster.getRegionWidth(), poster.getRegionHeight());
            } else {
                poster.setRegion(0, 0, (int) (ow / oh * h), (int) h);
                poster.setRegion((int) ((w - poster.getRegionWidth()) / 2f), 0, poster.getRegionWidth(), poster.getRegionHeight());
            }
            music.play();
            slider.setPaused(false);
            slider.setActive(true);
            musicLoaded = false;
        }
        if (music != null) {
            if (slider.isPaused()) {
                music.pause();
            } else if (!isCutting && music.isPlaying()) {
                musicP = music.getPosition();
                for (int i = 0; i < cut.size(); i++)
                    if (cutCheck(cut.get(i++), cut.get(i))) break;
                if (slider.isSelected()) music.pause();
                slider.setValue(musicP);
            } else if (!isCutting && !slider.isSelected()) {
                musicP = slider.getValue();
                music.play();
                music.setPosition(musicP);
            }
        }
    }
    // Загрузка музыки
    private void musicLoad(int N) {
        musicN = N;
        game.assets.load(musics.musics.get(N).path);
        game.assets.load(musics.musics.get(N).image);
        slider.setValues(0, (int) musics.musics.get(N).len);
        slider.setValue(0);
        slider.setPaused(true);
        musicLoaded = true;
        cut.clear();
        String cutT = musics.musics.get(N).cut;
        if (!cutT.equalsIgnoreCase("0")) {
            for (String s : cutT.split(" ")) {
                for (String ss : s.split("-")) {
                    cut.add(Float.parseFloat(ss));
                }
            }
        }
    }
    // Проверка названия
    public boolean musicCheck(String x) {
        scoreMusic = 100;
        String[] names = (selectedQuest>2?musics.musics.get(musicN).title:musics.musics.get(musicN).artist).split("__");
        x = x.replace("\n", "").replace("ё", "е").replace("Ё", "Е");
        for (String s : names) {
            String name = s.trim().replace('ё', 'е').replace('Ё', 'Е');
            if (name.equals(x.trim())) return true;
            scoreMusic -= 15;
            if (name.equalsIgnoreCase(x.trim())) return true;
            scoreMusic -= 10;
            for (String j : ".,:;_¡!¿?\"'+-*/()[]={}%".split("")) {
                name = name.replace(j, "");
                x = x.replace(j, "");
            }
            if (name.equals(x.trim())) return true;
            scoreMusic -= 10;
            if (name.equalsIgnoreCase(x.trim())) return true;
            scoreMusic -= 15;
            scoreMusic -= 50 / names.length;
        }
        return false;
    }
    // Обновление плейера
    private void playerUpdate(float delta) {
        game.getBatch().setColor(0, 0, 0, 1);
        fragment.draw(game);
        game.getBatch().setColor(1, 1, 1, 1);
        if (isGuessed && poster.getTexture() != null) {
            poster.draw(game.getBatch());
        } else {
            float height = MainGDX.HEIGHT / 3f;
            game.getBatch().draw(texture.findRegion("ask"), fragment.getX() + fragment.getWidth() / 2f - height / 5 * 2, fragment.getY() + fragment.getHeight() / 2f - height / 2f, height / 5 * 4, height);
        }
        if (showText_delta > 0f){
            if (!nextLoading) showText_delta-=delta;
            String[] ss = showedText.split("_");
            for (int i = 0; i < ss.length; i++){
                game.world.setText(ss[ss.length-1-i],1f,fragment.getX()+fragment.getWidth()/2f,fragment.getY()+MainGDX.HEIGHT/20f+game.world.getSizes()[1]*1.5f*i,Color.WHITE,Color.BLACK,true, GameWorld.FONTS.SMEDIAN);
            }
        } else if (isGuessed) {
            if (musicN + 1 == musics.musics.size()){
                isWin = true;
                isEnd = true;
            } else if (musicN+1 == musicL) {
                showedText = "Загрузка следующей песни_Расслабтесь, выйпейте чашечку чая и вернитесь в игру завтра";
                nextLoading = true;
                showText_delta = 1;
            } else {
                input.setText("", game);
                slider.setPaused(true);
                music.pause();
                isGuessed = false;
                new Thread(() -> musicLoad(musicN + 1)).start();
            }
        }
        pic_bg.draw(game);
        live_bg.draw(game);

        game.world.setText("Угадано:", 1.3f, pic_bg.getX() + 20, pic_bg.getY() + pic_bg.getHeight() - 20, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        game.world.setText("Всего:", 1.3f, pic_bg.getX() + 20, pic_bg.getY() + pic_bg.getHeight() - 20 - game.world.getSizes()[1] * 2f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);

        game.world.setText("" + guessed, 1.3f, pic_bg.getX() + pic_bg.getWidth() - 20 - game.world.getTextSize("" + guessed, 1.3f, GameWorld.FONTS.SMEDIAN)[0], pic_bg.getY() + pic_bg.getHeight() - 20, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);
        game.world.setText("" + musicCount, 1.3f, pic_bg.getX() + pic_bg.getWidth() - 20 - game.world.getTextSize("" + musicCount, 1.3f, GameWorld.FONTS.SMEDIAN)[0], pic_bg.getY() + pic_bg.getHeight() - 20 - game.world.getSizes()[1] * 2f, Color.DARK_GRAY, false, GameWorld.FONTS.SMEDIAN);

        game.world.setText("Чтобы использовать подсказку нажмите на неё",0.5f,pic_bg.getX()+pic_bg.getWidth()/2f,(live_bg.getY()+pic_bg.getY())/2f+2,Color.DARK_GRAY,true, GameWorld.FONTS.SMALL);
        if (levelMultiplexer) {
            ok.setCursor(cursor);
            exit.setCursor(cursor);
            input.setCursor(cursor);
            slider.setCursor(cursor);
            live_btn.setCursor(cursor);
            help_btn.setCursor(cursor);
            ticket_btn.setCursor(cursor);
        }
        slider.draw(game, delta);
        live_btn.draw(game, delta);
        help_btn.draw(game, delta);
        ticket_btn.draw(game, delta);
        ok.draw(game, delta);
        exit.draw(game, delta);
        input.draw(game, delta);
        musicUpdate();
    }
}
