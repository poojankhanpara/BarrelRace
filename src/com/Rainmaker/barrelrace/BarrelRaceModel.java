package com.Rainmaker.barrelrace;

import java.util.concurrent.atomic.AtomicReference;

import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

/**
 * Class Name: BarrelRaceModel.java
 * 
 * @author Vaishali Shah, Poojan khanpara, Parth trivedi net id: vxs135730,
 *         pdk130130, pxt131830
 *  This data model tracks the width and height of the playing field along with
 *  the current position of a ball.
 * 	the ball speed is meters / second. When we draw to the screen,
 *	1 pixel represents 1 meter. That ends up too slow, so multiply
 *	by this number. Bigger numbers speeds things up.
 */
public class BarrelRaceModel {
	private final float pixelsPerMeter = 10;

	private final int ballRadius;
	private String timeString;
	public String getTimeString() {
		return timeString;
	}

	public void setTimeString(String timeString) {
		this.timeString = timeString;
	}

	public long getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(long updatedTime) {
		this.updatedTime = updatedTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	private long timeInMilliseconds = 0L;
	private long updatedTime = 0L;
	long startTime = 0L;
	boolean barrelLCompleted = false;
	public boolean barrelRCompleted = false;
	public boolean barrelMCompleted = false;

	public int flagBarrelLeft[] = new int[4];
	public int flagBarrelRight[] = new int[4];
	public int flagBarrelMiddle[] = new int[4];
	public int roundStateChanged[] = new int[3];
	public static boolean alternateAxis = false;

	int[][] traceMatrix = null;
	
	// these are public, so make sure you synchronize on LOCK
	// when reading these. I made them public since you need to
	// get both X and Y in pairs, and this is more efficient than
	// getter methods. With two getters, you'd still need to
	// synchronize.
	public float ballPixelX, ballPixelY;

	private int pixelWidth, pixelHeight;

	public int getPixelWidth() {
		return pixelWidth;
	}

	public void setPixelWidth(int pixelWidth) {
		this.pixelWidth = pixelWidth;
	}

	public int getPixelHeight() {
		return pixelHeight;
	}

	public void setPixelHeight(int pixelHeight) {
		this.pixelHeight = pixelHeight;
	}

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
	
	

	

	/*
	 * public int flagBarrelLeft[] = new int[4]; public int flagBarrelRight[] =
	 * new int[4]; public int flagBarrelMiddle[] = new int[4]; public int
	 * roundStateChanged[] = new int[3]; public static boolean alternateAxis =
	 * false;
	 */
	
	/**
	 * @return the timeString
	 */
	
	
	public BarrelRaceModel(int ballRadius) {
		this.ballRadius = ballRadius;
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
			traceMatrix = new int[width + 100][height + 100];
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
		double distLX = BarrelRaceActivity.barrelLeftX - ballPixelX;
		double distLY = BarrelRaceActivity.barrelLeftY - ballPixelY;
		double distFromLeftBarrel = distLX * distLX + distLY * distLY;

		double distRX = BarrelRaceActivity.barrelRightX - ballPixelX;
		double distRY = BarrelRaceActivity.barrelRightY - ballPixelY;
		double distFromRightBarrel = distRX * distRX + distRY * distRY;

		double distMX = BarrelRaceActivity.barrelMiddleX - ballPixelX;
		double distMY = BarrelRaceActivity.barrelMiddleY - ballPixelY;
		double distFromMiddleBarrel = distMX * distMX + distMY * distMY;

		int sumOfRadius = ballRadius + BarrelRaceActivity.BARREL_RADIUS;
		sumOfRadius = sumOfRadius * sumOfRadius;
		if (sumOfRadius >= distFromLeftBarrel
				|| sumOfRadius >= distFromMiddleBarrel
				|| sumOfRadius >= distFromRightBarrel) {

			moveBall((int) ballPixelX + 1, (int) ballPixelY + 1); // stop the
																	// ball
			Vibrator v = vibratorRef.get();
			if (v != null) {
				v.vibrate(35L);
			}
			return true;
		} else
			return false;

	}

	public void updatePhysics() {

		// copy everything to local vars (hence the 'l' prefix)
		if ((accelX < 0 & velocityX > 0) || (accelX > 0 & velocityX < 0)) {
			accelX = accelX;
		}
		if ((accelY < 0 & velocityY < 0) || (accelY > 0 & velocityY > 0)) {
			accelY = accelY;
		}
		Log.d("tag", "inside update1");

		float lWidth, lHeight, lBallX, lBallY, lAx, lAy, lVx, lVy;
		synchronized (LOCK) {
			BOTTOM_PADDING = BarrelRaceActivity.BOTTOM_PADDING;
			lWidth = pixelWidth;
			lHeight = pixelHeight;
			lBallX = ballPixelX;
			lBallY = ballPixelY;
			lVx = velocityX;
			lVy = velocityY;
			lAx = accelX;
			lAy = -accelY;
		}
		Log.d("tag", "inside update2");
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

		// Code for circle
		int integerx = (int) Math.ceil(lBallX);
		int integery = (int) Math.ceil(lBallY);
		Log.d("tag", "inside update3" + integerx + integery);

		/*
		 * if (traceMatrix[integerx][integery] == 1) {
		 * 
		 * Log.w("insideMAtrix", "We found x and y " + integerx + " " +
		 * integery);
		 * 
		 * boolean foundXPoints = false; boolean foundYPoints = false; for (int
		 * i = barrelLeftX-40; i < barrelLeftX+40; i = i + 8) { boolean
		 * foundXPointIn5 = false; for (int a = 0; a < 8; a++) { boolean
		 * foundPointIn5AbovePoint = false; boolean foundPointIn5BelowPoint =
		 * false; for (int j = 0; j < barrelLeftY; j++) { if
		 * (traceMatrix[j][i+a] == 1) { foundPointIn5AbovePoint = true;
		 * Log.w("checkx","hiiiiiiiii"); break; } }
		 * 
		 * for (int j = barrelLeftY; j < pixelHeight; j++) { if
		 * (traceMatrix[j][i+a] == 1) { foundPointIn5BelowPoint = true;
		 * Log.w("checkx","hiiiiiiiii11111"); break; } } if
		 * (foundPointIn5BelowPoint & foundPointIn5AbovePoint) { foundXPointIn5
		 * = true; break;
		 * 
		 * } } if(!foundXPointIn5) break;
		 * 
		 * if(i+8>=barrelLeftX+40){ foundXPoints=true; }
		 * 
		 * 
		 * } for (int i = barrelLeftY-40; i < barrelLeftY+40; i = i + 8) {
		 * boolean foundYPointIn5 = false; for (int a = 0; a < 8; a++) { boolean
		 * foundPointIn5LeftPoint = false; boolean foundPointIn5RightPoint =
		 * false; for (int j = 0; j < barrelLeftX; j++) { if
		 * (traceMatrix[i+a][j] == 1) { foundPointIn5LeftPoint = true;
		 * Log.w("checky","hiiiiiiiii"); break; } }
		 * 
		 * for (int j = barrelLeftX; j < pixelWidth; j++) { if
		 * (traceMatrix[i+a][j] == 1) { foundPointIn5RightPoint = true;
		 * Log.w("checky","hiiiiiiiii111111111111111"); break; } } if
		 * (foundPointIn5LeftPoint & foundPointIn5RightPoint) { foundYPointIn5 =
		 * true; break; } } if (!foundYPointIn5) { break; }
		 * 
		 * if(i+8>=barrelLeftY+40){ foundYPoints=true; }
		 * 
		 * 
		 * 
		 * } if(foundXPoints & foundYPoints){ for(int i=0;i<pixelWidth+100;i++){
		 * for(int j=0;j<pixelHeight+100;j++){ traceMatrix[i][j]=0; } }
		 * barrelLCompleted=true; Log.d("checkLeft ", "true"); }
		 * 
		 * //for right barrel
		 * 
		 * 
		 * for (int i = barrelRightX-40; i < barrelRightX+40; i = i + 8) {
		 * boolean foundXPointIn5 = false; for (int a = 0; a < 8; a++) { boolean
		 * foundPointIn5AbovePoint = false; boolean foundPointIn5BelowPoint =
		 * false; for (int j = 0; j < barrelRightY; j++) { if
		 * (traceMatrix[j][i+a] == 1) { foundPointIn5AbovePoint = true;
		 * 
		 * break; } }
		 * 
		 * for (int j = barrelRightY; j < pixelHeight; j++) { if
		 * (traceMatrix[j][i+a] == 1) { foundPointIn5BelowPoint = true;
		 * 
		 * break; } } if (foundPointIn5BelowPoint & foundPointIn5AbovePoint) {
		 * foundXPointIn5 = true; break;
		 * 
		 * } } if(!foundXPointIn5) break;
		 * 
		 * if(i+8>=barrelRightX+40){ foundXPoints=true; }
		 * 
		 * 
		 * } for (int i = barrelRightY-40; i < barrelRightY+40; i = i + 8) {
		 * boolean foundYPointIn5 = false; for (int a = 0; a < 8; a++) { boolean
		 * foundPointIn5LeftPoint = false; boolean foundPointIn5RightPoint =
		 * false; for (int j = 0; j < barrelRightX; j++) { if (traceMatrix[i +
		 * a][j] == 1) { foundPointIn5LeftPoint = true;
		 * 
		 * break; } }
		 * 
		 * for (int j = barrelRightX; j < pixelWidth; j++) { if (traceMatrix[i +
		 * a][j] == 1) { foundPointIn5RightPoint = true;
		 * 
		 * break; } } if (foundPointIn5LeftPoint & foundPointIn5RightPoint) {
		 * foundYPointIn5 = true; break; } } if (!foundYPointIn5) { break; }
		 * if(i+8>=barrelRightY+40){ foundYPoints=true; }
		 * 
		 * 
		 * 
		 * } if(foundXPoints & foundYPoints){ for(int i=0;i<pixelWidth+100;i++){
		 * for(int j=0;j<pixelHeight+100;j++){ traceMatrix[i][j]=0; } }
		 * barrelRCompleted=true; }
		 * 
		 * //middle barrel for (int i = barrelMiddleX-40; i < barrelMiddleX+40;
		 * i = i + 8) { boolean foundXPointIn5 = false; for (int a = 0; a < 8;
		 * a++) { boolean foundPointIn5AbovePoint = false; boolean
		 * foundPointIn5BelowPoint = false; for (int j = 0; j < barrelMiddleY;
		 * j++) { if (traceMatrix[j][i+a] == 1) { foundPointIn5AbovePoint =
		 * true;
		 * 
		 * break; } }
		 * 
		 * for (int j = barrelMiddleY; j < pixelHeight; j++) { if
		 * (traceMatrix[j][i+a] == 1) { foundPointIn5BelowPoint = true;
		 * 
		 * break; } } if (foundPointIn5BelowPoint & foundPointIn5AbovePoint) {
		 * foundXPointIn5 = true; break;
		 * 
		 * } } if(!foundXPointIn5) break;
		 * 
		 * if(i+8>=barrelMiddleX+40){ foundXPoints=true; }
		 * 
		 * 
		 * } for (int i = barrelMiddleY-40; i < barrelMiddleY+40; i = i + 8) {
		 * boolean foundYPointIn5 = false; for (int a = 0; a < 8; a++) { boolean
		 * foundPointIn5LeftPoint = false; boolean foundPointIn5RightPoint =
		 * false; for (int j = 0; j < barrelMiddleX; j++) { if (traceMatrix[i +
		 * a][j] == 1) { foundPointIn5LeftPoint = true;
		 * 
		 * break; } }
		 * 
		 * for (int j = barrelMiddleX; j < pixelWidth; j++) { if (traceMatrix[i
		 * + a][j] == 1) { foundPointIn5RightPoint = true;
		 * 
		 * break; } } if (foundPointIn5LeftPoint & foundPointIn5RightPoint) {
		 * foundYPointIn5 = true; break; } } if (!foundYPointIn5) { break; }
		 * if(i+8>=barrelMiddleY+40){ foundYPoints=true; }
		 * 
		 * 
		 * 
		 * } if(foundXPoints & foundYPoints){ for(int i=0;i<pixelWidth+100;i++){
		 * for(int j=0;j<pixelHeight+100;j++){ traceMatrix[i][j]=0; } }
		 * barrelMCompleted=true; }
		 * 
		 * } else { traceMatrix[integerx][integery] = 1; Log.w("insideMAtrix",
		 * "We ENTERED x and y " + integerx + " " + integery);
		 * 
		 * }
		 */

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
			startTime -= 5000; // 5 seconds penalty
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
					timeString = new String("" + mins + ":" + String.format("%02d", secs)
							+ ":" + String.format("%03d", milliseconds));
					
		
	}

	public int[] isCompletedCircle() {
		float ballX, ballY;
		float barrelLeftX, barrelLeftY;
		float barrelRightX, barrelRightY;
		float barrelMiddleX, barrelMiddleY;

		synchronized (LOCK) {
			ballX = (int) ballPixelX;
			ballY = (int) ballPixelY;
			barrelLeftX = (int) BarrelRaceActivity.barrelLeftX;
			barrelLeftY = (int) BarrelRaceActivity.barrelLeftY;
			barrelMiddleX = (int) BarrelRaceActivity.barrelMiddleX;
			barrelMiddleY = (int) BarrelRaceActivity.barrelMiddleY;
			barrelRightX = (int) BarrelRaceActivity.barrelRightX;
			barrelRightY = (int) BarrelRaceActivity.barrelRightY;
		}

		// Logic for left ball to be circled

		if (ballX > (barrelLeftX - 100) && ballX <= barrelLeftX) {

			if (!alternateAxis) {
				flagBarrelLeft[0] = 1;
				alternateAxis = true;
			}

		}
		if (ballX < (barrelLeftX + 100) && ballX > barrelLeftX) {

			if (!alternateAxis) {
				flagBarrelLeft[1] = 1;
				alternateAxis = true;
			}

		}

		if (ballY > (barrelLeftY - 100) && ballY <= barrelLeftY) {

			if (alternateAxis) {
				flagBarrelLeft[2] = 1;
				alternateAxis = false;
			}

		}

		if (ballY < (barrelLeftY + 100) && ballY > barrelLeftY) {

			if (alternateAxis) {
				flagBarrelLeft[3] = 1;
				alternateAxis = false;
			}

		}

		Log.d("tag", "msg " + ballX + " " + ballY + " " + barrelLeftX + " "
				+ barrelLeftY + " " + flagBarrelLeft[0] + " "
				+ flagBarrelLeft[1] + " " + flagBarrelLeft[2] + " "
				+ flagBarrelLeft[3]);

		if (flagBarrelLeft[0] + flagBarrelLeft[1] + flagBarrelLeft[2]
				+ flagBarrelLeft[3] == 4) {
			roundStateChanged[0] = 1;

			flagBarrelLeft[0] = 0;
			flagBarrelLeft[1] = 0;
			flagBarrelLeft[2] = 0;
			flagBarrelLeft[3] = 0;

			flagBarrelRight[0] = 0;
			flagBarrelRight[1] = 0;
			flagBarrelRight[2] = 0;
			flagBarrelRight[3] = 0;

			flagBarrelMiddle[0] = 0;
			flagBarrelMiddle[1] = 0;
			flagBarrelMiddle[2] = 0;
			flagBarrelMiddle[3] = 0;

		}

		// For Right ball to be circled logic

		if (ballX > (barrelRightX - 100) && ballX <= barrelRightX) {

			if (!alternateAxis) {
				flagBarrelRight[0] = 1;
				alternateAxis = true;
			}

		}
		if (ballX < (barrelRightX + 100) && ballX > barrelRightX) {

			if (!alternateAxis) {
				flagBarrelRight[1] = 1;
				alternateAxis = true;
			}

		}

		if (ballY > (barrelRightY - 100) && ballY <= barrelRightY) {

			if (alternateAxis) {
				flagBarrelRight[2] = 1;
				alternateAxis = false;
			}

		}

		if (ballY < (barrelRightY + 100) && ballY > barrelRightY) {

			if (alternateAxis) {
				flagBarrelRight[3] = 1;
				alternateAxis = false;
			}

		}

		Log.d("tag", "msg " + ballX + " " + ballY + " " + barrelRightX + " "
				+ barrelRightY + " " + flagBarrelRight[0] + " "
				+ flagBarrelRight[1] + " " + flagBarrelRight[2] + " "
				+ flagBarrelRight[3]);

		if (flagBarrelRight[0] + flagBarrelRight[1] + flagBarrelRight[2]
				+ flagBarrelRight[3] == 4) {
			roundStateChanged[1] = 1;

			flagBarrelLeft[0] = 0;
			flagBarrelLeft[1] = 0;
			flagBarrelLeft[2] = 0;
			flagBarrelLeft[3] = 0;

			flagBarrelRight[0] = 0;
			flagBarrelRight[1] = 0;
			flagBarrelRight[2] = 0;
			flagBarrelRight[3] = 0;

			flagBarrelMiddle[0] = 0;
			flagBarrelMiddle[1] = 0;
			flagBarrelMiddle[2] = 0;
			flagBarrelMiddle[3] = 0;

		}

		// Logic for Middle ball to be Circle

		if (ballX > (barrelMiddleX - 100) && ballX <= barrelMiddleX) {

			if (!alternateAxis) {
				flagBarrelMiddle[0] = 1;
				alternateAxis = true;
			}

		}
		if (ballX < (barrelMiddleX + 100) && ballX > barrelMiddleX) {

			if (!alternateAxis) {
				flagBarrelMiddle[1] = 1;
				alternateAxis = true;
			}

		}

		if (ballY > (barrelMiddleY - 100) && ballY <= barrelMiddleY) {

			if (alternateAxis) {
				flagBarrelMiddle[2] = 1;
				alternateAxis = false;
			}

		}

		if (ballY < (barrelMiddleY + 100) && ballY > barrelMiddleY) {

			if (alternateAxis) {
				flagBarrelMiddle[3] = 1;
				alternateAxis = false;
			}
		}
		Log.d("tag", "msg " + ballX + " " + ballY + " " + barrelMiddleX + " "
				+ barrelMiddleY + " " + flagBarrelMiddle[0] + " "
				+ flagBarrelMiddle[1] + " " + flagBarrelMiddle[2] + " "
				+ flagBarrelMiddle[3]);

		if (flagBarrelMiddle[0] + flagBarrelMiddle[1] + flagBarrelMiddle[2]
				+ flagBarrelMiddle[3] == 4) {
			roundStateChanged[2] = 1;

			flagBarrelLeft[0] = 0;
			flagBarrelLeft[1] = 0;
			flagBarrelLeft[2] = 0;
			flagBarrelLeft[3] = 0;

			flagBarrelRight[0] = 0;
			flagBarrelRight[1] = 0;
			flagBarrelRight[2] = 0;
			flagBarrelRight[3] = 0;

			flagBarrelMiddle[0] = 0;
			flagBarrelMiddle[1] = 0;
			flagBarrelMiddle[2] = 0;
			flagBarrelMiddle[3] = 0;

		}
		return roundStateChanged;
	}

	public void setVibrator(Vibrator v) {
		vibratorRef.set(v);
	}
}
