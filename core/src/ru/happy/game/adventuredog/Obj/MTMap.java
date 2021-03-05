package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private final TextureRegion item_bg;

    private ThreeItem item_selected, item_dragged;

    public MTMap(String mapFile, TextureAtlas atlas) {
        this.atlas = atlas;
        int i = 0, j = 0;
        ThreeItem item;
        item_bg = atlas.findRegion("item_bg");
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
        ThreeItem item;
        boolean change = false, change2;
        for (int i = map.rows() - 1; i >= 0; i--) {
            for (int j = 0; j < map.columns(); j++) {
                change2 = false;
                item = map.get(i, j);
                if (item.type() > 4) {
                    game.getBatch().draw(item_bg,
                            START_X + j * (ITEM_SIZE + OFFSET * 2) - OFFSET,
                            MainGDX.HEIGHT - (ITEM_SIZE + OFFSET * 5) - (START_Y + i * (ITEM_SIZE + OFFSET * 2)),
                            ITEM_SIZE + OFFSET * 4,
                            ITEM_SIZE + OFFSET * 4);
                }
                if (item.isMovingEnd()) {
                    if (!item.isHidden())
                        change2 = check(item);
                    if (!change2)
                        item.show();
                    change = change2 || change;
                }
            }
        }
        if (change)
            swap();
        for (int i = 0; i < map.rows(); i++) {
            for (int j = 0; j < map.columns(); j++) {
                item = map.get(i, j);
                item.draw(game, delta);
            }
        }
    }

    public ThreeItem contain(Vector2 pos) {
        return contain(pos.x, pos.y);
    }

    public ThreeItem contain(float x, float y) {
        if (x < START_X || x > START_X + (map.columns() - 1) * (ITEM_SIZE + OFFSET * 2) ||
                y < START_Y || y > START_Y + map.rows() * (ITEM_SIZE + OFFSET * 2))
            return null;
        int i = (int) ((y - START_Y) / (ITEM_SIZE + OFFSET * 2));
        int j = (int) ((x - START_X) / (ITEM_SIZE + OFFSET * 2));
        return map.get(i, j);
    }

    public boolean check(ThreeItem item) {
        ThreeMap.SideItems side = map.getRoundItems(item);
        if (item.isBonus()) {
            item.use();
            return true;
        }
        if (side == null || !item.isBonus() && !side.haveAroundItems()) return false;
        int i = item.y, j = item.x, count = 0, type = item.type();

        // Наличие горизонтального ряда
        if (side.haveHorizontal()) {
            for (int k = j - side.left; k <= j + side.right; k++) {
                if (!map.get(i, k).isHidden()) count++;
                map.get(i, k).use();
            }
        }
        // Наличие вертикального ряда
        if (side.haveVertical()) {
            for (int k = i - side.up; k <= i + side.down; k++) {
                if (!map.get(k, j).isHidden()) count++;
                map.get(k, j).use();
            }
        }
        // Наличие кубика
        if (side.haveSquare()) {
            // Слева сверху
            if (side.upLeft > 0) {
                for (int k = 0; k <= side.upLeft; k++) {
                    for (int m = 0; m <= k; m++) {
                        if (!map.get(i - k, j - m).isHidden()) count++;
                        if (!map.get(i - m, j - k).isHidden()) count++;
                        map.get(i - k, j - m).use();
                        map.get(i - m, j - k).use();
                    }
                }
            }
            // Справа сверху
            if (side.upRight > 0) {
                for (int k = 0; k <= side.upRight; k++) {
                    for (int m = 0; m <= k; m++) {
                        if (!map.get(i - k, j + m).isHidden()) count++;
                        if (!map.get(i - m, j + k).isHidden()) count++;
                        map.get(i - k, j + m).use();
                        map.get(i - m, j + k).use();
                    }
                }
            }
            // Слева снизу
            if (side.downLeft > 0) {
                for (int k = 0; k <= side.downLeft; k++) {
                    for (int m = 0; m <= k; m++) {
                        if (!map.get(i + k, j - m).isHidden()) count++;
                        if (!map.get(i + m, j - k).isHidden()) count++;
                        map.get(i + k, j - m).use();
                        map.get(i + m, j - k).use();
                    }
                }
            }
            // Справа снизу
            if (side.downRight > 0) {
                for (int k = 0; k <= side.downRight; k++) {
                    for (int m = 0; m <= k; m++) {
                        if (!map.get(i + k, j + m).isHidden()) count++;
                        if (!map.get(i + m, j + k).isHidden()) count++;
                        map.get(i + k, j + m).use();
                        map.get(i + m, j + k).use();
                    }
                }
            }
        }
        item._type_ = type;
        item.setBonus(count);
        return true;
    }

    private void swap() {
        for (int j = 0; j < map.columns(); j++) {
            for (int i = map.rows() - 1, k = 0; i >= 0; i--) {
                ThreeItem item = map.get(i, j);
                if (item.type() > 4) {
                    if (item.isHidden()) k++;
                    else if (k > 0) {
                        item.down(k);
                        map.swap(i, j, item.y, item.x, false);
                    }
                }
            }
        }
        /*for (int j = 0; j < map.columns(); j++) {
            for (int i = map.rows() - 1, k = 0; i >= 0; i--) {
                ThreeItem item = map.get(i, j);
                if (item.x != j || item.y != i) {
                    map.swap(i, j, item.y, item.x, false);
                }
            }
        }*/
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
        item_selected = contain(GraphicTool.toLocal(x, y));
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        item_selected = null;
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        item_dragged = contain(GraphicTool.toLocal(x, y));
        if (map.itemDragged(item_selected, item_dragged))
            item_selected = null;
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
        private static final float ANIMATION_DEFAULT_TIME = .75f;

        private float deltaAction, finishAction;
        private boolean moving, movingEnd, hide, drawing, bonus, bonus_active;
        private int _type_, x, y, bonus_type;

        private final Rectangle rect_current;
        private final Rectangle rect_move;
        private final Rectangle rect_bg;
        private MoveAction moveAction;

        private Sprite picture;

        ThreeItem(int x, int y, String _type_) {
            this.rect_current = new Rectangle();
            this.rect_move = new Rectangle();
            this.rect_bg = new Rectangle();
            this.movingEnd = false;
            this.moving = false;
            this.drawing = false;
            this.bonus_active = false;
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
                    this.picture = new Sprite();
                    setRandomItem();
                    break;
                case "randbonus":
                    this._type_ = 5 + MathUtils.random(items.size() - 1);
                    this.picture = new Sprite();
                    setBonus(MathUtils.random(4, 7));
                    hide = true;
                    drawing = false;
                    break;
            }
        }

        // Установить рандомный элемент
        public void setRandomItem() {
            bonus = false;
            bonus_active = false;
            if (this._type_ > 4) {
                this._type_ = 5 + MathUtils.random(items.size() - 1);
            }
        }

        // Установить рандомный элемент
        public void setBonus(int c) {
            if (_type_ > 4 && c > 3 && !bonus) {
                correct_pos();
                switch (c) {
                    case 4:
                        this.bonus_type = 0;
                        break;
                    case 5:
                        this.bonus_type = 1;
                        break;
                    case 6:
                        this.bonus_type = 2;
                        break;
                    default:
                        this.bonus_type = 3;
                        break;
                }
                this.moving = false;
                this.hide = false;
                this.movingEnd = false;
                this.moveAction = null;
                this.drawing = true;
                this.bonus = true;
                this.finishAction = 0;

                this.picture.setRegion(atlas.findRegion(items.get(_type_ - 5) + (this.bonus_type + 1)));
            }
        }

        public void setMoveAction(MoveAction moveAction) {
            this.moveAction = moveAction;
        }

        // Свойства
        public boolean isMoving() {
            return moving;
        }                // Элемент двигается

        public boolean isHidden() {
            return hide;
        }                // Элемент скрыт

        public boolean isBonus() {
            return bonus;
        }     //

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

        public boolean equals(int type) {
            return type == _type_ && !bonus;
        }

        public Rectangle getRect() {
            return rect_current;
        }               // Текущие границы элемента

        // Передвинуть элемент
        public void move(float x, float y, float w, float h, float milliseconds) {
            deltaAction = 0;
            finishAction = milliseconds;
            moving = finishAction > 0;
            movingEnd = false;
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

        public void moveTo(int x, int y) {
            if (this.x != x)
                right(x - this.x);
            else if (this.y != y)
                down(y - this.y);
        }

        public void setBonusActive() {
            this.bonus_active = true;
        }

        // Передвинуть элемент на клетку вниз
        public void down(int n) {
            if (n == 0) return;
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
            if (n == 0) return;
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
            if (isHidden())
                return;
            correct_pos();
            move(rect_bg.x + ITEM_SIZE / 2f + OFFSET,
                    rect_bg.y + ITEM_SIZE / 2f + OFFSET,
                    1,
                    1);
            setMoveAction(item -> item.drawing = false);
            hide = true;
            setRandomItem();
        }

        public void use() {
            if (_type_ < 5) return;
            if (isBonus()) useBonus();
            else hide();
        }

        public void useBonus() {
            if (!bonus_active) return;
            MainGDX.write("USE BONUS " + bonus_type);
            int type = type();
            hide();
            switch (bonus_type) {
                case 0:
                    map.get(y - 1, x - 1).setBonusActive();
                    map.get(y - 1, x).setBonusActive();
                    map.get(y - 1, x + 1).setBonusActive();
                    map.get(y, x - 1).setBonusActive();
                    map.get(y, x + 1).setBonusActive();
                    map.get(y + 1, x - 1).setBonusActive();
                    map.get(y + 1, x).setBonusActive();
                    map.get(y + 1, x + 1).setBonusActive();

                    map.get(y - 1, x - 1).use();
                    map.get(y - 1, x).use();
                    map.get(y - 1, x + 1).use();
                    map.get(y, x - 1).use();
                    map.get(y, x + 1).use();
                    map.get(y + 1, x - 1).use();
                    map.get(y + 1, x).use();
                    map.get(y + 1, x + 1).use();
                    break;
                case 1:
                    for (int j = 0; j < map.columns(); j++) {
                        ThreeItem item = map.get(y, j);
                        if (x != j) {
                            item.use();
                            item.setBonusActive();
                        }
                    }
                    break;
                case 2:
                    for (int i = 0; i < map.rows(); i++) {
                        ThreeItem item = map.get(i, x);
                        if (y != i) {
                            item.use();
                            item.setBonusActive();
                        }
                    }
                    break;
                case 3:
                    for (int i = 0; i < map.rows(); i++) {
                        for (int j = 0; j < map.columns(); j++) {
                            ThreeItem item = map.get(i, j);
                            if (item.type() == type && (x != j || y != i)) {
                                item.setBonusActive();
                                item.use();
                            }
                        }
                    }
                    break;
            }
        }

        // Показать элемент
        public void show() {
            correct_pos();
            if (isHidden()) {
                drawing = true;
                if (_type_ > 4 && _type_ < 5 + items.size())
                    this.picture.setRegion(atlas.findRegion(items.get(this._type_ - 5)));
                hide = false;
                move(rect_bg);
                rect_bg.set(rect_bg.x + ITEM_SIZE / 2f + OFFSET,
                        rect_bg.y + ITEM_SIZE / 2f + OFFSET,
                        1,
                        1);
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
                    if (moveAction != null) {
                        moveAction.finishMove(this);
                        moveAction = null;
                    }
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
            if (this.drawing) picture.draw(game.getBatch());
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

        private class SideItems {
            int left, right, up, down, upLeft, upRight, downLeft, downRight;

            public SideItems() {
                left = 0;
                right = 0;
                up = 0;
                down = 0;
            }

            public boolean haveHorizontal() {
                return left + right >= 2;
            }

            public boolean haveVertical() {
                return up + down >= 2;
            }

            public boolean haveSquare() {
                return upLeft + upRight + downLeft + downRight > 0;
            }

            public boolean haveAroundItems() {
                return haveHorizontal() || haveVertical() || haveSquare();
            }

            @Override
            public String toString() {
                return "SideItems{" +
                        "l=" + left +
                        ", r=" + right +
                        ", t=" + up +
                        ", d=" + down +
                        ", tl=" + upLeft +
                        ", tr=" + upRight +
                        ", dl=" + downLeft +
                        ", dr=" + downRight +
                        '}';
            }
        }

        private final ArrayList<ArrayList<ThreeItem>> ITEMS_MAP;
        private ThreeItem swapped1, swapped2;

        public ThreeMap() {
            ITEMS_MAP = new ArrayList<>();
        }

        public ThreeItem get(int i, int j) {
            return ITEMS_MAP.get(i).get(j);
        }

        public void set(int i, int j, ThreeItem item) {
            ITEMS_MAP.get(i).set(j, item);
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
            swap(i1, j1, i2, j2, true);
        }

        public void swap(ThreeItem item1, ThreeItem item2) {
            swapped1 = item1;
            swapped2 = item2;
            swap(item1.y, item1.x, item2.y, item2.x, false);
        }

        public boolean itemDragged(ThreeItem item_selected, ThreeItem item_dragged) {
            if (item_selected != null && item_dragged != null && item_dragged.type() > 4 &&
                    item_selected.type() > 4 && item_dragged != item_selected) {

                int x1, x2, y1, y2;
                x1 = item_selected.x;
                y1 = item_selected.y;
                x2 = Math.max(-1, Math.min(1, x1 - item_dragged.x));
                y2 = Math.max(-1, Math.min(1, y1 - item_dragged.y));

                item_dragged = map.get(y1 - (x2 != 0 ? 0 : y2), x1 - x2);

                if (item_dragged.type() < 5)
                    return false;

                item_selected.correct_pos();
                item_dragged.correct_pos();

                item_dragged.moveTo(x1, y1);
                item_selected.moveTo(x1 - x2, y1 - (x2 != 0 ? 0 : y2));
                map.swap(item_dragged, item_selected);

                SideItems side1, side2;
                side1 = getRoundItems(item_dragged);
                side2 = getRoundItems(item_selected);

                if (!(side1 != null && side1.haveAroundItems() ||
                        side2 != null && side2.haveAroundItems() ||
                        item_dragged.isBonus() || item_selected.isBonus())) {
                    map.swap(item_selected, item_dragged);
                    item_dragged.setMoveAction(
                            item -> item.moveTo(x1 - x2, y1 - (x2 != 0 ? 0 : y2)));
                    item_selected.setMoveAction(
                            item -> item.moveTo(x1, y1));
                }
                if (item_dragged.isBonus()) item_dragged.setBonusActive();
                if (item_selected.isBonus()) item_selected.setBonusActive();
                return true;
            }
            return false;
        }

        public void add(int i, ThreeItem item) {
            if (ITEMS_MAP.size() <= i)
                ITEMS_MAP.add(new ArrayList<>());
            ITEMS_MAP.get(i).add(item);
        }

        public int rows() {
            return ITEMS_MAP.size();
        }

        public int columns() {
            return ITEMS_MAP.get(0).size();
        }

        public void correct_position() {
            clear();
            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < columns(); j++) {
                    get(i, j).show();
                }
            }
        }

        public void clear() {
            boolean cleared = false;
            ThreeItem item;
            SideItems side;
            int i, j, type;
            while (!cleared) {
                cleared = true;
                for (i = 0; i < rows(); i++) {
                    for (j = 0; j < columns(); j++) {
                        item = get(i, j);
                        side = getRoundItems(item);
                        if (side != null && side.haveAroundItems()) {
                            type = item.type();
                            while (item.type() == type)
                                item.setRandomItem();
                            cleared = false;
                        }
                    }
                }
            }
        }

        public SideItems getRoundItems(int i, int j) {
            return getRoundItems(get(i, j));
        }

        public SideItems getRoundItems(ThreeItem item) {
            if (item == null || item.type() < 5)
                return null;
            SideItems side = new SideItems();
            // Подсчёт схожих элементов сверху
            for (side.up = 0; item.y - 1 - side.up >= 0; side.up++)
                if (!get(item.y - 1 - side.up, item.x).equals(item.type()))
                    break;
            // Подсчёт схожих элементов снизу
            for (side.down = 0; item.y + 1 + side.down < rows(); side.down++)
                if (!get(item.y + 1 + side.down, item.x).equals(item.type()))
                    break;
            // Подсчёт схожих элементов слева
            for (side.left = 0; item.x - 1 - side.left >= 0; side.left++)
                if (!get(item.y, item.x - 1 - side.left).equals(item.type()))
                    break;
            // Подсчёт схожих элементов справа
            for (side.right = 0; item.x + 1 + side.right < columns(); side.right++)
                if (!get(item.y, item.x + 1 + side.right).equals(item.type()))
                    break;
            // Наличие кубика сверху слева
            for (int i = 1; i <= side.up && i <= side.left; i++) {
                boolean b = false;
                for (int j = 1; j <= i; j++) {
                    b = get(item.y - i, item.x - j).equals(item.type()) &&
                            get(item.y - j, item.x - i).equals(item.type());
                    if (!b) break;
                }
                if (!b) break;
                side.upLeft++;
            }
            // Наличие кубика сверху справа
            for (int i = 1; i <= side.up && i <= side.right; i++) {
                boolean b = false;
                for (int j = 1; j <= i; j++) {
                    b = get(item.y - i, item.x + j).equals(item.type()) &&
                            get(item.y - j, item.x + i).equals(item.type());
                    if (!b) break;
                }
                if (!b) break;
                side.upRight++;
            }
            // Наличие кубика снизу слева
            for (int i = 1; i <= side.down && i <= side.left; i++) {
                boolean b = false;
                for (int j = 1; j <= i; j++) {
                    b = get(item.y + i, item.x - j).equals(item.type()) &&
                            get(item.y + j, item.x - i).equals(item.type());
                    if (!b) break;
                }
                if (!b) break;
                side.downLeft++;
            }
            // Наличие кубика снизу справа
            for (int i = 1; i <= side.down && i <= side.right; i++) {
                boolean b = false;
                for (int j = 1; j <= i; j++) {
                    b = get(item.y + i, item.x + j).equals(item.type()) &&
                            get(item.y + j, item.x + i).equals(item.type());
                    if (!b) break;
                }
                if (!b) break;
                side.downRight++;
            }
            return side;
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
            return out.toString().trim() + "]\n";
        }
    }

    public interface MoveAction {
        void finishMove(ThreeItem item);
    }
}
