package game.ru;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class JetPackObject extends GameObject {
    private final Animation<TextureRegion> animation;
    private final GameScreen gameScreen;
    private float stateTime;
    public int shieldCharges = 0;
    private final ShapeRenderer shapeRenderer;
    private boolean isFlying = false;

    public JetPackObject(int x, int y, int width, int height, Animation<TextureRegion> animation, World world, GameScreen gameScreen) {
        super(animation.getKeyFrame(0).getTexture(), x, y, width, height, GameSettings.JETPACK_BIT, world, false, 0.6f, 1.0f, (short)(GameSettings.BOUNDS_BIT | GameSettings.LASER_BIT | GameSettings.COIN_BIT));
        this.animation = animation;
        this.gameScreen = gameScreen;
        this.stateTime = 0;
        this.shapeRenderer = new ShapeRenderer();

        body.setGravityScale(1.0f);
        body.setLinearDamping(1f);
        this.shieldCharges = GameState.getShieldLevel();
    }

    public void fly() {
        isFlying = true;
        float bonus = GameState.getJumpLevel() * 0.15f;
        float impulse = body.getMass() * (GameSettings.JUMP_FORCE + bonus);
        body.applyLinearImpulse(0, impulse, body.getWorldCenter().x, body.getWorldCenter().y, true);

        float maxVelocityY = 15f + (GameState.getJumpLevel() * 0.5f);
        if (body.getLinearVelocity().y > maxVelocityY) {
            body.setLinearVelocity(0, maxVelocityY);
        }
    }

    public void update(float delta) {
        stateTime += delta;
        body.setLinearVelocity(0, body.getLinearVelocity().y);
        body.setAngularVelocity(0);
        isFlying = false;
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        super.draw(batch, currentFrame);

        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        Gdx.gl.glEnable(Gdx.gl20.GL_BLEND);
        // 1. Отрисовка щита
        if (shieldCharges > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.CYAN);
            Gdx.gl.glLineWidth(3);
            shapeRenderer.circle(getX(), getY(), height / 2f + 10);
            shapeRenderer.end();
        }

        // 2. Радиус магнита
        int magnetLevel = GameState.getMagnetLevel();
        if (magnetLevel > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 0.9f, 0, 0.05f);
            shapeRenderer.circle(getX(), getY(), (150f + magnetLevel * 50f));
            shapeRenderer.end();
        }
        Gdx.gl.glDisable(Gdx.gl20.GL_BLEND);

        batch.begin();
    }

    @Override
    public void hit(GameObject other) {
        if (other instanceof LaserObject || other instanceof RocketObject) {
            boolean isActive = (other instanceof LaserObject) ? ((LaserObject)other).active : ((RocketObject)other).active;
            if (isActive) {
                if (shieldCharges > 0) {
                    shieldCharges--;
                    if (other instanceof LaserObject) ((LaserObject)other).active = false;
                    else ((RocketObject)other).active = false;
                    gameScreen.triggerFlash(); // Эффект вспышки в GameScreen
                } else {
                    gameScreen.gameOver();
                }
            }
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
