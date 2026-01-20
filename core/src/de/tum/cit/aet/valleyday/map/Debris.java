package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Debris is a destructible static object with a hitbox.
 */
public class Debris implements Drawable {
    
    private final float x;
    private final float y;
    //added world state
    private Body body;
    private final World world;
    
    /**
     * Creates debris at a given position.
     * @param world The Box2D world to add the debris's hitbox to.
     * @param x The position of x.
     * @param y The position of y.
     */
    public Debris(World world, float x, float y) {
        this.x = x;
        this.y = y;
        this.world = world;
        createHitbox(world);
    }
    
    /**
     * Creates a Box2D body for the debris.
     * @param world The Box2D world to add the body to.
     */
    private void createHitbox(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(this.x, this.y);
        Body body = world.createBody(bodyDef);
        
        PolygonShape box = new PolygonShape();
        box.setAsBox(0.5f, 0.5f);
        body.createFixture(box, 1.0f);
        box.dispose();
        body.setUserData(this);
    }
    /**
     * Destroys debris from the game.
     */
    public void destroy() {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // the texture of the debris
        return SpriteSheet.OUTSIDE_TILES.at(3, 2);
    }
    
    @Override
    public float getX() {
        return x;
    }
    
    @Override
    public float getY() {
        return y;
    }
}
