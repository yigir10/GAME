package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class SettingsScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final Texture background;

    public SettingsScreen(MyGdxGame game) {
        this.game = game;
        this.background = new Texture(GameResources.BACKGROUND_IMG_PATH);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.DARK_GRAY);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
        // Здесь можно добавить элементы управления настройками
        game.batch.end();

        if (Gdx.input.justTouched()) {
            // Возврат в меню по клику
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void dispose() {
        background.dispose();
    }
}
