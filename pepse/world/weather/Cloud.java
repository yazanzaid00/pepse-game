package pepse.world.weather;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;
import pepse.world.JumpListener;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Represents a cloud entity that moves across the screen and can generate raindrops.
 * Implements JumpListener to trigger rain effects when the avatar jumps.
 */
public class Cloud implements JumpListener {
	
	private static final Color BASE_CLOUD_COLOR = new Color(255, 255, 255);
	
	// Shape definition for the cloud: 1 = block, 0 = empty.
	private static final List< List< Integer > > DEFAULT_CLOUD_SHAPE = List.of(
			List.of(0, 1, 1, 1, 0),
			List.of(1, 1, 1, 1, 1),
			List.of(1, 1, 1, 1, 1),
			List.of(0, 1, 1, 1, 0)
	);
	
	// Constants for movement and raindrop generation.
	private static final float START_X_OFFSET = - 150.0f;
	private static final float FINAL_X_OFFSET = 150.0f;
	private static final float START_Y_RATIO = 0.15f;
	private static final int MIN_RAINDROPS = 3;
	private static final int MAX_RAINDROPS = 30;
	private static final String CLOUD_BLOCK_TAG = "cloudBlock";
	
	// Instance fields.
	private final List< Block > cloudBlocks = new ArrayList<>();
	private final Random rand = new Random();
	private Vector2 windowDimensions;
	private float movementTimeSeconds;
	
	private Function< Vector2, GameObject > createRaindropCallback;
	
	// Private constructor: use static factory methods.
	private Cloud() {
	}
	
	/**
	 * Factory method to create a Cloud instance.
	 *
	 * @param windowDimensions       the dimensions of the game window.
	 * @param movementTimeSeconds    the time it takes for the cloud to traverse the screen.
	 * @param createRaindropCallback a callback (factory) to create a raindrop effect at a given position.
	 * @return a fully initialized Cloud instance.
	 */
	public static Cloud create(Vector2 windowDimensions,
	                           float movementTimeSeconds,
	                           Function< Vector2, GameObject > createRaindropCallback) {
		final Cloud cloud = new Cloud();
		cloud.windowDimensions = windowDimensions;
		cloud.movementTimeSeconds = movementTimeSeconds;
		cloud.createRaindropCallback = createRaindropCallback;
		cloud.initializeCloud();
		return cloud;
	}
	
	
	/**
	 * Returns the list of Block objects that form this cloud.
	 *
	 * @return a List of Block.
	 */
	public List< Block > getCloudBlocks() {
		return this.cloudBlocks;
	}
	
	/**
	 * Builds the cloud blocks from the DEFAULT_CLOUD_SHAPE and sets up a continuous
	 * horizontal movement via a Transition.
	 */
	private void initializeCloud() {
		final float startX = Cloud.START_X_OFFSET;
		final float startY = this.windowDimensions.y() * Cloud.START_Y_RATIO;
		final float blockSize = Block.SIZE;
		
		// Build cloud blocks based on the defined shape.
		for ( int row = 0; row < Cloud.DEFAULT_CLOUD_SHAPE.size(); row++ ) {
			for ( int col = 0; col < Cloud.DEFAULT_CLOUD_SHAPE.get(row).size(); col++ ) {
				if (Cloud.DEFAULT_CLOUD_SHAPE.get(row).get(col) == 1) {
					final RectangleRenderable renderable = new RectangleRenderable(
							ColorSupplier.approximateMonoColor(Cloud.BASE_CLOUD_COLOR, 0));
					final Vector2 position = new Vector2(startX + col * blockSize, startY + row * blockSize);
					final Block cloudBlock = new Block(position, renderable);
					cloudBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
					cloudBlock.setTag(Cloud.CLOUD_BLOCK_TAG);
					this.cloudBlocks.add(cloudBlock);
				}
			}
		}
		
		// Set up a looping transition on the first block (anchor) to move the cloud.
		if (! this.cloudBlocks.isEmpty()) {
			final float finalX = this.windowDimensions.x() + Cloud.FINAL_X_OFFSET;
			new Transition<>(
					this.cloudBlocks.get(0),
					this :: moveCloudByX,
					startX,
					finalX,
					Transition.LINEAR_INTERPOLATOR_FLOAT,
					this.movementTimeSeconds,
					Transition.TransitionType.TRANSITION_LOOP,
					null
			);
		}
	}
	
	/**
	 * Moves all cloud blocks horizontally according to the change in the first block's x-coordinate.
	 *
	 * @param newX the new x-coordinate from the Transition.
	 */
	private void moveCloudByX(Float newX) {
		if (this.cloudBlocks.isEmpty() || newX == null) {
			return;
		}
		final Block anchor = this.cloudBlocks.get(0);
		final float currentX = anchor.getTopLeftCorner().x();
		final float dx = newX - currentX;
		for ( Block block: this.cloudBlocks ) {
			final Vector2 oldPos = block.getTopLeftCorner();
			block.setTopLeftCorner(oldPos.add(new Vector2(dx, 0)));
		}
	}
	
	/**
	 * When a jump occurs, this method calculates the bounding box of the cloud and
	 * then generates a random number of raindrop effects within that area by calling
	 * the provided raindrop creation callback.
	 */
	@Override
	public void onJump() {
		if (this.cloudBlocks.isEmpty() || this.createRaindropCallback == null) {
			return;
		}
		float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
		for ( Block block: this.cloudBlocks ) {
			final Vector2 pos = block.getTopLeftCorner();
			if (pos.x() < minX) minX = pos.x();
			if (pos.x() > maxX) maxX = pos.x();
			if (pos.y() < minY) minY = pos.y();
			if (pos.y() > maxY) maxY = pos.y();
		}
		final int dropsCount =
				Cloud.MIN_RAINDROPS + this.rand.nextInt(Cloud.MAX_RAINDROPS - Cloud.MIN_RAINDROPS + 1);
		for ( int i = 0; i < dropsCount; i++ ) {
			final float dropX = minX + this.rand.nextFloat() * ( maxX - minX );
			final float dropY = maxY + Block.SIZE; // start a bit below the cloud
			// Create the raindrop via the provided callback.
			this.createRaindropCallback.apply(new Vector2(dropX, dropY));
		}
	}
}
