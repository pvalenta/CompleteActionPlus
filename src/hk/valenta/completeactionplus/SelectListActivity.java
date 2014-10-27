package hk.valenta.completeactionplus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SelectListActivity extends Activity {

	private PackageManager pManager;
	private List<ActivityInfo> activities;
	private boolean[] selection;
	ProgressBar progress;
	ListView selectList;
	String intentId; 
	
	@SuppressWarnings({ "deprecation" })
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
		setContentView(R.layout.activity_select_list);

		if (theme.equals("Dark")) {
			TextView warning = (TextView)findViewById(R.id.manage_list_warning);
			warning.setBackgroundColor(Color.parseColor("#303030"));
		}
			
		// get intent
		Intent myIntent = getIntent();
		String action = myIntent.getStringExtra("action");
		String type = myIntent.getStringExtra("type");
		String scheme = myIntent.getStringExtra("scheme");
		intentId = String.format("%s;%s;%s", action, type, scheme);
		String[] items = myIntent.getStringArrayExtra("items");
		
		// display info
		TextView tvAction = (TextView)findViewById(R.id.select_list_action_value);
		if (action == null) {
			tvAction.setText(getString(R.string.manage_list_not_available));
		} else {
			tvAction.setText(action.substring(action.lastIndexOf(".") + 1));
		}
		TextView tvType = (TextView)findViewById(R.id.select_list_type_value);
		if (type == null) {
			tvType.setText(getString(R.string.manage_list_not_available));
		} else {
			tvType.setText(type);
		}
		TextView tvScheme = (TextView)findViewById(R.id.select_list_scheme_value);
		if (scheme == null) {
			tvScheme.setText(getString(R.string.manage_list_not_available));
		} else {
			tvScheme.setText(scheme);
		}
			
		// set list
		selectList = (ListView)findViewById(R.id.select_list_items);
		
		// load packages
		pManager = getPackageManager();	
		progress = (ProgressBar)findViewById(R.id.select_list_progress);	
		new LoadPackages().execute(items);
		
		// buttons
		Button cancel = (Button)findViewById(R.id.select_list_cancel_button);
		cancel.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// finish
				finish();
			}
		});
		Button ok = (Button)findViewById(R.id.select_list_ok_button);
		ok.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// get selection
				ArrayList<String> selected = new ArrayList<String>();
				for (int i=0; i<selection.length; i++) {
					if (selection[i]) {
						// add to list
						ActivityInfo info = activities.get(i);
						selected.add(info.packageName + "/" + info.name);
					}
				}
				if (selected.size() == 0) {
					Toast.makeText(v.getContext(), R.string.add_no_activity, Toast.LENGTH_SHORT).show();
					return;
				}
				
				// save it in config
				SharedPreferences pref = v.getContext().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putString(intentId + "_add", TextUtils.join(";", selected)).commit();					
				
				// set result
				finish();
			}
		});
	} 
	
	private class LoadPackages extends AsyncTask<String[], Integer, List<ActivityInfo>> {
		@Override
		protected List<ActivityInfo> doInBackground(String[]... params) {
			// get packages with activities
			List<PackageInfo> packages = pManager.getInstalledPackages(PackageManager.GET_ACTIVITIES);
			
			// build list of available activities
			ArrayList<ActivityInfo> availableActivities = new ArrayList<ActivityInfo>();
			
			// remove packages without activities or already exist packages
			int size = packages.size();
			for (int i=0; i<size; i++) {
				PackageInfo info = packages.get(i);
				if (info.activities == null) {
					continue;
				}
				boolean removed = false;
				for (String p : params[0]) {
					if (!p.contains("/") && p.equals(info.packageName)) {
						removed = true;
						break;
					}
				}
				if (removed) continue;
				for (int a=0; a<info.activities.length; a++) {
					// already existing?
					for (String p : params[0]) {
						if (p.contains("/") && p.equals(info.packageName + "/" + info.activities[a].name)) {
							removed = true;
							break;
						}
					}
					if (removed) {
						removed = false;
						continue;
					}
					
					// get activity info
					try {
						ActivityInfo moreInfo = pManager.getActivityInfo(new ComponentName(info.packageName, info.activities[a].name),
								0x00002000);
						if (moreInfo != null) {
							// Add to list
							availableActivities.add(moreInfo);
						}
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// sort it
			Collections.sort(availableActivities, new ActivitityComparator());
			
			// return it
			return availableActivities;
		}
		
		private class ActivitityComparator implements Comparator<ActivityInfo> {

			@Override
			public int compare(ActivityInfo lhs, ActivityInfo rhs) {
				// get names
				String lhsName = (String)lhs.loadLabel(pManager);
				String rhsName = (String)rhs.loadLabel(pManager);
				
				return lhsName.compareToIgnoreCase(rhsName);
			}
		}

		@Override
		protected void onPostExecute(List<ActivityInfo> result) {
			// hide progress
			progress.setVisibility(View.GONE);
			activities = result;
			selection = new boolean[activities.size()];
			
			// setup adapter			
			SelectActivityAdapter adapter = new SelectActivityAdapter(getApplicationContext());
			selectList.setAdapter(adapter);		
		}
	}
		
	private final class SelectActivityAdapter extends BaseAdapter {

		private final LayoutInflater inflater;
		
		public SelectActivityAdapter(Context context) {
			// setup inflater
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return selection.length;
		}

		@Override
		public Object getItem(int position) {
			return activities.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// inflate list item
			View item = inflater.inflate(R.layout.list_select_activity_item, parent, false);
			
			// get activity
			ActivityInfo aInfo = activities.get(position);
			
			// set controls
			CheckBox select = (CheckBox)item.findViewById(R.id.list_item_select);
			select.setChecked(selection[position]);
			select.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// remember
					selection[position] = buttonView.isChecked();
				}
			});
			ImageView icon = (ImageView)item.findViewById(R.id.list_item_icon);
			icon.setImageDrawable(aInfo.loadIcon(pManager));					
			TextView title = (TextView)item.findViewById(R.id.list_item_title);
			title.setText(aInfo.loadLabel(pManager));
			if (title.getText().length() == 0) {
				title.setVisibility(View.GONE);
			}
			TextView txtPackage = (TextView)item.findViewById(R.id.list_item_package);
			txtPackage.setText(aInfo.name);
			
			// get lists
			ArrayList<String> actions = aInfo.metaData.getStringArrayList("actions");
			ArrayList<String> dataTypes = aInfo.metaData.getStringArrayList("dataTypes");
			ArrayList<String> schemes = aInfo.metaData.getStringArrayList("schemes");
			TextView action = (TextView)item.findViewById(R.id.list_item_action);
			int actionSize = actions.size();
			if (actionSize > 0) {				
				for (int i=0; i<actionSize; i++) {
					String a = actions.get(i);
					if (a.contains(".")) {
						// keep only last part
						a = a.substring(a.lastIndexOf(".")+1);
						actions.set(i, a);
					}
				}
				action.setText(String.format("%s %s", getString(R.string.manage_list_action), TextUtils.join(";", actions)));
			} else {
				action.setVisibility(View.GONE);
			}
			TextView dataType = (TextView)item.findViewById(R.id.list_item_dataType);
			if (dataTypes.size() > 0) {
				dataType.setText(String.format("%s %s", getString(R.string.manage_list_type), TextUtils.join(";", dataTypes)));
			} else {
				dataType.setVisibility(View.GONE);
			}
			TextView scheme = (TextView)item.findViewById(R.id.list_item_scheme);
			if (schemes.size() > 0) {
				scheme.setText(String.format("%s %s", getString(R.string.manage_list_scheme), TextUtils.join(";", schemes)));
			} else {
				scheme.setVisibility(View.GONE);
			}
			
			return item;
		}		
	}
}
