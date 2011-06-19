package esis.android.sudoku.backend;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import esis.android.sudoku.R;

import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

public class MyApp extends Application {
	//FIXME BUG: when restarting device, time is reseted when loading.
	private static final String TAG = MyApp.class.getSimpleName();
	
	private static int difficulty;

	public static boolean saved_game_exists;
	public static String SUDOKU_SAVED_FILE = "saved_sudoku_game";
	public static String SUDOKU_HIGHSCORED_FILE = "sudoku_highscores";
	public static DataOutputStream dos;
	public static FileOutputStream fos;

	public static int getdifficulty() {
		return MyApp.difficulty;
	}

	public static void setDifficulty(int id){
	    switch (id) {
	        case R.id.radio_easy:
	            MyApp.difficulty = 1;//Easy
	            break;
	        case R.id.radio_medium:
	            MyApp.difficulty = 2;//Medium
	            break;
	        case R.id.radio_hard:
	            MyApp.difficulty = 3;//Hard
	            break;
	        default:
	            break;
	    }
	    Log.d(TAG, "difficulty changed to " + MyApp.getdifficulty());	    
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

}
