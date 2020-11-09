package ru.happy.game.adventuredog.Tools;

public interface View {
    void onSizeChange(float w, float h);

    void addListener(SizeChangeListener listener);

    void clear();

    float getWidth();

    float getHeight();
}
