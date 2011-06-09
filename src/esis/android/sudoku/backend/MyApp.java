package esis.android.sudoku.backend;

import java.io.DataOutputStream;

import android.app.Application;

public class MyApp extends Application{
	
	  private int difficulty;
	  public static String SUDOKU_SAVED_FILE = "saved_sudoku_game";
	  public static DataOutputStream dos;
	  public static boolean GIVEN = true;
	  public static boolean saved_game_exists = false; 

	  public int getdifficulty(){
	    return difficulty;
	  }
	  public void setdifficulty(int d){
	    difficulty = d;
	  }

}
