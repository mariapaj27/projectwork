package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.ValleyDayGame;
import de.tum.cit.aet.valleyday.texture.Drawable;
import java.util.HashSet;
import java.util.Random;

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

    private boolean questSeedsSpawned = false;
    private int seedsCollected = 0;
    private static final int QUEST_SEED_COUNT = 6;
    private static final float SEED_MIN_DISTANCE_FROM_PLAYER = 6.0f;
    // seed spawning criteria
    private int mapMinX = 0;
    private int mapMaxX = 6;
    private int mapMinY = 0;
    private int mapMaxY = 6;

    private boolean gameLost = false;
    private static final int WILDLIFE_COUNT = 3;
    private static final float WILDLIFE_MIN_DISTANCE_FROM_PLANTS = 6.0f;
    private static final float PERIODIC_SPAWN_START_TIME = 180f;
    private static final float PERIODIC_SPAWN_INTERVAL = 15f;
    private float lastPeriodicSpawnTime = MAX_DAYLIGHT_TIME;

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
    private final List<Seed> seeds;
    private final List<GardenBed> gardenBeds;
    private final List<Plant> plants;
    private final List<Wildlife> wildlife;

    private boolean hasShovel = false;
    /** if fertilizer spawned */
    private boolean questFertilizerSpawned = false;
    /** if picked up  */
    private boolean hasFertilizer = false;

    private static final int QUEST_FERTILIZER_X = 1;
    private static final int QUEST_FERTILIZER_Y = 9;

    private boolean hasWateringCan = false;
    private float[] hiddenWateringCanDebrisPos = null;

    private int plantsCollected = 0;
    private float gameTime = 0f;

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
        this.mapMinX = mapData.minX;
        this.mapMaxX = mapData.maxX;
        this.mapMinY = mapData.minY;
        this.mapMaxY = mapData.maxY;
        this.seeds = new ArrayList<>();
        this.gardenBeds = new ArrayList<>();
        this.plants = new ArrayList<>();
        this.wildlife = new ArrayList<>();
        // creates player at entrance position
        this.player = new Player(this.world, mapData.entranceX, mapData.entranceY);
        this.player.setHitCallback(this::onPlayerHit);
        this.player.setShoutCallback(this::onPlayerShout);

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
                    wateringCans.add(new WateringCan(world, obj.x, obj.y));
                    break;
                case 7: // Shovel
                    shovels.add(new Shovel(obj.x, obj.y));
                    break;
                case 9: // Garden Bed
                    gardenBeds.add(new GardenBed(obj.x, obj.y));
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
        // hides water can under a random debris
        hideWateringCanBehindRandomDebris();

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
        this.seeds = new ArrayList<>();
        this.gardenBeds = new ArrayList<>();
        this.plants = new ArrayList<>();
        this.wildlife = new ArrayList<>();
        this.player.setShoutCallback(this::onPlayerShout);


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
            // updates game time
            gameTime += frameTime;

            // updates plant growth stages
            for (Plant plant : plants) {
                plant.update(gameTime);
            }
            if (!gameLost) {
                float playerX = player.getX();
                float playerY = player.getY();

                // Update wildlife and remove.
                List<Wildlife> toRemove = new ArrayList<>();
                for (Wildlife w : wildlife) {
                    if (w.tick(frameTime, plants, playerX, playerY)) {
                        toRemove.add(w);
                    }
                }

                // Remove wildlife that ran away
                for (Wildlife w : toRemove) {
                    w.destroy();
                    wildlife.remove(w);
                }
                //if wildlife hit plant or player
                checkWildlifeCollisions();
            }
            // updates daylight timer
            daylightTimeRemaining -= frameTime;
            if (daylightTimeRemaining < 0) {
                daylightTimeRemaining = 0;
            }
            // Periodic wildlife spawn from 3:00 and every 15 seconds.
            if (daylightTimeRemaining <= PERIODIC_SPAWN_START_TIME) {
                float timeSinceLastSpawn = lastPeriodicSpawnTime - daylightTimeRemaining;
                if (timeSinceLastSpawn >= PERIODIC_SPAWN_INTERVAL) {
                    spawnPeriodicWildlife();
                    lastPeriodicSpawnTime = daylightTimeRemaining;
                }
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
        drawables.addAll(seeds);
        drawables.addAll(gardenBeds);
        drawables.addAll(plants);
        drawables.addAll(wildlife);

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
            float debrisX = toRemove.getX();
            float debrisY = toRemove.getY();
            // checks if debris had a hidden watering can
            if (hiddenWateringCanDebrisPos != null &&
                    Math.abs(hiddenWateringCanDebrisPos[0] - debrisX) < 0.1f &&
                    Math.abs(hiddenWateringCanDebrisPos[1] - debrisY) < 0.1f) {
                // spawn watering can
                wateringCans.add(new WateringCan(world, debrisX, debrisY));
                hiddenWateringCanDebrisPos = null; // watering can spawned
            }
            toRemove.destroy();
            debris.remove(toRemove); // removes from debris list
            debrisCollected++;
        }
        Seed seedToRemove = null;
        for (Seed s : seeds) {
            float dx = Math.abs(s.getX() - targetX);
            float dy = Math.abs(s.getY() - targetY);
            if (dx < 0.5f && dy < 0.5f) {
                seedToRemove = s;
                break;
            }
        }
        if (seedToRemove != null) {
            seedToRemove.destroy();
            seeds.remove(seedToRemove);
            seedsCollected++;
        }
    }
    /**
     * Called after player shouted.
     * @param playerX The X coordinate of the player.
     * @param playerY The Y coordinate of the player.
     */
    private void onPlayerShout(float playerX, float playerY) {
        float scareRadius = 2.0f; // radius of the scare

        for (Wildlife w : wildlife) {
            if (w.isScared()) continue; //if already scared

            float dx = w.getX() - playerX;
            float dy = w.getY() - playerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= scareRadius) { // if in the radius
                w.scare();
            }
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
    public int getSeedsCollected() {
        return seedsCollected;
    }

    public int getPlantsCollected() {
        return plantsCollected;
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

    public boolean hasWateringCan() {
        return hasWateringCan;
    }

    /**
     * When E key pressed, picks up the closest item.
     * @return true if something picked up.
     */
    public boolean tryInteract() {
        if (tryPickupFertilizer()) return true;
        if (tryPickupWateringCan()) return true;
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
        // when fertilizer is picked up, add pickable seeds on map
        spawnQuestSeedsIfNeeded();
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

    private void spawnQuestSeedsIfNeeded() {
        if (questSeedsSpawned) return;
        if (!hasFertilizer) return;

        // checks for not empty points
        HashSet<Long> occupied = new HashSet<>();
        addOccupied(occupied, fences);
        addOccupied(occupied, debris);
        addOccupied(occupied, exits);
        addOccupied(occupied, wildlifeVisitors);
        addOccupied(occupied, fertilizers);
        addOccupied(occupied, wateringCans);
        addOccupied(occupied, shovels);
        addOccupied(occupied, seeds);

        int playerTileX = Math.round(player.getX());
        int playerTileY = Math.round(player.getY());

        List<int[]> candidatesFar = new ArrayList<>();
        List<int[]> candidatesAny = new ArrayList<>();
        //spawn seeds in specific distance from player
        for (int x = mapMinX; x <= mapMaxX; x++) {
            for (int y = mapMinY; y <= mapMaxY; y++) {
                long key = pack(x, y);
                if (occupied.contains(key)) continue;
                candidatesAny.add(new int[]{x, y}); // empty point
                //check distance
                float dx = x - playerTileX;
                float dy = y - playerTileY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist >= SEED_MIN_DISTANCE_FROM_PLAYER) {
                    candidatesFar.add(new int[]{x, y});
                }
            }
        }

        Random rnd = new Random();
        int spawned = 0;

        // prefer far points
        while (spawned < QUEST_SEED_COUNT && (!candidatesFar.isEmpty() || !candidatesAny.isEmpty())) {
            List<int[]> pool = !candidatesFar.isEmpty() ? candidatesFar : candidatesAny;
            int idx = rnd.nextInt(pool.size());
            int[] pos = pool.remove(idx);

            // remove points from candidates
            removePos(candidatesAny, pos[0], pos[1]);
            removePos(candidatesFar, pos[0], pos[1]);

            seeds.add(new Seed(world, pos[0], pos[1]));
            spawned++;
        }
        questSeedsSpawned = true;
    }

    /**
     * Helper method to remove used points from list.
     */
    private static void removePos(List<int[]> list, int x, int y) {
        for (int i = 0; i < list.size(); i++) {
            int[] p = list.get(i);
            if (p[0] == x && p[1] == y) {
                list.remove(i);
                return;
            }
        }
    }

    /**
     * Helper method to create key of object on the coordinate
     */
    private static long pack(int x, int y) {
        return (((long) x) << 32) ^ (y & 0xffffffffL);
    }

    /**
     * Marks that point on the map is occupied.
     */
    private static void addOccupied(HashSet<Long> occupied, List<? extends Drawable> objects) {
        for (Drawable d : objects) {
            int x = Math.round(d.getX());
            int y = Math.round(d.getY());
            occupied.add(pack(x, y));
        }
    }
    /**
     * Hides the water can under a random debris.
     */
    private void hideWateringCanBehindRandomDebris() {
        if (debris.isEmpty()) return; // no debris
        Random rnd = new Random();
        Debris selectedDebris = debris.get(rnd.nextInt(debris.size())); //random debris
        hiddenWateringCanDebrisPos = new float[]{selectedDebris.getX(), selectedDebris.getY()};
    }

    /**
     * Tries to pick up a watering can next to the player.
     */
    public boolean tryPickupWateringCan() {
        if (hasWateringCan) return false;

        WateringCan nearest = getNearestWateringCan();
        if (nearest == null) return false;

        // removes from world,pick up
        nearest.destroy();
        wateringCans.remove(nearest);
        hasWateringCan = true;
        return true;
    }

    /**
     * Checks if watering can is ext to the player
     * @return nearest watering can
     */
    public WateringCan getNearestWateringCan() {
        if (hasWateringCan) return null;

        float playerX = player.getX();
        float playerY = player.getY();
        Player.Direction playerDir = player.getCurrentDirection();

        for (WateringCan wateringCan : wateringCans) {
            float dx = wateringCan.getX() - playerX;
            float dy = wateringCan.getY() - playerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 1.5f) {
                boolean facing = false;
                switch (playerDir) {
                    case UP: facing = dy > 0 && Math.abs(dy) > Math.abs(dx); break;
                    case DOWN: facing = dy < 0 && Math.abs(dy) > Math.abs(dx); break;
                    case LEFT: facing = dx < 0 && Math.abs(dx) > Math.abs(dy); break;
                    case RIGHT: facing = dx > 0 && Math.abs(dx) > Math.abs(dy); break;
                }

                if (facing) return wateringCan;
            }
        }

        return null;
    }
    /**
     * Plant actions.
     * Called when player presses 'A' key.
     * @return True if an action was performed.
     */
    public boolean tryPlantAction() {
        float playerX = player.getX();
        float playerY = player.getY();
        Player.Direction playerDir = player.getCurrentDirection();

        // gets target position
        float targetX = playerX;
        float targetY = playerY;
        switch (playerDir) {
            case UP: targetY += 1.0f; break;
            case DOWN: targetY -= 1.0f; break;
            case LEFT: targetX -= 1.0f; break;
            case RIGHT: targetX += 1.0f; break;
        }
        // checks if there's a plant
        Plant plantAtPos = getPlantAt(targetX, targetY);
        if (plantAtPos != null) {
            // if fully grown, tries to harvest
            if (plantAtPos.canHarvest()) {
                plants.remove(plantAtPos);
                plantsCollected++;
                return true;
            }
            // restores rotten plant if has watering can
            if (plantAtPos.canRestore() && hasWateringCan) {
                plantAtPos.restore(gameTime);
                return true;
            }
            return false; // plant is still growing, no action
        }

        // checks if is garden bed
        GardenBed bedAtPos = getGardenBedAt(targetX, targetY);
        if (bedAtPos != null) {
            // checks if there is no plant
            if (plantAtPos == null && seedsCollected > 0) {
                //plants a seed
                plants.add(new Plant(targetX, targetY, gameTime));
                seedsCollected--;
                spawnWildlifeIfNeeded();
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the plant at the specified position.
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return The plant at the position, or null if none.
     */
    private Plant getPlantAt(float x, float y) {
        for (Plant plant : plants) {
            float dx = Math.abs(plant.getX() - x);
            float dy = Math.abs(plant.getY() - y);
            if (dx < 0.5f && dy < 0.5f) {
                return plant;
            }
        }
        return null;
    }

    /**
     * Gets the garden bed at the specified position.
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return The garden bed at the position, or null if none.
     */
    private GardenBed getGardenBedAt(float x, float y) {
        for (GardenBed bed : gardenBeds) {
            float dx = Math.abs(bed.getX() - x);
            float dy = Math.abs(bed.getY() - y);
            if (dx < 0.5f && dy < 0.5f) {
                return bed;
            }
        }
        return null;
    }

    /**
     * Gets the nearest plantable garden bed or harvestable plant in front of player.
     * @return Information about what action is available, or null.
     */
    public PlantActionInfo getNearestPlantAction() {
        float playerX = player.getX();
        float playerY = player.getY();
        Player.Direction playerDir = player.getCurrentDirection();

        // gets target position
        float targetX = playerX;
        float targetY = playerY;
        switch (playerDir) {
            case UP: targetY += 1.0f; break;
            case DOWN: targetY -= 1.0f; break;
            case LEFT: targetX -= 1.0f; break;
            case RIGHT: targetX += 1.0f; break;
        }

        Plant plant = getPlantAt(targetX, targetY);
        if (plant != null) {
            if (plant.canHarvest()) {
                return new PlantActionInfo(PlantActionInfo.ActionType.HARVEST, targetX, targetY);
            }
            if (plant.canRestore() && hasWateringCan) {
                return new PlantActionInfo(PlantActionInfo.ActionType.RESTORE, targetX, targetY);
            }
        }

        GardenBed bed = getGardenBedAt(targetX, targetY);
        if (bed != null && plant == null && seedsCollected > 0) {
            return new PlantActionInfo(PlantActionInfo.ActionType.PLANT, targetX, targetY);
        }

        return null;
    }

    /**
     * Information about current plant action.
     */
    public static class PlantActionInfo {
        public enum ActionType {
            PLANT, HARVEST, RESTORE
        }

        public final ActionType type;
        public final float x;
        public final float y;

        public PlantActionInfo(ActionType type, float x, float y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }
    /**
     * Spawns wildlife creatures after a plant is planted.
     */
    private void spawnWildlifeIfNeeded() {
        if (plants.isEmpty()) return; // wait until one plant is planted.

        // Gets occupied cells
        HashSet<Long> occupied = new HashSet<>();
        addOccupied(occupied, fences);
        addOccupied(occupied, debris);
        addOccupied(occupied, exits);
        addOccupied(occupied, wildlifeVisitors);
        addOccupied(occupied, fertilizers);
        addOccupied(occupied, wateringCans);
        addOccupied(occupied, shovels);
        addOccupied(occupied, seeds);
        addOccupied(occupied, plants);
        addOccupied(occupied, gardenBeds);
        addOccupied(occupied, wildlife);

        // gets plant positions to check the distance .
        List<float[]> plantPositions = new ArrayList<>();
        for (Plant plant : plants) {
            plantPositions.add(new float[]{plant.getX(), plant.getY()});
        }

        List<int[]> candidatesFar = new ArrayList<>();
        List<int[]> candidatesAny = new ArrayList<>();

        for (int x = mapMinX; x <= mapMaxX; x++) {
            for (int y = mapMinY; y <= mapMaxY; y++) {
                long key = pack(x, y);
                if (occupied.contains(key)) continue;

                // checks distance from plants
                boolean farEnough = true;
                for (float[] plantPos : plantPositions) {
                    float dx = x - plantPos[0];
                    float dy = y - plantPos[1];
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist < WILDLIFE_MIN_DISTANCE_FROM_PLANTS) {
                        farEnough = false;
                        break;
                    }
                }
                if (farEnough) {
                    candidatesFar.add(new int[]{x, y});
                } else {
                    candidatesAny.add(new int[]{x, y});
                }
            }
        }

        Random rnd = new Random();
        int spawned = 0;

        // prefer far cells
        while (spawned < WILDLIFE_COUNT && (!candidatesFar.isEmpty() || !candidatesAny.isEmpty())) {
            List<int[]> pool = !candidatesFar.isEmpty() ? candidatesFar : candidatesAny;
            int idx = rnd.nextInt(pool.size());
            int[] pos = pool.remove(idx);

            // Remove from list to avoid duplicates
            removePos(candidatesAny, pos[0], pos[1]);
            removePos(candidatesFar, pos[0], pos[1]);

            wildlife.add(new Wildlife(world, pos[0], pos[1]));
            spawned++;
        }
    }

    /**
     * Spawns every 15 seconds after 3:00.
     */
    private void spawnPeriodicWildlife() {
        HashSet<Long> occupied = new HashSet<>();
        addOccupied(occupied, fences);
        addOccupied(occupied, debris);
        addOccupied(occupied, exits);
        addOccupied(occupied, wildlifeVisitors);
        addOccupied(occupied, fertilizers);
        addOccupied(occupied, wateringCans);
        addOccupied(occupied, shovels);
        addOccupied(occupied, seeds);
        addOccupied(occupied, plants);
        addOccupied(occupied, gardenBeds);
        addOccupied(occupied, wildlife);

        List<float[]> plantPositions = new ArrayList<>();
        for (Plant plant : plants) {
            plantPositions.add(new float[]{plant.getX(), plant.getY()});
        }

        List<int[]> candidatesFar = new ArrayList<>();
        List<int[]> candidatesAny = new ArrayList<>();

        for (int x = mapMinX; x <= mapMaxX; x++) {
            for (int y = mapMinY; y <= mapMaxY; y++) {
                long key = pack(x, y);
                if (occupied.contains(key)) continue;

                boolean farEnough = true;
                if (!plantPositions.isEmpty()) {
                    for (float[] plantPos : plantPositions) {
                        float dx = x - plantPos[0];
                        float dy = y - plantPos[1];
                        float dist = (float) Math.sqrt(dx * dx + dy * dy);
                        if (dist < WILDLIFE_MIN_DISTANCE_FROM_PLANTS) {
                            farEnough = false;
                            break;
                        }
                    }
                }

                if (farEnough && !plantPositions.isEmpty()) {
                    candidatesFar.add(new int[]{x, y});
                } else {
                    candidatesAny.add(new int[]{x, y});
                }
            }
        }

        Random rnd = new Random();
        int spawned = 0;

        while (spawned < WILDLIFE_COUNT && (!candidatesFar.isEmpty() || !candidatesAny.isEmpty())) {
            List<int[]> pool = !candidatesFar.isEmpty() ? candidatesFar : candidatesAny;
            int idx = rnd.nextInt(pool.size());
            int[] pos = pool.remove(idx);

            removePos(candidatesAny, pos[0], pos[1]);
            removePos(candidatesFar, pos[0], pos[1]);

            wildlife.add(new Wildlife(world, pos[0], pos[1]));
            spawned++;
        }

    }

    /**
     * Checks if wildlife touched plants/player.
     */
    private void checkWildlifeCollisions() {
        if (gameLost) return; // already lost

        for (Wildlife w : wildlife) {
            // Check if touched plants
            if (!plants.isEmpty()) {
                for (Plant plant : new ArrayList<>(plants)) {
                    if (w.isTouchingPlant(plant)) {
                        // if wildlife touched plant -> game over
                        plants.remove(plant);
                        gameLost = true;
                        return;
                    }
                }
            }

            // Check if touched thr player
            if (w.isTouchingPlayer(player)) {
                // if wildlife touched player -> game over
                gameLost = true;
                return;
            }
        }
    }

    public boolean isGameLost() {
        return gameLost;
    }



}