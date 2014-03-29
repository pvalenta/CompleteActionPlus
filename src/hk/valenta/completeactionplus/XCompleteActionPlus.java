package hk.valenta.completeactionplus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.android.internal.app.AlertController;
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
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
					param.setResult("1.8.1");
					return "1.8.1";
				}
			});
		}
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity", null, "onItemClick", AdapterView.class, View.class, int.class, long.class,
				new XC_MethodReplacement() {
			@SuppressWarnings("unchecked")
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
				
//				// let's find our resolver
//				Field[] fields = rObject.getDeclaredFields();
//				Field resolver = null;
//				for (Field f : fields) {
//					String name = f.getName();
//					if (name.equals("mGrid") || name.equals("mListView")) {
//						resolver = f;
//						break;
//					}
//				}
//				if (resolver == null) {
//					XposedBridge.log("Resolver field not found.");
//					return null;
//				}

//				// move up
//				AbsListView rControl = (AbsListView)resolver.get(param.thisObject);
//				if (rControl == null) {
//					XposedBridge.log("Resolver field found, but it's null.");
//					return null;
//				}
				
//				// get adapter
//				Field mAdapter = rObject.getDeclaredField("mAdapter");
//				BaseAdapter adapter = (BaseAdapter)mAdapter.get(param.thisObject);
				
//				// check for valid
//				if (rControl.getCount() == 0 || position > rControl.getCount() || rControl.getItemAtPosition(position) == null ||
//						adapter.isEmpty() || position > adapter.getCount() || adapter.getItem(position) == null) {
//					// invalid call
//					return null;
//				}
				
				// get method
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
				boolean showAlways = pref.getBoolean("ShowAlways", false);
				if (showAlways) {
					// get view
					boolean always = isAlwaysChecked((View)param.args[0]);
					boolean manageList = pref.getBoolean("ManageList", false);
					if (always && manageList) {
						// get adapter
						Field mAdapter = rObject.getDeclaredField("mAdapter");
						Object adapter = mAdapter.get(param.thisObject);
						Field mCurrentResolveList = null;
						try {
							mCurrentResolveList = adapter.getClass().getDeclaredField("mCurrentResolveList");
							XposedBridge.log("Android 4.2 mCurrentResolveList");
						} catch (Exception ex) { }
						if (mCurrentResolveList == null) {
							try {
								mCurrentResolveList = adapter.getClass().getDeclaredField("mOrigResolveList");
								XposedBridge.log("Android 4.4 mOrigResolveList");
							} catch (Exception ex) { }
						}
						if (mCurrentResolveList == null) {
							try {
								mCurrentResolveList = adapter.getClass().getDeclaredField("mBaseResolveList");
								XposedBridge.log("Android 4.3 mBaseResolveList");
							} catch (Exception ex) { }
						}
						List<ResolveInfo> mCurrent = null;
						if (mCurrentResolveList != null) {
							mCurrent =(List<ResolveInfo>)mCurrentResolveList.get(adapter); 
						}						
						if (mCurrent == null) {
							XposedBridge.log("Original list is NULL.");
						} else {
							Field mList = adapter.getClass().getDeclaredField("mList");
							List<Object> mL = (List<Object>)mList.get(adapter);
							XposedBridge.log(String.format("mCurrent size = %d", mCurrent.size()));
							if (mCurrent.size() != mL.size()) {
								// get DisplayResolveInfo class
								Class<?> DisplayResolveInfo = mL.get(0).getClass();
								Constructor<?> driCon = DisplayResolveInfo.getDeclaredConstructors()[0];
								driCon.setAccessible(true);
								
								// add missing one back
								for (ResolveInfo r : mCurrent) {
									boolean missing = true;
									for (Object l : mL) {
										// get resolve info
										Field ri = l.getClass().getDeclaredField("ri");
										ResolveInfo info = (ResolveInfo)ri.get(l);
										if (info.activityInfo.packageName.equals(r.activityInfo.packageName)) {
											missing = false;
											break;
										}
									}
									if (missing) {
										// let's add back
										Object n = driCon.newInstance(param.thisObject, r, "", "", null);
										mL.add(n);
									}
								}
							}
						}
					}
					startSelected(param.thisObject, rObject, position, always);
				} else {
					// call it
					startSelected(param.thisObject, rObject, position, false);
				}
				
				return null;
			}
		});
		XposedHelpers.findAndHookMethod("com.android.internal.app.ResolverActivity", null, "onCreate", Bundle.class, Intent.class, CharSequence.class, 
				Intent[].class, List.class, boolean.class, new XC_MethodHook() {
			
			Unhook hookResolveAttribute = null;
			
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				hookResolveAttribute = XposedHelpers.findAndHookMethod(Resources.Theme.class, "resolveAttribute", int.class, TypedValue.class, boolean.class, new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						return false;
					}
				});
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
				if (root.getChildCount() == 2) {
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
					makeManageListButton(param, rObject, myIntent, !layoutStyle.equals("Default"), theme, pref);
				} else {
					themeTitleView(param, rObject, !layoutStyle.equals("Default"), theme, pref);
				}
				
				// dialog gravity
				Window currentWindow = (Window)XposedHelpers.callMethod(param.thisObject, "getWindow");
				setDialogGravity(root.getContext(), currentWindow, pref);
				currentWindow.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				
				// change layout?
				boolean activeXHalo = pref.getBoolean("ActiveXHalo", false);
				if (layoutStyle.equals("Default")) {
					if (activeXHalo) {
						if (resolver.get(param.thisObject).getClass().equals(GridView.class)) {
							// set it
							GridView resGrid = (GridView)resolver.get(param.thisObject);
							setHaloWindow(resGrid);
						} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
							// set it
							ListView resList = (ListView)resolver.get(param.thisObject);
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
						if (activeXHalo) {
							setHaloWindow(resGrid);
						}
					} else if (resolver.get(param.thisObject).getClass().equals(ListView.class)) {
						// set it
						ListView resList = (ListView)resolver.get(param.thisObject);
						if (activeXHalo) {
							setHaloWindow(resList);
						}
					}
					
					return;
				}

				// get adapter
				Field mAdapter = rObject.getDeclaredField("mAdapter");
				BaseAdapter adapter = (BaseAdapter)mAdapter.get(param.thisObject);
				
				// let's get new layout
				if (layoutStyle.equals("List")) {
					ListView list = (ListView)frame.getChildAt(1);
					list.setAdapter(adapter);
					list.setOnItemClickListener((OnItemClickListener)param.thisObject);
					if (activeXHalo) {
						setHaloWindow(list);
					}
				} else if (layoutStyle.equals("Grid")) {
					GridView grid = (GridView)frame.getChildAt(1);
					int columns = getColumnsNumber(frame.getContext(), pref);
					int itemCounts = adapter.getCount();
					if (columns > itemCounts) {
						// wrap content
						//currentWindow.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
						
						// reduce columns?
						columns = itemCounts;
						if (pref.getBoolean("DontReduceColumns", false)) {
							grid.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
							DisplayMetrics metrics = grid.getContext().getResources().getDisplayMetrics();
							grid.setColumnWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics));
							grid.setStretchMode(GridView.NO_STRETCH);
						}
					} else {
						// fill parent
						//currentWindow.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					}
					grid.setNumColumns(columns);
					grid.setAdapter(adapter);
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
				Field mIntent = param.thisObject.getClass().getDeclaredField("mIntent");
				Intent myIntent = (Intent)mIntent.get(param.thisObject);
				String intentId = String.format("%s;%s;%s", myIntent.getAction(), myIntent.getType(), myIntent.getScheme());
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
					Field mList = param.thisObject.getClass().getDeclaredField("mList");
					List<Object> items = (List<Object>)mList.get(param.thisObject);
					
					// get original list to solve KitKat issue
					List<ResolveInfo> baseList = null;
					try {
						Field mBaseResolveList = param.thisObject.getClass().getDeclaredField("mBaseResolveList");
						baseList = (List<ResolveInfo>)mBaseResolveList.get(param.thisObject);
						if (baseList == null) {
							baseList = new ArrayList<ResolveInfo>();
						}
					} catch (Exception ex) { }
					
					// let's try to find
					for (String h : hiddenItems) {
						int count = items.size();
						for (int i=0; i<count; i++) {
							// get resolve info
							Field ri = items.get(i).getClass().getDeclaredField("ri");
							ResolveInfo info = (ResolveInfo)ri.get(items.get(i));
							
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
					Field mList = param.thisObject.getClass().getDeclaredField("mList");
					List<Object> items = (List<Object>)mList.get(param.thisObject);
					
					// loop by favourites
					int favIndex = 0;
					int itemSize = items.size();
					for (int i=0; i<itemSize; i++) {
						// get resolve info
						Object o = items.get(i);
						Field ri = o.getClass().getDeclaredField("ri");
						ResolveInfo info = (ResolveInfo)ri.get(o);
						
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
		Button button_always = (Button)liparam.view.findViewById(liparam.res.getIdentifier("button_always", "id", framework));
		if (button_always != null) {
			hideElement(button_always);
		}		
		Button button_once = (Button)liparam.view.findViewById(liparam.res.getIdentifier("button_once", "id", framework));
		if (button_once != null) {
			hideElement(button_once);
		}
		
		// get current configuration
		XSharedPreferences pref = new XSharedPreferences("hk.valenta.completeactionplus", "config");
		String theme = pref.getString("LayoutTheme", "Default");
		boolean showAlways = pref.getBoolean("ShowAlways", false);
		if (showAlways) {			
			// add check box
			LinearLayout buttonBar = (LinearLayout)liparam.view.findViewById(liparam.res.getIdentifier("button_bar", "id", framework));
			if (buttonBar != null) {
				addAlwaysCheckbox(liparam, buttonBar, button_always.getText(), theme, pref);
				if (theme.equals("Light")) {
					buttonBar.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
				} else if (theme.equals("Dark")) {
					buttonBar.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
				}
			}
		} else {
			// hide button bar
			LinearLayout buttonBar = (LinearLayout)liparam.view.findViewById(liparam.res.getIdentifier("button_bar", "id", framework));
			if (buttonBar != null) {
				hideElement(buttonBar);
			}
		}
		
		// change layout?
		String layoutStyle = pref.getString("LayoutStyle", "Default");
		if (!layoutStyle.equals("Default")) {
			// get element
			View resolver_grid = liparam.view.findViewById(liparam.res.getIdentifier(listName, "id", framework));
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
		GridLayout.LayoutParams params = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		if (pref.getBoolean("DontReduceColumns", false)) {
//			params.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//			grid.setStretchMode(GridView.NO_STRETCH);
//		} else {
//			params.setGravity(Gravity.CENTER);
//		}
		grid.setLayoutParams(params);
		if (theme.equals("Light")) {
			grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
		} else if (theme.equals("Dark")) {
			grid.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
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
		if (theme.equals("Light")) {
			list.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
		} else if (theme.equals("Dark")) {
			list.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
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
		if (layoutStyle.equals("Default")) return;

		// what layout
		String theme = pref.getString("LayoutTheme", "Default");
		if (layoutStyle.equals("List")) {
			convertToAOSPListItem(liparam, (LinearLayout)liparam.view, pref.getString("ListTextSize", "Regular"), theme, framework, pref);
		} else if (layoutStyle.equals("Grid")) {
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
			Class<?> alertActivity = resolverActivity.getSuperclass();
			Field mAlert = alertActivity.getDeclaredField("mAlert");
			AlertController aControl = (AlertController)mAlert.get(param.thisObject);

			// get title view
			Field mTitleView = aControl.getClass().getDeclaredField("mTitleView");
			TextView titleView = (TextView)mTitleView.get(aControl);
			
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
			Field mAdapter = resolverActivity.getDeclaredField("mAdapter");
			Object adapter = (Object)mAdapter.get(param.thisObject);
			Method getCount = adapter.getClass().getDeclaredMethod("getCount");
			int count = (Integer)getCount.invoke(adapter);
			ArrayList<String> items = new ArrayList<String>();
			Method resolveInfoForPosition = adapter.getClass().getDeclaredMethod("resolveInfoForPosition", int.class);
			for (int i=0; i<count; i++) {
				ResolveInfo info = (ResolveInfo)resolveInfoForPosition.invoke(adapter, i);
				if (!items.contains(info.activityInfo.packageName)) {
					items.add(info.activityInfo.packageName);
				}
			}
			
			// let's assemble intent
			Intent manage = new Intent(Intent.ACTION_EDIT);
			manage.putExtra("action", myIntent.getAction());
			manage.putExtra("type", myIntent.getType());
			manage.setType("complete/action");
			manage.putExtra("scheme", myIntent.getScheme());
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
				if (theme.equals("Light")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.BLACK));
					titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
				} else if (theme.equals("Dark")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
					titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
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
			Class<?> alertActivity = resolverActivity.getSuperclass();
			Field mAlert = alertActivity.getDeclaredField("mAlert");
			AlertController aControl = (AlertController)mAlert.get(param.thisObject);
			
			// get title view
			Field mTitleView = aControl.getClass().getDeclaredField("mTitleView");
			TextView titleView = (TextView)mTitleView.get(aControl);
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
				if (theme.equals("Light")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.BLACK));
					titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.WHITE));
				} else if (theme.equals("Dark")) {
					titleView.setTextColor(pref.getInt("TextColor", Color.parseColor("#BEBEBE")));
					titleParent.setBackgroundColor(pref.getInt("BackgroundColor", Color.parseColor("#101214")));
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
	
	private void startSelected(Object thisObject, Class<?> thisClass, int position, boolean always) {
		try {
			// get method
			Method startSelected = thisClass.getDeclaredMethod("startSelected", int.class, boolean.class);
			
			// call selected value
			//XposedBridge.log(String.format("StartSelected(%d,%b)", position, always));
			startSelected.invoke(thisObject, position, always);
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
					Method intentForPosition = adapter.getClass().getDeclaredMethod("intentForPosition", int.class);
					Intent intent = (Intent)intentForPosition.invoke(adapter, position);
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
					Method intentForPosition = adapter.getClass().getDeclaredMethod("intentForPosition", int.class);
					Intent intent = (Intent)intentForPosition.invoke(adapter, position);
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
}
