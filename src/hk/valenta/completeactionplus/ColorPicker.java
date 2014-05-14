package hk.valenta.completeactionplus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ColorPicker {
	// interface
	public interface OnResultListener {
		void OnCancel(ColorPicker dialog);
		void OnDone(ColorPicker dialog, int color);
	}
	
	// listener
	final OnResultListener listener;
	final float[] currentColorHsv = new float[3];
	final View hueView;
	final View satView;
	final View newColor;
	final AlertDialog alert;
	final ImageView cursorHue;
	final ImageView cursorSat;
	final EditText hexEdit;
	
	// constructor
	@SuppressWarnings("deprecation")
	@SuppressLint("WorldReadableFiles")
	public ColorPicker(final Context context, int color, OnResultListener listener) {
		// setup properties
		this.listener = listener;
		Color.colorToHSV(color, currentColorHsv);
		
		// setup views
		final View layer = LayoutInflater.from(context).inflate(R.layout.fragment_color_picker, null);
		hueView = layer.findViewById(R.id.fragment_color_picker_hue);
		satView = layer.findViewById(R.id.fragment_color_picker_sat);
		View origColor = layer.findViewById(R.id.fragment_color_orig);
		cursorHue = (ImageView)layer.findViewById(R.id.fragment_color_cursor_hue);
		cursorSat = (ImageView)layer.findViewById(R.id.fragment_color_cursor_sat);
		origColor.setBackgroundColor(color);
		newColor = layer.findViewById(R.id.fragment_color_new);
		newColor.setBackgroundColor(color);
		hexEdit = (EditText)layer.findViewById(R.id.fragment_color_hexEdit);
		drawSat(currentColorHsv[0]);
		hexEdit.setText(String.format("#%06X", (0xFFFFFF & color)));
		
		// hue slider
		hueView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// only events what we want
				int action = event.getAction();
				if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
					// get position
					float posX = event.getX();
					int width = view.getMeasuredWidth();
					if (posX < 0) posX = 0;
					if (posX > width) posX = width - 0.001f;
					
					// calculate hue
					float hue = 360.0f / width * posX;
					currentColorHsv[0] = hue;
					drawSat(hue);
					newColor.setBackgroundColor(Color.HSVToColor(currentColorHsv));
					hexEdit.setText(String.format("#%06X", (0xFFFFFF & Color.HSVToColor(currentColorHsv))));
					moveHueCursor(posX);
					
					return true;
				}
				
				return false;
			}
		});
		
		// sat slider
		satView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// only events what we want
				int action = event.getAction();
				if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
					// get position
					float posX = event.getX();
					float posY = event.getY();
					int width = view.getMeasuredWidth();
					int height = view.getMeasuredHeight();
					if (posX < 0) posX = 0;
					if (posX > width) posX = width;
					if (posY < 0) posY = 0;
					if (posY > height) posY = height;
					
					// set it
					currentColorHsv[1] = 1.0f / width * posX;
					currentColorHsv[2] = 1.0f - (1.0f / height * posY);					
					newColor.setBackgroundColor(Color.HSVToColor(currentColorHsv));
					hexEdit.setText(String.format("#%06X", (0xFFFFFF & Color.HSVToColor(currentColorHsv))));
					moveSatCursor(posX, posY);
					
					return true;
				}
				
				return false;
			}
		});
		
		// set color in hex
		hexEdit.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				// enter
				if (actionId == EditorInfo.IME_ACTION_DONE) {

					try {
						// parse color
						int color = Color.parseColor(hexEdit.getText().toString());
						
						// let's update
						Color.colorToHSV(color, currentColorHsv);
						newColor.setBackgroundColor(color);						
						drawSat(currentColorHsv[0]);
						
						// move cursors
						moveHueCursor(currentColorHsv[0] * hueView.getMeasuredWidth() / 360.0f);
						moveSatCursor(currentColorHsv[1] * satView.getMeasuredWidth() / 1.0f, (1.0f - currentColorHsv[2]) * satView.getMeasuredHeight() / 1.0f);
					} catch (Exception ex) {
						// invalid color
						hexEdit.setText(String.format("#%06X", (0xFFFFFF & Color.HSVToColor(currentColorHsv))));
					}
					
					// hide keyboard
					((InputMethodManager)hexEdit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(hexEdit.getWindowToken(), 0);
					
					return true;
				} else {
					return false;
				}
			}
		});
		
		// get current configuration
		SharedPreferences pref = context.getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		int currentTheme = EnumConvert.themeIndex(pref.getString("AppTheme", "Light"));
//		int theme = android.R.style.Theme_Holo_Light_NoActionBar;
		if (currentTheme == 1) {
//			theme = android.R.style.Theme_Holo_NoActionBar;
			ImageView arrow = (ImageView)layer.findViewById(R.id.fragment_color_arrow);
			arrow.setImageResource(R.drawable.icon_arrow_white);
		}
		
		// setup alert
		alert = new AlertDialog.Builder(context)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// success
						if (ColorPicker.this.listener != null) {
							ColorPicker.this.listener.OnDone(ColorPicker.this, Color.HSVToColor(currentColorHsv));
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// cancel
						if (ColorPicker.this.listener != null) {
							ColorPicker.this.listener.OnCancel(ColorPicker.this);
						}
					}
				})
				.setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						// cancel
						if (ColorPicker.this.listener != null) {
							ColorPicker.this.listener.OnCancel(ColorPicker.this);
						}
					}
				}).create();	
		alert.setView(layer);
		
		// initialise cursors
		ViewTreeObserver vto = layer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// Initialise cursors
				moveHueCursor(currentColorHsv[0] * hueView.getMeasuredWidth() / 360.0f);
				moveSatCursor(currentColorHsv[1] * satView.getMeasuredWidth() / 1.0f, (1.0f - currentColorHsv[2]) * satView.getMeasuredHeight() / 1.0f);
				layer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}
	
	private void drawSat(float hue) {
		DisplayMetrics metrics = satView.getContext().getResources().getDisplayMetrics();
		ShapeDrawable shape = new ShapeDrawable(new RectShape());
		LinearGradient bw = new LinearGradient(0.f, 0.f, 0.f, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 256, metrics),
				0xffffffff, 0xff000000, TileMode.CLAMP);
		LinearGradient my = new LinearGradient(0.f, 0.f, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 256, metrics), 0.f,
				0xffffffff, Color.HSVToColor(new float[] { hue, 1.f, 1.f }), TileMode.CLAMP);		
		shape.getPaint().setShader(new ComposeShader(bw, my, PorterDuff.Mode.MULTIPLY));
		satView.setBackground(shape);
	}
	
	private void moveHueCursor(float posX) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)cursorHue.getLayoutParams();
		params.leftMargin = (int)(hueView.getLeft() + posX - (cursorHue.getMeasuredWidth() / 2));
		cursorHue.setLayoutParams(params);
	}
	
	private void moveSatCursor(float posX, float posY) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)cursorSat.getLayoutParams();
		params.leftMargin = (int)(satView.getLeft() + posX - (cursorSat.getMeasuredWidth() / 2));
		params.topMargin = (int)(satView.getTop() + posY - (cursorSat.getMeasuredHeight() / 2));
		cursorSat.setLayoutParams(params);
	}
	
	public void show() {
		alert.show();
	}
}
