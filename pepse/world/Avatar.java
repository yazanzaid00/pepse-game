package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Player's avatar with movement, energy management and collision handling capabilities.
 */
public class Avatar extends GameObject {
	
	private static final float GRAVITY = 700.0f;
	private static final float AVATAR_MASS = 0.001f;
	private static final float HORIZONTAL_SPEED = 200.0f;
	private static final float JUMP_SPEED = 475.0f;
	
	private static final float ENERGY_GAIN_IDLE = 1.0f;
	private static final float ENERGY_LOSS_RUN = 0.5f;
	private static final float ENERGY_LOSS_JUMP = 10.0f;
	private static final float MAX_ENERGY = 100.0f;
	private static final float MIN_ENERGY = 0.0f;
	
	private static final Vector2 DEFAULT_AVATAR_DIMENSIONS = new Vector2(29.0f, 59.0f);
	
	// Idle animation uses 12 frames (example)
	private static final String[] IDLE_FRAMES = { "assets/Idle-hero01_001.png", "assets/Idle-hero01_002.png",
			"assets/Idle-hero01_003.png", "assets/Idle-hero01_004.png", "assets/Idle-hero01_005.png",
			"assets/Idle-hero01_006.png", "assets/Idle-hero01_007.png", "assets/Idle-hero01_008.png",
			"assets/Idle-hero01_009.png", "assets/Idle-hero01_010.png", "assets/Idle-hero01_011.png",
			"assets/Idle-hero01_012.png" };
	// Run animation uses 7 frames
	private static final String[] RUN_FRAMES = { "assets/Run-hero01_001.png", "assets/Run-hero01_002.png",
			"assets/Run-hero01_003.png", "assets/Run-hero01_004.png", "assets/Run-hero01_005.png",
			"assets/Run-hero01_006.png", "assets/Run-hero01_007.png" };
	// Jump animation uses 9 frames
	private static final String[] JUMP_FRAMES = { "assets/Jump-hero01_001.png", "assets/Jump-hero01_002.png",
			"assets/Jump-hero01_003.png", "assets/Jump-hero01_004.png", "assets/Jump-hero01_005.png",
			"assets/Jump-hero01_006.png", "assets/Jump-hero01_007.png", "assets/Jump-hero01_008.png",
			"assets/Jump-hero01_009.png" };
	
	private static final double IDLE_FRAME_DURATION = 0.08;
	private static final double RUN_FRAME_DURATION = 0.10;
	private static final double JUMP_FRAME_DURATION = 0.125;
	
	private static final String GROUND_TAG = "ground";
	private static final String TRUNK_TAG = "trunk";
	
	// Key constants
	private static final int MOVE_LEFT = KeyEvent.VK_LEFT;
	private static final int MOVE_RIGHT = KeyEvent.VK_RIGHT;
	private static final int JUMP = KeyEvent.VK_SPACE;
	
	private static final String AVATAR_TAG = "avatar";
	private static final double EPSILON = 1.0e-6;
	
	private final UserInputListener inputListener;
	private final List< JumpListener > jumpListeners = new ArrayList<>();
	private int groundContacts = 0;
	private boolean onGround = true;
	private float energy;
	
	// Renderables for the three states
	private Renderable idleRenderable;
	private AnimationRenderable runRenderable;
	private AnimationRenderable jumpRenderable;
	private AvatarState currentState;
	
	/**
	 * Creates an avatar at specified position with input and animation capabilities.
	 *
	 * @param topLeftCorner Initial position
	 * @param inputListener Keyboard input handler
	 * @param imageReader   Loads animation frames
	 */
	public Avatar(Vector2 topLeftCorner, UserInputListener inputListener, ImageReader imageReader) {
		super(topLeftCorner, Avatar.DEFAULT_AVATAR_DIMENSIONS, null);
		this.inputListener = inputListener;
		this.energy = Avatar.MAX_ENERGY;
		this.setTag(Avatar.AVATAR_TAG);
		
		// Setup physics
		this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
		this.physics().setMass(Avatar.AVATAR_MASS);
		this.transform().setAccelerationY(Avatar.GRAVITY);
		
		// Initialize animations
		this.initAnimations(imageReader);
		this.renderer().setRenderable(this.idleRenderable);
		this.currentState = AvatarState.IDLE;
	}
	
	/**
	 * Adds a JumpListener to be notified when the avatar jumps.
	 *
	 * @param listener The listener to add.
	 */
	public void addJumpListener(JumpListener listener) {
		this.jumpListeners.add(listener);
	}
	
	/**
	 * Removes a JumpListener.
	 *
	 * @param listener The listener to remove.
	 */
	private void removeJumpListener(JumpListener listener) {
		this.jumpListeners.remove(listener);
	}
	
	/**
	 * Gets current energy level.
	 *
	 * @return Energy value between MIN_ENERGY and MAX_ENERGY
	 */
	public double getEnergyLevel() {
		return this.energy;
	}
	
	/**
	 * Increases energy by given amount, capped at MAX_ENERGY.
	 *
	 * @param amount The amount to add.
	 */
	public void addEnergy(float amount) {
		this.energy = Math.min(Avatar.MAX_ENERGY, this.energy + amount);
	}
	
	/**
	 * Reduces the avatar's energy.
	 */
	private void reduceEnergy(float amount) {
		this.energy = Math.max(Avatar.MIN_ENERGY, this.energy - amount);
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		this.handleHorizontalMovement(deltaTime);
		this.handleJump();
		this.handleIdleEnergyGain(deltaTime);
		this.handleAnimation();
	}
	
	/**
	 * Handles collision entry events.
	 * Increments ground contacts and sets onGround to true if the avatar collides
	 * with the ground or trunk block.
	 * Stops vertical movement upon collision.
	 */
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		super.onCollisionEnter(other, collision);
		if (( Avatar.GROUND_TAG.equals(other.getTag()) || Avatar.TRUNK_TAG.equals(other.getTag()) ) &&
				    ( collision.getNormal().y() < 0 )) {
			
			this.groundContacts++;
			this.onGround = this.groundContacts > 0;
			
			// Reset vertical velocity to prevent sinking into the ground
			this.transform().setVelocityY(0);
		}
	}
	
	/**
	 * Handles collision exit events.
	 * Decrements ground contacts and sets onGround to true if the avatar is still in contact
	 * with the ground or trunk block.
	 * Ensures groundContacts does not go below zero.
	 */
	@Override
	public void onCollisionExit(GameObject other) {
		super.onCollisionExit(other);
		if (Avatar.GROUND_TAG.equals(other.getTag()) || Avatar.TRUNK_TAG.equals(other.getTag())) {
			this.groundContacts--;
			if (this.groundContacts < 0) {
				this.groundContacts = 0;
			}
			this.onGround = ( this.groundContacts > 0 );
		}
	}
	
	/**
	 * Initializes the animations using paths.
	 */
	private void initAnimations(ImageReader imageReader) {
		this.idleRenderable = new AnimationRenderable(Avatar.IDLE_FRAMES, imageReader, true,
				Avatar.IDLE_FRAME_DURATION);
		this.runRenderable = new AnimationRenderable(Avatar.RUN_FRAMES, imageReader, true,
				Avatar.RUN_FRAME_DURATION);
		this.jumpRenderable = new AnimationRenderable(Avatar.JUMP_FRAMES, imageReader, true,
				Avatar.JUMP_FRAME_DURATION);
	}
	
	private void handleHorizontalMovement(float deltaTime) {
		if (this.energy < Avatar.ENERGY_LOSS_RUN) {
			this.transform().setVelocityX(0.0f);
			return;
		}
		float vx = 0.0f;
		if (this.inputListener.isKeyPressed(Avatar.MOVE_LEFT)) {
			vx -= Avatar.HORIZONTAL_SPEED;
		}
		if (this.inputListener.isKeyPressed(Avatar.MOVE_RIGHT)) {
			vx += Avatar.HORIZONTAL_SPEED;
		}
		if (Math.abs(vx) >= Avatar.HORIZONTAL_SPEED) {
			this.reduceEnergy(Avatar.ENERGY_LOSS_RUN * deltaTime);
			this.transform().setVelocityX(vx);
			this.renderer().setIsFlippedHorizontally(vx < 0);
		} else {
			this.transform().setVelocityX(0);
		}
	}
	
	private void handleJump() {
		final boolean onSolidGround = this.onGround && Math.abs(this.getVelocity().y()) <= Avatar.EPSILON;
		if (this.inputListener.isKeyPressed(Avatar.JUMP) && onSolidGround &&
				    this.energy >= Avatar.ENERGY_LOSS_JUMP) {
			this.transform().setVelocityY(- Avatar.JUMP_SPEED);
			this.reduceEnergy(Avatar.ENERGY_LOSS_JUMP);
			for ( JumpListener listener: this.jumpListeners ) {
				listener.onJump();
			}
		}
	}
	
	private void handleIdleEnergyGain(float deltaTime) {
		// Left XOR Right -> true if only one is pressed
		final boolean isMovingHorizontally = this.inputListener.isKeyPressed(Avatar.MOVE_LEFT) ^
				                                     this.inputListener.isKeyPressed(Avatar.MOVE_RIGHT);
		if (this.determineState() == AvatarState.IDLE && ! isMovingHorizontally) {
			this.addEnergy(Avatar.ENERGY_GAIN_IDLE * deltaTime);
		}
	}
	
	private AvatarState determineState() {
		if (Math.abs(this.getVelocity().y()) > Avatar.EPSILON && ! this.onGround) {
			return AvatarState.JUMPING;
		} else if (Math.abs(this.getVelocity().x()) > Avatar.EPSILON) {
			return AvatarState.RUNNING;
		} else {
			return AvatarState.IDLE;
		}
	}
	
	private void handleAnimation() {
		final AvatarState newState = this.determineState();
		if (newState == this.currentState) {
			if (this.currentState == AvatarState.RUNNING) {
				this.renderer().setIsFlippedHorizontally(this.getVelocity().x() < 0);
			}
			return;
		}
		this.currentState = newState;
		switch ( this.currentState ) {
			case JUMPING:
				this.renderer().setRenderable(this.jumpRenderable);
				break;
			case RUNNING:
				this.renderer().setRenderable(this.runRenderable);
				this.renderer().setIsFlippedHorizontally(this.getVelocity().x() < 0);
				break;
			case IDLE:
			default:
				this.renderer().setRenderable(this.idleRenderable);
				break;
		}
	}
	
	private enum AvatarState {
		IDLE,
		RUNNING,
		JUMPING
	}
}
