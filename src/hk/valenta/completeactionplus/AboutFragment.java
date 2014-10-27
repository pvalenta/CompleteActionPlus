package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class AboutFragment extends Fragment {

	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// get view
		View layout = inflater.inflate(R.layout.fragment_about, container, false);
		
		// get current configuration
		SharedPreferences pref = this.getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		int currentTheme = EnumConvert.themeIndex(pref.getString("AppTheme", "Light"));
		
		// populate theme spinner
		Spinner themeSpinner = (Spinner)layout.findViewById(R.id.fragment_about_theme_spinner);
		themeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@SuppressLint("WorldReadableFiles")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("AppTheme", EnumConvert.themeName(pos)).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
		});
		ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.theme, android.R.layout.simple_spinner_item);
		themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		themeSpinner.setAdapter(themeAdapter);
		
		// preselect
		themeSpinner.setSelection(currentTheme);
		
		// debug 
		CheckBox debug = (CheckBox)layout.findViewById(R.id.fragment_about_debug_checkbox);
		debug.setChecked(pref.getBoolean("DebugLog", false));
		debug.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = buttonView.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("DebugLog", buttonView.isChecked()).apply();
			}
		});
		
		// show configuration manager
		Button manager = (Button)layout.findViewById(R.id.fragment_about_manager);
		manager.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// start manager activity
				startActivity(new Intent(getActivity(), ManagerPagerActivity.class));
			}
		});
		
		// setup XDA link
		TextView xda = (TextView)layout.findViewById(R.id.fragment_about_xda);
		xda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// launch XDA				
				Intent showUrl = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2588306&goto=newpost"));
				startActivity(showUrl);
			}
		});
		
		return layout;
	}
}
