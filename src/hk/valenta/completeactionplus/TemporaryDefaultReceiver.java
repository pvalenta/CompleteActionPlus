package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class TemporaryDefaultReceiver extends BroadcastReceiver {

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		// get information about last started selected application
		String intentId = intent.getStringExtra("intentId");
		Long timeStamp = intent.getLongExtra("timeStamp", 0);
		String activity = intent.getStringExtra("activity");
		
		// save it configuration
		SharedPreferences pref = context.getSharedPreferences("temp", Context.MODE_WORLD_READABLE);
		pref.edit().putString(intentId, timeStamp + "_" + activity).apply();
		
		PackageManager pManager = context.getPackageManager();
		try {
			// get info
			ActivityInfo info = pManager.getActivityInfo(ComponentName.unflattenFromString(activity), PackageManager.GET_ACTIVITIES);
			pref = context.getSharedPreferences("config", Context.MODE_WORLD_READABLE);

			// show toast
			Toast.makeText(context, String.format(context.getString(R.string.set_temporary_default), info.loadLabel(pManager), pref.getInt("TemporaryTimeout", 5)), 
					Toast.LENGTH_LONG).show();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
