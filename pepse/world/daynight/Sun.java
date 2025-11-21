package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Manages the sun object that rotates in a circular path in the game world.
 * The sun completes one full rotation cycle based on the provided cycle length.
 */
public class Sun {
	private static final Color SUN_COLOR = Color.YELLOW;
	private static final float FULL_CYCLE_DEGREES = 360.0f;
	private static final String SUN_TAG = "sun";
	
	// Factors for sun positioning and sizing relative to window dimensions
	private static final float ORBIT_RADIUS_FACTOR = 0.35f;
	private static final float DIAMETER_FACTOR = 0.10f;
	private static final float HORIZON_Y_FACTOR = 2.0f / 3.0f;
	private static final float HALF_FACTOR = 0.5f;
	private static final float HORIZON_X_FACTOR = Sun.HALF_FACTOR;
	private static final float START_ANGLE = 0.0f;
	private static Vector2 initialSunCenter;
	
	// Utility class - prevent instantiation
	private Sun() {
	}
	
	/**
	 * Creates and configures a sun GameObject that orbits above the horizon.
	 *
	 * @param windowDimensions The game window dimensions for positioning
	 * @param cycleLength      Duration of one complete sun rotation cycle
	 * @return A configured sun GameObject ready for the game world
	 */
	public static GameObject create(Vector2 windowDimensions, float cycleLength) {
		final Vector2 cycleCenter = new Vector2(
				windowDimensions.x() * Sun.HORIZON_X_FACTOR,
				windowDimensions.y() * Sun.HORIZON_Y_FACTOR
		);
		final float orbitRadius = windowDimensions.y() * Sun.ORBIT_RADIUS_FACTOR;
		final float diameter = windowDimensions.x() * Sun.DIAMETER_FACTOR;
		
		Sun.initialSunCenter = cycleCenter.add(new Vector2(0, - orbitRadius));
		
		final Vector2 sunSize = new Vector2(diameter, diameter);
		final Vector2 topLeft = Sun.initialSunCenter.subtract(sunSize.mult(Sun.HALF_FACTOR));
		final Renderable sunRenderable = new OvalRenderable(Sun.SUN_COLOR);
		final GameObject sun = new GameObject(topLeft, sunSize, sunRenderable);
		sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		sun.setTag(Sun.SUN_TAG);
		
		new Transition< Float >(
				sun,
				(angle) -> {
					sun.setCenter(Sun.initialSunCenter.subtract(cycleCenter).rotated(angle).add(cycleCenter));
				},
				Sun.START_ANGLE,
				Sun.FULL_CYCLE_DEGREES,
				Transition.LINEAR_INTERPOLATOR_FLOAT,
				cycleLength,
				Transition.TransitionType.TRANSITION_LOOP,
				null
		);
		
		return sun;
	}
}
