package ru.happy.game.adventuredog.Tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.Map;

public class GamePrefs implements Preferences {

    Preferences p;

    public GamePrefs(String name) {
        p = Gdx.app.getPreferences(name);
    }

    @Override
    public Preferences putBoolean(String key, boolean val) {
        if (!key.equals("sync")) p.putBoolean("sync", false);
        return p.putBoolean(key, val);
    }

    @Override
    public Preferences putInteger(String key, int val) {
        p.putBoolean("sync", false);
        return p.putInteger(key, val);
    }

    @Override
    public Preferences putLong(String key, long val) {
        p.putBoolean("sync", false);
        return p.putLong(key, val);
    }

    @Override
    public Preferences putFloat(String key, float val) {
        p.putBoolean("sync", false);
        return p.putFloat(key, val);
    }

    @Override
    public Preferences putString(String key, String val) {
        p.putBoolean("sync", false);
        return p.putString(key, val);
    }

    @Override
    public Preferences put(Map<String, ?> vals) {
        p.putBoolean("sync", false);
        return p.put(vals);
    }

    @Override
    public boolean getBoolean(String key) {
        return p.getBoolean(key);
    }

    @Override
    public int getInteger(String key) {
        return p.getInteger(key);
    }

    @Override
    public long getLong(String key) {
        return p.getLong(key);
    }

    @Override
    public float getFloat(String key) {
        return p.getFloat(key);
    }

    @Override
    public String getString(String key) {
        return p.getString(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return p.getBoolean(key, defValue);
    }

    @Override
    public int getInteger(String key, int defValue) {
        return p.getInteger(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return p.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return p.getFloat(key, defValue);
    }

    @Override
    public String getString(String key, String defValue) {
        return p.getString(key, defValue);
    }

    @Override
    public Map<String, ?> get() {
        return p.get();
    }

    @Override
    public boolean contains(String key) {
        return p.contains(key);
    }

    @Override
    public void clear() {
        p.clear();
    }

    @Override
    public void remove(String key) {
        p.remove(key);
    }

    @Override
    public void flush() {
        p.flush();
    }
}
