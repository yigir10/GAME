package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class UpgradeScreen extends ScreenAdapter {
    private final MyGdxGame game;
    private final Texture background;
    private final Texture menuButton;
    private final Texture upgradeButton;
    private final Texture coinIcon;
    private final Rectangle menuButtonBounds;
    private final Rectangle jumpUpgradeBounds;
    private final Rectangle coinUpgradeBounds;
    private final Rectangle magnetUpgradeBounds;
    private final Rectangle shieldUpgradeBounds;
    private final BitmapFont font;
    private final GlyphLayout layout;

    private Sound buySound;
    private Music backgroundMusic;

    private final int BASE_COST_JUMP = 50;
    private final int BASE_COST_COIN = 100;
    private final int BASE_COST_MAGNET = 150;
    private final int BASE_COST_SHIELD = 200;

    public UpgradeScreen(MyGdxGame game) {
        this.game = game;
        this.background = new Texture(GameResources.MENU_BACKGROUND_PATH);
        this.menuButton = new Texture(GameResources.MAIN_MENU_BUTTON_PATH);
        this.upgradeButton = new Texture(GameResources.UPGRADE_BUTTON_PATH);
        this.coinIcon = new Texture(GameResources.COIN_ICON_PATH);

        float btnWidth = 280;
        float btnHeight = 100;

        // Кнопки сдвинуты еще правее (580)
        float buttonsX = 350;
        this.jumpUpgradeBounds = new Rectangle(buttonsX, 800, btnWidth, btnHeight);
        this.coinUpgradeBounds = new Rectangle(buttonsX, 650, btnWidth, btnHeight);
        this.magnetUpgradeBounds = new Rectangle(buttonsX, 500, btnWidth, btnHeight);
        this.shieldUpgradeBounds = new Rectangle(buttonsX, 350, btnWidth, btnHeight);
        this.menuButtonBounds = new Rectangle(GameSettings.SCREEN_WIDTH / 2f - 175, 100, 350, btnHeight);

        this.font = FontBuilder.buildFont(1.5f, Color.WHITE);
        this.layout = new GlyphLayout();

        try {
            buySound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_COIN));
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MUSIC_MAIN));
            backgroundMusic.setLooping(true);
            if (GameState.isMusicOn()) backgroundMusic.play();
        } catch (Exception e) {
            Gdx.app.log("UpgradeScreen", "Audio resources missing");
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

        FontBuilder.setScale(font, 2.5f);
        layout.setText(font, "SHOP");
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, GameSettings.SCREEN_HEIGHT - 40);

        // БАЛАНС (Top Left)
        float coinBaselineY = GameSettings.SCREEN_HEIGHT - 160;
        game.batch.draw(coinIcon, 30, coinBaselineY + 5, 50, 50);
        FontBuilder.setScale(font, 1.8f);
        font.draw(game.batch, ": " + GameState.getTotalCoins(), 90, coinBaselineY + 50);

        // Отрисовка элементов
        drawUpgradeItem("JETPACK", "FORCE", GameState.getJumpLevel(), GameState.getUpgradeCost(GameState.getJumpLevel(), BASE_COST_JUMP), jumpUpgradeBounds);
        drawUpgradeItem("COINS", "BONUS", GameState.getCoinLevel(), GameState.getUpgradeCost(GameState.getCoinLevel(), BASE_COST_COIN), coinUpgradeBounds);
        drawUpgradeItem("MAGNET", "RANGE", GameState.getMagnetLevel(), GameState.getUpgradeCost(GameState.getMagnetLevel(), BASE_COST_MAGNET), magnetUpgradeBounds);
        drawUpgradeItem("SHIELD", "LIVES", GameState.getShieldLevel(), GameState.getUpgradeCost(GameState.getShieldLevel(), BASE_COST_SHIELD), shieldUpgradeBounds);

        game.batch.draw(menuButton, menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);

            if (jumpUpgradeBounds.contains(game.touch.x, game.touch.y)) buyUpgrade(0);
            else if (coinUpgradeBounds.contains(game.touch.x, game.touch.y)) buyUpgrade(1);
            else if (magnetUpgradeBounds.contains(game.touch.x, game.touch.y)) buyUpgrade(2);
            else if (shieldUpgradeBounds.contains(game.touch.x, game.touch.y)) buyUpgrade(3);
            else if (menuButtonBounds.contains(game.touch.x, game.touch.y)) game.setScreen(new MenuScreen(game));
        }
    }

    private void buyUpgrade(int type) {
        int level = 0, cost = 0;
        if (type == 0) { level = GameState.getJumpLevel(); cost = GameState.getUpgradeCost(level, BASE_COST_JUMP); }
        else if (type == 1) { level = GameState.getCoinLevel(); cost = GameState.getUpgradeCost(level, BASE_COST_COIN); }
        else if (type == 2) { level = GameState.getMagnetLevel(); cost = GameState.getUpgradeCost(level, BASE_COST_MAGNET); }
        else if (type == 3) { level = GameState.getShieldLevel(); cost = GameState.getUpgradeCost(level, BASE_COST_SHIELD); }

        if (GameState.spendCoins(cost)) {
            if (GameState.isSoundOn() && buySound != null) buySound.play();
            if (type == 0) GameState.upgradeJump();
            else if (type == 1) GameState.upgradeCoin();
            else if (type == 2) GameState.upgradeMagnet();
            else if (type == 3) GameState.upgradeShield();
        }
    }

    private void drawUpgradeItem(String title, String desc, int level, int cost, Rectangle bounds) {
        game.batch.draw(upgradeButton, bounds.x, bounds.y, bounds.width, bounds.height);

        // Подписи СЛЕВА от кнопок
        font.setColor(Color.WHITE);
        FontBuilder.setScale(font, 1.7f);
        font.draw(game.batch, title, 10, bounds.y + 80);
        FontBuilder.setScale(font, 1f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(game.batch, desc, 10, bounds.y + 35);

        // Уровень и цена - Слева от кнопок (инфо блок)
        float infoX = bounds.x - 145;

        font.setColor(Color.CYAN);
        FontBuilder.setScale(font, 1.3f);
        font.draw(game.batch, "L" + level, infoX, bounds.y + 85);

        font.setColor(Color.GOLD);
        game.batch.draw(coinIcon, infoX, bounds.y + 15, 35, 35);
        font.draw(game.batch, "" + cost, infoX + 42, bounds.y + 45);

        font.setColor(Color.WHITE);
    }

    @Override
    public void hide() { if (backgroundMusic != null) backgroundMusic.stop(); }

    @Override
    public void dispose() {
        background.dispose(); menuButton.dispose(); upgradeButton.dispose(); coinIcon.dispose();
        font.dispose(); if (buySound != null) buySound.dispose(); if (backgroundMusic != null) backgroundMusic.dispose();
    }
}
