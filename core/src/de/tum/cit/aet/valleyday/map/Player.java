package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import de.tum.cit.aet.valleyday.texture.Animations;
import de.tum.cit.aet.valleyday.texture.Drawable;

/**
 * Represents the player character in the game.
 * The player has a hitbox, so it can collide with other objects in the game.
 */
public class Player implements Drawable {


    /** Total time elapsed since the game started. We use this for calculating the player movement and animating it. */
    private float elapsedTime;
    
    /** The Box2D hitbox of the player, used for position and collision detection. */
    private final Body hitbox;

    // speed of the move
    private static final float MOVE_SPEED = 4.0f;
    //
    private Direction currentDirection = Direction.DOWN;

    private boolean isHitting = false;
    private float hittingTime = 0f;
    private static final float BASE_HITTING_DURATION = 1.0f;
    private static final float SHOVEL_HITTING_DURATION = 0.5f;
    //Action after hitting completed
    private HitCallback hitCallback;
    private boolean hasShovel = false;

    private boolean isShouting = false;
    private float shoutingTime = 0f;
    private static final float SHOUTING_DURATION = 1.0f;
    private ShoutCallback shoutCallback;

    /**
     * Interface for hit callback.
     */
    public interface HitCallback {
        void onHitComplete(float targetX, float targetY, Direction direction);
    }
    /**
     * Interface for shout callback.
     */
    public interface ShoutCallback {
        void onShoutComplete(float playerX, float playerY);
    }

    public void setShoutCallback(ShoutCallback callback) {
        this.shoutCallback = callback;
    }
    /**
     * Gets the current hitting duration based on if player has shovel.
     * @return Hitting duration in seconds.
     */
    private float getHittingDuration() {
        return hasShovel ? SHOVEL_HITTING_DURATION : BASE_HITTING_DURATION;
    }
    public Player(World world, float x, float y) {
        this.hitbox = createHitbox(world, x, y);
    }

    public void setShovelEquipped(boolean equipped) {
        this.hasShovel = equipped;
    }
    
    /**
     * Creates a Box2D body for the player.
     * This is what the physics engine uses to move the player around and detect collisions with other bodies.
     * @param world The Box2D world to add the body to.
     * @param startX The initial X position.
     * @param startY The initial Y position.
     * @return The created body.
     */
    private Body createHitbox(World world, float startX, float startY) {
        // BodyDef is like a blueprint for the movement properties of the body.
        BodyDef bodyDef = new BodyDef();
        // Dynamic bodies are affected by forces and collisions.
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set the initial position of the body.
        bodyDef.position.set(startX, startY);
        // Create the body in the world using the body definition.
        Body body = world.createBody(bodyDef);
        // Now we need to give the body a shape so the physics engine knows how to collide with it.
        // We'll use a circle shape for the player.
        CircleShape circle = new CircleShape();
        // Give the circle a radius of 0.3 tiles (the player is 0.6 tiles wide).
        circle.setRadius(0.3f);
        // Attach the shape to the body as a fixture.
        // Bodies can have multiple fixtures, but we only need one for the player.
        body.createFixture(circle, 1.0f);
        // We're done with the shape, so we should dispose of it to free up memory.
        circle.dispose();
        // Set the player as the user data of the body so we can look up the player from the body later.
        body.setUserData(this);
        return body;
    }
    
    /**
     * Move the player around in a circle by updating the linear velocity of its hitbox every frame.
     * This doesn't actually move the player, but it tells the physics engine how the player should move next frame.
     * @param frameTime the time since the last frame.
     */
    public void tick(float frameTime) {
        // Check if S key is pressed
        boolean sPressed = Gdx.input.isKeyJustPressed(Input.Keys.S);

        if (sPressed && !isShouting && !isHitting) {
            // Start shouting when S is pressed
            isShouting = true;
            shoutingTime = 0f;
        }

        //  shouting state
        if (isShouting) {
            shoutingTime += frameTime;

            // Stop player while shouting
            this.hitbox.setLinearVelocity(0, 0);

            // Check if shouting  complete
            if (shoutingTime >= SHOUTING_DURATION) {
                if (shoutCallback != null) {
                    shoutCallback.onShoutComplete(getX(), getY());
                }
                // Reset
                isShouting = false;
                shoutingTime = 0f;
            }

            return;
        }
           //if D key is pressed
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.D);
        
        if (dPressed && !isHitting) {
            isHitting = true;
            hittingTime = 0f;
        }
        
        if (isHitting) {
            // if D key not pressed - cancel the hit
            if (!dPressed) {
                isHitting = false;
                hittingTime = 0f;
            } else {
                //continue hitting
                hittingTime += frameTime;
                
                // no player movement while hitting
                this.hitbox.setLinearVelocity(0, 0);

                float hitDuration = getHittingDuration();
                if (hittingTime >= hitDuration)  {
                    float targetX = getX();
                    float targetY = getY();
                  
                    //get target position based on player direction
                    switch (currentDirection) {
                        case UP:
                            targetY += 1;
                            break;
                        case DOWN:
                            targetY -= 1;
                            break;
                        case LEFT:
                            targetX -= 1;
                            break;
                        case RIGHT:
                            targetX += 1;
                            break;
                    }
                    
                    if (hitCallback != null) {
                        hitCallback.onHitComplete(targetX, targetY, currentDirection);
                    }
                    
                    // resets hitting state
                    isHitting = false;
                    hittingTime = 0f;
                }
                
                return;
            }
        }

        //check arrow keys for input
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean upPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean downPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        // coordinates of movement
        float xVelocity = 0;
        float yVelocity = 0;

        //calculate velocity based on keys
        if (leftPressed) {
            xVelocity -= MOVE_SPEED;
            currentDirection = Direction.LEFT;
        }
        if (rightPressed) {
            xVelocity += MOVE_SPEED;
            currentDirection = Direction.RIGHT;
        }
        if (downPressed) {
            yVelocity -= MOVE_SPEED;
            currentDirection = Direction.DOWN;
        }
        if (upPressed) {
            yVelocity += MOVE_SPEED;
            currentDirection = Direction.UP;
        }
        //BONUS DIAGONAL MOVEMENT
        //diagonal movement(two buttons pressed together)
        if (xVelocity != 0 && yVelocity != 0) {
            float length = (float) Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity);
            xVelocity = (xVelocity / length) * MOVE_SPEED;
            yVelocity = (yVelocity / length) * MOVE_SPEED;
        }

        // Update animation only when moving
        if (xVelocity != 0 || yVelocity != 0) {
            this.elapsedTime += frameTime;
        } else {
            this.elapsedTime = 0;
        }

        this.hitbox.setLinearVelocity(xVelocity, yVelocity);
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }
    public boolean isHitting() {
        return isHitting;
    }

    public boolean isShouting() {
        return isShouting;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        // chooses animation based on player's action
        Animation<TextureRegion> animation;

        if (isShouting || isHitting) {
            // chooses attack animation
            animation = switch (currentDirection) {
                case UP -> Animations.CHARACTER_ATTACK_UP;
                case LEFT -> Animations.CHARACTER_ATTACK_LEFT;
                case RIGHT -> Animations.CHARACTER_ATTACK_RIGHT;
                case DOWN -> Animations.CHARACTER_ATTACK_DOWN;
            };
            float animTime = isShouting ? shoutingTime : hittingTime;
            return animation.getKeyFrame(animTime, false);
        } else {
            // chooses walk animation
            animation = switch (currentDirection) {
                case UP -> Animations.CHARACTER_WALK_UP;
                case LEFT -> Animations.CHARACTER_WALK_LEFT;
                case RIGHT -> Animations.CHARACTER_WALK_RIGHT;
                case DOWN -> Animations.CHARACTER_WALK_DOWN;
            };
            return animation.getKeyFrame(this.elapsedTime, true);
        }

    }

    
    @Override
    public float getX() {
        // The x-coordinate of the player is the x-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().x;
    }
    
    @Override
    public float getY() {
        // The y-coordinate of the player is the y-coordinate of the hitbox (this can change every frame).
        return hitbox.getPosition().y;
    }

    /**
     * Enum of movement directions for the player.
     */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public void setHitCallback(HitCallback hitCallback) {
        this.hitCallback = hitCallback;
    }

}

