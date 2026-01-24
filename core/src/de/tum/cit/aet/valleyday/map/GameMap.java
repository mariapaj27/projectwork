package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.texture.Drawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the game map.
 * Contains all the objects and entities in the game.
 */
public class GameMap {

    
    // executed when the class is referenced for the first time.
    static {
        // initializes the Box2D physics engine.
        com.badlogic.gdx.physics.box2d.Box2D.init();
    }
    
    // Box2D physics simulation parameters (you can experiment with these if you want, but they work well as they are)
    /**
     * The time step for the physics simulation.
     * This is the amount of time that the physics simulation advances by in each frame.
     * It is set to 1/refreshRate, where refreshRate is the refresh rate of the monitor, e.g., 1/60 for 60 Hz.
     */
    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate;
    /** The number of velocity iterations for the physics simulation. */
    private static final int VELOCITY_ITERATIONS = 6;
    /** The number of position iterations for the physics simulation. */
    private static final int POSITION_ITERATIONS = 2;
    /**
     * The accumulated time since the last physics step.
     * We use this to keep the physics simulation at a constant rate even if the frame rate is variable.
     */
    private float physicsTime = 0;
    
    /** The game, in case the map needs to access it. */
    private final ValleyDayGame game;
    /** The Box2D world for physics simulation. */
    private final World world;

    //BONUS darkening
    private float daylightTimeRemaining = 300f;
    private static final float MAX_DAYLIGHT_TIME = 300f;
    /** Time when darkness starts */
    private static final float DARKNESS_THRESHOLD = 90f;
    private int debrisCollected = 0;
    private static final int MIN_DEBRIS_REQUIRED = 6;
    private boolean paused = false;
    
    // Game objects
    private final Player player;
    
    private final Chest chest;
    
    private final Flowers[][] flowers;

    // objects loaded from file
    private final List<Fence> fences;
    private final List<Debris> debris;
    private final List<Exit> exits;
    private final List<WildlifeVisitor> wildlifeVisitors;
    private final List<Fertilizer> fertilizers;
    private final List<WateringCan> wateringCans;
    private final List<Shovel> shovels;
    private final List<Grass> grass;
    private boolean hasShovel = false;
    /** if fertilizer spawned */
    private boolean questFertilizerSpawned = false;
    /** if picked up  */
    private boolean hasFertilizer = false;

    private static final int QUEST_FERTILIZER_X = 1;
    private static final int QUEST_FERTILIZER_Y = 9;

    /**
     * Constructor that loads map from MapLoader data.
     * @param game The game instance.
     * @param mapData The map data loaded from a file.
     */
    public GameMap(ValleyDayGame game, MapLoader.MapData mapData) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);

        // initializes lists
        this.fences = new ArrayList<>();
        this.debris = new ArrayList<>();
        this.exits = new ArrayList<>();
        this.wildlifeVisitors = new ArrayList<>();
        this.fertilizers = new ArrayList<>();
        this.wateringCans = new ArrayList<>();
        this.shovels = new ArrayList<>();
        this.grass = new ArrayList<>();

        // creates player at entrance position
        this.player = new Player(this.world, mapData.entranceX, mapData.entranceY);
        this.player.setHitCallback(this::onPlayerHit);

        // creates objects based on map data
        for (MapLoader.MapObject obj : mapData.objects) {
            switch (obj.type) {
                case 0: // Fence
                    fences.add(new Fence(world, obj.x, obj.y));
                    break;
                case 1: // Debris
                    debris.add(new Debris(world, obj.x, obj.y));
                    break;
                case 4: // Exit
                    exits.add(new Exit(world, obj.x, obj.y));
                    break;
                case 3: // Wildlife visitor
                    wildlifeVisitors.add(new WildlifeVisitor(obj.x, obj.y));
                    break;
                case 5: // Fertilizer, added world
                    fertilizers.add(new Fertilizer(world, obj.x, obj.y));
                    break;
                case 6: // Watering Can
                    wateringCans.add(new WateringCan(obj.x, obj.y));
                    break;
                case 7: // Shovel
                    shovels.add(new Shovel(obj.x, obj.y));
                    break;
            }
        }

        // fills all cells with grass
        // objects with transparent backgrounds have also grass
        for (int x = mapData.minX; x <= mapData.maxX; x++) {
            for (int y = mapData.minY; y <= mapData.maxY; y++) {
                grass.add(new Grass(x, y));
            }
        }

        this.chest = null;
        this.flowers = null;
    }

    /**
     * Legacy constructor for backward compatibility.
     * Creates a default map with hardcoded objects.
     * @param game The game instance.
     */
    public GameMap(ValleyDayGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);
        // Create a player with initial position (1, 3)
        this.player = new Player(this.world, 1, 3);
        // Create a chest in the middle of the map
        this.chest = new Chest(world, 3, 3);
        // Create flowers in a 7x7 grid
        this.flowers = new Flowers[7][7];
        for (int i = 0; i < flowers.length; i++) {
            for (int j = 0; j < flowers[i].length; j++) {
                this.flowers[i][j] = new Flowers(i, j);
            }
        }
        // initializes empty lists for map objects
        this.fences = new ArrayList<>();
        this.debris = new ArrayList<>();
        this.exits = new ArrayList<>();
        this.wildlifeVisitors = new ArrayList<>();
        this.fertilizers = new ArrayList<>();
        this.wateringCans = new ArrayList<>();
        this.shovels = new ArrayList<>();
        this.grass = new ArrayList<>();
    }
    
    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     * @param frameTime the time that has passed since the last update
     */
    public void tick(float frameTime) {
        //updated tick
        if (!paused) {
            this.player.tick(frameTime);
            doPhysicsStep(frameTime);

            // updates daylight timer
            daylightTimeRemaining -= frameTime;
            if (daylightTimeRemaining < 0) {
                daylightTimeRemaining = 0;
            }
        }
        //spawn fertilizer if quest completed
        maybeSpawnQuestFertilizer();
    }

    private void maybeSpawnQuestFertilizer() {
        if (questFertilizerSpawned || hasFertilizer) return;
        if (!hasShovel) return;
        if (debrisCollected < MIN_DEBRIS_REQUIRED) return;

        // spawn fertilizer
        fertilizers.add(new Fertilizer(world, QUEST_FERTILIZER_X, QUEST_FERTILIZER_Y));
        questFertilizerSpawned = true;
    }

    /**
     * Performs as many physics steps as necessary to catch up to the given frame time.
     * This will update the Box2D world by the given time step.
     * @param frameTime Time since last frame in seconds
     */
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }
    
    /** Returns the player on the map. */
    public Player getPlayer() {
        return player;
    }
    
    /** Returns the chest on the map. */
    public Chest getChest() {
        return chest;
    }
    
    /** Returns the flowers on the map. */
    public List<Flowers> getFlowers() {
        if (flowers == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(flowers).flatMap(Arrays::stream).toList();
    }

    /** Returns all drawable objects on the map. */
    public List<Drawable> getAllDrawables() {
        List<Drawable> drawables = new ArrayList<>();

        // adds grass first (background)
        drawables.addAll(grass);

        // adds all map objects
        drawables.addAll(fences);
        drawables.addAll(debris);
        drawables.addAll(exits);
        drawables.addAll(wildlifeVisitors);
        drawables.addAll(fertilizers);
        drawables.addAll(wateringCans);
        drawables.addAll(shovels);

        // adds legacy objects if they exist
        if (flowers != null) {
            drawables.addAll(getFlowers());
        }
        if (chest != null) {
            drawables.add(chest);
        }

        // adds play as last on top
        drawables.add(player);

        return drawables;
    }
     //getters
    public List<Fence> getFences() {
        return fences;
    }

    public List<Debris> getDebris() {
        return debris;
    }

    public List<Exit> getExits() {
        return exits;
    }

    public List<WildlifeVisitor> getWildlifeVisitors() {
        return wildlifeVisitors;
    }

    public List<Fertilizer> getFertilizers() {
        return fertilizers;
    }

    public List<WateringCan> getWateringCans() {
        return wateringCans;
    }

    public List<Shovel> getShovels() {
        return shovels;
    }

    public List<Grass> getGrass() {
        return grass;
    }
    /**
     * Called after a completed hit action.If debris exists-destroy it.
     * @param targetX The X coordinate of the target.
     * @param targetY The Y coordinate of the target.
     * @param direction The direction of the player.
     */
    private void onPlayerHit(float targetX, float targetY, Player.Direction direction) {
        // finds debris
        Debris toRemove = null;
        for (Debris d : debris) {
            float dx = Math.abs(d.getX() - targetX);
            float dy = Math.abs(d.getY() - targetY);

            //checks distance to target
            if (dx < 0.5f && dy < 0.5f) {
                toRemove = d;
                break;
            }
        }
        //removes debris
        if (toRemove != null) {
            toRemove.destroy();
            debris.remove(toRemove); // removes from debris list
            debrisCollected++;
        }
    }
    //getters
    public float getDaylightTimeRemaining() {
        return daylightTimeRemaining;
    }

    public String getFormattedTime() {
        int minutes = (int) (daylightTimeRemaining / 60);
        int seconds = (int) (daylightTimeRemaining % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    public int getDebrisCollected() {
        return debrisCollected;
    }

    public int getMinDebrisRequired() {
        return MIN_DEBRIS_REQUIRED;
    }

    //BONUS darkenning
    /**
     * Returns the darkness level based on remaining time.
     * Darkness increases, then stays at max
     * @return Darkness level between 0.0 and 0.7
     */
    public float getDarknessLevel() {
        if (daylightTimeRemaining <= DARKNESS_THRESHOLD) {
            return 0.7f;
        } else {
            // gradual darkening
            // calculates how much time passed
            float timeRange = MAX_DAYLIGHT_TIME - DARKNESS_THRESHOLD;
            float timeElapsed = MAX_DAYLIGHT_TIME - daylightTimeRemaining;
            float progress = timeElapsed / timeRange;

            // returns darkness level
            return 0.7f * progress;
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }
    //checks if u can exit
    public boolean canExit() {
        return debrisCollected >= MIN_DEBRIS_REQUIRED;
    }

    /**
     * Returns if the player has the shovel.
     * @return True if player has shovel, false if not.
     */
    public boolean hasShovel() {
        return hasShovel;
    }
    public boolean hasFertilizer() {
        return hasFertilizer;
    }

    /**
     * When E key pressed, picks up the closest item.
     * @return true if something picked up.
     */
    public boolean tryInteract() {
        if (tryPickupFertilizer()) return true;
        return tryPickupShovel();
    }

    /**
     * Picks up fertilizer if near the player.
     */
    public boolean tryPickupFertilizer() {
        if (hasFertilizer) return false;
        //get nearest
        Fertilizer nearest = getNearestFertilizer();
        if (nearest == null) return false;

        // remove from world and list
        nearest.destroy();
        fertilizers.remove(nearest);
        hasFertilizer = true;
        return true;
    }

    public Fertilizer getNearestFertilizer() {
        if (hasFertilizer) return null;

        float playerX = player.getX();
        float playerY = player.getY();
        Player.Direction playerDir = player.getCurrentDirection();

        for (Fertilizer fertilizer : fertilizers) {
            float dx = fertilizer.getX() - playerX;
            float dy = fertilizer.getY() - playerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 1.5f) {
                boolean facing = false;
                switch (playerDir) {
                    case UP: facing = dy > 0 && Math.abs(dy) > Math.abs(dx); break;
                    case DOWN: facing = dy < 0 && Math.abs(dy) > Math.abs(dx); break;
                    case LEFT: facing = dx < 0 && Math.abs(dx) > Math.abs(dy); break;
                    case RIGHT: facing = dx > 0 && Math.abs(dx) > Math.abs(dy); break;
                }
                if (facing) return fertilizer;
            }
        }
        return null;
    }

    /**
     * Tries to pick up a shovel infront of the player
     * when player presses E key.
     * @return True if a shovel was picked up, false if not.
     */
    public boolean tryPickupShovel() {
        if (hasShovel) return false; // if already has shovel

        //where is player
        float playerX = player.getX();
        float playerY = player.getY();
        
        // Find shovel next to player (within 1.5)
        Shovel toRemove = null;
        for (Shovel shovel : shovels) {
	// distance to the shovel
            float dx = Math.abs(shovel.getX() - playerX);
            float dy = Math.abs(shovel.getY() - playerY);
            //picks what shovel to remove
            if (dx < 1.5f && dy < 1.5f) {
                toRemove = shovel;
                break;
            }
        }
        //shovel found
        if (toRemove != null) {
            shovels.remove(toRemove);
            hasShovel = true;
            player.setShovelEquipped(true); // equipped true
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns the nearest shovel to the player if facing it.
     * @return Nearest shovel or null if none nearby.
     */
    public Shovel getNearestShovel() {
        if (hasShovel) return null; // already has shovel

        ///player coordinates
        float playerX = player.getX();
        float playerY = player.getY();
        Player.Direction playerDir = player.getCurrentDirection(); //direction of player
        
        // check if player facing a shovel 
        for (Shovel shovel : shovels) {
            float dx = shovel.getX() - playerX;
            float dy = shovel.getY() - playerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy); //distance to shovel
            
            if (distance < 1.5f) {
               boolean facing = false;
                // check if player facing the shovel
                switch (playerDir) {
                    case UP: facing = dy > 0 && Math.abs(dy) > Math.abs(dx); break;
                    case DOWN: facing = dy < 0 && Math.abs(dy) > Math.abs(dx); break;
                    case LEFT: facing = dx < 0 && Math.abs(dx) > Math.abs(dy); break;
                    case RIGHT: facing = dx > 0 && Math.abs(dx) > Math.abs(dy); break;
                }
                
                if (facing) {
                    return shovel;
                }
            }
        }
        
        return null;
    }

}
