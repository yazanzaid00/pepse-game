package pepse.world.trees;

import danogl.GameObject;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Represents a procedurally generated tree with trunk, leaves and fruits.
 * Ensures proper placement above ground using terrain height function.
 */
class Tree {
	private static final double FRUIT_DENSITY = 0.2;
	private static final double LEAF_DENSITY = 0.8;
	
	private static final float FRUIT_DIAMETER = 25.0f;
	private static final float LEAF_SIZE = Block.SIZE;
	
	private static final int CANOPY_RADIUS = 2; // columns around trunk used for canopy
	
	private final Function< Float, Float > groundHeightFunction;
	private final Color trunkColor;
	private final Color leafColor;
	private final Color fruitColor;
	
	/**
	 * Creates a new tree generator with specified colors and terrain height function.
	 *
	 * @param groundHeightFunction Function to get terrain height at any X coordinate
	 * @param trunkColor           Base color for trunk blocks
	 * @param leafColor            Base color for leaves
	 * @param fruitColor           Base color for fruits
	 */
	Tree(Function< Float, Float > groundHeightFunction,
	     Color trunkColor,
	     Color leafColor,
	     Color fruitColor) {
		this.groundHeightFunction = groundHeightFunction;
		this.trunkColor = trunkColor;
		this.leafColor = leafColor;
		this.fruitColor = fruitColor;
	}
	
	/**
	 * Builds one tree at xCoord with a specified trunk height.
	 *
	 * @param xCoord       The x coordinate of the trunk.
	 * @param groundHeight The ground height at xCoord.
	 * @param trunkHeight  The number of blocks in the trunk.
	 * @param rand         A Random instance for reproducible generation.
	 * @return A list of trunk blocks, leaves, and (optionally) fruits.
	 */
	List< GameObject > createSingleTree(int xCoord,
	                                    float groundHeight,
	                                    int trunkHeight,
	                                    Random rand) {
		final List< GameObject > results = new ArrayList<>();
		
		// 1) Build the trunk
		results.addAll(this.buildTrunk(xCoord, groundHeight, trunkHeight));
		
		// 2) Build canopy (leaves + fruit) above trunk
		final float trunkTopY = groundHeight - ( trunkHeight * Block.SIZE );
		final float canopyTopY = trunkTopY - Block.SIZE; // start canopy 1 block above the trunk
		results.addAll(this.buildCanopy(xCoord, canopyTopY, rand));
		
		return results;
	}
	
	/**
	 * Builds a vertical trunk of the specified height, placing trunk blocks
	 * from groundHeight downward up to trunkHeight blocks.
	 */
	private List< GameObject > buildTrunk(int xCoord, float groundHeight, int trunkHeight) {
		final List< GameObject > trunkBlocks = new ArrayList<>();
		for ( int i = 0; i < trunkHeight; i++ ) {
			final float y = groundHeight - ( ( i + 1 ) * Block.SIZE );
			final Log trunkBlock = new Log(
					new Vector2(xCoord, y),
					ColorSupplier.approximateColor(this.trunkColor)
			);
			trunkBlocks.add(trunkBlock);
		}
		return trunkBlocks;
	}
	
	/**
	 * Builds the region of leaves + fruit forming the canopy, skipping
	 * any that would appear on/under the ground or in the trunk's column.
	 */
	private List< GameObject > buildCanopy(int trunkX, float canopyTopY, Random rand) {
		final List< GameObject > canopyObjects = new ArrayList<>();
		
		final int startX = trunkX - ( Block.SIZE * Tree.CANOPY_RADIUS );
		final int endX = trunkX + Block.SIZE + ( Block.SIZE * Tree.CANOPY_RADIUS );
		
		final int startY = ( int ) ( canopyTopY - ( Block.SIZE * Tree.CANOPY_RADIUS ) );
		final int endY = ( int ) ( canopyTopY + Block.SIZE + ( Block.SIZE * Tree.CANOPY_RADIUS ) );
		
		for ( int x = startX; x < endX; x += Block.SIZE ) {
			// Retrieve terrain height once for the entire column
			final float terrainY = this.groundHeightFunction.apply(( float ) x);
			
			for ( int y = startY; y < endY; y += Block.SIZE ) {
				final double r = rand.nextDouble();
				final boolean inTrunkColumn = ( x >= trunkX && x < trunkX + Block.SIZE );
				
				// We skip fruit/leaves if they'd overlap or sit on the ground:
				final boolean wouldLeafOverlapGround = ( ( y + Tree.LEAF_SIZE + Block.SIZE ) >= terrainY );
				final boolean wouldFruitOverlapGround = ( ( y + Tree.FRUIT_DIAMETER + Block.SIZE ) >=
						                                          terrainY );
				
				if (inTrunkColumn) {
					// trunk column => no fruit, maybe a leaf if above ground
					if (! wouldLeafOverlapGround && r < Tree.LEAF_DENSITY) {
						final Leaf leaf = new Leaf(
								new Vector2(x, y),
								ColorSupplier.approximateColor(this.leafColor, 20)
						);
						canopyObjects.add(leaf);
					}
				} else {
					// outside trunk column => maybe fruit or leaf
					if (! wouldFruitOverlapGround && ( r < Tree.FRUIT_DENSITY )) {
						final Fruit fruit = new Fruit(
								new Vector2(x, y),
								ColorSupplier.approximateColor(this.fruitColor, 60)
						);
						fruit.setCollisionStrategy(new ColorfulFruitCollisionStrategy());
						canopyObjects.add(fruit);
					} else if (! wouldLeafOverlapGround && ( r < Tree.FRUIT_DENSITY + Tree.LEAF_DENSITY )) {
						final Leaf leaf = new Leaf(
								new Vector2(x, y),
								ColorSupplier.approximateColor(this.leafColor, 20)
						);
						canopyObjects.add(leaf);
					}
				}
			}
		}
		
		return canopyObjects;
	}
}
