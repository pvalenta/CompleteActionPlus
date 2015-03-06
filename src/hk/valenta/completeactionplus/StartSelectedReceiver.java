package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartSelectedReceiver extends BroadcastReceiver {

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		// get information about last started selected application
		String intentId = intent.getStringExtra("intentId");
		//Long timeStamp = intent.getLongExtra("timeStamp", 0);
		String activity = intent.getStringExtra("activity");
		
		// save it configuration
		SharedPreferences pref = context.getSharedPreferences("started", Context.MODE_WORLD_READABLE);
		pref.edit().putString(intentId, activity).apply();
	}
}
