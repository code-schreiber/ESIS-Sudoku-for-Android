package esis.android.sudoku.frontend;


import java.io.FileInputStream;
import java.util.Random;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.BackendSudoku;
import esis.android.sudoku.backend.FileSystemTool;
import esis.android.sudoku.backend.MyApp;
import esis.android.sudoku.backend.MyChronometer;

//Sign for 12 years (2033)
//TODO release focus when number is typed in (listener to all cells)
//TODO make highscores with view
//TODO make settings with view
public class Game extends Activity {

	private static final String TAG = Game.class.getSimpleName();
	private final int SIZE = BackendSudoku.SIZE;
	private EditText guiText;
	private BackendSudoku backendsudoku;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		InitButtons();
		NewGame();
	}

	private void NewGame() {
		MyApp myapp = (MyApp) getApplicationContext();
		backendsudoku = new BackendSudoku();
		if (MyApp.saved_game_exists) {// Load previously saved game
			loadGame();// copy user grid to GUI
		} else {
			Log.d(TAG, "Creating grid..............");
			backendsudoku.create_game(myapp.getdifficulty());
			Log.d(TAG, "Grid Created...............");

			if (myapp.getdifficulty() != 1 && myapp.getdifficulty() != 2 && myapp.getdifficulty() != 3)
				Log.e(TAG, "Difficulty not set.");
			
			Log.d(TAG, "New Game Called with difficulty " + myapp.getdifficulty());

			ResetGame();
			
		}
		((Button) findViewById(R.id.CheckButton)).setEnabled(true);
		enableOrDisableHelpResetPause(true);
		((MyChronometer) findViewById(R.id.chronometer)).start();
	}

	private void PauseGame() {
		boolean action;
		final View sudokuGridLayout = findViewById(R.id.SudokuGridLayout);
		Button button = (Button) findViewById(R.id.PauseButton);
		
		if (sudokuGridLayout.getVisibility() == View.VISIBLE) {
			Log.d(TAG, "Pausing Game");
			action = false;// pause
			((MyChronometer) findViewById(R.id.chronometer)).pause();
			button.setText("Resume");
			sudokuGridLayout.setVisibility(View.GONE);// INVISIBLE
		} else {
			Log.d(TAG, "Resuming Game");
			action = true;// Resume
			((MyChronometer) findViewById(R.id.chronometer)).resume();
			button.setText("Pause");
			sudokuGridLayout.setVisibility(View.VISIBLE);// VISIBLE
		}

		((Button) findViewById(R.id.CheckButton)).setEnabled(action);
		((Button) findViewById(R.id.HelpButton)).setEnabled(action);
		((Button) findViewById(R.id.ResetButton)).setEnabled(action);

	}

	private void ResetGame() {
		copyGrid();
		((MyChronometer) findViewById(R.id.chronometer)).reset();
	}

	private void HelpGame() {
		if (sudokuIsComplete()) {
			Toast.makeText(this, R.string.no_help_needed, Toast.LENGTH_SHORT).show();
			return;
		}
		// Look for an empty place, a better implementation would know which
		// places are empty, and would take one randomly
		Random rand = new Random();
		int row;
		int column;
		while (true) {
			row = rand.nextInt(SIZE);
			column = rand.nextInt(SIZE);
			guiText = (EditText) findViewById(getEditTextId(row, column));
			if (guiText.getText().toString().equals("")) {
				guiText.requestFocus();
				guiText.setText(Integer.toString(backendsudoku.solved_grid[row][column]));
				return;
			}
		}
	}

	private void saveGame() {
		Toast.makeText(this, R.string.saving_game, Toast.LENGTH_SHORT).show();
		//Open file
		long base = ((MyChronometer) findViewById(R.id.chronometer)).getBase();
		FileSystemTool.openFile(getApplicationContext(), base, MyApp.getdifficulty());
		int[][] guiCells = new int[SIZE][SIZE];
		copyGuiCellsToArray(guiCells);
		FileSystemTool.writeGameToFile(backendsudoku.solved_grid, backendsudoku.unsolved_grid, guiCells);
	}

	private void copyGuiCellsToArray(int[][] guiCells) {
	    for (int row = 0; row < SIZE; ++row)
	    	for (int column = 0; column < SIZE; ++column){
	    		guiText = (EditText) findViewById(getEditTextId(row, column));				
	    		if (guiText.isFocusable() && guiText.isEnabled() && !guiText.getText().toString().equals(""))
	    		    guiCells[row][column] = Integer.parseInt(guiText.getText().toString());
	    		else
	    		    guiCells[row][column] = 0;
	    	}
	}

	private void loadGame() {
		FileInputStream fis = null;
		int[][] user_entered_numbers = new int[SIZE][SIZE];
		
		fis = FileSystemTool.openFileToLoad(fis, getApplicationContext());
		int loadedDifficulty = FileSystemTool.readBytes(fis);
		loadDifficulty(loadedDifficulty);
		long savedTime = FileSystemTool.getsavedTime(fis);
		loadData(fis, user_entered_numbers);
		FileSystemTool.closeFis(fis);
		
		// write the unsolved grid in the GUI
		copyGrid();		
		// write user entered numbers in the GUI
		copyUserNumbersToGui(user_entered_numbers);		
		//Start Chronometer from saved time
		((MyChronometer) findViewById(R.id.chronometer)).setBase(savedTime);
		((MyChronometer) findViewById(R.id.chronometer)).start();
	}

	private void loadDifficulty(int loadedDifficulty) {
	    Log.d(TAG, "Loading difficulty: " + loadedDifficulty);
	    if (loadedDifficulty == 1)
	        loadedDifficulty = R.id.radio_easy;
	    else if (loadedDifficulty == 2)
	        loadedDifficulty = R.id.radio_medium;
	    else if (loadedDifficulty == 3)
	    	loadedDifficulty = R.id.radio_hard;
	    else
		return;
	       
	    ((RadioGroup) findViewById(R.id.DifficultyRadioGroup)).check(loadedDifficulty);
	    MyApp.setDifficulty(((RadioGroup) findViewById(R.id.DifficultyRadioGroup)).getCheckedRadioButtonId());
	}

	private void copyUserNumbersToGui(int[][] user_entered_numbers) {
	    for (int row = 0; row < SIZE; ++row)
	        for (int column = 0; column < SIZE; ++column)
	    	if (user_entered_numbers[row][column] != 0) {
	    	    guiText = (EditText) findViewById(getEditTextId(row, column));
	    	    guiText.setText(Integer.toString(user_entered_numbers[row][column]));
	    	}
	}

	private void loadData(FileInputStream fis, int[][] user_entered_numbers) {
	    for (int row = 0; row < SIZE; ++row)
	    	for (int column = 0; column < SIZE; ++column) {
	    		// Read cell from solved
	    		backendsudoku.solved_grid[row][column] = FileSystemTool.readBytes(fis);
	    		// Read cell from unsolved
	    		backendsudoku.unsolved_grid[row][column] = FileSystemTool.readBytes(fis);
	    		// Read cells entered from user
	    		user_entered_numbers[row][column] = FileSystemTool.readBytes(fis);
	    	}
	}

	private void InitButtons() {	    	
		Button button;

		button = (Button) findViewById(R.id.CheckButton);
		button.setEnabled(false);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CheckGrid();
			}
		});
		button = (Button) findViewById(R.id.HelpButton);
		button.setEnabled(false);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				HelpGame();
			}
		});
		button = (Button) findViewById(R.id.ResetButton);
		button.setEnabled(false);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ResetGame();
			}
		});
		button = (Button) findViewById(R.id.PauseButton);
		button.setEnabled(false);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PauseGame();
			}
		});
		button = (Button) findViewById(R.id.SaveButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveGame();
			}
		});
	}

	private void copyGrid() {
		for (int row = 0; row < SIZE; ++row) {
			for (int column = 0; column < SIZE; ++column) {
				guiText = (EditText) findViewById(getEditTextId(row, column));
				int backendCellNumber = backendsudoku.unsolved_grid[row][column];
				if (backendCellNumber != 0)
					setGivenCell(guiText, backendCellNumber);
				else
					setUserCell(guiText);
			}
		}
	}

	private void setUserCell(EditText gT) {
		gT.setText("");
		gT.setEnabled(true);
		gT.setFocusable(true);
		gT.setTextColor(getResources().getColor(R.color.solid_black));
	}

	private void setGivenCell(EditText gT, int backendCellNumber) {
		gT.setEnabled(false);
		gT.setFocusable(false);
		gT.setText(Integer.toString(backendCellNumber));
		gT.setTextColor(getResources().getColor(color.primary_text_dark));
	}

	private int getEditTextId(int row, int column) {
		TableLayout tl = (TableLayout) findViewById(R.id.SudokuGridLayout);
		return ((TableRow) tl.getChildAt(row)).getChildAt(column).getId();
	}

	private void CheckGrid() {

		boolean checking = false;// Unchecking
		if (((Button) findViewById(R.id.CheckButton)).getText().equals(" Check "))
			checking = true;// Checking

		check(checking);

		if (sudokuIsComplete())
			if (sudokuIsCorrect())
				gameWon();
			else
				keepPlaying(checking);
	}

	private void check(boolean action) {
		Log.d(TAG, "check called with action " + action);
		String text;

		if (action)
			text = "Uncheck";
		else
			text = " Check ";

		((Button) findViewById(R.id.CheckButton)).setText(text);
		enableOrDisableHelpResetPause(!action);
		((Button) findViewById(R.id.SaveButton)).setEnabled(!action);


        	for (int row = 0; row < SIZE; ++row)
        	    for (int column = 0; column < SIZE; ++column) {
        		guiText = (EditText) findViewById(getEditTextId(row, column));
        		if (guiText.isFocusable()) {// if it is a user cell
        		    guiText.setEnabled(!action);
        		    if (!guiText.getText().toString().equals("")) {
        			if (!action)
        			    guiText.setTextColor(getResources().getColor(R.color.solid_black));
        			else
        			    markMistakes(row, column);
        		    }
        		}
        	    }
	}

	private void enableOrDisableHelpResetPause(boolean b) {
		((Button) findViewById(R.id.HelpButton)).setEnabled(b);
		((Button) findViewById(R.id.ResetButton)).setEnabled(b);
		((Button) findViewById(R.id.PauseButton)).setEnabled(b);	    
	}

	private void markMistakes(int row, int column) {
		if (guiText.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
			guiText.setTextColor(getResources().getColor(R.color.solid_blue));// good TODO IDEA: only mark errors.
		else
			guiText.setTextColor(getResources().getColor(R.color.solid_red));// make red
	}

	private boolean sudokuIsComplete() {
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
				guiText = (EditText) findViewById(getEditTextId(row, column));
				if (guiText.getText().toString().equals(""))
					return false;
			}
		return true;
	}

	private boolean sudokuIsCorrect() {
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
				guiText = (EditText) findViewById(getEditTextId(row, column));
				if (!guiText.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
					return false;
			}
		return true;
	}

	private void keepPlaying(boolean action) {
		if (action)
			Toast.makeText(this, R.string.keepPlayingText, Toast.LENGTH_LONG)
					.show();
	}

	private void gameWon() {

		((MyChronometer) findViewById(R.id.chronometer)).stop();
		MyApp.saved_game_exists = false;// no chance to load an already won game
		check(true);// uncheck the game in background

		new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle(R.string.Won_Title)
			.setMessage(R.string.Won_Mesage)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked OK so do some stuff */
						NewGame();
					}
				}).setNegativeButton("Menu",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked Cancel so do some stuff */
								Game.this.finish();
							}
						}).create().show();
		//TODO here: save the time if its faster than THE HIGSCORE.
		((RadioGroup) findViewById(R.id.DifficultyRadioGroup)).getCheckedRadioButtonId();//TODO save checked radio button also when saving
	}

}
