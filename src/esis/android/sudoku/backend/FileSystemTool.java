package esis.android.sudoku.backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.util.Log;
import esis.android.sudoku.R;

/**
 * @author Sebastian Guillen
 *
 */

public class FileSystemTool {

    private static final String TAG = FileSystemTool.class.getSimpleName();
    private final static int SIZE = BackendSudoku.SIZE;
    private static final String SUDOKU_SAVED_FILE = "saved_sudoku_game";

    public static void openFile(Context context, long base, int difficulty) {
	try {
	    MyApp.fos = context.openFileOutput(SUDOKU_SAVED_FILE,Context.MODE_PRIVATE);
	    MyApp.dos = new DataOutputStream(MyApp.fos);
	    MyApp.dos.writeByte(difficulty);
	    // set flag to load this saved game the next time a game starts
	    Log.d(TAG, "saving time: " + (base - System.currentTimeMillis()));// FIXME delete
	    // Save Chronometer's time
	    MyApp.dos.writeLong(base - System.currentTimeMillis());
	    //Save Difficulty
	    MyApp.saved_game_exists = true;
	} catch (FileNotFoundException e) {
	    MyApp.saved_game_exists = false;
	    Log.e(TAG, e.getMessage());
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

    public static DataInputStream openFileToLoad(Context context) {
	DataInputStream dis = null;
	try {		
            FileInputStream fis = context.openFileInput(SUDOKU_SAVED_FILE);
            dis = new DataInputStream(fis);
	} catch (FileNotFoundException e) {
	    Log.d(TAG, 
		    context.getString(R.string.no_game_to_load) +
		    "" + e.getMessage());//AUSPROBIEREN IN EMULATOR
	    MyApp.saved_game_exists = false;
	}
	return dis;
    }    

    public static long getsavedTime(DataInputStream dis) {
	long savedTime = 0;
	    try {
		savedTime += dis.readLong();
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
	    }
	Log.d(TAG, "returning saved time: " + savedTime);// FIXME delete
	return savedTime + System.currentTimeMillis();
    }

    public static int readBytes(DataInputStream dis) {
	int aByte = 0;
	try {
	    aByte = dis.read();
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
	return aByte;
    }

    public static void closeFis(DataInputStream dis) {
	try {
	    dis.close();
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
    }


}
