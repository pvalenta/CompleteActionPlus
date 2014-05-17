package hk.valenta.completeactionplus;

import hk.valenta.completeactionplus.ColorPicker.OnResultListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class DialogFragment extends Fragment {

	private RelativeLayout manageTriggerBlock;
	private RelativeLayout colorBlock;
	private View titleColorView;
	private View textColorView;
	private View backgroundColorView;
	private int titleColor;
	private int textColor;
	private int backgroundColor;
	private CheckBox alwaysCheckbox;
	private CheckBox keepButtonsCheckbox;
	private TextView transparencyValue;
	private TextView roundCornerValue;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// get view
		View layout = inflater.inflate(R.layout.fragment_dialog, container, false);
		
		// get current configuration
		SharedPreferences pref = this.getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		
		// populate layout theme
		Spinner layoutThemeSpinner = (Spinner)layout.findViewById(R.id.fragment_dialog_theme_spinner);
		layoutThemeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				int layoutThemeIndex = EnumConvert.layouThemeIndex(pref.getString("LayoutTheme", "Default"));
				boolean defaultColors = layoutThemeIndex != pos;
				pref.edit().putString("LayoutTheme", EnumConvert.layoutThemeName(pos)).commit();				
				showColorBlock(pos, defaultColors);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
			
		});
		ArrayAdapter<CharSequence> layoutThemeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.dialog_theme, android.R.layout.simple_spinner_item);
		layoutThemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		layoutThemeSpinner.setAdapter(layoutThemeAdapter);
		
		// preselect
		int layoutThemeIndex = EnumConvert.layouThemeIndex(pref.getString("LayoutTheme", "Default"));
		layoutThemeSpinner.setSelection(layoutThemeIndex);	

		// color block
		colorBlock = (RelativeLayout)layout.findViewById(R.id.fragment_dialog_color_block);
		titleColorView = layout.findViewById(R.id.fragment_dialog_color_title);
		textColorView = layout.findViewById(R.id.fragment_dialog_color_text);
		backgroundColorView = layout.findViewById(R.id.fragment_dialog_color_background);
		showColorBlock(layoutThemeIndex, false);
		
		// on color click
		titleColorView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View view) {
				ColorPicker picker = new ColorPicker(view.getContext(), titleColor, new OnResultListener() {			
					@Override
					public void OnDone(ColorPicker dialog, int color) {
						// set it
						titleColor = color;
						titleColorView.setBackgroundColor(color);
						SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
						pref.edit().putInt("TitleColor", titleColor).commit();
					}
					
					@Override
					public void OnCancel(ColorPicker dialog) {
					}
				});
				picker.show();
			}
		});
		textColorView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				ColorPicker picker = new ColorPicker(view.getContext(), textColor, new OnResultListener() {			
					@Override
					public void OnDone(ColorPicker dialog, int color) {
						// set it
						textColor = color;
						textColorView.setBackgroundColor(color);
						SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
						pref.edit().putInt("TextColor", textColor).commit();
					}
					
					@Override
					public void OnCancel(ColorPicker dialog) {
					}
				});
				picker.show();
			}
		});
		backgroundColorView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				ColorPicker picker = new ColorPicker(view.getContext(), backgroundColor, new OnResultListener() {			
					@Override
					public void OnDone(ColorPicker dialog, int color) {
						// set it
						backgroundColor = color;
						backgroundColorView.setBackgroundColor(color);
						SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
						pref.edit().putInt("BackgroundColor", backgroundColor).commit();
					}
					
					@Override
					public void OnCancel(ColorPicker dialog) {
					}
				});
				picker.show();
			}
		});
		
		// round corners
		roundCornerValue = (TextView)layout.findViewById(R.id.fragment_dialog_roundCorners_value);
		SeekBar roundCorner = (SeekBar)layout.findViewById(R.id.fragment_dialog_roundCorners_seek);
		int r = pref.getInt("RoundCorner", 0);
		roundCorner.setProgress(r);
		roundCornerValue.setText(String.format("%s (%d)", getString(R.string.round_corners), r));
		roundCorner.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// set it in preferences
				SharedPreferences pref = seekBar.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putInt("RoundCorner", seekBar.getProgress()).commit();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// let's display it
				roundCornerValue.setText(String.format("%s (%d)", getString(R.string.round_corners), progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// nothing				
			}
		});
		
		// populate position portrait
		Spinner positionPortraitSpinner = (Spinner)layout.findViewById(R.id.fragment_dialog_position_portrait_spinner);
		positionPortraitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("PositionPortrait", EnumConvert.positionName(pos)).commit();
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
		TextView positionPortraitLabel = (TextView)layout.findViewById(R.id.fragment_dialog_position_portrait_label);
		positionPortraitLabel.setText(String.format(getString(R.string.dialog_gravity), getString(R.string.portrait)));

		// populate position landscape
		Spinner positionLandscapeSpinner = (Spinner)layout.findViewById(R.id.fragment_dialog_position_landscape_spinner);
		positionLandscapeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("PositionLandscape", EnumConvert.positionName(pos)).commit();
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
		TextView positionLandscapeLabel = (TextView)layout.findViewById(R.id.fragment_dialog_position_landscape_label);
		positionLandscapeLabel.setText(String.format(getString(R.string.dialog_gravity), getString(R.string.landscape)));

		// populate position landscape
		Spinner manageTriggerSpinner = (Spinner)layout.findViewById(R.id.fragment_dialog_manage_style_spinner);
		manageTriggerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("ManageTriggerStyle", EnumConvert.manageTriggerName(pos)).commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
			
		});
		ArrayAdapter<CharSequence> manageTriggerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.manageTrigger, android.R.layout.simple_spinner_item);
		manageTriggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		manageTriggerSpinner.setAdapter(manageTriggerAdapter);
		
		// preselect
		manageTriggerSpinner.setSelection(EnumConvert.manageTriggerIndex(pref.getString("ManageTriggerStyle", "Wrench")));				
		
		// transparency
		transparencyValue = (TextView)layout.findViewById(R.id.fragment_dialog_transparency_value);
		SeekBar transparency = (SeekBar)layout.findViewById(R.id.fragment_dialog_trasparency_seek);
		int t = pref.getInt("Transparency", 0);
		transparency.setProgress(t);
		transparencyValue.setText(String.format("%s (%d%%)", getString(R.string.transparency), t));
		transparency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// set it in preferences
				SharedPreferences pref = seekBar.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putInt("Transparency", seekBar.getProgress()).commit();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// let's display it
				transparencyValue.setText(String.format("%s (%d%%)", getString(R.string.transparency), progress));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// nothing				
			}
		});
		
		// always checkbox
		alwaysCheckbox = (CheckBox)layout.findViewById(R.id.fragment_dialog_display_always_checkbox);
		alwaysCheckbox.setChecked(pref.getBoolean("ShowAlways", false));
		alwaysCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@SuppressLint("WorldReadableFiles")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("ShowAlways", buttonView.isChecked()).commit();
				if (buttonView.isChecked()) {
					// cannot be both at same time
					keepButtonsCheckbox.setChecked(false);
				}
			}
		});
		
		// keep buttons checkbox
		keepButtonsCheckbox = (CheckBox)layout.findViewById(R.id.fragment_dialog_keep_buttons_checkbox);
		keepButtonsCheckbox.setChecked(pref.getBoolean("KeepButtons", false));
		keepButtonsCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("KeepButtons", buttonView.isChecked()).commit();
				if (buttonView.isChecked()) {
					// cannot be both at same time
					alwaysCheckbox.setChecked(false);
				}
			}
		});
		
		// manage list		
		manageTriggerBlock = (RelativeLayout)layout.findViewById(R.id.fragment_dialog_manage_block);
		CheckBox manageList = (CheckBox)layout.findViewById(R.id.fragment_dialog_manage_list_checkbox);
		boolean manageListOn = pref.getBoolean("ManageList", false);
		manageList.setChecked(manageListOn);	
		showManageTriggerStyle(manageListOn);
		manageList.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@SuppressLint("WorldReadableFiles")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				boolean checked = buttonView.isChecked();
				pref.edit().putBoolean("ManageList", checked).commit();
				showManageTriggerStyle(checked);
			}
		});

		// populate long press
		Spinner longPressSpinner = (Spinner)layout.findViewById(R.id.fragment_dialog_long_press_spinner);
		longPressSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// set it in preferences
				SharedPreferences pref = parent.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString("LongPress", EnumConvert.longPressName(pos)).commit();				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do				
			}
			
		});
		String[] actions = getResources().getStringArray(R.array.long_press_actions);
		if (!existXHalo()) {
			// actions without XHalo
			String[] temp = new String[3];
			temp[0] = actions[0];
			temp[1] = actions[1];
			temp[2] = actions[2];
			actions = temp;
		}		
		ArrayAdapter<CharSequence> longPressAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, actions);
		longPressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		longPressSpinner.setAdapter(longPressAdapter);
		
		// preselect
		int longPressIndex = EnumConvert.longPressIndex(pref.getString("LongPress", "Nothing"));
		longPressSpinner.setSelection(longPressIndex);	
		
		return layout;
	}
	
	private boolean existXHalo() {
		PackageManager pm = getActivity().getPackageManager();
		try {
			if (pm.getPackageInfo("com.zst.xposed.halo.floatingwindow", PackageManager.GET_META_DATA) != null) {
				return true;
			}
		} catch (NameNotFoundException e1) {
			// not found package
		}
		
		return false;
	}	
	
	private void showManageTriggerStyle(boolean show) {
		if (show) {
			manageTriggerBlock.setVisibility(View.VISIBLE);
		} else {
			manageTriggerBlock.setVisibility(View.GONE);
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	private void showColorBlock(int layoutThemeIndex, boolean defaultColors) {
		SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		
		if (layoutThemeIndex == 0) {
			// default color
			colorBlock.setVisibility(View.GONE);
		} else if (layoutThemeIndex == 1) {
			// holo light
			colorBlock.setVisibility(View.VISIBLE);
			if (defaultColors) {
				// set default
				titleColor = Color.BLACK;
				textColor = Color.BLACK;
				backgroundColor = Color.WHITE;
				titleColorView.setBackgroundColor(titleColor);
				textColorView.setBackgroundColor(textColor);
				backgroundColorView.setBackgroundColor(backgroundColor);
				pref.edit().putInt("TitleColor", titleColor)
					.putInt("TextColor", textColor)
					.putInt("BackgroundColor", backgroundColor).commit();
			} else {
				// get current one
				titleColor = pref.getInt("TitleColor", Color.BLACK);
				textColor = pref.getInt("TextColor", Color.BLACK);
				backgroundColor = pref.getInt("BackgroundColor", Color.WHITE);
				titleColorView.setBackgroundColor(titleColor);
				textColorView.setBackgroundColor(textColor);
				backgroundColorView.setBackgroundColor(backgroundColor);
			}
		} else if (layoutThemeIndex == 2) {
			// holo dark
			colorBlock.setVisibility(View.VISIBLE);
			if (defaultColors) {
				// set default
				titleColor = Color.parseColor("#BEBEBE");
				textColor = Color.parseColor("#BEBEBE");
				backgroundColor = Color.parseColor("#101214");
				titleColorView.setBackgroundColor(titleColor);
				textColorView.setBackgroundColor(textColor);
				backgroundColorView.setBackgroundColor(backgroundColor);
				pref.edit().putInt("TitleColor", titleColor)
					.putInt("TextColor", textColor)
					.putInt("BackgroundColor", backgroundColor).commit();
			} else {
				// get current one
				titleColor = pref.getInt("TitleColor", Color.parseColor("#BEBEBE"));
				textColor = pref.getInt("TextColor", Color.parseColor("#BEBEBE"));
				backgroundColor = pref.getInt("BackgroundColor", Color.parseColor("#101214"));
				titleColorView.setBackgroundColor(titleColor);
				textColorView.setBackgroundColor(textColor);
				backgroundColorView.setBackgroundColor(backgroundColor);
			}
		}
	}
}
