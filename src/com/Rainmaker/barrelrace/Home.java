package com.Rainmaker.barrelrace;

import java.io.IOException;

import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class Home extends Activity implements OnClickListener {
	
	private Button startBtn;
	private Button exitBtn;
	private Button settingsBtn;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        startBtn = (Button) findViewById(R.id.start_button);
        startBtn.setOnClickListener(this);
        
        exitBtn = (Button) findViewById(R.id.exit_button);
        exitBtn.setOnClickListener(this);
        
        settingsBtn = (Button) findViewById(R.id.settings_button);
        settingsBtn.setOnClickListener(this);
        
        LinearLayout layout=(LinearLayout) findViewById(R.id.LinearLayout);
        try {
			Drawable d = Drawable.createFromStream(getAssets().open("background.png"), null);
			layout.setBackgroundColor(color.holo_blue_bright);
			layout.setBackgroundDrawable(d);
		} catch (IOException e) {
			e.printStackTrace();
		}
      } 
    
    public void onClick(View v) {
    	switch (v.getId())
    		{
    		case R.id.start_button:
    			startActivity(new Intent(Home.this, BouncingBallActivity.class));
    		case R.id.exit_button:
    			finish();
    		}
    }
}
