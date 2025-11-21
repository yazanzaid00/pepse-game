package pepse.world.weather;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;

import java.awt.Color;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Represents a raindrop that falls from the cloud. Its opacity
 * transitions from full to 0 over time, upon which we remove it.
 */
public class Raindrop {
	
	private static final Color BASE_DROP_COLOR = new Color(173, 216, 230); // LightBlue
	private static final float RAINDROP_SIZE = 8.0f;
	private static final float GRAVITY = 250.0f;
	private static final float MAX_OPACITY = 1.0f;
	private static final float MIN_OPACITY = 0.0f;
	private static final String RAINDROP_TAG = "raindrop";
	private static final float SPEED_FACTOR = 50.0f;
	
	private Raindrop() {
	} // Private constructor to prevent instantiation
	
	/**
	 * Creates a Raindrop with the specified position, removal callback,
	 * and fade-out transition duration.
	 *
	 * @param topLeftCorner      The top-left position
	 * @param removeSelfCallback Callback to remove the drop from the game
	 * @param transitionDuration Duration for fade-out
	 * @return A new Raindrop instance
	 */
	public static GameObject create(Vector2 topLeftCorner,
	                                Consumer< GameObject > removeSelfCallback,
	                                float transitionDuration) {
		final Vector2 dimensions = new Vector2(Raindrop.RAINDROP_SIZE, Raindrop.RAINDROP_SIZE);
		final RectangleRenderable renderable = new RectangleRenderable(
				ColorSupplier.approximateColor(Raindrop.BASE_DROP_COLOR));
		
		final GameObject drop = new GameObject(topLeftCorner, dimensions, renderable);
		drop.setTag(Raindrop.RAINDROP_TAG);
		drop.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		
		// downward velocity w/ random variation
		final Random rand = new Random();
		final float velocityY = Raindrop.GRAVITY + rand.nextFloat() * Raindrop.SPEED_FACTOR;
		drop.transform().setVelocityY(velocityY);
		
		// fade from 1.0f to 0.0f
		new Transition<>(
				drop,
				drop.renderer() :: setOpaqueness,
				Raindrop.MAX_OPACITY,
				Raindrop.MIN_OPACITY,
				Transition.CUBIC_INTERPOLATOR_FLOAT,
				transitionDuration,
				Transition.TransitionType.TRANSITION_ONCE,
				() -> {
					if (removeSelfCallback != null) {
						removeSelfCallback.accept(drop);
					}
				}
		);
		
		return drop;
	}
	
}
