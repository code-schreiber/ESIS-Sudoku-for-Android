package esis.android.sudoku.backend;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

/**
 * Class made to make chronometer pausing easier.
 * 
 * @author Sebastian Guillen
 */

public class MyChronometer extends Chronometer {

	/** The last time paused. */
	private static long lastTimePaused;
	
	/* NOTE: The three constructors avoid an Inflate Exception
	 * when changing default attributes in XML file. */
	public MyChronometer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyChronometer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyChronometer(Context context) {
		super(context);
	}
	
	/**
	 * Resume chronometer from previously saved time.
     	 */
	public void resume() {
		this.setBase(getResumeTime());
		this.start();
	}

	/**
	 * Pause chronometer and save his time.
	 */
	public void pause() {
		setLastTimePaused(this.getBase());
		this.stop();
	}

	/**
	 * Change the base to now.
	 */
	public void reset() {
		this.setBase(SystemClock.elapsedRealtime());
	}

	/**
         * Sets the last time paused.
         * 
         * @param base the new last time paused
         */
	private static void setLastTimePaused(long base) {
		lastTimePaused = base - SystemClock.elapsedRealtime();
	}

	/**
         * Gets the resume time.
         * 
         * @return the resume time
         */
	private static long getResumeTime() {
		return lastTimePaused + SystemClock.elapsedRealtime();
	}


}
