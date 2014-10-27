package hk.valenta.completeactionplus;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class ManagerPagerActivity extends FragmentActivity {

	ManagerPageAdapter pageAdapter;
	ViewPager viewPager;
	int selectedBackupIndex = 0;
	File restoreFile = null;
	ArrayList<String> cachePackage;
	ArrayList<Drawable> cacheIcons;
	ArrayList<String> cacheNames;
	
	
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
		
		// initialize cache
		cachePackage = new ArrayList<String>();
		cacheIcons = new ArrayList<Drawable>();
		cacheNames = new ArrayList<String>();
		
		// setup pager
		pageAdapter = new ManagerPageAdapter(getSupportFragmentManager());
		viewPager = (ViewPager)findViewById(R.id.manager_pager_viewPager);
		viewPager.setAdapter(pageAdapter);
		viewPager.setCurrentItem(0);
		
		// any file to open?
		Intent launch = getIntent();
		String uriString = launch.toUri(0);
		if (uriString != null && uriString.length() > 0) {
			restoreFile = new File(Uri.parse(uriString).getPath());
		}
		if (restoreFile != null && restoreFile.exists()) {
			// we got file to restore
			AlertDialog restore = new AlertDialog.Builder(this)
				.setTitle(R.string.manager_restore_rules)
				.setMessage(String.format(getString(R.string.manager_restore_intent), restoreFile.getName()))
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dismiss
						dialog.dismiss();
					}
				})
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// share it
						dialog.dismiss();
						proceedWithRestore(restoreFile);
					}
				})
				.create();
			restore.show();
		}
	}	
	
	@SuppressLint("SimpleDateFormat")
	public void backupButtonClick(View view) {
		// setup input for name
		final EditText edit = new EditText(this);
		edit.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		edit.setText(String.format("rules_%s", dateFormat.format(new Date())));
		
		// setup dialog
		AlertDialog pickName = new AlertDialog.Builder(this)
			.setTitle(R.string.manager_backup_rules)
			.setView(edit)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// dismiss
					dialog.dismiss();
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// proceed
					proceedWithBackup(String.format("%s.xml", edit.getText().toString()));
					dialog.dismiss();
				}
			})
			.create();
		pickName.show();
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	private void proceedWithBackup(String filename) {
		// get current configuration
		Log.d("Backup", filename);
		SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		Map<String,?> all = pref.getAll();
		
		// build xml file
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		xml.append("<rules version=\"1.9.3\">\n");
		
		// loop and add
		for (Map.Entry<String, ?> entry : all.entrySet()) {
			String key = entry.getKey(); 
			if (key.contains(";")) {
				// add to list
				xml.append(String.format("\t<rule key=\"%s\">%s</rule>\n", key, (String)entry.getValue()));
			}
		}
		xml.append("</rules>");
		
		// folder
		File folder = new File(Environment.getExternalStorageDirectory() + "/CompleteActionPlus");
		if (!folder.exists() && !folder.mkdir()) {
			// create folder failed
			Log.d("Backup", "failed create folder");
			backupFailed();
			return;
		}
		
		// save it
		final File file = new File(folder.getAbsolutePath() + "/" + filename);
		try {
			FileWriter writer = new FileWriter(file, false);
			writer.write(xml.toString());
			writer.flush();
			writer.close();
			
			// toast it
			Log.d("Backup", "done");
			Toast.makeText(this, R.string.manager_backup_success, Toast.LENGTH_LONG).show();
			
			// share it?
			AlertDialog share = new AlertDialog.Builder(this)
				.setTitle(R.string.manager_backup_success)
				.setMessage(R.string.manager_backup_send)
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// dismiss
						dialog.dismiss();
					}
				})
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// share it
						dialog.dismiss();
						Intent share = new Intent(Intent.ACTION_SEND);
						share.setType("application/xml");
						share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
						share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
						startActivity(share);
					}
				})
				.create();
			share.show();			
		} catch (Exception e) {
			// failed
			Log.d("Backup", "failed create file");
			e.printStackTrace();
			backupFailed();
		}
	}
	
	private void backupFailed() {
		// create message
		AlertDialog folderFailed = new AlertDialog.Builder(this)
			.setTitle(R.string.manager_backup_rules)
			.setMessage(R.string.manager_backup_failed)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// dismiss
					dialog.dismiss();
				}
			})
			.create();
		folderFailed.show();		
	}
	
	public void restoreButtonClick(View view) {
		// folder
		File folder = new File(Environment.getExternalStorageDirectory() + "/CompleteActionPlus");
		if (!folder.exists()) {
			backupNotFound();
			return;
		}
		
		// get restore files
		final File[] backups = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				// only xml
				return filename.endsWith(".xml");
			}
		});
		if (backups.length == 0) {
			backupNotFound();
			return;
		}
		
		// let's sort by date (descending)
		Arrays.sort(backups, new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				// TODO Auto-generated method stub
				if (lhs.lastModified() > rhs.lastModified()) {
					return -1;
				} else if (lhs.lastModified() < rhs.lastModified()) {
					return +1;
				} else {
					return 0;
				}
			}
		});
		
		// create filename list only
		String[] filenames = new String[backups.length];
		for (int i=0; i<backups.length; i++) {
			String name = backups[i].getName();
			filenames[i] = name.substring(0, name.length() - 4);
		}
		
		// choose dialog
		AlertDialog choose = new AlertDialog.Builder(this)
			.setTitle(R.string.manager_choose_backup)
			.setSingleChoiceItems(filenames, selectedBackupIndex, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// set selection
					selectedBackupIndex = which;
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// dismiss
					dialog.dismiss();
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// proceed
					dialog.dismiss();
					proceedWithRestore(backups[selectedBackupIndex]);
				}
			})
			.create();		
		choose.show();
	}
	
	private void backupNotFound() {
		// create message
		AlertDialog restoreFailed = new AlertDialog.Builder(this)
			.setTitle(R.string.manager_restore_rules)
			.setMessage(R.string.manager_no_backup)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// dismiss
					dialog.dismiss();
				}
			})
			.create();
		restoreFailed.show();		
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	private void proceedWithRestore(File backup) {
		try {
			// setup document
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.parse(backup);
	        doc.normalizeDocument();
	        
	        // get root node
	        Element root = doc.getDocumentElement();
	        
	        // validate
	        if (!root.getNodeName().equals("rules") || root.getAttribute("version") == null) {
	        	// invalid
	        	restoreFailed();
	        	return;
	        }
	        
            // get rule nodes
            NodeList rules = doc.getElementsByTagName("rule");
    		SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
	        int length = rules.getLength();
	        for (int i=0; i<length; i++) {
	        	Node r = rules.item(i);
	        	Element e = (Element)r;
	        	
	        	// set config values
	        	String key = e.getAttribute("key");
	        	String value = r.getChildNodes().item(0).getNodeValue();
	        	if (key != null &&  value != null) {
		        	pref.edit().putString(key, value).apply();
	        	}
	        }
	        
	        // done
	        Toast.makeText(this, R.string.manager_restore_success, Toast.LENGTH_LONG).show();
	        
	        // refresh views
	        viewPager.setAdapter(null);
	        viewPager.setAdapter(pageAdapter);
		} catch (Exception e) {
			// invalid xml
			e.printStackTrace();
			restoreFailed();			
		}
	}
	
	private void restoreFailed() {
		// create message
		AlertDialog restoreFailed = new AlertDialog.Builder(this)
			.setTitle(R.string.manager_restore_rules)
			.setMessage(R.string.manager_restore_failed)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// dismiss
					dialog.dismiss();
				}
			})
			.create();
		restoreFailed.show();		
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
			} else if (position == 2) {
				return getString(R.string.fragment_added);
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
			} else if (position == 2) {
				return new AddedFragment();
			} else {
				return null;
			}
		}

		@Override
		public int getCount() {
			// number of pages
			return 3;
		}		
	}
}
