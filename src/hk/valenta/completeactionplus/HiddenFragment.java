package hk.valenta.completeactionplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class HiddenFragment extends Fragment {

	ListView list;
	List<String> keys;
	List<String> values;
	HiddenAdapter adapter;
	int selectedIndex = -1;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// get view
		View layout = inflater.inflate(R.layout.fragment_hidden, container, false);

		// get current configuration
		SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		Map<String,?> all = pref.getAll();
		keys = new ArrayList<String>();
		values = new ArrayList<String>();
		
		// loop and add
		for (Map.Entry<String, ?> entry : all.entrySet()) {
			String key = entry.getKey(); 
			if (key.contains(";") && !key.endsWith("_fav")) {
				// add to list
				keys.add(key);
				values.add((String)entry.getValue());
			}
		}
		
		// find list
		list = (ListView)layout.findViewById(R.id.fragment_hidden_list);
		
		// setup adapter
		adapter = new HiddenAdapter(getActivity().getPackageManager());
		list.setAdapter(adapter);
		
		return layout;
	}
	
	public class HiddenAdapter extends BaseAdapter {
	
		final PackageManager pm;
		
		public HiddenAdapter(PackageManager pm) {
			this.pm = pm;
		}
		
		@Override
		public int getCount() {
			return keys.size();
		}

		@Override
		public Object getItem(int position) {
			return keys.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.list_rule_item, null);
			
			// split key into pairs
			String[] pairs = keys.get(position).split(";");
			
			// display info
			TextView tvAction = (TextView)view.findViewById(R.id.rule_item_action);
			tvAction.setText(pairs[0].substring(pairs[0].lastIndexOf(".") + 1));
			TextView tvType = (TextView)view.findViewById(R.id.rule_item_type);
			if (pairs[1].length() == 0) {
				tvType.setText(getString(R.string.manage_list_not_available));
			} else {
				tvType.setText(pairs[1]);
			}
			TextView tvScheme = (TextView)view.findViewById(R.id.rule_item_scheme);
			if (pairs[2].length() == 0) {
				tvScheme.setText(getString(R.string.manage_list_not_available));
			} else {
				tvScheme.setText(pairs[2]);
			}
			
			// apps
			String[] apps = values.get(position).split(";");
			LinearLayout list = (LinearLayout)view.findViewById(R.id.rule_item_apps);
			
			// loop
			FragmentActivity activity = getActivity();
			for (String a : apps) {
				// add to list
				RuleItemHelper.createRuleAppElement(activity, pm, list, a);
			}
			
			// delete button
			ImageButton delete = (ImageButton)view.findViewById(R.id.rule_item_delete_button);
			delete.setTag(keys.get(position));
			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// delete item
					String key = (String)v.getTag();
					selectedIndex = keys.indexOf(key);
					
					// confirmation
					AlertDialog confirm = new AlertDialog.Builder(v.getContext())
							.setTitle(R.string.manager_rule_delete)
							.setMessage(R.string.manager_rule_delete_confirm)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								@SuppressLint("WorldReadableFiles")
								@SuppressWarnings("deprecation")
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// delete
									String key = keys.get(selectedIndex);
									SharedPreferences pref = getActivity().getSharedPreferences("config", Context.MODE_WORLD_READABLE);
									pref.edit().remove(key).commit();
									keys.remove(selectedIndex);
									values.remove(selectedIndex);
									HiddenFragment.this.list.removeViews(selectedIndex, 1);					
									dialog.dismiss();
								}
							})
							.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// close
									dialog.dismiss();
								}
							})
							.create();
					confirm.show();				
				}
			});
			
			return view;
		}
	}
}
