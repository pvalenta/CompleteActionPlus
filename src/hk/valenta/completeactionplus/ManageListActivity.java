package hk.valenta.completeactionplus;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ManageListActivity extends Activity {

	private Intent myIntent;
	private String intentId;
	private String[] names;
	private String[] items;
	private Boolean[] hidden;
	private boolean[] favourites;
	private ArrayList<String> hiddenItems;
	private ArrayList<String> favouriteItems;
	private PackageManager pManager;
	private int remain;
	private int minLimit = 2;
	private boolean noFavorite;
	private ArrayList<String> added;
	
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
		setContentView(R.layout.activity_manage_list);

		if (theme.equals("Dark")) {
			TextView warning = (TextView)findViewById(R.id.manage_list_warning);
			warning.setBackgroundColor(Color.parseColor("#303030"));
		}
		
		// only one
		if (pref.getBoolean("OnlyOneRule", false)) {
			minLimit = 1;
			TextView warning = (TextView)findViewById(R.id.manage_list_warning);
			warning.setText(R.string.manage_list_warning_one);
		}
		
		// get intent
		myIntent = getIntent();
		final String action = myIntent.getStringExtra("action");
		final String type = myIntent.getStringExtra("type");
		final String scheme = myIntent.getStringExtra("scheme");
		intentId = String.format("%s;%s;%s", action, type, scheme);
		items = myIntent.getStringArrayExtra("items");
		names = new String[items.length];
		noFavorite = myIntent.getBooleanExtra("nofavorite", false);
		
		// display info
		TextView tvAction = (TextView)findViewById(R.id.manage_list_action_value);
		if (action == null) {
			tvAction.setText(getString(R.string.manage_list_not_available));
		} else {
			tvAction.setText(action.substring(action.lastIndexOf(".") + 1));
		}
		TextView tvType = (TextView)findViewById(R.id.manage_list_type_value);
		if (type == null) {
			tvType.setText(getString(R.string.manage_list_not_available));
		} else {
			tvType.setText(type);
		}
		TextView tvScheme = (TextView)findViewById(R.id.manage_list_scheme_value);
		if (scheme == null) {
			tvScheme.setText(getString(R.string.manage_list_not_available));
		} else {
			tvScheme.setText(scheme);
		}
		
		// setup hidden
		hidden = new Boolean[names.length];
		favourites = new boolean[names.length];
		remain = names.length;
		
		// get manager
		pManager = getPackageManager();
		
		// get current configuration
		String cHidden = pref.getString(intentId, null);
		String cFavourite = pref.getString(intentId + "_fav", null);
		String cAdd = pref.getString(intentId + "_add", null);
		
		// Initialise 
		hiddenItems = new ArrayList<String>();
		favouriteItems = new ArrayList<String>();
		
		// let's continue
		if (cHidden != null && cHidden.length() > 0) {
			// split by ;
			String[] hI = cHidden.split(";");
			for (String h : hI) {
				if (!hiddenItems.contains(h)) {
					// only unique
					hiddenItems.add(h);
				}
			}
			
			// add to the end
			ArrayList<String> rNames = new ArrayList<String>();
			ArrayList<String> rItems = new ArrayList<String>();
			ArrayList<Boolean> rHidden = new ArrayList<Boolean>();
			
			// add first current one in
			for (int i=0; i<names.length; i++) {
				try {
					if (items[i].contains("/")) {
						// activity
						ActivityInfo info = pManager.getActivityInfo(ComponentName.unflattenFromString(items[i]), PackageManager.GET_ACTIVITIES);
						rNames.add(info.loadLabel(pManager).toString());
					} else {
						// package
						PackageInfo info = pManager.getPackageInfo(items[i], PackageManager.GET_ACTIVITIES);
						rNames.add(info.applicationInfo.loadLabel(pManager).toString());
					}
					rItems.add(items[i]);
					rHidden.add(false);
				} catch (NameNotFoundException e) {
					// not care
				}
			}
			
			// let's add old one
			for (int i=0; i<hiddenItems.size(); i++) {
				try {
					if (hiddenItems.get(i).contains("/")) {
						// activity
						ActivityInfo info = pManager.getActivityInfo(ComponentName.unflattenFromString(hiddenItems.get(i)), PackageManager.GET_ACTIVITIES);
						rNames.add(info.loadLabel(pManager).toString());
					} else {
						// package
						PackageInfo info = pManager.getPackageInfo(hiddenItems.get(i), PackageManager.GET_ACTIVITIES);
						rNames.add(info.applicationInfo.loadLabel(pManager).toString());
					}
					rItems.add(hiddenItems.get(i));
					rHidden.add(true);
				} catch (NameNotFoundException e) {
					// not exist packages skip
				}
			}

			// set back
			favourites = new boolean[rNames.size()];
			names = new String[rNames.size()];
			rNames.toArray(names);
			items = new String[rNames.size()];
			rItems.toArray(items);
			hidden = new Boolean[rNames.size()];
			rHidden.toArray(hidden);
		} else {
			// add first current one in
			for (int i=0; i<items.length; i++) {
				try {
					if (items[i].contains("/")) {
						// activity
						ActivityInfo info = pManager.getActivityInfo(ComponentName.unflattenFromString(items[i]), PackageManager.GET_ACTIVITIES);
						names[i] = info.loadLabel(pManager).toString();
					} else {
						// package
						PackageInfo info = pManager.getPackageInfo(items[i], PackageManager.GET_ACTIVITIES);
						names[i] = info.applicationInfo.loadLabel(pManager).toString();
					}
				} catch (NameNotFoundException e) {
					// not care
				}
				hidden[i] = false;
			}
		}		
		if (cFavourite != null && cFavourite.length() > 0) {
			// split by ;
			String[] fI = cFavourite.split(";");
			for (String f : fI) {
				if (!favouriteItems.contains(f)) {
					// only unique
					favouriteItems.add(f);
				}
			}
			
			// preselect array
			for (int i=0; i<items.length; i++) {
				favourites[i] = (favouriteItems.contains(items[i]));
				if (!favourites[i] && items[i].contains("/")) {
					// try match only package name
					int slashIndex = items[i].indexOf('/');
					favourites[i] = favouriteItems.contains(items[i].substring(0, slashIndex));
				}
			}
		}
		if (cAdd != null && cAdd.length() > 0) {
			// split by ;
			added = new ArrayList<String>(Arrays.asList(cAdd.split(";")));
		}
		
		// get list
		ListView manageList = (ListView)findViewById(R.id.manage_list_items);
		ResolveListAdapter adapter = new ResolveListAdapter(this);
		manageList.setAdapter(adapter);
		
		// add to list
		boolean addFeature = pref.getBoolean("AddFeature", false);
		Button add = (Button)findViewById(R.id.manage_list_add);
		if (addFeature && !noFavorite) {
			add.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					// select activity
					Intent select = new Intent(getApplicationContext(), SelectListActivity.class);
					select.putExtra("action", action);
					select.putExtra("type", type);
					select.putExtra("scheme", scheme);
					select.putExtra("items", items);
					startActivity(select);
					finish();
				}
			});
		} else {
			add.setVisibility(View.GONE);
		}
	}
	
	private final class ResolveListAdapter extends BaseAdapter {

		private final LayoutInflater inflater;
		
		public ResolveListAdapter(Context context) {
			// setup inflater
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int position) {
			return items[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// inflate list item
			View item = inflater.inflate(R.layout.list_item, parent, false);
			
			// get controls
			ImageView icon = (ImageView)item.findViewById(R.id.list_item_icon);
			try {
				if (items[position].contains("/")) {
					// activity
					icon.setImageDrawable(pManager.getActivityIcon(ComponentName.unflattenFromString(items[position])));
				} else {
					// package
					icon.setImageDrawable(pManager.getApplicationIcon(items[position]));
				}
			} catch (NameNotFoundException e) {
				// should not happen
			}
			TextView text1 = (TextView)item.findViewById(R.id.list_item_title);
			text1.setText(names[position]);
			if (added != null && added.contains(items[position])) {
				// let's make it red
				text1.setTextColor(Color.RED);
			}
			ToggleButton button = (ToggleButton)item.findViewById(R.id.list_item_hide_button);
			button.setChecked(hidden[position] == false);
			button.setTag(position);
			ImageButton favButton = (ImageButton)item.findViewById(R.id.list_item_favourite_button);
			favButton.setTag(position);
			if (hidden[position] == true) {
				favButton.setVisibility(View.INVISIBLE);
				favButton.setImageResource(android.R.drawable.btn_star_big_off);
			} else if (favourites[position]) {
				favButton.setVisibility(View.VISIBLE);
				favButton.setImageResource(android.R.drawable.btn_star_big_on);
			} else {
				favButton.setVisibility(View.VISIBLE);
				favButton.setImageResource(android.R.drawable.btn_star_big_off);
			}
			if (noFavorite) {
				favButton.setVisibility(View.INVISIBLE);
			}
			
			// on clicks
			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@SuppressWarnings({ "deprecation", "unchecked" })
				@SuppressLint("WorldReadableFiles")
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// select
					int position = (Integer)buttonView.getTag();
					
					// get fav button
					RelativeLayout parent = (RelativeLayout)buttonView.getParent();
					ImageButton favButton = (ImageButton)parent.getChildAt(2);
					SharedPreferences pref = buttonView.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
					
					if (!buttonView.isChecked() && remain <= minLimit) {
						buttonView.setChecked(true);
						if (minLimit == 1) {
							Toast.makeText(buttonView.getContext(), getString(R.string.manage_list_warning_one),  Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(buttonView.getContext(), getString(R.string.manage_list_warning),  Toast.LENGTH_LONG).show();
						}
						return;
					} 
					hidden[position] = !buttonView.isChecked();		
					ArrayList<String> configAdd = new ArrayList<String>();
					if (added != null) {
						configAdd = (ArrayList<String>) added.clone();
					}
					int index;
					
					// remove or add to list & config
					if (!buttonView.isChecked()) {
						// add to list
						remain -= 1;
						if (added != null && added.contains(items[position])) {
							// remove from added
							index = added.indexOf(items[position]);
							if (index >= 0) {
								configAdd.remove(index);
							}
						} else {
							// add to hidden
							hiddenItems.add(items[position]);
						}
						
						// hide favourites button
						favourites[position] = false;
						favButton.setVisibility(View.INVISIBLE);
					} else {
						// remove from list
						remain += 1;
						if (added == null || !added.contains(items[position])) {
							index = hiddenItems.indexOf(items[position]);
							if (index >= 0) {
								hiddenItems.remove(index);
							} 
						}
						
						// remove from list
						index = favouriteItems.indexOf(items[position]);
						if (index >= 0) {
							favouriteItems.remove(items[position]);
						}
						
						// save in config
						if (favouriteItems.size() > 0) {
							pref.edit().putString(intentId + "_fav", TextUtils.join(";", favouriteItems)).apply();					
						} else {
							pref.edit().remove(intentId + "_fav").apply();
						}
						
						// enable favourites button
						if (!noFavorite) {
							favButton.setVisibility(View.VISIBLE);
							favButton.setImageResource(android.R.drawable.btn_star_big_off);
						}
					}
					
					// save it in config
					if (hiddenItems.size() > 0) {
						pref.edit().putString(intentId, TextUtils.join(";", hiddenItems)).apply();					
					} else {
						pref.edit().remove(intentId).apply();
					}
					
					// added?
					if (added != null) {
						if (configAdd.size() > 0) {
							pref.edit().putString(intentId + "_add", TextUtils.join(";", configAdd)).apply();
						} else {
							pref.edit().remove(intentId + "_add").apply();
						}
					}
				}
			});
			favButton.setOnClickListener(new OnClickListener() {
				
				@SuppressWarnings("deprecation")
				@SuppressLint("WorldReadableFiles")
				@Override
				public void onClick(View view) {
					// select
					int position = (Integer)view.getTag();
					favourites[position] = !favourites[position];
					
					// switch state
					ImageButton favButton = (ImageButton)view;
					if (favourites[position]) {
						// add to list
						favouriteItems.add(items[position]);
						favButton.setImageResource(android.R.drawable.btn_star_big_on);
					} else {
						// remove from list
						int index = favouriteItems.indexOf(items[position]);
						if (index >= 0) {
							favouriteItems.remove(items[position]);
						}
						favButton.setImageResource(android.R.drawable.btn_star_big_off);
					}

					// save it in config
					SharedPreferences pref = favButton.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
					if (favouriteItems.size() > 0) {
						pref.edit().putString(intentId + "_fav", TextUtils.join(";", favouriteItems)).apply();					
					} else {
						pref.edit().remove(intentId + "_fav").apply();
					}
				}
			});			
			
			return item;
		}		
	}
}
