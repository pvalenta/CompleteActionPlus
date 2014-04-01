package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AdvancedFragment extends Fragment {

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
		
		return layout;
	}
}
