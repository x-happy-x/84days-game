package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.happy.game.adventuredog.MainGDX;

public class Door {

    public static ArrayList<Integer> opened;
    Map<String, Map<String, String>> doors = new HashMap<>();
    Map<String, Sprite> sprites = new HashMap<>();
    ArrayList<String> names = new ArrayList<>();

    public Door() {
        opened = new ArrayList<>();
        opened.add(1);
    }

    public void append(Map<String, String> map, TextureAtlas atlas) {
        doors.put(map.get("pic"), map);
        Sprite sprite = new Sprite();
        TextureAtlas.AtlasRegion region = atlas.findRegion(map.get("pic"));
        sprite.setRotation(region.rotate ? -90 : 0);
        doors.get(map.get("pic")).put("rotate", String.valueOf(region.rotate));
        doors.get(map.get("pic")).put("showed", String.valueOf(map.get("mult").equals("locked")));
        sprite.setRegion(region);
        sprites.put(map.get("pic"), sprite);
        names.add(map.get("pic"));
    }

    public void setRegion(int x, int y, int w, int h) {
        int ww, hh, xx, yy;
        boolean rotated;
        for (String name : names) {
            ww = (int) (Float.parseFloat(doors.get(name).get("w")) / w * MainGDX.WIDTH);
            hh = (int) (Float.parseFloat(doors.get(name).get("h")) / h * MainGDX.HEIGHT);
            xx = Integer.parseInt(doors.get(name).get("x")) - x;
            yy = Integer.parseInt(doors.get(name).get("y")) - y;
            rotated = Boolean.parseBoolean(doors.get(name).get("rotate"));
            sprites.get(name).setBounds((float) xx / w * MainGDX.WIDTH, MainGDX.HEIGHT - (!rotated ? hh : 0) - (float) yy / h * MainGDX.HEIGHT, rotated ? hh : ww, !rotated ? hh : ww);
        }
    }

    public String isClicked(float x, float y) {
        for (String name : names) {
            if (Boolean.parseBoolean(doors.get(name).get("showed")) && doors.get(name).get("mult").startsWith("lo")) {
                if (sprites.get(name).getBoundingRectangle().contains(x, y)) return name;
            }
        }
        return null;
    }

    public void open(String name) {
        if (doors.containsKey(name)) {
            doors.get(name).put("showed", "false");
            opened.add(Integer.parseInt(doors.get(name).get("layer")));
            doors.get(name + "u").put("showed", "true");
        }
    }

    public void draw(Batch batch) {
        for (String name : names) {
            if (Boolean.parseBoolean(doors.get(name).get("showed"))) {
                sprites.get(name).draw(batch);
            }
        }
    }

    public ArrayList<Integer> getOpened() {
        return opened;
    }
}
