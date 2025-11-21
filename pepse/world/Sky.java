package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Creates and manages the sky background in the game world.
 */
public class Sky {
	private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");
	private static final String SKY_TAG = "sky";
	
	private Sky() {
	}
	
	/**
	 * Creates a sky GameObject with the default color, spanning the entire window.
	 */
	public static GameObject create(Vector2 windowDimensions) {
		final GameObject sky = new GameObject(Vector2.ZERO, windowDimensions,
				new RectangleRenderable(Sky.BASIC_SKY_COLOR));
		sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		sky.setTag(Sky.SKY_TAG);
		return sky;
	}
}
