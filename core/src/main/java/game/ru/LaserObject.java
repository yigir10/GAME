package game.ru;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class LaserObject extends GameObject {
    public boolean active = true;
    private Animation<TextureRegion> animation;
    private float stateTime;

    public LaserObject(int x, int y, int width, int height, Animation<TextureRegion> animation, World world) {
        // Инициализируем GameObject первым кадром анимации
        super(animation.getKeyFrame(0).getTexture(), x, y, width, height, GameSettings.LASER_BIT, world, true, 0.3f, 0.8f, GameSettings.JETPACK_BIT);
        this.animation = animation;
        this.stateTime = 0;

        body.setType(BodyDef.BodyType.KinematicBody);
    }

    public void update(float delta) {
        stateTime += delta;
        float x = body.getPosition().x - GameSettings.GAME_SPEED * delta * GameSettings.SCALE;
        body.setTransform(x, body.getPosition().y, 0);

        if (getX() < -width) {
            active = false;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        super.draw(batch, currentFrame);
    }
}
