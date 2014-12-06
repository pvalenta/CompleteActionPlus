package hk.valenta.completeactionplus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.RelativeLayout;
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
import android.widget.Toast;
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
					param.setResult("2.6.0");
					return "2.6.0";
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
				boolean debugOn = pref.getBoolean("DebugLog", false);
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
						String theme = pref.getString("LayoutTheme", "Default");
						Button mAlwaysButton = (Button)XposedHelpers.getObjectField(param.thisObject, "mAlwaysButton");
						if (mAlwaysButton != null) {
							mAlwaysButton.setEnabled(true);
							if (theme.equals("Light")) {
								mAlwaysButton.setTextColor(pref.getInt("TextColor", Color.BLACK));
							} else if (theme.equals("Dark")) {
								mAlwaysButton.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
							}
						}
						Button mOnceButton = (Button)XposedHelpers.getObjectField(param.thisObject, "mOnceButton");
						if (mOnceButton != null) {
							mOnceButton.setEnabled(true);
							if (theme.equals("Light")) {
								mOnceButton.setTextColor(pref.getInt("TextColor", Color.BLACK));
							} else if (theme.equals("Dark")) {
								mOnceButton.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
							}
						}
						// LG G3 OK button
						try {
							Field mAllowButtonField = param.thisObject.getClass().getDeclaredField("mAllowButton");
							mAllowButtonField.setAccessible(true);
							Button mAllowButton = (Button)XposedHelpers.getObjectField(param.thisObject, "mAllowButton");
							if (mAllowButton != null) {
								mAllowButton.setEnabled(true);
							}
						} catch (Exception e) {
							//XposedBridge.log("CAP: No LG G3");
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
//					boolean manageList = pref.getBoolean("ManageList", false);
//					boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//					if (always && manageList && oldWayHide) {
//						// restore items
//						restoreListItems(param.thisObject, pref);
//					}
					if (always && debugOn) {
						XposedBridge.log("CAP: Application set as default.");
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
					XposedBridge.log("CAP: Resolver field not found.");
					return null;
				}
				if (rControl == null) {
					XposedBridge.log("CAP: Resolver field found, but it's null.");
					return null;
				}	
				
				// what we got?
				int selectedIndex = -1;
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				String layoutStyle = pref.getString("LayoutStyle", "Default");
				boolean debugOn = pref.getBoolean("DebugLog", false);
				FrameLayout frame = (FrameLayout)rControl.getParent();
				if (layoutStyle.equals("Default")) {				
					if (resolver.get(param.thisObject).getClass().equals(GridView.class)) {
						// set it
						if (debugOn) {
							XposedBridge.log("CAP: Grid found.");
						}
						GridView resGrid = (GridView)resolver.get(param.thisObject);
						selectedIndex = resGrid.getCheckedItemPosition();
					} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
						// set it
						if (debugOn) {
							XposedBridge.log("CAP: List found.");
						}
						ListView resList = (ListView)resolver.get(param.thisObject);
						selectedIndex = resList.getCheckedItemPosition();
					}
				} else if (frame.getChildCount() < 2) {
					if (resolver.get(param.thisObject).getClass().equals(GridView.class)) {
						// set it
						if (debugOn) {
							XposedBridge.log("CAP: Grid found.");						
						}
						GridView resGrid = (GridView)resolver.get(param.thisObject);
						selectedIndex = resGrid.getCheckedItemPosition();
					} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
						// set it
						if (debugOn) {
							XposedBridge.log("CAP: List found.");
						}
						ListView resList = (ListView)resolver.get(param.thisObject);
						selectedIndex = resList.getCheckedItemPosition();
					}
				} else {
					// let's get new layout
					if (layoutStyle.equals("List")) {
						ListView list = (ListView)frame.getChildAt(1);
						if (list != null) {
							if (debugOn) {
								XposedBridge.log("CAP: List found.");
							}
							selectedIndex = list.getCheckedItemPosition();
						}
					} else if (layoutStyle.equals("Grid")) {
						GridView grid = (GridView)frame.getChildAt(1);
						if (grid != null) {
							if (debugOn) {
								XposedBridge.log("CAP: Grid found.");
							}
							selectedIndex = grid.getCheckedItemPosition();
						}
					}
				}
				if (selectedIndex == -1) {
					if (debugOn) {
						XposedBridge.log("CAP: Nothing selected.");
					}
					return null;
				}
				
				// always button?
				Button button = (Button)param.args[0];
				Button mAlwaysButton = (Button)XposedHelpers.getObjectField(param.thisObject, "mAlwaysButton");
				boolean always = (button.getId() == mAlwaysButton.getId());
				
				try {
					// LG G3 checkbox
					Field mAlwaysUseField = param.thisObject.getClass().getDeclaredField("mAlwaysUse");
					mAlwaysUseField.setAccessible(true);
					CheckBox mAlwaysUse = (CheckBox)XposedHelpers.getObjectField(param.thisObject, "mAlwaysUse");
					if (mAlwaysUse != null) {
						always = mAlwaysUse.isChecked();
					}
				} catch (Exception e) {
					//XposedBridge.log("CAP: No LG G3");					
				}
				
				// restore items?
//				boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//				boolean manageList = pref.getBoolean("ManageList", false);
//				if (always && manageList && oldWayHide) {
//					// restore items
//					restoreListItems(param.thisObject, pref);
//				}
				if (always && debugOn) {
					XposedBridge.log("CAP: Application set as default.");
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
				String theme = pref.getString("LayoutTheme", "Default");
				if (transparency > 0 || theme.equals("Transparent")) {
					// let's get activity
					Activity activity = (Activity)param.thisObject;
					Window window = activity.getWindow();
					WindowManager.LayoutParams params = window.getAttributes();

					// set transparency
//					params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
					if (theme.equals("Transparent")) {
//						params.format = PixelFormat.TRANSLUCENT;
//						params.height = LayoutParams.MATCH_PARENT;
//						params.width = LayoutParams.MATCH_PARENT;		
//						params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
						params.dimAmount = 0.999f;
					}
					if (transparency > 0) {
						params.alpha = 1f - ((float)transparency / 100f);	
					}
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
					XposedBridge.log("CAP: Resolver field not found.");
					return;
				}
				if (rControl == null) {
					XposedBridge.log("CAP: Resolver field found, but it's null.");
					return;
				}
				
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				boolean showAlways = pref.getBoolean("ShowAlways", false);
				boolean debugOn = pref.getBoolean("DebugLog", false);
				
				// make sure we got buttons hidden
				FrameLayout frame = (FrameLayout)rControl.getParent();
				LinearLayout root = (LinearLayout)frame.getParent();
				Context context = root.getContext();
				boolean mAlwaysUseOption = XposedHelpers.getBooleanField(param.thisObject, "mAlwaysUseOption");
				if (root.getChildCount() == 2 && pref.getBoolean("KeepButtons", false) == false && mAlwaysUseOption) {
					int buttonBarId = context.getResources().getIdentifier("button_bar", "id", context.getPackageName());
					if (buttonBarId > 0) {
						View buttonBar = root.findViewById(buttonBarId);	
						if (buttonBar != null) {
							if (!showAlways && buttonBar.getVisibility() != View.GONE) {
								// make sure it's gone
								hideElement(buttonBar);
							} else if (showAlways && buttonBar.getVisibility() == View.VISIBLE && 
									((ViewGroup)buttonBar).getChildCount() >= 3) {
								// make sure buttons are gone
								hideButtonBarButtons((LinearLayout)buttonBar);
								
								// any LG G3 element?
								int alwaysUseId = context.getResources().getIdentifier("alwaysUse", "id", context.getPackageName());
								if (alwaysUseId > 0) {
									hideElement(root.findViewById(alwaysUseId));
								}
								int clearDefaultHintId = context.getResources().getIdentifier("clearDefaultHint", "id", context.getPackageName());
								if (clearDefaultHintId > 0) {
									hideElement(root.findViewById(clearDefaultHintId));
								}
							}
						}
					}
				}				
				
				// get application context				
				//XposedBridge.log(String.format("CAP: AppContext - %s", AndroidAppHelper.currentApplication().getPackageName()));
				
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

				// timeout only for view dialog
				DisplayMetrics metrics = frame.getContext().getResources().getDisplayMetrics();
				int autoStart = pref.getInt("AutoStart", 0);
				ProgressBar progress = null;
				if (autoStart > 0 && mAlwaysUseOption) {
					// add progress bar
					progress = new ProgressBar(frame.getContext(), null, android.R.attr.progressBarStyleHorizontal);
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
				
				// manage list?
				boolean manageList = pref.getBoolean("ManageList", false);
				String layoutStyle = pref.getString("LayoutStyle", "Default");
				String theme = pref.getString("LayoutTheme", "Default");
				if (manageList) {
					makeManageListButton(param, rObject, myIntent, !theme.equals("Default"), theme, pref, progress);
				} else {
					themeTitleView(param, rObject, !theme.equals("Default"), theme, pref);
				}
//				if (theme.equals("Transparent")) {
//					XposedBridge.log("CAP: Let's make full screen dialog.");
//					// let's get activity
//					Activity activity = (Activity)param.thisObject;
//					Window window = activity.getWindow();
//					WindowManager.LayoutParams params = window.getAttributes();
//					params.width = WindowManager.LayoutParams.MATCH_PARENT;
//					params.height = WindowManager.LayoutParams.MATCH_PARENT; 					
//					XposedBridge.log("CAP: Should be full screen dialog.");
//				}
				
				// dialog gravity
				Window currentWindow = (Window)XposedHelpers.callMethod(param.thisObject, "getWindow");
				if (theme.equals("Transparent")) {
					currentWindow.setGravity(Gravity.LEFT | Gravity.TOP);
					currentWindow.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				} else {
					setDialogGravity(root.getContext(), currentWindow, pref);
					currentWindow.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				}
				
				Activity mParent = (Activity)XposedHelpers.getObjectField(param.thisObject, "mParent");
				if (mParent != null && debugOn) {
					XposedBridge.log(String.format("CAP: Found parent activity: %s", mParent.getLocalClassName()));
				}
				
				// default layout for long press
				String longPressAction = pref.getString("LongPress", "Nothing");
				String doubleTapAction = pref.getString("DoubleTap", "Nothing");
				boolean keepButtons = pref.getBoolean("KeepButtons", false);
				boolean flipBottom = pref.getBoolean("FlipBottom", false);
				if (layoutStyle.equals("Default")) {
					if (resolver.get(param.thisObject).getClass().equals(GridView.class)) {
						// set it
						GridView resGrid = (GridView)resolver.get(param.thisObject);
						if (flipBottom) {
							resGrid.setTranscriptMode(GridView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
							resGrid.setStackFromBottom(true);
						}
						resGrid.setItemChecked(-1, true);
						setLongPress(resGrid, longPressAction, mAlwaysUseOption, param.thisObject, pref);
						if (keepButtons) {
							setGesture(resGrid, doubleTapAction, mAlwaysUseOption, param.thisObject, pref);
						}
					} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
						// set it
						ListView resList = (ListView)resolver.get(param.thisObject);
						if (flipBottom) {
							resList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
							resList.setStackFromBottom(true);
						}
						resList.setItemChecked(-1, true);
						setLongPress(resList, longPressAction, mAlwaysUseOption, param.thisObject, pref);
						if (keepButtons) {
							setGesture(resList, doubleTapAction, mAlwaysUseOption, param.thisObject, pref);
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
						if (flipBottom) {
							resGrid.setTranscriptMode(GridView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
							resGrid.setStackFromBottom(true);
						}
						resGrid.setNumColumns(columns);
						resGrid.setItemChecked(-1, true);
						setLongPress(resGrid, longPressAction, mAlwaysUseOption, param.thisObject, pref);
						if (keepButtons) {
							setGesture(resGrid, doubleTapAction, mAlwaysUseOption, param.thisObject, pref);
						}
					} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
						// set it
						ListView resList = (ListView)resolver.get(param.thisObject);
						if (flipBottom) {
							resList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
							resList.setStackFromBottom(true);
						}
						resList.setItemChecked(-1, true);
						setLongPress(resList, longPressAction, mAlwaysUseOption, param.thisObject, pref);
						if (keepButtons) {
							setGesture(resList, doubleTapAction, mAlwaysUseOption, param.thisObject, pref);
						}
					}
					
					return;
				}

				// get adapter
				BaseAdapter adapter = (BaseAdapter)XposedHelpers.getObjectField(param.thisObject, "mAdapter");
				
				// let's get new layout
				if (layoutStyle.equals("List")) {
					ListView list = (ListView)frame.getChildAt(1);
					if (flipBottom) {
						list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
						list.setStackFromBottom(true);
					}
					list.setAdapter(adapter);
					list.setItemChecked(-1, true);
					list.setOnItemClickListener((OnItemClickListener)param.thisObject);
					setLongPress(list, longPressAction, mAlwaysUseOption, param.thisObject, pref);
					if (keepButtons) {
						setGesture(list, doubleTapAction, mAlwaysUseOption, param.thisObject, pref);
					}
				} else if (layoutStyle.equals("Grid")) {
					GridView grid = (GridView)frame.getChildAt(1);
					int columns = getColumnsNumber(frame.getContext(), pref);
					int itemCounts = adapter.getCount();
					if (flipBottom) {
						grid.setTranscriptMode(GridView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
						grid.setStackFromBottom(true);
					}
					if (columns > itemCounts) {
						// reduce columns?
						columns = itemCounts;
						if (pref.getBoolean("DontReduceColumns", false)) {
							grid.setGravity(Gravity.LEFT | Gravity.START | Gravity.CENTER_VERTICAL);
							grid.setColumnWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics));
							grid.setStretchMode(GridView.NO_STRETCH);
						}
					}
					grid.setNumColumns(columns);
					grid.setAdapter(adapter);
					grid.setItemChecked(-1, true);
					grid.setOnItemClickListener((OnItemClickListener)param.thisObject);
					setLongPress(grid, longPressAction, mAlwaysUseOption, param.thisObject, pref);		
					if (keepButtons) {
						setGesture(grid, doubleTapAction, mAlwaysUseOption, param.thisObject, pref);
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
				boolean flipBottom = pref.getBoolean("FlipBottom", false);
				if (!manageList && !flipBottom) return;
				
				// let's get intent
				Intent myIntent = (Intent)XposedHelpers.getObjectField(param.thisObject, "mIntent");
				String scheme = myIntent.getScheme();
				if (pref.getBoolean("RulePerWebDomain", false) && scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
					// add domain
					scheme = String.format("%s_%s", scheme ,myIntent.getData().getAuthority());
				} 
				String intentId = String.format("%s;%s;%s", myIntent.getAction(), myIntent.getType(), scheme);
//				boolean oldWayHide = pref.getBoolean("OldWayHide", false);
				boolean debugOn = pref.getBoolean("DebugLog", false);
				int removed = myIntent.getIntExtra("CAP-Removed", 0);
				int favorited = 0;
//				if (oldWayHide) {
//					String cHidden = pref.getString(intentId, null);
//					if (cHidden != null && cHidden.length() > 0) {
//						// split by ;
//						String[] hI = cHidden.split(";");
//						ArrayList<String> hiddenItems = new ArrayList<String>();
//						for (String h : hI) {
//							if (!hiddenItems.contains(h)) {							
//								hiddenItems.add(h);
//							}
//						}
//						
//						// get list
//						List<Object> items = (List<Object>)XposedHelpers.getObjectField(param.thisObject, "mList");
//						
//						// get original list to solve 4.3 issue
//						List<ResolveInfo> baseList = null;
//						try {
//							baseList = (List<ResolveInfo>)XposedHelpers.getObjectField(param.thisObject, "mBaseResolveList");
//							if (baseList == null) {
//								baseList = new ArrayList<ResolveInfo>();
//							}
//						} catch (Exception ex) { }
//						
//						// let's try to find
//						for (String h : hiddenItems) {
//							int count = items.size();
//							for (int i=0; i<count; i++) {
//								// get resolve info
//								ResolveInfo info = (ResolveInfo)XposedHelpers.getObjectField(items.get(i), "ri");
//								
//								// match?
//								if (info.activityInfo.packageName.equals(h)) {
//									// store in original list for KitKat
//									if (baseList != null) {
//										baseList.add(info);
//									}
//									
//									// remove it
//									items.remove(i);
//									i -= 1;
//									count -= 1;
//									removed += 1;
//								}
//							}
//						}
//						if (debugOn && removed > 0) {
//							XposedBridge.log(String.format("CAP: Removed %d from %s", removed, intentId));
//						}
//					}					
//				}
								
				// add custom
				String cAdd = pref.getString(intentId + "_add", null);
				boolean addFeature = pref.getBoolean("AddFeature", false);
				if (addFeature && cAdd != null && cAdd.length() > 0) {
					// get list
					List<Object> items = (List<Object>)XposedHelpers.getObjectField(param.thisObject, "mList");
					LayoutInflater mInflater = (LayoutInflater)XposedHelpers.getObjectField(param.thisObject, "mInflater");
					PackageManager pm = mInflater.getContext().getPackageManager();
										
					// get DisplayResolveInfo class
					Class<?> DisplayResolveInfo = items.get(0).getClass();
					Constructor<?> driCon = DisplayResolveInfo.getDeclaredConstructors()[0];
					driCon.setAccessible(true);
					
					String[] aI = cAdd.split(";");
					for (String a : aI) {
						ResolveInfo info = new ResolveInfo();
						info.activityInfo = pm.getActivityInfo(ComponentName.unflattenFromString(a), 0x00002000);
						if (info.activityInfo != null) {
							if (debugOn) {
								XposedBridge.log(String.format("CAP: Added %s", info.activityInfo.name));
							}
							info.filter = info.activityInfo.metaData.getParcelable("filter");
							info.match = IntentFilter.MATCH_ADJUSTMENT_NORMAL;												
							items.add(driCon.newInstance(mInflater.getContext(), info, info.loadLabel(pm), "", null));
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
						
						// match package or activity?
						if ((favItems.contains(info.activityInfo.packageName) || 
							favItems.contains(info.activityInfo.packageName + "/" + info.activityInfo.name))&& favIndex < i) {
							// take out
							items.remove(o);
							items.add(favIndex, o);
							
							// move up
							favIndex += 1;
							favorited += 1;
						}
					}
					if (debugOn && favorited > 0) {
						XposedBridge.log(String.format("CAP: Favorited %d from %s", favorited, intentId));
					}
				}
				
				// reverse order?
				if (flipBottom) {
					// get list
					List<Object> items = (List<Object>)XposedHelpers.getObjectField(param.thisObject, "mList");

					// reverse order
					Collections.reverse(items);
				}
				
				// debug on for toast?
				if (debugOn && (removed > 0 || favorited > 0)) {
					LayoutInflater mInflater = (LayoutInflater)XposedHelpers.getObjectField(param.thisObject, "mInflater");
					Toast.makeText(mInflater.getContext(), String.format("CAP: Call captured, hidden %d and favorited %d",  removed, favorited), Toast.LENGTH_SHORT).show();
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
				int size = list.size();

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
				String action = myIntent.getAction();
				String intentId = String.format("%s;%s;%s", action, type, scheme);
				
				// do it old way?
//				boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//				if (oldWayHide) {
//					// one of ours?
//					if (!intentId.equals("android.intent.action.SEND;image/jpeg;null") &&
//						!intentId.equals("android.intent.action.SEND;image/*;null") &&
//						!intentId.equals("android.intent.action.SEND_MULTIPLE;image/*;null") &&
//						!intentId.equals("android.intent.action.SEND_MULTIPLE;image/jpeg;null")) {
//						if (debugOn) {
//							XposedBridge.log("CAP: Hiding app old way.");
//						}
//						return;
//					}
//				}	
//				
//				// add custom
//				int flags = (Integer)param.args[2];
//				String cAdd = pref.getString(intentId + "_add", null);
//				if ((flags&PackageManager.MATCH_DEFAULT_ONLY) != 0 && cAdd != null && cAdd.length() > 0) {
//					String[] aI = cAdd.split(";");
//					for (String a : aI) {
//						
//						ResolveInfo info = new ResolveInfo();
//						info.activityInfo = (ActivityInfo)XposedHelpers.callMethod(param.thisObject, "getActivityInfo", 
//								ComponentName.unflattenFromString(a), 0x00002000, param.args[3]);
//						XposedBridge.log(String.format("CAP: Added %s", info.activityInfo.name));
//						info.filter = info.activityInfo.metaData.getParcelable("filter");
//						info.match = IntentFilter.MATCH_ADJUSTMENT_NORMAL;												
//						list.add(info);
//					}
//				}
											
				// get hidden
				String cHidden = pref.getString(intentId, null);
				if (cHidden == null || cHidden.length() == 0) {
					// found
					if (debugOn) {
						XposedBridge.log(String.format("CAP: Found no match: %s", intentId));
					}
										
					// intent recording?
					if (pref.getBoolean("IntentRecord", false) &&
						!action.equals("android.intent.action.MAIN") &&
						(size > 1 || size == 0)) {
						// collect all packages
						StringBuilder builder = new StringBuilder();
						builder.append(intentId);
						for (int i=0; i<size; i++) {
							ResolveInfo info = list.get(i);
							builder.append(";");
							builder.append(info.activityInfo.packageName + "/" + info.activityInfo.name);							
						}
						
						// broadcast it
						Intent intent = new Intent();
						intent.setAction("hk.valenta.completeactionplus.INTENT");
						intent.putExtra("Intent", builder.toString());					
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						mContext.sendBroadcast(intent);
					}						
					return;
				}
				
				// found
				if (debugOn) {
					XposedBridge.log(String.format("CAP: Found match: %s", intentId));
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
				int removed = 0;
				if (debugOn) {
					XposedBridge.log(String.format("CAP: Before removal: %d", size));
				}
				for (int i=0; i<size; i++) {
					ResolveInfo info = list.get(i);
					if (hiddenItems.contains(info.activityInfo.packageName) ||
						hiddenItems.contains(info.activityInfo.packageName + "/" + info.activityInfo.name)) {
						// remove it
						list.remove(i);
						i-=1;
						size-=1;
						removed+=1;
					}
				}
				if (debugOn) {
					XposedBridge.log(String.format("CAP: After removal: %d, removed: %d", size, removed));
					if (removed > 0) {
						myIntent.putExtra("CAP-Removed", removed);						
					}
				}
				
				// intent recording?
				if (pref.getBoolean("IntentRecord", false) &&
					!action.equals("android.intent.action.MAIN") &&
					(size > 1 || size == 0)) {
					// collect all packages
					StringBuilder builder = new StringBuilder();
					builder.append(intentId);
					for (int i=0; i<size; i++) {
						ResolveInfo info = list.get(i);
						builder.append(";");
						builder.append(info.activityInfo.packageName + "/" + info.activityInfo.name);							
					}
					
					// broadcast it
					Intent intent = new Intent();
					intent.setAction("hk.valenta.completeactionplus.INTENT");
					intent.putExtra("Intent", builder.toString());					
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					mContext.sendBroadcast(intent);
				}					
				
				// set it back
				param.setResult(list);
			}
		});
		try {
		XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity.ItemLongClickListener", null, "onItemLongClick",
				AdapterView.class, View.class, int.class, long.class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {				
				// get parameters
				AdapterView<?> parent = (AdapterView<?>)param.args[0];
				int position = (Integer)param.args[2];
				
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				String action = pref.getString("LongPress", "Nothing");
				if (action.equals("AppInfo")) {
					// call activity directly
					Object adapter = parent.getAdapter();
					try {
						ResolveInfo info = (ResolveInfo)XposedHelpers.callMethod(adapter, "resolveInfoForPosition", position);
						if (info != null) {
							Intent intent = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
									.setData(Uri.fromParts("package", info.activityInfo.packageName, null))
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							parent.getContext().startActivity(intent);
							Activity a = (Activity)parent.getContext();
							a.finish();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						XposedBridge.log(e.getMessage());
					}
				} else if (action.equals("XHalo")) {
					// call activity directly
					Object adapter = parent.getAdapter();
					try {
						Intent intent = (Intent)XposedHelpers.callMethod(adapter, "intentForPosition", position);
						if (intent != null) {
							intent.setFlags(0x00002000);
							parent.getContext().startActivity(intent);
							Activity a = (Activity)parent.getContext();
							a.finish();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						XposedBridge.log(e.getMessage());
					}
				} else if (action.equals("Default") && XposedHelpers.getBooleanField(param.thisObject, "mAlwaysUseOption")) {
//					boolean manageList = pref.getBoolean("ManageList", false);
//					boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//					if (manageList && oldWayHide) {
//						// restore items
//						restoreListItems(param.thisObject, pref);
//					}
					// call activity directly
					XposedHelpers.callMethod(param.thisObject, "startSelected", position, true);
				} else if (action.equals("Launch")) {
					// call activity directly
					XposedHelpers.callMethod(param.thisObject, "startSelected", position, false);
				}
				
				return true;
			}
		});
		} catch(Exception e) {
			// not found
			XposedBridge.log("CAP: ItemLongClickListener not found.");
		}
		XposedHelpers.findAndHookMethod("com.android.server.pm.PackageManagerService", null, "getActivityInfo",
				ComponentName.class, int.class, int.class, new XC_MethodHook() {
			@SuppressWarnings("unchecked")
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// our custom flag?
				int flags = (Integer)param.args[1];
				if ((flags&0x00002000) != 0 && param.getResult() != null) {
					// 1) let's get it again 
					// PackageParser.Activity a = mActivities.mActivities.get(component);
					Object mActivities = XposedHelpers.getObjectField(param.thisObject, "mActivities");
					if (mActivities == null) return; 
					Object mmActivities = XposedHelpers.getObjectField(mActivities, "mActivities");
					if (mmActivities == null) return; 
					Object activity = XposedHelpers.callMethod(mmActivities, "get", param.args[0]);
					if (activity == null) return; 
					
					// 2) check if any intent filters
					Object intents = XposedHelpers.getObjectField(activity, "intents");
					if (intents == null) {
						// no filter, let's return null
						param.setResult(null);
						return;
					} else {
						// a.intents is ArrayList<II> intents
						ArrayList<IntentFilter> filters = (ArrayList<IntentFilter>)intents; 
						ArrayList<String> actions = new ArrayList<String>();
						ArrayList<String> dataTypes = new ArrayList<String>();
						ArrayList<String> schemes = new ArrayList<String>();
						int totalActions = 0;
						int size = filters.size();
						if (size == 0) {
							// we don't want such activities
							param.setResult(null);
							return;
						}
						IntentFilter idealFilter = null;
						for (int i=0; i<size; i++) {
							IntentFilter f = filters.get(i);
							int actionSize = f.countActions();
							int dataTypeSize = f.countDataTypes();
							int dataSchemeSize = f.countDataSchemes();
							totalActions += dataTypeSize + dataSchemeSize;
							
							// build lists
							if (actionSize > 0) {
								for (int a=0; a<actionSize; a++) {
									actions.add(f.getAction(a));
								}
							}
							if (dataTypeSize > 0) {
								for (int d=0; d<dataTypeSize; d++) {
									dataTypes.add(f.getDataType(d));
								}
							}
							if (dataSchemeSize > 0) {
								for (int d=0; d<dataSchemeSize; d++) {
									schemes.add(f.getDataScheme(d));
								}
							}
							if ((dataTypeSize > 0 || dataSchemeSize > 0) && idealFilter == null) {
								idealFilter = f;
							}
						}
						
						// any data types?
						if (totalActions == 0 || idealFilter == null) {
							// we don't want such activities
							param.setResult(null);
							return;
						}
								
						// store lists in bundle
						ActivityInfo result = (ActivityInfo)param.getResult();
						result.metaData = new Bundle();
						result.metaData.putStringArrayList("actions", actions);
						result.metaData.putStringArrayList("dataTypes", dataTypes);
						result.metaData.putStringArrayList("schemes", schemes);
						result.metaData.putParcelable("filter", idealFilter);
//						result.metaData.putParcelableArrayList("filters", filters);
						param.setResult(result);
						return;
					}
				}
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
			XposedBridge.log("CAP: Grid/List resource not found.");
		}
		if (!foundItems) {
			XposedBridge.log("CAP: Grid/List item resource not found.");
		}
	}
	
	private void modifyLayout(LayoutInflatedParam liparam, String listName, boolean htc) {
		// framework
		String framework = htc ? "com.htc.framework" : "android";
		
		// hide buttons
		XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
//		Button button_always = (Button)liparam.view.findViewById(liparam.res.getIdentifier("button_always", "id", framework));
//		Button button_once = (Button)liparam.view.findViewById(liparam.res.getIdentifier("button_once", "id", framework));
		LinearLayout buttonBar = (LinearLayout)liparam.view.findViewById(liparam.res.getIdentifier("button_bar", "id", framework));
		int g3AlwaysUse = liparam.res.getIdentifier("alwaysUse", "id", framework);
		int g3DefaultHint = liparam.res.getIdentifier("clearDefaultHint", "id", framework);
		if (g3AlwaysUse > 0 && g3DefaultHint > 0) {
			// hide both LG G3 elements
			hideElement(liparam.view.findViewById(g3AlwaysUse));
			hideElement(liparam.view.findViewById(g3DefaultHint));
		}
		
		// hide them
		boolean keepButtons = pref.getBoolean("KeepButtons", false);
		if (!keepButtons) {
			// hide all buttons inside
			int buttonCount = buttonBar.getChildCount();
			for (int i=0; i<buttonCount; i++) {
				hideElement(buttonBar.getChildAt(i));
			}
//			if (button_always != null) {
//				hideElement(button_always);
//			}		
//			if (button_once != null) {
//				hideElement(button_once);
//			}
		}
		
		// get current configuration
		String theme = pref.getString("LayoutTheme", "Default");
		boolean showAlways = pref.getBoolean("ShowAlways", false);
		if (showAlways) {			
			// add check box
			if (buttonBar != null) {
				addAlwaysCheckbox(liparam, buttonBar, 
						liparam.view.getContext().getString(
								liparam.res.getIdentifier("activity_resolver_use_always", "string", framework)), theme, pref);
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
			if (buttonBar != null && !keepButtons) {
				hideElement(buttonBar);
			}
		}
		if (keepButtons) {
			// transparent button bar
			buttonBar.setBackgroundColor(Color.TRANSPARENT);

			// button theme
			String buttonThemePref = pref.getString("ButtonTheme", "");
			if (buttonThemePref == "") buttonThemePref = theme;
			
			// change buttons
			int buttonCount = buttonBar.getChildCount();
			for (int i=0; i<buttonCount; i++) {
				if (buttonBar.getChildAt(i).getClass().equals(Button.class)) {
					Button button = (Button)buttonBar.getChildAt(i);
					if (buttonThemePref.equals("Light")) {				
						button.setBackgroundResource(button.getResources().getIdentifier("btn_default_holo_light", "drawable", "android"));
						button.setTextColor(pref.getInt("TextColor", Color.BLACK));
					}
					if (buttonThemePref.equals("Dark")) {
						button.setBackgroundResource(button.getResources().getIdentifier("btn_default_holo_dark", "drawable", "android"));
						button.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
					}
					LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams)button.getLayoutParams();
					buttonParams.setMargins(0, 0, 0, 0);
				}
			}
			
//			// change color
//			if (buttonThemePref.equals("Light")) {				
//				if (button_always != null) {
//					button_always.setBackgroundResource(button_always.getResources().getIdentifier("btn_default_holo_light", "drawable", "android"));
//					//button_always.setBackgroundColor(Color.TRANSPARENT);
//					button_always.setTextColor(pref.getInt("TextColor", Color.BLACK));
//				}		
//				if (button_once != null) {
//					button_once.setBackgroundResource(button_once.getResources().getIdentifier("btn_default_holo_light", "drawable", "android"));
//					//button_once.setBackgroundColor(Color.TRANSPARENT);
//					button_once.setTextColor(pref.getInt("TextColor", Color.BLACK));
//				}
//			}
//			if (buttonThemePref.equals("Dark")) {
//				if (button_always != null) {
//					button_always.setBackgroundResource(button_always.getResources().getIdentifier("btn_default_holo_dark", "drawable", "android"));
//					//button_always.setBackgroundColor(Color.TRANSPARENT);
//					button_always.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
//				}		
//				if (button_once != null) {
//					button_once.setBackgroundResource(button_once.getResources().getIdentifier("btn_default_holo_dark", "drawable", "android"));
//					//button_once.setBackgroundColor(Color.TRANSPARENT);
//					button_once.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
//				}
//			}
//			
//			// make sure no extra margin
//			if (button_always != null) {
//				LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams)button_always.getLayoutParams();
//				buttonParams.setMargins(0, 0, 0, 0);
//			}
//			if (button_once != null) {
//				LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams)button_once.getLayoutParams();
//				buttonParams.setMargins(0, 0, 0, 0);
//			}
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
		if (element != null) {
			element.setVisibility(View.GONE);
			element.setMinimumHeight(0);
			LayoutParams params = element.getLayoutParams();
			params.height = 0;		
		}
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
		if (theme.equals("Light") || theme.equals("Transparent")) {
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
				if (theme.equals("Light") || theme.equals("Transparent")) {
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
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// get checkbox
				CheckBox buttonView = (CheckBox)view;
				
				// get current configuration
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				String theme = pref.getString("LayoutTheme", "Default");
				if (theme.equals("Light") || theme.equals("Transparent")) {
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
		// make sure buttons are gone
		int childCount = buttonBar.getChildCount();
		for (int i=1; i<childCount; i++) {
			View child = buttonBar.getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				hideElement(child);
			}
		}
		
//		Button button_always = (Button)buttonBar.getChildAt(1);
//		if (button_always != null && button_always.getVisibility() != View.GONE) {
//			hideElement(button_always);
//		}
//		Button button_once = (Button)buttonBar.getChildAt(2);
//		if (button_once != null && button_once.getVisibility() != View.GONE) {
//			hideElement(button_once); 
//		}
//		if (buttonBar.getChildCount() > 3) {
//			// LG G3
//			Button extraButton = (Button)buttonBar.getChildAt(3);
//			if (extraButton != null && extraButton.getVisibility() != View.GONE) {
//				hideElement(extraButton);				
//			}
//		}
	}
	
	private void createGridLayout(LayoutInflatedParam liparam, View oldList, int numberOfColumns, String theme, XSharedPreferences pref) {
		FrameLayout parent = (FrameLayout)oldList.getParent();
		
		GridView grid = new GridView(liparam.view.getContext());
		if (pref.getBoolean("FlipRight", false)) {
			grid.setRotationY(180);
		}		
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
		LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics));
		icon.setScaleType(ScaleType.FIT_CENTER);
		icon.setLayoutParams(iconLayout);
		icon.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
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
	
	@SuppressLint("DefaultLocale")
	private void convertToGridItem(LayoutInflatedParam liparam, LinearLayout parent, String textSize, String theme, String framework, XSharedPreferences pref) {
		// setup parent
		if (pref.getBoolean("FlipRight", false)) {
			parent.setRotationY(180);
		}		
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
		LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics),
				(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics));
		icon.setScaleType(ScaleType.FIT_CENTER);
		icon.setLayoutParams(iconLayout);
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
	private void makeManageListButton(MethodHookParam param, Class<?> resolverActivity, Intent myIntent, boolean changeLayout, 
			String theme, XSharedPreferences pref, ProgressBar autoStartProgressBar) {
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
			ViewGroup.MarginLayoutParams titleViewParams = (ViewGroup.MarginLayoutParams)titleView.getLayoutParams();
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
				items.add(info.activityInfo.packageName + "/" + info.activityInfo.name);
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
				titleView.setTag(R.id.TAG_AUTOSTART_PROGRESSBAR, autoStartProgressBar);
				titleView.setOnLongClickListener(new OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View view) {
						// progress bar
						ProgressBar autoStartProgressBar = (ProgressBar)view.getTag(R.id.TAG_AUTOSTART_PROGRESSBAR);
						if (autoStartProgressBar != null) {
							autoStartProgressBar.setVisibility(View.GONE);
						}
						
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
			ViewGroup titleParent = (ViewGroup)titleView.getParent();
			titleParent.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			ViewGroup.MarginLayoutParams titleParams = (ViewGroup.MarginLayoutParams)titleParent.getLayoutParams();
			titleParams.setMargins(0, 0, 0, 0);
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				titleParams.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
				titleParams.setMarginEnd((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			}
			if (changeLayout) {
				// get divider
				ViewGroup bigParent = (ViewGroup)titleParent.getParent();
				View sibling = null;
				if (bigParent.getChildCount() > 2) {
					sibling = bigParent.getChildAt(2);
				} else {
					XposedBridge.log("CAP: Image divider not found.");
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
						setRoundCorners((ViewGroup)bigParent.getParent(), pref.getInt("BackgroundColor", Color.WHITE), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics),
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics));
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
						setRoundCorners((ViewGroup)bigParent.getParent(), pref.getInt("BackgroundColor", Color.parseColor("#101214")), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics),
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics));
					}
					if (sibling != null) {
						sibling.setBackgroundColor(titleColor);
					}
				} else if (theme.equals("Transparent")) {
					int titleColor = pref.getInt("TitleColor", Color.BLACK);
					titleView.setTextColor(titleColor);				
					setTransparentDialog((LinearLayout)bigParent.getParent());
				}
				
				// set no margin
//				setNoMargin((ViewGroup)bigParent.getParent());
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
				prefButton.setTag(R.id.TAG_AUTOSTART_PROGRESSBAR, autoStartProgressBar);
				prefButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						// progress bar
						ProgressBar autoStartProgressBar = (ProgressBar)view.getTag(R.id.TAG_AUTOSTART_PROGRESSBAR);
						if (autoStartProgressBar != null) {
							autoStartProgressBar.setVisibility(View.GONE);
						}
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
			XposedBridge.log("CAP: makeManageListButton exception: " + e.toString());
		}		
	}
	
	@SuppressLint("NewApi")
	private void themeTitleView(MethodHookParam param, Class<?> resolverActivity, boolean changeLayout, String theme, XSharedPreferences pref) {
		try {
			// move one level up
			Object aControl = XposedHelpers.getObjectField(param.thisObject, "mAlert");
			
			// get title view
			TextView titleView = (TextView)XposedHelpers.getObjectField(aControl, "mTitleView");
			ViewGroup titleParent = (ViewGroup)titleView.getParent();
			DisplayMetrics metrics = titleParent.getContext().getResources().getDisplayMetrics();
			titleParent.setPadding((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics),
					(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			ViewGroup.MarginLayoutParams titleParams = (ViewGroup.MarginLayoutParams)titleParent.getLayoutParams();
			titleParams.setMargins(0, 0, 0, 0);
			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				titleParams.setMarginStart((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
				titleParams.setMarginEnd((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, metrics));
			}
			if (changeLayout) {
				// get divider
				ViewGroup bigParent = (ViewGroup)titleParent.getParent();
				View sibling = null;
				if (bigParent.getChildCount() > 2) {
					sibling = bigParent.getChildAt(2);
				} else {
					XposedBridge.log("CAP: Image divider not found.");
				}
				
				// round corners
				int roundCorners = pref.getInt("RoundCorner", 0);
				
				// set colors
				if (theme.equals("Light")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.BLACK));
					if (roundCorners == 0) {
						titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));						
					} else {
						setRoundCorners((ViewGroup)bigParent.getParent(), pref.getInt("BackgroundColor", Color.WHITE), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics),
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics));
					}
					if (sibling != null) {
						sibling.setBackgroundColor(Color.BLACK);
					}
				} else if (theme.equals("Dark")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
					if (roundCorners == 0) {
						titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
					} else {
						setRoundCorners((ViewGroup)bigParent.getParent(), pref.getInt("BackgroundColor", Color.parseColor("#101214")), 
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, roundCorners, metrics),
								(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics));
					}
					if (sibling != null) {
						sibling.setBackgroundColor(Color.parseColor("#BEBEBE"));
					}
				} else if (theme.equals("Transparent")) {
					int titleColor = pref.getInt("TitleColor", Color.BLACK);
					titleView.setTextColor(titleColor);				
					setTransparentDialog((LinearLayout)bigParent.getParent());
				}					
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			XposedBridge.log("CAP: makeManageListButton exception: " + e.getMessage());
		}		
	}
	
	private boolean isAlwaysChecked(View adapterView) {		
		// one level up
		if (adapterView == null || adapterView.getParent() == null || !adapterView.getParent().getClass().equals(FrameLayout.class)) {
			XposedBridge.log("CAP: AdapterView is null.");
			return false;
		}
		FrameLayout frame = (FrameLayout)adapterView.getParent();
		
		// one more to root
		if (frame.getParent() == null || !frame.getParent().getClass().equals(LinearLayout.class)) {
			XposedBridge.log("CAP: Parent is wrong.");
			return false;
		}
		LinearLayout root = (LinearLayout)frame.getParent();
		int buttonBarId = root.getContext().getResources().getIdentifier("button_bar", "id", root.getContext().getPackageName());
		if (buttonBarId == 0) {
			XposedBridge.log("CAP: Cannot find button bar id.");
			return false;
		}
		
		// get button bar
		LinearLayout buttonBar = (LinearLayout)root.findViewById(buttonBarId);
		if (buttonBar.getChildCount() == 0) {
			XposedBridge.log("CAP: There is no button bar with checkbox.");
			return false;
		}
		
		// get checkbox
		if (!buttonBar.getChildAt(0).getClass().equals(CheckBox.class)) {
			XposedBridge.log("CAP: There is no checkbox.");
			return false;
		}
		CheckBox alwaysCheck = (CheckBox)buttonBar.getChildAt(0);
		return alwaysCheck.isChecked();
	}
	
	@SuppressLint("DefaultLocale")
	private void startSelected(Object thisObject, int position, boolean always) {
		try {
			// call selected value
			XposedHelpers.callMethod(thisObject, "startSelected", position, always);
			//XposedBridge.log(String.format("CAP: StartSelect position %d with %b", position, always));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			XposedBridge.log("CAP: StartSelected method failed: " + e.toString());
		}		
	}
	
	private void setLongPress(ListView list, String action, final boolean mAlwaysUseOption, Object thisObject, XSharedPreferences pref) {
		if (action.equals("Nothing")) return;
//		if (action.equals("Default") && mAlwaysUseOption) {
//			boolean manageList = pref.getBoolean("ManageList", false);
//			boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//			if (manageList && oldWayHide) {
//				// restore items
//				restoreListItems(thisObject, pref);
//			}
//		}
		list.setTag(thisObject);		
		
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				String longPress = pref.getString("LongPress", "Nothing");
				if (longPress.equals("AppInfo")) {
					// call activity directly
					Object adapter = parent.getAdapter();
					try {
						ResolveInfo info = (ResolveInfo)XposedHelpers.callMethod(adapter, "resolveInfoForPosition", position);
						if (info != null) {
							Intent intent = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
									.setData(Uri.fromParts("package", info.activityInfo.packageName, null))
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							parent.getContext().startActivity(intent);
							Activity a = (Activity)parent.getContext();
							a.finish();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						XposedBridge.log(e.getMessage());
					}
				} else if (longPress.equals("Default") && mAlwaysUseOption) {
					// call activity directly
					Object thisObject = parent.getTag();
					XposedHelpers.callMethod(thisObject, "startSelected", position, true);
				} else if (longPress.equals("XHalo")) {
					// call activity directly
					Object adapter = parent.getAdapter();
					try {
						Intent intent = (Intent)XposedHelpers.callMethod(adapter, "intentForPosition", position);
						if (intent != null) {
							intent.setFlags(0x00002000);
							parent.getContext().startActivity(intent);
							Activity a = (Activity)parent.getContext();
							a.finish();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						XposedBridge.log(e.getMessage());
					}
				} else if (longPress.equals("Launch")) {
					// call activity directly
					Object thisObject = parent.getTag();
					XposedHelpers.callMethod(thisObject, "startSelected", position, false);
				}
				
				return true;
			}			
		});				
	}
		
	private void setLongPress(GridView grid, String action, final boolean mAlwaysUseOption, Object thisObject, XSharedPreferences pref) {
		if (action.equals("Nothing")) return;
//		if (action.equals("Default") && mAlwaysUseOption) {
//			boolean manageList = pref.getBoolean("ManageList", false);
//			boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//			if (manageList && oldWayHide) {
//				// restore items
//				restoreListItems(thisObject, pref);
//			}
//		}
		grid.setTag(thisObject);
		
		grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				String longPress = pref.getString("LongPress", "Nothing");
				if (longPress.equals("AppInfo")) {
					// call activity directly
					Object adapter = parent.getAdapter();
					try {
						ResolveInfo info = (ResolveInfo)XposedHelpers.callMethod(adapter, "resolveInfoForPosition", position);
						if (info != null) {
							Intent intent = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
									.setData(Uri.fromParts("package", info.activityInfo.packageName, null))
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							parent.getContext().startActivity(intent);
							Activity a = (Activity)parent.getContext();
							a.finish();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						XposedBridge.log(e.getMessage());
					}
				} else if (longPress.equals("Default") && mAlwaysUseOption) {
					// call activity directly
					Object thisObject = parent.getTag();
					XposedHelpers.callMethod(thisObject, "startSelected", position, true);
				} else if (longPress.equals("XHalo")) {
					// call activity directly
					Object adapter = parent.getAdapter();
					try {
						Intent intent = (Intent)XposedHelpers.callMethod(adapter, "intentForPosition", position);
						if (intent != null) {
							intent.setFlags(0x00002000);
							parent.getContext().startActivity(intent);
							Activity a = (Activity)parent.getContext();
							a.finish();
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						XposedBridge.log(e.getMessage());
					}
				} else if (longPress.equals("Launch")) {
					// call activity directly
					Object thisObject = parent.getTag();
					XposedHelpers.callMethod(thisObject, "startSelected", position, false);
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
	
	@SuppressLint("NewApi")
	private void setRoundCorners(ViewGroup root, int color, int roundValue, int border) {
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
		
		// remove padding
		root.setPadding(0, 0, 0, 0);
		ViewGroup.MarginLayoutParams rParams = (ViewGroup.MarginLayoutParams)root.getLayoutParams();
		rParams.setMargins(border, border, border, border);
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			rParams.setMarginStart(border);
			rParams.setMarginEnd(border);
		}

		// new round corner background black border
		GradientDrawable blackBorder = new GradientDrawable(Orientation.TOP_BOTTOM, new int[] { Color.BLACK, Color.BLACK });
		blackBorder.setCornerRadii(new float[] { roundValue, roundValue, roundValue, roundValue, 
				roundValue, roundValue, roundValue, roundValue });
		
		// let's set black background
		ViewGroup rootParent = (ViewGroup)root.getParent();
		rootParent.setBackground(blackBorder);
	}
	
	@SuppressLint("NewApi")
	private void setTransparentDialog(LinearLayout root) {
		// set first child
		View first = root.getChildAt(0);
		first.setBackgroundColor(Color.TRANSPARENT);
		
		// find bottom
		int lastIndex = root.getChildCount() - 1;
		while(root.getChildAt(lastIndex).getVisibility() != View.VISIBLE) {
			lastIndex -= 1;
		}
		
		// set bottom
		View last = root.getChildAt(lastIndex);
		last.setBackgroundColor(Color.TRANSPARENT);
		
		// set in between
		if (lastIndex > 1) {
			for (int i=1; i<lastIndex;i++) {
				View m = root.getChildAt(i);
				m.setBackgroundColor(Color.TRANSPARENT);
			}
		}
		
		// remove padding
		root.setPadding(0, 0, 0, 0);
		FrameLayout.LayoutParams rootParams = (FrameLayout.LayoutParams)root.getLayoutParams();
		rootParams.setMargins(0, 0, 0, 0);		
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			rootParams.setMarginStart(0);
			rootParams.setMarginEnd(0);
		}
		
		// let's try to set root background
		FrameLayout rootParent = (FrameLayout)root.getParent();
//		rootParent.setBackgroundColor(Color.MAGENTA);
		rootParent.setPadding(0, 0, 0, 0);
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)rootParent.getLayoutParams();
		params.setMargins(0, 0, 0, 0);
		params.width = FrameLayout.LayoutParams.MATCH_PARENT;
//		params.height = 1920;
		
		// add relative layout
		final RelativeLayout relative = new RelativeLayout(root.getContext());
		relative.setGravity(Gravity.CENTER);
		FrameLayout.LayoutParams rParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1920);
		relative.setLayoutParams(rParams);
		rootParent.addView(relative);
		rootParent.removeView(root);
		relative.addView(root);
		
		// setup root gravity
		root.setGravity(Gravity.CENTER);
//		relative.setBackgroundColor(Color.YELLOW);
		
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			blurBackground(relative);
//			if (relative.getWidth() > 0) {
//				blurBackground(relative);
//			} else {
//				relative.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//					@Override
//					public void onGlobalLayout() {
//						blurBackground(relative);
//					}
//				});
//			}
		}
        
		// what is our parent?
//		FrameLayout parentParent = (FrameLayout)rootParent.getParent();
//		parentParent.setBackgroundColor(Color.YELLOW);
//		parentParent.setPadding(0, 0, 0, 0);
//		params = (FrameLayout.LayoutParams)parentParent.getLayoutParams();
//		params.width = 1080;
//		params.width = LayoutParams.MATCH_PARENT;
//		params.setMargins(0, 0, 0, 0);
//		XposedBridge.log(String.format("CAP: Parent Parent children count is: %d", parentParent.getChildCount()));		
//		XposedBridge.log(String.format("CAP: Parent Parent Child 0 is: %s", parentParent.getChildAt(0).getClass().getName()));		
//		XposedBridge.log(String.format("CAP: Parent Parent Child 1 is: %s", parentParent.getChildAt(1).getClass().getName()));		
		
		// decor view = com.android.internal.policy.impl.PhoneWindow$DecorView
//		XposedBridge.log(String.format("CAP: Total Root Parent is: %s", parentParent.getParent().getClass().getName()));		
//		XposedBridge.log(String.format("CAP: Total Root Parent Superclass is: %s", parentParent.getParent().getClass().getSuperclass().getName()));		
//		Object decorView = parentParent.getParent();
//		Object decorParam = XposedHelpers.callMethod(decorView, "getLayoutParams");
//		XposedBridge.log(String.format("CAP: DecorView LayoutParams is: %s", decorParam.getClass().getName()));		
//		decorView.setBackgroundColor(Color.CYAN);
//		decorView.setPadding(0, 0, 0, 0);
		
//		params = (FrameLayout.LayoutParams)decorView.getLayoutParams();
//		params.width = LayoutParams.MATCH_PARENT;
//		params.setMargins(0, 0, 0, 0);
//		XposedBridge.log(String.format("CAP: DecorView children count is: %d", decorView.getChildCount()));		
//		XposedBridge.log(String.format("CAP: Total Root Parent is: %s", parentParent.getParent().getClass().getName()));		
	}
	
	@SuppressLint("NewApi")
	private void blurBackground(RelativeLayout relative) {
		// create screenshot
//		Bitmap bitmap = Bitmap.createBitmap(relative.getWidth(), relative.getHeight(), Bitmap.Config.ARGB_8888);
//		Canvas c = new Canvas(bitmap);
//		relative.draw(c);
		
		Bitmap bitmap = null;
		try {
			bitmap = (Bitmap)XposedHelpers.callStaticMethod(Class.forName("android.view.SurfaceControl"), 
					"screenshot", 1080, 1920);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			XposedBridge.log(e.toString());
		}
		
		// no screenshot
		if (bitmap == null) {
			XposedBridge.log("CAP: No screenshot.");
			return;
		}
		
		// prepare for blur
		int bWidth = Math.round(bitmap.getWidth() * 0.4f);
		int bHeight = Math.round(bitmap.getHeight() * 0.4f);
		Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, bWidth, bHeight, false);
		Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
		
		// render it
		RenderScript rs = RenderScript.create(relative.getContext());
		ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
		Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(7.5f);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
		
        // set it as background
        relative.setBackground(new BitmapDrawable(relative.getContext().getResources(), outputBitmap));
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
					XposedBridge.log("CAP: Android 4.2 mCurrentResolveList");
				}
			} catch (Exception ex) { }
			if (mCurrentResolveList == null) {
				try {
					mCurrentResolveList = mAdapter.getClass().getDeclaredField("mOrigResolveList");
					if (debugOn) {
						XposedBridge.log("CAP: Android 4.4 mOrigResolveList");
					}
				} catch (Exception ex) { }
			}
			if (mCurrentResolveList == null) {
				try {
					mCurrentResolveList = mAdapter.getClass().getDeclaredField("mBaseResolveList");
					if (debugOn) {
						XposedBridge.log("CAP: Android 4.3 mBaseResolveList");
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
					XposedBridge.log("CAP: Original list is NULL.");
				}
			} else {
				List<Object> mList = (List<Object>)XposedHelpers.getObjectField(mAdapter, "mList");
				if (debugOn) {
					XposedBridge.log(String.format("CAP: mCurrent size = %d", mCurrent.size()));
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

	private void setGesture(final ListView list, String action, final boolean mAlwaysUseOption, Object thisObject, XSharedPreferences pref) {
		if (action.equals("Nothing")) return;
//		if (action.equals("Default") && mAlwaysUseOption) {
//			boolean manageList = pref.getBoolean("ManageList", false);
//			boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//			if (manageList && oldWayHide) {
//				// restore items
//				restoreListItems(thisObject, pref);
//			}
//		}
		list.setTag(thisObject);	
		
		list.setOnTouchListener(new OnTouchListener() {
			private GestureDetector gestureDetector = new GestureDetector(list.getContext(), new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onDoubleTap(MotionEvent e) {
					// report it
					return true;
				}
			});
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// setup double tap
				boolean doubleTap = gestureDetector.onTouchEvent(event);
				if (doubleTap) {
					XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
					String doubleTapAction = pref.getString("DoubleTap", "Nothing");
					int position = list.getCheckedItemPosition();
					if (position == ListView.INVALID_POSITION) {
						Toast.makeText(v.getContext(), "No selection", Toast.LENGTH_SHORT).show();
						return false;
					}
					if (doubleTapAction.equals("AppInfo")) {
						// call activity directly
						Object adapter = list.getAdapter();
						try {
							ResolveInfo info = (ResolveInfo)XposedHelpers.callMethod(adapter, "resolveInfoForPosition", position);
							if (info != null) {
								Intent intent = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
										.setData(Uri.fromParts("package", info.activityInfo.packageName, null))
										.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
								list.getContext().startActivity(intent);
								Activity a = (Activity)list.getContext();
								a.finish();
							}							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							XposedBridge.log(e.getMessage());
						}
					} else if (doubleTapAction.equals("Default") && mAlwaysUseOption) {
						// call activity directly
						Object thisObject = list.getTag();
						XposedHelpers.callMethod(thisObject, "startSelected", position, true);
					} else if (doubleTapAction.equals("XHalo")) {
						// xhalo
						Object adapter = list.getAdapter();
						try {
							Intent intent = (Intent)XposedHelpers.callMethod(adapter, "intentForPosition", position);
							if (intent != null) {
								intent.setFlags(0x00002000);
								list.getContext().startActivity(intent);
								Activity a = (Activity)list.getContext();
								a.finish();
							}							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							XposedBridge.log(e.getMessage());
						}
					} else if (doubleTapAction.equals("Launch")) {
						// call activity directly
						Object thisObject = list.getTag();
						XposedHelpers.callMethod(thisObject, "startSelected", position, false);
					}
					return true;
				}
				return false;
			}
		});
	}
	
	private void setGesture(final GridView grid, String action, final boolean mAlwaysUseOption, Object thisObject, XSharedPreferences pref) {
		if (action.equals("Nothing")) return;
//		if (action.equals("Default") && mAlwaysUseOption) {
//			boolean manageList = pref.getBoolean("ManageList", false);
//			boolean oldWayHide = pref.getBoolean("OldWayHide", false);
//			if (manageList && oldWayHide) {
//				// restore items
//				restoreListItems(thisObject, pref);
//			}
//		}
		grid.setTag(thisObject);
		
		grid.setOnTouchListener(new OnTouchListener() {
			private GestureDetector gestureDetector = new GestureDetector(grid.getContext(), new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onDoubleTap(MotionEvent e) {
					// report it
					return true;
				}
			});
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// setup double tap
				boolean doubleTap = gestureDetector.onTouchEvent(event);
				if (doubleTap) {
					XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
					String doubleTapAction = pref.getString("DoubleTap", "Nothing");
					int position = grid.getCheckedItemPosition();
					if (position == GridView.INVALID_POSITION) {
						return false;
					}
					if (doubleTapAction.equals("AppInfo")) {
						// call activity directly
						Object adapter = grid.getAdapter();
						try {
							ResolveInfo info = (ResolveInfo)XposedHelpers.callMethod(adapter, "resolveInfoForPosition", position);
							if (info != null) {
								Intent intent = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
										.setData(Uri.fromParts("package", info.activityInfo.packageName, null))
										.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
								grid.getContext().startActivity(intent);
								Activity a = (Activity)grid.getContext();
								a.finish();
							}							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							XposedBridge.log(e.getMessage());
						}
					} else if (doubleTapAction.equals("Default") && mAlwaysUseOption) {
						// call activity directly
						Object thisObject = grid.getTag();
						XposedHelpers.callMethod(thisObject, "startSelected", position, true);
					} else if (doubleTapAction.equals("XHalo")) {
						// xhalo
						Object adapter = grid.getAdapter();
						try {
							Intent intent = (Intent)XposedHelpers.callMethod(adapter, "intentForPosition", position);
							if (intent != null) {
								intent.setFlags(0x00002000);
								grid.getContext().startActivity(intent);
								Activity a = (Activity)grid.getContext();
								a.finish();
							}							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							XposedBridge.log(e.getMessage());
						}
					} else if (doubleTapAction.equals("Launch")) {
						// call activity directly
						Object thisObject = grid.getTag();
						XposedHelpers.callMethod(thisObject, "startSelected", position, false);
					}
					return true;
				}
				return false;
			}
		});
	}
	
	@SuppressLint("NewApi")
	private void setNoMargin(ViewGroup view) {
		// loop up to root and set zero padding and zero margin
		ViewGroup parent = view;
		while (parent != null) {
			parent.setPadding(0, 0, 0, 0);
			if (parent.getLayoutParams().getClass().isAssignableFrom(FrameLayout.LayoutParams.class)) {
				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)parent.getLayoutParams();
//				params.width = LayoutParams.MATCH_PARENT;
				params.width = 1080;
				params.setMargins(0, 0, 0, 0);
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
					params.setMarginStart(0);
					params.setMarginEnd(0);
				}
			} else if (parent.getLayoutParams().getClass().isAssignableFrom(ViewGroup.MarginLayoutParams.class)) {
				ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)parent.getLayoutParams();
//				params.width = LayoutParams.MATCH_PARENT;
				params.width = 1080;
				params.setMargins(0, 0, 0, 0);
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
					params.setMarginStart(0);
					params.setMarginEnd(0);
				}
			}
			
			if (parent.getParent() == null) break;
			else if (!parent.getParent().getClass().isAssignableFrom(ViewGroup.class)) break;
			parent = (ViewGroup)parent.getParent();
		}
	}
}
