package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Tools.GraphicTool;

public class MTMap implements InputProcessor {

    private static float START_Y, START_X, ITEM_SIZE, OFFSET;

    private final ArrayList<String> items;
    private final Map<String, String> types;
    private final ThreeMap map;
    private final TextureAtlas atlas;

    private ThreeItem item_selected, item_dragged;

    public MTMap(String mapFile, TextureAtlas atlas) {
        this.atlas = atlas;
        int i = 0, j = 0;
        ThreeItem item;
        items = new ArrayList<>();
        types = new HashMap<>();
        map = new ThreeMap();
        for (String line : mapFile.trim().split("\n")) {
            if (line.startsWith("#")) {
                if (line.contains("ITEMS")) {
                    i = 0;
                } else if (line.contains("TYPES")) {
                    i = 1;
                } else if (line.contains("MAP")) {
                    i = 2;
                    j = 0;
                }
                continue;
            }
            switch (i) {
                case 0:
                    items.add(line);
                    break;
                case 1:
                    types.put(line.split("-")[0].trim(), line.split("-")[1].trim());
                    break;
                case 2:
                    String[] symbols = line.split("");
                    for (int k = 0; k < symbols.length; k++) {
                        item = new ThreeItem(k, j, types.get(symbols[k]));
                        map.add(j, item);
                    }
                    j++;
                    break;
            }
        }
        //MainGDX.write(items + "");
        //MainGDX.write(types + "");
        //MainGDX.write(map + "");
        ITEM_SIZE = (float) MainGDX.HEIGHT / map.rows();
        OFFSET = ITEM_SIZE / 20;
        ITEM_SIZE -= OFFSET * 2;
        START_Y = 0;
        START_X = (MainGDX.WIDTH - map.columns() * (ITEM_SIZE + OFFSET * 2)) / 2f;
        map.correct_position();
    }

    public void draw(MainGDX game, float delta) {
        for (int i = map.rows() - 1; i >= 0; i--) {
            for (int j = 0; j < map.columns(); j++) {
                ThreeItem item = map.get(i, j);
                if (item.isMovingEnd()) {
                    map.swap(i, j, item.y, item.x);
                    check(i,j);
                }
            }
        }
        for (int i = 0; i < map.rows(); i++) {
            for (int j = 0; j < map.columns(); j++) {
                map.get(i, j).draw(game, delta);
            }
        }
    }

    public ThreeItem contain(Vector2 pos) {
        return contain(pos.x,pos.y);
    }

    public ThreeItem contain(float x, float y) {
        if (x < START_X || x > START_X + (map.columns() - 1) * (ITEM_SIZE + OFFSET * 2) ||
                y < START_Y || y > START_Y + map.rows() * (ITEM_SIZE + OFFSET * 2))
            return null;
        int i = (int) ((y - START_Y) / (ITEM_SIZE + OFFSET * 2));
        int j = (int) ((x - START_X) / (ITEM_SIZE + OFFSET * 2));
        return map.get(i, j);
    }

    public void check(int i, int j) {
        if (map.get(i, j).isMoving() || map.get(i, j).type() < 5)
            return;
        int left, right, up, down;
        int _type_ = map.get(i, j).type();

        // Подсчёт схожих элементов сверху
        for (up = 0; i - 1 - up >= 0; up++)
            if (map.get(i - 1 - up, j).type() != _type_)
                break;
        // Подсчёт схожих элементов снизу
        for (down = 0; i + 1 + down < map.rows(); down++)
            if (map.get(i + 1 + down, j).type() != _type_)
                break;
        // Подсчёт схожих элементов слева
        for (left = 0; j - 1 - left >= 0; left++)
            if (map.get(i, j - 1 - left).type() != _type_)
                break;
        // Подсчёт схожих элементов справа
        for (right = 0; j + 1 + right < map.columns(); right++)
            if (map.get(i, j + 1 + right).type() != _type_)
                break;
        // Наличие кубика
        if (left > 0) {
            if (up > 0 && map.get(i - 1, j - 1).type() == _type_) {

            } else if (down > 0 && map.get(i + 1, j - 1).type() == _type_) {

            }
        }
        if (right > 0) {
            if (up > 0 && map.get(i - 1, j + 1).type() == _type_) {

            } else if (down > 0 && map.get(i + 1, j + 1).type() == _type_) {

            }
        }
        // Наличие горизонтального ряда
        if (left + right >= 2) {
            int m;
            for (int k = j - left; k <= j + right; k++) {
                for (m = i - 1; m >= 0 && map.get(m, k).type() > 4; m--) {
                    map.get(m, k).down(1);
                }
                map.get(i, k).hide();
            }
        }
        // Наличие вертикального ряда
        if (up + down >= 2) {
            for (int m = 0; m <= i + down; m++) {
                ThreeItem item = map.get(m, j);
                if (m < i - up) {
                    if (item.type() >= 5) item.down(up + down + 1);
                } else {
                    item.hide();
                }
            }
//            for (int m = i + down; m >= 0; m--) {
//                if (m > i - up - 1)
//                    map.get(m, j).hide(up + down);
//                else
//                    map.get(m, j).down(up + down);
//            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        MainGDX.write("TOUCH: "+x+" "+y+" "+pointer+" "+button);
        item_selected = contain(GraphicTool.toLocal(x,y));
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        item_selected = null;
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        MainGDX.write("TOUCH: "+x+" "+y+" "+pointer);
        item_dragged = contain(GraphicTool.toLocal(x,y));
        if (item_selected != null && item_dragged != null && item_dragged != item_selected) {
            int x1 = Math.max(-1,Math.min(1,item_selected.x-item_dragged.x)),
                    y1 = Math.max(-1,Math.min(1,item_selected.y - item_dragged.y));
            item_dragged = map.get(item_selected.x-x1,item_selected.y-y1);
            item_selected.correct_pos();
            item_dragged.correct_pos();
            item_dragged.moveTo(item_selected.x,item_selected.y);
            item_selected.moveTo(item_selected.x-x1,item_selected.y-y1);
            map.swap(item_dragged,item_selected);
            item_selected = null;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }

    // Элементы игры
    public class ThreeItem {

        private static final int EMPTY = 0, UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
        private static final float ANIMATION_DEFAULT_TIME = 0.5f;

        private float deltaAction, finishAction;
        private boolean moving, movingEnd, hide;
        private int _type_, x, y;

        private final Rectangle rect_current;
        private final Rectangle rect_move;
        private final Rectangle rect_bg;

        private Sprite picture;

        ThreeItem(int x, int y, String _type_) {
            this.rect_current = new Rectangle();
            this.rect_move = new Rectangle();
            this.rect_bg = new Rectangle();
            this.movingEnd = false;
            this.moving = false;
            this.hide = true;
            this.finishAction = 0;
            this.deltaAction = 0;
            this.x = x;
            this.y = y;
            correct_pos();
            switch (_type_) {
                case "empty":
                    this._type_ = EMPTY;
                    break;
                case "idown":
                    this._type_ = DOWN;
                    break;
                case "itop":
                    this._type_ = UP;
                    break;
                case "ileft":
                    this._type_ = LEFT;
                    break;
                case "iright":
                    this._type_ = RIGHT;
                    break;
                case "randitem":
                    this._type_ = 5;
                    setRandomItem();
                    break;
                case "randbonus":
                    this._type_ = 5 + items.size() + MathUtils.random(items.size() - 1);
                    break;
            }
        }

        // Установить рандомный элемент
        public void setRandomItem() {
            if (this._type_ > 4) {
                this._type_ = 5 + MathUtils.random(items.size() - 1);
                this.picture = new Sprite();
                this.picture.setRegion(atlas.findRegion(items.get(this._type_ - 5)));
            }
        }

        // Свойства
        public boolean isMoving() {
            return moving;
        }   // Элемент двигается
        public boolean isHidden() {
            return hide;
        }     // Элемент скрыт
        public boolean isMovingEnd() {
            if (movingEnd) {
                movingEnd = false;
                return true;
            }
            return false;
        }             // Элемент закончил двигаться
        public int type() {
            return _type_;
        }                        // Тип элемента
        public Rectangle getRect() {
            return rect_current;
        }               // Текущие границы элемента

        // Передвинуть элемент
        public void move(float x, float y, float w, float h, float milliseconds) {
            deltaAction = 0;
            finishAction = milliseconds;
            moving = finishAction > 0;
            rect_move.set(x, y, w, h);
        }
        public void move(Rectangle position, float milliseconds) {
            move(position.x, position.y, position.width, position.height, milliseconds);
        }
        public void move(float x, float y, float w, float h) {
            move(x, y, w, h, ANIMATION_DEFAULT_TIME);
        }
        public void move(Rectangle position) {
            move(position, ANIMATION_DEFAULT_TIME);
        }
        public void moveTo(int x, int y){
            if (this.x != x)
                right(x-this.x);
            else if (this.y != y)
                down(y-this.y);
        }

        // Передвинуть элемент на клетку вниз
        public void down(int n) {
            move(rect_bg.x,
                    rect_bg.y + n * (ITEM_SIZE + OFFSET * 2),
                    rect_bg.width,
                    rect_bg.height);
            y += n;
        }
        public void top(int n) {
            down(-n);
        }
        public void right(int n) {
            move(rect_bg.x + n * (ITEM_SIZE + OFFSET * 2),
                    rect_bg.y,
                    rect_bg.width,
                    rect_bg.height);
            x += n;
        }
        public void left(int n) {
            right(-n);
        }

        // Корректировка позиции по х и у
        public void correct_pos() {
            this.rect_current.set(
                    START_X + x * (ITEM_SIZE + OFFSET * 2),
                    START_Y + y * (ITEM_SIZE + OFFSET * 2),
                    ITEM_SIZE + OFFSET * 2,
                    ITEM_SIZE + OFFSET * 2);
            this.rect_bg.set(rect_current);
            this.rect_move.set(rect_current);
            if (picture != null) {
                this.picture.setBounds(
                        rect_current.x + OFFSET,
                        MainGDX.HEIGHT - rect_current.height - (rect_current.y + OFFSET),
                        rect_current.width - OFFSET * 2,
                        rect_current.height - OFFSET * 2);
            }
        }

        // Скрыть элемент
        public void hide() {
            move(rect_bg.x + ITEM_SIZE / 2f + OFFSET,
                    rect_bg.y + ITEM_SIZE / 2f + OFFSET,
                    0,
                    0);
            hide = true;
        }
        // Показать элемент
        public void show() {
            correct_pos();
            if (isHidden()) {
                hide = false;
                setRandomItem();
                move(rect_bg);
                rect_bg.set(rect_bg.x + ITEM_SIZE / 2f + OFFSET,
                        rect_bg.y + ITEM_SIZE / 2f + OFFSET,
                        0,
                        0);
                rect_current.set(rect_bg);
            }
        }

        // Отрисовка
        public void draw(MainGDX game, float delta) {
            if (picture == null)
                return;
            if (moving) {
                deltaAction += delta;
                if (deltaAction >= finishAction) {
                    moving = false;
                    movingEnd = true;
                    rect_bg.set(rect_move);
                } else {
                    rect_current.x = game.interpolation.apply(rect_bg.x, rect_move.x, deltaAction / finishAction);
                    rect_current.y = game.interpolation.apply(rect_bg.y, rect_move.y, deltaAction / finishAction);
                    rect_current.width = game.interpolation.apply(rect_bg.width, rect_move.width, deltaAction / finishAction);
                    rect_current.height = game.interpolation.apply(rect_bg.height, rect_move.height, deltaAction / finishAction);
                    picture.setBounds(
                            rect_current.x + OFFSET,
                            MainGDX.HEIGHT - rect_current.height - (rect_current.y + OFFSET),
                            rect_current.width - OFFSET * 2,
                            rect_current.height - OFFSET * 2);
                }
            }
            picture.draw(game.getBatch());
        }

        // Вывод
        @Override
        public String toString() {
            return "ThreeItem{" +
                    "type=" + _type_ +
                    ", moving=" + moving +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    // Карта (массив) для хранения элементов
    public class ThreeMap {

        private final ArrayList<ArrayList<ThreeItem>> items;

        public ThreeMap() {
            items = new ArrayList<>();
        }

        public ThreeItem get(int i, int j) {
            return items.get(i).get(j);
        }

        public void set(int i, int j, ThreeItem item) {
            items.get(i).set(j, item);
        }

        public void swap(int i1, int j1, int i2, int j2, boolean show) {
            ThreeItem item1 = get(i1, j1);
            ThreeItem item2 = get(i2, j2);
            item1.x = j2;
            item1.y = i2;
            item2.x = j1;
            item2.y = i1;
            if (show) {
                item1.show();
                item2.show();
            }
            set(i1, j1, item2);
            set(i2, j2, item1);
        }
        public void swap(int i1, int j1, int i2, int j2) {
            swap(i1,j1,i2,j2,true);
        }
        public void swap(ThreeItem item1, ThreeItem item2) {
            swap(item1.y,item1.x,item2.y,item2.x,false);
        }

        public void add(int i, ThreeItem item) {
            if (items.size() <= i)
                items.add(new ArrayList<>());
            items.get(i).add(item);
        }

        public int rows() {
            return items.size();
        }

        public int columns() {
            return items.get(0).size();
        }

        public void correct_position() {
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < columns(); j++) {
                    ThreeItem item = get(i, j);
                    item.correct_pos();
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder("[");
            for (int i = 0; i < rows(); i++) {
                out.append("[");
                for (int j = 0; j < columns(); j++) {
                    out.append(get(i, j).type());
                }
                out.append("]\n");
            }
            return out.toString().trim() + "]";
        }
    }
}
