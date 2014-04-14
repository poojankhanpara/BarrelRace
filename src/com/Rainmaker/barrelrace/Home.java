package com.Rainmaker.barrelrace;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class Home extends Activity implements OnClickListener {
	
	private Button bouncingBallBtn;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        bouncingBallBtn = (Button) findViewById(R.id.bouncing_ball_btn);
        bouncingBallBtn.setOnClickListener(this);
        LinearLayout layout=(LinearLayout) findViewById(R.id.LinearLayout);
        try {
			Drawable d = Drawable.createFromStream(getAssets().open("background.png"), null);
			layout.setBackgroundDrawable(d);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      } 
    
    public void onClick(View v) {
    		startActivity(new Intent(Home.this, BouncingBallActivity.class));
    }
}
