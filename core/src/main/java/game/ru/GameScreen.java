package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private final MyGdxGame myGdxGame;
    private final World world;
    private final JetPackObject jetPackObject;
    private final MovingBackground movingBackground;
    private final ArrayList<LaserObject> lasers;
    private final ArrayList<CoinObject> coins;
    private final ArrayList<RocketObject> rockets;
    private final GameSession gameSession;
    private final ShapeRenderer shapeRenderer;

    private static class FloatingText {
        String text;
        float x, y, life = 1.0f;
        Color color = Color.YELLOW;
        FloatingText(String t, float x, float y) { this.text = t; this.x = x; this.y = y; }
        FloatingText(String t, float x, float y, Color c) { this.text = t; this.x = x; this.y = y; this.color = c; }
    }
    private final ArrayList<FloatingText> floatingTexts = new ArrayList<>();

    private float laserTimer = 0;
    private float coinTimer = 0;
    private float rocketTimer = 0;

    private float accumulator = 0;
    private float distanceTimer = 0;
    private float currentSpeed;
    private float flashTimer = 0;

    private boolean isPaused = false;
    private final Texture pauseOverlay, pauseBtnTex, resumeBtn, retryBtn, menuBtn, coinIcon, warningIcon;
    private final Rectangle resumeRect, retryRect, menuRect, pauseBtnRect;

    private final BitmapFont font;
    private final Animation<TextureRegion> laserAnimation;
    private final Animation<TextureRegion> coinAnimation;
    private final Animation<TextureRegion> rocketAnimation;
    private final ArrayList<Texture> textures;

    private Sound coinSound, hitSound, jumpSound, launchSound, warningSound;
    private Music backgroundMusic;

    public GameScreen(MyGdxGame myGdxGame) {
        this(myGdxGame, new GameSession());
    }

    public GameScreen(MyGdxGame myGdxGame, GameSession session) {
        this.myGdxGame = myGdxGame;
        this.gameSession = session;
        this.world = new World(new Vector2(0, GameSettings.GRAVITY), true);
        this.lasers = new ArrayList<>();
        this.coins = new ArrayList<>();
        this.rockets = new ArrayList<>();
        this.textures = new ArrayList<>();
        this.shapeRenderer = new ShapeRenderer();
        this.currentSpeed = GameSettings.GAME_SPEED;

        this.font = FontBuilder.buildFont(2.5f, Color.WHITE);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pauseOverlay = new Texture(pixmap);
        pixmap.dispose();

        pauseBtnTex = new Texture(GameResources.PAUSE_BUTTON_PATH);
        resumeBtn = new Texture(GameResources.RESUME_BUTTON_PATH);
        retryBtn = new Texture(GameResources.RETRY_BUTTON_PATH);
        menuBtn = new Texture(GameResources.MAIN_MENU_BUTTON_PATH);
        coinIcon = new Texture(GameResources.COIN_ICON_PATH);
        warningIcon = new Texture(GameResources.WARNING_ICON_PATH);

        textures.add(pauseOverlay);
        textures.add(pauseBtnTex);
        textures.add(resumeBtn);
        textures.add(retryBtn);
        textures.add(menuBtn);
        textures.add(coinIcon);
        textures.add(warningIcon);

        float bw = 350, bh = 120;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - bw / 2f;
        resumeRect = new Rectangle(centerX, 750, bw, bh);
        retryRect = new Rectangle(centerX, 600, bw, bh);
        menuRect = new Rectangle(centerX, 450, bw, bh);
        pauseBtnRect = new Rectangle(GameSettings.SCREEN_WIDTH - 120, GameSettings.SCREEN_HEIGHT - 120, 100, 100);

        laserAnimation = createAnimation(GameResources.LASER_ANIMATION_PATHS, 0.1f);
        coinAnimation = createAnimation(GameResources.COIN_ANIMATION_PATHS, 0.08f);
        rocketAnimation = createAnimation(GameResources.ROCKET_ANIMATION_PATHS, 0.07f);
        Animation<TextureRegion> jetPackAnimation = createAnimation(GameResources.JETPACK_ANIMATION_PATHS, 0.1f);

        jetPackObject = new JetPackObject(150, GameSettings.SCREEN_HEIGHT / 2, GameSettings.JETPACK_WIDTH, GameSettings.JETPACK_HEIGHT, jetPackAnimation, world, this);
        movingBackground = new MovingBackground(GameResources.BACKGROUND_IMG_PATH);
        new ContactManager(world);
        createBounds();

        coinSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_COIN));
        hitSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_HIT));
        jumpSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_JUMP));
        launchSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_ROCKET_LAUNCH));
        warningSound = Gdx.audio.newSound(Gdx.files.internal(GameResources.SOUND_ROCKET_WARNING));
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

    public void triggerFlash() {
        flashTimer = 0.15f;
        if (GameState.isSoundOn() && hitSound != null) hitSound.play();
    }

    private Animation<TextureRegion> createAnimation(String[] paths, float frameDuration) {
        Array<TextureRegion> frames = new Array<>();
        for (String path : paths) {
            Texture tex = new Texture(path);
            textures.add(tex);
            frames.add(new TextureRegion(tex));
        }
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    private void createBounds() {
        float scale = GameSettings.SCALE;
        float width = GameSettings.SCREEN_WIDTH * scale;
        float thickness = 100f * scale;
        float floorY = 120f * scale;
        float ceilingY = (GameSettings.SCREEN_HEIGHT - 180f) * scale;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        PolygonShape shape = new PolygonShape();
        FixtureDef fDef = new FixtureDef();
        fDef.shape = shape;
        fDef.filter.categoryBits = GameSettings.BOUNDS_BIT;
        fDef.filter.maskBits = GameSettings.JETPACK_BIT;

        Body floor = world.createBody(bodyDef);
        shape.setAsBox(width / 2f, thickness / 2f, new Vector2(width / 2f, floorY - thickness / 2f), 0);
        floor.createFixture(fDef).setUserData("ground");

        Body ceiling = world.createBody(bodyDef);
        shape.setAsBox(width / 2f, thickness / 2f, new Vector2(width / 2f, ceilingY + thickness / 2f), 0);
        ceiling.createFixture(fDef).setUserData("ground");
        shape.dispose();
    }

    @Override
    public void render(float delta) {
        handleInput();
        if (!isPaused && !gameSession.isGameOver) {
            update(delta);
        }
        draw();
    }

    private void update(float delta) {
        currentSpeed = GameSettings.GAME_SPEED + (gameSession.distance / 1000f) * 20f;
        movingBackground.update(delta, currentSpeed);
        jetPackObject.update(delta);

        if (flashTimer > 0) flashTimer -= delta;

        distanceTimer += delta;
        if (distanceTimer >= 0.1f) {
            gameSession.addDistance(1);
            distanceTimer = 0;

            if (gameSession.distance == GameState.getDistanceTarget()) {
                GameState.completeDistanceMission();
                floatingTexts.add(new FloatingText("MISSION COMPLETE: " + gameSession.distance + "m", GameSettings.SCREEN_WIDTH/2f - 200, GameSettings.SCREEN_HEIGHT/2f, Color.GREEN));
            }
        }

        laserTimer += delta;
        if (laserTimer >= GameSettings.LASER_SPAWN_INTERVAL) {
            lasers.add(new LaserObject(GameSettings.SCREEN_WIDTH + 100, MathUtils.random(250, 1000), GameSettings.LASER_WIDTH, GameSettings.LASER_HEIGHT, laserAnimation, world));
            laserTimer = 0;
        }

        coinTimer += delta;
        if (coinTimer >= GameSettings.COIN_SPAWN_INTERVAL) {
            coins.add(new CoinObject(GameSettings.SCREEN_WIDTH + 100, MathUtils.random(250, 1000), GameSettings.COIN_WIDTH, GameSettings.COIN_HEIGHT, coinAnimation, world));
            coinTimer = 0;
        }

        rocketTimer += delta;
        if (rocketTimer >= GameSettings.ROCKET_SPAWN_INTERVAL) {
            rockets.add(new RocketObject(jetPackObject.getY(), rocketAnimation, warningIcon, currentSpeed, world));
            if (GameState.isSoundOn() && warningSound != null) warningSound.play();
            rocketTimer = 0;
        }

        Iterator<LaserObject> laserIt = lasers.iterator();
        while (laserIt.hasNext()) {
            LaserObject laser = laserIt.next();
            laser.update(delta);
            float x = laser.body.getPosition().x - currentSpeed * delta * GameSettings.SCALE;
            laser.body.setTransform(x, laser.body.getPosition().y, 0);
            if (!laser.active || laser.getX() < -laser.width) {
                world.destroyBody(laser.body);
                laserIt.remove();
            }
        }

        Iterator<RocketObject> rocketIt = rockets.iterator();
        Vector2 playerPosMeters = new Vector2(jetPackObject.body.getPosition());
        while (rocketIt.hasNext()) {
            RocketObject rocket = rocketIt.next();
            boolean wasLaunched = rocket.isLaunched();
            rocket.update(delta, playerPosMeters);
            if (!wasLaunched && rocket.isLaunched()) {
                if (GameState.isSoundOn() && launchSound != null) launchSound.play();
            }
            if (!rocket.active || rocket.getX() < -rocket.width) {
                world.destroyBody(rocket.body);
                rocketIt.remove();
            }
        }

        Iterator<CoinObject> coinIt = coins.iterator();
        while (coinIt.hasNext()) {
            CoinObject coin = coinIt.next();
            coin.update(delta, playerPosMeters);
            if (!coin.active) {
                if (coin.getX() >= -coin.width) {
                    if (GameState.isSoundOn() && coinSound != null) coinSound.play();
                    int amount = 1 + GameState.getCoinLevel();
                    gameSession.addCoin(amount);
                    GameState.addCoins(amount);
                    floatingTexts.add(new FloatingText("+" + amount, jetPackObject.getX(), jetPackObject.getY() + 50));

                    if (gameSession.coins == GameState.getCoinsTarget()) {
                        GameState.completeCoinsMission();
                        floatingTexts.add(new FloatingText("MISSION COMPLETE: " + gameSession.coins + " coins", GameSettings.SCREEN_WIDTH/2f - 200, GameSettings.SCREEN_HEIGHT/2f + 100, Color.GREEN));
                    }
                }
                world.destroyBody(coin.body);
                coinIt.remove();
            }
        }

        Iterator<FloatingText> ftIt = floatingTexts.iterator();
        while (ftIt.hasNext()) {
            FloatingText ft = ftIt.next();
            ft.life -= delta;
            ft.y += 100 * delta;
            if (ft.life <= 0) ftIt.remove();
        }

        stepWorld(delta);

        if (jetPackObject.isDead && !gameSession.isGameOver) {
            gameSession.isGameOver = true;
            if (backgroundMusic != null) backgroundMusic.stop();
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    myGdxGame.setScreen(new GameOverScreen(myGdxGame, gameSession.distance, gameSession.coins));
                }
            }, 1.5f);
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            myGdxGame.touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            myGdxGame.camera.unproject(myGdxGame.touch);

            if (isPaused) {
                if (resumeRect.contains(myGdxGame.touch.x, myGdxGame.touch.y)) isPaused = false;
                if (retryRect.contains(myGdxGame.touch.x, myGdxGame.touch.y)) myGdxGame.setScreen(new GameScreen(myGdxGame));
                if (menuRect.contains(myGdxGame.touch.x, myGdxGame.touch.y)) myGdxGame.setScreen(new MenuScreen(myGdxGame));
            } else if (pauseBtnRect.contains(myGdxGame.touch.x, myGdxGame.touch.y)) {
                isPaused = true;
            } else if (!gameSession.isGameOver) {
                jetPackObject.fly();
                if (GameState.isSoundOn() && jumpSound != null) jumpSound.play(0.3f);
            }
        } else if (Gdx.input.isTouched() && !isPaused && !gameSession.isGameOver) {
            jetPackObject.fly();
        }
    }

    public void gameOver() {
        if (GameState.isSoundOn() && hitSound != null) hitSound.play();
        if (backgroundMusic != null) backgroundMusic.stop();
        gameSession.isGameOver = true;
        GameState.updateHighScore(gameSession.distance);
        myGdxGame.setScreen(new GameOverScreen(myGdxGame, gameSession.distance, gameSession.coins));
    }

    private void draw() {
        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);
        ScreenUtils.clear(Color.BLACK);
        myGdxGame.batch.begin();
        movingBackground.draw(myGdxGame.batch);
        for (LaserObject laser : lasers) laser.draw(myGdxGame.batch);
        for (CoinObject coin : coins) coin.draw(myGdxGame.batch);
        for (RocketObject rocket : rockets) rocket.draw(myGdxGame.batch);
        jetPackObject.draw(myGdxGame.batch);

        for (FloatingText ft : floatingTexts) {
            font.setColor(ft.color.r, ft.color.g, ft.color.b, ft.life);
            font.draw(myGdxGame.batch, ft.text, ft.x, ft.y);
        }

        font.setColor(Color.WHITE);
        font.draw(myGdxGame.batch, "Distance: " + gameSession.distance + "m", 30, GameSettings.SCREEN_HEIGHT - 50);

        float coinTextY = GameSettings.SCREEN_HEIGHT - 150;
        myGdxGame.batch.draw(coinIcon, 30, coinTextY - 62, 50, 50);
        font.draw(myGdxGame.batch, ": " + gameSession.coins, 95, coinTextY);

        myGdxGame.batch.draw(pauseBtnTex, pauseBtnRect.x, pauseBtnRect.y, pauseBtnRect.width, pauseBtnRect.height);

        if (flashTimer > 0) {
            myGdxGame.batch.setColor(1, 1, 1, flashTimer * 6f);
            myGdxGame.batch.draw(pauseOverlay, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
            myGdxGame.batch.setColor(Color.WHITE);
        }

        if (isPaused) {
            myGdxGame.batch.setColor(0, 0, 0, 0.5f);
            myGdxGame.batch.draw(pauseOverlay, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
            myGdxGame.batch.setColor(Color.WHITE);

            myGdxGame.batch.draw(resumeBtn, resumeRect.x, resumeRect.y, resumeRect.width, resumeRect.height);
            myGdxGame.batch.draw(retryBtn, retryRect.x, retryRect.y, retryRect.width, retryRect.height);
            myGdxGame.batch.draw(menuBtn, menuRect.x, menuRect.y, menuRect.width, menuRect.height);
        }

        myGdxGame.batch.end();
    }

    private void stepWorld(float delta) {
        accumulator += Math.min(delta, 0.25f);
        while (accumulator >= 1 / 60f) {
            world.step(1 / 60f, 6, 2);
            accumulator -= 1 / 60f;
        }
    }

    @Override
    public void dispose() {
        if (backgroundMusic != null) backgroundMusic.dispose();
        if (coinSound != null) coinSound.dispose();
        if (hitSound != null) hitSound.dispose();
        if (launchSound != null) launchSound.dispose();
        if (warningSound != null) warningSound.dispose();
        if (jumpSound != null) jumpSound.dispose();
        world.dispose();
        movingBackground.dispose();
        shapeRenderer.dispose();
        jetPackObject.dispose();
        for (Texture tex : textures) tex.dispose();
    }
}
