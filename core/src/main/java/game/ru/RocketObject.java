package game.ru;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class RocketObject extends GameObject {
    public boolean active = true;
    private float warningTimer;
    private boolean isLaunched = false;
    private final Texture warningIcon;
    private final Animation<TextureRegion> animation;
    private float stateTime;
    private float speed;

    public RocketObject(int yPixels, Animation<TextureRegion> animation, Texture warningIcon, float speed, World world) {
        super(animation.getKeyFrame(0).getTexture(), GameSettings.SCREEN_WIDTH + 100, yPixels, 100, 50, GameSettings.LASER_BIT, world, true, 0.8f, 0.5f, GameSettings.JETPACK_BIT);
        this.animation = animation;
        this.warningIcon = warningIcon;
        this.warningTimer = 2.0f;
        this.speed = speed * 2.5f;
        this.stateTime = 0;
        this.body.setType(BodyDef.BodyType.KinematicBody);
    }

    public void update(float delta, Vector2 playerPosMeters) {
        stateTime += delta;
        if (!isLaunched) {
            warningTimer -= delta;
            if (warningTimer <= 0) {
                isLaunched = true;
            }
            float targetY = playerPosMeters.y;
            float currentY = body.getPosition().y;
            float newY = currentY + (targetY - currentY) * delta * 3f;
            body.setTransform(body.getPosition().x, newY, 0);
        } else {
            float x = body.getPosition().x - speed * delta * GameSettings.SCALE;
            body.setTransform(x, body.getPosition().y, 0);

            if (getX() < -width) {
                active = false;
            }
        }
    }

    public boolean isLaunched() {
        return isLaunched;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!isLaunched) {
            float blink = 0.4f + 0.6f * (float) Math.abs(Math.sin(System.currentTimeMillis() / 150f));
            batch.setColor(1, 1, 1, blink);
            batch.draw(warningIcon, GameSettings.SCREEN_WIDTH - 120, getY() - 40, 80, 80);
            batch.setColor(1, 1, 1, 1);
        } else {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            super.draw(batch, currentFrame);
        }
    }
}
