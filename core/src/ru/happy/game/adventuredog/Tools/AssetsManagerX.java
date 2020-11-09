package ru.happy.game.adventuredog.Tools;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AssetsManagerX {
    // Карты для хранения свойств
    Map<Integer, Map<String, String>> prop;
    Map<Integer, ArrayList<String>> levels;
    Map<String, AssetDescriptor<Texture>> texture;
    Map<String, AssetDescriptor<TextureAtlas>> atlas;
    Map<String, AssetDescriptor<Music>> musics;
    Map<String, AssetDescriptor<Sound>> sounds;

    // Текущий уровень
    private int level;

    // Конструктор
    public AssetsManagerX() {
        prop = new HashMap<>();
        texture = new HashMap<>();
        levels = new HashMap<>();
        atlas = new HashMap<>();
        musics = new HashMap<>();
        sounds = new HashMap<>();
    }

    // Установка текущего уровня
    public void setLevel(int level) {
        this.level = level;
    }

    // Добавление свойств
    public void setProperty(String key, String value) {
        setProperty(-1, key, value);
    }
    public void setProperty(Integer level, String key, String value) {
        if (!prop.containsKey(level)) prop.put(level, new HashMap<>());
        prop.get(level).put(key, value);
    }

    // Получение свойств ввиде числа
    public Integer getInt(Integer level, String key) {
        return getInt(level, key, 0);
    }
    public Integer getInt(String key, Integer def) {
        return getInt(-1, key, def);
    }
    public Integer getInt(Integer level, String key, Integer def) {
        if (prop.containsKey(level) && prop.get(level).containsKey(key)) {
            try {
                return Integer.parseInt(prop.get(level).get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }
    public Integer getInt(String key) {
        return getInt(-1, key);
    }

    // Получение свойств ввиде строки
    public String getString(Integer level, String key) {
        return getString(level, key, null);
    }
    public String getString(Integer level, String key, String def) {
        if (prop.containsKey(level) && prop.get(level).containsKey(key))
            return prop.get(level).get(key);
        return def;
    }
    public String getString(String key, String def) {
        return getString(-1, key, def);
    }
    public String getString(String key) {
        return getString(-1, key);
    }

    // Добавление ресурса
    public void add(Integer level, String name, String path, String type) {
        if (!levels.containsKey(level)) levels.put(level, new ArrayList<>());
        levels.get(level).add(name);
        if (type.equalsIgnoreCase("TA")) addAtlas(level + "_" + name, path);
        else if (type.equalsIgnoreCase("T")) addTexture(level + "_" + name, path);
        else if (type.equalsIgnoreCase("M")) addMusic(level + "_" + name, path);
        else if (type.equalsIgnoreCase("S")) addSound(level + "_" + name, path);
    }
    private void addTexture(String name, String path) {
        this.texture.put(name, new AssetDescriptor<>(path, Texture.class));
    }
    private void addAtlas(String name, String path) {
        this.atlas.put(name, new AssetDescriptor<>(path, TextureAtlas.class));
    }
    private void addMusic(String name, String path) {
        this.musics.put(name, new AssetDescriptor<>(path, Music.class));
    }
    private void addSound(String name, String path) {
        this.sounds.put(name, new AssetDescriptor<>(path, Sound.class));
    }

    // Получение ресурса
    public AssetDescriptor<?> get(Integer level, String name) {
        return getMain(level+"_"+name);
    }
    public AssetDescriptor<TextureAtlas> getAtlas(String name) {
        return atlas.get(name);
    }
    public AssetDescriptor<Texture> getTexture(String name) {
        return texture.get(name);
    }
    public AssetDescriptor<Music> getMusic(String name) {
        return musics.get(name);
    }
    public AssetDescriptor<Sound> getSound(String name) {
        return sounds.get(name);
    }
    public AssetDescriptor<?> get(String name){
        return get(name,true);
    }
    public AssetDescriptor<?> get(String name, boolean isLevel) {
        return isLevel?get(level,name):getMain(name);
    }
    public AssetDescriptor<?> getMain(String name) {
        if (texture.containsKey(name)) return getTexture(name);
        else if (atlas.containsKey(name)) return getAtlas(name);
        else if (musics.containsKey(name)) return getMusic(name);
        else if (sounds.containsKey(name)) return getSound(name);
        return null;
    }

    // Удаление ресурса
    public void delete(String name){
        delete(name,true);
    }
    public void delete(String name, boolean level){
        String n = level?this.level+"_"+name:name;
        levels.get(level?this.level:-1).remove(name);
        texture.remove(n);
        atlas.remove(n);
        musics.remove(n);
        sounds.remove(n);
    }

    // Получение основного ресурса интерфейса
    public AssetDescriptor<TextureAtlas> getGUI() {
        return getAtlas("0_graphic");
    }

    // Получение всех ресурсов уровня
    public ArrayList<String> getFiles(Integer level) {
        return levels.get(level);
    }

    // Очистка всех ресурсов
    public void clear() {
        prop.clear();
        atlas.clear();
        levels.clear();
        sounds.clear();
        musics.clear();
        texture.clear();
    }
}
