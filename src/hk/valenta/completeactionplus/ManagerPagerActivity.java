package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class ManagerPagerActivity extends FragmentActivity {

	ManagerPageAdapter pageAdapter;
	ViewPager viewPager;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
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
		setContentView(R.layout.activity_manager_pager);
		
		// setup pager
		pageAdapter = new ManagerPageAdapter(getSupportFragmentManager());
		viewPager = (ViewPager)findViewById(R.id.manager_pager_viewPager);
		viewPager.setAdapter(pageAdapter);
		viewPager.setCurrentItem(0);
	}	
	
	public class ManagerPageAdapter extends FragmentPagerAdapter {

		public ManagerPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// get title
			if (position == 0) {
				return getString(R.string.fragment_hidden);
			} else if (position == 1) {
				return getString(R.string.fragment_favorite);
			} else {
				return null;
			}
		}

		@Override
		public Fragment getItem(int position) {
			// get fragment
			if (position == 0) {
				return new HiddenFragment();
			} else if (position == 1) {
				return new FavoriteFragment();
			} else {
				return null;
			}
		}

		@Override
		public int getCount() {
			// number of pages
			return 2;
		}		
	}
}
