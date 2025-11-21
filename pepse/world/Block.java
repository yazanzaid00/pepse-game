package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a basic building block in the game environment.
 * Each block has fixed dimensions and physics properties.
 */
public class Block extends GameObject {
	
	/**
	 * The size of a block.
	 */
	public static final int SIZE = 30;
	
	/**
	 * The tag assigned to block objects.
	 */
	private static final String TAG_BLOCK = "block";
	
	/**
	 * Creates a new block with specified position and appearance.
	 *
	 * @param topLeftCorner Starting position of the block
	 * @param renderable    Visual representation of the block
	 */
	public Block(Vector2 topLeftCorner, Renderable renderable) {
		super(topLeftCorner, Vector2.ONES.mult(Block.SIZE), renderable);
		this.physics().preventIntersectionsFromDirection(Vector2.ZERO);
		this.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
		this.setTag(Block.TAG_BLOCK);
	}
}
