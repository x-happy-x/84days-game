package ru.happy.game.adventuredog.Tools;

import ru.happy.game.adventuredog.Interfaces.VideoPlayer;
import ru.happy.game.adventuredog.Interfaces.View;

public class ApplicationBundle {

    private final ru.happy.game.adventuredog.Interfaces.View view;
    private final VideoPlayer player;

    public ApplicationBundle(View view, VideoPlayer player) {
        this.view = view;
        this.player = player;
    }

    public View getView() {
        return view;
    }

    public VideoPlayer getVideo() {
        return player;
    }
}
