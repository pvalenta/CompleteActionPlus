package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class TestIntentActivity extends Activity {

	private Intent myIntent;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// get current configuration
		SharedPreferences pref = this.getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		String theme = pref.getString("AppTheme", "Light");
		if (theme.equals("Dark")) {
			setTheme(android.R.style.Theme_Holo_NoActionBar);
		} else {
			setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
		}
		
		// super
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_intent);

		// get intent
		myIntent = getIntent();
		final String action = myIntent.getAction();
		final String type = myIntent.getType();
		final String scheme = myIntent.getScheme();
		
		// display info
		TextView tvAction = (TextView)findViewById(R.id.test_intent_action_value);
		if (action == null) {
			tvAction.setText(getString(R.string.manage_list_not_available));
		} else {
			tvAction.setText(action.substring(action.lastIndexOf(".") + 1));
		}
		TextView tvType = (TextView)findViewById(R.id.test_intent_type_value);
		if (type == null) {
			tvType.setText(getString(R.string.manage_list_not_available));
		} else {
			tvType.setText(type);
		}
		TextView tvScheme = (TextView)findViewById(R.id.test_intent_scheme_value);
		if (scheme == null) {
			tvScheme.setText(getString(R.string.manage_list_not_available));
		} else {
			tvScheme.setText(scheme);
		}		
	}	
}
