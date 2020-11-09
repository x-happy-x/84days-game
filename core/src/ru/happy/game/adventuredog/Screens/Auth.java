package ru.happy.game.adventuredog.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;

import java.util.regex.Pattern;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Obj.User;
import ru.happy.game.adventuredog.Tools.AssetsTool;
import ru.happy.game.adventuredog.Tools.NetTask;
import ru.happy.game.adventuredog.Tools.GraphicTool;
import ru.happy.game.adventuredog.UI.Button;
import ru.happy.game.adventuredog.UI.Slider;
import ru.happy.game.adventuredog.UI.TextEditor;

import static ru.happy.game.adventuredog.Tools.AssetsTool.isAndroid;
import static ru.happy.game.adventuredog.Tools.GraphicTool.toLocal;
import static ru.happy.game.adventuredog.Tools.LevelSwitcher.setLevel;

public class Auth implements Screen {

    MainGDX game;
    NetTask task, task1;
    private int state, new_state, sex;
    private boolean showing, backspaced, keyboardOpen, stateChanging, _reg_;
    private float deltaT, deltaE, alpha, rotate;
    private Button reg, auth, ok, back, sexWoman, sexMan, resend;
    private TextEditor name, email, pass;
    private Slider age;
    User tmpUser;

    enum QueryType {SignIn, LogIn, codeCheck, mailCheck, nameCheck, uCheck}

    QueryType tQuery;
    boolean bQuery;
    public static Color textColor = new Color(1, 1, 1, 1);
    Rectangle orig;
    String error;
    Vector2 errorPos;
    Vector2 visibleArea;
    TextureAtlas atlas;
    Sprite bg;
    public static final Pattern VALID_EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public Auth(MainGDX mainGDX) {
        tmpUser = new User();
        game = mainGDX;
        error = "";
        errorPos = new Vector2();
        visibleArea = new Vector2(MainGDX.WIDTH, MainGDX.HEIGHT);
        orig = new Rectangle();
        deltaT = 0;
        deltaE = -1f;
        showing = true;
        keyboardOpen = false;
        atlas = game.assets.get(game.manager.getGUI());
        bg = new Sprite();
        bg.setBounds(0, 0, MainGDX.WIDTH, MainGDX.HEIGHT);
        bg.setTexture(game.assets.get(game.assets.bg));
        float w = bg.getTexture().getWidth(), h = bg.getTexture().getHeight();
        if ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() > w / h) {
            bg.setRegion(0, 0, (int) w, (int) ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() * w));
            bg.setRegion(0, (int) ((h - bg.getRegionHeight()) / 2f), bg.getRegionWidth(), bg.getRegionHeight());
        } else {
            bg.setRegion(0, 0, (int) ((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * h), (int) h);
            bg.setRegion((int) ((w - bg.getRegionWidth()) / 2f), 0, bg.getRegionWidth(), bg.getRegionHeight());
        }
        reg = new Button("НОВАЯ ИГРА", atlas.findRegion("red_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                _reg_ = true;
                changeState(2);
            }

            @Override
            public void isSelected() {

            }
        });
        auth = new Button("ЕСТЬ АККАУНТ", atlas.findRegion("blueL_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                _reg_ = false;
                changeState(5);
            }

            @Override
            public void isSelected() {

            }
        });
        ok = new Button("ДАЛЕЕ", atlas.findRegion("green_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (state == 0) {
                    LogIn((game.world.prefs.getString("mail").equals("@") ? game.world.prefs.getString("mail") : game.world.prefs.getString("name")), game.world.prefs.getString("pass", "None"));
                } else if (state == 2) {
                    if (!ok.isActive()) {
                        error = "Заполните все данные";
                        errorPos.set(MainGDX.WIDTH / 2f, ok.getY() + ok.getHeight() / 2f);
                        deltaE = 0f;
                    } else {
                        checkName(name.getText());
                        tQuery = QueryType.uCheck;
                    }
                } else if (state == 3) {
                    if (!ok.isActive()) {
                        error = "Заполните все данные";
                        errorPos.set(MainGDX.WIDTH / 2f, ok.getY() + ok.getHeight() / 2f);
                        deltaE = 0f;
                    } else {
                        tmpUser.setAge(age.getValue());
                        tmpUser.setSex(sex);
                        tmpUser.setName(name.getText());
                        tmpUser.setPass(pass.getText());
                        tmpUser.setMail(email.getText());
                        SignIn(tmpUser);
                        changeState(state + 1);
                    }
                } else if (state == 4) {
                    if (tmpUser.getId() != 0 && pass.getText().length() == 6) {
                        setCode(tmpUser.getId(), Integer.parseInt(pass.getText()));
                    }
                } else if (state == 5) {
                    if (ok.isActive()) LogIn(email.getText(), pass.getText());
                }
            }

            @Override
            public void isSelected() {

            }
        });
        resend = new Button("ОТПРАВИТЬ СНОВА", atlas.findRegion("blue_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                SignIn(tmpUser);
            }

            @Override
            public void isSelected() {

            }
        });
        back = new Button("НАЗАД", atlas.findRegion("red_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (_reg_) changeState(state - 1);
                else changeState(1);
            }

            @Override
            public void isSelected() {

            }
        });
        sexMan = new Button("МУЖСКОЙ", atlas.findRegion("white_btn"), game.world, Color.GRAY, new Button.Action() {
            @Override
            public void isClick() {
                if (sex == 0) {
                    sexWoman.setTexture(atlas.findRegion("white_btn"));
                    sexWoman.setTextColor(Color.GRAY);
                    sexMan.setTexture(atlas.findRegion("blue_btn"));
                    sexMan.setTextColor(Color.WHITE);
                    sexMan.setText(sexMan.getText(), game);
                    sexWoman.setText(sexWoman.getText(), game);
                    sex = 1;
                }
            }

            @Override
            public void isSelected() {

            }
        });
        sexWoman = new Button("ЖЕНСКИЙ", atlas.findRegion("blue_btn"), game.world, Color.WHITE, new Button.Action() {
            @Override
            public void isClick() {
                if (sex == 1) {
                    sexMan.setTexture(atlas.findRegion("white_btn"));
                    sexMan.setTextColor(Color.GRAY);
                    sexWoman.setTexture(atlas.findRegion("blue_btn"));
                    sexWoman.setTextColor(Color.WHITE);
                    sexMan.setText(sexMan.getText(), game);
                    sexWoman.setText(sexWoman.getText(), game);
                    sex = 0;
                }
            }

            @Override
            public void isSelected() {

            }
        });
        name = new TextEditor("Введите имя", atlas.findRegion("white_btn"), atlas.findRegion("editIcon"), game.world, Color.DARK_GRAY, new TextEditor.Action() {
            @Override
            public void isInput(String text) {
                if (state == 2) {
                    if (text.length() < 2) name.setError(true);
                    else {
                        name.setLoad(true);
                        checkName(text);
                    }
                }
            }

            @Override
            public void isClick() {
                name.setEdit(true);
                Gdx.input.setOnscreenKeyboardVisible(true);
            }

            @Override
            public void isSelected() {

            }
        });
        email = new TextEditor("Введите почту", atlas.findRegion("white_btn"), atlas.findRegion("editIcon"), game.world, Color.DARK_GRAY, new TextEditor.Action() {
            @Override
            public void isInput(String text) {
                if (state == 3 || state == 2) {
                    if (text.length() == 0) {
                        email.setDone(true);
                    } else if (VALID_EMAIL.matcher(text).find()) {
                        checkMail(text);
                        email.setLoad(true);
                    } else {
                        email.setError(true);
                        game.user.setSuccess(3);
                    }
                } else if (state == 5) {
                    bQuery = false;
                    if (text.length() < 2) {
                        email.setError(true);
                    } else {
                        email.setDone(true);
                    }
                }
            }

            @Override
            public void isClick() {
                pass.setEdit(false);
                if (keyboardOpen) {
                    pass.move(auth.getX(), auth.getY(), auth.getWidth(), auth.getHeight(), 0.3f);
                    email.move(MainGDX.WIDTH / 16f, visibleArea.y + email.getHeight(), MainGDX.WIDTH - MainGDX.WIDTH / 8f, email.getHeight(), 0.3f);
                }
                email.setEdit(true);
                Gdx.input.setOnscreenKeyboardVisible(true);
            }

            @Override
            public void isSelected() {

            }
        });
        pass = new TextEditor("Введите пароль", atlas.findRegion("white_btn"), atlas.findRegion("editIcon"), game.world, Color.DARK_GRAY, new TextEditor.Action() {
            @Override
            public void isInput(String text) {
                if (text.length() < 4) {
                    pass.setError(true);
                } else pass.setDone(true);
            }

            @Override
            public void isClick() {
                email.setEdit(false);
                if (keyboardOpen)
                    email.move(reg.getX(), visibleArea.y + reg.getHeight() * 2.5f, reg.getWidth(), reg.getHeight(), 0.3f);
                pass.setEdit(true);
                Gdx.input.setOnscreenKeyboardVisible(true);
            }

            @Override
            public void isSelected() {

            }
        });
        age = new Slider(Color.valueOf("#5090ff"), Color.valueOf("#f1f1f1"));
        pass.addIcon(atlas.findRegion("done"), atlas.findRegion("close"), atlas.findRegion("loadblue"));
        email.addIcon(atlas.findRegion("done"), atlas.findRegion("close"), atlas.findRegion("loadblue"));
        name.addIcon(atlas.findRegion("done"), atlas.findRegion("close"), atlas.findRegion("loadblue"));
        name.setSize(MainGDX.WIDTH / 2f - MainGDX.HEIGHT / 8f, MainGDX.HEIGHT / 10f);
        name.setPosition(MainGDX.WIDTH / 2f - name.getWidth() / 2f, MainGDX.HEIGHT - name.getHeight() * 3f);
        sexWoman.setSize(name.getWidth() / 2.2f, name.getHeight());
        sexWoman.setPosition(name.getX(), name.getY() - name.getHeight() * 4.8f);
        sexMan.setSize(sexWoman.getWidth(), sexWoman.getHeight());
        sexMan.setPosition(name.getX() + name.getWidth() - sexMan.getWidth(), sexWoman.getY());
        name.setMaxLength(40);
        email.setMaxLength(40);
        pass.setMaxLength(30);
        reg.setSize(MainGDX.WIDTH / 2f, MainGDX.HEIGHT / 10f);
        reg.setPosition(MainGDX.WIDTH / 4f, MainGDX.HEIGHT / 2f + MainGDX.HEIGHT / 18f);
        auth.setSize(MainGDX.WIDTH / 2f, MainGDX.HEIGHT / 10f);
        auth.setPosition(MainGDX.WIDTH / 4f, MainGDX.HEIGHT / 2f - MainGDX.HEIGHT / 8f);
        age.setSizeSlider(60, 40);
        age.setValues(1, 99);
        age.setValue(18);
        sex = 0;
        if (game.world.prefs.getString("mail", "None").equals("None")) state = 1;
        else state = 0;
        task = new NetTask(new NetTask.NetListener() {
            @Override
            public void onDownloadComplete(String filename) {
                bQuery = true;
                try {
                    game.user.set(new Json(JsonWriter.OutputType.json).fromJson(User.class, filename));
                } catch (SerializationException e) {
                    game.user.reset();
                    game.user.setSuccess(-1);
                    game.user.setMessage(e.getMessage());
                }
                //game.user.setMessage(isAndroid() ? game.user.getMessage() : AssetsTool.encodeString(game.user.getMessage(), false));
                if (tQuery == QueryType.SignIn) {
                    tmpUser.setId(game.user.getId());
                } else if (tQuery == QueryType.codeCheck) {
                    if (game.user.getSuccess() == 1) pass.setDone(true);
                    else pass.setError(true);
                } else if (tQuery == QueryType.mailCheck) {
                    if (game.user.getSuccess() == 1 || game.user.getSuccess() == 0)
                        email.setDone(true);
                    else email.setError(true);
                } else if (tQuery == QueryType.nameCheck) {
                    if (game.user.getSuccess() == 1) name.setDone(true);
                    else name.setError(true);
                }
            }

            @Override
            public void onProgressUpdate(int progress) {

            }

            @Override
            public void onDownloadFailure(String msg) {
                bQuery = true;
                game.user.setSuccess(-1);
                game.user.setMessage(msg);
                //if (state == 2) name.setError(true);
            }
        });
        game.world.setFont("SimsBold", 30, false);
        game.world.resetMultiplexer();
        game.world.addProcessor(new GestureDetector(new GestureDetector.GestureAdapter() {
            @Override
            public boolean tap(float x, float y, int count, int button) {
                if (!stateChanging) {
                    Vector2 v = toLocal(x, y);
                    v.set(v.x, MainGDX.HEIGHT - v.y);
                    if (state <= 1) {
                        reg.isClick(v);
                        auth.isClick(v);
                        if (state == 0) ok.isClick(v);
                    } else if (state == 2) {
                        if (!name.isClick(v)) {
                            if (!keyboardOpen) name.setEdit(false);
                            Gdx.input.setOnscreenKeyboardVisible(false);
                        }
                        sexWoman.isClick(v);
                        sexMan.isClick(v);
                        ok.isClick(v);
                        back.isClick(v);
                    } else if (state == 3) {
                        if (!email.isClick(v) && !pass.isClick(v)) {
                            if (!keyboardOpen) {
                                email.setEdit(false);
                                pass.setEdit(false);
                            }
                            Gdx.input.setOnscreenKeyboardVisible(false);
                        }
                        ok.isClick(v);
                        back.isClick(v);
                    } else if (state == 4) {
                        if (!pass.isClick(v)) {
                            if (!keyboardOpen) {
                                pass.setEdit(false);
                            }
                            Gdx.input.setOnscreenKeyboardVisible(false);
                        }
                        ok.isClick(v);
                        resend.isClick(v);
                        back.isClick(v);
                    } else if (state == 5) {
                        if (!email.isClick(v) && !pass.isClick(v)) {
                            if (!keyboardOpen) {
                                email.setEdit(false);
                                pass.setEdit(false);
                            }
                            Gdx.input.setOnscreenKeyboardVisible(false);
                        }
                        ok.isClick(v);
                        back.isClick(v);
                    }
                }
                return false;
            }
        }));
        game.world.addProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.BACKSPACE) backspaced = false;
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (!stateChanging) {
                    if (keycode == Input.Keys.BACKSPACE) backspaced = true;
                    if (backspaced) {
                        if (name.isEdit()) name.removeLast(game);
                        else if (email.isEdit()) email.removeLast(game);
                        else if (pass.isEdit()) pass.removeLast(game);
                    } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                        if (state <= 1) {
                            Gdx.app.exit();
                        } else if (state == 2) {
                            if (name.isEdit()) {
                                if (!keyboardOpen) name.setEdit(false);
                                else Gdx.input.setOnscreenKeyboardVisible(false);
                            } else {
                                changeState(state - 1);
                            }
                        } else if (state == 3) {
                            if (email.isEdit() || pass.isEdit()) {
                                if (!keyboardOpen) {
                                    email.setEdit(false);
                                    pass.setEdit(false);
                                } else Gdx.input.setOnscreenKeyboardVisible(false);
                            } else {
                                changeState(state - 1);
                            }
                        } else if (state == 4) {
                            if (pass.isEdit()) {
                                if (!keyboardOpen) {
                                    pass.setEdit(false);
                                } else Gdx.input.setOnscreenKeyboardVisible(false);
                            } else {
                                changeState(state - 1);
                            }
                        } else if (state == 5) {
                            if (email.isEdit() || pass.isEdit()) {
                                if (!keyboardOpen) {
                                    email.setEdit(false);
                                    pass.setEdit(false);
                                } else Gdx.input.setOnscreenKeyboardVisible(false);
                            } else {
                                changeState(1);
                            }
                        }
                    } else if (keycode == Input.Keys.ENTER) {
                        if (state == 2) {
                            if (!keyboardOpen) name.setEdit(false);
                            Gdx.input.setOnscreenKeyboardVisible(false);
                        } else if (state == 3 || state == 5) {
                            if (email.isEdit()) {
                                email.setEdit(false);
                                if (keyboardOpen) {
                                    pass.move(MainGDX.WIDTH / 16f, visibleArea.y + pass.getHeight(), MainGDX.WIDTH - MainGDX.WIDTH / 8f, pass.getHeight(), 0.3f);
                                    email.move(reg.getX(), visibleArea.y + pass.getHeight() * 2.5f, reg.getWidth(), reg.getHeight(), 0.3f);
                                }
                                pass.setEdit(true);
                            } else {
                                if (!keyboardOpen) pass.setEdit(false);
                                Gdx.input.setOnscreenKeyboardVisible(false);
                            }
                        } else if (state == 4) {
                            if (pass.isEdit()) {
                                if (keyboardOpen) Gdx.input.setOnscreenKeyboardVisible(false);
                                else pass.setEdit(false);
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                if (!stateChanging && GameWorld.FONT_CHARACTERS.contains(String.valueOf(character))) {
                    if (state == 2 && name.isEdit()) name.add(character, game);
                    else if ((state == 3 || state == 5) && email.isEdit())
                        email.add(character, game);
                    else if ((state == 3 || state == 4 || state == 5) && pass.isEdit())
                        pass.add(character, game);
                }
                return false;
            }
        });
        game.world.updateMultiplexer();
        game.addSizeChangeListener((w1, h1) -> {
            toLocal(visibleArea, w1, h1);
            if (visibleArea.y < MainGDX.HEIGHT / 1.1) {
                keyboardOpen = true;
                if (name.isEdit()) {
                    name.move(MainGDX.WIDTH / 16f, visibleArea.y + name.getHeight(), MainGDX.WIDTH - MainGDX.WIDTH / 8f, name.getHeight(), 0.3f);
                }
                if (email.isEdit()) {
                    email.move(MainGDX.WIDTH / 16f, visibleArea.y + email.getHeight(), MainGDX.WIDTH - MainGDX.WIDTH / 8f, email.getHeight(), 0.3f);
                }
                if (pass.isEdit()) {
                    pass.move(MainGDX.WIDTH / 16f, visibleArea.y + pass.getHeight(), MainGDX.WIDTH - MainGDX.WIDTH / 8f, pass.getHeight(), 0.3f);
                    email.move(email.getX(), visibleArea.y + email.getHeight() * 2.5f, email.getWidth(), email.getHeight(), 0.3f);
                }
            } else if ((int) visibleArea.y > MainGDX.HEIGHT - 10) {
                keyboardOpen = false;
                if (name.isEdit()) {
                    name.setEdit(false);
                    Rectangle r = name.getOrig();
                    name.move(r.x, r.y, r.width, r.height, 0.3f);
                }
                if (email.isEdit()) {
                    email.setEdit(false);
                    email.move(reg.getX(), reg.getY(), reg.getWidth(), reg.getHeight(), 0.3f);
                }
                if (pass.isEdit()) {
                    pass.setEdit(false);
                    pass.move(auth.getX(), auth.getY(), auth.getWidth(), auth.getHeight(), 0.3f);
                    email.move(reg.getX(), reg.getY(), reg.getWidth(), reg.getHeight(), 0.3f);
                }
            }
        });
        alpha = 1f;
        //game.clearBg.set(1,1,1,1);
    }

    @Override
    public void show() {
        if (state == 0) {
            LogIn((game.world.prefs.getString("mail").equals("@") ? game.world.prefs.getString("mail") : game.world.prefs.getString("name")), game.world.prefs.getString("pass", "None"));
            ok.setActive(true);
            ok.setText("ПОПРОБОВАТЬ ЕЩЁ", game);
            ok.setSize(sexMan.getWidth() * 1.2f, sexMan.getHeight());
            ok.setPosition(MainGDX.WIDTH - ok.getWidth() - MainGDX.HEIGHT / 16f, MainGDX.HEIGHT / 30f);
        }
    }

    public void SignIn(String mail, String pass, String name, int age, int sex) {
        bQuery = false;
        tQuery = QueryType.SignIn;
        if (mail.length() > 0)
            task.GET("", "mode","0","mail",mail,"pass", pass,"name", name,"age", age + "","sex", sex + "","v", "" + MainGDX.VERSION);
        else
            task.GET("", "mode","0","pass", pass,"name", name,"age", age + "","sex", sex + "","v", "" + MainGDX.VERSION);
    }

    public void SignIn(User user) {
        SignIn(user.getMail(), user.getPass(), user.getName(), user.getAge(), user.getSex());
    }

    public void LogIn(String mail, String pass) {
        bQuery = false;
        tQuery = QueryType.LogIn;
        task.GET("", "mode","1","mail", mail,"pass", pass);
    }

    public void setCode(int id, int code) {
        bQuery = false;
        tQuery = QueryType.codeCheck;
        task.GET("", "mode","2","id", id+"","code", code+"");
    }

    public void checkMail(String mail) {
        bQuery = false;
        tQuery = QueryType.mailCheck;
        task.GET("", "mode","3","mail", mail);
    }

    public void checkName(String name) {
        bQuery = false;
        tQuery = QueryType.nameCheck;
        task.GET("", "mode","3","name", name);
    }

    public void changeState(int stateNew) {
        if (stateNew != state && !stateChanging) {
            stateChanging = true;
            deltaT = 0f;
            new_state = stateNew;
            showing = false;
        }
    }

    @Override
    public void render(float delta) {
        Vector2 pos = GraphicTool.getClick();
        game.draw();
        bg.draw(game.getBatch());
        if (deltaE >= 0 && deltaE < 3f) {
            game.world.setText(error.toUpperCase(), 1f, errorPos.x, errorPos.y, Color.SALMON, true, GameWorld.FONTS.SMALL);
            deltaE += delta;
        }
        game.end();
        if (deltaT >= 0 && deltaT < 0.5f) {
            alpha = game.interpolation.apply(0f, 1f, showing ? deltaT * 2 : 1 - deltaT * 2);
            game.getBatch().setColor(1f, 1f, 1f, alpha);
            deltaT += delta * 2;
            if (deltaT >= 0.5f) {
                deltaT = showing ? -1f : 0f;
                if (!showing) {
                    showing = true;
                    if (new_state == 1) {
                        if (game.world.prefs.contains("name")) {
                            new_state = 0;
                            ok.setActive(true);
                            ok.setText("ПОПРОБОВАТЬ ЕЩЁ", game);
                            ok.setSize(sexMan.getWidth() * 1.2f, sexMan.getHeight());
                            ok.setPosition(MainGDX.WIDTH - ok.getWidth() - MainGDX.HEIGHT / 16f, MainGDX.HEIGHT / 30f);
                        }
                    } else if (new_state == 2) {
                        name.setCharset(GameWorld.getNumCharset() + GameWorld.getRusCharset() + GameWorld.getEngCharset() + ".-_ /,()[]");
                        ok.setText("ДАЛЕЕ", game);
                        name.setEdit(false);
                        ok.setSize(sexMan.getWidth(), sexMan.getHeight());
                        ok.setPosition(MainGDX.WIDTH - ok.getWidth() - MainGDX.HEIGHT / 16f, MainGDX.HEIGHT / 30f);
                        back.setPosition(MainGDX.HEIGHT / 16f, ok.getY());
                        back.setSize(sexMan.getWidth(), sexMan.getHeight());
                        age.setSize(name.getWidth() - ok.getY(), 8);
                        age.setPosition(name.getX() + ok.getY() / 2, name.getY() - name.getHeight() * 2);
                        age.setValue(age.getValue());
                        email.setHint("Введите почту (не обязательно)", game);
                        if (state == 3) {
                            tmpUser.setPass(pass.getText());
                            tmpUser.setMail(email.getText());
                        }
                    } else if (new_state == 3) {
                        // Загрузка 3 сцены
                        // Доступные для ввода символы
                        pass.setMask("");
                        pass.setMaxLength(30);
                        pass.setCharset(GameWorld.getNumCharset() + GameWorld.getEngCharset() + GameWorld.getRusCharset() + "_-.*");
                        email.setCharset(GameWorld.getEngCharset() + GameWorld.getNumCharset() + ".@-_");
                        // Сброс ввода
                        email.setEdit(false);
                        pass.setEdit(false);
                        // Установка текста
                        ok.setText("ДАЛЕЕ", game);
                        email.setText(tmpUser.getMail(), game);
                        pass.setText(tmpUser.getPass(), game);
                        // Установка размеров
                        ok.setSize(sexMan.getWidth(), sexMan.getHeight());
                        ok.setPosition(MainGDX.WIDTH - ok.getWidth() - MainGDX.HEIGHT / 16f, MainGDX.HEIGHT / 30f);
                        back.setPosition(MainGDX.HEIGHT / 16f, ok.getY());
                        back.setSize(sexMan.getWidth(), sexMan.getHeight());
                        email.setSize(reg.getWidth(), reg.getHeight());
                        email.setPosition(reg.getX(), reg.getY());
                        pass.setSize(auth.getWidth(), auth.getHeight());
                        pass.setPosition(auth.getX(), auth.getY());
                        email.getAction().isInput(email.getText());
                        pass.setDone(pass.getText().length() >= 4);
                    } else if (new_state == 4) {
                        pass.setCharset(GameWorld.getNumCharset());
                        pass.setSize(auth.getWidth(), auth.getHeight());
                        pass.setPosition(auth.getX(), auth.getY());
                        resend.setSize(pass.getWidth() / 2.2f, pass.getHeight());
                        resend.setPosition(pass.getX(), pass.getY() - sexWoman.getHeight() * 1.4f);
                        ok.setSize(resend.getWidth(), resend.getHeight());
                        ok.setPosition(pass.getX() + pass.getWidth() - ok.getWidth(), resend.getY());
                        pass.setEdit(false);
                        ok.setText("ПРОВЕРИТЬ", game);
                        pass.setMask("___-___");
                        pass.setText("", game);
                    } else if (new_state == 5) {
                        ok.setText("ВХОД", game);
                        pass.setCharset(GameWorld.getNumCharset() + GameWorld.getEngCharset() + GameWorld.getRusCharset() + "_-.*");
                        email.setCharset(GameWorld.getEngCharset() + GameWorld.getNumCharset() + GameWorld.getRusCharset() + ".@-_");
                        ok.setSize(sexMan.getWidth(), sexMan.getHeight());
                        ok.setPosition(MainGDX.WIDTH - ok.getWidth() - MainGDX.HEIGHT / 16f, MainGDX.HEIGHT / 30f);
                        back.setPosition(MainGDX.HEIGHT / 16f, ok.getY());
                        back.setSize(sexMan.getWidth(), sexMan.getHeight());
                        email.setSize(reg.getWidth(), reg.getHeight());
                        email.setPosition(reg.getX(), reg.getY());
                        pass.setSize(auth.getWidth(), auth.getHeight());
                        pass.setPosition(auth.getX(), auth.getY());
                        email.setHint("Введите имя или почту", game);
                        email.getAction().isInput(email.getText());
                        pass.setDone(pass.getText().length() >= 4);
                    } else if (new_state == 99) {
                        //game.user.setName(AssetsTool.encodePlatform(game.user.getName(),true));
                        //game.user.setPass(AssetsTool.encodePlatform(game.user.getPass(),true));
                        game.world.setUser(game.user);
                        //if (game.world.prefs.getBoolean("sync", true))
                        game.world.LoadData();
                        ScreenAnim.level = 0;
                        ScreenAnim.setClose();
                        ScreenAnim.setState(true);
                    }
                    rotate = 0;
                    state = new_state;
                } else {
                    alpha = 1f;
                    stateChanging = false;
                }
            }
            textColor.a = alpha;
        }
        if (state == 0) {
            game.draw();
            float titleY = MainGDX.HEIGHT - MainGDX.HEIGHT / 13f;
            game.world.setText("ВХОД", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.CUSTOM);
            titleY = MainGDX.HEIGHT / 4f;
            reg.setCursor(pos);
            auth.setCursor(pos);
            reg.draw(game, delta);
            auth.draw(game, delta);
            if (task.isAlive())
                game.world.setText("Пожалуйста подождите немного...", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.SMALL);
            else if (game.user.getSuccess() <= 0) {
                int i = 0;
                for (String s : game.user.getMessage().split("_")) {
                    game.world.setText(s, 1f, MainGDX.WIDTH / 2f, titleY - game.world.getSizes()[1] * 1.5f * i, textColor, true, GameWorld.FONTS.SMALL);
                    i++;
                }
                ok.setCursor(pos);
                ok.draw(game, delta);
            } else if (game.user.getSuccess() == 1) {
                changeState(99);
            }
            game.end();
        } else if (state == 1) {
            game.draw();
            reg.setCursor(pos);
            auth.setCursor(pos);
            reg.draw(game, delta);
            auth.draw(game, delta);
            game.end();
        } else if (state == 2) {
            if (name.getText().length() == 0 || !name.isDone()) ok.setActive(false);
            name.setCursor(pos);
            sexMan.setCursor(pos);
            sexWoman.setCursor(pos);
            ok.setCursor(pos);
            back.setCursor(pos);
            age.setCursor(pos);
            if (!keyboardOpen) age.draw(game, delta);
            game.draw();
            float titleY = Math.max(name.getY() + name.getHeight() * 2f, MainGDX.HEIGHT - MainGDX.HEIGHT / 13f);
            game.world.setText("РЕГИСТРАЦИЯ", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.CUSTOM);
            titleY -= game.world.getSizes()[1] * 1.2f;
            if (name.isDone()) {
                ok.setActive(true);
                if (bQuery && tQuery == QueryType.uCheck) {
                    if (game.user.getSuccess() == 1) {
                        changeState(state + 1);
                        bQuery = false;
                        tQuery = QueryType.nameCheck;
                    } else
                        name.setError(true);
                }
            } else if (name.getText().length() < 2) {
                game.world.setText("Имя должно содержать хотя бы 2 буковки", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.SMALL);
            } else if (name.isError() && game.user.getSuccess() == 0) {
                game.world.setText("Такой пользователь уже зарегистрирован", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.SMALL);
            } else if (bQuery && tQuery == QueryType.nameCheck && game.user.getSuccess() == -1) {
                name.setError(true);
                int i = 0;
                for (String s : game.user.getMessage().split("_")) {
                    game.world.setText(s, 1f, MainGDX.WIDTH / 2f, titleY - game.world.getSizes()[1] * 1.5f * i, textColor, true, GameWorld.FONTS.SMALL);
                    i++;
                }
            }
            if (!keyboardOpen) {
                game.world.setText("Возраст:", 1f, sexWoman.getX() + 20, age.getY() + age.getHeight() + game.world.getTextSize("В", 1f, GameWorld.FONTS.SMALL)[1] * 1.5f, textColor, false, GameWorld.FONTS.SMALL);
                game.world.setText("Пол:", 1f, sexWoman.getX() + 20, sexWoman.getY() + sexWoman.getHeight() + game.world.getTextSize("В", 1f, GameWorld.FONTS.SMALL)[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
                sexMan.draw(game, delta);
                sexWoman.draw(game, delta);
            }
            game.world.setText("Ваше имя:", 1f, name.getX() + 20, name.getY() + name.getHeight() + game.world.getTextSize("В", 1f, GameWorld.FONTS.SMALL)[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
            name.draw(game, delta);
            ok.draw(game, delta);
            back.draw(game, delta);
            game.end();
        } else if (state == 3) {
            ok.setActive(false);
            email.setCursor(pos);
            pass.setCursor(pos);
            ok.setCursor(pos);
            back.setCursor(pos);
            game.draw();
            email.draw(game, delta);
            pass.draw(game, delta);
            ok.setActive(false);
            float titleY = Math.max(email.getY() + email.getHeight() * 2f, MainGDX.HEIGHT - MainGDX.HEIGHT / 8f);
            game.world.setText("РЕГИСТРАЦИЯ", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.CUSTOM);
            titleY -= game.world.getSizes()[1] * 1.2f;
            if (email.isDone() && pass.isDone()) {
                ok.setActive(true);
            } else if (!email.isDone()) {
                game.world.setText(game.user.getSuccess() == 3 ? "Не верный формат почты" : game.user.getSuccess() == 2 ? "Этот адрес электронной почты уже зарегистрирован" : email.isError() ? "Ошибка на сервере, попройбуйте позже" : "", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.SMALL);
            } else {
                game.world.setText(pass.getText().length() < 4 ? "Длина пароля должна быть больше 3 символов" : "", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.SMALL);
            }
            game.world.setText("Электронная почта:", 1f, email.getX() + 20, email.getY() + email.getHeight() + game.world.getTextSize("В", 1f, GameWorld.FONTS.SMALL)[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
            game.world.setText("Придумайте пароль:", 1f, pass.getX() + 20, pass.getY() + pass.getHeight() + game.world.getSizes()[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
            ok.draw(game, delta);
            back.draw(game, delta);
            game.end();
        } else if (state == 4) {
            pass.setCursor(pos);
            ok.setCursor(pos);
            back.setCursor(pos);
            resend.setCursor(pos);
            game.draw();
            back.draw(game, delta);
            float titleY = Math.max(pass.getY() + pass.getHeight() * 2f, MainGDX.HEIGHT - MainGDX.HEIGHT / 8f);
            game.world.setText("РЕГИСТРАЦИЯ", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.CUSTOM);
            titleY -= game.world.getSizes()[1] * 2f;
            if (!bQuery) {
                rotate -= 2f;
                game.getBatch().draw(atlas.findRegion("loadwhite"), MainGDX.WIDTH / 2f - 20, MainGDX.HEIGHT - MainGDX.HEIGHT / 3.5f - 20, 20, 20, 40, 40, 1, 1, rotate);
            } else if (tQuery == QueryType.SignIn) {
                if (tmpUser.getMail().length() == 0) {
                    if (game.user.getSuccess() == 1) {
                        LogIn(tmpUser.getName(), tmpUser.getPass());
                    } else {
                        int i = 0;
                        for (String s : game.user.getMessage().split("_")) {
                            game.world.setText(s, 1f, MainGDX.WIDTH / 2f, titleY - game.world.getSizes()[1] * 1.5f * i, textColor, true, GameWorld.FONTS.SMALL);
                            i++;
                        }
                    }
                } else {
                    logIn();
                    pass.draw(game, delta);
                    ok.draw(game, delta);
                    resend.draw(game, delta);
                    game.world.setText("Код подтверждения:", 1f, pass.getX() + 20, pass.getY() + pass.getHeight() + game.world.getTextSize("D", 1f, GameWorld.FONTS.SMALL)[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
                    Color.DARK_GRAY.add(0, 0, 0, 1 - textColor.a);
                    int i = 0;
                    for (String s : game.user.getMessage().split("_")) {
                        game.world.setText(s, 1f, MainGDX.WIDTH / 2f, titleY - game.world.getSizes()[1] * 1.5f * i, textColor, true, GameWorld.FONTS.SMALL);
                        i++;
                    }
                }
            } else if (tQuery == QueryType.codeCheck) {
                if (game.user.getSuccess() == 1) {
                    LogIn(tmpUser.getMail(), tmpUser.getPass());
                } else {
                    logIn();
                    pass.draw(game, delta);
                    ok.draw(game, delta);
                    resend.draw(game, delta);
                    game.world.setText("Код подтверждения:", 1f, pass.getX() + 20, pass.getY() + pass.getHeight() + game.world.getTextSize("D", 1f, GameWorld.FONTS.SMALL)[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
                    Color.DARK_GRAY.add(0, 0, 0, 1 - textColor.a);
                    int i = 0;
                    for (String s : game.user.getMessage().split("_")) {
                        game.world.setText(s, 1f, MainGDX.WIDTH / 2f, titleY - game.world.getSizes()[1] * 1.5f * i, textColor, true, GameWorld.FONTS.SMALL);
                        i++;
                    }
                }
            } else if (tQuery == QueryType.LogIn) {
                if (game.user.getSuccess() == 1) changeState(99);
                else if (game.user.getSuccess() != 1) {
                    int i = 0;
                    for (String s : game.user.getMessage().split("_")) {
                        game.world.setText(s, 1f, MainGDX.WIDTH / 2f, titleY - game.world.getSizes()[1] * 1.5f * i, textColor, true, GameWorld.FONTS.SMALL);
                        i++;
                    }
                }
            }
            game.end();
        } else if (state == 5) {
            ok.setActive(false);
            email.setCursor(pos);
            pass.setCursor(pos);
            ok.setCursor(pos);
            back.setCursor(pos);
            game.draw();
            float titleY = Math.max(email.getY() + email.getHeight() * 2f, MainGDX.HEIGHT - MainGDX.HEIGHT / 8f);
            game.world.setText("ВХОД В АККАУНТ", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.CUSTOM);
            titleY -= game.world.getSizes()[1] * 1.2f;
            ok.setActive(email.isDone() && pass.isDone());
            if (bQuery && tQuery == QueryType.LogIn) {
                if (game.user.getSuccess() == 1) changeState(99);
                else if (game.user.getSuccess() != 1) {
                    int i = 0;
                    for (String s : game.user.getMessage().split("_")) {
                        game.world.setText(s, 1f, MainGDX.WIDTH / 2f, titleY - game.world.getSizes()[1] * 1.5f * i, textColor, true, GameWorld.FONTS.SMALL);
                        i++;
                    }
                }
            } else if (email.getText().length() < 2) {
                game.world.setText("Слишком короткое имя или адрес электронной почты (минимум 2 буковки)", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.SMALL);
            } else if (pass.getText().length() < 4) {
                game.world.setText("Длина пароля должна быть больше 3 символов", 1f, MainGDX.WIDTH / 2f, titleY, textColor, true, GameWorld.FONTS.SMALL);
            }
            email.draw(game, delta);
            pass.draw(game, delta);
            game.world.setText("Электронная почта или имя:", 1f, email.getX() + 20, email.getY() + email.getHeight() + game.world.getTextSize("В", 1f, GameWorld.FONTS.SMALL)[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
            game.world.setText("Пароль:", 1f, pass.getX() + 20, pass.getY() + pass.getHeight() + game.world.getSizes()[1] * 1.3f, textColor, false, GameWorld.FONTS.SMALL);
            ok.draw(game, delta);
            back.draw(game, delta);
            game.end();
        }
        if (keyboardOpen && MainGDX.DISPLAY_CUTOUT_MODE == 1) {
            game.drawShape();
            game.renderer.setColor(Color.BLACK);
            game.renderer.rect(0, 0, 40, MainGDX.HEIGHT - toLocal(1, game.view.getHeight()).y);
            game.endShape();
        }
        if (ScreenAnim.getState()) {
            Gdx.gl.glEnable(GL30.GL_BLEND);
            game.drawShape();
            if (ScreenAnim.show(game)) {
                if (ScreenAnim.isClosing()) {
                    game.getBatch().setColor(1, 1, 1, 1);
                    textColor.a = 1f;
                    Color.WHITE.set(1, 1, 1, 1);
                    setLevel(game, ScreenAnim.level);
                }
                ScreenAnim.setState(false);
            }
            game.endShape();
            Gdx.gl.glDisable(GL30.GL_BLEND);
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

    float delta;

    public void logIn() {
        if (task1 == null) {
            task1 = new NetTask(new NetTask.NetListener() {
                @Override
                public void onDownloadComplete(String filename) {
                    User tmp;
                    try {
                        tmp = new Json(JsonWriter.OutputType.json).fromJson(User.class, isAndroid() ? filename : AssetsTool.encodeString(filename, false));
                    } catch (SerializationException e) {
                        tmp = new User();
                        tmp.setSuccess(-1);
                        tmp.setMessage(e.getMessage());
                    }
                    if (tmp.getSuccess() == 1) {
                        tmpUser.set(tmp);
                        game.user.set(tmp);
                        changeState(99);
                    }
                }

                @Override
                public void onProgressUpdate(int progress) {

                }

                @Override
                public void onDownloadFailure(String msg) {

                }
            });
            delta = 1f;
        }
        delta += Gdx.graphics.getDeltaTime();
        if (delta > 0.5f) {
            task1.GET("", "mode","1","mail",tmpUser.getMail(),"pass",tmpUser.getPass());
            delta = 0f;
        }
    }
}
