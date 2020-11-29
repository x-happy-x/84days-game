package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import ru.happy.game.adventuredog.Anim.ScreenAnim;
import ru.happy.game.adventuredog.Interfaces.ElementsUI;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;
import ru.happy.game.adventuredog.Screens.LoadScreen;
import ru.happy.game.adventuredog.Tools.GraphicTool;

public class Dialog extends ImageView {

    public final float SHOW_TIME = 0.2f, DARK_ALPHA = 0.4f;
    ArrayList<ElementsUI> elements;
    float show_delta;
    boolean showing;

    public Dialog(TextureRegion region, Button.Action action) {
        super(region, action);
        elements = new ArrayList<>();
        show_delta = 0;
        showing = false;
    }

    public Dialog addElement(ElementsUI element){
        elements.add(element);
        return this;
    }

    public boolean isClick(Vector2 v){
        if (showing) {
            if (!pos.contains(v)) {
                close();
                return true;
            }
            for (ElementsUI elem : elements) {
                if (elem.isActive() && elem.isClick(v.x, v.y)) {
                    close();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isOpened() {
        return showing;
    }

    public void open(){
        showing = true;
    }

    public void close(){
        showing = false;
    }

    Color batchColor = Color.valueOf("#00000000");
    public void draw(MainGDX game, float delta, Vector2 v) {
        if (showing) {
            if (show_delta < SHOW_TIME) {
                show_delta += delta;
                if (show_delta > SHOW_TIME) show_delta = SHOW_TIME;
                batchColor.a = DARK_ALPHA / SHOW_TIME * show_delta;
            }
        } else {
            if (show_delta > 0) {
                show_delta-=delta;
                if (show_delta < 0) show_delta = 0;
            }
            if (show_delta == 0) return;
        }
        game.drawShape();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        game.renderer.setColor(batchColor);
        game.renderer.rect(0,0,MainGDX.WIDTH,MainGDX.HEIGHT);
        game.endShape();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        game.draw();
        super.draw(game);
        for (ElementsUI elem: elements){
            elem.setCursor(v);
            elem.draw(game,delta);
        }
        game.end();
    }
}
