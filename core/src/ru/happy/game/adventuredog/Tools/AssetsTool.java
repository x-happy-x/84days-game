package ru.happy.game.adventuredog.Tools;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ru.happy.game.adventuredog.MainGDX;

public class AssetsTool {

    public static int ZIP_UNPACK_COUNT, ZIP_UNPACK_ALL;
    public AssetDescriptor<Texture> bg;
    AssetsManagerX managerX;
    AssetManager manager;
    float progressNow;
    int level;

    // Конструктор
    public AssetsTool() {
        manager = new AssetManager(new ExtFileHandleResolver());
    }


    // ----------------------------- СТАТИЧЕСКИЕ ФУНКЦИИ -------------------------------------------
    // Игра запущена на Android
    public static boolean isAndroid() {
        return Gdx.app.getType().equals(Application.ApplicationType.Android);
    }


    // Сохранения и загрузка хэш-таблицы
    public static void setParamToFile(String path, Map<String, String> map) {
        String out = "";
        for (String key : map.keySet()) {
            out += key + ": " + map.get(key) + "\n";
        }
        AssetsTool.getFileHandler(path).writeString(out, false);
    }

    public static Map<String, String> getParamFromFile(String fileText) {
        Map<String, String> map = new HashMap<>();
        for (String row : fileText.replace("\r", "").trim().split("\n")) {
            map.put(row.split(":")[0].trim(), row.split(":", 2)[1].trim());
        }
        return map;
    }


    // Кодировка текста
    public static String encodeString(String x, boolean y) {
        try {
            return new String(x.getBytes(y ? "UTF-8" : "cp1251"), y ? "cp1251" : "UTF-8");
        } catch (UnsupportedEncodingException | NullPointerException ignored) {
        }
        return x;
    }

    public static String encodePlatform(String text, boolean fromUTF) {
        return isAndroid() ? text : encodeString(text, fromUTF);
    }

    public static String encodePlatform(String text) {
        return encodePlatform(text, false);
    }

    public static FileHandle getAbsoluteFileHandle(String path) {
        return Gdx.files.absolute(path);
    }


    // Получить файл-хандлер из директории игры
    public static FileHandle getFileHandler(String path) {
        return Gdx.files.absolute(getDataPath() + "/" + path);
    }


    // Получить файл
    public static File getFile(String path) {
        return getFileHandler(path).file();
    }


    // Получить содержимое файла
    public static String readFile(String path) {
        return getFileHandler(path).readString().replace("\r", "");
    }


    // Проверка на существование такой папки или файла
    public static boolean isExists(String path) {
        return getFileHandler(path).exists();
    }


    // Получить путь расположения игровых файлов
    private static String getDataPath() {
        switch (Gdx.app.getType()) {
            case Android:
                return Gdx.files.getLocalStoragePath();
            case Desktop:
                return "../../data";
        }
        return null;
    }

    public static String removeDataPath(String path) {
        String absolutePath = getFile("menu").getParentFile().getAbsolutePath();
        return path.replace(absolutePath, "").trim();
    }

    public static void delete(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) delete(f);
                else f.delete();
            }
        }
        folder.delete();
    }


    // Распаковка обновления
    public static boolean extractObb(File obbFile) {
        return unpackZip(obbFile, getDataPath());
    }


    // Распаковка zip-архива
    public static boolean unpackZip(File zip, String path) {
        try {
            int BUFFER_SIZE = 1024;
            ZIP_UNPACK_COUNT = 0;
            ZIP_UNPACK_ALL = new ZipFile(zip.getAbsoluteFile()).size();
            int size;
            byte[] buf = new byte[BUFFER_SIZE];
            File ePath = new File(path);
            MainGDX.write("UNPACK: " + zip.getAbsolutePath());
            if (ePath.mkdirs())
                MainGDX.write("UNPACK: Created output dir: " + ePath.getAbsolutePath());
            ZipInputStream source = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
            ZipEntry ze;
            while ((ze = source.getNextEntry()) != null) {
                File child = new File(path + "/" + ze.getName());
                MainGDX.write("UNPACK: " + ze.getName());
                if (ze.isDirectory()) {
                    if (!child.isDirectory() && child.mkdirs())
                        ZIP_UNPACK_COUNT++;
                } else {
                    if (!child.getParentFile().exists() && child.getParentFile().mkdirs())
                        ZIP_UNPACK_COUNT++;
                    ZIP_UNPACK_COUNT++;
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(child, false));
                    while ((size = source.read(buf, 0, BUFFER_SIZE)) != -1) {
                        out.write(buf, 0, size);
                    }
                    source.closeEntry();
                    out.flush();
                    out.close();
                }
            }
            source.close();
            return true;
        } catch (IOException e) {
            MainGDX.write("UNPACK: ERROR - " + e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean packZip(File zip, ZipPart... files) {
        FileOutputStream fOut;
        ZipOutputStream zOut;
        try {
            fOut = new FileOutputStream(zip);
            zOut = new ZipOutputStream(fOut);
            for (ZipPart f : files) {
                ZipEntry ze = new ZipEntry(f.path);
                zOut.putNextEntry(ze);
                zOut.write(Gdx.files.absolute(f.file.getAbsolutePath()).readBytes());
                zOut.closeEntry();
            }
            zOut.close();
            return true;
        } catch (IOException e) {
            MainGDX.write(e.getLocalizedMessage());
        }
        return false;
    }


    // Размер файла из байтов в любую другую
    public static String formatSize(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " Б";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("КМГТПЕ");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cБ", value / 1024.0, ci.current());
    }


    // Установить менеджер параметров
    public void setManager(AssetsManagerX manager) {
        managerX = manager;
    }


    // Получить уровень
    public int getLevel() {
        return level;
    }


    // Сменить уровень
    public void setLevel(int level) {
        this.level = level;
    }


    // Получить путь к файлам текущего уровня
    public String getLevelPath() {
        return managerX.getString(level, "path");
    }


    // Проверка на существование файла в текущем уровне
    public boolean isLevelFile(String file) {
        return isExists(getLevelPath() + "/" + file);
    }


    // Получить содержимое файла из текущего уровня
    public String getLevelContent(String file) {
        return readFile(getLevelPath() + "/" + file);
    }


    // Получить содержимое файла из общих файлов
    public String getContent(String file) {
        return readFile("menu/" + file);
    }


    // Добавить ресурсы в менеджер
    public void load() {
        if (level >= 0) {
            try {
                for (String o : managerX.getFiles(level)) {
                    manager.load(managerX.get(level, o));
                }
            } catch (NullPointerException ignored){}
        } else manager.load(bg);
        if (level != 0) {
            manager.load(managerX.getGUI());
        }
    }

    public void load(String name) {
        manager.load(managerX.get(name));
    }


    // Получить ресурс
    public <T> T get(AssetDescriptor<T> descriptor) {
        return manager.get(descriptor);
    }

    public <T> T get(String name) {
        return manager.get((AssetDescriptor<T>) managerX.get(name));
    }


    // Очистить ресурсы и прогресс
    public void fresh() {
        manager.clear();
        progressNow = 0;
    }


    // Загрузка ресурсов
    public void finishLoad(AssetDescriptor<?> assetDescriptor) {
        manager.finishLoadingAsset(assetDescriptor.fileName);
    }


    // Фоновая загрузка ресурсов
    public boolean updating() {
        return !manager.update();
    }


    // Прогресс
    public float getProgress(boolean smooth) {
        if (smooth) return getProgress();
        return manager.getProgress();
    }


    // Плавный прогресс
    public float getProgress() {
        float progressNew = manager.getProgress();
        if (progressNow < 1 && progressNew >= progressNow)
            progressNow += Math.max(0.0005f, (progressNew - progressNow) / 100);
        if (progressNow > 1) progressNow = 1f;
        return progressNow;
    }

    public static class ZipPart {
        public File file;
        public String path;

        public ZipPart(File f, String path) {
            this.file = f;
            this.path = path;
        }
    }

    public static String replace(String str, String charset, String r, boolean clearing){
        for (String char_: charset.split("")){
            str = str.replace(char_,r);
        }
        if (clearing)
            while (str.contains("  "))
                str = str.replace("  ", " ");
        return str;
    }
}