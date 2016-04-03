package scn;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import jog.Graphics.HorizontalAlign;

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
	private static double bounciness = 1.000001;
	private static BallFrame ballFrame = BallFrame.DOTS;
	
	private ArrayList<Bounce> bounces;
	private Ball ball;
	private boolean hasBegun;
	private Paddle playerPaddle;
	private Paddle oppenentPaddle;
	private int playerScore;
	private int opponentScore;
	
	@Override
	public void start() {
		ball = new Ball(0, 0, 0);
		playerPaddle = new Paddle(64, 64, (int)(dimensions.z/2));
		oppenentPaddle = new Paddle(64, 64, (int)(-dimensions.z/2));
		playerScore = 0;
		opponentScore = 0;
		hasBegun = false;
		bounces = new ArrayList<Bounce>();
	}
	
	public static double getDistanceAtDepth(double distance, double depth) {
		double scale = scaleAtDepth(depth);
		return distance * scale;
	}
	
	public static double getWorldPosition(double distance, double depth) {
		double scale = scaleAtDepth(depth);
		return distance / scale;
	}
	
	private static double scaleAtDepth(double depth) {
		// (-depth / 2) <= distance <= (depth / 2)
		double perspective = depth / (Arena.dimensions.z / 2);
		// -1 <= perspective <= 1
		double scale = Math.pow(depthOfField, perspective);
     	// 1 / depthOfField <= scale <= depthOfField
		return scale;
	}
	
	private void reset() {
		ball = new Ball(0, 0, 0);
		hasBegun = false;
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
		updatePlayer(dt);
		updateOpponent(dt);
		updateBall(dt);
		for (Bounce b : bounces) b.update(dt);
		for (int i = bounces.size() - 1; i >= 0; i --) {
			if (bounces.get(i).hasFinished()) bounces.remove(i);
		}
	}
	
	private void updatePlayer(double dt) {
		int ox = jog.Graphics.getWidth() / 2;
		int oy = jog.Graphics.getHeight() / 2;
		double x = jog.Input.getMouseX() - ox;
		double y = jog.Input.getMouseY() - oy;
		x = getWorldPosition(x, playerPaddle.depth);
		y = getWorldPosition(y, playerPaddle.depth);
		playerPaddle.setPosition(x, y);
		playerPaddle.update(dt);
	}

	private void updateOpponent(double dt) {
		double x = ball.position.x + ball.velocity.x * dt;
		double y = ball.position.y + ball.velocity.y * dt;
		oppenentPaddle.setPosition(x, y);
		oppenentPaddle.update(dt);
	}
	
	private void updateBall(double dt) {
		ball.update(dt);
		ball.updateCollisions(dimensions, bounciness, bounces);
		// Has anyone scored?
		if (ball.position.z >= dimensions.z / 2 + ball.radius && ball.velocity.z > 0) {
			opponentScore ++;
			System.out.printf("%s [%d].\n", ball.position.toString(), ball.radius);
			System.out.printf("%s [%d, %d].\n", playerPaddle.position.toString(), playerPaddle.width, playerPaddle.height);
			reset();
			return;
		}
		if (ball.position.z <= -dimensions.z / 2 - ball.radius && ball.velocity.z < 0) {
			playerScore ++;
			System.out.printf("%s [%d].\n", ball.position.toString(), ball.radius);
			System.out.printf("%s [%d, %d].\n", playerPaddle.position.toString(), playerPaddle.width, playerPaddle.height);
			reset();
			return;
		}
		// Has anyone hit the ball?
		if (ball.position.z >= dimensions.z / 2 && ball.velocity.z > 0) {
			boolean boost = jog.Input.isMouseDown(MouseEvent.BUTTON1);
			if (boost) bounciness *= 1.5; // TODO is this a good feature?
			updatePaddleHit(playerPaddle);
			if (boost) bounciness /= 1.5; // TODO well is it?
		}
		if (ball.position.z <= -dimensions.z / 2 && ball.velocity.z < 0) {
			updatePaddleHit(oppenentPaddle);
		}
	}
	
	private boolean updatePaddleHit(Paddle p) {
		int x = (int)ball.position.x;
		int y = (int)ball.position.y;
		if (x + ball.radius < p.position.x - (p.width / 2)) return false;
		if (x - ball.radius > p.position.x + (p.width / 2)) return false;
		if (y + ball.radius < p.position.y - (p.height / 2)) return false;
		if (y - ball.radius > p.position.y + (p.height / 2)) return false;

		p.justHit = true;
		Vector3 spin = p.velocity.scale(10.0 / dimensions.x);
		System.out.println("Hit with spin " + spin);
		ball.velocity = Vector3.add(ball.velocity, spin);
		ball.spin = Vector3.add(ball.spin, spin.normalise());
		ball.bounce(Vector3.k.negate(), bounciness, null);
		return true;
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
		drawScore();
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
		drawPaddle(playerPaddle);
		drawPaddle(oppenentPaddle);
	}
	
	private void drawPaddle(Paddle p) {
		int depth = p.depth;
		int minX = (int)getDistanceAtDepth(-dimensions.x / 2, depth);
		int minY = (int)getDistanceAtDepth(-dimensions.y / 2, depth);
		int maxX = (int)getDistanceAtDepth(dimensions.x / 2, depth);
		int maxY = (int)getDistanceAtDepth(dimensions.y / 2, depth);
		p.draw(new Rectangle(minX, minY, maxX, maxY));
	}
	
	private void drawScore() {
		jog.Graphics.print("Player", 32, 32, HorizontalAlign.LEFT);
		jog.Graphics.print("" + playerScore, 32, 64, HorizontalAlign.LEFT);
		jog.Graphics.print("Computer", jog.Graphics.getWidth() - 32, 32, HorizontalAlign.RIGHT);
		jog.Graphics.print("" + opponentScore, jog.Graphics.getWidth() - 32, 64, HorizontalAlign.RIGHT);
	}

}
