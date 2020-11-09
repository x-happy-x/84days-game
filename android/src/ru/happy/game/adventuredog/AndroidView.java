package ru.happy.game.adventuredog;

import java.util.ArrayList;

import ru.happy.game.adventuredog.Tools.SizeChangeListener;
import ru.happy.game.adventuredog.Tools.View;

public class AndroidView implements View {
    private ArrayList<SizeChangeListener> listeners;
    private float width, height;
    public AndroidView(int width, int height){
        this.width = width;
        this.height = height;
        listeners = new ArrayList<>();
    }
    @Override
    public void onSizeChange(float w, float h) {
        this.width = w;
        this.height = h;
        for (SizeChangeListener listener: listeners)
            listener.onSizeChange(w,h);
    }

    @Override
    public void addListener(SizeChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void clear() {
        listeners.clear();
    }
}
