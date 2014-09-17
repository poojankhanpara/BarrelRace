package com.Rainmaker.barrelrace;

import java.io.IOException;

import com.Rainmaker.barrelrace.R.id;

import android.R.color;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Class Name: BarrelRaceModel.java
 * 
 * @author Vaishali Shah, Poojan khanpara, Parth trivedi net id: vxs135730,
 *         pdk130130, pxt131830
 * 	This activity shows the menu of the Game
 * 	With menu Play, Settings and Highscores.
 */
public class Home extends Activity implements OnClickListener {

	private ImageButton startBtn;
	private ImageButton highscore;
	private ImageButton settingsBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		startBtn = (ImageButton) findViewById(R.id.start_button);
		// startBtn.setBackgroundDrawable(R.drawable.ic_start_button);
		startBtn.setOnClickListener(this);

		highscore = (ImageButton) findViewById(R.id.highscore);
		highscore.setOnClickListener(this);

		settingsBtn = (ImageButton) findViewById(R.id.settings_button);
		settingsBtn.setOnClickListener(this);
		Dialog dialog = new Dialog(this);
		LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout);

		try {
			Drawable d = Drawable.createFromStream(
					getAssets().open("background.png"), null);
			layout.setBackgroundColor(color.holo_blue_bright);
			layout.setBackgroundDrawable(d);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_button:
			startActivity(new Intent(Home.this, BarrelRaceActivity.class));

			break;

		case R.id.highscore:

			SharedPreferences high = getSharedPreferences("findhighscore", 0);
			String score = high.getString("highscore", "NO SCORE AVAILABLE");
			Toast.makeText(this, score, Toast.LENGTH_SHORT).show();
			break;

		case R.id.settings_button:
			
			startActivity(new Intent(Home.this, SettingsActivity.class));
		}
	}
}
