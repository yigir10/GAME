package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final int distance;
    private final int coins;
    private final Texture background;
    private final Texture retryButton, menuButton, reviveButton, coinIcon;
    private final Rectangle retryButtonBounds, menuButtonBounds, reviveButtonBounds;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private Music backgroundMusic;
    private final int REVIVE_COST = 100;
    private boolean canRevive;

    public GameOverScreen(MyGdxGame game, int distance, int coins) {
        this.game = game;
        this.distance = distance;
        this.coins = coins;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.retryButton = new Texture(GameResources.RETRY_BUTTON_PATH);
        this.menuButton = new Texture(GameResources.MAIN_MENU_BUTTON_PATH);
        this.reviveButton = new Texture(GameResources.RESUME_BUTTON_PATH);
        this.coinIcon = new Texture(GameResources.COIN_ICON_PATH);
        this.canRevive = GameState.getTotalCoins() >= REVIVE_COST;
        float btnWidth = 350;
        float btnHeight = 110;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - btnWidth / 2f;
        this.reviveButtonBounds = new Rectangle(centerX, 600, btnWidth, btnHeight);
        this.retryButtonBounds = new Rectangle(centerX, 450, btnWidth, btnHeight);
        this.menuButtonBounds = new Rectangle(centerX, 300, btnWidth, btnHeight);
        this.font = FontBuilder.buildFont(1.5f, Color.WHITE);
        this.layout = new GlyphLayout();
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MUSIC_MAIN));
        backgroundMusic.setLooping(true);
    }

    @Override
    public void show() {
        if (GameState.isMusicOn() && backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    @Override
    public void hide() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
        FontBuilder.setScale(font, 4.0f);
        layout.setText(font, "GAME OVER");
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, GameSettings.SCREEN_HEIGHT * 0.92f);
        FontBuilder.setScale(font, 2.0f);
        font.draw(game.batch, "Dist: " + distance + "m", 50, GameSettings.SCREEN_HEIGHT - 150);
        game.batch.draw(coinIcon, 50, GameSettings.SCREEN_HEIGHT - 240, 40, 40);
        font.draw(game.batch, ": " + coins, 100, GameSettings.SCREEN_HEIGHT - 200);
        if (canRevive) {
            game.batch.draw(reviveButton, reviveButtonBounds.x, reviveButtonBounds.y, reviveButtonBounds.width, reviveButtonBounds.height);
        } else {
            game.batch.setColor(1, 1, 1, 0.5f);
            game.batch.draw(reviveButton, reviveButtonBounds.x, reviveButtonBounds.y, reviveButtonBounds.width, reviveButtonBounds.height);
            game.batch.setColor(Color.WHITE);
        }
        game.batch.draw(retryButton, retryButtonBounds.x, retryButtonBounds.y, retryButtonBounds.width, retryButtonBounds.height);
        game.batch.draw(menuButton, menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height);
        game.batch.end();
        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);
            if (canRevive && reviveButtonBounds.contains(game.touch.x, game.touch.y)) {
                GameState.spendCoins(REVIVE_COST);
                GameSession revivedSession = new GameSession();
                revivedSession.distance = distance;
                revivedSession.coins = coins;
                game.setScreen(new GameScreen(game, revivedSession));
            } else if (retryButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new GameScreen(game));
            } else if (menuButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new MenuScreen(game));
            }
        }
    }

    @Override
    public void dispose() {
        background.dispose();
        retryButton.dispose();
        menuButton.dispose();
        reviveButton.dispose();
        coinIcon.dispose();
        font.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}
