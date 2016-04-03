package cls;

import java.util.ArrayList;

import scn.Arena;
import lib.Vector3;

public class Ball {
	
	public final int radius = 16;
	public final int speed = 96;
	public final int spinSpeed = 32;
	
	public final Vector3 startPosition; 
	public Vector3 position;
	public Vector3 velocity;
	public Vector3 spin;
	
	public Ball(int x, int y, int z) {
		startPosition = new Vector3(x, y, z);
		position = startPosition;
		velocity = Vector3.ZERO;
		spin = Vector3.ZERO;
	}
	
	public void begin() {
		position = startPosition;
		int k = 0;
		if (Math.random() < 0.5) {	k = 1;} else { k = -1; }
			
		velocity = new Vector3(Math.random(), Math.random(), k).normalise();
		spin = Vector3.ZERO;
	}
	
	public void update(double dt) {
		position = Vector3.add(position, velocity.scale(dt * speed));
//		velocity = Vector3.add(velocity, spin.negate().scale(dt * spinSpeed));
//		spin = spin.scale(Math.pow(0.9, dt));
	}
	
	public void updateCollisions(Vector3 dimensions, double bounciness, ArrayList<Bounce> bounces) {
		if (position.x - radius < -dimensions.x / 2) {
			bounce(Vector3.i, bounciness, bounces);
		}
		if (position.x + radius > dimensions.x / 2) {
			bounce(Vector3.i.negate(), bounciness, bounces);
		}
		if (position.y - radius < -dimensions.y / 2) {
			bounce(Vector3.j, bounciness, bounces);
		}
		if (position.y + radius > dimensions.y / 2) {
			bounce(Vector3.j.negate(), bounciness, bounces);
		}
		// Don't bounce on the z-axis as the players should be doing that.
	}
	
	public void bounce(Vector3 normal, double bounciness, ArrayList<Bounce> bounces) {
		// R = 2*(V dot N)*N - V
		Vector3 n = normal;
		Vector3 v = velocity;
		double b = bounciness;
		velocity = Vector3.subtract(n.scale(Vector3.dotProduct(v, n) * 2), v).scale(-b);
		// TODO have certain bounces affect spin differently
		spin = spin.scale(0.8);
		if (bounces != null) {
			bounces.add(new Bounce(position, normal));
		}
	}
	
	public void draw() {
		int radius = (int)Arena.getDistanceAtDepth(this.radius, position.z);
		int x = (int)Arena.getDistanceAtDepth(position.x, position.z);
		int y = (int)Arena.getDistanceAtDepth(position.y, position.z);
		jog.Graphics.circle(true, x, y, radius);
	}

}
