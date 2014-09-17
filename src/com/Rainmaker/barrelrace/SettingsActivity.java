package com.Rainmaker.barrelrace;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends Activity implements
		OnItemSelectedListener {

	private Spinner horseSpinner;
	private Spinner barrelSpinner;
	private Spinner bgSpinner;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		bgSpinner = (Spinner) findViewById(R.id.spinnerbg);
		barrelSpinner = (Spinner) findViewById(R.id.spinnerbarrel);
		horseSpinner = (Spinner) findViewById(R.id.spinnerhorse);
		
		ArrayAdapter<CharSequence> adapterBarrel = ArrayAdapter
				.createFromResource(this, R.array.barrel_color_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapterBarrel
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		barrelSpinner.setAdapter(adapterBarrel);

		ArrayAdapter<CharSequence> adapterHorse = ArrayAdapter
				.createFromResource(this, R.array.barrel_color_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapterHorse
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		horseSpinner.setAdapter(adapterHorse);

		ArrayAdapter<CharSequence> adapterBg = ArrayAdapter.createFromResource(
				this, R.array.bg_color_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapterBg
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		bgSpinner.setAdapter(adapterBg);
		bgSpinner.setOnItemSelectedListener(this);
		barrelSpinner.setOnItemSelectedListener(this);
		horseSpinner.setOnItemSelectedListener(this);
		
		
		SharedPreferences settingsPref = getSharedPreferences("settingColor", 0);
		editor = settingsPref.edit();

	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
*/

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// An item was selected. You can retrieve the selected item using
		switch (parent.getId()) {

		case R.id.spinnerbarrel:
			editor.putString("barrelColor", parent.getItemAtPosition(pos).toString());
			editor.commit();

			break;
		case R.id.spinnerhorse:

			editor.putString("horseColor", parent.getItemAtPosition(pos)
					.toString());
			editor.commit();
			break;
		case R.id.spinnerbg:
			editor.putString("bgColor", parent.getItemAtPosition(pos)
					.toString());
			editor.commit();
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
