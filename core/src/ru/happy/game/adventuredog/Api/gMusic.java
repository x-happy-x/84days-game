package ru.happy.game.adventuredog.Api;

import java.util.List;

public class gMusic {

    public int success;
    public String message, path;
    public List<Music> musics;

    public static class Music {
        public float len;
        public int success, type;
        public String name, music, author, video;
    }
}
