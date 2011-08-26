/**
 * 
 */
package esis.android.sudoku.backend;

import android.R.style;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

/**
 * This class does most of the work for our {@link PopupWindow} so it's easier
 * to use.
 * 
 * @author Sebastian Guillen
 */
public class MyPopup extends PopupWindow {

	public MyPopup(View anchor) {
		super(anchor.getContext());
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		/* Remove background imposed by super (constructor) popup window does
		 * not respond to onTouch or onKey events unless it has a background
		 * that != null	 */
		this.setBackgroundDrawable(new BitmapDrawable());
		this.setAnimationStyle(style.Animation_Toast);// TODO try with style.Animation_Dialog,Animation_Toast
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		setListener();
	}

	private void setListener() {
		// When User changed his mind and touches outside
		this.setTouchInterceptor(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					// when a touch even happens outside of the window make the window go away
					MyPopup.this.dismiss();
					return true;
				}
				return false;
			}
		});
	}
}
