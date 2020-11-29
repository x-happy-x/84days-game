package ru.happy.game.adventuredog.Interfaces;

import ru.happy.game.adventuredog.MainGDX;

public interface VideoPlayer {
    void stop();

    boolean isPlaying();

    void pause();

    void play();

    void setBounds(float x, float y, float w, float h);

    void loadVideo(String url, String title, String subtitle);

    void setListener(PlayerListener listener);

    class PlayerListener {
        public void onStop() {
            MainGDX.write("VIDEO STOP");
        }

        public void onStart() {
            MainGDX.write("VIDEO START");
        }

        public void onError(String error) {
            MainGDX.write("VIDEO ERROR: " + error);
        }
    }
}
