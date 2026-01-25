package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * Plant model with 5 growth stages.
 * Stage 1-3: Growing
 * Stage 4: Fully grown
 * Stage 5: Rotten
 *
 * Each stage lasts 30 seconds after planting.
 */
public class Plant implements Drawable {

    public enum GrowthStage {
        SEEDLING(1),      // Stage 1
        GROWING(2),       // Stage 2
        ALMOST_GROWN(3),  // Stage 3
        FULLY_GROWN(4),   // Stage 4
        ROTTEN(5);        // Stage 5

        private final int stageNumber;

        GrowthStage(int stageNumber) {
            this.stageNumber = stageNumber;
        }

        public int getStageNumber() {
            return stageNumber;
        }
    }

    private final float x;
    private final float y;
    private GrowthStage stage;
    private float timePlanted;
    private static final float STAGE_DURATION = 30.0f; // 30 seconds per cycle

    /**
     * Creates a new plant at the given position.
     * @param x The X position.
     * @param y The Y position.
     * @param currentTime The current game time when planted.
     */
    public Plant(float x, float y, float currentTime) {
        this.x = x;
        this.y = y;
        this.stage = GrowthStage.SEEDLING;
        this.timePlanted = currentTime;
    }

    /**
     * Updates the plant's growth stage based on  time.
     * @param currentTime The current game time.
     */
    public void update(float currentTime) {
        if (stage == GrowthStage.ROTTEN) {
            return;
        }

        float elapsed = currentTime - timePlanted;
        int stagesPassed = (int) (elapsed / STAGE_DURATION);

        // stages 1-4
        if (stagesPassed >= 3) {
            stage = GrowthStage.FULLY_GROWN;
        } else if (stagesPassed >= 2) {
            stage = GrowthStage.ALMOST_GROWN;
        } else if (stagesPassed >= 1) {
            stage = GrowthStage.GROWING;
        } else {
            stage = GrowthStage.SEEDLING;
        }

        // After stage 4 if not harvested -> rotten
        if (stage == GrowthStage.FULLY_GROWN && elapsed >= STAGE_DURATION * 4) {
            stage = GrowthStage.ROTTEN;
        }
    }

    /**
     * Restores a rotten plant.
     * @param currentTime The current game time.
     */
    public void restore(float currentTime) {
        if (stage == GrowthStage.ROTTEN) {
            stage = GrowthStage.FULLY_GROWN;
            // resets time
            timePlanted = currentTime - STAGE_DURATION * 3;
        }
    }

    /**
     * Checks if the plant can be harvested.
     * @return True if plant is fully grown and can be harvested.
     */
    public boolean canHarvest() {
        return stage == GrowthStage.FULLY_GROWN;
    }

    /**
     * Checks if the plant can be restored.
     * @return True if plant is rotten and can be restored.
     */
    public boolean canRestore() {
        return stage == GrowthStage.ROTTEN;
    }

    public GrowthStage getStage() {
        return stage;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // returns texture based on growth stage
        switch (stage) {
            case SEEDLING:
                return SpriteSheet.HARVEST.at(3, 1); // stage 1 texture
            case GROWING:
                return SpriteSheet.HARVEST.at(3, 2); // stage 2 texture
            case ALMOST_GROWN:
                return SpriteSheet.HARVEST.at(3, 3); // stage 3 texture
            case FULLY_GROWN:
                return SpriteSheet.HARVEST.at(3, 4); // stage 4 texture
            case ROTTEN:
                return SpriteSheet.HARVEST.at(3, 8); // stage 5 texture
            default:
                return SpriteSheet.HARVEST.at(3, 1);
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
}
