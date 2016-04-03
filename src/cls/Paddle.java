package cls;

import java.awt.Rectangle;

import scn.Arena;
import lib.Vector3;

public class Paddle {

	private static final double FADE_DURATION = 0.4;
	
	public final int width;
	public final int height;
	public final int depth;
	
	public Vector3 position;
	public Vector3 velocity;
	public boolean justHit;
	private double fadeTimer;
	private boolean fading;
	private int opacity;
	
	public Paddle(int width, int height, int depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		position = Vector3.ZERO;
		velocity = Vector3.ZERO;
		justHit = false;
		fading = false;
		fadeTimer = 0;
		opacity = 128;
	}
	
	public boolean hasJustHit() { return justHit; }
	
	public void update(double dt) {
		if (hasJustHit()) {
			fadeTimer = 0;
			fading = true;
			justHit = false;
		}
		if (fading) {
			fadeTimer += dt;
			opacity = 255 - (int)(128 * (fadeTimer / FADE_DURATION));
			if (opacity < 128) {
				opacity = 128;
				fading = false;
			}
		}
	}
	
	public void setPosition(double x, double y) {
		velocity = new Vector3(x - position.x, y - position.y, 0);
		position = new Vector3(x, y, depth);
	}
	
	public void draw(Rectangle bounds) {
		int scaledWidth = (int)Arena.getDistanceAtDepth(width, depth);
		int scaledHeight = (int)Arena.getDistanceAtDepth(height, depth);
		int x = (int)Arena.getDistanceAtDepth(position.x, depth) - scaledWidth / 2;
		int y = (int)Arena.getDistanceAtDepth(position.y, depth) - scaledHeight / 2;
		
		x = (int)Math.max(bounds.x, x);
		y = (int)Math.max(bounds.y, y);
		x = (int)Math.min(bounds.width, x + scaledWidth) - scaledWidth;
		y = (int)Math.min(bounds.height, y + scaledHeight) - scaledHeight;
		
		jog.Graphics.setColour(255, 255, 255, opacity);
		jog.Graphics.rectangle(true, x, y, scaledWidth, scaledHeight);
		jog.Graphics.setColour(255, 255, 255);
		jog.Graphics.rectangle(false, x, y, scaledWidth, scaledHeight);
	}

}
