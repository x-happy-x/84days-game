package ru.happy.game.adventuredog.Tools;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class AssetsTool {

    int level;
    float progressNow;
    AssetManager manager;
    AssetsManagerX managerX;
    public AssetDescriptor<Texture> bg;
    //public static String[] LEVELS;
    //public static ArrayList<AssetDescriptor>[] objects;
    //public static Map<String,String>[] levelsProp;
    public static int extractCount, allExtractCount;

    public static Map<String, String> getParamFromFile(String fileText) {
        Map<String, String> map = new HashMap<>();
        for (String row : fileText.replace("\r", "").trim().split("\n")) {
            map.put(row.split(":")[0].trim(), row.split(":", 2)[1].trim());
        }
        return map;
    }
    public static String encodePlatform(String text){
        return encodePlatform(text,false);
    }
    public static String encodePlatform(String text,boolean fromUTF){
        return isAndroid()?text:encodeString(text,fromUTF);
    }

    public static void setParamToFile(String path, Map<String, String> map) {
        String out = "";
        for (String key : map.keySet()) {
            out += key + ": " + map.get(key) + "\n";
        }
        AssetsTool.getFile(path).writeString(out, false);
    }

    public static String encodeString(String x, boolean y) {
        try {
            return new String(x.getBytes(y ? "UTF-8" : "cp1251"), y ? "cp1251" : "UTF-8");
        } catch (UnsupportedEncodingException | NullPointerException ignored) { }
        return x;
    }

    public static String encodeString(String x, boolean y, boolean upper) {
        return encodeString(upper ? x.toUpperCase() : x, y);
    }

    public static boolean isAndroid() {
        return Gdx.app.getType().equals(Application.ApplicationType.Android);
    }

    public AssetsTool() {
        manager = new AssetManager(new ExtFileHandleResolver());
    }
    public void setManager(AssetsManagerX manager){
        managerX = manager;
    }

    public void fresh() {
        manager.clear();
        progressNow = 0;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public String getLevelPath() {
        return managerX.getString(level,"path");
    }

    public String getLevelFile(String file) {
        return readFile(getLevelPath() + "/" + file);
    }

    public boolean isLevelFile(String file) {
        return isExists(getLevelPath() + "/" + file);
    }

    //public Map<String, String> getLevelProp() {
    //    return levelsProp[level];
    //}

    public void load() {
        if (level >= 0) {
            for (String o : managerX.getFiles(level)) {
                manager.load(managerX.get(level,o));
            }
        }
        else manager.load(bg);
        if (level != 0) {
            manager.load(managerX.getGUI());
        }
    }
    public void load(String name) {
        manager.load(managerX.get(name));
    }

    public <T> T get(AssetDescriptor<T> descriptor) {
        return manager.get(descriptor);
    }
    public <T> T get(String name) {
        return manager.get((AssetDescriptor<T>) managerX.get(name));
    }

    //public AssetDescriptor get(int n) {
    //    return objects[level].get(n);
    //}

    public void finishLoad() {
        manager.finishLoading();
    }

    public void finishLoad(AssetDescriptor<?> assetDescriptor) {
        manager.finishLoadingAsset(assetDescriptor.fileName);
    }

    public float getProgress() {
        float progressNew = manager.getProgress();
        if (progressNow < 1 && progressNew >= progressNow)
            progressNow += Math.max(0.0005f, (progressNew - progressNow) / 100);
        if (progressNow > 1) progressNow = 1f;
        return progressNow;
    }

    public float getProgress(boolean smooth) {
        if (smooth) return getProgress();
        return manager.getProgress();
    }

    //public AssetDescriptor<TextureAtlas> getLoadGUI() {
    //    return managerX.getGUI();
    //}

    //public ArrayList<AssetDescriptor> getAssets() {
    //    return objects[level];
    //}

    public boolean updating() {
        return !manager.update();
    }

    private static FileHandle getAbsoluteFile(String path){
        switch (Gdx.app.getType()) {
            case Android:
                return Gdx.files.local(path);
            case Desktop:
                return Gdx.files.external(path);
        }
        return null;
    }
    public static FileHandle getFile(String path) {
        return getAbsoluteFile(getDataPath() + "/" + path);
    }

    public static File getFile(String path, boolean root) {
        return getAbsoluteFile((!root ? getDataPath() + "/" : "") + path).file();
    }

    public static String readFile(String path) {
        return getFile(path).readString().replace("\r", "");
    }

    public static boolean isExists(String path) {
        return getFile(path).exists();
    }

    public static String getDataPath() {
        switch (Gdx.app.getType()) {
            case Android:
                return "";
            case Desktop:
                return "Desktop/GameData";
        }
        return null;
    }

    public static void extractObb(File obbFile) {
        try {
            int BUFFER_SIZE = 1024;
            extractCount = 0;
            allExtractCount = new ZipFile(obbFile.getAbsoluteFile()).size();
            int size;
            byte[] buf = new byte[BUFFER_SIZE];
            File ePath = getFile(getDataPath(), true);
            if (!obbFile.isDirectory()) {
                ePath.mkdirs();
            }
            ZipInputStream source = new ZipInputStream(new BufferedInputStream(new FileInputStream(obbFile)));
            ZipEntry ze = null;
            while ((ze = source.getNextEntry()) != null) {
                String nPath = getDataPath() + "/" + ze.getName();
                if (ze.isDirectory()) {
                    extractCount++;
                    File unzipFile = getFile(nPath, true);
                    if (!unzipFile.isDirectory()) unzipFile.mkdirs();
                } else {
                    extractCount++;
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(getFile(nPath, true), false));
                    while ((size = source.read(buf, 0, BUFFER_SIZE)) != -1) {
                        out.write(buf, 0, size);
                    }
                    source.closeEntry();
                    out.flush();
                    out.close();
                }
            }
            source.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}