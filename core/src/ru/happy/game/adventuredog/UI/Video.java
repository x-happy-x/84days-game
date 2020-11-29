package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Video {

    public long lastTime;
    public int frames;
    public float position;
    boolean playing;
    private final float fps;
    private final long milliDelay;
    private final Music audio;
    private final ArrayList<byte[]> videoData;
    private Pixmap pixmap;
    private final Texture texture;

    public Video(Music audio, FileHandle video, int w, int h) {
        this.audio = audio;
        int i;
        ByteBuffer buffer = ByteBuffer.wrap(video.readBytes());
        fps = buffer.getFloat();
        position = 0;
        milliDelay = (long) (1000 / fps);
        int videoCount = buffer.getInt();
        frames = videoCount;
        int[] videoPositions = new int[videoCount];
        int[] videoSizes = new int[videoCount];
        for (i = 0; i < videoCount; i++) {
            videoPositions[i] = buffer.getInt();
            videoSizes[i] = buffer.getInt();
        }
        videoData = new ArrayList<>();
        for (i = 0; i < videoCount; i++) {
            byte[] data = new byte[videoSizes[i]];
            buffer.get(data);
            videoData.add(data);
        }
        texture = new Texture(w, h, Pixmap.Format.RGB888);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public long getMilliDelay() {
        return milliDelay;
    }

    public Texture getFrame() {
        return getFrameAtTime(position);
    }

    public void update(float delta) {
        if (playing) {
            position += delta;
            if ((int) (fps * position) >= frames) {
                position = frames / fps;
                playing = false;
            }
        }
    }

    public Texture getFrame(int frame) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= getMilliDelay()) {
            if (frame >= frames) {
                frame = frames - 1;
                stop();
            }
            if (videoData.get(frame).length > 0) {
                pixmap = new Pixmap(videoData.get(frame), 0, videoData.get(frame).length);
                texture.draw(pixmap, 0, 0);
                pixmap.dispose();
                pixmap = null;
            }
            lastTime = currentTime;
        }
        return texture;
    }

    public Texture getFrameAtTime(float seconds) {
        return getFrame((int) (fps * seconds));
    }

    public void play() {
        playing = true;
        audio.play();
    }

    public void stop() {
        playing = false;
        position = 0;
        audio.stop();
    }
}
