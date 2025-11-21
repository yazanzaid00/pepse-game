package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Avatar;
import pepse.world.Block;
import pepse.world.InfiniteWorldManager;
import pepse.world.InfiniteWorldObjectPlacer;
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;
import pepse.world.ui.EnergyIndicator;
import pepse.world.weather.Cloud;
import pepse.world.weather.Raindrop;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The main game manager for PEPSE.
 * Demonstrates how to orchestrate objects into the game,
 * now extended for an infinite world by using InfiniteWorldManager
 * and an InfiniteWorldObjectPlacer facade.
 * <p>
 * We adhere to the Open-Closed principle by treating this manager as
 * the single orchestration point, kept minimal and stable.
 */
public class PepseGameManager extends GameManager {
	private static final float DAY_CYCLE_LENGTH = 30.0f;
	private static final float CLOUD_MOVEMENT_TIME = PepseGameManager.DAY_CYCLE_LENGTH / 3;
	private static final float RAINDROP_TRANSITION_DURATION = 2.0f;
	private static final int SEED = 42;
	
	// Layers
	private static final int SKY_LAYER = Layer.BACKGROUND;
	private static final int NIGHT_LAYER = PepseGameManager.SKY_LAYER + 1;
	private static final int SUN_LAYER = PepseGameManager.NIGHT_LAYER + 1;
	private static final int SUN_HALO_LAYER = PepseGameManager.SUN_LAYER + 1;
	private static final int CLOUD_LAYER = PepseGameManager.SUN_HALO_LAYER + 1;
	private static final int RAINDROP_LAYER = PepseGameManager.CLOUD_LAYER + 1;
	private static final int COLLIDABLE_LAYER = Layer.STATIC_OBJECTS;
	private static final int LEAVES_LAYER = PepseGameManager.COLLIDABLE_LAYER + 1;
	private static final int FRUIT_LAYER = PepseGameManager.LEAVES_LAYER + 1;
	private static final int AVATAR_LAYER = Layer.DEFAULT;
	
	private static final Vector2 ENERGY_INDICATOR_POSITION = new Vector2(20.0F, 20.0F);
	private static final Vector2 ENERGY_INDICATOR_SIZE = new Vector2(100.0F, 30.0F);
	private static final String ENERGY_LABEL_PREFIX = "Energy: ";
	private static final String PEPSE_TITLE = "PEPSE";
	// Tag constants
	private static final String TRUNK_TAG = "trunk";
	private static final String GROUND_TAG = "ground";
	private static final String TAG_BLOCK = "block";
	private static final String LEAF_TAG = "leaf";
	private static final String FRUIT_TAG = "fruit";
	private static final String DEBUG_START_MARKER_TAG = "debugStartMarker";
	private static final int WINDOW_WIDTH = 1024;
	private static final int WINDOW_HEIGHT = 768;
	private Terrain terrain;
	private Avatar avatar;
	private InfiniteWorldManager infiniteWorldManager;
	
	private PepseGameManager() {
		super(PepseGameManager.PEPSE_TITLE, new Vector2(WINDOW_WIDTH, WINDOW_HEIGHT));
	}
	
	/**
	 * Main entry point.
	 */
	public static void main(String[] args) {
		new PepseGameManager().run();
	}
	
	@Override
	public void initializeGame(ImageReader imageReader, SoundReader soundReader,
	                           UserInputListener inputListener, WindowController windowController) {
		super.initializeGame(imageReader, soundReader, inputListener, windowController);
		
		final Vector2 windowDimensions = windowController.getWindowDimensions();
		
		// 1) Sky
		final GameObject sky = pepse.world.Sky.create(windowDimensions);
		this.gameObjects().addGameObject(sky, PepseGameManager.SKY_LAYER);
		
		// 2) Night
		final GameObject night = Night.create(windowDimensions, PepseGameManager.DAY_CYCLE_LENGTH);
		this.gameObjects().addGameObject(night, PepseGameManager.NIGHT_LAYER);
		
		// 3) Sun
		final GameObject sun = Sun.create(windowDimensions, PepseGameManager.DAY_CYCLE_LENGTH);
		this.gameObjects().addGameObject(sun, PepseGameManager.SUN_LAYER);
		
		// 4) Sun halo
		final GameObject sunHalo = SunHalo.create(sun);
		this.gameObjects().addGameObject(sunHalo, PepseGameManager.SUN_HALO_LAYER);
		
		// 5) Terrain
		this.terrain = new Terrain(windowDimensions, PepseGameManager.SEED);
		
		// 6) Flora
		final Flora flora = new Flora(this.terrain :: groundHeightAt, PepseGameManager.SEED);
		
		// 7) Avatar near x=0
		final float groundHeightAtZero = this.terrain.groundHeightAt(0);
		final Vector2 avatarPos = new Vector2(0, groundHeightAtZero - ( Block.SIZE * 2 ));
		
		this.avatar = new Avatar(avatarPos, inputListener, imageReader);
		this.gameObjects().addGameObject(this.avatar, PepseGameManager.AVATAR_LAYER);

//		// Optional debugging marker
//		final GameObject startMarker = new GameObject(avatarPos, new Vector2(10, 10),
//				new RectangleRenderable(Color.RED));
//		startMarker.setTag(PepseGameManager.DEBUG_START_MARKER_TAG);
//		this.gameObjects().addGameObject(startMarker, Layer.UI);
		
		// 8) Energy Indicator
		final EnergyIndicator energyIndicator = new EnergyIndicator(
				PepseGameManager.ENERGY_INDICATOR_POSITION, PepseGameManager.ENERGY_INDICATOR_SIZE,
				() -> ( int ) Math.floor(this.avatar.getEnergyLevel()), Color.YELLOW,
				PepseGameManager.ENERGY_LABEL_PREFIX);
		this.gameObjects().addGameObject(energyIndicator, Layer.UI);
		
		// 9) Collisions
		this.setupCollisions();
		
		// 10) Camera: follow the avatar
		this.setCamera(new Camera(this.avatar, Vector2.ZERO, windowDimensions, windowDimensions));
		
		// 11) Cloud + raindrop callback
		final Function< Vector2, GameObject > createRaindrop = position -> {
			final Consumer< GameObject > removeDrop = dropObj -> this.gameObjects().removeGameObject(dropObj,
					PepseGameManager.RAINDROP_LAYER);
			
			final GameObject drop = Raindrop.create(position, removeDrop,
					PepseGameManager.RAINDROP_TRANSITION_DURATION);
			this.gameObjects().addGameObject(drop, PepseGameManager.RAINDROP_LAYER);
			return drop;
		};
		
		final Cloud cloud = Cloud.create(windowDimensions, PepseGameManager.CLOUD_MOVEMENT_TIME,
				createRaindrop);
		for ( Block block: cloud.getCloudBlocks() ) {
			this.gameObjects().addGameObject(block, PepseGameManager.CLOUD_LAYER);
		}
		// Register the cloud as a jump listener
		this.avatar.addJumpListener(cloud);
		
		// 12) InfiniteWorldManager
		final InfiniteWorldObjectPlacer objectPlacer = new InfiniteWorldObjectPlacer() {
			
			@Override
			public void placeObject(GameObject obj) {
				final String tag = obj.getTag();
				if (PepseGameManager.TRUNK_TAG.equals(tag) || PepseGameManager.GROUND_TAG.equals(tag) ||
						    PepseGameManager.TAG_BLOCK.equals(tag)) {
					PepseGameManager.this.gameObjects().addGameObject(obj, PepseGameManager.COLLIDABLE_LAYER);
				} else if (PepseGameManager.LEAF_TAG.equals(tag)) {
					PepseGameManager.this.gameObjects().addGameObject(obj, PepseGameManager.LEAVES_LAYER);
				} else if (PepseGameManager.FRUIT_TAG.equals(tag)) {
					PepseGameManager.this.gameObjects().addGameObject(obj, PepseGameManager.FRUIT_LAYER);
				}
			}
			
			@Override
			public void removeObject(GameObject gameObject) {
				// Remove from all potential layers
				PepseGameManager.this.gameObjects()
						.removeGameObject(gameObject, PepseGameManager.COLLIDABLE_LAYER);
				PepseGameManager.this.gameObjects()
						.removeGameObject(gameObject, PepseGameManager.LEAVES_LAYER);
				PepseGameManager.this.gameObjects()
						.removeGameObject(gameObject, PepseGameManager.FRUIT_LAYER);
			}
		};
		
		// Now create infinite world manager
		this.infiniteWorldManager = new InfiniteWorldManager(this.terrain, flora, objectPlacer,
				windowDimensions.x());
	}
	
	/**
	 * Sets up collision rules between layers.
	 */
	private void setupCollisions() {
		// Avatar collides with terrain + trunk
		this.gameObjects().layers()
				.shouldLayersCollide(PepseGameManager.AVATAR_LAYER, PepseGameManager.COLLIDABLE_LAYER, true);
		// Avatar DOES collide with fruits
		this.gameObjects().layers()
				.shouldLayersCollide(PepseGameManager.AVATAR_LAYER, PepseGameManager.FRUIT_LAYER, true);
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		
		// Let the infinite world manager handle new chunk creation as the avatar moves
		if (this.infiniteWorldManager != null && this.avatar != null) {
			final float avatarX = this.avatar.getCenter().x();
			this.infiniteWorldManager.update(avatarX);
		}
	}
}
