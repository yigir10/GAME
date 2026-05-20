package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final int score;
    private final Texture background;
    private final BitmapFont font;

    public GameOverScreen(MyGdxGame game, int score) {
        this.game = game;
        this.score = score;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.font = new BitmapFont();
        this.font.getData().setScale(3);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
        font.draw(game.batch, "GAME OVER", GameSettings.SCREEN_WIDTH / 2f - 150, GameSettings.SCREEN_HEIGHT * 0.7f);
        font.draw(game.batch, "SCORE: " + score, GameSettings.SCREEN_WIDTH / 2f - 100, GameSettings.SCREEN_HEIGHT * 0.5f);
        font.draw(game.batch, "TAP TO RESTART", GameSettings.SCREEN_WIDTH / 2f - 180, GameSettings.SCREEN_HEIGHT * 0.3f);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void dispose() {
        background.dispose();
        font.dispose();
    }
}
