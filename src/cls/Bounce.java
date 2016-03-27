package cls;

import scn.Arena;
import lib.Vector3;

public class Bounce {

	private static final double GROW_DURATION = 0.2;
	private static final double FADE_DURATION = 1.2;
	private static final double MAX_SIZE = 16;
	
	public final int x;
	public final int y;
	public final int maxSize;
	private double growTimer;
	private double fadeTimer;
	private double size;
	private double xScale, yScale;
	private boolean finished;
	private int opacity;
	
	public Bounce(Vector3 position, Vector3 normal) {
		x = (int)Arena.getDistanceAtDepth(position.x, position.z);
		y = (int)Arena.getDistanceAtDepth(position.y, position.z);
		maxSize = (int)Arena.getDistanceAtDepth(MAX_SIZE, position.z);
		xScale = 1; yScale = 1;
		if (Vector3.equals(normal, Vector3.i, true)) {
			xScale = 0.5;
		}
		if (Vector3.equals(normal, Vector3.j, true)) {
			yScale = 0.5;
		}
		growTimer = 0;
		fadeTimer = 0;
		size = 0;
		opacity = 255;
		finished = false;
	}
	
	public void update(double dt) {
		if (finished) return;
		if (fadeTimer >= FADE_DURATION && growTimer >= GROW_DURATION) finished = true;
		if (size < maxSize) {
			growTimer += dt;
			size = Math.min(maxSize, maxSize * growTimer / GROW_DURATION);
		} else {
			fadeTimer += dt;
			opacity = 255 - (int)Math.min(255, 255 * fadeTimer / FADE_DURATION);
		}
	}
	
	public void draw() {
		if (finished) return;
		jog.Graphics.setColour(255, 255, 255, opacity);
		jog.Graphics.ellipse(true, x, y, size * xScale, size * yScale);
	}

	public boolean hasFinished() {
		return finished;
	}

}
