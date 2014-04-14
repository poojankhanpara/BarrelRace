package com.Rainmaker.barrelrace;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * This activity shows a ball that bounces around. The phone's accelerometer
 * acts as gravity on the ball. When the ball hits the edge, it bounces back and
 * triggers the phone vibrator.
 */

public class BouncingBallActivity extends Activity implements Callback,
		SensorEventListener {
	private static final int BALL_RADIUS = 35;
	public static final int BARREL_RADIUS = 40;

	public static int BOTTOM_PADDING;
	private SurfaceView surface;
	private SurfaceHolder holder;
	private final BouncingBallModel model = new BouncingBallModel(BALL_RADIUS);
	private GameLoop gameLoop;
	private Paint backgroundPaint;
	private Paint borderPaint;
	private Paint barrelPaint;
	private Paint ballPaint;

	private long lastSensorUpdate = -1;

	public static int barrelLeftX;
	public static int barrelLeftY;
	public static int barrelRightX;
	public static int barrelRightY;
	public static int barrelMiddleX;
	public static int barrelMiddleY;
	private SensorManager mSensorManager;
	private Sensor mAccel;
	private TextView timeTextView;
	private Handler timeHandler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.bouncing_ball);
		// setting vibrator
		Vibrator vibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
		model.setVibrator(vibrator);
		
		// making court
		surface = (SurfaceView) findViewById(R.id.bouncing_ball_surface);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		// this weird logic makes it adaptable for landscape mode
		screenWidth = Math.min(screenHeight, screenWidth);

		LayoutParams lp = surface.getLayoutParams();
		lp.width = screenWidth;
		BOTTOM_PADDING = BALL_RADIUS * 2 + 10;
		lp.height = screenWidth + BOTTOM_PADDING; // To make square height =
													// width
		surface.setLayoutParams(lp);

		holder = surface.getHolder();
		surface.getHolder().addCallback(this);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_GAME);

		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.WHITE);

		ballPaint = new Paint();
		ballPaint.setColor(Color.BLUE);
		ballPaint.setAntiAlias(true);

		barrelPaint = new Paint();
		barrelPaint.setColor(Color.RED);
		barrelPaint.setAntiAlias(true);

		borderPaint = new Paint();
		borderPaint.setColor(Color.DKGRAY);
		borderPaint.setAntiAlias(true);
		borderPaint.setStrokeWidth(10);
		// Starting position of ball
		model.moveBall(lp.width / 2, lp.height);

		timeTextView = (TextView) findViewById(R.id.time);
		Typeface font = Typeface.createFromAsset(getAssets(), "LCD.ttf");
		timeTextView.setTypeface(font);
		//timer
		timeHandler.postDelayed(updateTimerThread, 0);
		
		}
	/**
	 * Updates the time of the game
	 */
	private Runnable updateTimerThread = new Runnable() {
		public void run() {
			timeTextView.setText(model.getTimeString());
			try {
				TimeUnit.MILLISECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeHandler.postDelayed(this, 0);
		}
	};
	@Override
	protected void onPause() {
		super.onPause();
		try {
			if (mSensorManager != null && this != null) {
				mSensorManager.unregisterListener(this);
			}
		} catch (Exception e) {
			Log.w("exceptions", e.getMessage());
		}
		model.setVibrator(null);
		model.setAccel(0, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_GAME);
		Vibrator vibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
		model.setVibrator(vibrator);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_GAME);
		Vibrator vibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
		model.setVibrator(vibrator);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mSensorManager.unregisterListener(this);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		model.setSize(width, height);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		gameLoop = new GameLoop();
		gameLoop.start();
	}

	private void draw() {
		// thread safety - the SurfaceView could go away while we are drawing

		Canvas c = null;
		try {
			c = holder.lockCanvas();

			// this needs to synchronize on something
			if (c != null) {
				doDraw(c);
			}
		} finally {
			if (c != null) {
				holder.unlockCanvasAndPost(c);
			}
		}
	}

	private void doDraw(Canvas c) {
		int width = c.getWidth();
		int height = c.getHeight();
		c.drawRect(0, 0, width, height, backgroundPaint); // White background

		c.drawLine(5, 0, 5, height, borderPaint); // left border
		c.drawLine(width - 5, 0, width - 5, height, borderPaint); // right
																	// border
		c.drawLine(0, 5, width, 5, borderPaint); // top border

		// Bottom border lines
		c.drawLine(0, height - BOTTOM_PADDING, width / 2 - BOTTOM_PADDING / 2,
				height - BOTTOM_PADDING, borderPaint);
		c.drawLine(width / 2 + BOTTOM_PADDING / 2, height - BOTTOM_PADDING,
				width, height - BOTTOM_PADDING, borderPaint);

		// Barrel Circles
		barrelLeftX = width / 4;
		barrelLeftY = height / 4;
		barrelRightX = width * 3 / 4;
		barrelRightY = height / 4;
		barrelMiddleX = width / 2;
		barrelMiddleY = height * 2 / 3;

		c.drawCircle(barrelLeftX, barrelLeftY, BARREL_RADIUS, barrelPaint);// top
																			// left
																			// barrel
		c.drawCircle(barrelRightX, barrelRightY, BARREL_RADIUS, barrelPaint);// top
																				// right
																				// barrel
		c.drawCircle(barrelMiddleX, barrelMiddleY, BARREL_RADIUS, barrelPaint);// bottom
																				// barrel

		c.drawCircle(model.ballPixelX, model.ballPixelY, BALL_RADIUS, ballPaint);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			model.setSize(0, 0);
			gameLoop.safeStop();
		} finally {
			gameLoop = null;
		}
	}

	private class GameLoop extends Thread {
		private volatile boolean running = true;

		public void run() {
			while (running) {
				try {
					// don't like this hardcoding
					TimeUnit.MILLISECONDS.sleep(5);

					draw();

					model.updatePhysics();

					// TODO Create a win condition and record time
					if (model.isUserLost()) {

						// create intent
						Intent lostIntent = new Intent(
								BouncingBallActivity.this, FinalActivity.class);
						lostIntent.putExtra("win", false);
						lostIntent.putExtra("time", model.getTimeString());
						startActivity(lostIntent);

						// stop updatephysics thread
						safeStop();
					}
				} catch (InterruptedException ie) {
					running = false;
				}
			}
		}

		public void safeStop() {
			running = false;
			interrupt();
		}
	}

	@Override
	/**
	 * Has no use for this application.
	 */
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**
	 * changes the model by setting the acceleration every 50ms.
	 */
	@Override
	public void onSensorChanged(SensorEvent evt) {
		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			if (lastSensorUpdate == -1 || (curTime - lastSensorUpdate) > 50) {
				lastSensorUpdate = curTime;
				model.setAccel(-evt.values[0], -evt.values[1]);
			}
		}
	}
}
