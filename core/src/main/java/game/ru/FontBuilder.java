package game.ru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontBuilder {
    private static final float COMP_X = 1.0f;
    private static final float COMP_Y = 2.0f;

    public static BitmapFont buildFont(float baseScale, Color color) {
        BitmapFont font = new BitmapFont();
        setScale(font, baseScale);
        font.setColor(color);
        return font;
    }

    public static void setScale(BitmapFont font, float baseScale) {
        font.getData().setScale(baseScale * COMP_X, baseScale * COMP_Y);
    }
}
