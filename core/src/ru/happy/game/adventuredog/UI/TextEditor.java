package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;

public class TextEditor extends Button {

    TextureRegion icEdit, icDone, icError, icLoad;
    boolean edit, done, error, load;
    String text, mask, charset;
    GlyphLayout hint;
    Color hintColor;
    Action action;
    float rotate;
    int maxLen;

    // Конструкторы
    public TextEditor(String hint, TextureRegion region, TextureRegion ic, GameWorld world, Color color) {
        this(hint, region, ic, world, color, Color.GRAY, null);
    }
    public TextEditor(String hint, TextureRegion region, TextureRegion ic, GameWorld world, Color color, Action action) {
        this(hint, region, ic, world, color, Color.GRAY, action);
    }
    public TextEditor(String hint, TextureRegion region, TextureRegion ic, GameWorld world, Color color, Color hintColor) {
        this(hint, region, ic, world, color, hintColor, null);
    }
    public TextEditor(String hint, TextureRegion region, TextureRegion ic, GameWorld world, Color color, Color hintColor, Action action) {
        super("", region, world, color);
        this.text = "";
        this.mask = "";
        this.hint = world.getGlyphLayout(hint, 1f, hintColor, GameWorld.FONTS.SMEDIAN);
        this.hintColor = hintColor;
        this.icEdit = ic;
        this.setDark(0);
        this.charset = GameWorld.FONT_CHARACTERS;
        this.OnAutoSize = false;
        this.action = action;
    }

    // Интерфейс слушателя
    public interface Action extends Button.Action {
        void isInput(String text);
    }

    // Установить слушатель действий
    public void setAction(Action action) {
        this.action = action;
    }
    // Установить значение редактирования
    public void setEdit(boolean edit) {
        this.edit = edit;
    }
    // Установить значение правильности
    public void setDone(boolean done) {
        this.done = done;
        this.error = false;
    }
    // Установить значение наличия ошибки
    public void setError(boolean error) {
        this.error = error;
        this.done = false;
    }
    // Установить значение загрузки
    public void setLoad(boolean load) {
        this.load = load;
        this.done = false;
        this.error = false;
        rotate = 360f;
    }

    // Получить слушатель действий
    @Override
    public Action getAction() {
        return action;
    }
    // Текст редактируется
    public boolean isEdit() {
        return edit;
    }
    // Правильное ли значение
    public boolean isDone() {
        return done;
    }
    // Есть ли ошибка
    public boolean isError() {
        return error;
    }
    // Загружается ли
    public boolean isLoad() {
        return load;
    }

    // Изменить текст
    @Override
    public void setText(String text, MainGDX game) {
        if (mask.length() == 0) {
            super.setText(text, game);
        } else {
            String maskedText = mask;
            if (text.length() > 0) {
                for (String s : text.split(""))
                    maskedText = maskedText.replaceFirst("_", s);
            }
            super.setText(maskedText, game);
        }
        this.text = text;
    }
    // Получить текст
    public String getText() {
        return text;
    }
    // Изменить подсказку
    public void setHint(String text, MainGDX game) {
        game.world.getFont(GameWorld.FONTS.SMEDIAN).setColor(hintColor);
        hint.setText(game.world.getFont(GameWorld.FONTS.SMEDIAN), text);
    }
    // Установить максимальную длину
    public void setMaxLength(int len) {
        maxLen = len;
    }
    // Установить допустимые символов
    public void setCharset(String charset) {
        if (charset.equals(""))
            this.charset = GameWorld.FONT_CHARACTERS;
        else
            this.charset = charset;
    }
    // Установить маску
    public void setMask(String mask) {
        this.mask = mask;
        int i = 0;
        for (String s : mask.split(""))
            if (s.equals("_")) i++;
        setMaxLength(i);
    }
    // Добавить символ
    public void add(char character, MainGDX game) {
        if (text.length() < maxLen && charset.contains(character + "")) {
            setText(text + character, game);
            if (action != null) action.isInput(text);
        }
    }
    // Удалить последний символ
    public void removeLast(MainGDX game) {
        if (text.length() > 0) {
            setText(text.substring(0, text.length() - 1), game);
            if (action != null) action.isInput(text);
        }
    }
    // Добавить иконку
    public void addIcon(TextureRegion done, TextureRegion err, TextureRegion load) {
        icDone = done;
        icError = err;
        icLoad = load;
    }
    // Рисование на экране
    @Override
    public void draw(MainGDX game, float _delta_) {
        super.draw(game, _delta_);
        if (edit)
            game.getBatch().draw(icEdit, pos.x + pos.height / 6f, pos.y + pos.height / 6f, pos.height / 1.5f, pos.height / 1.5f);
        if (done && icDone != null)
            game.getBatch().draw(icDone, pos.x + pos.width - pos.height / 1.5f - pos.height / 6f, pos.y + pos.height / 6f, pos.height / 1.5f, pos.height / 1.5f);
        else if (error && icError != null)
            game.getBatch().draw(icError, pos.x + pos.width - pos.height / 1.5f - pos.height / 6f, pos.y + pos.height / 6f, pos.height / 1.5f, pos.height / 1.5f);
        else if (load && icLoad != null) {
            game.getBatch().draw(icLoad, pos.x + pos.width - pos.height / 1.5f - pos.height / 6f, pos.y + pos.height / 6f, pos.height / 1.5f / 2f, pos.height / 1.5f / 2f, pos.height / 1.5f, pos.height / 1.5f, 1, 1, rotate);
            rotate -= 3;
        }
        if (text.length() == 0 && mask.length() == 0)
            game.world.setText(hint, drawing.x + drawing.width / 2f, drawing.y + drawing.height / 2f, true, true, GameWorld.FONTS.SMEDIAN);
    }
}
