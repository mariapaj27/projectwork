package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.Textures;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Grass is a static object.
 */
public class Grass implements Drawable {
    
    private final int x;
    private final int y;
    
    public Grass(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public TextureRegion getCurrentAppearance() {
        // texture of the grass
        return SpriteSheet.BASIC_TILES.at(9, 1);
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
