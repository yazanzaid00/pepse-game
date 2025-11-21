package pepse.world.trees;

import danogl.GameObject;
import pepse.world.Block;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Manages tree generation within a specified X-range using reproducible randomization.
 * Uses seeded randomness to ensure consistent tree placement across game runs.
 */
public class Flora {
	private static final Color DEFAULT_TRUNK_COLOR = new Color(100, 50, 20);
	private static final Color DEFAULT_LEAF_COLOR = new Color(50, 200, 30);
	private static final Color DEFAULT_FRUIT_COLOR = new Color(220, 100, 60);
	
	/**
	 * Probability factor for deciding whether to plant a tree at a given column
	 * (biased coin toss).
	 */
	private static final int TRUNK_PROBABILITY = 4;
	
	/**
	 * The minimum trunk height for newly planted trees.
	 */
	private static final int TREE_MIN_HEIGHT = 3;
	
	/**
	 * The maximum trunk height for newly planted trees.
	 * Used along with TREE_MIN_HEIGHT to produce random trunk sizes.
	 */
	private static final int TREE_MAX_HEIGHT = 5;
	
	private static final int AVOID_AVATAR_RADIUS = 2 * Block.SIZE;
	private static final int MIN_TREE_GAP = Block.SIZE * 6; // gap between columns
	
	private final Function< Float, Float > groundHeightFunction;
	private final Tree singleTreeHelper;
	private final int seed;
	
	/**
	 * Creates a new Flora instance for tree generation.
	 *
	 * @param groundHeightFunction Provides terrain height at given X coordinate
	 * @param seed                 Seed for reproducible randomization
	 */
	public Flora(Function< Float, Float > groundHeightFunction, int seed) {
		this.groundHeightFunction = groundHeightFunction;
		this.seed = seed;
		/*
		 * The Tree will handle all trunk/leaves/fruit logic internally,
		 * relying on groundHeightFunction to ensure fruit won't spawn underground.
		 */
		this.singleTreeHelper = new Tree(
				this.groundHeightFunction,
				Flora.DEFAULT_TRUNK_COLOR,
				Flora.DEFAULT_LEAF_COLOR,
				Flora.DEFAULT_FRUIT_COLOR
		);
	}
	
	/**
	 * Generates trees within specified X range.
	 *
	 * @param minX Leftmost X coordinate
	 * @param maxX Rightmost X coordinate
	 * @return List of created tree objects (trunks, leaves, fruits)
	 */
	public List< GameObject > createInRange(int minX, int maxX) {
		final List< GameObject > createdObjects = new ArrayList<>();
		
		// Align to multiples of MIN_TREE_GAP for consistent spacing
		int startX = ( minX / Flora.MIN_TREE_GAP ) * Flora.MIN_TREE_GAP;
		if (minX < 0 && ( minX % Flora.MIN_TREE_GAP ) != 0) {
			startX -= Flora.MIN_TREE_GAP;
		}
		
		for ( int x = startX; x < maxX; x += Flora.MIN_TREE_GAP ) {
			// Skip columns too close to the avatar's initial position if desired, this was allowed in forums
			if (Math.abs(x) < Flora.AVOID_AVATAR_RADIUS) {
				continue;
			}
			final Random columnRand = new Random(Objects.hash(x, this.seed));
			
			if (columnRand.nextInt(Flora.TRUNK_PROBABILITY) == 0) {
				float groundHeight = this.groundHeightFunction.apply(( float ) x);
				// Round down to nearest block size
				groundHeight = ( float ) ( Math.floor(groundHeight / Block.SIZE)
						                           * Block.SIZE );
				
				// Choose a random trunk height between TREE_MIN_HEIGHT and TREE_MAX_HEIGHT (inclusive).
				final int trunkHeight = Flora.TREE_MIN_HEIGHT +
						                        columnRand.nextInt(
								                        Flora.TREE_MAX_HEIGHT - Flora.TREE_MIN_HEIGHT + 1);
				
				createdObjects.addAll(
						this.singleTreeHelper.createSingleTree(
								x, groundHeight, trunkHeight, columnRand
						)
				);
			}
		}
		
		return createdObjects;
	}
}
