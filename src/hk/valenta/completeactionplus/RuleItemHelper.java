package hk.valenta.completeactionplus;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class RuleItemHelper {
	public final static void createRuleAppElement(FragmentActivity activity, PackageManager pm, LinearLayout list, String pkg) {
		try {
			// get information
			PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
			
			// setup layout
			LinearLayout layout = new LinearLayout(activity);
			layout.setOrientation(LinearLayout.VERTICAL);
			list.addView(layout);
			DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
			LinearLayout.LayoutParams params = (LayoutParams)layout.getLayoutParams();
			params.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics);
			params.height = LayoutParams.WRAP_CONTENT;		
			params.gravity = Gravity.TOP;
			
			// setup icon
			ImageView img = new ImageView(activity);
			img.setScaleType(ScaleType.FIT_XY);
			try {
				img.setImageDrawable(info.applicationInfo.loadIcon(pm));
			} catch (Exception e) {
				img.setImageResource(R.drawable.ic_launcher);
			}
			layout.addView(img);
			params = (LayoutParams)img.getLayoutParams();
			params.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
			params.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
			params.gravity = Gravity.CENTER_HORIZONTAL;
			
			// setup text
			TextView text = new TextView(activity);
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			text.setLines(2);
			text.setGravity(Gravity.CENTER);
			text.setText(info.applicationInfo.loadLabel(pm).toString());
			layout.addView(text);
			params = (LayoutParams)text.getLayoutParams();
			params.width = LayoutParams.WRAP_CONTENT;
			params.height = LayoutParams.WRAP_CONTENT;
			params.gravity = Gravity.CENTER_HORIZONTAL;
		} catch (NameNotFoundException e) {
			// nothing to do
			return;
		} catch (Exception e) {
			// not exist anymore
			e.printStackTrace();
		}
	}
}
