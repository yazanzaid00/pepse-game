package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages procedural terrain generation using noise and block-based construction.
 */
public class Terrain {
	// Block tags and styling
	private static final String GROUND_TAG = "ground";
	private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
	// Terrain generation parameters
	private static final int TERRAIN_DEPTH = 20;
	private static final float NOISE_FACTOR = ( Block.SIZE * 11 );
	private static final float GROUND_HEIGHT_RATIO = 2.0f / 3.0f;
	private static final int INITIAL_BLOCK_DEPTH = 0;
	private static final int MAX_BLOCK_DEPTH = Terrain.TERRAIN_DEPTH;
	
	private final float groundHeightAtX0;
	private final NoiseGenerator noiseGenerator;
	
	/**
	 * Creates a terrain generator with specified dimensions and randomization.
	 *
	 * @param windowDimensions The game window dimensions
	 * @param seed             Random seed for terrain generation
	 */
	public Terrain(Vector2 windowDimensions, int seed) {
		this.groundHeightAtX0 = Terrain.GROUND_HEIGHT_RATIO * windowDimensions.y();
		this.noiseGenerator = new NoiseGenerator(seed, ( int ) this.groundHeightAtX0);
	}
	
	/**
	 * Calculates ground height at given x coordinate.
	 *
	 * @param x The x coordinate to check
	 * @return Ground height at x
	 */
	public float groundHeightAt(float x) {
		final double noiseVal = this.noiseGenerator.noise(x, Terrain.NOISE_FACTOR);
		return this.groundHeightAtX0 + ( float ) noiseVal;
	}
	
	/**
	 * Generates terrain blocks within specified x-range.
	 *
	 * @param minX Leftmost x coordinate
	 * @param maxX Rightmost x coordinate
	 * @return List of generated terrain blocks
	 */
	public List< Block > createInRange(int minX, int maxX) {
		final List< Block > blocks = new ArrayList<>();
		
		final int startCol = this.calculateStartColumn(minX);
		final int endCol = this.calculateEndColumn(maxX);
		
		for ( int x = startCol; x < endCol; x += Block.SIZE ) {
			float topY = this.groundHeightAt(x);
			topY = ( float ) ( Math.floor(( topY / Block.SIZE )) * Block.SIZE );
			
			for ( int depth = Terrain.INITIAL_BLOCK_DEPTH; depth < Terrain.MAX_BLOCK_DEPTH; depth++ ) {
				final float y = topY + ( depth * Block.SIZE );
				final RectangleRenderable renderable =
						new RectangleRenderable(ColorSupplier.approximateColor(Terrain.BASE_GROUND_COLOR));
				final Block block = new Block(new Vector2(x, y), renderable);
				block.setTag(Terrain.GROUND_TAG);
				blocks.add(block);
			}
		}
		return blocks;
	}
	
	private int calculateStartColumn(int minX) {
		int startCol = ( minX / Block.SIZE ) * Block.SIZE;
		if (minX < 0 && minX % Block.SIZE != 0) {
			startCol -= Block.SIZE;
		}
		return startCol;
	}
	
	private int calculateEndColumn(int maxX) {
		return ( maxX / Block.SIZE ) * Block.SIZE;
	}
}
