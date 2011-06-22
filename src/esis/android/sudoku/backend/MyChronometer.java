package esis.android.sudoku.backend;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

/**
 * @author Sebastian Guillen
 *
 */

/* Class made to make chronometer pausing easier */
public class MyChronometer extends Chronometer {

	static long lastTimePaused;
	
	/*
	 * NOTE: The three constructors avoid an Inflate Exception when
	 * changing default attributes in XML file.
	 */
	public MyChronometer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyChronometer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyChronometer(Context context) {
		super(context);
	}
	
	public void resume() {
		this.setBase(getResumeTime());
		this.start();
	}

	public void pause() {
		setLastTimePaused(this.getBase());
		this.stop();
	}

	public void reset() {
		this.setBase(SystemClock.elapsedRealtime());
	}

	private static void setLastTimePaused(long base) {
		lastTimePaused = base - SystemClock.elapsedRealtime();
	}

	private static long getResumeTime() {
		return lastTimePaused + SystemClock.elapsedRealtime();
	}
	

}
