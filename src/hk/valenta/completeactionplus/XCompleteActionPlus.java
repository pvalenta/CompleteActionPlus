package hk.valenta.completeactionplus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated.LayoutInflatedParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XCompleteActionPlus implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		// hook own configuration method		
		if (lpparam.packageName.equals("hk.valenta.completeactionplus")) {
			XposedHelpers.findAndHookMethod("hk.valenta.completeactionplus.MainPagerActivity", lpparam.classLoader, "modVersion", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					// return version number
					param.setResult("2.1.5");
					return "2.1.5";
				}
			});
		}
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity", null, "onItemClick", AdapterView.class, View.class, int.class, long.class,
				new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
				// invalid index?
				int position = (Integer)param.args[2];
				if (param.args[0] == null || position < 0) {
					// invalid call
					return null;
				}
				
				// are we correct class?
				Class<?> rObject = param.thisObject.getClass();
				while (!rObject.getName().equals("com.android.internal.app.ResolverActivity")) {
					rObject = rObject.getSuperclass();
				}

				// auto start?
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				int autoStart = pref.getInt("AutoStart", 0);
				boolean mAlwaysUseOption = XposedHelpers.getBooleanField(param.thisObject, "mAlwaysUseOption");
				if (autoStart > 0 && mAlwaysUseOption) {
					// make sure we got buttons hidden
					AdapterView<?> rControl = (AdapterView<?>)param.args[0];
					FrameLayout frame = (FrameLayout)rControl.getParent();
					LinearLayout root = (LinearLayout)frame.getParent();
					ProgressBar progress = (ProgressBar)root.getChildAt(0);
					progress.setVisibility(View.GONE);
				}
				
				// get method
				if (pref.getBoolean("KeepButtons", false) == true) {
					// simulate original method
					if (mAlwaysUseOption) {
						// enable buttons
						Button mAlwaysButton = (Button)XposedHelpers.getObjectField(param.thisObject, "mAlwaysButton");
						if (mAlwaysButton != null) {
							mAlwaysButton.setEnabled(true);
						}
						Button mOnceButton = (Button)XposedHelpers.getObjectField(param.thisObject, "mOnceButton");
						if (mOnceButton != null) {
							mOnceButton.setEnabled(true);
						}
					} else {
						// start it
						startSelected(param.thisObject, position, false);						
					}
					return null;
				}
				boolean showAlways = pref.getBoolean("ShowAlways", false);
				if (showAlways) {
					// get view
					boolean always = isAlwaysChecked((View)param.args[0]);
					boolean manageList = pref.getBoolean("ManageList", false);
					boolean oldWayHide = pref.getBoolean("OldWayHide", false);
					if (always && manageList && oldWayHide) {
						// restore items
						restoreListItems(param.thisObject, pref);
					}
					startSelected(param.thisObject, position, always);
				} else {
					// call it
					startSelected(param.thisObject, position, false);
				}
				
				return null;
			}
		});
		XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity", null, "onButtonClick", View.class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
				// call next step
				Class<?> rObject = param.thisObject.getClass();
				while (!rObject.getName().equals("com.android.internal.app.ResolverActivity")) {
					rObject = rObject.getSuperclass();
				}
				
				// let's find our resolver
				Field[] fields = rObject.getDeclaredFields();
				Field resolver = null;
				View rControl = null;
				for (Field f : fields) {
					String name = f.getName();
					if (name.equals("mListV") || name.equals("mGrid") || name.equals("mListView")) {
						resolver = f;
						
						// try to get control
						try {
							resolver.setAccessible(true);
							rControl = (View)resolver.get(param.thisObject);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (rControl != null) {
							break;
						} else {
							// not right yet
							rControl = null;
							resolver = null;
						}
					}
				}
				if (resolver == null) {
					XposedBridge.log("Resolver field not found.");
					return null;
				}
				if (rControl == null) {
					XposedBridge.log("Resolver field found, but it's null.");
					return null;
				}	
				
				// what we got?
				int selectedIndex = -1;
				if (resolver.get(param.thisObject).getClass().equals(GridView.class)) {
					// set it
					XposedBridge.log("Grid found.");
					GridView resGrid = (GridView)resolver.get(param.thisObject);
					selectedIndex = resGrid.getCheckedItemPosition();
				} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
					// set it
					XposedBridge.log("List found.");
					ListView resList = (ListView)resolver.get(param.thisObject);
					selectedIndex = resList.getCheckedItemPosition();
				}
				if (selectedIndex == -1) {
					XposedBridge.log("Nothing selected.");
					return null;
				}
				
				// always button?
				Button button = (Button)param.args[0];
				Button mAlwaysButton = (Button)XposedHelpers.getObjectField(param.thisObject, "mAlwaysButton");
				boolean always = (button.getId() == mAlwaysButton.getId());

				// restore items?
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				boolean oldWayHide = pref.getBoolean("OldWayHide", false);
				boolean manageList = pref.getBoolean("ManageList", false);
				if (always && manageList && oldWayHide) {
					// restore items
					restoreListItems(param.thisObject, pref);
				}
				
				// call it
				startSelected(param.thisObject, selectedIndex, always);
				
				return null;
			}
		});
		XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity", null, "onCreate", Bundle.class, Intent.class, CharSequence.class, 
				Intent[].class, List.class, boolean.class, new XC_MethodHook() {
			
			Unhook hookResolveAttribute = null;
			
			class FirstChoiceTimer extends CountDownTimer {

				private ProgressBar progressBar;		
				private Object resolver;
				
				public FirstChoiceTimer(ProgressBar progress, Object resolver, long millisInFuture, long countDownInterval) {
					super(millisInFuture, countDownInterval);
					// setup
					this.progressBar = progress;
					this.resolver = resolver;
				}

				@Override
				public void onTick(long millisUntilFinished) {
					// are we cancel?
					if (this.progressBar.getVisibility() == View.GONE) {
						// cancel
//						Handler mHandler = (Handler)XposedHelpers.getObjectField(this, "mHandler");
//						mHandler.removeMessages(1);
//						super.cancel();
						return;
					}
					
					// tick
					int p = this.progressBar.getProgress() + 1;
					this.progressBar.setProgress(p);
				}

				@Override
				public void onFinish() {
					// are we cancel?
					if (this.progressBar.getVisibility() == View.GONE) {
						// cancel
//						super.cancel();
						return;
					}
					// start it
					startSelected(resolver, 0, false);
				}
			}
					
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				hookResolveAttribute = XposedHelpers.findAndHookMethod(Resources.Theme.class, "resolveAttribute", int.class, TypedValue.class, boolean.class, new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						return false;
					}
				});
				
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				int transparency = pref.getInt("Transparency", 0);
				if (transparency > 0) {
					// let's get activity
					Activity activity = (Activity)param.thisObject;
					Window window = activity.getWindow();
					WindowManager.LayoutParams params = window.getAttributes();

					// set transparency
//					params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//					params.dimAmount = 0.8f;
					params.alpha = 1f - ((float)transparency / 100f);				
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// do we hook theme?
				if (hookResolveAttribute != null) {
					hookResolveAttribute.unhook();
				}
				
				// get current intent
				Intent myIntent = (Intent)param.args[1];
				
//				XposedBridge.log(String.format("Intent Action: %s Type: %s Scheme: %s", myIntent.getAction(), myIntent.getType(), myIntent.getScheme()));
				
				// are we correct class?
				Class<?> rObject = param.thisObject.getClass();
//				XposedBridge.log("Class: " + rObject.getName());
				while (!rObject.getName().equals("com.android.internal.app.ResolverActivity")) {
					rObject = rObject.getSuperclass();
				}
				
				// let's find our resolver
				Field[] fields = rObject.getDeclaredFields();
				Field resolver = null;
				View rControl = null;
				for (Field f : fields) {
					String name = f.getName();
					if (name.equals("mListV") || name.equals("mGrid") || name.equals("mListView")) {
						resolver = f;
						
						// try to get control
						resolver.setAccessible(true);						
						rControl = (View)resolver.get(param.thisObject);
						if (rControl != null) {
							break;
						} else {
							// not right yet
							rControl = null;
							resolver = null;
						}
					}
				}
				if (resolver == null) {
					XposedBridge.log("Resolver field not found.");
					return;
				}
				if (rControl == null) {
					XposedBridge.log("Resolver field found, but it's null.");
					return;
				}
				
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				boolean showAlways = pref.getBoolean("ShowAlways", false);
				
				// make sure we got buttons hidden
				FrameLayout frame = (FrameLayout)rControl.getParent();
				LinearLayout root = (LinearLayout)frame.getParent();
				if (root.getChildCount() == 2 && pref.getBoolean("KeepButtons", false) == false) {
					LinearLayout buttonBar = (LinearLayout)root.getChildAt(1);
					if (buttonBar != null) {
						if (!showAlways && buttonBar.getVisibility() != View.GONE) {
							// make sure it's gone
							hideElement(buttonBar);
						} else if (showAlways && buttonBar.getVisibility() == View.VISIBLE && buttonBar.getChildCount() == 3) {
							// make sure buttons are gone
							hideButtonBarButtons(buttonBar);
						}
					}
				}				
				
//				LG doesn't support always for NFC dialog				
//				else if (showAlways && buttonBar.getVisibility() != View.VISIBLE) {
//					// let's show it
//					buttonBar.setVisibility(View.VISIBLE);
//					
//					// do we have 3 items?
//					if (buttonBar.getChildCount() == 2 && buttonBar.getChildAt(0).getClass().equals(Button.class)) {
//						// missing always checkbox
//						Button ba = (Button)buttonBar.getChildAt(0);
//						addAlwaysCheckbox(buttonBar, ba.getText());
//					}
//					
//					// hide buttons
//					hideButtonBarButtons(buttonBar);
//				}
				
				// manage list?
				boolean manageList = pref.getBoolean("ManageList", false);
				String layoutStyle = pref.getString("LayoutStyle", "Default");
				String theme = pref.getString("LayoutTheme", "Default");
				if (manageList) {
					makeManageListButton(param, rObject, myIntent, !theme.equals("Default"), theme, pref);
				} else {
					themeTitleView(param, rObject, !theme.equals("Default"), theme, pref);
				}
				
				// dialog gravity
				Window currentWindow = (Window)XposedHelpers.callMethod(param.thisObject, "getWindow");
				setDialogGravity(root.getContext(), currentWindow, pref);
				currentWindow.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

				// timeout only for view dialog
				DisplayMetrics metrics = frame.getContext().getResources().getDisplayMetrics();
				int autoStart = pref.getInt("AutoStart", 0);
				if (autoStart > 0 && XposedHelpers.getBooleanField(param.thisObject, "mAlwaysUseOption")) {
					// add progress bar
					ProgressBar progress = new ProgressBar(frame.getContext(), null, android.R.attr.progressBarStyleHorizontal);
					progress.setMax(autoStart);
					progress.setProgress(1);
					progress.setPadding(0, 0, 0, 0);
					root.addView(progress, 0);
					LinearLayout.LayoutParams progParam = (LinearLayout.LayoutParams)progress.getLayoutParams();
					progParam.width = LayoutParams.MATCH_PARENT;
					progParam.height = LayoutParams.WRAP_CONTENT;
					progParam.setMargins(0 ,(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, metrics), 
							0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -6, metrics));

					// timer
					FirstChoiceTimer timer = new FirstChoiceTimer(progress, param.thisObject, autoStart * 1000, 1000);
					rControl.setTag(timer);
					timer.start();
				}
				
				// change layout?
				boolean activeXHalo = pref.getBoolean("ActiveXHalo", false);
				if (layoutStyle.equals("Default")) {
					if (activeXHalo) {
						if (resolver.get(param.thisObject).getClass().equals(GridView.class)) {
							// set it
							GridView resGrid = (GridView)resolver.get(param.thisObject);
							resGrid.setItemChecked(-1, true);
							setHaloWindow(resGrid);
						} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
							// set it
							ListView resList = (ListView)resolver.get(param.thisObject);
							resList.setItemChecked(-1, true);
							setHaloWindow(resList);
						}
					}
					
					return;
				}
				
				// nothing in layout changed
				if (frame.getChildCount() < 2) {
					if (resolver.get(param.thisObject).getClass().equals(GridView.class)) {
						// number of columns
						int columns = getColumnsNumber(frame.getContext(), pref);

						// set it
						GridView resGrid = (GridView)resolver.get(param.thisObject);
						resGrid.setNumColumns(columns);
						resGrid.setItemChecked(-1, true);
						if (activeXHalo) {
							setHaloWindow(resGrid);
						}
					} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
						// set it
						ListView resList = (ListView)resolver.get(param.thisObject);
						resList.setItemChecked(-1, true);
						if (activeXHalo) {
							setHaloWindow(resList);
						}
					}
					
					return;
				}

				// get adapter
				BaseAdapter adapter = (BaseAdapter)XposedHelpers.getObjectField(param.thisObject, "mAdapter");
				
				// let's get new layout
				if (layoutStyle.equals("List")) {
					ListView list = (ListView)frame.getChildAt(1);
					list.setAdapter(adapter);
					list.setItemChecked(-1, true);
					list.setOnItemClickListener((OnItemClickListener)param.thisObject);
					if (activeXHalo) {
						setHaloWindow(list);
					}
				} else if (layoutStyle.equals("Grid")) {
					GridView grid = (GridView)frame.getChildAt(1);
					int columns = getColumnsNumber(frame.getContext(), pref);
					int itemCounts = adapter.getCount();
					if (columns > itemCounts) {
						// reduce columns?
						columns = itemCounts;
						if (pref.getBoolean("DontReduceColumns", false)) {
							grid.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
							grid.setColumnWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics));
							grid.setStretchMode(GridView.NO_STRETCH);
						}
					}
					grid.setNumColumns(columns);
					grid.setAdapter(adapter);
					grid.setItemChecked(-1, true);
					grid.setOnItemClickListener((OnItemClickListener)param.thisObject);
					if (activeXHalo) {
						setHaloWindow(grid);
					}
				}
			}
		});
		XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity.ResolveListAdapter", null, "rebuildList", new XC_MethodHook() {
			@SuppressWarnings("unchecked")
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				boolean manageList = pref.getBoolean("ManageList", false);
				if (!manageList) return;
				
				// let's get intent
				Intent myIntent = (Intent)XposedHelpers.getObjectField(param.thisObject, "mIntent");
				String scheme = myIntent.getScheme();
				if (pref.getBoolean("RulePerWebDomain", false) && scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
					// add domain
					scheme = String.format("%s_%s", scheme ,myIntent.getData().getAuthority());
				} 
				String intentId = String.format("%s;%s;%s", myIntent.getAction(), myIntent.getType(), scheme);
				boolean oldWayHide = pref.getBoolean("OldWayHide", false);
				if (oldWayHide) {
					String cHidden = pref.getString(intentId, null);
					if (cHidden != null && cHidden.length() > 0) {
						// split by ;
						String[] hI = cHidden.split(";");
						ArrayList<String> hiddenItems = new ArrayList<String>();
						for (String h : hI) {
							if (!hiddenItems.contains(h)) {							
								hiddenItems.add(h);
							}
						}
						
						// get list
						List<Object> items = (List<Object>)XposedHelpers.getObjectField(param.thisObject, "mList");
						
						// get original list to solve 4.3 issue
						List<ResolveInfo> baseList = null;
						try {
							baseList = (List<ResolveInfo>)XposedHelpers.getObjectField(param.thisObject, "mBaseResolveList");
							if (baseList == null) {
								baseList = new ArrayList<ResolveInfo>();
							}
						} catch (Exception ex) { }
						
						// let's try to find
						for (String h : hiddenItems) {
							int count = items.size();
							for (int i=0; i<count; i++) {
								// get resolve info
								ResolveInfo info = (ResolveInfo)XposedHelpers.getObjectField(items.get(i), "ri");
								
								// match?
								if (info.activityInfo.packageName.equals(h)) {
									// store in original list for KitKat
									if (baseList != null) {
										baseList.add(info);
									}
									
									// remove it
									items.remove(i);
									i -= 1;
									count -= 1;
								}
							}
						}
					}					
				}
				
				// favourites
				String cFavorites = pref.getString(intentId + "_fav", null);
				if (cFavorites != null && cFavorites.length() > 0) {
					// split by ;
					String[] fI = cFavorites.split(";");
					ArrayList<String> favItems = new ArrayList<String>();
					for (String f : fI) {
						if (!favItems.contains(f)) {							
							favItems.add(f);
						}
					}
					
					// get list
					List<Object> items = (List<Object>)XposedHelpers.getObjectField(param.thisObject, "mList");
					
					// loop by favourites
					int favIndex = 0;
					int itemSize = items.size();
					for (int i=0; i<itemSize; i++) {
						// get resolve info
						Object o = items.get(i);
						ResolveInfo info = (ResolveInfo)XposedHelpers.getObjectField(o, "ri");
						
						// match?
						if (favItems.contains(info.activityInfo.packageName) && favIndex < i) {
							// take out
							items.remove(o);
							items.add(favIndex, o);
							
							// move up
							favIndex += 1;
						}
					}
				}
			}
		});
		XposedHelpers.findAndHookMethod("com.android.server.pm.PackageManagerService", null, "queryIntentActivities", 
				Intent.class, String.class, int.class, int.class, new XC_MethodHook() {
			@SuppressWarnings("unchecked")
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// let's get intent
				Intent myIntent = (Intent)param.args[0];
				if (myIntent == null) return;
				
				// any items back?
				List<ResolveInfo> list = (List<ResolveInfo>)param.getResult();
				if (list == null || list.size() == 0) return;

				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				boolean manageList = pref.getBoolean("ManageList", false);
				boolean debugOn = pref.getBoolean("DebugLog", false);
				if (!manageList) return;
				
				String scheme = myIntent.getScheme();
				if (pref.getBoolean("RulePerWebDomain", false) && scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
					// add domain
					scheme = String.format("%s_%s", scheme ,myIntent.getData().getAuthority());
				} 
				String type = myIntent.getType();
				if (scheme == null && type == null) return;
				String intentId = String.format("%s;%s;%s", myIntent.getAction(), type, scheme);
				
				// do it old way?
				boolean oldWayHide = pref.getBoolean("OldWayHide", false);
				if (oldWayHide) {
					// one of ours?
					if (!intentId.equals("android.intent.action.SEND;image/jpeg;null") &&
						!intentId.equals("android.intent.action.SEND;image/*;null") &&
						!intentId.equals("android.intent.action.SEND_MULTIPLE;image/*;null") &&
						!intentId.equals("android.intent.action.SEND_MULTIPLE;image/jpeg;null")) {
						if (debugOn) {
							XposedBridge.log("Hiding app old way.");
						}
						return;
					}
				}				
				
				// get hidden
				String cHidden = pref.getString(intentId, null);
				if (cHidden == null || cHidden.length() == 0) {
					// found
					if (debugOn) {
						XposedBridge.log(String.format("Found no match: %s", intentId));
					}
					return;
				}
				
				// found
				if (debugOn) {
					XposedBridge.log(String.format("Found match: %s", intentId));
				}
				
				// split by ;
				String[] hI = cHidden.split(";");
				ArrayList<String> hiddenItems = new ArrayList<String>();
				for (String h : hI) {
					if (!hiddenItems.contains(h)) {							
						hiddenItems.add(h);
					}
				}
				
				// loop & remove
				int size = list.size();
				if (debugOn) {
					XposedBridge.log(String.format("Before removal: %d", size));
				}
				for (int i=0; i<size; i++) {
					if (hiddenItems.contains(list.get(i).activityInfo.packageName)) {
						// remove it
						list.remove(i);
						i-=1;
						size-=1;
					}
				}
				if (debugOn) {
					XposedBridge.log(String.format("After removal: %d", size));
				}
				
				// set it back
				param.setResult(list);
			}
		});
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		
		boolean foundGrid = false;
		boolean foundItems = false;
		
		try {
			// hook activity chooser
			resparam.res.hookLayout("android", "layout", "resolver_grid", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					modifyLayout(liparam, "resolver_grid", false);
				}
			});
			foundGrid = true;
		} catch (Exception e) {
			// not found
		}
		try {
			// hook activity chooser
			resparam.res.hookLayout("android", "layout", "resolver_list", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					modifyLayout(liparam, "resolver_list", false);
				}
			});
			foundGrid = true;
		} catch (Exception e) {
			// not found
		}
		try {
			// hook activity chooser
			resparam.res.hookLayout("com.htc.framework", "layout", "resolveractivity_list", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					modifyLayout(liparam, "resolver_list", true);
				}
			});
			foundGrid = true;
		} catch (Exception e) {
			// not found
		}		
		try {
			resparam.res.hookLayout("android", "layout", "resolve_list_item", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					changeItems(liparam, false);
				}
			});
			foundItems = true;
		} catch (Exception e) {
			// not found
		}
		try {
			resparam.res.hookLayout("android", "layout", "resolve_grid_item", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					changeItems(liparam, false);
				}
			});
			foundItems = true;
		} catch (Exception e) {
			// not found
		}
		try {
			resparam.res.hookLayout("com.htc.framework", "layout", "resolveractivity_list_item", new XC_LayoutInflated() {
				
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					changeItems(liparam, true);
				}
			});
			foundItems = true;
		} catch (Exception e) {
			// not found
		}
		if (!foundGrid) {
			XposedBridge.log("Grid/List resource not found.");
		}
		if (!foundItems) {
			XposedBridge.log("Grid/List item resource not found.");
		}
	}
	
	private void modifyLayout(LayoutInflatedParam liparam, String listName, boolean htc) {
		// framework
		String framework = htc ? "com.htc.framework" : "android";
		
		// hide buttons
		XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
		Button button_always = (Button)liparam.view.findViewById(liparam.res.getIdentifier("button_always", "id", framework));
		Button button_once = (Button)liparam.view.findViewById(liparam.res.getIdentifier("button_once", "id", framework));
		boolean keepButtons = pref.getBoolean("KeepButtons", false);
		if (!keepButtons) {
			if (button_always != null) {
				hideElement(button_always);
			}		
			if (button_once != null) {
				hideElement(button_once);
			}
		}
		
		// get current configuration
		String theme = pref.getString("LayoutTheme", "Default");
		boolean showAlways = pref.getBoolean("ShowAlways", false);
		if (showAlways) {			
			// add check box
			LinearLayout buttonBar = (LinearLayout)liparam.view.findViewById(liparam.res.getIdentifier("button_bar", "id", framework));
			if (buttonBar != null) {
				addAlwaysCheckbox(liparam, buttonBar, button_always.getText(), theme, pref);
				if (pref.getInt("RoundCorner", 0) == 0) {
					if (theme.equals("Light")) {
						buttonBar.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
					} else if (theme.equals("Dark")) {
						buttonBar.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
					}
				}
			}
		} else {
			// hide button bar
			LinearLayout buttonBar = (LinearLayout)liparam.view.findViewById(liparam.res.getIdentifier("button_bar", "id", framework));
			if (buttonBar != null && !keepButtons) {
				hideElement(buttonBar);
			}
		}
		
		// get element
		View resolver_grid = liparam.view.findViewById(liparam.res.getIdentifier(listName, "id", framework));		
		
		// change layout?
		String layoutStyle = pref.getString("LayoutStyle", "Default");
		if (!layoutStyle.equals("Default")) {
			if (resolver_grid.getClass().equals(GridView.class) && layoutStyle.equals("List")) {
				hideElement(resolver_grid);
				// create list
				createListLayout(liparam, resolver_grid, theme, pref);
			} else if (resolver_grid.getClass().equals(ListView.class) && layoutStyle.equals("Grid")) {
				hideElement(resolver_grid);
				// number of columns
				int columns = getColumnsNumber(resolver_grid.getContext(), pref);
				
				// create grid
				createGridLayout(liparam, resolver_grid, columns, theme, pref);
			} else if (resolver_grid.getClass().equals(GridView.class) && layoutStyle.equals("Grid")) {
				hideElement(resolver_grid);
				// number of columns
				int columns = getColumnsNumber(resolver_grid.getContext(), pref);
				
				// create grid
				createGridLayout(liparam, resolver_grid, columns, theme, pref);
			} else {
				if (pref.getInt("RoundCorner", 0) == 0) {
					if (theme.equals("Light")) {
						resolver_grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
					} else if (theme.equals("Dark")) {
						resolver_grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
					}		
				}
			}
		} else {
			if (pref.getInt("RoundCorner", 0) == 0) {
				if (theme.equals("Light")) {
					resolver_grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
				} else if (theme.equals("Dark")) {
					resolver_grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
				}		
			}
		}
	}
	
	private void hideElement(View element) {
		element.setVisibility(View.GONE);
		element.setMinimumHeight(0);
		LayoutParams params = element.getLayoutParams();
		params.height = 0;		
	}
	
	@SuppressLint("NewApi")
	private void addAlwaysCheckbox(LayoutInflatedParam liparam, LinearLayout buttonBar, CharSequence text, String theme, XSharedPreferences pref) {
		// add it
		CheckBox alwaysCheck = new CheckBox(buttonBar.getContext());
		alwaysCheck.setText(text);
		DisplayMetrics metrics = buttonBar.getContext().getResources().getDisplayMetrics();
		alwaysCheck.setMinHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics));
		//alwaysCheck.setMinimumHeight(liparam.res.getDimensionPixelSize(liparam.res.getIdentifier("alert_dialog_button_bar_height", "dimen", "android")));
		alwaysCheck.setGravity(Gravity.CENTER);
		if (theme.equals("Light")) {
			alwaysCheck.setTextColor(pref.getInt("TextColor", Color.BLACK));
			alwaysCheck.setButtonDrawable(liparam.res.getIdentifier("btn_check_off_holo_light", "drawable", "android"));
		} else if (theme.equals("Dark")) {
			alwaysCheck.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
			alwaysCheck.setButtonDrawable(liparam.res.getIdentifier("btn_check_off_holo_dark", "drawable", "android"));
		}
		
		// events
		alwaysCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				String theme = pref.getString("LayoutTheme", "Default");
				if (theme.equals("Light")) {
					if (buttonView.isChecked()) {
						buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_on_holo_light", "drawable", "android"));
					} else {
						buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_off_holo_light", "drawable", "android"));
					}
				} else if (theme.equals("Dark")) {
					if (buttonView.isChecked()) {
						buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_on_holo_dark", "drawable", "android"));
					} else {
						buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_off_holo_dark", "drawable", "android"));
					}
				}
			}
		});
		alwaysCheck.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// get checkbox
				CheckBox buttonView = (CheckBox)view;
				
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				String theme = pref.getString("LayoutTheme", "Default");
				if (theme.equals("Light")) {
					if (buttonView.isChecked()) {
						if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_on_pressed_holo_light", "drawable", "android"));
						} else {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_on_holo_light", "drawable", "android"));
						}
					} else {
						if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_off_pressed_holo_light", "drawable", "android"));
						} else {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_off_holo_light", "drawable", "android"));
						}
					}
				} else if (theme.equals("Dark")) {
					if (buttonView.isChecked()) {
						if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_on_pressed_holo_dark", "drawable", "android"));
						} else {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_on_holo_dark", "drawable", "android"));
						}
					} else {
						if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_off_pressed_holo_dark", "drawable", "android"));
						} else {
							buttonView.setButtonDrawable(buttonView.getResources().getIdentifier("btn_check_off_holo_dark", "drawable", "android"));
						}
					}
				}
				
				return false;
			}
		});
		
		buttonBar.addView(alwaysCheck, 0);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)alwaysCheck.getLayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			params.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
		}
	}
	
	private void hideButtonBarButtons(LinearLayout buttonBar) {
		// just to make sure we have correct layout
		if (buttonBar.getChildCount() != 3 || !buttonBar.getChildAt(1).getClass().equals(Button.class) || 
			!buttonBar.getChildAt(2).getClass().equals(Button.class)) return;
		
		// make sure buttons are gone
		Button button_always = (Button)buttonBar.getChildAt(1);
		if (button_always == null) {
			return;
		}
		if (button_always.getVisibility() != View.GONE) {
			hideElement(button_always);
		}
		Button button_once = (Button)buttonBar.getChildAt(2);
		if (button_once == null) {
			return;
		}
		if (button_once.getVisibility() != View.GONE) {
			hideElement(button_once); 
		}
	}
	
	private void createGridLayout(LayoutInflatedParam liparam, View oldList, int numberOfColumns, String theme, XSharedPreferences pref) {
		FrameLayout parent = (FrameLayout)oldList.getParent();
		
		GridView grid = new GridView(liparam.view.getContext());
		grid.setNumColumns(numberOfColumns);
		DisplayMetrics metrics = liparam.view.getContext().getResources().getDisplayMetrics();
		grid.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics));
		grid.setClipToPadding(false);
		grid.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		grid.setColumnWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics));
		grid.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
		GridLayout.LayoutParams params = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		grid.setLayoutParams(params);
		if (pref.getInt("RoundCorner", 0) == 0) {
			if (theme.equals("Light")) {
				grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
			} else if (theme.equals("Dark")) {
				grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
			}		
		}
		parent.addView(grid);
		parent.setMinimumHeight(0);
		parent.setMeasureAllChildren(false);
		LinearLayout.LayoutParams frameParams = (LinearLayout.LayoutParams)parent.getLayoutParams();
		frameParams.width = LayoutParams.WRAP_CONTENT;
		frameParams.height = LayoutParams.WRAP_CONTENT;
		
		// make sure all parents are WRAP_CONTENT
		ViewParent loopParent = parent.getParent();
		while (loopParent != null) {
			Class<?> parentClass = loopParent.getClass();			
			if (parentClass.equals(LinearLayout.class)) {
				// set layout params
				LinearLayout loopLinear = (LinearLayout)loopParent;
				LinearLayout.LayoutParams loopParams = (LinearLayout.LayoutParams)loopLinear.getLayoutParams();
				if (loopParams != null) {
					loopParams.width = LayoutParams.WRAP_CONTENT;
					loopParams.height = LayoutParams.WRAP_CONTENT;		
				}
			} else if (parentClass.equals(FrameLayout.class)) {
				// set layout params
				FrameLayout loopLinear = (FrameLayout)loopParent;
				LinearLayout.LayoutParams loopParams = (LinearLayout.LayoutParams)loopLinear.getLayoutParams();
				if (loopParams != null) {
					loopParams.width = LayoutParams.WRAP_CONTENT;
					loopParams.height = LayoutParams.WRAP_CONTENT;		
				}
			} else break;
			
			loopParent = loopParent.getParent(); 
		}
	}
	
	private void createListLayout(LayoutInflatedParam liparam, View oldList, String theme, XSharedPreferences pref) {
		FrameLayout parent = (FrameLayout)oldList.getParent();
		
		ListView list = new ListView(liparam.view.getContext());
		DisplayMetrics metrics = liparam.view.getContext().getResources().getDisplayMetrics();
		list.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics));
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (pref.getInt("RoundCorner", 0) == 0) {
			if (theme.equals("Light")) {
				list.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
			} else if (theme.equals("Dark")) {
				list.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
			}		
		}
		parent.addView(list);
		parent.setMinimumHeight(0);
		parent.setMeasureAllChildren(false);
		LinearLayout.LayoutParams frameParams = (LinearLayout.LayoutParams)parent.getLayoutParams();
		frameParams.height = LayoutParams.WRAP_CONTENT;
		LayoutParams params = list.getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;
	}
	
	private void changeItems(LayoutInflatedParam liparam, boolean htc) {
		// framework
		String framework = htc ? "com.htc.framework" : "android";
		
		// get current configuration
		XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
		String layoutStyle = pref.getString("LayoutStyle", "Default");
		String theme = pref.getString("LayoutTheme", "Default");
		if (layoutStyle.equals("Default") && theme.equals("Default")) return;
		else if (layoutStyle.equals("Default"))
		{
			// keep same style, but change color
			TextView text1 = (TextView)liparam.view.findViewById(android.R.id.text1);
			if (theme.equals("Light")) {
				text1.setTextColor(pref.getInt("TextColor", Color.BLACK));
			} else if (theme.equals("Dark")) {
				text1.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
			}		
			TextView text2 = (TextView)liparam.view.findViewById(android.R.id.text2);
			if (theme.equals("Light")) {
				text2.setTextColor(pref.getInt("TextColor", Color.BLACK));
			} else if (theme.equals("Dark")) {
				text2.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
			}		
		} else if (layoutStyle.equals("List")) {
			// convert to list
			convertToAOSPListItem(liparam, (LinearLayout)liparam.view, pref.getString("ListTextSize", "Regular"), theme, framework, pref);
		} else if (layoutStyle.equals("Grid")) {
			// convert to grid
			convertToGridItem(liparam, (LinearLayout)liparam.view, pref.getString("GridTextSize", "Regular"), theme, framework, pref);
		} 
		
		// HTC
		if (htc) {
			LinearLayout parent = (LinearLayout)liparam.view;
			CheckedTextView htcCheck = new CheckedTextView(parent.getContext());
			htcCheck.setId(liparam.res.getIdentifier("ctxv1", "id", framework));
			htcCheck.setVisibility(View.GONE);
			parent.addView(htcCheck);
		}
	}
	
	@SuppressLint("NewApi")
	private void convertToAOSPListItem(LayoutInflatedParam liparam, LinearLayout parent, String textSize, String theme, String framework, XSharedPreferences pref) {
		// setup parent
		parent.setOrientation(0); // horizontal
		parent.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
		DisplayMetrics metrics = parent.getContext().getResources().getDisplayMetrics();
		parent.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
		
		// remove all children
		parent.removeAllViews();

		// add icon
		ImageView icon = new ImageView(parent.getContext());
		icon.setId(liparam.res.getIdentifier("icon", "id", framework));
		LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams(0, 0);
		icon.setLayoutParams(iconLayout);
		icon.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
		icon.setScaleType(ScaleType.FIT_CENTER);
		parent.addView(icon);		
		
		// linear layout
		LinearLayout linear = new LinearLayout(parent.getContext());
		LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		linearParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			linearParams.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
		}
		linear.setLayoutParams(linearParams);
		linear.setOrientation(1);
		linear.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
		parent.addView(linear);
		
		// add text1
		TextView text1 = new TextView(parent.getContext());
		text1.setId(liparam.res.getIdentifier("text1", "id", framework));
		if (textSize.equals("Extra Large")) {
			text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
			text1.setTypeface(null, Typeface.BOLD);
		} else if (textSize.equals("Large")) {
			text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			text1.setTypeface(null, Typeface.BOLD);
		} else {
			text1.setTextAppearance(parent.getContext(), liparam.res.getIdentifier("textAppearanceMedium", "attr", "android"));
		}
		LinearLayout.LayoutParams text1layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		text1.setLayoutParams(text1layout);
		text1.setMaxLines(2);
		if (theme.equals("Light")) {
			text1.setTextColor(pref.getInt("TextColor", Color.BLACK));
		} else if (theme.equals("Dark")) {
			text1.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
		}		
		linear.addView(text1);
		
		// add text2
		TextView text2 = new TextView(parent.getContext());
		text2.setId(liparam.res.getIdentifier("text2", "id", framework));
		text2.setTextAppearance(parent.getContext(), liparam.res.getIdentifier("textAppearanceSmall", "attr", "android"));
		LinearLayout.LayoutParams text2layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		text2.setLayoutParams(text2layout);
		text2.setMaxLines(2);
		text2.setPadding(0,
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics),
				0,
				0);
		if (theme.equals("Light")) {
			text2.setTextColor(pref.getInt("TextColor", Color.BLACK));
		} else if (theme.equals("Dark")) {
			text2.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
		}				
		linear.addView(text2);
	}
	
	private void convertToGridItem(LayoutInflatedParam liparam, LinearLayout parent, String textSize, String theme, String framework, XSharedPreferences pref) {
		// setup parent
		parent.setOrientation(1); // vertical
		parent.setGravity(Gravity.CENTER);
		DisplayMetrics metrics = parent.getContext().getResources().getDisplayMetrics();
		parent.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
		parent.setMinimumHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, metrics));
		parent.setMinimumWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics));
		// remove all children
		parent.removeAllViews();
		
		// add text2
		TextView text2 = new TextView(parent.getContext());
		text2.setId(liparam.res.getIdentifier("text2", "id", framework));
		text2.setTextAppearance(parent.getContext(), liparam.res.getIdentifier("textAppearance", "attr", "android"));
		LinearLayout.LayoutParams text2layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		text2layout.gravity = Gravity.CENTER;
		text2.setLayoutParams(text2layout);
		text2.setMinLines(2);
		text2.setMaxLines(2);
		text2.setGravity(Gravity.CENTER);
		text2.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics),
				0,
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics),
				0);
		if (theme.equals("Light")) {
			text2.setTextColor(pref.getInt("TextColor", Color.BLACK));
		} else if (theme.equals("Dark")) {
			text2.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
		}				
		parent.addView(text2);
		
		// add icon
		ImageView icon = new ImageView(parent.getContext());
		icon.setId(liparam.res.getIdentifier("icon", "id", framework));
		LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams(0, 0);
		//LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		icon.setLayoutParams(iconLayout);
		icon.setScaleType(ScaleType.FIT_CENTER);
		parent.addView(icon);
		
		// add text1
		TextView text1 = new TextView(parent.getContext());
		text1.setId(liparam.res.getIdentifier("text1", "id", framework));
		if (textSize.equals("Tiny")) {
			text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
		} else if (textSize.equals("Small")) {
			text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		} else if (textSize.equals("Hidden")) {
			text1.setVisibility(View.GONE);
		} else {
			text1.setTextAppearance(parent.getContext(), liparam.res.getIdentifier("textAppearanceSmall", "attr", "android"));
		}
		LinearLayout.LayoutParams text1layout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		text1layout.gravity = Gravity.CENTER;
		text1.setLayoutParams(text1layout);
		text1.setMinLines(2);
		text1.setMaxLines(2);
		text1.setGravity(Gravity.CENTER);
		text1.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics),
				0,
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics),
				0);
		if (theme.equals("Light")) {
			text1.setTextColor(pref.getInt("TextColor", Color.BLACK));
		} else if (theme.equals("Dark")) {
			text1.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
		}				
		parent.addView(text1);
	}
	
	@SuppressLint("NewApi")
	private void makeManageListButton(MethodHookParam param, Class<?> resolverActivity, Intent myIntent, boolean changeLayout, String theme, XSharedPreferences pref) {
		try {
			// move one level up
			Object aControl = XposedHelpers.getObjectField(param.thisObject, "mAlert");

			// get title view
			TextView titleView = (TextView)XposedHelpers.getObjectField(aControl, "mTitleView");
			
			// get config
			String triggerStyle = pref.getString("ManageTriggerStyle", "Wrench");
			
			// continue			
			DisplayMetrics metrics = titleView.getContext().getResources().getDisplayMetrics();
			titleView.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
			LinearLayout.LayoutParams titleViewParams = (LinearLayout.LayoutParams)titleView.getLayoutParams();
			int titleMargin = 0;
			if (triggerStyle.equals("Title")) titleMargin = 15;
			titleViewParams.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, titleMargin, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				titleViewParams.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, titleMargin, metrics));
				titleViewParams.setMarginEnd((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			}
			
			// let's Assemble list of items
			Object adapter = XposedHelpers.getObjectField(param.thisObject, "mAdapter");
			int count = (Integer)XposedHelpers.callMethod(adapter, "getCount");
			ArrayList<String> items = new ArrayList<String>();
			for (int i=0; i<count; i++) {
				ResolveInfo info = (ResolveInfo)XposedHelpers.callMethod(adapter, "resolveInfoForPosition", i);
				if (!items.contains(info.activityInfo.packageName)) {
					items.add(info.activityInfo.packageName);
				}
			}
			
			// let's assemble intent
			Intent manage = new Intent(Intent.ACTION_EDIT);
			manage.putExtra("action", myIntent.getAction());
			manage.putExtra("type", myIntent.getType());
			manage.setType("complete/action");
			String scheme = myIntent.getScheme();
			if (pref.getBoolean("RulePerWebDomain", false) && scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
				// add domain
				manage.putExtra("scheme", String.format("%s_%s", scheme ,myIntent.getData().getAuthority()));
			} else {
				manage.putExtra("scheme", scheme);
			}
			String[] aItems = new String[items.size()];
			manage.putExtra("items", items.toArray(aItems));
			
			if (triggerStyle.equals("Title")) {
				titleView.setTag(manage);
				titleView.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View view) {
						// execute intent
						Intent manage = (Intent)view.getTag();
						view.getContext().startActivity(manage);
						Activity a = (Activity)view.getContext();
						a.finish();
						
						return true;
					}
				});
			}
			
			// set title parent layout
			LinearLayout titleParent = (LinearLayout)titleView.getParent();
			titleParent.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
			LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams)titleParent.getLayoutParams();
			titleParams.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				titleParams.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
				titleParams.setMarginEnd((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			}
			if (changeLayout) {
				// get divider
				LinearLayout bigParent = (LinearLayout)titleParent.getParent();
				View sibling = null;
				if (bigParent.getChildCount() > 2) {
					sibling = bigParent.getChildAt(2);
				} else {
					XposedBridge.log("Image divider not found.");
				}
				
				// round corners
				int roundCorners = pref.getInt("RoundCorner", 0);

				// set colors
				if (theme.equals("Light")) {
					int titleColor = pref.getInt("TitleColor", Color.BLACK);
					titleView.setTextColor(titleColor);				
					if (roundCorners == 0) {
						titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));						
					} else {
						setRoundCorners((LinearLayout)bigParent.getParent(), pref.getInt("BackgroundColor", Color.WHITE), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics));
					}
					if (sibling != null) {
						sibling.setBackgroundColor(titleColor);
					}
				} else if (theme.equals("Dark")) {
					int titleColor = pref.getInt("TitleColor", Color.parseColor("#BEBEBE"));
					titleView.setTextColor(titleColor);
					if (roundCorners == 0) {
						titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
					} else {
						setRoundCorners((LinearLayout)bigParent.getParent(), pref.getInt("BackgroundColor", Color.parseColor("#101214")), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics));
					}
					if (sibling != null) {
						sibling.setBackgroundColor(titleColor);
					}
				}					
			}
			
			if (triggerStyle.equals("Wrench")) {
				// let's add image button
				ImageButton prefButton = new ImageButton(titleView.getContext());
				prefButton.setImageResource(titleView.getResources().getIdentifier("ic_menu_manage", "drawable", "android"));
				prefButton.setBackgroundResource(android.R.color.transparent);
				LinearLayout.LayoutParams prefLayout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				prefLayout.gravity = Gravity.CENTER_VERTICAL;
				prefLayout.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
						(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
						(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
						(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
				prefButton.setLayoutParams(prefLayout);
				prefButton.setTag(manage);
				prefButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent manage = (Intent)view.getTag();
						view.getContext().startActivity(manage);
						Activity a = (Activity)view.getContext();
						a.finish();
					}
				});
				titleParent.addView(prefButton, 0);			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			XposedBridge.log("makeManageListButton exception: " + e.getMessage());
		}		
	}
	
	@SuppressLint("NewApi")
	private void themeTitleView(MethodHookParam param, Class<?> resolverActivity, boolean changeLayout, String theme, XSharedPreferences pref) {
		try {
			// move one level up
			Object aControl = XposedHelpers.getObjectField(param.thisObject, "mAlert");
			
			// get title view
			TextView titleView = (TextView)XposedHelpers.getObjectField(aControl, "mTitleView");
			LinearLayout titleParent = (LinearLayout)titleView.getParent();
			DisplayMetrics metrics = titleParent.getContext().getResources().getDisplayMetrics();
			titleParent.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics));
			LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams)titleParent.getLayoutParams();
			titleParams.setMargins((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				titleParams.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
				titleParams.setMarginEnd((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			}
			if (changeLayout) {
				// get divider
				LinearLayout bigParent = (LinearLayout)titleParent.getParent();
				View sibling = null;
				if (bigParent.getChildCount() > 2) {
					sibling = bigParent.getChildAt(2);
				} else {
					XposedBridge.log("Image divider not found.");
				}
				
				// round corners
				int roundCorners = pref.getInt("RoundCorner", 0);
				
				// set colors
				if (theme.equals("Light")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.BLACK));
					if (roundCorners == 0) {
						titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));						
					} else {
						setRoundCorners((LinearLayout)bigParent.getParent(), pref.getInt("BackgroundColor", Color.WHITE), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics));
					}
					if (sibling != null) {
						sibling.setBackgroundColor(Color.BLACK);
					}
				} else if (theme.equals("Dark")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
					if (roundCorners == 0) {
						titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
					} else {
						setRoundCorners((LinearLayout)bigParent.getParent(), pref.getInt("BackgroundColor", Color.parseColor("#101214")), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics));
					}
					if (sibling != null) {
						sibling.setBackgroundColor(Color.parseColor("#BEBEBE"));
					}
				}					
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			XposedBridge.log("makeManageListButton exception: " + e.getMessage());
		}		
	}
	
	private boolean isAlwaysChecked(View adapterView) {		
		// one level up
		if (adapterView == null || adapterView.getParent() == null || !adapterView.getParent().getClass().equals(FrameLayout.class)) {
			XposedBridge.log("AdapterView is null.");
			return false;
		}
		FrameLayout frame = (FrameLayout)adapterView.getParent();
		
		// one more to root
		if (frame.getParent() == null || !frame.getParent().getClass().equals(LinearLayout.class)) {
			XposedBridge.log("Parent is wrong.");
			return false;
		}
		LinearLayout root = (LinearLayout)frame.getParent();

		// get button bar
		if (root.getChildCount() < 2 || !root.getChildAt(1).getClass().equals(LinearLayout.class)) {
			XposedBridge.log("Wrong number of children.");
			return false;
		}
		LinearLayout buttonBar = (LinearLayout)root.getChildAt(1);
		if (buttonBar.getChildCount() == 0) {
			XposedBridge.log("There is no button bar with checkbox.");
			return false;
		}
		
		// get checkbox
		if (!buttonBar.getChildAt(0).getClass().equals(CheckBox.class)) {
			XposedBridge.log("There is no checkbox.");
			return false;
		}
		CheckBox alwaysCheck = (CheckBox)buttonBar.getChildAt(0);
		return alwaysCheck.isChecked();
	}
	
	private void startSelected(Object thisObject, int position, boolean always) {
		try {
			// call selected value
			XposedHelpers.callMethod(thisObject, "startSelected", position, always);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			XposedBridge.log("StartSelected method failed: " + e.toString());
		}		
	}
	
	private void setHaloWindow(ListView list) {
		
		PackageManager pm = list.getContext().getPackageManager();
		try {
			if (pm.getPackageInfo("com.zst.xposed.halo.floatingwindow", PackageManager.GET_META_DATA) == null) {
				return;
			}
		} catch (NameNotFoundException e1) {
			// not found package
			return;
		}
		
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// call activity directly
				Object adapter = parent.getAdapter();
				try {
					Intent intent = (Intent)XposedHelpers.callMethod(adapter, "intentForPosition", position);
					if (intent != null) {
						intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | 0x00002000);
						parent.getContext().startActivity(intent);
						Activity a = (Activity)parent.getContext();
						a.finish();
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					XposedBridge.log(e.getMessage());
				}
				
				return true;
			}
			
		});		
	}	
	
	private void setHaloWindow(GridView grid) {
		
		PackageManager pm = grid.getContext().getPackageManager();
		try {
			if (pm.getPackageInfo("com.zst.xposed.halo.floatingwindow", PackageManager.GET_META_DATA) == null) {
				return;
			}
		} catch (NameNotFoundException e1) {
			// not found package
			return;
		}
		
		grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// call activity directly
				Object adapter = parent.getAdapter();
				try {
					Intent intent = (Intent)XposedHelpers.callMethod(adapter, "intentForPosition", position);
					if (intent != null) {
						intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | 0x00002000);
						parent.getContext().startActivity(intent);
						Activity a = (Activity)parent.getContext();
						a.finish();
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					XposedBridge.log(e.getMessage());
				}
				
				return true;
			}
			
		});		
	}
	
	private int getColumnsNumber(Context context, XSharedPreferences pref) {
		// get orientation
		int orientation = context.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// landscape
			return Integer.parseInt(pref.getString("GridColumnsLandscape", "5"));
		} else {
			// portrait
			return Integer.parseInt(pref.getString("GridColumns", "3"));
		}
	}
	
	private void setDialogGravity(Context context, Window mWindow, XSharedPreferences pref) {
		// get orientation
		int orientation = context.getResources().getConfiguration().orientation;
		String position = "Center";
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// landscape
			position = pref.getString("PositionLandscape", "Center");
		} else {
			// portrait
			position = pref.getString("PositionPortrait", "Center");
		}
		if (position.equals("Center")) return;
		
		// let's change it
		if (position.equals("Bottom")) mWindow.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM);
		else if (position.equals("BottomRight")) mWindow.setGravity(Gravity.RIGHT | Gravity.END | Gravity.BOTTOM);
		else if (position.equals("Right")) mWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.RIGHT | Gravity.END);
		else if (position.equals("TopRight")) mWindow.setGravity(Gravity.RIGHT | Gravity.END | Gravity.TOP);
		else if (position.equals("Top")) mWindow.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP);
		else if (position.equals("TopLeft")) mWindow.setGravity(Gravity.LEFT | Gravity.START | Gravity.TOP);
		else if (position.equals("Left")) mWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.LEFT | Gravity.START);
		else if (position.equals("BottomLeft")) mWindow.setGravity(Gravity.LEFT | Gravity.START | Gravity.BOTTOM);
		else mWindow.setGravity(Gravity.CENTER);
	}
	
	private void setRoundCorners(LinearLayout root, int color, int roundValue) {
		// top round corners
		GradientDrawable topBorder = new GradientDrawable(Orientation.BOTTOM_TOP, new int[] { color, color });
		topBorder.setCornerRadii(new float[] { roundValue, roundValue, roundValue, roundValue, 0, 0, 0, 0 });

		// bottom round corners
		GradientDrawable bottomBorder = new GradientDrawable(Orientation.TOP_BOTTOM, new int[] { color, color });
		bottomBorder.setCornerRadii(new float[] { 0, 0, 0, 0, roundValue, roundValue, roundValue, roundValue });
		
		// set first child
		View first = root.getChildAt(0);
		first.setBackground(topBorder);
		
		// find bottom
		int lastIndex = root.getChildCount() - 1;
		while(root.getChildAt(lastIndex).getVisibility() != View.VISIBLE) {
			lastIndex -= 1;
		}
		
		// set bottom
		View last = root.getChildAt(lastIndex);
		last.setBackground(bottomBorder);
		
		// set in between
		if (lastIndex > 1) {
			for (int i=1; i<lastIndex;i++) {
				View m = root.getChildAt(i);
				m.setBackgroundColor(color);
			}
		}
	}
	
	@SuppressLint("DefaultLocale")
	@SuppressWarnings("unchecked")
	private void restoreListItems(Object thisObject, XSharedPreferences pref) {
		try {
			// get adapter
			boolean debugOn = pref.getBoolean("DebugLog", false);
			Object mAdapter = XposedHelpers.getObjectField(thisObject, "mAdapter");
			Field mCurrentResolveList = null;
			try {
				mCurrentResolveList = mAdapter.getClass().getDeclaredField("mCurrentResolveList");
				if (debugOn) {
					XposedBridge.log("Android 4.2 mCurrentResolveList");
				}
			} catch (Exception ex) { }
			if (mCurrentResolveList == null) {
				try {
					mCurrentResolveList = mAdapter.getClass().getDeclaredField("mOrigResolveList");
					if (debugOn) {
						XposedBridge.log("Android 4.4 mOrigResolveList");
					}
				} catch (Exception ex) { }
			}
			if (mCurrentResolveList == null) {
				try {
					mCurrentResolveList = mAdapter.getClass().getDeclaredField("mBaseResolveList");
					if (debugOn) {
						XposedBridge.log("Android 4.3 mBaseResolveList");
					}
				} catch (Exception ex) { }
			}
			List<ResolveInfo> mCurrent = null;
			if (mCurrentResolveList != null) {
				mCurrentResolveList.setAccessible(true);
				mCurrent =(List<ResolveInfo>)mCurrentResolveList.get(mAdapter); 
			}						
			if (mCurrent == null) {
				if (debugOn) {
					XposedBridge.log("Original list is NULL.");
				}
			} else {
				List<Object> mList = (List<Object>)XposedHelpers.getObjectField(mAdapter, "mList");
				if (debugOn) {
					XposedBridge.log(String.format("mCurrent size = %d", mCurrent.size()));
				}
				if (mCurrent.size() != mList.size()) {
					// get DisplayResolveInfo class
					Class<?> DisplayResolveInfo = mList.get(0).getClass();
					Constructor<?> driCon = DisplayResolveInfo.getDeclaredConstructors()[0];
					driCon.setAccessible(true);
					
					// add missing one back
					for (ResolveInfo r : mCurrent) {
						boolean missing = true;
						for (Object l : mList) {
							// get resolve info
							ResolveInfo info = (ResolveInfo)XposedHelpers.getObjectField(l, "ri");
							if (info.activityInfo.packageName.equals(r.activityInfo.packageName) &&
								info.activityInfo.name.equals(r.activityInfo.name)) {
								missing = false;
								break;
							}
						}
						if (missing) {
							// let's add back
							Object n = driCon.newInstance(thisObject, r, "", "", null);
							mList.add(n);
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
