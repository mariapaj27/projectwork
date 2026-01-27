package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

/**
 * A Heads-Up Display (HUD) that displays information on the screen.
 * It uses a separate camera so that it is always fixed on the screen.
 */
public class Hud {

    /** The SpriteBatch used to draw the HUD. This is the same as the one used in the GameScreen. */
    private final SpriteBatch spriteBatch;
    /** The font used to draw text on the screen. */
    private final BitmapFont font;
    /** The camera used to render the HUD. */
    private final OrthographicCamera camera;
    /** The game map to get statistics from. */
    private final GameMap map;
    /** A white pixel texture for drawing rectangles. */
    private final Texture whitePixel;

    public Hud(SpriteBatch spriteBatch, BitmapFont font, GameMap map) {
        this.spriteBatch = spriteBatch;
        this.font = font;
        this.camera = new OrthographicCamera();
        this.map = map;

        // Creates texture for box
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whitePixel = new Texture(pixmap);
        pixmap.dispose();
    }

    /**
     * Renders the HUD on the screen.
     * This uses a different OrthographicCamera so that the HUD is always fixed on the screen.
     */
    public void render() {
        // Render from the camera's perspective
        spriteBatch.setProjectionMatrix(camera.combined);
        // Start drawing
        spriteBatch.begin();

        // smaller font
        float baseFontScale = 1.0f / 1.5f;
        font.getData().setScale(baseFontScale);

        // draws box in top-left corner
        drawStatisticsPanel();
        // draw  hint if player is near shovel
        drawPickupHint();
        // Reset font scale
        font.getData().setScale(1.0f);
        // Finish drawing
        spriteBatch.end();
    }

    /**
     *  Draws box with timer, debris, and exit status.
     */
    private void drawStatisticsPanel() {
        int panelX = 10;
        int panelWidth = 200;
        int padding = 12;
        int iconSize = 24;
        int lineHeight = 32;

        // height of panel
        int contentLines = 7;
        contentLines++;
        int panelHeight = padding * 2 + iconSize * contentLines + lineHeight + 20;
        int panelY = Gdx.graphics.getHeight() - panelHeight - 10;

        // draws brown background for panel
        spriteBatch.setColor(0.4f, 0.3f, 0.2f, 1f); // brown
        spriteBatch.draw(whitePixel, panelX, panelY, panelWidth, panelHeight);

        // borders lighter
        spriteBatch.setColor(0.5f, 0.4f, 0.3f, 1f);
        int borderWidth = 1;
        spriteBatch.draw(whitePixel, panelX, panelY, panelWidth, borderWidth);
        spriteBatch.draw(whitePixel, panelX, panelY + panelHeight - borderWidth, panelWidth, borderWidth);
        spriteBatch.draw(whitePixel, panelX, panelY, borderWidth, panelHeight);
        spriteBatch.draw(whitePixel, panelX + panelWidth - borderWidth, panelY, borderWidth, panelHeight);

        spriteBatch.setColor(Color.WHITE);

        int currentY = panelY + panelHeight - padding - iconSize;

        // Timer
        drawTimerIcon(panelX + padding, currentY, iconSize);
        font.setColor(0.9f, 0.8f, 0.6f, 1f);
        font.draw(spriteBatch, map.getFormattedTime(), panelX + padding + iconSize + 8, currentY + iconSize - 6);

        currentY -= lineHeight;

        // Debris
        drawDebrisIcon(panelX + padding, currentY, iconSize);
        font.setColor(0.9f, 0.8f, 0.6f, 1f);
        String debrisText = map.getDebrisCollected() + "/" + map.getMinDebrisRequired();
        font.draw(spriteBatch, debrisText, panelX + padding + iconSize + 8, currentY + iconSize - 6);

        currentY -= lineHeight;

        // Shovel
        boolean hasShovel = map.hasShovel();
        drawShovelIcon(panelX + padding, currentY, iconSize, hasShovel);
        if (hasShovel) {
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
        } else {
            font.setColor(0.5f, 0.5f, 0.5f, 1f); // grey if not picked yet
        }
        font.draw(spriteBatch, "Shovel", panelX + padding + iconSize + 8, currentY + iconSize - 6);
        currentY -= lineHeight;

        // Fertilizer
        boolean hasFertilizer = map.hasFertilizer();
        drawFertilizerIcon(panelX + padding, currentY, iconSize, hasFertilizer);
        if (hasFertilizer) {
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
        } else {
            font.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        font.draw(spriteBatch, "Fertilizer", panelX + padding + iconSize + 8, currentY + iconSize - 6);
        currentY -= lineHeight;

        // Watering Can
        boolean hasWateringCan = map.hasWateringCan();
        drawWateringCanIcon(panelX + padding, currentY, iconSize, hasWateringCan);
        if (hasWateringCan) {
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
        } else {
            font.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        font.draw(spriteBatch, "Watering Can", panelX + padding + iconSize + 8, currentY + iconSize - 6);
        currentY -= lineHeight;

        // Seeds
        int seedsCollected = map.getSeedsCollected();
        drawSeedIcon(panelX + padding, currentY, iconSize, seedsCollected > 0);
        if (seedsCollected > 0) {
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
        } else {
            font.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        font.draw(spriteBatch, "Seeds: " + seedsCollected, panelX + padding + iconSize + 8, currentY + iconSize - 6);
        currentY -= lineHeight;

        // Plants
        int plantsCollected = map.getPlantsCollected();
        drawPlantIcon(panelX + padding, currentY, iconSize, plantsCollected > 0);
        if (plantsCollected > 0) {
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
        } else {
            font.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        font.draw(spriteBatch, "Plants: " + plantsCollected, panelX + padding + iconSize + 8, currentY + iconSize - 6);
        currentY -= lineHeight;

        // EXIT status
        boolean canExit = map.canExit();
        if (canExit) {
            font.setColor(0.3f, 0.8f, 0.3f, 1f); // green
        } else {
            font.setColor(0.8f, 0.2f, 0.2f, 1f); // red
        }

        // exit in center
        String exitText = "EXIT";
        float exitScale = 1.0f;
        font.getData().setScale((1.0f / 1.5f) * exitScale);
        float exitWidth = font.getData().getGlyph(exitText.charAt(0)).width * exitText.length() * 1.2f;
        float exitX = panelX + (panelWidth - exitWidth) / 2;
        font.draw(spriteBatch, exitText, exitX, currentY + 20);
        font.getData().setScale(1.0f / 1.5f);

        spriteBatch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    /**
     * Draws a timer icon.
     */
    private void drawTimerIcon(int x, int y, int size) {
        //draws clock background
        spriteBatch.setColor(0.8f, 0.7f, 0.3f, 1f);
        drawCircle(x + size/2, y + size/2, size/2);

        //draws clock arrow
        spriteBatch.setColor(0.3f, 0.2f, 0.1f, 1f);
        spriteBatch.draw(whitePixel, x + size/2 - 1, y + size/2, 2, size/3);

        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws debris icon.
     */
    private void drawDebrisIcon(int x, int y, int size) {
        TextureRegion debrisTexture = SpriteSheet.OUTSIDE_TILES.at(3, 2);
        spriteBatch.draw(debrisTexture, x, y, size, size);
    }

    /**
     * Draws a circle for clock.
     */
    private void drawCircle(int centerX, int centerY, int radius) {
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    spriteBatch.draw(whitePixel, centerX + i, centerY + j, 1, 1);
                }
            }
        }
    }

    /**
     * Draws a shovel icon.
     * @param hasItem If false, draws grayed.
     */
    private void drawShovelIcon(int x, int y, int size, boolean hasItem) {
        TextureRegion shovelTexture = SpriteSheet.BASIC_ITEMS.at(3, 7);
        if (hasItem) {
            spriteBatch.setColor(Color.WHITE);
        } else {
            spriteBatch.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        spriteBatch.draw(shovelTexture, x, y, size, size);
        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws a fertilizer icon.
     * @param hasItem If false, draws grayed.
     */
    private void drawFertilizerIcon(int x, int y, int size, boolean hasItem) {
        TextureRegion fertilizerTexture = SpriteSheet.BASIC_TILES.at(4, 4);
        if (hasItem) {
            spriteBatch.setColor(Color.WHITE);
        } else {
            spriteBatch.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        spriteBatch.draw(fertilizerTexture, x, y, size, size);
        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws a seed icon.
     * @param hasItem If false, draws grayed.
     */
    private void drawSeedIcon(int x, int y, int size, boolean hasItem) {
        TextureRegion seedTexture = SpriteSheet.BASIC_TILES.at(3, 5);
        if (hasItem) {
            spriteBatch.setColor(Color.WHITE);
        } else {
            spriteBatch.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        spriteBatch.draw(seedTexture, x, y, size, size);
        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws a watering can icon.
     * @param hasItem If false, draws grayed.
     */
    private void drawWateringCanIcon(int x, int y, int size, boolean hasItem) {
        TextureRegion wateringCanTexture = SpriteSheet.BASIC_ITEMS.at(2, 13);
        if (hasItem) {
            spriteBatch.setColor(Color.WHITE);
        } else {
            spriteBatch.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        spriteBatch.draw(wateringCanTexture, x, y, size, size);
        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws a plant icon.
     * @param hasItem If false, draws grayed.
     */
    private void drawPlantIcon(int x, int y, int size, boolean hasItem) {
        TextureRegion plantTexture = SpriteSheet.HARVEST.at(3, 4); // fully grown
        if (hasItem) {
            spriteBatch.setColor(Color.WHITE);
        } else {
            spriteBatch.setColor(0.5f, 0.5f, 0.5f, 1f);//gray
        }
        spriteBatch.draw(plantTexture, x, y, size, size);
        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws pickup hint for shovel.
     */
    private void drawPickupHint() {
        // priority: fertilizer,watering can,shovel
        if (map.getNearestFertilizer() != null && !map.hasFertilizer()) {
            drawHint("Press 'E' to take the fertilizer");
            return;
        }
        if (map.getNearestWateringCan() != null && !map.hasWateringCan()) {
            drawHint("Press 'E' to take the watering can");
            return;
        }
        if (map.getNearestShovel() != null && !map.hasShovel()) {
            drawHint("Press 'E' to take the shovel");
            return;
        }

        // plant actions (plant/harvest/restore)
        GameMap.PlantActionInfo plantAction = map.getNearestPlantAction();
        if (plantAction != null) {
            switch (plantAction.type) {
                case PLANT:
                    drawHint("Press 'A' to plant a seed");
                    break;
                case HARVEST:
                    drawHint("Press 'A' to harvest the plant");
                    break;
                case RESTORE:
                    drawHint("Press 'A' to restore the plant");
                    break;
            }
        }
    }
    /**
    * Draws the base of hint body.
     * */
    private void drawHint(String hintText) {
        float textWidth = font.getData().getGlyph('E').width * hintText.length() * 1f;
        float x = (Gdx.graphics.getWidth() - textWidth) / 2;
        float y = 120;

        int bgWidth = (int) (textWidth + 24);
        int bgHeight = 36;
        int bgX = (int) (x - 12);
        int bgY = (int) (y - 28);

        // brown background
        spriteBatch.setColor(0.4f, 0.3f, 0.2f, 0.95f);
        spriteBatch.draw(whitePixel, bgX, bgY, bgWidth, bgHeight);

        // borders
        spriteBatch.setColor(0.5f, 0.4f, 0.3f, 1f);
        int borderWidth = 2;
        spriteBatch.draw(whitePixel, bgX, bgY, bgWidth, borderWidth);
        spriteBatch.draw(whitePixel, bgX, bgY + bgHeight - borderWidth, bgWidth, borderWidth);
        spriteBatch.draw(whitePixel, bgX, bgY, borderWidth, bgHeight);
        spriteBatch.draw(whitePixel, bgX + bgWidth - borderWidth, bgY, borderWidth, bgHeight);

        // text
        font.setColor(0.9f, 0.8f, 0.6f, 1f);
        font.draw(spriteBatch, hintText, x, y);

        spriteBatch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    /**
     * Resizes the HUD when the screen size changes.
     * This is called when the window is resized.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    /**
     * Disposes of resources used by the HUD.
     */
    public void dispose() {
        whitePixel.dispose();
    }

}
