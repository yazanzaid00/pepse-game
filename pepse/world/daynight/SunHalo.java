package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Manages a halo effect around the sun, following its position.
 * The halo appears as a semi-transparent yellow circle that tracks the sun's center.
 */
public class SunHalo {
	private static final String HALO_TAG = "sunHalo";
	private static final float HALO_SIZE_MULTIPLIER = 1.5f;
	private static final Color DEFAULT_HALO_COLOR = new Color(255, 255, 0, 20);
	private static final float HALO_CENTER_OFFSET_FACTOR = 0.5f;
	
	private SunHalo() {
	}
	
	/**
	 * Creates a halo GameObject that tracks the given sun.
	 *
	 * @param sun The sun GameObject to create a halo around
	 * @return A new GameObject representing the sun's halo
	 */
	public static GameObject create(GameObject sun) {
		final Vector2 sunSize = sun.getDimensions();
		final Vector2 haloSize = sunSize.mult(SunHalo.HALO_SIZE_MULTIPLIER);
		
		final Vector2 haloTopLeft =
				sun.getCenter().subtract(haloSize.mult(SunHalo.HALO_CENTER_OFFSET_FACTOR));
		final OvalRenderable haloRenderable = new OvalRenderable(SunHalo.DEFAULT_HALO_COLOR);
		final GameObject halo = new GameObject(haloTopLeft, haloSize, haloRenderable);
		halo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		halo.setTag(SunHalo.HALO_TAG);
		
		halo.addComponent(deltaTime -> halo.setCenter(sun.getCenter()));
		return halo;
	}
}
