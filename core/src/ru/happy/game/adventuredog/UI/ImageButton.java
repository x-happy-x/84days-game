package ru.happy.game.adventuredog.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import ru.happy.game.adventuredog.MainGDX;
import ru.happy.game.adventuredog.Obj.GameWorld;

public class ImageButton extends Button {

    TextureRegion icon, icon2;
    public ALIGN alignIV, alignIH, alignIV2, alignIH2;
    float offsetIX, offsetIY, iconW, iconH, offsetIX2, offsetIY2, iconW2, iconH2;

    private ImageButton() {
        super();
    };

    public ImageButton(String text, TextureRegion region, TextureRegion icon, GameWorld world, Color color, Action action, ALIGN textGravityV, ALIGN textGravityH, ALIGN iconGravityV, ALIGN iconGravityH) {
        super(text, region, world, color, action, textGravityV, textGravityH);
        this.icon = icon;
        setDark(0);
        setOffsetX(25);
        setOffsetIX(10);
        setOffsetIY(8);
        setSize(MainGDX.WIDTH / 7.5f, getHeight());
        setIconSize(getHeight() - offsetIY * 2f);
        alignIV = iconGravityV;
        alignIH = iconGravityH;
    }

    public ImageButton(String text, TextureRegion region, TextureRegion icon, GameWorld world, Color color, Action action) {
        this(text, region, icon, world, color, action, ALIGN.CENTER, ALIGN.RIGHT, ALIGN.CENTER, ALIGN.LEFT);
        super.offsetY = 20;
    }

    public ImageButton(String text, TextureRegion region, TextureRegion icon, GameWorld world, Color color) {
        this(text, region, icon, world, color, null);
    }

    public ImageButton(String text, TextureRegion region, TextureRegion icon, GameWorld world) {
        this(text, region, icon, world, Color.BLACK);
    }

    public void setIconSize(float w, float h) {
        iconH = h;
        iconW = w;
    }

    public void setIconSize(float h) {
        iconH = h;
        iconW = (float) icon.getRegionWidth() / icon.getRegionHeight() * iconH;
    }

    public float getIconH() {
        return iconH;
    }

    public float getIconW() {
        return iconW;
    }

    public void setOffsetIX(float offsetX) {
        this.offsetIX = offsetX;
    }

    public void setOffsetIY(float offsetY) {
        this.offsetIY = offsetY;
    }

    public void setAlignI(ALIGN alignIH, ALIGN alignIV) {
        this.alignIH = alignIH;
        this.alignIV = alignIV;
    }

    public void setAlignI2(ALIGN alignIH2, ALIGN alignIV2) {
        this.alignIH2 = alignIH2;
        this.alignIV2 = alignIV2;
    }

    public float getOffsetIY() {
        return offsetIY;
    }

    public void addSmallIcon(TextureRegion icon) {
        icon2 = icon;
        alignIH2 = ALIGN.RIGHT;
        alignIV2 = ALIGN.CENTER;
        setOffsetIX2(-7);
        setOffsetIY2(getHeight() / 4f);
        setIconSize2(getHeight() - offsetIY * 2f);
    }

    public void setIconSize2(float w, float h) {
        iconH2 = h;
        iconW2 = w;
    }

    public void setIconSize2(float h) {
        iconH2 = h;
        iconW2 = (float) icon2.getRegionWidth() / icon2.getRegionHeight() * iconH2;
    }

    public float getIconH2() {
        return iconH2;
    }

    public float getIconW2() {
        return iconW2;
    }

    public void setOffsetIX2(float offsetX) {
        this.offsetIX2 = offsetX;
    }

    public void setOffsetIY2(float offsetY) {
        this.offsetIY2 = offsetY;
    }

    @Override
    public void draw(MainGDX game, float _delta_) {
        super.draw(game, _delta_);
        float iconX, iconY;
        boolean centerX, centerY;
        switch (alignIH) {
            case TOP:
            case LEFT:
                iconX = drawing.x + offsetIX;
                centerX = false;
                break;
            case BOTTOM:
            case RIGHT:
                iconX = drawing.x + drawing.width - iconW - offsetIX;
                centerX = false;
                break;
            case CUSTOM:
                iconX = drawing.x + drawing.width / 2f + offsetIX;
                centerX = true;
                break;
            case CENTER:
            default:
                iconX = drawing.x + drawing.width / 2f;
                centerX = true;
        }
        switch (alignIV) {
            case LEFT:
            case TOP:
                iconY = drawing.y + drawing.height - iconH - offsetIY;
                centerY = false;
                break;
            case RIGHT:
            case BOTTOM:
                iconY = drawing.y + offsetIY;
                centerY = false;
                break;
            case CUSTOM:
                iconY = drawing.y + drawing.height / 2f + offsetIY;
                centerY = true;
                break;
            case CENTER:
            default:
                iconY = drawing.y + drawing.height / 2f;
                centerY = true;
        }
        game.getBatch().draw(icon, centerX ? iconX - iconW / 2f : iconX, centerY ? iconY - iconH / 2f : iconY, iconW, iconH);
        if (icon2 != null) {
            switch (alignIH2) {
                case TOP:
                case LEFT:
                    iconX = drawing.x + offsetIX2;
                    centerX = false;
                    break;
                case BOTTOM:
                case RIGHT:
                    iconX = drawing.x + drawing.width - iconW2 - offsetIX2;
                    centerX = false;
                    break;
                case CUSTOM:
                    iconX = drawing.x + drawing.width / 2f + offsetIX2;
                    centerX = true;
                    break;
                case CENTER:
                default:
                    iconX = drawing.x + drawing.width / 2f;
                    centerX = true;
            }
            switch (alignIV2) {
                case LEFT:
                case TOP:
                    iconY = drawing.y + drawing.height - iconH2 - offsetIY2;
                    centerY = false;
                    break;
                case RIGHT:
                case BOTTOM:
                    iconY = drawing.y + offsetIY2;
                    centerY = false;
                    break;
                case CUSTOM:
                    iconY = drawing.y + drawing.height / 2f + offsetIY2;
                    centerY = true;
                    break;
                case CENTER:
                default:
                    iconY = drawing.y + drawing.height / 2f;
                    centerY = true;
            }
            game.getBatch().draw(icon2, centerX ? iconX - iconW2 / 2f : iconX, centerY ? iconY - iconH2 / 2f : iconY, iconW2, iconH2);
        }
    }
    /*public ImageButton copy(){
        ImageButton im = new ImageButton();
        return im;
    }*/
}
