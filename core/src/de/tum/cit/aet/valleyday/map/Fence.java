package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * A fence is a static object with a hitbox.
 */
public class Fence implements Drawable {
    
    private final float x;
    private final float y;
    
    /**
     * Creates a fence at the given position.
     * @param world The Box2D world to add the fence's hitbox to.
     * @param x The position x.
     * @param y The position y.
     */
    public Fence(World world, float x, float y) {
        this.x = x;
        this.y = y;
        createHitbox(world);
    }
    
    /**
     * Create a Box2D body for the fence.
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
    
    @Override
    public TextureRegion getCurrentAppearance() {
        // texture of the fence
        return SpriteSheet.BASIC_TILES.at(1, 1);
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
