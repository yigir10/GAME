package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class CasinoScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final Texture background;
    private final Texture menuButton;
    private final Texture spinButton;
    private final Texture settingsButton;
    private final Texture coinIcon;
    private final Texture rocketSymbol;

    private final Rectangle menuButtonBounds;
    private final Rectangle spinButtonBounds;
    private final Rectangle settingsButtonBounds;

    private final BitmapFont font;
    private final GlyphLayout layout;

    private Sound spinSound, winSound;
    private Music backgroundMusic;
    private Animation<TextureRegion> coinAnimation;
    private float stateTime = 0;
    private final ArrayList<Texture> textures = new ArrayList<>();

    private final int[] currentSymbols = {0, 0, 0}; // 0: Coin, 1: Rocket, 2: Settings
    private String resultMessage = "LUCKY SPIN";
    private float spinTimer = 0;
    private boolean isSpinning = false;
    private final int SPIN_COST = 20;

    public CasinoScreen(MyGdxGame game) {
        this.game = game;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.menuButton = new Texture(GameResources.MAIN_MENU_BUTTON_PATH);
        this.spinButton = new Texture(GameResources.SPIN_BUTTON_PATH);
        this.settingsButton = new Texture(GameResources.SETTINGS_BUTTON_PATH);
        this.coinIcon = new Texture(GameResources.COIN_ICON_PATH);
        this.rocketSymbol = new Texture(GameResources.ROCKET_IMG_PATH);

        float btnWidth = 350;
        float btnHeight = 120;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - btnWidth / 2f;

        this.spinButtonBounds = new Rectangle(centerX, 500, btnWidth, btnHeight);
        this.menuButtonBounds = new Rectangle(centerX, 150, btnWidth, btnHeight);
        this.settingsButtonBounds = new Rectangle(GameSettings.SCREEN_WIDTH - 110, 20, 90, 90);

        this.font = FontBuilder.buildFont(2.0f, Color.WHITE);
        this.layout = new GlyphLayout();

        // Загрузка анимации для слотов
        Array<TextureRegion> frames = new Array<>();
        for (String path : GameResources.COIN_ANIMATION_PATHS) {
            Texture tex = new Texture(path);
            textures.add(tex);
            frames.add(new TextureRegion(tex));
        }
        coinAnimation = new Animation<>(0.05f, frames, Animation.PlayMode.LOOP);

        try {
            spinSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_JUMP));
            winSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_COIN));
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MUSIC_MAIN));
            backgroundMusic.setLooping(true);
        } catch (Exception e) {
            Gdx.app.log("CasinoScreen", "Audio missing");
        }
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
        stateTime += delta;
        if (isSpinning) {
            spinTimer -= delta;
            if (spinTimer <= 0) {
                isSpinning = false;
                applySpinResult();
            }
        }

        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

        // Header
        FontBuilder.setScale(font, 2.5f);
        layout.setText(font, "CASINO");
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, GameSettings.SCREEN_HEIGHT - 30);

        // БАЛАНС
        game.batch.draw(coinIcon, 30, GameSettings.SCREEN_HEIGHT - 145, 45, 45);
        FontBuilder.setScale(font, 1.8f);
        font.draw(game.batch, ": " + GameState.getTotalCoins(), 85, GameSettings.SCREEN_HEIGHT - 105);

        // Отрисовка слотов
        float slotSize = 130f;
        float startX = GameSettings.SCREEN_WIDTH / 2f - (slotSize * 1.5f) - 20;
        for (int i = 0; i < 3; i++) {
            float x = startX + i * (slotSize + 20);
            float y = 750;

            // Рамка слота
            game.batch.setColor(Color.DARK_GRAY);
            game.batch.draw(coinIcon, x - 5, y - 5, slotSize + 10, slotSize + 10);
            game.batch.setColor(Color.WHITE);

            if (isSpinning) {
                TextureRegion frame = coinAnimation.getKeyFrame(stateTime + i * 0.2f);
                game.batch.draw(frame, x, y, slotSize, slotSize);
            } else {
                Texture sym = getTextureForSymbol(currentSymbols[i]);
                game.batch.draw(sym, x, y, slotSize, slotSize);
            }
        }

        // Сообщение результата (вверху барабанов)
        FontBuilder.setScale(font, 2.2f);
        layout.setText(font, resultMessage);
        if (isSpinning) font.setColor(Color.GOLD);
        else if (resultMessage.contains("+")) font.setColor(Color.GREEN);
        else font.setColor(Color.WHITE);
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, 1000);
        font.setColor(Color.WHITE);

        // Кнопки
        if (isSpinning) game.batch.setColor(Color.GRAY);
        game.batch.draw(spinButton, spinButtonBounds.x, spinButtonBounds.y, spinButtonBounds.width, spinButtonBounds.height);
        game.batch.setColor(Color.WHITE);

        game.batch.draw(menuButton, menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height);
        game.batch.draw(settingsButton, settingsButtonBounds.x, settingsButtonBounds.y, settingsButtonBounds.width, settingsButtonBounds.height);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);

            if (!isSpinning && spinButtonBounds.contains(game.touch.x, game.touch.y)) {
                startSpin();
            } else if (menuButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new MenuScreen(game));
            } else if (settingsButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new SettingsScreen(game));
            }
        }
    }

    private Texture getTextureForSymbol(int symbol) {
        if (symbol == 1) return rocketSymbol;
        if (symbol == 2) return settingsButton;
        return coinIcon;
    }

    private void startSpin() {
        if (GameState.spendCoins(SPIN_COST)) {
            if (GameState.isSoundOn() && spinSound != null) spinSound.play(0.5f);
            isSpinning = true;
            spinTimer = 1.5f;
            resultMessage = "Spinning...";
        } else {
            resultMessage = "Not enough coins!";
        }
    }

    private void applySpinResult() {
        currentSymbols[0] = MathUtils.random(0, 2);
        currentSymbols[1] = MathUtils.random(0, 2);
        currentSymbols[2] = MathUtils.random(0, 2);

        int win = 0;
        if (currentSymbols[0] == currentSymbols[1] && currentSymbols[1] == currentSymbols[2]) {
            int type = currentSymbols[0];
            if (type == 0) win = 500;      // 3 монеты
            else if (type == 1) win = 300; // 3 ракеты
            else win = 200;                // 3 значка настроек
            resultMessage = "JACKPOT! +" + win;
        } else if (currentSymbols[0] == currentSymbols[1] || currentSymbols[1] == currentSymbols[2] || currentSymbols[0] == currentSymbols[2]) {
            win = 40; // 2 одинаковых
            resultMessage = "WIN! +" + win;
        } else {
            resultMessage = "Try again!";
        }

        if (win > 0) {
            GameState.addCoins(win);
            if (GameState.isSoundOn() && winSound != null) winSound.play();
        }
    }

    @Override
    public void dispose() {
        background.dispose();
        menuButton.dispose();
        spinButton.dispose();
        settingsButton.dispose();
        coinIcon.dispose();
        rocketSymbol.dispose();
        font.dispose();
        for (Texture tex : textures) tex.dispose();
        if (spinSound != null) spinSound.dispose();
        if (winSound != null) winSound.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}
