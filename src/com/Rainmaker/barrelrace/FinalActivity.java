package com.Rainmaker.barrelrace;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class Name: BarrelRaceModel.java
 * 
 * @author Vaishali Shah, Poojan khanpara, Parth trivedi net id: vxs135730,
 *         pdk130130, pxt131830 This activity shows the Final scores whether the
 *         user lost or won. Shows the highscore.
 * 
 */
public class FinalActivity extends Activity implements OnClickListener {

	private Button replay;
	private Button home;
	private Button exit;

	String winString = "Congrates!!";
	String loose = "Game Over!!!";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_final);
		
		replay = (Button) findViewById(R.id.replay);
		replay.setOnClickListener(this);

		home = (Button) findViewById(R.id.mainmenu);
		home.setOnClickListener(this);

	//	exit = (Button) findViewById(R.id.buttonexit);
		//exit.setOnClickListener(this);
		
		TextView txt1 = (TextView) findViewById(R.id.textView1);
		Typeface font = Typeface.createFromAsset(getAssets(),
				"Chantelli_Antiqua.ttf");
		txt1.setTypeface(font, Typeface.BOLD);

		TextView scoreTextView = (TextView) findViewById(R.id.scoreTextView);
		scoreTextView.setTypeface(font, Typeface.BOLD);
		String time = (String) getIntent().getExtras().get("time");

		TextView high = (TextView) findViewById(R.id.highTextView);
		high.setTypeface(font, Typeface.BOLD);
		SharedPreferences highPref = getSharedPreferences("findhighscore", 0);
		String score = highPref.getString("highscore", "NO SCORE AVAILABLE");
		high.setText(score);
		high.setTextColor(Color.LTGRAY);
		TextView resultView = (TextView) findViewById(R.id.result);
		resultView.setTypeface(font, Typeface.BOLD);
		
	
//		if ((boolean) getIntent().getExtras().get("win") == true) {
//			resultView.setText(winString);
//			scoreTextView.setText(time);
//			scoreTextView.setTextColor(Color.GREEN);
//			resultView.setTextColor(Color.GREEN);
//		} else {
//			resultView.setText(loose);
//			scoreTextView.setText(time);
//			scoreTextView.setTextColor(Color.RED);
//			resultView.setTextColor(Color.RED);
//		}

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.replay:
			startActivity(new Intent(FinalActivity.this,
					BarrelRaceActivity.class));
			finish();
			break;
		
		case R.id.mainmenu:
			startActivity(new Intent(FinalActivity.this,
					Home.class));
		}

		
	}
	@Override
	public void onBackPressed() {
	}
}
