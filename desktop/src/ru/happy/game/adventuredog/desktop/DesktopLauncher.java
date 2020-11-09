package ru.happy.game.adventuredog.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.happy.game.adventuredog.MainGDX;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1000;
		config.height = 500;
		//config.fullscreen = true;
		config.samples = 2;
		config.title = "Adventure Dog";
		config.addIcon("icon.png", Files.FileType.Internal);
		new LwjglApplication(new MainGDX(), config);
	}
}
