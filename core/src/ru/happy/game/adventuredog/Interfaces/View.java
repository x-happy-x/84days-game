package ru.happy.game.adventuredog.Interfaces;

public interface View {
    void onSizeChange(float w, float h);

    void addListener(SizeChangeListener listener);

    void clear();

    float getWidth();

    float getHeight();

    interface SizeChangeListener {
        void onSizeChange(float w, float h);
    }
}