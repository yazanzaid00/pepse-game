package pepse.world.ui;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.Color;
import java.util.function.Supplier;

/**
 * A UI component that displays and updates the Avatar's energy level.
 * Implements automatic boundary checking and text formatting.
 */
public class EnergyIndicator extends GameObject {
	private static final String DEFAULT_TEXT = "";
	private static final String TEXT_FORMAT = "%s%d";
	private static final int MIN_ENERGY = 0;
	private static final int MAX_ENERGY = 100;
	
	private final Supplier< Integer > energySupplier;
	private final TextRenderable textRenderable;
	private final String label;
	
	/**
	 * Creates a new energy indicator UI element.
	 *
	 * @param topLeftCorner  Position of the indicator
	 * @param dimensions     Size of the indicator
	 * @param energySupplier Callback to retrieve current energy value
	 * @param textColor      Color of the display text
	 * @param label          Prefix text before the energy value
	 */
	public EnergyIndicator(Vector2 topLeftCorner, Vector2 dimensions,
	                       Supplier< Integer > energySupplier,
	                       Color textColor, String label) {
		super(topLeftCorner, dimensions, null);
		this.energySupplier = energySupplier;
		this.label = label;
		this.textRenderable = new TextRenderable(EnergyIndicator.DEFAULT_TEXT);
		this.textRenderable.setColor(textColor);
		this.renderer().setRenderable(this.textRenderable);
		this.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
	}
	
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		int energyValue = this.energySupplier.get();
		if (energyValue < EnergyIndicator.MIN_ENERGY) energyValue = EnergyIndicator.MIN_ENERGY;
		if (energyValue > EnergyIndicator.MAX_ENERGY) energyValue = EnergyIndicator.MAX_ENERGY;
		this.textRenderable.setString(String.format(EnergyIndicator.TEXT_FORMAT, this.label, energyValue));
	}
}
