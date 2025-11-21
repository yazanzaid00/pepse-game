package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.world.Block;

import java.awt.Color;
import java.util.Random;

/**
 * Represents a leaf object that oscillates in angle and size.
 * Extends Block to inherit basic block properties while adding leaf-specific behavior.
 */
class Leaf extends Block {
	private static final String LEAF_TAG = "leaf";
	private static final Random RAND = new Random();
	private static final float INITIAL_DELAY_MIN = 0.5f;
	private static final float ANGLE_MIN = - 0.25f;
	private static final float ANGLE_MAX = 0.25f;
	private static final float SCALE_FACTOR_MIN = 0.95f;
	private static final float INITIAL_SCALE = 1.0f;
	private static final float CYCLE_LENGTH_MIN = Leaf.INITIAL_SCALE;
	
	/**
	 * Creates a new leaf with oscillating behavior.
	 *
	 * @param topLeftCorner Initial position of the leaf
	 * @param leafColor     Color of the leaf
	 */
	Leaf(Vector2 topLeftCorner, Color leafColor) {
		super(topLeftCorner, new RectangleRenderable(leafColor));
		this.setTag(Leaf.LEAF_TAG);
		this.physics().preventIntersectionsFromDirection(null);
		
		final float initialDelay = Leaf.INITIAL_DELAY_MIN + Leaf.RAND.nextFloat();
		new ScheduledTask(this, initialDelay, false, this :: initLeafSwayTransitions);
	}
	
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		super.onCollisionEnter(other, collision);
		// leaves won't do anything special
	}
	
	/**
	 * Initializes the leaf's oscillating movements for angle and size.
	 */
	private void initLeafSwayTransitions() {
		new Transition<>(
				this,
				this.renderer() :: setRenderableAngle,
				Leaf.ANGLE_MIN,
				Leaf.ANGLE_MAX,
				Transition.CUBIC_INTERPOLATOR_FLOAT,
				Leaf.CYCLE_LENGTH_MIN + Leaf.RAND.nextFloat(),
				Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
				null
		);
		
		new Transition<>(
				this,
				scaleFactor -> this.setDimensions(Vector2.ONES.mult(Block.SIZE * scaleFactor)),
				Leaf.INITIAL_SCALE,
				Leaf.SCALE_FACTOR_MIN,
				Transition.CUBIC_INTERPOLATOR_FLOAT,
				Leaf.CYCLE_LENGTH_MIN + Leaf.RAND.nextFloat(),
				Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
				null
		);
	}
}
