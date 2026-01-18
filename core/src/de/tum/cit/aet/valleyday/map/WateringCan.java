package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Watering Can is an item without a hitbox.
 */
public class WateringCan implements Drawable {
    
    private final int x;
    private final int y;
    
    /**
     * Create a watering can at the given position.
     * @param x The position of x.
     * @param y The position of y.
     */
    public WateringCan(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public TextureRegion getCurrentAppearance() {
     // texture of the watering can
        return SpriteSheet.BASIC_TILES.at(6, 1);
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
