package ru.happy.game.adventuredog.Tools;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Screens.LoadScreen;

public class LevelSwitcher {

    public static void setLevel(MainGDX game, int level) {
        game.world.startSync();
        game.clearBg.set(0, 0, 0, 1);
        game.world.skipLevel = false;
        game.world.usedBonus = false;
        game.world.getBonus = false;
        game.world.firstErrVisible = false;
        game.world.firstVisible = false;
        game.world.getActors().clear();
        game.world.resetMultiplexer();
        if (game.getScreen() != null) game.getScreen().dispose();
        game.assets.fresh();
        game.assets.setLevel(level);
        game.manager.setLevel(level);
        game.assets.load();
        game.setScreen(new LoadScreen(game));
    }
}
