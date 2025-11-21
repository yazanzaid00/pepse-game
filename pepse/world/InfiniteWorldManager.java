package pepse.world;

import danogl.GameObject;
import pepse.world.trees.Flora;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages procedural generation of an infinite world around the player's position.
 * Handles chunk loading/unloading and terrain/flora generation.
 */
public class InfiniteWorldManager {
	// How many chunks to keep loaded on each side of the avatar
	private static final int CHUNKS_TO_LOAD_ON_EACH_SIDE = 1;
	
	// Holds references to all GameObjects in each chunk so we can remove them if needed.
	private final Map< ChunkRange, List< GameObject > > chunks = new HashMap<>();
	
	private final int chunkWidth;
	private final Terrain terrain;
	private final Flora flora;
	private final InfiniteWorldObjectPlacer objectPlacer;
	
	private int minChunkIndex;
	private int maxChunkIndex;
	
	/**
	 * Constructs the InfiniteWorldManager.
	 *
	 * @param terrain      Terrain generator (seeded) for creating ground blocks.
	 * @param flora        Flora generator (seeded) for creating trees, leaves, fruits, etc.
	 * @param objectPlacer A facade for placing/removing objects in the world.
	 * @param windowWidth  Width of the window, used to determine chunk width.
	 */
	public InfiniteWorldManager(Terrain terrain, Flora flora, InfiniteWorldObjectPlacer objectPlacer,
	                            float windowWidth) {
		this.terrain = terrain;
		this.flora = flora;
		this.objectPlacer = objectPlacer;
		
		// We treat one "chunk" width as the entire window width
		this.chunkWidth = ( int ) windowWidth;
		this.minChunkIndex = - InfiniteWorldManager.CHUNKS_TO_LOAD_ON_EACH_SIDE;
		this.maxChunkIndex = InfiniteWorldManager.CHUNKS_TO_LOAD_ON_EACH_SIDE;
		
		this.initInitialChunks();
	}
	
	/**
	 * Updates the manager based on the avatar's current X, loading/unloading chunks
	 * so that CHUNKS_TO_LOAD_ON_EACH_SIDE remain visible on each side.
	 *
	 * @param avatarX The avatar's current X coordinate.
	 */
	public void update(float avatarX) {
		final int currentChunkIndex = ( int ) Math.floor(avatarX / this.chunkWidth);
		
		// Ensure we have enough chunks to the left
		while ( currentChunkIndex - this.minChunkIndex < InfiniteWorldManager.CHUNKS_TO_LOAD_ON_EACH_SIDE ) {
			this.minChunkIndex--;
			this.loadChunk(this.minChunkIndex);
		}
		
		// Ensure we have enough chunks to the right
		while ( this.maxChunkIndex - currentChunkIndex < InfiniteWorldManager.CHUNKS_TO_LOAD_ON_EACH_SIDE ) {
			this.maxChunkIndex++;
			this.loadChunk(this.maxChunkIndex);
		}
		
		// Unload chunks too far left
		while ( currentChunkIndex - this.minChunkIndex > InfiniteWorldManager.CHUNKS_TO_LOAD_ON_EACH_SIDE ) {
			this.unloadChunk(this.minChunkIndex);
			this.minChunkIndex++;
		}
		
		// Unload chunks too far right
		while ( this.maxChunkIndex - currentChunkIndex > InfiniteWorldManager.CHUNKS_TO_LOAD_ON_EACH_SIDE ) {
			this.unloadChunk(this.maxChunkIndex);
			this.maxChunkIndex--;
		}
	}
	
	/**
	 * Loads all chunks in the initial range.
	 */
	private void initInitialChunks() {
		for ( int i = this.minChunkIndex; i <= this.maxChunkIndex; i++ ) {
			this.loadChunk(i);
		}
	}
	
	/**
	 * Loads a chunk by creating terrain and flora in [minX, maxX),
	 * then passing them to objectPlacer callbacks.
	 *
	 * @param chunkIndex The index of the chunk (chunk width is window width).
	 */
	private void loadChunk(int chunkIndex) {
		final int minX = chunkIndex * this.chunkWidth;
		final int maxX = minX + this.chunkWidth;
		final ChunkRange chunkRange = new ChunkRange(minX, maxX);
		
		// If chunk is already loaded, do nothing
		if (this.chunks.containsKey(chunkRange)) {
			return;
		}
		
		// 1) Terrain blocks
		final List< Block > terrainBlocks = this.terrain.createInRange(minX, maxX);
		// 2) Flora objects (trees, leaves, fruits, etc.)
		final List< GameObject > floraObjects = this.flora.createInRange(minX, maxX);
		
		// Combine all objects in one list so we can store for unloading
		final List< GameObject > combined = new ArrayList<>();
		combined.addAll(terrainBlocks);
		combined.addAll(floraObjects);
		
		// Place them via the interface callback
		for ( GameObject obj: combined ) {
			this.objectPlacer.placeObject(obj);
		}
		
		// Store references for unloading
		this.chunks.put(chunkRange, combined);
	}
	
	/**
	 * Unloads a chunk by removing its objects from the game.
	 *
	 * @param chunkIndex The index of the chunk to unload.
	 */
	private void unloadChunk(int chunkIndex) {
		final int minX = chunkIndex * this.chunkWidth;
		final int maxX = minX + this.chunkWidth;
		final ChunkRange chunkRange = new ChunkRange(minX, maxX);
		
		if (! this.chunks.containsKey(chunkRange)) {
			return;
		}
		final List< GameObject > objectsInChunk = this.chunks.get(chunkRange);
		for ( GameObject obj: objectsInChunk ) {
			this.objectPlacer.removeObject(obj);
		}
		this.chunks.remove(chunkRange);
	}
	
	/**
	 * A private helper class representing the min/max X range for a chunk.
	 */
	private static class ChunkRange {
		private static final int HASH_FACTOR = 31;
		final int minX;
		final int maxX;
		
		ChunkRange(int minX, int maxX) {
			this.minX = minX;
			this.maxX = maxX;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (! ( obj instanceof ChunkRange other )) {
				return false;
			}
			return this.minX == other.minX && this.maxX == other.maxX;
		}
		
		@Override
		public int hashCode() {
			int result = Integer.hashCode(this.minX);
			result = ChunkRange.HASH_FACTOR * result + Integer.hashCode(this.maxX);
			return result;
		}
	}
}
