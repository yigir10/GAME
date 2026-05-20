package game.ru;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MyGdxGame extends Game {
    public World world;
    public SpriteBatch batch;
    public Vector3 touch;
    public OrthographicCamera camera;
    float accumulator = 0;

    @Override
    public void create() {
        Box2D.init();
        world = new World(new Vector2(0, GameSettings.GRAVITY), true);
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
        touch = new Vector3();

        setScreen(new MenuScreen(this));
    }

    public void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;

        while (accumulator >= GameSettings.STEP_TIME) {
            accumulator -= GameSettings.STEP_TIME;
            world.step(GameSettings.STEP_TIME, GameSettings.VELOCITY_ITERATIONS, GameSettings.POSITION_ITERATIONS);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) batch.dispose();
        if (world != null) world.dispose();
    }
}
