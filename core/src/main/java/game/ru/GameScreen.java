package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen extends ScreenAdapter {
    private final MyGdxGame myGdxGame;
    private final World world;
    private final JetPackObject jetPackObject;
    private final MovingBackground movingBackground;
    private final ArrayList<LaserObject> lasers;
    private final GameSession gameSession;

    private float laserTimer = 0;
    private final float laserSpawnInterval = 2.0f;
    private float accumulator = 0;
    private float scoreTimer = 0;

    private boolean isPaused = false;
    private final Texture pauseOverlay, pauseBtnTex, resumeBtn, retryBtn, menuBtn;
    private final Rectangle resumeRect, retryRect, menuRect, pauseBtnRect;

    private final BitmapFont font;
    private final Animation<TextureRegion> laserAnimation;
    private final ArrayList<Texture> textures;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        this.world = new World(new Vector2(0, GameSettings.GRAVITY), true);
        this.lasers = new ArrayList<>();
        this.textures = new ArrayList<>();
        this.gameSession = new GameSession();
        this.font = new BitmapFont();
        this.font.getData().setScale(2.5f);

        // Создаем overlay программно, так как файл отсутствует
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pauseOverlay = new Texture(pixmap);
        pixmap.dispose();

        pauseBtnTex = new Texture(GameResources.PAUSE_BUTTON_PATH);
        resumeBtn = new Texture(GameResources.RESUME_BUTTON_PATH);
        retryBtn = new Texture(GameResources.RETRY_BUTTON_PATH);
        menuBtn = new Texture(GameResources.MAIN_MENU_BUTTON_PATH);

        textures.add(pauseOverlay);
        textures.add(pauseBtnTex);
        textures.add(resumeBtn);
        textures.add(retryBtn);
        textures.add(menuBtn);

        float bw = 350, bh = 120;
        float centerX = GameSettings.SCREEN_WIDTH / 2f - bw / 2f;
        resumeRect = new Rectangle(centerX, 750, bw, bh);
        retryRect = new Rectangle(centerX, 600, bw, bh);
        menuRect = new Rectangle(centerX, 450, bw, bh);
        pauseBtnRect = new Rectangle(GameSettings.SCREEN_WIDTH - 120, GameSettings.SCREEN_HEIGHT - 120, 100, 100);

        // Анимации
        laserAnimation = createAnimation(GameResources.LASER_ANIMATION_PATHS, 0.1f);
        Animation<TextureRegion> jetPackAnimation = createAnimation(GameResources.JETPACK_ANIMATION_PATHS, 0.1f);

        jetPackObject = new JetPackObject(150, GameSettings.SCREEN_HEIGHT / 2, GameSettings.JETPACK_WIDTH, GameSettings.JETPACK_HEIGHT, jetPackAnimation, world, this);
        movingBackground = new MovingBackground(GameResources.BACKGROUND_IMG_PATH);
        new ContactManager(world);
        createBounds();
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
        movingBackground.update(delta);
        jetPackObject.update(delta);

        scoreTimer += delta;
        if (scoreTimer >= 0.5f) {
            gameSession.addScore(1);
            scoreTimer = 0;
        }

        laserTimer += delta;
        if (laserTimer >= laserSpawnInterval) {
            lasers.add(new LaserObject(GameSettings.SCREEN_WIDTH + 100, MathUtils.random(250, 1000), GameSettings.LASER_WIDTH, GameSettings.LASER_HEIGHT, laserAnimation, world));
            laserTimer = 0;
        }

        Iterator<LaserObject> it = lasers.iterator();
        while (it.hasNext()) {
            LaserObject laser = it.next();
            laser.update(delta);
            if (!laser.active) {
                world.destroyBody(laser.body);
                it.remove();
            }
        }
        stepWorld(delta);
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
            }
        } else if (Gdx.input.isTouched() && !isPaused && !gameSession.isGameOver) {
            jetPackObject.fly();
        }
    }

    public void gameOver() {
        gameSession.isGameOver = true;
        myGdxGame.setScreen(new GameOverScreen(myGdxGame, gameSession.score));
    }

    private void draw() {
        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);
        ScreenUtils.clear(Color.BLACK);
        myGdxGame.batch.begin();
        movingBackground.draw(myGdxGame.batch);
        for (LaserObject laser : lasers) laser.draw(myGdxGame.batch);
        jetPackObject.draw(myGdxGame.batch);

        font.draw(myGdxGame.batch, "Score: " + gameSession.score, 30, GameSettings.SCREEN_HEIGHT - 30);

        if (!isPaused) {
            myGdxGame.batch.draw(pauseBtnTex, pauseBtnRect.x, pauseBtnRect.y, pauseBtnRect.width, pauseBtnRect.height);
        }

        if (isPaused) {
            myGdxGame.batch.setColor(0, 0, 0, 0.7f); // Сделаем затемнение черным
            myGdxGame.batch.draw(pauseOverlay, 0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
            myGdxGame.batch.setColor(Color.WHITE);
            myGdxGame.batch.draw(resumeBtn, resumeRect.x, resumeRect.y, resumeRect.width, resumeRect.height);
            myGdxGame.batch.draw(retryBtn, retryRect.x, retryRect.y, retryRect.width, retryRect.height);
            myGdxGame.batch.draw(menuBtn, menuRect.x, menuRect.y, menuRect.width, menuRect.height);
        }
        myGdxGame.batch.end();
    }

    private void stepWorld(float delta) {
        accumulator += delta;
        while (accumulator >= GameSettings.STEP_TIME) {
            accumulator -= GameSettings.STEP_TIME;
            world.step(GameSettings.STEP_TIME, GameSettings.VELOCITY_ITERATIONS, GameSettings.POSITION_ITERATIONS);
        }
    }

    @Override
    public void dispose() {
        for (Texture t : textures) t.dispose();
        font.dispose();
        world.dispose();
        movingBackground.dispose();
    }
}
