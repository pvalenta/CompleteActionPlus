package hk.valenta.completeactionplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class IntentRecorderActivity extends Activity {

	TextView stats;
	ListView list;
	RecordAdapter adapter;
	ToggleButton toggle;

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// get current configuration
		SharedPreferences pref = this.getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		int currentTheme = EnumConvert.themeIndex(pref.getString("AppTheme", "Light"));
		if (currentTheme == 1) {
			setTheme(android.R.style.Theme_Holo_NoActionBar);
		} else {
			setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
		}

		// super
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intent_recorder);

		// stats
		stats = (TextView) findViewById(R.id.intent_recorder_stats);

		// list view
		list = (ListView) findViewById(R.id.intent_recorder_items);

		// toggle button
		toggle = (ToggleButton) findViewById(R.id.intent_recorder_toggle);
		toggle.setChecked(pref.getBoolean("IntentRecord", false));
		toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				boolean record = buttonView.isChecked();
				NotificationManager mngr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				if (record) {
					// clean up file
					File file = new File(getFilesDir(), "record.log");
					if (file.exists()) {
						file.delete();
					}

					// Create notification
					NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(buttonView.getContext());
					notBuilder.setSmallIcon(R.drawable.ic_launcher);
					notBuilder.setContentTitle(getString(R.string.intent_recorder));
					notBuilder.setContentText(getString(R.string.intent_recording));
					notBuilder.setOngoing(true);
					notBuilder.setContentIntent(PendingIntent.getActivity(
							buttonView.getContext(), 0,
							new Intent(buttonView.getContext(),IntentRecorderActivity.class),
							Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
//							0));
//							Intent.FLAG_ACTIVITY_NO_HISTORY));

					// show it
					mngr.notify(0, notBuilder.build());
				} else {
					// close notification if exists
					mngr.cancelAll();
				}
				pref.edit().putBoolean("IntentRecord", record).apply();

				// refresh
				onResume();
			}
		});

		// refresh button
		Button refresh = (Button) findViewById(R.id.intent_recorder_refresh);
		refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// refresh
				onResume();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get items
		ArrayList<String> items = getRecords(this);

		// setup adapter
		if (adapter == null) {
			adapter = new RecordAdapter(items);
			list.setAdapter(adapter);
		} else {
			adapter.setItems(items);
			list.invalidate();
		}
		
		// write text
		if (!toggle.isChecked()) {
			stats.setText(R.string.intent_record_note);
		} else {
			stats.setText(String.format(getString(R.string.intent_record_captured), items.size()));
		}
	}

	public static ArrayList<String> getRecords(Context context) {
		ArrayList<String> records = new ArrayList<String>();

		File file = new File(context.getFilesDir(), "record.log");
		if (!file.exists())
			return records;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;

			// read each line
			while ((line = reader.readLine()) != null) {
				records.add(line);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// reverse and clean up
		ArrayList<String> items = new ArrayList<String>(records);
		Collections.reverse(items);

		// third list for cleaning
		records = new ArrayList<String>();
		for (int i = 0; i < items.size(); i++) {
			if (!records.contains(items.get(i))) {
				// add if not exist
				records.add(items.get(i));
			}
		}

		return records;
	}

	public class RecordAdapter extends BaseAdapter {

		ArrayList<String> items;

		public RecordAdapter(ArrayList<String> items) {
			this.items = items;
		}

		public void setItems(ArrayList<String> items) {
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint({ "ViewHolder", "InflateParams" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.list_record_item, null);

			// split key into pairs
			String[] pairs = items.get(position).split(";");

			// display info
			TextView tvAction = (TextView)view.findViewById(R.id.record_item_action);
			tvAction.setText(pairs[1].substring(pairs[1].lastIndexOf(".") + 1));
			TextView tvType = (TextView)view.findViewById(R.id.record_item_type);
			if (pairs[2].length() == 0) {
				tvType.setText(getString(R.string.manage_list_not_available));
			} else {
				tvType.setText(pairs[2]);
			}
			TextView tvScheme = (TextView)view.findViewById(R.id.record_item_scheme);
			if (pairs[3].length() == 0) {
				tvScheme.setText(getString(R.string.manage_list_not_available));
			} else {
				tvScheme.setText(pairs[3]);
			}

			// date
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
			Date date = new Date();
			try {
				date = df.parse(pairs[0]);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long epoch = date.getTime();
			
			// date
			TextView tvDate = (TextView)view.findViewById(R.id.record_item_date);
			tvDate.setText(DateUtils.getRelativeTimeSpanString(epoch, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));

			// app count
			TextView tvCount = (TextView)view.findViewById(R.id.record_item_app_count);
			tvCount.setText(String.format(getString(R.string.intent_record_number_apps), pairs.length - 4));			
			
			// delete button
			ImageButton edit = (ImageButton) view.findViewById(R.id.record_item_edit_button);
			edit.setTag(pairs);
//			edit.setEnabled(!toggle.isChecked());
			edit.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					// get item
					String[] pairs = (String[])v.getTag();
					
					// let's get array of packages
					ArrayList<String> items = new ArrayList<String>();
					int count = pairs.length;
					for (int i=4; i<count; i++) {
						items.add(pairs[i]);
					}
					
					// let's assemble intent
					Intent manage = new Intent(getApplicationContext(), ManageListActivity.class);
					manage.putExtra("action", pairs[1]);
					manage.putExtra("type", pairs[2]);
					manage.putExtra("scheme", pairs[3]);
					String[] aItems = new String[items.size()];
					manage.putExtra("items", items.toArray(aItems));
					manage.putExtra("nofavorite", true);
					
					// let's go there
					startActivity(manage);				
				}
			});
			
			return view;
		}
	}
}
