package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Wildlife visitor is a decorative object without a hitbox.
 */
public class WildlifeVisitor implements Drawable {
    
    private final int x;
    private final int y;
    
    /**
     * Create a wildlife visitor at the given position.
     * @param x The X position.
     * @param y The Y position.
     */
    public WildlifeVisitor(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public TextureRegion getCurrentAppearance() {
        // Using a basic tile texture - you may want to adjust this based on your sprite sheet
        return SpriteSheet.BASIC_TILES.at(4, 1);
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
