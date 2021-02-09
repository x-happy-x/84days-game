package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.happy.game.adventuredog.MainGDX;

public class MTMap {

    private final ArrayList<String> items;
    private final Map<String, String> types;
    private final ThreeMap map;
    private final TextureAtlas atlas;
    private static float START_Y, START_X, ITEM_SIZE, OFFSET;

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
        for (int i = 0; i < map.rows(); i++) {
            for (int j = 0; j < map.columns(); j++) {
                ThreeItem item = map.get(i, j);
                if (item.isMovingEnd()) {
                    ThreeItem buf = get(i+1,j);
                    set(i+1,j);
                }
                item.draw(game, delta);
            }
        }
    }

    public ThreeItem contain(Vector2 pos) {
        if (pos.x < START_X || pos.x > START_X + (map.columns() - 1) * (ITEM_SIZE + OFFSET * 2) ||
                pos.y < START_Y || pos.y > START_Y + map.rows() * (ITEM_SIZE + OFFSET * 2))
            return null;
        int i = (int) ((pos.y - START_Y) / (ITEM_SIZE + OFFSET * 2));
        int j = (int) ((pos.x - START_X) / (ITEM_SIZE + OFFSET * 2));
        return get(i, j);
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
            for (int k = j - left; k <= j + right; k++) {
                //map.swap(i,k,);
                //get(i,k).d
                for (int m = i; m >= 0; m--) {
                    map.get(m, k).down();
                }
            }
        }
        // Наличие вертикального ряда
        if (up + down >= 2) {
            for (int m = i - up - 1; m >= 0; m--) {
                map.get(m, j).down(up + down);
            }
        }
    }

    public int rows() {
        return map.rows();
    }

    public int columns() {
        return map.columns();
    }

    public ThreeItem get(int i, int j) {
        return map.get(i, j);
    }

    // Элементы игры
    public class ThreeItem {

        private static final int EMPTY = 0, UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
        private static final float ANIMATION_DEFAULT_TIME = 1f;

        private float deltaAction, finishAction;
        private boolean moving, movingEnd;
        private int _type_;
        public int x, y;

        private final Rectangle rect_current;
        private final Rectangle rect_move;
        private final Rectangle rect_bg;

        private Sprite picture;

        ThreeItem(int x, int y, String _type_) {
            this.rect_current = new Rectangle();
            this.rect_move = new Rectangle();
            this.rect_bg = new Rectangle();
            this.finishAction = 0;
            this.deltaAction = 0;
            this.x = x;
            this.y = y;
            correct_pos(x, y);
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
                    this._type_ = 5 + MathUtils.random(items.size() - 1);
                    break;
                case "randbonus":
                    this._type_ = 5 + items.size() + MathUtils.random(items.size() - 1);
                    break;
            }
            if (this._type_ > 4) {
                this.picture = new Sprite();
                this.picture.setRegion(atlas.findRegion(items.get(this._type_ - 5)));
            }
        }

        @Override
        public String toString() {
            return "ThreeItem{" +
                    "type=" + _type_ +
                    ", moving=" + moving +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }

        public void correct_pos(int x, int y) {
            this.rect_current.set(
                    START_X + x * (ITEM_SIZE + OFFSET * 2),
                    START_Y + y * (ITEM_SIZE + OFFSET * 2),
                    ITEM_SIZE + OFFSET * 2,
                    ITEM_SIZE + OFFSET * 2);
            this.rect_bg.set(rect_current);
            this.rect_move.set(rect_current);
            this.x = x;
            this.y = y;
            if (picture != null) {
                this.picture.setBounds(
                        rect_current.x + OFFSET,
                        rect_current.y + OFFSET,
                        rect_current.width - OFFSET * 2,
                        rect_current.height - OFFSET * 2);
            }
        }

        public int type() {
            return _type_;
        }

        public boolean isMoving() {
            return moving;
        }

        public Rectangle getRect() {
            return rect_current;
        }

        public void move(float x, float y, float w, float h, float milliseconds) {
            if (moving) return;
            deltaAction = 0;
            finishAction = milliseconds;
            moving = finishAction > 0;
            rect_move.x = x;
            rect_move.y = y;
            rect_move.width = w;
            rect_move.height = h;
        }

        public void move(float x, float y, float w, float h) {
            move(x, y, w, h, ANIMATION_DEFAULT_TIME);
        }

        public void move(Rectangle position, float milliseconds) {
            move(position.x, position.y, position.width, position.height, milliseconds);
        }

        public void move(Rectangle position) {
            move(position, ANIMATION_DEFAULT_TIME);
        }

        public void down(int n) {
            move(rect_bg.x,
                    rect_bg.y - n * (ITEM_SIZE + OFFSET * 2),
                    rect_bg.width,
                    rect_bg.height);
            y += n;
        }

        public void down() {
            down(1);
        }

        public void reset() {
            this.rect_current.y = START_Y;
            this.rect_bg.set(rect_current);
            this.rect_move.set(rect_current);
            this.deltaAction = this.finishAction = 0;
            this.moving = false;
            this._type_ = 0;
            this.picture = null;
        }

        public boolean isMovingEnd() {
            if (movingEnd) {
                movingEnd = false;
                return true;
            }
            return false;
        }

        public void draw(MainGDX game, float delta) {
            if (picture == null) return;
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
                            rect_current.y + OFFSET,
                            rect_current.width - OFFSET * 2,
                            rect_current.height - OFFSET * 2);
                }
            }
            picture.draw(game.getBatch());
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

        public void swap(int i1, int j1, int i2, int j2) {
            ThreeItem item = get(i1, j1);
            set(i1, j1, get(i2, j2));
            set(i2, j2, item);
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
                    ThreeItem item = get(i,j);
                    item.correct_pos(item.x,item.y);
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
