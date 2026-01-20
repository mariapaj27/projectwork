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
                case 5: // Fertilizer
                    fertilizers.add(new Fertilizer(obj.x, obj.y));
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
        this.player.tick(frameTime);
        doPhysicsStep(frameTime);
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
        }
    }
}
