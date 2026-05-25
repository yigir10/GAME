package game.ru;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;


public class ContactManager {

    public ContactManager(World world) {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                // Получаем объекты, которые столкнулись
                Object dataA = fixA.getUserData();
                Object dataB = fixB.getUserData();

                if (dataA instanceof GameObject && dataB instanceof GameObject) {
                    GameObject objA = (GameObject) dataA;
                    GameObject objB = (GameObject) dataB;

                    // Вызываем метод hit у обоих объектов, передавая соседа
                    objA.hit(objB);
                    objB.hit(objA);
                }
            }

            @Override
            public void endContact(Contact contact) {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }
}
