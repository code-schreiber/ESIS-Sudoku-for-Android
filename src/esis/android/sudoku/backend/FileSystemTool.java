package esis.android.sudoku.backend;

import java.io.FileInputStream;
import java.io.IOException;


public class FileSystemTool {

	public static void writeBytes(int solvedGrid, int unsolvedGrid, int userCell) {
		
		try {
			// Write cell from solved
			MyApp.dos.writeByte(solvedGrid);
			// Write cell from unsolved
			MyApp.dos.writeByte(unsolvedGrid);
			// Write cells entered from user
			MyApp.dos.writeByte(userCell);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void closeDos() {
		try {
			MyApp.dos.close();//this closes fos also	
		} catch (IOException e) {
			MyApp.saved_game_exists = false;
			e.printStackTrace();
		}
	}

	public static int readBytes(FileInputStream fis) {
		int target = 0;
		try {
			target = fis.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return target;
	}

	public static void closeFis(FileInputStream fis) {
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
