package ru.happy.game.adventuredog.Obj;

import java.util.List;

public class AestheticsObject {

    public int success;
    public String message, path;
    public List<aesthetics> aesthetics;

    public static class aesthetics {
        public float len;
        public int success, id, type;
        public String name, hide, path, added;
    }
}
