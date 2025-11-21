/***************************************
 * File: FruitCollisionStrategy.java
 ***************************************/
package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;

/**
 * Strategy interface for collision handling of Fruit objects.
 * Implementations determine specific collision behavior (e.g., fruit removal or energy awards).
 */
@FunctionalInterface
interface FruitCollisionStrategy {
	
	/**
	 * Handles collision between a fruit and another game object.
	 *
	 * @param fruit     The fruit object in collision
	 * @param other     The colliding game object
	 * @param collision Engine-provided collision details
	 */
	void onCollisionEnter(Fruit fruit, GameObject other, Collision collision);
}
