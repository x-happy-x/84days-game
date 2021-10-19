package ru.happy.game.adventuredog.Obj;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.awt.Rectangle;

import ru.happy.game.adventuredog.Maps.MapTemplate;

public class MapFR implements MapTemplate {

    public MapFR(String config, String blocks, TextureAtlas atlas) {

    }

    @Override
    public boolean collide(Rectangle rectangle) {
        return false;
    }

    public class Block {
        public Block() {

        }
    }
}
