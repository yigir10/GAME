package game.ru;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class CoinObject extends GameObject {
    public boolean active = true;
    private final Animation<TextureRegion> animation;
    private float stateTime;
    private float pulseTime = 0;
    private float currentScale = 1.0f;

    public CoinObject(int x, int y, int width, int height, Animation<TextureRegion> animation, World world) {
        super(animation.getKeyFrame(0).getTexture(), x, y, width, height, GameSettings.COIN_BIT, world, true, 0.8f, 0.8f, GameSettings.JETPACK_BIT);
        this.animation = animation;
        this.stateTime = 0;
        body.setType(BodyDef.BodyType.KinematicBody);
    }

    public void update(float delta, Vector2 playerPos) {
        stateTime += delta;
        Vector2 currentPos = body.getPosition();
        float targetX = currentPos.x - GameSettings.GAME_SPEED * delta * GameSettings.SCALE;
        float targetY = currentPos.y;

        int magnetLevel = GameState.getMagnetLevel();
        if (magnetLevel > 0) {
            float radius = (150f + magnetLevel * 50f) * GameSettings.SCALE;
            float dist = currentPos.dst(playerPos);
            if (dist < radius) {
                float pullSpeed = (GameSettings.GAME_SPEED * 1.5f) * GameSettings.SCALE;
                Vector2 direction = new Vector2(playerPos).sub(currentPos).nor();
                targetX = currentPos.x + direction.x * pullSpeed * delta;
                targetY = currentPos.y + direction.y * pullSpeed * delta;
                pulseTime += delta * 10f;
                currentScale = 1.0f + (float) Math.sin(pulseTime) * 0.2f;
            } else {
                currentScale = 1.0f;
            }
        }
        body.setTransform(targetX, targetY, 0);
        if (getX() < -width) {
            active = false;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        float angle = (float) Math.toDegrees(body.getAngle());
        batch.draw(currentFrame,
                   getX() - (width * currentScale / 2f), getY() - (height * currentScale / 2f),
                   (width * currentScale) / 2f, (height * currentScale) / 2f,
                   width * currentScale, height * currentScale,
                   1f, 1f,
                   angle);
    }

    @Override
    public void hit(GameObject other) {
        active = false;
    }
}
