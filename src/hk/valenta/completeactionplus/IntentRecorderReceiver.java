package hk.valenta.completeactionplus;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class IntentRecorderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// store in file
		FileOutputStream fileStream;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
		String record = String.format("%s;%s\n", dateFormat.format(new Date()), intent.getStringExtra("Intent"));
		
		try {
			fileStream = new FileOutputStream(new File(context.getFilesDir(), "record.log"), true);
			fileStream.write(record.getBytes());
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// toast it
		Toast.makeText(context, "Intent Recorded.", Toast.LENGTH_SHORT).show();
	}
}
