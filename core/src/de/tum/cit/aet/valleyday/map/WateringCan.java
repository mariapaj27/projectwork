package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Watering Can model.
 */
public class WateringCan implements Drawable {

    private final float x;
    private final float y;
    private final World world;
    private Body body;

    /**
     * Create a watering can at the given position.
     *
     * @param world The Box2D world.
     * @param x The X position.
     * @param y The Y position.
     */
    public WateringCan(World world, float x, float y) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.body = createHitbox(world, x, y);
        if (this.body != null) {
            this.body.setUserData(this);
        }
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // water can texture
        return SpriteSheet.BASIC_ITEMS.at(2, 13);
    }

    /**
     * Removes from world when picked up.
     */
    public void destroy() {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    private static Body createHitbox(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.setAsBox(0.5f, 0.5f);
        body.createFixture(box, 1.0f);
        box.dispose();

        return body;
    }
}
