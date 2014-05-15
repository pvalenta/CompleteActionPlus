package hk.valenta.completeactionplus;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AdvancedFragment extends Fragment {

	Spinner shareSpinner;
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
				pref.edit().putBoolean("OnlyOneRule", buttonView.isChecked()).commit();
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
				pref.edit().putBoolean("RulePerWebDomain", buttonView.isChecked()).commit();
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
				pref.edit().putBoolean("OldWayHide", buttonView.isChecked()).commit();
			}
		});
		
		// populate share spinner
		shareSpinner = (Spinner)layout.findViewById(R.id.fragment_advanced_share_spinner);
		ArrayAdapter<CharSequence> shareAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.appShare, android.R.layout.simple_spinner_item);
		shareAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		shareSpinner.setAdapter(shareAdapter);
		
		// share set
		Button shareSet = (Button)layout.findViewById(R.id.fragment_advanced_share_set);
		shareSet.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// gallery intent
				Intent target = getShareIntent();
				
				// find activities
				PackageManager pm = getActivity().getPackageManager();
				List<ResolveInfo> list = pm.queryIntentActivities(target, 0);
				
				// let's get array of packages
				ArrayList<String> items = new ArrayList<String>();
				int count = list.size();
				for (int i=0; i<count; i++) {
					if (!items.contains(list.get(i).activityInfo.packageName)) {
						items.add(list.get(i).activityInfo.packageName);
					}
				}
				
				// let's assemble intent
				Intent manage = new Intent(getActivity(), ManageListActivity.class);
				manage.putExtra("action", target.getAction());
				manage.putExtra("type", target.getType());
				String[] aItems = new String[items.size()];
				manage.putExtra("items", items.toArray(aItems));
				manage.putExtra("nofavorite", true);
				
				// let's go there
				startActivity(manage);
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
				pref.edit().putInt("AutoStart", seekBar.getProgress()).commit();
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
		
		return layout;
	}
	
	private Intent getShareIntent() {
		int shareIndex = shareSpinner.getSelectedItemPosition();
		
		// continue by index
		switch (shareIndex) {
		case 0:
			// single photo intent
			Intent photo1 = new Intent(Intent.ACTION_SEND);
			photo1.setType("image/jpeg");
			return photo1;
		case 1:
			// multiple photo intent
			Intent multiple1 = new Intent(Intent.ACTION_SEND_MULTIPLE);
			multiple1.setType("image/*");
			return multiple1;
		case 2:
			// multiple photo intent
			Intent multiple2 = new Intent(Intent.ACTION_SEND_MULTIPLE);
			multiple2.setType("image/jpeg");
			return multiple2;
		case 3:
			// single photo intent
			Intent photo2 = new Intent(Intent.ACTION_SEND);
			photo2.setType("image/*");
			return photo2;
		default:
			return null;
		}
	}
}
