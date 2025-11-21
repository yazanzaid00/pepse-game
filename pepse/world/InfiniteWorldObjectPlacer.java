package pepse.world;

import danogl.GameObject;

/**
 * Facade interface for managing game objects in the world.
 * Provides abstraction for object placement and removal operations.
 */
public interface InfiniteWorldObjectPlacer {
	/**
	 * Places a game object in the appropriate world layer based on its type.
	 *
	 * @param obj The game object to place in the world
	 */
	void placeObject(GameObject obj);
	
	/**
	 * Removes a game object from all relevant world layers.
	 *
	 * @param gameObject The game object to remove from the world
	 */
	void removeObject(GameObject gameObject);
}
