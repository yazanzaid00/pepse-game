package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;

/**
 * Implements basic fruit collision behavior that fades out the fruit and schedules its reappearance.
 * No additional effects (like energy awards) are applied.
 *
 * @see FruitCollisionStrategy
 */
class BasicFruitCollisionStrategy implements FruitCollisionStrategy {
	
	@Override
	public void onCollisionEnter(Fruit fruit, GameObject other, Collision collision) {
		// Basic behavior: vanish the fruit
		fruit.eatFruit();
	}
}
