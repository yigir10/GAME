package game.ru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontBuilder {
    // Коэффициенты компенсации для типичного растяжения (сужаем по X и вытягиваем по Y)
    // Увеличиваем Y относительно X, чтобы убрать эффект растянутости по горизонтали на мониторах
    private static final float COMP_X = 1.0f;
    private static final float COMP_Y = 2.2f;

    public static BitmapFont buildFont(float baseScale, Color color) {
        BitmapFont font = new BitmapFont();
        setScale(font, baseScale);
        font.setColor(color);
        return font;
    }

    public static void setScale(BitmapFont font, float baseScale) {
        font.getData().setScale(baseScale * COMP_X, baseScale * COMP_Y);
    }

    public static void setScale(BitmapFont font, float scaleX, float scaleY) {
        font.getData().setScale(scaleX * COMP_X, scaleY * COMP_Y);
    }
}
