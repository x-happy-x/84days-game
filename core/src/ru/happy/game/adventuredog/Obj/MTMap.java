package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import ru.happy.game.adventuredog.MainGDX;

public class MTMap {
    private final TextureAtlas atlas;
    private final ArrayList<ArrayList<ThreeItem>> map;
    private static final String[] elements = new String[]{
            "bird",
            "mouse",
            "bear",
            "fox",
            "panda"
    };
    private static float START_Y, START_X, ITEM_SIZE, OFFSET;

    public MTMap(String mapFile, TextureAtlas atlas) {
        this.atlas = atlas;
        map = new ArrayList<>();
        String[] map_string = mapFile.trim().split("\n");
        ITEM_SIZE = (float) MainGDX.HEIGHT / map_string.length;
        OFFSET = ITEM_SIZE / 20;
        ITEM_SIZE -= OFFSET * 2;
        START_Y = 0;
        START_X = (MainGDX.WIDTH - map_string[0].length() * (ITEM_SIZE + OFFSET * 2)) / 2f;

        for (int i = 0; i < map_string.length; i++) {
            map.add(new ArrayList<>());
            String[] k = map_string[i].split("");
            for (int j = 0; j < k.length; j++) {
                try {
                    map.get(i).add(new ThreeItem(j, i, Integer.parseInt(k[j])));
                } catch (NumberFormatException ignored) {
                    map.get(i).add(new ThreeItem(j, i, 0));
                }
            }
        }
    }

    public ThreeItem get(int i, int j) {
        return map.get(rows()-1-i).get(j);
    }

    public void set(int i, int j, ThreeItem item) {
        map.get(i).set(j,item);
    }

    public void draw(MainGDX game, float delta) {
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                ThreeItem item = get(i,j);
                //if (item.isMovingEnd()) {
                //    ThreeItem buf = get(i+1,j);
                //    set(i+1,j);
                //}
                item.draw(game,delta);
            }
        }
    }

    public ThreeItem contain(Vector2 pos) {
        if (pos.x < START_X || pos.x > START_X + (columns()-1) * (ITEM_SIZE + OFFSET * 2) || pos.y < START_Y || pos.y > START_Y + (rows()-1) * (ITEM_SIZE + OFFSET * 2)) return null;
        int i;
        return null;
        //if ()
    }

    public void check(int i, int j) {
        if (get(i,j).isMoving() || get(i,j).getType() == 0) return;
        int left, right, up, down, _type_ = get(i, j).getItemType();
        for (up = 0; i-1-up >= 0; up++) {
            if (get(i-1-up, j).getItemType() != _type_) break;
        }
        for (down = 0; i+1+down < rows(); down++) {
            if (get(i+1+down, j).getItemType() != _type_) break;
        }
        for (left = 0; j-1-left >= 0; left++) {
            if (get(i, j-1-left).getItemType() != _type_) break;
        }
        for (right = 0; j+1+right < columns(); right++) {
            if (get(i, j+1+right).getItemType() != _type_) break;
        }
        if (left + right >= 2) {
            for (int k = j - left; k <= j + right; k++) {
                //get(i,k).d
                for (int m = i; m >= 0; m--) {
                    get(m,k).down();
                }
            }
        }
        if (up + down >= 2) {
            for (int m = i-up-1; m >= 0; m--) {
                get(m,j).down(up+down);
            }
        }
    }

    public int rows(){
        return map.size();
    }

    public int columns(){
        return map.get(0).size();
    }

    public class ThreeItem {

        private Sprite picture;
        private static final float ANIMATION_DEFAULT_TIME = 1f;
        private final Rectangle rect_bg;
        private final Rectangle rect_current;
        private final Rectangle rect_move;
        private float deltaAction, finishAction;
        private int _type_, _item_;
        private boolean moving, movingEnd;

        ThreeItem(int x, int y, int _type_) {
            this.rect_current = new Rectangle(
                    START_X + x * (ITEM_SIZE + OFFSET * 2),
                    START_Y + y * (ITEM_SIZE + OFFSET * 2),
                    ITEM_SIZE + OFFSET * 2,
                    ITEM_SIZE + OFFSET * 2);
            this.rect_bg = new Rectangle(rect_current);
            this.rect_move = new Rectangle(rect_current);
            this._type_ = _type_;
            this.deltaAction = this.finishAction = 0;
            switch (this._type_) {
                case 1:
                    this._item_ = MathUtils.random(elements.length - 1);
                    this.picture = new Sprite();
                    this.picture.setRegion(atlas.findRegion(elements[_item_]));
                    this.picture.setBounds(
                            rect_current.x+OFFSET,
                            rect_current.y+OFFSET,
                            rect_current.width-OFFSET*2,
                            rect_current.height-OFFSET*2);
                    break;
                case 0:
                    break;
            }
        }

        public int getType() {
            return _type_;
        }

        public int getItemType() {
            return _item_;
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
                            rect_current.x+OFFSET,
                            rect_current.y+OFFSET,
                            rect_current.width-OFFSET*2,
                            rect_current.height-OFFSET*2);
                }
            }
            picture.draw(game.getBatch());
        }
    }
}
