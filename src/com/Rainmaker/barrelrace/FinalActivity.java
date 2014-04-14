package com.Rainmaker.barrelrace;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FinalActivity extends Activity implements OnClickListener {
	
	private Button replay;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);
        replay = (Button) findViewById(R.id.replay);
        replay.setOnClickListener(this);
        LinearLayout layout=(LinearLayout) findViewById(R.id.LinearLayout);
        layout.setBackgroundColor(Color.LTGRAY);
        
        TextView txt1 = (TextView) findViewById(R.id.textView1);  
        Typeface font = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");  
        txt1.setTypeface(font, Typeface.BOLD);  
       
        TextView scoreTextView = (TextView) findViewById(R.id.scoreTextView);  
        scoreTextView.setTypeface(font,Typeface.BOLD);
        String time = (String)getIntent().getExtras().get("time");
        scoreTextView.setText(time); 
        }
    
    public void onClick(View v) {
    		startActivity(new Intent(FinalActivity.this, BouncingBallActivity.class));
    }
}
