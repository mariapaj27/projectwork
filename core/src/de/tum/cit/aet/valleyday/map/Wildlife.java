package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Drawable;
import de.tum.cit.aet.valleyday.texture.SpriteSheet;

import java.util.List;

/**
 * Wildlife animal model.
 */
public class Wildlife implements Drawable {
    
    /** The speed at which wildlife moves  */
    private static final float MOVE_SPEED = 1.0f;
    
    /** Speed when scared (running away) */
    private static final float SCARED_SPEED = 3.0f;
    
    private boolean isScared = false;
    
    /** Time since wildlife was scared */
    private float scaredTime = 0f;
    
    private static final float SCARED_DURATION = 1.5f;
    
    private final Body hitbox;
    private Fixture fixture;
    private final World world;
    
    /** Position when stacked */
    private float lastX;
    private float lastY;
    private float stuckTime = 0f;
    
    /** Time for unstack */
    private static final float STUCK_THRESHOLD = 1.0f;
    
    /** Minimum distance moved to reset stuck timer */
    private static final float MOVEMENT_THRESHOLD = 0.1f;
    
    /** Current avoidance direction */
    private float avoidanceAngle = 0f;

    /** Time spent avoiding the obstacles */
    private float avoidanceTime = 0f;
    
    /** Duration to avoid before direct path  */
    private static final float AVOIDANCE_DURATION = 2.0f;
    
    /**
     * Creates a new wildlife creature at the given position.
     * @param world The Box2D world.
     * @param x The X position.
     * @param y The Y position.
     */
    public Wildlife(World world, float x, float y) {
        this.world = world;
        this.hitbox = createHitbox(world, x, y);
        this.lastX = x;
        this.lastY = y;
    }
    
    /**
     * Creates a Box2D body for the wildlife.
     * @param world The Box2D world to add the body to.
     * @param startX The initial X position.
     * @param startY The initial Y position.
     * @return The created body.
     */
    private Body createHitbox(World world, float startX, float startY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY);
        Body body = world.createBody(bodyDef);
        
        CircleShape circle = new CircleShape();
        circle.setRadius(0.3f);
        Fixture fixture = body.createFixture(circle, 1.0f);
        circle.dispose();
        
        body.setUserData(this);
        this.fixture = fixture;
        return body;
    }
    
    /**
     * Updates the wildlife's movement towards the nearest plant or player.
     * @param frameTime The time since the last frame.
     * @param plants List of all plants on the map.
     * @param playerX Player X position.
     * @param playerY Player Y position.
     * @return True if wildlife should be removed (scared for too long).
     */
    public boolean tick(float frameTime, List<Plant> plants, float playerX, float playerY) {
        if (isScared) {
            scaredTime += frameTime;
            
            // runs away from the player
            float dx = getX() - playerX;
            float dy = getY() - playerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0.1f) {
                // running away speed
                float xVelocity = (dx / distance) * SCARED_SPEED;
                float yVelocity = (dy / distance) * SCARED_SPEED;
                
                // velocity
                hitbox.setLinearVelocity(xVelocity, yVelocity);
            } else {
                hitbox.setLinearVelocity(0, 0);
            }
            if (scaredTime >= SCARED_DURATION) {
                return true; // should be removed
            }
            return false;
        }
        
        // Unstuck algorithm
        float currentX = getX();
        float currentY = getY();
        float movedDistance = (float) Math.sqrt(
            (currentX - lastX) * (currentX - lastX) + 
            (currentY - lastY) * (currentY - lastY)
        );
        
        if (movedDistance < MOVEMENT_THRESHOLD) {
            stuckTime += frameTime;
        } else {
            stuckTime = 0f;
            avoidanceTime = 0f;
            avoidanceAngle = 0f;
        }

        lastX = currentX;
        lastY = currentY;

        float targetX = 0f;
        float targetY = 0f;
        boolean hasTarget = false;
        
        // if there is  no plants, chase the player
        if (plants.isEmpty()) {
            targetX = playerX;
            targetY = playerY;
            hasTarget = true;
        } else {
            // move towards nearest plant
            Plant nearestPlant = findNearestPlant(plants);
            if (nearestPlant != null) {
                targetX = nearestPlant.getX();
                targetY = nearestPlant.getY();
                hasTarget = true;
            }
        }
        
        if (!hasTarget) {
            hitbox.setLinearVelocity(0, 0);
            return false;
        }

        float dx = targetX - currentX;
        float dy = targetY - currentY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 0.1f) {
            hitbox.setLinearVelocity(0, 0);
            return false;
        }
        
        // normal direction
        float dirX = dx / distance;
        float dirY = dy / distance;
        
        // Avoidance angle algorithm
        if (stuckTime >= STUCK_THRESHOLD) {
            avoidanceTime += frameTime;

            if (avoidanceAngle == 0f) {
                float perpX = -dirY;
                float perpY = dirX;
                
                avoidanceAngle = (float) Math.atan2(perpY, perpX);
            }
            
            float avoidX = (float) Math.cos(avoidanceAngle);
            float avoidY = (float) Math.sin(avoidanceAngle);
            
            float xVelocity = avoidX * MOVE_SPEED;
            float yVelocity = avoidY * MOVE_SPEED;
            
            hitbox.setLinearVelocity(xVelocity, yVelocity);
            
            // resets path to target
            if (avoidanceTime >= AVOIDANCE_DURATION) {
                stuckTime = 0f;
                avoidanceTime = 0f;
                avoidanceAngle = 0f;
            }
            
            return false;
        }

        float xVelocity = dirX * MOVE_SPEED;
        float yVelocity = dirY * MOVE_SPEED;
        hitbox.setLinearVelocity(xVelocity, yVelocity);
        
        return false;
    }
    
    /**
     * Finds the nearest plant .
     * @param plants List of all plants.
     * @return The nearest plant, or null if list is empty.
     */
    private Plant findNearestPlant(List<Plant> plants) {
        if (plants.isEmpty()) return null;
        
        Plant nearest = null;
        float minDistance = Float.MAX_VALUE;
        
        float wildlifeX = getX();
        float wildlifeY = getY();
        
        for (Plant plant : plants) {
            float dx = plant.getX() - wildlifeX;
            float dy = plant.getY() - wildlifeY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = plant;
            }
        }
        
        return nearest;
    }
    
    /**
     * Scares animal.
     */
    public void scare() {
        if (!isScared) {
            isScared = true;
            scaredTime = 0f;
            
            // animal can pass through objects
            if (fixture != null) {
                fixture.setSensor(true);
            }
        }
    }
    
    /**
     * Checks if this wildlife is scared.
     * @return True if wildlife is scared.
     */
    public boolean isScared() {
        return isScared;
    }
    
    /**
     * Checks if the wildlife is touching a plant.
     * @param plant The plant to check.
     * @return True if wildlife is touching the plant.
     */
    public boolean isTouchingPlant(Plant plant) {
        if (isScared) return false;
        float dx = Math.abs(getX() - plant.getX());
        float dy = Math.abs(getY() - plant.getY());
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < 0.6f;
    }
    
    /**
     * Checks if the wildlife is touching the player.
     * Only works if not scared (scared wildlife ignores player).
     * @param player The player to check.
     * @return True if wildlife is touching the player.
     */
    public boolean isTouchingPlayer(Player player) {
        if (isScared) return false;
        float dx = Math.abs(getX() - player.getX());
        float dy = Math.abs(getY() - player.getY());
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < 0.6f;
    }
    
    /**
     * Destroys the wildlife's Box2D body.
     */
    public void destroy() {
        if (hitbox != null) {
            world.destroyBody(hitbox);
        }
    }
    
    @Override
    public TextureRegion getCurrentAppearance() {
        // texture
        return SpriteSheet.ANIMALS.at(5, 1);
    }
    
    @Override
    public float getX() {
        return hitbox.getPosition().x;
    }
    
    @Override
    public float getY() {
        return hitbox.getPosition().y;
    }
}
