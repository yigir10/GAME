package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final int score;
    private final Texture background;
    private final Texture retryButton, menuButton;
    private final Rectangle retryButtonBounds, menuButtonBounds;
    private final BitmapFont font;
    private final GlyphLayout layout;

    public GameOverScreen(MyGdxGame game, int score) {
        this.game = game;
        this.score = score;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.retryButton = new Texture(GameResources.RETRY_BUTTON_PATH);
        this.menuButton = new Texture(GameResources.MAIN_MENU_BUTTON_PATH);

        float btnWidth = 350;
        float btnHeight = 120;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - btnWidth / 2f;

        // Кнопки расположены друг под другом
        this.retryButtonBounds = new Rectangle(centerX, 550, btnWidth, btnHeight);
        this.menuButtonBounds = new Rectangle(centerX, 400, btnWidth, btnHeight);

        // Используем FontBuilder для борьбы с растягиванием
        this.font = FontBuilder.buildFont(1.0f, Color.WHITE);
        this.layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

        // Заголовок "GAME OVER"
        FontBuilder.setScale(font, 4.0f);
        layout.setText(font, "GAME OVER");
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, GameSettings.SCREEN_HEIGHT * 0.85f);

        // Счет
        FontBuilder.setScale(font, 2.5f);
        layout.setText(font, "SCORE: " + score);
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, GameSettings.SCREEN_HEIGHT * 0.7f);

        // Отрисовка кнопок
        game.batch.draw(retryButton, retryButtonBounds.x, retryButtonBounds.y, retryButtonBounds.width, retryButtonBounds.height);
        game.batch.draw(menuButton, menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);

            if (retryButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new GameScreen(game));
            } else if (menuButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new MenuScreen(game));
            }
        }
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        background.dispose();
        retryButton.dispose();
        menuButton.dispose();
        font.dispose();
    }
}
