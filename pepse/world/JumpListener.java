package pepse.world;

/**
 * Functional interface for observer pattern implementation that receives jump event notifications.
 * Implemented by classes that need to respond to avatar jump events.
 */
@FunctionalInterface
public interface JumpListener {
	/**
	 * Callback triggered when the avatar performs a jump action.
	 */
	void onJump();
}
