package esis.android.sudoku.backend;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class FileSystemTool {

    private static final String TAG = FileSystemTool.class.getSimpleName();
    private final static int SIZE = BackendSudoku.SIZE;

    public static void openFile(Context context, long base, int difficulty) {
	try {
	    MyApp.fos = context.openFileOutput(MyApp.SUDOKU_SAVED_FILE,Context.MODE_PRIVATE);
	    MyApp.dos = new DataOutputStream(MyApp.fos);
	    Log.d(TAG, "saving time: " + (base - System.currentTimeMillis()));// FIXME delete
	    // Save Chronometer's time
	    MyApp.dos.writeLong(base - System.currentTimeMillis());
	    //Save Difficulty
	    MyApp.dos.writeByte(difficulty);
	    // set flag to load this saved game the next time a game starts
	    MyApp.saved_game_exists = true;
	} catch (FileNotFoundException e) {
	    MyApp.saved_game_exists = false;
	    Log.e(TAG, e.getMessage());// TODO Make all exceptions log to console
	} catch (IOException e) {
	    MyApp.saved_game_exists = false;
	    Log.e(TAG, e.getMessage());
	}
    }
    
    public static void writeGameToFile(int solvedGrid[][], int unsolvedGrid[][], int guiCells[][]) {

	try {
	    for (int row = 0; row < SIZE; ++row)
		for (int column = 0; column < SIZE; ++column) {
		    // Write cell from solved
		    MyApp.dos.writeByte(solvedGrid[row][column]);
		    // Write cell from unsolved
		    MyApp.dos.writeByte(unsolvedGrid[row][column]);
		    // Write cells entered from user
		    MyApp.dos.writeByte(guiCells[row][column]);
		}
	} catch (IOException e) {
	    MyApp.saved_game_exists = false;
	    Log.e(TAG, e.getMessage());
	}
	closeDos();
    }

    private static void closeDos() {
	try {
	    MyApp.dos.close();// this closes fos also
	} catch (IOException e) {
	    MyApp.saved_game_exists = false;
	    Log.e(TAG, e.getMessage());
	}
    }

    public static FileInputStream openFileToLoad(FileInputStream fis, Context context) {
	try {
	    fis = context.openFileInput(MyApp.SUDOKU_SAVED_FILE);
	} catch (FileNotFoundException e) {
	    Log.e(TAG, e.getMessage());
	}
	return fis;
    }    

    public static long getsavedTime(FileInputStream fis) {
	long savedTime = 0;
	for (int i = 0; i < 8; i++)
	    try {
		savedTime += fis.read();
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
	    }
	Log.d(TAG, "returning saved time: " + savedTime);// FIXME delete
	return savedTime + System.currentTimeMillis();
    }

    public static int readBytes(FileInputStream fis) {
	int aByte = 0;
	try {
	    aByte = fis.read();
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
	return aByte;
    }

    public static void closeFis(FileInputStream fis) {
	try {
	    fis.close();
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
    }

}
