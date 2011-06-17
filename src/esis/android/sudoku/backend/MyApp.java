package esis.android.sudoku.backend;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

public class MyApp extends Application {
	//FIXME BUG: when restarting device, time is reseted when loading.
	private static final String TAG = MyApp.class.getSimpleName();
	
	private int difficulty;

	private static long savedTime;
	public static boolean saved_game_exists;
	public static String SUDOKU_SAVED_FILE = "saved_sudoku_game";
	public static DataOutputStream dos;
	public static FileOutputStream fos;

	public int getdifficulty() {
		return this.difficulty;
	}

	public void setdifficulty(int d) {
		this.difficulty = d;
	}
	
	public static long getsavedTime() {
		Log.d(TAG, "time was: "+savedTime);
		return savedTime  + System.currentTimeMillis();		
	}
	
	public static void saveTime(long base) {
		savedTime = base - System.currentTimeMillis();;		
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
