package pepse.world.trees;

import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Block;

import java.awt.Color;

/**
 * A fruit object that appears as a circle and can be collected by the avatar.
 * When collected, it fades out and reappears after a delay.
 */
class Fruit extends Block {
	private static final String FRUIT_TAG = "fruit";
	private static final float FRUIT_DIAMETER = 25.0f;
	private static final float FRUIT_RESPAWN_TIME = 30.0f;
	private static final float FADE_OUT_DURATION = 0.5f;
	private static final float FADE_IN_DURATION = 0.5f;
	private final Vector2 originalPosition;
	private final Color baseFruitColor;
	private FruitCollisionStrategy collisionStrategy = new BasicFruitCollisionStrategy();
	private boolean isEaten;
	private boolean collisionsEnabled = true;
	
	/**
	 * Creates a new fruit at the specified position.
	 *
	 * @param topLeftCorner Initial position of the fruit
	 * @param fruitColor    Color of the fruit
	 */
	Fruit(Vector2 topLeftCorner, Color fruitColor) {
		super(topLeftCorner, Fruit.createFruitRenderable(fruitColor));
		this.setTag(Fruit.FRUIT_TAG);
		this.originalPosition = topLeftCorner;
		this.baseFruitColor = fruitColor;
		this.isEaten = false;
		this.setDimensions(new Vector2(Fruit.FRUIT_DIAMETER, Fruit.FRUIT_DIAMETER));
		this.physics().preventIntersectionsFromDirection(null);
	}
	
	private static Renderable createFruitRenderable(Color color) {
		return new OvalRenderable(color);
	}
	
	/**
	 * Sets the collision handling strategy for this fruit.
	 */
	void setCollisionStrategy(FruitCollisionStrategy strategy) {
		this.collisionStrategy = strategy;
	}
	
	@Override
	public void onCollisionEnter(danogl.GameObject other, Collision collision) {
		if (! this.collisionsEnabled) return;
		super.onCollisionEnter(other, collision);
		if (! this.isEaten && this.collisionStrategy != null) {
			this.collisionStrategy.onCollisionEnter(this, other, collision);
		}
	}
	
	/**
	 * Handles fruit collection by fading it out and scheduling respawn.
	 */
	void eatFruit() {
		if (this.isEaten) return;
		this.isEaten = true;
		this.renderer().fadeOut(Fruit.FADE_OUT_DURATION, () -> {
			this.disableCollisions();
			new ScheduledTask(this, Fruit.FRUIT_RESPAWN_TIME, false, this :: respawnFruit);
		});
	}
	
	private void respawnFruit() {
		this.isEaten = false;
		this.enableCollisions();
		this.setTopLeftCorner(this.originalPosition);
		this.renderer().setRenderable(new OvalRenderable(this.baseFruitColor));
		this.renderer().fadeIn(Fruit.FADE_IN_DURATION);
	}
	
	private void disableCollisions() {
		this.collisionsEnabled = false;
	}
	
	private void enableCollisions() {
		this.collisionsEnabled = true;
	}
}
