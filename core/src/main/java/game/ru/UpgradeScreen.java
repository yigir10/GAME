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

        // Загрузка ресурсов через файловую систему Gdx.files
        this.background = new Texture(Gdx.files.internal(GameResources.MENU_BACKGROUND_PATH));
        this.menuButton = new Texture(Gdx.files.internal(GameResources.MAIN_MENU_BUTTON_PATH));
        this.upgradeButton = new Texture(Gdx.files.internal(GameResources.UPGRADE_BUTTON_PATH));
        this.coinIcon = new Texture(Gdx.files.internal(GameResources.COIN_ICON_PATH));

        float btnWidth = 350;
        float btnHeight = 100;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - btnWidth / 2f;

        this.jumpUpgradeBounds = new Rectangle(centerX - 60, 800, btnWidth, btnHeight);
        this.coinUpgradeBounds = new Rectangle(centerX - 60, 650, btnWidth, btnHeight);
        this.magnetUpgradeBounds = new Rectangle(centerX - 60, 500, btnWidth, btnHeight);
        this.shieldUpgradeBounds = new Rectangle(centerX - 60, 350, btnWidth, btnHeight);
        this.menuButtonBounds = new Rectangle(centerX, 100, btnWidth, btnHeight);

        this.font = FontBuilder.buildFont(1.8f, Color.WHITE);
        this.layout = new GlyphLayout();

        try {
            buySound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_COIN));
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(GameResources.MUSIC_MAIN));
            backgroundMusic.setLooping(true);
        } catch (Exception e) {
            Gdx.app.log("UpgradeScreen", "Audio resources missing");
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
        ScreenUtils.clear(Color.BLACK);
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.batch.draw(background, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

        FontBuilder.setScale(font, 2.5f);
        layout.setText(font, "МАГАЗИН");
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, GameSettings.SCREEN_HEIGHT - 30);

        // БАЛАНС (данные берутся из файла настроек)
        float iconSize = 45f;
        game.batch.draw(coinIcon, 30, GameSettings.SCREEN_HEIGHT - 120, iconSize, iconSize);
        FontBuilder.setScale(font, 1.8f);
        font.draw(game.batch, ": " + GameState.getTotalCoins(), 85, GameSettings.SCREEN_HEIGHT - 82);

        // Отрисовка элементов магазина с описанием улучшений
        drawUpgradeItem("ДЖЕТПАК", "УЛУЧШАЕТ: СИЛУ ВЗЛЕТА", GameState.getJumpLevel(), GameState.getUpgradeCost(GameState.getJumpLevel(), BASE_COST_JUMP), jumpUpgradeBounds);
        drawUpgradeItem("МОНЕТЫ", "УЛУЧШАЕТ: ЦЕНУ МОНЕТЫ", GameState.getCoinLevel(), GameState.getUpgradeCost(GameState.getCoinLevel(), BASE_COST_COIN), coinUpgradeBounds);
        drawUpgradeItem("МАГНИТ", "УЛУЧШАЕТ: РАДИУС СБОРА", GameState.getMagnetLevel(), GameState.getUpgradeCost(GameState.getMagnetLevel(), BASE_COST_MAGNET), magnetUpgradeBounds);
        drawUpgradeItem("ЭНЕРГОЩИТ", "УЛУЧШАЕТ: ЗАЩИТУ", GameState.getShieldLevel(), GameState.getUpgradeCost(GameState.getShieldLevel(), BASE_COST_SHIELD), shieldUpgradeBounds);

        // Инфо о сохранении
        FontBuilder.setScale(font, 0.7f);
        font.setColor(Color.LIGHT_GRAY);
        layout.setText(font, "ПРОГРЕСС СОХРАНЯЕТСЯ В GDX.FILES (PREFERENCES)");
        font.draw(game.batch, layout, GameSettings.SCREEN_WIDTH / 2f - layout.width / 2f, 250);
        font.setColor(Color.WHITE);

        game.batch.draw(menuButton, menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height);
        game.batch.end();

        if (Gdx.input.justTouched()) {
            game.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(game.touch);

            if (jumpUpgradeBounds.contains(game.touch.x, game.touch.y)) {
                buyUpgrade(0);
            } else if (coinUpgradeBounds.contains(game.touch.x, game.touch.y)) {
                buyUpgrade(1);
            } else if (magnetUpgradeBounds.contains(game.touch.x, game.touch.y)) {
                buyUpgrade(2);
            } else if (shieldUpgradeBounds.contains(game.touch.x, game.touch.y)) {
                buyUpgrade(3);
            } else if (menuButtonBounds.contains(game.touch.x, game.touch.y)) {
                game.setScreen(new MenuScreen(game));
            }
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
            // Обновление файлов через Preferences
            if (type == 0) GameState.upgradeJump();
            else if (type == 1) GameState.upgradeCoin();
            else if (type == 2) GameState.upgradeMagnet();
            else if (type == 3) GameState.upgradeShield();
        }
    }

    private void drawUpgradeItem(String title, String description, int level, int cost, Rectangle bounds) {
        game.batch.draw(upgradeButton, bounds.x, bounds.y, bounds.width, bounds.height);

        // Название улучшения
        FontBuilder.setScale(font, 1.2f);
        font.setColor(Color.WHITE);
        font.draw(game.batch, title, bounds.x + 20, bounds.y + bounds.height - 20);

        // Что именно улучшает (серый цвет)
        FontBuilder.setScale(font, 0.75f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(game.batch, description, bounds.x + 20, bounds.y + 35);

        // Уровень справа
        font.setColor(Color.CYAN);
        FontBuilder.setScale(font, 1.7f);
        font.draw(game.batch, "L" + level, bounds.x + bounds.width + 15, bounds.y + bounds.height - 5);

        // Цена
        font.setColor(Color.GOLD);
        game.batch.draw(coinIcon, bounds.x + bounds.width + 15, bounds.y + 10, 35, 35);
        font.draw(game.batch, "" + cost, bounds.x + bounds.width + 55, bounds.y + 38);

        font.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        background.dispose();
        menuButton.dispose();
        upgradeButton.dispose();
        coinIcon.dispose();
        font.dispose();
        if (buySound != null) buySound.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}
