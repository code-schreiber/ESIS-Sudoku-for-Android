package esis.android.sudoku.backend;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Random;

import android.app.Application;
import android.util.Log;
import esis.android.sudoku.R;

/**
 * The Class MyApp is the singleton of this App.
 *
 * @author Sebastian Guillen
 */

public class MyApp extends Application {

	private static final String TAG = MyApp.class.getSimpleName();
	public static boolean saved_game_exists;
	public static DataOutputStream dos;
	public static FileOutputStream fos;
	
	/* Constants */
	public static final int EASY = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;
		
	/* Shared Preferences */
	public static final String HIGHSCORES = "sudoku_highscores";
	public static final String PREFERED_DIFFICULTY = "preffered_difficulty";

	/**
	 * Gets the difficulty from the given id.
	 *
	 * @param id the id of the radio button
	 * @return the difficulty as 1,2 or 3
	 */
	public static int getDifficulty(int id){
	    switch (id) {
	        case R.id.radio_easy:
	            return EASY;
	        case R.id.radio_medium:
	            return MEDIUM;
	        case R.id.radio_hard:
	            return HARD;
	        default:
	            Log.e(TAG, "Th1s should not happen");
	            return EASY;//Should not happen
	    }
	}
	
	/**
	 * Gets the difficulty name.
	 *
	 * @return the difficulty name
	 */
	public String getDifficultyString(int d) {
	    switch (d) {
	        case MyApp.EASY:
	            return getString(R.string.Easy);
	        case MyApp.MEDIUM:
	            return getString(R.string.Medium);
	        case MyApp.HARD:
	            return getString(R.string.Hard);
	        default:
	            Log.e(TAG, "This sh0uld not happen");
	            return "";//Should not happen
	    }
	}
	
	/**
	 * Gets the difficulty id.
	 *
	 * @param d the d
	 * @return the difficulty radio button's id
	 */
	public static int getDifficultyID(int d) {
	    switch (d) {
	        case MyApp.EASY:
	            return R.id.radio_easy;
	        case MyApp.MEDIUM:
	            return R.id.radio_medium;
	        case MyApp.HARD:
	            return R.id.radio_hard;
	        default:
	            Log.e(TAG, "Thi3 should not happen");
	            return 0;//Should not happen
	    }
	}
	
	/**
	 * Randomises text for OK buttons.
	 * @return the random text
	 */
	public static String getPositiveText() {
		String[] all = {"OK", "ok", "Ok", "oK", "oki", "0K", "yeap", "yes", "yep", "ja", "da", "si", "positive", "go!"};
		Random rand = new Random();
		int i= rand.nextInt(all.length);
		return all[i];
	}



}
