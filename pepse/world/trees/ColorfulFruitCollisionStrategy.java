package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;

/**
 * A fruit collision strategy that awards energy to the avatar upon collision.
 * Extends the basic strategy by adding energy rewards while maintaining encapsulation.
 */
class ColorfulFruitCollisionStrategy extends BasicFruitCollisionStrategy {
	private static final float ENERGY_AWARDED = 10.0f;
	private static final String AVATAR_TAG = "avatar";
	
	/**
	 * Handles collision between fruit and other game objects.
	 * Awards energy if the other object is an avatar.
	 *
	 * @param fruit     The fruit involved in the collision
	 * @param other     The object colliding with the fruit
	 * @param collision Details of the collision event
	 */
	@Override
	public void onCollisionEnter(Fruit fruit, GameObject other, Collision collision) {
		// If collided object is the avatar, award energy
		final pepse.world.Avatar avatar = ( pepse.world.Avatar ) other;
		avatar.addEnergy(ColorfulFruitCollisionStrategy.ENERGY_AWARDED);
		// Then do the basic hide behavior
		super.onCollisionEnter(fruit, other, collision);
	}
}
