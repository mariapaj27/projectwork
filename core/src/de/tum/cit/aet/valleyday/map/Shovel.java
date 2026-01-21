package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Shovel is an item without a hitbox.
 */
public class Shovel implements Drawable {
    
    private final int x;
    private final int y;
    
    /**
     * Creates a shovel at the given position.
     * @param x The position of x.
     * @param y The position of y.
     */
    public Shovel(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public TextureRegion getCurrentAppearance() {
        //  texture of the shovel
        return SpriteSheet.BASIC_ITEMS.at(3, 7);
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
