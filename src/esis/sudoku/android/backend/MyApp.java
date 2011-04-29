package esis.sudoku.android.backend;

import java.io.DataOutputStream;

import android.app.Application;

public class MyApp extends Application{
	
	  private int difficulty;
	  public static String NUMBERS_FILE = "saved_sudoku_numbers";
	  public static String CELLTYPE_FILE = "saved_sudoku_celltypes";
	  public static DataOutputStream dos_numbers;
	  public static DataOutputStream dos_cells;
	  public static boolean GIVEN = true;
	  public static boolean LOAD_GAME = false; 

	  public int getdifficulty(){
	    return difficulty;
	  }
	  public void setdifficulty(int d){
	    difficulty = d;
	  }

}
