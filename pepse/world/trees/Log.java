package pepse.world.trees;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.Color;

/**
 * Represents a single log block in a tree trunk.
 * Extends Block to provide basic block functionality with tree-specific properties.
 */
class Log extends Block {
	private static final String TRUNK_TAG = "trunk";
	
	/**
	 * Creates a log block at the specified position with given color.
	 *
	 * @param topLeftCorner Starting position of the log block
	 * @param trunkColor    Base color for the log
	 */
	Log(Vector2 topLeftCorner, Color trunkColor) {
		super(topLeftCorner, new RectangleRenderable(ColorSupplier.approximateColor(trunkColor)));
		this.setTag(Log.TRUNK_TAG);
	}
}
