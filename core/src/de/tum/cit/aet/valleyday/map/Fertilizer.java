package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Fertilizer Model.
 */
public class Fertilizer implements Drawable {

    private final float x;
    private final float y;
    private final World world;
    private Body body;

    /**
     * Create fertilizer at the given position.
     * @param world The Box2D world.
     * @param x The X position.
     * @param y The Y position.
     */
    public Fertilizer(World world, float x, float y) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.body = createHitbox(world, x, y);
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        return SpriteSheet.BASIC_TILES.at(4, 4);
    }

    /**
     * Destroys fertilizer body.
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
