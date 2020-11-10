package ru.happy.game.adventuredog.Obj;

import java.util.List;

public class LoadedMusic {

    public int success;
    public String message;
    public List<Music> musics;

    public static class Music {
        public float len;
        public int success, id, year, type, bit;
        public String title, path, artist, image, cut;
    }
}
