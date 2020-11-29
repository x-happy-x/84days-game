package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.io.File;
import java.util.Map;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Tools.AssetsTool;

public class VideoPlayer {

    MainGDX game;
    Video video;
    Thread videoLoader;
    Sprite frame;
    Music audio;
    int width, height;
    float fps, shadow_h;
    boolean loaded;
    Color bg, transparent;

    public VideoPlayer(MainGDX game, int x, float y, float w, float h) {
        this.game = game;
        frame = new Sprite();
        frame.setBounds(x, y, w, h);
        bg = Color.valueOf("#000000FF");
        transparent = Color.valueOf("#00000000");
        shadow_h = 0.1f;
    }

    public void draw(float delta) {
        if (isLoaded() && video == null) {
            video = new Video(audio, AssetsTool.getFileHandler("cache/video_buffer/data"), width, height);
        }
        if (video != null) {
            frame.setRegion(video.getFrame());
            video.update(delta);
            float w = frame.getRegionWidth(), h = frame.getRegionHeight();
            if (frame.getWidth() / frame.getHeight() > w / h) {
                frame.setRegion(0, 0, (int) w, (int) (frame.getHeight() / frame.getWidth() * w));
                frame.setRegion(0, (int) ((h - frame.getRegionHeight()) / 2f), frame.getRegionWidth(), frame.getRegionHeight());
            } else {
                frame.setRegion(0, 0, (int) (frame.getWidth() / frame.getHeight() * h), (int) h);
                frame.setRegion((int) ((w - frame.getRegionWidth()) / 2f), 0, frame.getRegionWidth(), frame.getRegionHeight());
            }
            frame.draw(game.getBatch());
            game.end();
            drawShadow(delta);
            game.draw();
        }
    }

    public void drawShadow(float delta) {
        game.drawShape();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        game.renderer.rect(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight() / 3f, bg, bg, transparent, transparent);
        game.renderer.rect(frame.getX(), frame.getY() + frame.getHeight() * (1 - shadow_h), frame.getWidth(), frame.getHeight() * shadow_h, transparent, transparent, bg, bg);
        game.renderer.rect(frame.getX(), frame.getY(), frame.getHeight() * shadow_h, frame.getHeight(), bg, transparent, transparent, bg);
        game.renderer.rect(frame.getX() + frame.getWidth() - frame.getHeight() * shadow_h, frame.getY(), frame.getHeight() * shadow_h, frame.getHeight(), transparent, bg, bg, transparent);
        game.endShape();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void play() {
        if (video != null) video.play();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load(File video) {
        loaded = false;
        if (videoLoader == null || !videoLoader.isAlive())
            videoLoader = new Thread(() -> {
                if (AssetsTool.unpackZip(video, "cache/video_buffer")) {
                    Map<String, String> info = AssetsTool.getParamFromFile(AssetsTool.readFile("cache/video_buffer/video"));
                    width = Integer.parseInt(info.get("WIDTH"));
                    height = Integer.parseInt(info.get("HEIGHT"));
                    fps = Float.parseFloat(info.get("FPS"));
                    audio = Gdx.audio.newMusic(AssetsTool.getFileHandler("cache/video_buffer/audio.mp3"));
                    loaded = true;
                }
            });
        videoLoader.start();
    }

    public boolean isPlaying() {
        return video != null && video.playing;
    }
}
