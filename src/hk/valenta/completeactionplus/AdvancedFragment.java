package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AdvancedFragment extends Fragment {

	TextView autostartValue;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// get view
		View layout = inflater.inflate(R.layout.fragment_advanced, container, false);
		
		// get current configuration
		SharedPreferences pref = this.getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		
		// only one
		CheckBox onlyOne = (CheckBox)layout.findViewById(R.id.fragment_advanced_allow_one_checkbox);
		onlyOne.setChecked(pref.getBoolean("OnlyOneRule", false));
		onlyOne.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("OnlyOneRule", buttonView.isChecked()).apply();
			}
		});
		
		// web domain
		CheckBox webDomain = (CheckBox)layout.findViewById(R.id.fragment_advanced_web_domain_checkbox);
		webDomain.setChecked(pref.getBoolean("RulePerWebDomain", false));
		webDomain.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("RulePerWebDomain", buttonView.isChecked()).apply();
			}
		});
		
		// old hide way
		CheckBox oldWayHide = (CheckBox)layout.findViewById(R.id.fragment_advanced_old_hide_checkbox);
		oldWayHide.setChecked(pref.getBoolean("OldWayHide", false));
		oldWayHide.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("OldWayHide", buttonView.isChecked()).apply();
			}
		});
		
		// autostart
		autostartValue = (TextView)layout.findViewById(R.id.fragment_advanced_timeout_value);
		SeekBar autostart = (SeekBar)layout.findViewById(R.id.fragment_advanced_timeout);
		int t = pref.getInt("AutoStart", 0);
		autostart.setProgress(t);
		autostartValue.setText(String.format("%s (%d)", getString(R.string.autostart_timeout), t));
		autostart.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// set it in preferences
				SharedPreferences pref = seekBar.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putInt("AutoStart", seekBar.getProgress()).apply();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// let's display it
				autostartValue.setText(String.format("%s (%d)", getString(R.string.autostart_timeout), progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// nothing				
			}
		});
		
		// populate position portrait
		Spinner positionPortraitSpinner = (Spinner)layout.findViewById(R.id.fragment_advanced_position_portrait_spinner);
		positionPortraitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("PositionPortrait", EnumConvert.positionName(pos)).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
			
		});
		ArrayAdapter<CharSequence> positionPortraitAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.dialog_gravity, android.R.layout.simple_spinner_item);
		positionPortraitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		positionPortraitSpinner.setAdapter(positionPortraitAdapter);
		
		// preselect
		positionPortraitSpinner.setSelection(EnumConvert.positionIndex(pref.getString("PositionPortrait", "Center")));				
		TextView positionPortraitLabel = (TextView)layout.findViewById(R.id.fragment_advanced_position_portrait_label);
		positionPortraitLabel.setText(String.format(getString(R.string.dialog_gravity), getString(R.string.portrait)));

		// populate position landscape
		Spinner positionLandscapeSpinner = (Spinner)layout.findViewById(R.id.fragment_advanced_position_landscape_spinner);
		positionLandscapeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("PositionLandscape", EnumConvert.positionName(pos)).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
			
		});
		ArrayAdapter<CharSequence> positionLandscapeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.dialog_gravity, android.R.layout.simple_spinner_item);
		positionLandscapeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		positionLandscapeSpinner.setAdapter(positionLandscapeAdapter);
		
		// preselect
		positionLandscapeSpinner.setSelection(EnumConvert.positionIndex(pref.getString("PositionLandscape", "Center")));				
		TextView positionLandscapeLabel = (TextView)layout.findViewById(R.id.fragment_advanced_position_landscape_label);
		positionLandscapeLabel.setText(String.format(getString(R.string.dialog_gravity), getString(R.string.landscape)));
		
		// add custom app
		CheckBox addFeature = (CheckBox)layout.findViewById(R.id.fragment_advanced_add_feature);
		addFeature.setChecked(pref.getBoolean("AddFeature", false));
		addFeature.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("AddFeature", buttonView.isChecked()).apply();
			}
		});
		
		// launcher icon
		CheckBox launcherIcon = (CheckBox)layout.findViewById(R.id.fragment_advanced_launcher_icon);
		launcherIcon.setChecked(pref.getBoolean("LauncherIcon", true));
		launcherIcon.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("LauncherIcon", buttonView.isChecked()).apply();
				
				// proceed
				int state = buttonView.isChecked() ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
				final ComponentName alias = new ComponentName(getActivity(), "hk.valenta.completeactionplus.MainPagerActivity-Alias");
				getActivity().getPackageManager().setComponentEnabledSetting(alias, state, PackageManager.DONT_KILL_APP);
			}
		});
		
		// add custom app
		CheckBox lastFirst = (CheckBox)layout.findViewById(R.id.fragment_advanced_last_first);
		lastFirst.setChecked(pref.getBoolean("LastFirst", false));
		lastFirst.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("LastFirst", buttonView.isChecked()).apply();
			}
		});		
		
		// indent recorder
		Button indentRecorder = (Button)layout.findViewById(R.id.fragment_advanced_indent_recorder);
		indentRecorder.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// launch recorder
				Intent recorder = new Intent(v.getContext(), IntentRecorderActivity.class);
				//recorder.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(recorder);
			}
		});
		
		return layout;
	}
}
