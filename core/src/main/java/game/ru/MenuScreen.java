package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final Texture background;
    private final Texture playButton;
    private final Rectangle playButtonBounds;

    public MenuScreen(MyGdxGame game) {
        this.game = game;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.playButton = new Texture(GameResources.PLAY_BUTTON_PATH);

        float btnWidth = 300;
        float btnHeight = 150;
        this.playButtonBounds = new Rectangle(
            GameSettings.SCREEN_WIDTH / 2f - btnWidth / 2f,
            GameSettings.SCREEN_HEIGHT / 2f - btnHeight / 2f,
            btnWidth,
            btnHeight
        );
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
        game.batch.draw(playButton, playButtonBounds.x, playButtonBounds.y, playButtonBounds.width, playButtonBounds.height);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);

            if (playButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new GameScreen(game));
            } else if (game.touch.y < 100) { // Например, клик в самом низу для настроек
                game.setScreen(new SettingsScreen(game));
            }
        }
    }

    @Override
    public void dispose() {
        background.dispose();
        playButton.dispose();
    }
}
