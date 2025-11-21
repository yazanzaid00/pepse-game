package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Controls the day/night cycle by managing a black overlay that transitions between
 * transparent and semi-transparent.
 */
public class Night {
	private static final float MIDNIGHT_OPACITY = 0.5f;
	private static final String NIGHT_TAG = "nightBlock";
	private static final float NOON_OPACITY = 0.0f;
	private static final float HALF_CYCLE = 2.0f;
	
	private Night() {
	}
	
	/**
	 * Creates a night overlay GameObject that cycles between day and night.
	 *
	 * @param windowDimensions The dimensions of the game window
	 * @param cycleLength      The duration of a full day/night cycle in seconds
	 * @return GameObject representing the night overlay
	 */
	public static GameObject create(Vector2 windowDimensions, float cycleLength) {
		final RectangleRenderable nightRenderable = new RectangleRenderable(Color.BLACK);
		final GameObject night = new GameObject(Vector2.ZERO, windowDimensions, nightRenderable);
		night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		night.setTag(Night.NIGHT_TAG);
		night.renderer().setOpaqueness(Night.NOON_OPACITY);
		
		new Transition<>(
				night,
				night.renderer() :: setOpaqueness,
				Night.NOON_OPACITY,
				Night.MIDNIGHT_OPACITY,
				Transition.CUBIC_INTERPOLATOR_FLOAT,
				cycleLength / Night.HALF_CYCLE,
				Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
				null
		);
		return night;
	}
}
