package esis.android.sudoku.backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.util.Log;
import esis.android.sudoku.R;

/**
 * This class does the writing/reading when saving/loading the game.
 * @author Sebastian Guillen
 *
 */

public class FileSystemTool {

    private static final String TAG = FileSystemTool.class.getSimpleName();
    private final static int SIZE = BackendSudoku.SIZE;
    private static final String SUDOKU_SAVED_FILE = "saved_sudoku_game";

	public static String getSavedGamesDate(Context c){
    	 String path = c.getFilesDir()+File.separator+SUDOKU_SAVED_FILE;
 		 Date lastModified = new Date(new File(path).lastModified());
		 String date = lastModified.toLocaleString();
		 return date;    	 
    }
    
    public static void openFileToSave(Context context, long base, int difficulty, int tries) {
		try {
		    opendos(context);
		    //Save Difficulty
		    MyApp.dos.writeByte(difficulty);
		    // Save Chronometer's time
		    Log.d(TAG, "saving time: "+base+"-"+System.currentTimeMillis()+"="+ (base - System.currentTimeMillis()));// FIXME delete
		    MyApp.dos.writeLong(base - System.currentTimeMillis());
		    Log.d(TAG, "saving tries: "+tries);
		    MyApp.dos.writeByte(tries);
		    // set flag to load this saved game the next time a game starts
		    MyApp.saved_game_exists = true;
		} catch (FileNotFoundException e) {
		    MyApp.saved_game_exists = false;
		    Log.e(TAG, e.getMessage());
		} catch (IOException e) {
		    MyApp.saved_game_exists = false;
		    Log.e(TAG, e.getMessage());
		}
    }

	private static void opendos(Context context) throws FileNotFoundException {
		MyApp.fos = context.openFileOutput(SUDOKU_SAVED_FILE,Context.MODE_PRIVATE);
		MyApp.dos = new DataOutputStream(MyApp.fos);
	}
    
    public static void writeGameToFile(int solvedGrid[][], int unsolvedGrid[][], int guiCells[][]) {
		try {
		    for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
				Log.d(TAG, "Writing: "+solvedGrid[row][column]+unsolvedGrid[row][column]+guiCells[row][column]);
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
			    "" + e.getMessage());
		    MyApp.saved_game_exists = false;
		}
		return dis;
    }    

    public static long getsavedTime(DataInputStream dis) {
		long savedTime = 0;
		    try {
		    	savedTime = dis.readLong();
		    } catch (IOException e) {
		    	Log.e(TAG, e.getMessage());
		    }
		Log.d(TAG, "Saved time: " + savedTime);// FIXME delete
		return savedTime + System.currentTimeMillis();
    }

    public static int readBytes(DataInputStream dis) {
		int aByte = 0;
		try {
		    aByte = dis.read();
		} catch (IOException e) {
		    Log.e(TAG, e.getMessage());
		}
		Log.d(TAG, "Read: "+aByte);
		return aByte;
    }

    public static void closeFis(DataInputStream dis) {
		try {
		    dis.close();
		} catch (IOException e) {
		    Log.e(TAG, e.getMessage());
		}
    }

    public static void deleteSavedFile(Context c) {
        MyApp.saved_game_exists = false;
    	c.deleteFile(FileSystemTool.SUDOKU_SAVED_FILE);
    }
    
}
