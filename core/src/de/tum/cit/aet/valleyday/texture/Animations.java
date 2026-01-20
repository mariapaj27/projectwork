package de.tum.cit.aet.valleyday.texture;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Contains all animation constants used in the game.
 * It is good practice to keep all textures and animations in constants to avoid loading them multiple times.
 * These can be referenced anywhere they are needed.
 */
public class Animations {
    
    /**
     * The animation for the character walking down.
     */
    public static final Animation<TextureRegion> CHARACTER_WALK_DOWN = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(1, 1),
            SpriteSheet.CHARACTER.at(1, 2),
            SpriteSheet.CHARACTER.at(1, 3),
            SpriteSheet.CHARACTER.at(1, 4)
    );

    /**
            * The animation for the character walking left.
            */
    public static final Animation<TextureRegion> CHARACTER_WALK_LEFT = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(4, 1),
            SpriteSheet.CHARACTER.at(4, 2),
            SpriteSheet.CHARACTER.at(4, 3),
            SpriteSheet.CHARACTER.at(4, 4)
    );

    /**
            * The animation for the character walking right.
            */
    public static final Animation<TextureRegion> CHARACTER_WALK_RIGHT = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(2, 1),
            SpriteSheet.CHARACTER.at(2, 2),
            SpriteSheet.CHARACTER.at(2, 3),
            SpriteSheet.CHARACTER.at(2, 4)
    );

    /**
     * The animation for the character walking up.
     */
    public static final Animation<TextureRegion> CHARACTER_WALK_UP = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(3, 1),
            SpriteSheet.CHARACTER.at(3, 2),
            SpriteSheet.CHARACTER.at(3, 3),
            SpriteSheet.CHARACTER.at(3, 4)
    );

    /**
     * The animation for the character attacking up.
     */
    public static final Animation<TextureRegion> CHARACTER_ATTACK_UP = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(3, 6),
            SpriteSheet.CHARACTER.at(3, 7),
            SpriteSheet.CHARACTER.at(3, 8)
    );
    /**
     * The animation for the character attacking down.
     */
    public static final Animation<TextureRegion> CHARACTER_ATTACK_DOWN = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(1, 6),
            SpriteSheet.CHARACTER.at(1, 7),
            SpriteSheet.CHARACTER.at(1, 8)
    );
    /**
     * The animation for the character attacking left.
     */
    public static final Animation<TextureRegion> CHARACTER_ATTACK_LEFT = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(4, 6),
            SpriteSheet.CHARACTER.at(4, 7),
            SpriteSheet.CHARACTER.at(4, 8)
    );
    /**
     * The animation for the character attacking right.
     */
    public static final Animation<TextureRegion> CHARACTER_ATTACK_RIGHT = new Animation<>(0.1f,
            SpriteSheet.CHARACTER.at(2, 6),
            SpriteSheet.CHARACTER.at(2, 7),
            SpriteSheet.CHARACTER.at(2, 8)
    );



}
