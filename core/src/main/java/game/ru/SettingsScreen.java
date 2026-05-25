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

public class SettingsScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final Texture background;
    private final Texture menuButton;
    private final Texture resetButton;
    private final Texture speakerOn;
    private final Texture speakerOff;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private final Rectangle menuButtonBounds;
    private final Rectangle resetProgressBounds;
    private final Rectangle soundToggleBounds;
    private final Rectangle musicToggleBounds;

    private String statusMessage = "SETTINGS";
    private Music settingsMusic;

    public SettingsScreen(MyGdxGame game) {
        this.game = game;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.menuButton = new Texture(GameResources.MAIN_MENU_BUTTON_PATH);
        this.resetButton = new Texture(GameResources.RETRY_BUTTON_PATH);
        this.speakerOn = new Texture(GameResources.SPEAKER_ON);
        this.speakerOff = new Texture(GameResources.SPEAKER_OFF);
        this.font = FontBuilder.buildFont(2.0f, Color.WHITE);
        this.layout = new GlyphLayout();

        float btnWidth = 350;
        float btnHeight = 110;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - btnWidth / 2f;
        float toggleSize = 100;

        // Элементы распределены по вертикали с запасом под текст
        this.soundToggleBounds = new Rectangle(100, 950, 500, toggleSize);
        this.musicToggleBounds = new Rectangle(100, 800, 500, toggleSize);
        this.resetProgressBounds = new Rectangle(centerX, 400, btnWidth, btnHeight);
        this.menuButtonBounds = new Rectangle(centerX, 150, btnWidth, btnHeight);

        try {
            settingsMusic = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MUSIC_MAIN));
            settingsMusic.setLooping(true);
        } catch (Exception e) {
            Gdx.app.log("SettingsScreen", "Music missing");
        }
    }

    @Override
    public void show() {
        if (GameState.isMusicOn() && settingsMusic != null) settingsMusic.play();
    }

    @Override
    public void hide() {
        if (settingsMusic != null) settingsMusic.stop();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

        // Заголовок (Крупный)
        FontBuilder.setScale(font, 2.5f);
        layout.setText(font, statusMessage);
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, GameSettings.SCREEN_HEIGHT - 60);

        // Переключатели (Средний шрифт)
        FontBuilder.setScale(font, 1.8f);
        Texture soundTex = GameState.isSoundOn() ? speakerOn : speakerOff;
        game.batch.draw(soundTex, soundToggleBounds.x, soundToggleBounds.y, 100, 100);
        font.draw(game.batch, "SOUND: " + (GameState.isSoundOn() ? "ON" : "OFF"), soundToggleBounds.x + 160, soundToggleBounds.y + 70);

        Texture musicTex = GameState.isMusicOn() ? speakerOn : speakerOff;
        game.batch.draw(musicTex, musicToggleBounds.x, musicToggleBounds.y, 100, 100);
        font.draw(game.batch, "MUSIC: " + (GameState.isMusicOn() ? "ON" : "OFF"), musicToggleBounds.x + 160, musicToggleBounds.y + 70);

        // Reset Section (Надпись выше кнопки)
        layout.setText(font, "RESET PROGRESS:");
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, resetProgressBounds.y + resetProgressBounds.height + 80);
        game.batch.draw(resetButton, resetProgressBounds.x, resetProgressBounds.y, resetProgressBounds.width, resetProgressBounds.height);

        // Back Button
        game.batch.draw(menuButton, menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height);

        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);

            if (soundToggleBounds.contains(game.touch.x, game.touch.y)) {
                GameState.setSoundOn(!GameState.isSoundOn());
            } else if (musicToggleBounds.contains(game.touch.x, game.touch.y)) {
                boolean nextVal = !GameState.isMusicOn();
                GameState.setMusicOn(nextVal);
                if (settingsMusic != null) {
                    if (nextVal) settingsMusic.play();
                    else settingsMusic.stop();
                }
            } else if (resetProgressBounds.contains(game.touch.x, game.touch.y)) {
                GameState.resetProgress();
                statusMessage = "PROGRESS RESET!";
            } else if (menuButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new MenuScreen(game));
            }
        }
    }

    @Override
    public void dispose() {
        background.dispose();
        menuButton.dispose();
        resetButton.dispose();
        speakerOn.dispose();
        speakerOff.dispose();
        font.dispose();
        if (settingsMusic != null) settingsMusic.dispose();
    }
}
