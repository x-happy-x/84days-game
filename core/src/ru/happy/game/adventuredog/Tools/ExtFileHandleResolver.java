package ru.happy.game.adventuredog.Tools;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

public class ExtFileHandleResolver implements FileHandleResolver {
    @Override
    public FileHandle resolve(String fileName) {
        return AssetsTool.getFileHandler(fileName);
    }
}
