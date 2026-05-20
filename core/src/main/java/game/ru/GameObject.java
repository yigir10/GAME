package game.ru;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class GameObject {
    Texture texture;
    public Body body;
    int width, height;
    public short cBits;

    GameObject(String texturePath, int x, int y, int width, int height, short cBits, World world) {
        this(new Texture(texturePath), x, y, width, height, cBits, world, false, 1f, 1f, (short) -1);
    }

    GameObject(Texture texture, int x, int y, int width, int height, short cBits, World world, boolean isSensor, float scaleX, float scaleY, short maskBits) {
        this.width = width;
        this.height = height;
        this.cBits = cBits;
        this.texture = texture;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.fixedRotation = true;
        def.position.set(x * GameSettings.SCALE, y * GameSettings.SCALE);
        body = world.createBody(def);

        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox((width * scaleX * GameSettings.SCALE) / 2f, (height * scaleY * GameSettings.SCALE) / 2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = boxShape;
        fixtureDef.isSensor = isSensor;
        fixtureDef.filter.categoryBits = cBits;
        fixtureDef.filter.maskBits = maskBits;

        body.createFixture(fixtureDef).setUserData(this);
        boxShape.dispose();
    }

    public void draw(SpriteBatch batch) {
        draw(batch, new TextureRegion(texture));
    }

    protected void draw(SpriteBatch batch, TextureRegion region) {
        float angle = (float) Math.toDegrees(body.getAngle());
        batch.draw(region,
                   getX() - (width / 2f), getY() - (height / 2f),
                   width / 2f, height / 2f,
                   width, height,
                   1f, 1f,
                   angle);
    }

    public void hit() {}

    public int getX() { return (int) (body.getPosition().x / GameSettings.SCALE); }
    public int getY() { return (int) (body.getPosition().y / GameSettings.SCALE); }
    public void setY(int y) { body.setTransform(body.getPosition().x, y * GameSettings.SCALE, body.getAngle()); }
}
