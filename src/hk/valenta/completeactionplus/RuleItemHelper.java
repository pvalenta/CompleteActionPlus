package hk.valenta.completeactionplus;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
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
			String name = null;
			Drawable icon = null;
			
			// exist in cache?
			ManagerPagerActivity mActivity = (ManagerPagerActivity)activity;
			if (mActivity.cachePackage.contains(pkg)) {
				// get it from cache
				int index = mActivity.cachePackage.indexOf(pkg);
				name = mActivity.cacheNames.get(index);
				icon = mActivity.cacheIcons.get(index);
			} else if (pkg.contains("/")) {
				// get activity info
				ActivityInfo aInfo = pm.getActivityInfo(ComponentName.unflattenFromString(pkg), PackageManager.GET_ACTIVITIES);
				name = aInfo.loadLabel(pm).toString();
				icon = aInfo.loadIcon(pm);
				
				// add to cache
				mActivity.cachePackage.add(pkg);
				mActivity.cacheNames.add(name);
				mActivity.cacheIcons.add(icon);
			} else {
				// get info
				PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
				name = info.applicationInfo.loadLabel(pm).toString();
				icon = info.applicationInfo.loadIcon(pm);
				
				// add to cache
				mActivity.cachePackage.add(pkg);
				mActivity.cacheNames.add(name);
				mActivity.cacheIcons.add(icon);
			}
			
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
			img.setImageDrawable(icon);
//			img.setImageResource(R.drawable.ic_launcher);
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
			text.setText(name);
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
