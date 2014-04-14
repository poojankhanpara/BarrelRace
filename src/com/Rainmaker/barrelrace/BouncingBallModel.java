package com.Rainmaker.barrelrace;

import java.util.concurrent.atomic.AtomicReference;

import android.R.bool;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

/**
 * This data model tracks the width and height of the playing field along with
 * the current position of a ball.
 */
public class BouncingBallModel {
	// the ball speed is meters / second. When we draw to the screen,
	// 1 pixel represents 1 meter. That ends up too slow, so multiply
	// by this number. Bigger numbers speeds things up.
	private final float pixelsPerMeter = 10;

	private final int ballRadius;

	// these are public, so make sure you synchronize on LOCK
	// when reading these. I made them public since you need to
	// get both X and Y in pairs, and this is more efficient than
	// getter methods. With two getters, you'd still need to
	// synchronize.
	public float ballPixelX, ballPixelY;

	private int pixelWidth, pixelHeight;

	// values are in meters/second
	private float velocityX, velocityY;

	// typical values range from -10...10, but could be higher or lower if
	// the user moves the phone rapidly
	private float accelX, accelY;

	/**
	 * When the ball hits an edge, multiply the velocity by the rebound. A value
	 * of 1.0 means the ball bounces with 100% efficiency. Lower numbers
	 * simulate balls that don't bounce very much.
	 */
	private static final float rebound = 0.5f;

	// if the ball bounces and the velocity is less than this constant,
	// stop bouncing.
	private static final float STOP_BOUNCING_VELOCITY = 2f;

	private volatile long lastTimeMs = -1;

	public final Object LOCK = new Object();
	// private BouncingBallActivity bounceActivity;
	private static int BOTTOM_PADDING;

	private boolean bouncedX = false;
	private boolean bouncedY = false;

	private AtomicReference<Vibrator> vibratorRef = new AtomicReference<Vibrator>();
	long startTime = 0L;
	private String timeString;
	private long timeInMilliseconds = 0L;
	private long updatedTime = 0L;

	/**
	 * @return the timeString
	 */
	public String getTimeString() {
		return timeString;
	}

	

	public BouncingBallModel(int ballRadius) {
		this.ballRadius = ballRadius;
		startTime = SystemClock.uptimeMillis();
	}

	public void setAccel(float ax, float ay) {
		synchronized (LOCK) {
			this.accelX = ax;
			this.accelY = ay;
		}
	}

	public void setSize(int width, int height) {
		synchronized (LOCK) {
			this.pixelWidth = width;
			this.pixelHeight = height;
		}
	}

	public int getBallRadius() {
		return ballRadius;
	}

	/**
	 * Call this to move the ball to a particular location on the screen. This
	 * resets the velocity to zero, but the acceleration doesn't change so the
	 * ball should start falling shortly.
	 */
	public void moveBall(int ballX, int ballY) {
		synchronized (LOCK) {
			this.ballPixelX = ballX;
			this.ballPixelY = ballY;
			velocityX = 0;
			velocityY = 0;
		}
	}

	public boolean isUserLost() {
		double distLX = BouncingBallActivity.barrelLeftX - ballPixelX;
		double distLY = BouncingBallActivity.barrelLeftY - ballPixelY;
		double distFromLeftBarrel = distLX * distLX + distLY * distLY;

		double distRX = BouncingBallActivity.barrelRightX - ballPixelX;
		double distRY = BouncingBallActivity.barrelRightY - ballPixelY;
		double distFromRightBarrel = distRX * distRX + distRY * distRY;

		double distMX = BouncingBallActivity.barrelMiddleX - ballPixelX;
		double distMY = BouncingBallActivity.barrelMiddleY - ballPixelY;
		double distFromMiddleBarrel = distMX * distMX + distMY * distMY;

		int sumOfRadius = ballRadius + BouncingBallActivity.BARREL_RADIUS;
		sumOfRadius = sumOfRadius * sumOfRadius;
		if (sumOfRadius >= distFromLeftBarrel
				|| sumOfRadius >= distFromMiddleBarrel
				|| sumOfRadius >= distFromRightBarrel) {
			moveBall((int) ballPixelX, (int) ballPixelY);
			return true;
		} else
			return false;

	}

	public void updatePhysics() {
		// copy everything to local vars (hence the 'l' prefix)
		float lWidth, lHeight, lBallX, lBallY, lAx, lAy, lVx, lVy;
		synchronized (LOCK) {
			BOTTOM_PADDING = BouncingBallActivity.BOTTOM_PADDING;
			lWidth = pixelWidth;
			lHeight = pixelHeight;
			lBallX = ballPixelX;
			lBallY = ballPixelY;
			lVx = velocityX;
			lVy = velocityY;
			lAx = accelX;
			lAy = -accelY;
		}

		if (lWidth <= 0 || lHeight <= 0) {
			// invalid width and height, nothing to do until the GUI comes up
			return;
		}

		long curTime = System.currentTimeMillis();
		if (lastTimeMs < 0) {
			lastTimeMs = curTime;
			return;
		}

		long elapsedMs = curTime - lastTimeMs;
		lastTimeMs = curTime;

		// update the velocity
		// (divide by 1000 to convert ms to seconds)
		// end result is meters / second
		lVx += ((elapsedMs * lAx) / 1000) * pixelsPerMeter;
		lVy += ((elapsedMs * lAy) / 1000) * pixelsPerMeter;

		// update the position
		// (velocity is meters/sec, so divide by 1000 again)
		lBallX += ((lVx * elapsedMs) / 1000) * pixelsPerMeter;
		lBallY += ((lVy * elapsedMs) / 1000) * pixelsPerMeter;

		bouncedX = false;
		bouncedY = false;

		if (lBallY - ballRadius < 0) {
			lBallY = ballRadius;
			lVy = -lVy * rebound;
			bouncedY = true;
		}

		else if (lBallY + ballRadius > (lHeight - BOTTOM_PADDING)) {
			lBallY = lHeight - ballRadius - BOTTOM_PADDING;
			lVy = -lVy * rebound;
			bouncedY = true;
		}
		if (bouncedY && Math.abs(lVy) < STOP_BOUNCING_VELOCITY) {
			lVy = 0;
			bouncedY = false;
		}

		if (lBallX - ballRadius < 0) {
			lBallX = ballRadius;
			lVx = -lVx * rebound;
			bouncedX = true;
		} else if (lBallX + ballRadius > lWidth) {
			lBallX = lWidth - ballRadius;
			lVx = -lVx * rebound;
			bouncedX = true;
		}
		if (bouncedX && Math.abs(lVx) < STOP_BOUNCING_VELOCITY) {
			lVx = 0;
			bouncedX = false;
		}

		// safely copy local vars back to object fields
		synchronized (LOCK) {
			ballPixelX = lBallX;
			ballPixelY = lBallY;
			velocityX = lVx;
			velocityY = lVy;
		}

		if (bouncedX || bouncedY) {
			Vibrator v = vibratorRef.get();
			startTime-=5000; // 5 seconds penalty 
			if (v != null) {
				v.vibrate(20L);
			}
		}

		// timer
		timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
		updatedTime = timeInMilliseconds;
		int secs = (int) (updatedTime / 1000);
		int mins = secs / 60;
		secs = secs % 60;
		int milliseconds = (int) (updatedTime % 1000);
		timeString= new String("" + mins + ":" + String.format("%02d", secs)
				+ ":" + String.format("%03d", milliseconds));
	}

	public void setVibrator(Vibrator v) {
		vibratorRef.set(v);
	}
}
