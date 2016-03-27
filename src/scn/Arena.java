package scn;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import lib.Vector3;

import cls.Ball;
import cls.Bounce;
import cls.Paddle;

public class Arena extends Scene {
	
	private enum BallFrame {
		OFF,
		DOTS,
		LINES,
		RECTANGLE;
	}
	
	private static Vector3 dimensions = new Vector3(400, 400, 300);
	private static double depthOfField = 1.5;
	private static double bounciness = 1;
	private static BallFrame ballFrame = BallFrame.DOTS;
	
	private ArrayList<Bounce> bounces;
	private Ball ball;
	private boolean hasBegun;
	private Paddle playerPaddle;
	
	@Override
	public void start() {
		ball = new Ball(0, 0, 0);
		playerPaddle = new Paddle(64, 64, (int)(dimensions.z/2));
		hasBegun = false;
		bounces = new ArrayList<Bounce>();
	}
	
	public static double getDistanceAtDepth(double distance, double depth) {
		// (-depth / 2) <= distance <= (depth / 2)
		double perspective = depth / (Arena.dimensions.z / 2);
		// -1 <= perspective <= 1
		double scale = Math.pow(depthOfField, perspective);
		// 1 / depthOfField <= scale <= depthOfField
		return scale * distance;
	}
	
	private void begin() {
		ball.begin();
		hasBegun = true;
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseKey) {
		if (mouseKey == MouseEvent.BUTTON1 && !hasBegun) {
			begin();
		}
	}
	
	@Override
	public void keyReleased(int key) {
		if (key == KeyEvent.VK_R) {
			ball.begin();
		}
		if (key == KeyEvent.VK_SHIFT) {
			int i = ballFrame.ordinal();
			i += 1;
			i %= BallFrame.values().length;
			ballFrame = BallFrame.values()[i];
		}
		if (key == KeyEvent.VK_1) {
			ball.spin = new Vector3(64, 64, 0).normalise();
		}
		
	}
	
	@Override
	public void update(double dt) {
		updateBall(dt);
		updatePaddle(dt);
		for (Bounce b : bounces) b.update(dt);
		for (int i = bounces.size() - 1; i >= 0; i --) {
			if (bounces.get(i).hasFinished()) bounces.remove(i);
		}
	}
	
	private void updateBall(double dt) {
		ball.update(dt);
		ball.updateCollisions(dimensions, bounciness, bounces);
		if (ball.position.z >= dimensions.z / 2 + ball.radius) {
			// TODO scoring
			begin();
			return;
		}
		if (ball.position.z >= dimensions.z / 2 && !playerPaddle.justHit) {
			int x = (int)getDistanceAtDepth(ball.position.x, ball.position.z);
			int y = (int)getDistanceAtDepth(ball.position.y, ball.position.z);
			if (x < playerPaddle.position.x - (playerPaddle.width / 2 + ball.radius)) return;
			if (x > playerPaddle.position.x + (playerPaddle.width / 2 + ball.radius)) return;
			if (y < playerPaddle.position.y - (playerPaddle.height / 2 + ball.radius)) return;
			if (y > playerPaddle.position.y + (playerPaddle.height / 2 + ball.radius)) return;
			Vector3 spin = playerPaddle.velocity.scale(10.0 / dimensions.x);
			System.out.println("Hit with spin " + spin);
			ball.velocity = Vector3.add(ball.velocity, spin);
			ball.spin = Vector3.add(ball.spin, spin.normalise());
			ball.bounce(Vector3.k.negate(), bounciness, null);
			playerPaddle.justHit = true;
		} else {
			playerPaddle.justHit = false;
		}
	}
	
	private void updatePaddle(double dt) {
		int ox = jog.Graphics.getWidth() / 2;
		int oy = jog.Graphics.getHeight() / 2;
		int x = jog.Input.getMouseX() - ox;
		int y = jog.Input.getMouseY() - oy;
		playerPaddle.setPosition(x, y);
		playerPaddle.update(dt);
	}

	@Override
	public void draw() {
		int ox = jog.Graphics.getWidth() / 2;
		int oy = jog.Graphics.getHeight() / 2;
		jog.Graphics.push();
		jog.Graphics.translate(ox, oy);
			jog.Graphics.setColour(0, 255, 255, 64);
			drawArena();
			jog.Graphics.setColour(255, 255, 255);
			drawBall();
			drawBallHelper();
			drawPaddles();
		jog.Graphics.pop();
	}
	
	private void drawArena() {
		int depth = (int)dimensions.z;
		Rectangle nearestRectangle = getRectangle(-depth / 2);
		jog.Graphics.rectangle(false, nearestRectangle);
		Rectangle farthestRectangle = getRectangle(depth / 2);
		jog.Graphics.rectangle(false, farthestRectangle);
		drawDiagonals(nearestRectangle, farthestRectangle);
		for (int i = -1; i <= 1; i ++) {
			Rectangle r = getRectangle(i * depth / 4);
			jog.Graphics.rectangle(false, r);
		}
		for (Bounce b : bounces) b.draw();
	}
	
	private void drawDiagonals(Rectangle near, Rectangle far) {
		int x1Near = near.x;
		int x2Near = near.x + near.width;
		int y1Near = near.y;
		int y2Near = near.y + near.height;
		
		int x1Far = far.x;
		int x2Far = far.x + far.width;
		int y1Far = far.y;
		int y2Far = far.y + far.height;
		
		jog.Graphics.line(x1Near, y1Near, x1Far, y1Far);
		jog.Graphics.line(x2Near, y1Near, x2Far, y1Far);
		jog.Graphics.line(x1Near, y2Near, x1Far, y2Far);
		jog.Graphics.line(x2Near, y2Near, x2Far, y2Far);
	}
	
	private Rectangle getRectangle(int depth) {
		int width = (int)getDistanceAtDepth(dimensions.x, depth);
		int height = (int)getDistanceAtDepth(dimensions.y, depth);
		int x = -width / 2;
		int y = -height / 2;
		return new Rectangle(x, y, width, height);
	}
	
	private void drawBall() {
		ball.draw();
	}
	
	private void drawBallHelper() {
		int x = (int)getDistanceAtDepth(dimensions.x, ball.position.z) / 2;
		int y = (int)getDistanceAtDepth(dimensions.y, ball.position.z) / 2;
		int bx = (int)getDistanceAtDepth(ball.position.x, ball.position.z);
		int by = (int)getDistanceAtDepth(ball.position.y, ball.position.z);
		switch (ballFrame) {
		case OFF: 
			break;
		case DOTS:
			jog.Graphics.point(-x, by);
			jog.Graphics.point(x, by);
			jog.Graphics.point(bx, -y);
			jog.Graphics.point(bx, y);
			break;
		case LINES:
			jog.Graphics.line(-x, by, x, by);
			jog.Graphics.line(bx, -y, bx, y);
			break;
		case RECTANGLE:
			jog.Graphics.rectangle(false, -x, -y, x * 2, y * 2);
			break;
		}
	}
	
	private void drawPaddles() {
		int depth = playerPaddle.depth;
		int minX = (int)getDistanceAtDepth(-dimensions.x / 2, depth);
		int minY = (int)getDistanceAtDepth(-dimensions.y / 2, depth);
		int maxX = (int)getDistanceAtDepth(dimensions.x / 2, depth);
		int maxY = (int)getDistanceAtDepth(dimensions.y / 2, depth);
		playerPaddle.draw(new Rectangle(minX, minY, maxX, maxY));
	}

}
