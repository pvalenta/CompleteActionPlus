package hk.valenta.completeactionplus;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
		
		// get intent
		myIntent = getIntent();
		String action = myIntent.getStringExtra("action");
		String type = myIntent.getStringExtra("type");
		String scheme = myIntent.getStringExtra("scheme");
		intentId = String.format("%s;%s;%s", action, type, scheme);
		items = myIntent.getStringArrayExtra("items");
		names = new String[items.length];
		
		// display info
		TextView tvAction = (TextView)findViewById(R.id.manage_list_action_value);
		tvAction.setText(action.substring(action.lastIndexOf(".") + 1));
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
					PackageInfo info = pManager.getPackageInfo(items[i], PackageManager.GET_ACTIVITIES);
					rNames.add(info.applicationInfo.loadLabel(pManager).toString());
					rItems.add(items[i]);
					rHidden.add(false);
				} catch (NameNotFoundException e) {
					// not care
				}
			}
			
			// let's add old one
			for (int i=0; i<hiddenItems.size(); i++) {
				try {
					PackageInfo info = pManager.getPackageInfo(hiddenItems.get(i), PackageManager.GET_ACTIVITIES);
					rNames.add(info.applicationInfo.loadLabel(pManager).toString());
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
					PackageInfo info = pManager.getPackageInfo(items[i], PackageManager.GET_ACTIVITIES);
					names[i] = info.applicationInfo.loadLabel(pManager).toString();
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
			}
		}
		
		// get list
		ListView manageList = (ListView)findViewById(R.id.manage_list_items);
		ResolveListAdapter adapter = new ResolveListAdapter(this);
		manageList.setAdapter(adapter);
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// inflate list item
			View item = inflater.inflate(R.layout.list_item, parent, false);
			
			// get controls
			ImageView icon = (ImageView)item.findViewById(R.id.list_item_icon);
			try {
				icon.setImageDrawable(pManager.getApplicationIcon(items[position]));
			} catch (NameNotFoundException e) {
				// should not happen
			}
			TextView text1 = (TextView)item.findViewById(R.id.list_item_title);
			text1.setText(names[position]);
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
			
			// on clicks
			button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@SuppressLint("WorldReadableFiles")
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// select
					int position = (Integer)buttonView.getTag();
					
					// get fav button
					RelativeLayout parent = (RelativeLayout)buttonView.getParent();
					ImageButton favButton = (ImageButton)parent.getChildAt(2);
					SharedPreferences pref = buttonView.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
					
					if (!buttonView.isChecked() && remain < 3) {
						buttonView.setChecked(true);
						Toast.makeText(buttonView.getContext(), getString(R.string.manage_list_warning),  Toast.LENGTH_LONG).show();
						return;
					} 
					hidden[position] = !buttonView.isChecked();
					
					// remove or add to list & config
					if (!buttonView.isChecked()) {
						// add to list
						remain -= 1;
						hiddenItems.add(items[position]);
						
						// hide favourites button
						favourites[position] = false;
						favButton.setVisibility(View.INVISIBLE);
					} else {
						// remove from list
						remain += 1;
						int index = hiddenItems.indexOf(items[position]);
						if (index >= 0) {
							hiddenItems.remove(index);
						} 
						
						// remove from list
						index = favouriteItems.indexOf(items[position]);
						if (index >= 0) {
							favouriteItems.remove(items[position]);
						}
						
						// save in config
						if (favouriteItems.size() > 0) {
							pref.edit().putString(intentId + "_fav", TextUtils.join(";", favouriteItems)).commit();					
						} else {
							pref.edit().remove(intentId + "_fav").commit();
						}
						
						// enable favourites button
						favButton.setVisibility(View.VISIBLE);
						favButton.setImageResource(android.R.drawable.btn_star_big_off);
					}
					
					// save it in config
					if (hiddenItems.size() > 0) {
						pref.edit().putString(intentId, TextUtils.join(";", hiddenItems)).commit();					
					} else {
						pref.edit().remove(intentId).commit();
					}
				}
			});
			favButton.setOnClickListener(new OnClickListener() {
				
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
						pref.edit().putString(intentId + "_fav", TextUtils.join(";", favouriteItems)).commit();					
					} else {
						pref.edit().remove(intentId + "_fav").commit();
					}
				}
			});			
			
			return item;
		}		
	}
}
