package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final Texture background;
    private final Texture playButton, upgradeButton, casinoButton, settingsButton, quitButton;
    private final Texture coinIcon;
    private final BitmapFont font;

    private final Rectangle playButtonBounds;
    private final Rectangle upgradeButtonBounds;
    private final Rectangle casinoButtonBounds;
    private final Rectangle settingsButtonBounds;
    private final Rectangle quitButtonBounds;

    private Music menuMusic;

    public MenuScreen(MyGdxGame game) {
        this.game = game;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.playButton = new Texture(GameResources.PLAY_BUTTON_PATH);
        this.upgradeButton = new Texture(GameResources.UPGRADE_BUTTON_PATH);
        this.casinoButton = new Texture(GameResources.CASINO_BUTTON_PATH);
        this.settingsButton = new Texture(GameResources.SETTINGS_BUTTON_PATH);
        this.quitButton = new Texture(GameResources.QUIT_BUTTON_PATH);
        this.coinIcon = new Texture(GameResources.COIN_ICON_PATH);
        this.font = FontBuilder.buildFont(2.5f, Color.WHITE);

        float btnWidth = 300;
        float btnHeight = 120;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - btnWidth / 2f;

        // Расположение кнопок по вертикали
        this.playButtonBounds = new Rectangle(centerX, 750, btnWidth, btnHeight);
        this.upgradeButtonBounds = new Rectangle(centerX, 600, btnWidth, btnHeight);
        this.casinoButtonBounds = new Rectangle(centerX, 450, btnWidth, btnHeight);
        this.quitButtonBounds = new Rectangle(centerX, 300, btnWidth, btnHeight);

        // Кнопка настроек в углу
        this.settingsButtonBounds = new Rectangle(GameSettings.SCREEN_WIDTH - 110, 20, 90, 90);

        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MUSIC_MAIN));
            menuMusic.setLooping(true);
            if (GameState.isMusicOn()) menuMusic.play();
        } catch (Exception e) {
            Gdx.app.log("MenuScreen", "Music missing");
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

        // Отрисовка кнопок
        game.batch.draw(playButton, playButtonBounds.x, playButtonBounds.y, playButtonBounds.width, playButtonBounds.height);
        game.batch.draw(upgradeButton, upgradeButtonBounds.x, upgradeButtonBounds.y, upgradeButtonBounds.width, upgradeButtonBounds.height);
        game.batch.draw(casinoButton, casinoButtonBounds.x, casinoButtonBounds.y, casinoButtonBounds.width, casinoButtonBounds.height);
        game.batch.draw(quitButton, quitButtonBounds.x, quitButtonBounds.y, quitButtonBounds.width, quitButtonBounds.height);
        game.batch.draw(settingsButton, settingsButtonBounds.x, settingsButtonBounds.y, settingsButtonBounds.width, settingsButtonBounds.height);

        // --- ИСПРАВЛЕНИЕ ЛЕВОГО ВЕРХНЕГО УГЛА ---

        // 1. Рекорд (верхняя строка)
        font.setColor(Color.GOLD);
        font.draw(game.batch, "BEST: " + GameState.getHighScore() + "m", 30, GameSettings.SCREEN_HEIGHT - 40);

        // 2. Монеты (вторая строка, опускаем ниже чтобы не было наложения)
        float coinY = GameSettings.SCREEN_HEIGHT - 160;
        // Иконка (55x55). Выравниваем по центру текста: Y_текста - Высота_текста/2 - Высота_иконки/2
        // С учетом вертикального растяжения Cap Height шрифта (75px), опускаем иконку на 65px от верха текста
        game.batch.draw(coinIcon, 30, coinY - 65, 55, 55);
        font.setColor(Color.WHITE);
        font.draw(game.batch, ": " + GameState.getTotalCoins(), 95, coinY);

        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);

            if (playButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new GameScreen(game));
            } else if (upgradeButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new UpgradeScreen(game));
            } else if (casinoButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new CasinoScreen(game));
            } else if (quitButtonBounds.contains(game.touch.x, game.touch.y)) {
                Gdx.app.exit();
            } else if (settingsButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new SettingsScreen(game));
            }
        }
    }

    @Override
    public void show() {
        if (GameState.isMusicOn() && menuMusic != null && !menuMusic.isPlaying()) menuMusic.play();
    }

    @Override
    public void hide() {
        if (menuMusic != null) menuMusic.stop();
    }

    @Override
    public void dispose() {
        background.dispose();
        playButton.dispose();
        upgradeButton.dispose();
        casinoButton.dispose();
        settingsButton.dispose();
        quitButton.dispose();
        coinIcon.dispose();
        font.dispose();
        if (menuMusic != null) menuMusic.dispose();
    }
}
