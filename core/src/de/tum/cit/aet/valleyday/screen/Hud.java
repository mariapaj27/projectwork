package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.aet.valleyday.map.GameMap;

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
    private final GameMap map;
     /** Box for timer/collected items */
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
       // draws box in top-left corner
        drawStatisticsPanel();
        // draw  hint if player is near shovel
        drawPickupHint();
        // Draw the HUD elements
        font.draw(spriteBatch, "Press Esc to Pause!", 10, Gdx.graphics.getHeight() - 10);
        // Finish drawing
        spriteBatch.end();
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
    
    //*  Draws box with timer, debris, and exit status.
     
    private void drawStatisticsPanel() {
        int panelX = 10;
        int panelWidth = 200;
        int padding = 12;
        int iconSize = 26;
        int lineHeight = 32;

        // dynamic height of panel
        int contentLines = 2; // timer + debris
        if (map.hasShovel()) contentLines++; // add shovel
        if (map.hasFertilizer()) contentLines++; // add fertilizer
        if (map.getSeedsCollected() > 0) contentLines++; // add seeds count
        contentLines++; // exit text
        int panelHeight = padding * 2 + iconSize * contentLines + lineHeight + 20;
        int panelY = Gdx.graphics.getHeight() - panelHeight - 10;

        // draws brown background for panel
        spriteBatch.setColor(0.3f, 0.2f, 0.1f, 1f); // brown color
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

        // draws shovel icon if player has it.
        if (map.hasShovel()) {
            drawShovelIcon(panelX + padding, currentY, iconSize);
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
            font.draw(spriteBatch, "Shovel", panelX + padding + iconSize + 8, currentY + iconSize - 6);
            currentY -= lineHeight;
        } else {
            currentY -= 10;
        }
        // draws fertilizer icon if player has it.
        if (map.hasFertilizer()) {
            drawFertilizerIcon(panelX + padding, currentY, iconSize);
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
            font.draw(spriteBatch, "Fertilizer", panelX + padding + iconSize + 8, currentY + iconSize - 6);
            currentY -= lineHeight;
        }
        //Draws seeds count + icon
        if (map.getSeedsCollected() > 0) {
            drawSeedIcon(panelX + padding, currentY, iconSize);
            font.setColor(0.9f, 0.8f, 0.6f, 1f);
            font.draw(spriteBatch, "Seeds: " + map.getSeedsCollected(), panelX + padding + iconSize + 8, currentY + iconSize - 6);
            currentY -= lineHeight;
        }
        // Debris icon and count
        drawDebrisIcon(panelX + padding, currentY, iconSize);
        font.setColor(0.9f, 0.8f, 0.6f, 1f);
        String debrisText = map.getDebrisCollected() + "/" + map.getMinDebrisRequired();
        font.draw(spriteBatch, debrisText, panelX + padding + iconSize + 8, currentY + iconSize - 6);
        
        currentY -= lineHeight + 10;
        
        // exit status 
        boolean canExit = map.canExit();
        if (canExit) {
            font.setColor(0.3f, 0.8f, 0.3f, 1f);
        } else {
            font.setColor(0.8f, 0.2f, 0.2f, 1f); 
        }
        
        //draws exit centered
        String exitText = "EXIT";
        float exitWidth = font.getData().getGlyph(exitText.charAt(0)).width * exitText.length() * 1.2f;
        float exitX = panelX + (panelWidth - exitWidth) / 2;
        font.getData().setScale(1.5f); 
        font.draw(spriteBatch, exitText, exitX, currentY + 20);
        font.getData().setScale(1.0f); 
        
        spriteBatch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }
    
    /**
     * draws timer icon.
     */
    private void drawTimerIcon(int x, int y, int size) {
        //drawdclock background
        spriteBatch.setColor(0.8f, 0.7f, 0.3f, 1f);
        drawCircle(x + size/2, y + size/2, size/2);
        
        //draws clock arrow
        spriteBatch.setColor(0.3f, 0.2f, 0.1f, 1f);
        spriteBatch.draw(whitePixel, x + size/2 - 1, y + size/2, 2, size/3);
        
        spriteBatch.setColor(Color.WHITE);
    }
    
    /**
     * draws a debris icon.
     */
    private void drawDebrisIcon(int x, int y, int size) {
        spriteBatch.setColor(0.5f, 0.35f, 0.25f, 1f);
        
        // draws icon body
        spriteBatch.draw(whitePixel, x + 2, y + 4, 8, 6);
        spriteBatch.draw(whitePixel, x + 8, y + 10, 6, 8);
        spriteBatch.draw(whitePixel, x + 14, y + 6, 7, 5);
        
        spriteBatch.setColor(Color.WHITE);
    }
    /**
     * Draws seed icon.
     */
    private void drawSeedIcon(int x, int y, int size) {
        spriteBatch.setColor(0.5f, 0.35f, 0.2f, 1f);
        spriteBatch.draw(whitePixel, x + size / 2 - 3, y + size / 2 - 2, 6, 4);

        spriteBatch.setColor(0.3f, 0.75f, 0.3f, 1f);
        spriteBatch.draw(whitePixel, x + size / 2 - 1, y + size / 2 + 2, 2, 6);

        spriteBatch.setColor(Color.WHITE);
    }
    
    /**
     * Draws a filled circle for timer
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
     * Disposes of resources.
     */
    public void dispose() {
        whitePixel.dispose();
    }
    /**
     * Draws a shovel icon.
     */
    private void drawShovelIcon(int x, int y, int size) {
        // shovel handle
        spriteBatch.setColor(0.45f, 0.3f, 0.2f, 1f);
        spriteBatch.draw(whitePixel, x + size/2 - 2, y + 4, 4, size - 6);

        //main of the shovel
        spriteBatch.setColor(0.6f, 0.6f, 0.6f, 1f);
        spriteBatch.draw(whitePixel, x + size/2 - 4, y, 8, 6);

        spriteBatch.setColor(Color.WHITE);
    }
    /**
     * Draws a fertilizer icon.
     */
    private void drawFertilizerIcon(int x, int y, int size) {
        // draws the bag
        spriteBatch.setColor(0.75f, 0.65f, 0.45f, 1f);
        spriteBatch.draw(whitePixel, x + 4, y + 3, size - 8, size - 6);

        // the top
        spriteBatch.setColor(0.6f, 0.5f, 0.35f, 1f);
        spriteBatch.draw(whitePixel, x + 6, y + size - 9, size - 12, 4);

        // body
        spriteBatch.setColor(0.3f, 0.7f, 0.3f, 1f);
        spriteBatch.draw(whitePixel, x + size/2 - 2, y + size/2 - 2, 4, 4);

        spriteBatch.setColor(Color.WHITE);
    }

    /**
     * Draws hint when player is near a shovel/fertilizer.
     */
    private void drawPickupHint() {
        if (map.getNearestFertilizer() != null && !map.hasFertilizer()) {
            drawHint("Press 'E' to take the fertilizer");
            return;
        }
        if (map.getNearestShovel() != null && !map.hasShovel()) {
            drawHint("Press 'E' to take the shovel");
        }
    }
    /**
     * Draws specific hint.
     */
    private void drawHint(String hintText) {
        float textWidth = font.getData().getGlyph('E').width * hintText.length() * 1f;
        float x = (Gdx.graphics.getWidth() - textWidth) / 2;
        float y = 120;

        int bgWidth = (int) (textWidth + 24);
        int bgHeight = 36;
        int bgX = (int) (x - 12);
        int bgY = (int) (y - 28);

        // draws background
        spriteBatch.setColor(0.4f, 0.3f, 0.2f, 0.95f);
        spriteBatch.draw(whitePixel, bgX, bgY, bgWidth, bgHeight);

        // draws border
        spriteBatch.setColor(0.5f, 0.4f, 0.3f, 1f);
        int borderWidth = 2;
        spriteBatch.draw(whitePixel, bgX, bgY, bgWidth, borderWidth);
        spriteBatch.draw(whitePixel, bgX, bgY + bgHeight - borderWidth, bgWidth, borderWidth);
        spriteBatch.draw(whitePixel, bgX, bgY, borderWidth, bgHeight);
        spriteBatch.draw(whitePixel, bgX + bgWidth - borderWidth, bgY, borderWidth, bgHeight);

        // draws text
        font.setColor(0.9f, 0.8f, 0.6f, 1f);
        font.draw(spriteBatch, hintText, x, y);

        spriteBatch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

}

