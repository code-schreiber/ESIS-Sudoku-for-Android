package esis.android.sudoku.backend;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Application;
import android.util.Log;
import esis.android.sudoku.R;

/**
 * @author Sebastian Guillen
 *
 */

public class MyApp extends Application {

	private static final String TAG = MyApp.class.getSimpleName();
	
	private static int difficulty;
	public static boolean saved_game_exists;
	public static DataOutputStream dos;
	public static FileOutputStream fos;
	
	/* Constants */
	public static final int EASY = 1;
	public static final int MEDIUM = 2;
	public static final int HARD = 3;
	public static final String SUDOKU_SAVED_FILE = "saved_sudoku_game";
	public static final String HIGHSCORES = "sudoku_highscores";
	public static final String PREFERED_DIFFICULTY = "preffered_difficulty";//TODO get rid of MyApp.difficulty 

	public static int getdifficulty() {
		return MyApp.difficulty;
	}

	public static void setDifficulty(int id){
	    switch (id) {
	        case R.id.radio_easy:
	            MyApp.difficulty = EASY;
	            break;
	        case R.id.radio_medium:
	            MyApp.difficulty = MEDIUM;
	            break;
	        case R.id.radio_hard:
	            MyApp.difficulty = HARD;
	            break;
	    }
	    Log.d(TAG, "Difficulty changed to " + MyApp.getdifficulty());	    
	}

	public void checkForSavedGame() {
		MyApp.saved_game_exists = true;
		try {
			FileInputStream fis = openFileInput(MyApp.SUDOKU_SAVED_FILE);
			if(fis.read()== -1)
				MyApp.saved_game_exists = false;
			fis.close();
		} catch (FileNotFoundException e) {
			MyApp.saved_game_exists = false;
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			MyApp.saved_game_exists = false;
			Log.e(TAG, e.getMessage());
		}
	}
	
	public String getDifficultyString() {
	    switch (MyApp.difficulty) {
	        case MyApp.EASY:
	            return getString(R.string.Easy);
	        case MyApp.MEDIUM:
	            return getString(R.string.Medium);
	        case MyApp.HARD:
	            return getString(R.string.Hard);
	        default:
	            return "";//Should not happen
	    }
	}
	
	public static int getDifficultyID(int d) {
	    switch (d) {
	        case MyApp.EASY:
	            return R.id.radio_easy;
	        case MyApp.MEDIUM:
	            return R.id.radio_medium;
	        case MyApp.HARD:
	            return R.id.radio_hard;
	        default:
	            return 0;//Should not happen
	    }
	}
	
/*	If needed, here it stays.

	public static int getDifficultyID() {
	    switch (MyApp.difficulty) {
	        case MyApp.EASY:
	            return R.id.radio_easy;
	        case MyApp.MEDIUM:
	            return R.id.radio_medium;
	        case MyApp.HARD:
	            return R.id.radio_hard;
	        default:
	            return 0;//Should not happen
	    }
	}
*/
	public static boolean difficultyIsValid() {
	    return !(MyApp.difficulty < MyApp.EASY || MyApp.difficulty > MyApp.HARD);
	}


}
