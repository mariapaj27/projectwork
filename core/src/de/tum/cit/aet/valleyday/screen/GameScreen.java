package de.tum.cit.aet.valleyday.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.map.Flowers;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.map.GameMap;
import de.tum.cit.aet.valleyday.map.MapLoader;
import de.tum.cit.aet.valleyday.audio.GameSound;


/**
 * The GameScreen class is responsible for rendering the gameplay screen.
 * It handles the game logic and rendering of the game elements.
 */
public class GameScreen implements Screen {
    
    /**
     * The size of a grid cell in pixels.
     * This allows us to think of coordinates in terms of square grid tiles
     * (e.g. x=1, y=1 is the bottom left corner of the map)
     * rather than absolute pixel coordinates.
     */
    public static final int TILE_SIZE_PX = 16;
    
    /**
     * The scale of the game.
     * This is used to make everything in the game look bigger or smaller.
     */
    public static final int SCALE = 4;

    private final ValleyDayGame game;
    private final SpriteBatch spriteBatch;
    private final GameMap map;
    private final Hud hud;
    private final OrthographicCamera mapCamera;
    private final OrthographicCamera overlayCamera;
    //darkness effect
    private final Texture whitePixel;
    //pause menu
    private final Stage pauseMenuStage;
    private final Stage winMenuStage;
    private final Stage gameOverMenuStage;
    private boolean showPauseMenu = false;
    private boolean showWinMenu = false;
    private boolean showGameOverMenu = false;


    /**
     * Constructor for GameScreen. Sets up the camera and font.
     *
     * @param game The main game class, used to access global resources and methods.
     */
    public GameScreen(ValleyDayGame game) {
        this.game = game;
        this.spriteBatch = game.getSpriteBatch();
        this.map = game.getMap();
        this.hud = new Hud(spriteBatch, game.getSkin().getFont("font"), this.map);
        // Create and configure the camera for the game view
        this.mapCamera = new OrthographicCamera();
        this.mapCamera.setToOrtho(false);
        //added
        // camera for darkness overlay
        this.overlayCamera = new OrthographicCamera();
        this.overlayCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //darkness texture
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whitePixel = new Texture(pixmap);
        pixmap.dispose();

        //pause menu
        this.pauseMenuStage = new Stage(new ScreenViewport());
        createPauseMenu();

        // creates win menu
        this.winMenuStage = new Stage(new ScreenViewport());
        createWinMenu();

        // creates game over menu
        this.gameOverMenuStage = new Stage(new ScreenViewport());
        createGameOverMenu();
    }
    
    /**
     * The render method is called every frame to render the game.
     * @param deltaTime The time in seconds since the last render.
     */
    @Override
    public void render(float deltaTime) {
        // checks if E key press to pick up the shovel
        if (!showPauseMenu && !showWinMenu && !showGameOverMenu && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            map.tryInteract();
        }
        // if escape key pressed open pause menu
        if (!showWinMenu && !showGameOverMenu && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePauseMenu();
        }
        // checks if A key press to plant/harvest/restore plants
        if (!showPauseMenu && !showWinMenu && !showGameOverMenu && Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            map.tryPlantAction();
        }
        
        // Clear the previous frame from the screen, or else the picture smears
        ScreenUtils.clear(Color.BLACK);
        
        // Cap frame time to 250ms to prevent spiral of death
        float frameTime = Math.min(deltaTime, 0.250f);
        
        // Update the map state
        map.tick(frameTime);

        // Check if game is lost
        if (!showGameOverMenu && map.isGameLost()) {
            showGameOverMenu = true;
            map.setPaused(true);
            Gdx.input.setInputProcessor(gameOverMenuStage);
            GameSound.LOSE.play();
        }
        //time over
        if (!showGameOverMenu && !showWinMenu && map.getDaylightTimeRemaining() <= 0) {
            map.setGameLost(true); // trigger the game over menu
            GameSound.LOSE.play();
        }

        //checks if the game is won
        if (!showWinMenu && map.isGameWon()) {
            showWinMenu = true;
            map.setPaused(true);
            Gdx.input.setInputProcessor(winMenuStage);
            GameSound.WIN.play();
        }
        
        // Update the camera
        updateCamera();
        
        // Render the map on the screen
        renderMap();
        // renders darkness
        renderDarkness();
        
        // Render the HUD on the screen
        hud.render();
        // renders pause menu
        if (showPauseMenu) {
            renderPauseMenuOverlay(); //pause background(little grey)
            pauseMenuStage.act(Math.min(deltaTime, 1 / 30f)); // time of appearance
            pauseMenuStage.draw(); //draws menu
        }
        // renders win menu
        if (showWinMenu) {
            renderPauseMenuOverlay();
            winMenuStage.act(Math.min(deltaTime, 1 / 30f));
            winMenuStage.draw();
        }

        // renders game over menu
        if (showGameOverMenu) {
            renderPauseMenuOverlay();
            gameOverMenuStage.act(Math.min(deltaTime, 1 / 30f));
            gameOverMenuStage.draw();
        }
    }

      /**
     * Updates the camera to match the current state of the game.
     * Centers the camera on the player's position.
     */
    private void updateCamera() {
        mapCamera.setToOrtho(false);
        
        // Center camera on player position
        if (map != null && map.getPlayer() != null) {
            float playerX = map.getPlayer().getX();
            float playerY = map.getPlayer().getY();
            
            // converts  coordinates to pixel coordinates
            mapCamera.position.x = playerX * TILE_SIZE_PX * SCALE;
            mapCamera.position.y = playerY * TILE_SIZE_PX * SCALE;
        } else {
            // default position if player doesn't exist
            mapCamera.position.x = 3.5f * TILE_SIZE_PX * SCALE;
            mapCamera.position.y = 3.5f * TILE_SIZE_PX * SCALE;
        }
        
        mapCamera.update(); // This is necessary to apply the changes
    }

     private void renderMap() {
        // This configures the spriteBatch to use the camera's perspective when rendering
        spriteBatch.setProjectionMatrix(mapCamera.combined);
        
        // Start drawing
        spriteBatch.begin();
        
        // Render everything in the map here, in order from lowest to highest (later things appear on top)
        for (Drawable drawable : map.getAllDrawables()) {
            if (drawable != null) {
                draw(spriteBatch, drawable);
            }
        }
        
        // Finish drawing, i.e. send the drawn items to the graphics card
        spriteBatch.end();
    }
    
    /**
     * Draws this object on the screen.
     * The texture will be scaled by the game scale and the tile size.
     * This should only be called between spriteBatch.begin() and spriteBatch.end(), e.g. in the renderMap() method.
     * @param spriteBatch The SpriteBatch to draw with.
     */
    private static void draw(SpriteBatch spriteBatch, Drawable drawable) {
        TextureRegion texture = drawable.getCurrentAppearance();
        // Drawable coordinates are in tiles, so we need to scale them to pixels
        float x = drawable.getX() * TILE_SIZE_PX * SCALE;
        float y = drawable.getY() * TILE_SIZE_PX * SCALE;
        // Additionally scale everything by the game scale
        float width = texture.getRegionWidth() * SCALE;
        float height = texture.getRegionHeight() * SCALE;
        spriteBatch.draw(texture, x, y, width, height);
    }
    /**
     * Renders darkness overlay based on remaining daylight time.
     */
    private void renderDarkness() {
        float darkness = map.getDarknessLevel();
        if (darkness > 0) {
            // full-screen covered
            spriteBatch.setProjectionMatrix(overlayCamera.combined);
            spriteBatch.begin();

            // draws darkness
            spriteBatch.setColor(0, 0, 0, darkness);
            spriteBatch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            //resets transparency to white for next render
            spriteBatch.setColor(Color.WHITE);
            spriteBatch.end();
        }
    }

    /**
     * Gray overlay when pause menu opened.
     */
    private void renderPauseMenuOverlay() {
        spriteBatch.setProjectionMatrix(overlayCamera.combined);
        spriteBatch.begin();

        // draw background
        spriteBatch.setColor(0.2f, 0.2f, 0.2f, 0.7f);
        //draws overlay
        spriteBatch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // resets color to white
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

    /**
     * Creates the pause menu buttons.
     */
    private void createPauseMenu() {
        //for structure
        Table table = new Table();
        table.setFillParent(true);

        // Menu title
        Label titleLabel = new Label("GAME PAUSED", game.getSkin());
        titleLabel.setFontScale(2.0f);// text size

        // Resume button
        TextButton resumeButton = new TextButton("Resume", game.getSkin());
        resumeButton.getLabel().setFontScale(1.0f / 1.5f);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                togglePauseMenu();
            }
        });

        // Back to Title button
        TextButton backButton = new TextButton("Back to Title", game.getSkin());
        backButton.getLabel().setFontScale(1.0f / 1.5f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showPauseMenu = false;
                map.setPaused(false);
                game.goToMenu();
            }
        });

        // added to menu table
        table.add(titleLabel).padBottom(40).row();
        table.add(resumeButton).width(200).height(50).padBottom(20).row();
        table.add(backButton).width(200).height(50).row();

        pauseMenuStage.addActor(table);
    }

    /**
     * Menu actions
     */
    private void togglePauseMenu() {
        showPauseMenu = !showPauseMenu;
        map.setPaused(showPauseMenu);

        // if menu buttons are clickable
        if (showPauseMenu) {
            Gdx.input.setInputProcessor(pauseMenuStage);
        } else {
            Gdx.input.setInputProcessor(null);
        }
    }
    
    /**
     * Called when the window is resized.
     * This is where the camera is updated to match the new window size.
     * @param width The new window width.
     * @param height The new window height.
     */
    @Override
    public void resize(int width, int height) {
        mapCamera.setToOrtho(false);
        overlayCamera.setToOrtho(false, width, height);// darkness on full screen
        hud.resize(width, height);
        pauseMenuStage.getViewport().update(width, height, true);// proper pause menu on resizing
        winMenuStage.getViewport().update(width, height, true);
        gameOverMenuStage.getViewport().update(width, height, true);
    }

    // Unused methods from the Screen interface
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {
    }
    //removes not used resources
    @Override
    public void dispose() {
    whitePixel.dispose();
    hud.dispose();
    pauseMenuStage.dispose();
    winMenuStage.dispose();
    gameOverMenuStage.dispose();
    }

    /**
     * Creates the win menu.
     */
    private void createWinMenu() {
        Table table = new Table();
        table.setFillParent(true);

        // title
        Label titleLabel = new Label("WINNER!", game.getSkin());
        titleLabel.setFontScale(2.0f);

        // back to menu button
        TextButton backButton = new TextButton("Back To Menu", game.getSkin());
        backButton.getLabel().setFontScale(1.0f / 1.5f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showWinMenu = false;
                map.setPaused(false);
                game.goToMenu();
            }
        });

        // layout
        table.add(titleLabel).padBottom(40).row();
        table.add(backButton).width(200).height(50).row();

        winMenuStage.addActor(table);
    }

    /**
     * Creates the game over menu .
     */
    private void createGameOverMenu() {
        Table table = new Table();
        table.setFillParent(true);

        // title
        Label titleLabel = new Label("GAME OVER", game.getSkin());
        titleLabel.setFontScale(2.0f);

        // start new map button
        TextButton newMapButton = new TextButton("Start New Map", game.getSkin());
        newMapButton.getLabel().setFontScale(1.0f / 1.5f);
        newMapButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showGameOverMenu = false;
                map.setPaused(false);
                game.loadMap(new MapLoader.MapLoadCallback() {
                    @Override
                    public void onMapLoaded(de.tum.cit.aet.valleyday.map.MapLoader.MapData mapData) {
                        game.goToGame();
                    }

                    @Override
                    public void onCancellation() {
                        showGameOverMenu = true;
                        map.setPaused(true);
                        Gdx.input.setInputProcessor(gameOverMenuStage);
                    }

                    @Override
                    public void onError(Exception exception) {
                        exception.printStackTrace();
                        showGameOverMenu = true;
                        map.setPaused(true);
                        Gdx.input.setInputProcessor(gameOverMenuStage);
                    }
                });
            }
        });

        // back to menu button
        TextButton backButton = new TextButton("Back to Menu", game.getSkin());
        backButton.getLabel().setFontScale(1.0f / 1.5f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showGameOverMenu = false;
                map.setPaused(false);
                game.goToMenu();
            }
        });

        // layout
        table.add(titleLabel).padBottom(40).row();
        table.add(newMapButton).width(200).height(50).padBottom(20).row();
        table.add(backButton).width(200).height(50).row();

        gameOverMenuStage.addActor(table);
    }

}