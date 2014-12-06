package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

public class MainPagerActivity extends FragmentActivity {

	SettingPageAdapter pageAdapter;
	ViewPager viewPager;
	int xposedVersion = 0;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// check status
		checkStatus();
		
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
		setContentView(R.layout.activity_main_pager);
		
		// setup pager
		pageAdapter = new SettingPageAdapter(getSupportFragmentManager());
		viewPager = (ViewPager)findViewById(R.id.main_pager_viewPager);
		viewPager.setAdapter(pageAdapter);
		viewPager.setCurrentItem(1);
		
		// get version number
		TextView version = (TextView)findViewById(R.id.main_pager_version);
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			version.setText(info.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			version.setText(e.getMessage());
		}
	}	
	
	public void openButtonClick(View view) {
		Intent showUrl = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2588306&goto=newpost"));
		startActivity(showUrl);
	}
	
	public void shareButtonClick(View view) {
		Intent shareUrl = new Intent(Intent.ACTION_SEND);
		shareUrl.setType("text/plain");
		shareUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareUrl.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
		shareUrl.putExtra(Intent.EXTRA_TEXT, "http://forum.xda-developers.com/showthread.php?t=2588306&goto=newpost");
		startActivity(Intent.createChooser(shareUrl, getString(R.string.activity_main_share_button)));
	}
	
	private void checkStatus() {
		// xposed installed?
		if (!existXposed()) {
			// show message
			AlertDialog alertXposed = new AlertDialog.Builder(this)
				.setMessage(R.string.xposed_not_installed)
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// we are done
						finish();
					}
				})
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// launch XDA website
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=1574401")));
						finish();
					}
				}).create();
			alertXposed.show();	
			return;
		}		

		// activated?
		String version = modVersion();
		if (version == null) {
			// show message
			AlertDialog alertActivate = new AlertDialog.Builder(this)
				.setMessage(R.string.mod_not_activated)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// continue to activation
						if (xposedVersion == 1) {
							Intent modules = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
							modules.setPackage("de.robv.android.xposed.installer");
							modules.putExtra("section", "modules");
							modules.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(modules);
						} else if (xposedVersion == 2) {
							Intent modules = new Intent(Intent.ACTION_MAIN);
							modules.setComponent(ComponentName.unflattenFromString("pro.burgerz.wsm.manager/pro.burgerz.wsm.manager.ModuleSettingActivity"));
							startActivity(modules);
						}
						finish();
					}
				}).create();
			alertActivate.show();	
			return;
		}
		
		// correct version?
		if (version != null && !version.equals("2.6.0")) {
			// show message
			AlertDialog alertActivate = new AlertDialog.Builder(this)
				.setMessage(R.string.not_yet_restarted)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// continue to activation
						if (xposedVersion == 1) {
							Intent modules = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
							modules.setPackage("de.robv.android.xposed.installer");
							modules.putExtra("section", "install");
							modules.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(modules);
						} else if (xposedVersion == 2) {
							Intent modules = new Intent(Intent.ACTION_MAIN);
							modules.setComponent(ComponentName.unflattenFromString("pro.burgerz.wsm.manager/pro.burgerz.wsm.manager.InstallerActivity"));
							startActivity(modules);
						}
						finish();
					}
				}).create();
			alertActivate.show();	
		}
	}
	
	private boolean existXposed() {
		try {
			if (getPackageManager().getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_META_DATA) != null) {
				xposedVersion = 1;
				return true;
			}
		} catch (NameNotFoundException e1) {
			// not found package
		}
		try {
			if (getPackageManager().getPackageInfo("pro.burgerz.wsm.manager", PackageManager.GET_META_DATA) != null) {
				xposedVersion = 2;
				return true;
			}
		} catch (NameNotFoundException e1) {
			// not found package
		}
		
		return false;
	}
	
	public static String modVersion() {
		return null;
	}
	
	public class SettingPageAdapter extends FragmentPagerAdapter {

		public SettingPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// get title
			if (position == 0) {
				return getString(R.string.fragment_about);
			} else if (position == 1) {
				return getString(R.string.fragment_dialog);
			} else if (position == 2) {
				return getString(R.string.fragment_layout);
			} else if (position == 3) {
				return getString(R.string.activity_main_advanced);
			} else {
				return null;
			}
		}

		@Override
		public Fragment getItem(int position) {
			// get fragment
			if (position == 0) {
				return new AboutFragment();
			} else if (position == 1) {
				return new DialogFragment();
			} else if (position == 2) {
				return new LayoutFragment();
			} else if (position == 3) {
				return new AdvancedFragment();
			} else {
				return null;
			}
		}

		@Override
		public int getCount() {
			// number of pages
			return 4;
		}		
	}
}
