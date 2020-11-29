package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import ru.happy.game.adventuredog.Interfaces.ElementsUI;
import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;

public class TextView implements ElementsUI {

    private final MainGDX game;
    public final float LINE_SPACE = 1.2f;
    private String text;
    float x, y, scale, line_height, max_width;
    ALIGN cX, cY;
    GameWorld.FONTS font;
    Color fill, stroke;

    public TextView(MainGDX game, String text, float x, float y, ALIGN cX, ALIGN cY, GameWorld.FONTS font, Color fill, Color stroke, float scale, float max_width){
        this.game = game;
        this.font = font;
        setColors(fill,stroke);
        setAlign(cX,cY);
        setMaxWidth(max_width);
        setPos(x,y);
        setScale(scale);
        setText(text);
    }

    public TextView(MainGDX game, String text, float x, float y, ALIGN cX, ALIGN cY, GameWorld.FONTS font, Color fill, Color stroke, float scale){
        this(game,text,x,y,cX,cY,font,fill,stroke,scale,0);
    }
    public TextView(MainGDX game, String text, float x, float y, ALIGN cX, ALIGN cY, GameWorld.FONTS font, Color fill, Color stroke){
        this(game,text,x,y,cX,cY,font,fill,stroke,1f);
    }
    public TextView(MainGDX game, String text, float x, float y, ALIGN cX, ALIGN cY, GameWorld.FONTS font, Color fill){
        this(game,text,x,y,cX,cY,font,fill,null);
    }
    public TextView(MainGDX game, String text, float x, float y, ALIGN cX, ALIGN cY, GameWorld.FONTS font){
        this(game,text,x,y,cX,cY,font,Color.valueOf("#000000"));
    }
    public TextView(MainGDX game, String text, float x, float y, ALIGN cX, ALIGN cY){
        this(game,text,x,y,cX,cY, GameWorld.FONTS.SMEDIAN);
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public TextView setColor(Color fill) {
        this.fill = fill;
        return this;
    }

    public TextView setColors(Color fill, Color stroke) {
        setColor(fill);
        this.stroke = stroke;
        return this;
    }

    public TextView setAlign(ALIGN cX, ALIGN cY) {
        this.cX = cX;
        this.cY = cY;
        return this;
    }

    public TextView setFont(GameWorld.FONTS font) {
        this.font = font;
        fixHeight();
        return this;
    }

    public TextView setScale(float scale) {
        this.scale = scale;
        fixHeight();
        return this;
    }

    public TextView setMaxWidth(float max_width) {
        this.max_width = max_width;
        return setText(text);
    }

    public TextView setText(String text) {
        this.text = max_width==0?text:format(text);
        return this;
    }

    private void fixHeight(){
        this.line_height = game.world.getTextSize("T",scale,font)[1]*LINE_SPACE;
    }

    private float width(String text){
        return game.world.getTextSize(text,scale,font)[0];
    }

    private String format(String i) {
        String formatted = format(i, " ");
        if (width(formatted) > max_width)
            formatted = format(i, "");
        return formatted;
    }

    private String format(String i, String j) {
        StringBuilder n = new StringBuilder();
        StringBuilder m = new StringBuilder();
        for (String s : i.split(j)) {
            if (width(n+s) > max_width) {
                m.append(n).append("\n");
                n = new StringBuilder();
            }
            n.append(s).append(j);
        }
        m.append(n.toString().trim());
        return m.toString();
    }

    @Override
    public void draw(MainGDX game, float delta) {
        String[] lines = text.split("\n");
        float posY = y, posX = x;
        switch (cY){
            case CENTER:
                posY -= line_height * (lines.length-1) / 2f;
                break;
            case BOTTOM:
            case RIGHT:
                posX += line_height * lines.length;
                break;
        }
        switch (cX){
            case BOTTOM:
            case RIGHT:
                posX -= width(text);
                break;
        }
        for (int i = 0; i < lines.length; i++)
            if (stroke == null) game.world.setText(lines[i],scale,posX,posY-i*line_height,fill,cX==ALIGN.CENTER,cY==ALIGN.CENTER,font);
            else game.world.setText(lines[i],scale,posX,posY-i*line_height,fill, stroke,cX==ALIGN.CENTER,cY==ALIGN.CENTER,font);
    }

    @Override
    public boolean isClick(float x, float y) {
        return false;
    }

    @Override
    public boolean isClick(Vector2 v) {
        return false;
    }

    @Override
    public void setCursor(float x, float y) {

    }

    @Override
    public void setCursor(Vector2 v) {

    }

    @Override
    public boolean isActive() {
        return false;
    }
}
