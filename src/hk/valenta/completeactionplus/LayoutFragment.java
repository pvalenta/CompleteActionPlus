package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class LayoutFragment extends Fragment {

	RelativeLayout listGroup;
	RelativeLayout gridGroup;	
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// get view
		View layout = inflater.inflate(R.layout.fragment_layout, container, false);
		
		// get current configuration
		SharedPreferences pref = this.getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		
		// get groups
		listGroup = (RelativeLayout)layout.findViewById(R.id.fragment_layout_listLayout_group);
		gridGroup = (RelativeLayout)layout.findViewById(R.id.fragment_layout_gridLayout_group);
		
		// populate layout spinner
		Spinner layoutSpinner = (Spinner)layout.findViewById(R.id.fragment_layout_spinner);
		layoutSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// toggle groups
				toggleGroups(pos);
				
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("LayoutStyle", EnumConvert.layoutName(pos)).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
			
		});
		ArrayAdapter<CharSequence> layoutAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.change_layout_styles, android.R.layout.simple_spinner_item);
		layoutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		layoutSpinner.setAdapter(layoutAdapter);
		
		// preselect
		int layoutIndex = EnumConvert.layoutIndex(pref.getString("LayoutStyle", "Default"));
		layoutSpinner.setSelection(layoutIndex);		
		toggleGroups(layoutIndex);
		
		// populate list text size spinner
		Spinner listTextSize = (Spinner)layout.findViewById(R.id.fragment_layout_listTextSize_spinner);
		listTextSize.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("ListTextSize", EnumConvert.listTextSizeName(pos)).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do
			}
		});
		ArrayAdapter<CharSequence> listTextSizeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.list_text_size, android.R.layout.simple_spinner_item);
		listTextSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		listTextSize.setAdapter(listTextSizeAdapter);
		
		// preselect
		listTextSize.setSelection(EnumConvert.listTextSizeIndex(pref.getString("ListTextSize", "Regular")));
		
		// populate grid columns spinner (Portrait)
		TextView gridColumnsTV = (TextView)layout.findViewById(R.id.fragment_layout_gridColumns_label);
		gridColumnsTV.setText(String.format(getString(R.string.activity_main_grid_columns), getString(R.string.portrait)));
		Spinner gridColumns = (Spinner)layout.findViewById(R.id.fragment_layout_gridColumns_spinner);
		gridColumns.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("GridColumns", parent.getItemAtPosition(pos).toString()).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
		});
		ArrayAdapter<CharSequence> columnAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.grid_layout_columns, android.R.layout.simple_spinner_item);
		columnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gridColumns.setAdapter(columnAdapter);
		
		// preselect
		String nColumns = pref.getString("GridColumns", "3");
		if (nColumns.equals("6")) {
			nColumns = "5";
		}
		gridColumns.setSelection(columnAdapter.getPosition(nColumns));
		
		// populate grid columns spinner (Landscape)
		TextView gridColumnsTVls = (TextView)layout.findViewById(R.id.fragment_layout_gridColumnsLandscape_label);
		gridColumnsTVls.setText(String.format(getString(R.string.activity_main_grid_columns), getString(R.string.landscape)));
		Spinner gridColumnsLs = (Spinner)layout.findViewById(R.id.fragment_layout_gridColumnsLandscape_spinner);
		gridColumnsLs.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("GridColumnsLandscape", parent.getItemAtPosition(pos).toString()).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
		});
		ArrayAdapter<CharSequence> columnAdapterLs = ArrayAdapter.createFromResource(getActivity(), R.array.grid_layout_columns_landscape, android.R.layout.simple_spinner_item);
		columnAdapterLs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gridColumnsLs.setAdapter(columnAdapterLs);
		
		// preselect
		gridColumnsLs.setSelection(columnAdapterLs.getPosition(pref.getString("GridColumnsLandscape", "5")));
		
		// populate list text size spinner
		Spinner gridTextSize = (Spinner)layout.findViewById(R.id.fragment_layout_gridTextSize_spinner);
		gridTextSize.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("GridTextSize", EnumConvert.gridTextSizeName(pos)).apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do
			}
		});
		ArrayAdapter<CharSequence> gridTextSizeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.grid_text_size, android.R.layout.simple_spinner_item);
		gridTextSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gridTextSize.setAdapter(gridTextSizeAdapter);
		
		// preselect
		gridTextSize.setSelection(EnumConvert.gridTextSizeIndex(pref.getString("GridTextSize", "Regular")));		
		
		// reduce columns
		CheckBox reduceColumns = (CheckBox)layout.findViewById(R.id.fragment_layout_reduce_columns_checkbox);
		reduceColumns.setChecked(pref.getBoolean("DontReduceColumns", false));
		reduceColumns.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@SuppressLint("WorldReadableFiles")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("DontReduceColumns", buttonView.isChecked()).apply();
			}
		});
		
		return layout;
	}
	
	private void toggleGroups(int layoutIndex) {
		if (layoutIndex == 1) {
			listGroup.setVisibility(View.VISIBLE);
			gridGroup.setVisibility(View.GONE);
		} else if (layoutIndex == 2) {
			listGroup.setVisibility(View.GONE);
			gridGroup.setVisibility(View.VISIBLE);
		} else {
			listGroup.setVisibility(View.GONE);
			gridGroup.setVisibility(View.GONE);
		}
	}
}
