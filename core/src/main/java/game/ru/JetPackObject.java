package game.ru;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

public class JetPackObject extends GameObject {
    private final Animation<TextureRegion> animation;
    private final GameScreen gameScreen;
    private float stateTime;

    public JetPackObject(int x, int y, int width, int height, Animation<TextureRegion> animation, World world, GameScreen gameScreen) {
        // Используем первый кадр для инициализации базовых свойств
        super(animation.getKeyFrame(0).getTexture(), x, y, width, height, GameSettings.JETPACK_BIT, world, false, 0.6f, 1.0f, (short)(GameSettings.BOUNDS_BIT | GameSettings.LASER_BIT));
        this.animation = animation;
        this.gameScreen = gameScreen;
        this.stateTime = 0;

        body.setGravityScale(1.0f);
        body.setLinearDamping(1f);
    }

    public void fly() {
        float impulse = body.getMass() * GameSettings.JUMP_FORCE;
        body.applyLinearImpulse(0, impulse, body.getWorldCenter().x, body.getWorldCenter().y, true);

        float maxVelocityY = 15f;
        if (body.getLinearVelocity().y > maxVelocityY) {
            body.setLinearVelocity(0, maxVelocityY);
        }
    }

    public void update(float delta) {
        stateTime += delta;
        body.setLinearVelocity(0, body.getLinearVelocity().y);
        body.setAngularVelocity(0);
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        super.draw(batch, currentFrame);
    }

    @Override
    public void hit() {
        gameScreen.gameOver();
    }
}
