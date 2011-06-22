package esis.android.sudoku.frontend;


import java.io.DataInputStream;
import java.util.Random;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.BackendSudoku;
import esis.android.sudoku.backend.FileSystemTool;
import esis.android.sudoku.backend.MyApp;
import esis.android.sudoku.backend.MyChronometer;

/**
 * @author Sebastian Guillen
 * TODO 's: 
 * Sign for 12 years (2033)
 * make settings with view
 */

public class Game extends Activity {

	private static final String TAG = Game.class.getSimpleName();
	private final int SIZE = BackendSudoku.SIZE;
	private int triesCounter = 0;
	private BackendSudoku backendsudoku;
	private int removedNrs = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		InitButtons();
		removedNrs = backendsudoku.getHowManyNumbersToRemove(MyApp.getdifficulty());
		InitCellListeners();
		NewGame();
	}

	/**	Release focus when number is typed in (listener to all cells) */
	private void InitCellListeners() {
	    for (int row = 0; row < SIZE; ++row)
	    	for (int column = 0; column < SIZE; ++column){
	    	    final EditText guiText = (EditText) findViewById(getEditTextId(row, column));		
	    	    guiText.addTextChangedListener(new TextWatcher() {
			        public void afterTextChanged(Editable s) {
			        	if(guiText.length() != 0){
			        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			        		imm.hideSoftInputFromWindow(guiText.getWindowToken(), 0);
			        		triesCounter++;
			        		if (triesCounter >= removedNrs)
			        			checkIfWon(true);	
			        	}
			        	else
			        		triesCounter--;
			        }
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
						if(guiText.length() > 0)
							guiText.setSelection(1);
					}
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						/* Nothing */}
	    	    });
	    	}
	}

	private void NewGame() {
		backendsudoku = new BackendSudoku();
		if (MyApp.saved_game_exists) {// Load previously saved game
			loadGame();// copy user grid to GUI
		} else {
			backendsudoku.create_game(MyApp.getdifficulty());
			if (!MyApp.difficultyIsValid())
				Log.e(TAG, "Difficulty not set.");			
			Log.d(TAG, "New Game Called with difficulty " + MyApp.getdifficulty());
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
			EditText guiText = (EditText) findViewById(getEditTextId(row, column));
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
	    	    EditText guiText = (EditText) findViewById(getEditTextId(row, column));				
	    	    if (guiText.isFocusable() && guiText.isEnabled() && !guiText.getText().toString().equals(""))
	    		guiCells[row][column] = Integer.parseInt(guiText.getText().toString());
	    	    else
	    		guiCells[row][column] = 0;
	    	}
	}

	private void loadGame() {
		int[][] user_entered_numbers = new int[SIZE][SIZE];

		DataInputStream dis = null;
		dis = FileSystemTool.openFileToLoad(getApplicationContext());
		int loadedDifficulty = FileSystemTool.readBytes(dis);
		loadDifficulty(loadedDifficulty);
		long savedTime = FileSystemTool.getsavedTime(dis);
		loadData(dis, user_entered_numbers);
		FileSystemTool.closeFis(dis);

		// write the unsolved grid in the GUI
		copyGrid();		
		// write user entered numbers in the GUI
		copyUserNumbersToGui(user_entered_numbers);		
		//Start Chronometer from saved time
		((MyChronometer) findViewById(R.id.chronometer)).setBase(savedTime);
		((MyChronometer) findViewById(R.id.chronometer)).start();
	}

	private void loadDifficulty(int loadedDifficulty) {	    
	    if (loadedDifficulty == MyApp.EASY)
	        loadedDifficulty = R.id.radio_easy;
	    else if (loadedDifficulty == MyApp.MEDIUM)
	        loadedDifficulty = R.id.radio_medium;
	    else if (loadedDifficulty == MyApp.HARD)
	    	loadedDifficulty = R.id.radio_hard;
	    else
		return;
	    MyApp.setDifficulty(loadedDifficulty);
	}

	private void copyUserNumbersToGui(int[][] user_entered_numbers) {
	    for (int row = 0; row < SIZE; ++row)
	        for (int column = 0; column < SIZE; ++column)
	    	if (user_entered_numbers[row][column] != 0) {
	    	    EditText guiText = (EditText) findViewById(getEditTextId(row, column));
	    	    setUserCell(guiText, Integer.toString(user_entered_numbers[row][column]));
	    	}
	}

	private void loadData(DataInputStream dis, int[][] user_entered_numbers) {
	    for (int row = 0; row < SIZE; ++row)
	    	for (int column = 0; column < SIZE; ++column) {
	    		// Read cell from solved
	    		backendsudoku.solved_grid[row][column] = FileSystemTool.readBytes(dis);
	    		// Read cell from unsolved
	    		backendsudoku.unsolved_grid[row][column] = FileSystemTool.readBytes(dis);
	    		// Read cells entered from user
	    		user_entered_numbers[row][column] = FileSystemTool.readBytes(dis);
	    	}
	}

	private void InitButtons() {//TODO make buttons same size    	
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
			    	EditText guiText = (EditText) findViewById(getEditTextId(row, column));
				int backendCellNumber = backendsudoku.unsolved_grid[row][column];
				if (backendCellNumber != 0)
					setGivenCell(guiText, backendCellNumber);
				else
					setUserCell(guiText, "");
			}
		}
	}

	private void setUserCell(EditText gT, String text) {
		gT.setText(text);
		gT.setEnabled(true);
		gT.setFocusable(true);
		gT.setTextColor(getResources().getColor(R.color.solid_black));
	}
	

	private void setGivenCell(EditText gT, int backendCellNumber) {
		gT.setText(Integer.toString(backendCellNumber));
		gT.setEnabled(false);
		gT.setFocusable(false);
		gT.setTextColor(getResources().getColor(color.primary_text_dark));
	}

	private int getEditTextId(int row, int column) {
		TableLayout tl = (TableLayout) findViewById(R.id.SudokuGridLayout);
		return ((TableRow) tl.getChildAt(row)).getChildAt(column).getId();
	}

	private void CheckGrid() {
		boolean checking = false;// Unchecking
		if (((Button) findViewById(R.id.CheckButton)).getText().equals("Check"))
			checking = true;// Checking
		check(checking);
		checkIfWon(checking);
	}

	private void checkIfWon(boolean checking) {
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
			text = "Check";

		((Button) findViewById(R.id.CheckButton)).setText(text);
		enableOrDisableHelpResetPause(!action);
		((Button) findViewById(R.id.SaveButton)).setEnabled(!action);

    	for (int row = 0; row < SIZE; ++row)
    	    for (int column = 0; column < SIZE; ++column) {
    		EditText guiText = (EditText) findViewById(getEditTextId(row, column));
        		if (guiText.isFocusable()) {// if it is a user cell
        		    guiText.setEnabled(!action);
        		    if (!guiText.getText().toString().equals("")) {
        			if (!action)
        			    guiText.setTextColor(getResources().getColor(R.color.solid_black));
        			else
        			    markMistakes(guiText, row, column);
        		    }
        		}
    	    }
	}

	private void enableOrDisableHelpResetPause(boolean b) {
		((Button) findViewById(R.id.HelpButton)).setEnabled(b);
		((Button) findViewById(R.id.ResetButton)).setEnabled(b);
		((Button) findViewById(R.id.PauseButton)).setEnabled(b);	    
	}

	private void markMistakes(TextView guiText, int row, int column) {
		if (guiText.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
			guiText.setTextColor(getResources().getColor(R.color.solid_blue));// good TODO IDEA: only mark errors.
		else
			guiText.setTextColor(getResources().getColor(R.color.solid_red));// make red
	}

	private boolean sudokuIsComplete() {
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
			    EditText guiText = (EditText) findViewById(getEditTextId(row, column));
				if (guiText.getText().toString().equals(""))
					return false;
			}
		return true;
	}

	private boolean sudokuIsCorrect() {
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
			    EditText guiText = (EditText) findViewById(getEditTextId(row, column));
				if (!guiText.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
					return false;
			}
		return true;
	}

	private void keepPlaying(boolean action) {
		if (action)
			Toast.makeText(this, R.string.keepPlayingText, Toast.LENGTH_LONG).show();
	}

	private void gameWon() {
		MyChronometer c = ((MyChronometer) findViewById(R.id.chronometer));
		c.stop();
		MyApp.saved_game_exists = false;// no chance to load an already won game
		check(false);// uncheck the game in background

		new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon)
			.setTitle(R.string.Won_Title)
			.setMessage(R.string.Won_Mesage)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        		    public void onClick(DialogInterface dialog, int whichButton) {
        			/* User clicked OK so do some stuff */
        			NewGame();
        			//TODO delete saved file
        		    }
        		}).setNegativeButton("Menu", new DialogInterface.OnClickListener() {
        			    public void onClick(DialogInterface dialog,
        				    int whichButton) {
        				/* User clicked Cancel so do some stuff */
        				Game.this.finish();
        			    }
        		}).create().show();
		saveHighscore(c.getText().toString());
	}

	private void saveHighscore(String newTime) {
		MyApp ma = (MyApp) getApplication();
		String difficulty = ma.getDifficultyString();
		
	    SharedPreferences settings = getSharedPreferences(MyApp.HIGHSCORES, MODE_WORLD_WRITEABLE);
	    String oldHighscore = settings.getString(difficulty, Highscores.defValue);
	    Log.d(TAG, "saving highscore with diff "+
	    		difficulty+" and time "+
	    		newTime+" and old hs "+
	    		oldHighscore);

	    /* Save the time if its was faster than old highscore. */
		if (oldHighscore.equals(Highscores.defValue))
			setNewHighscore(difficulty, newTime, settings);
	    else{
	    	String tempnt = newTime.replace(":", "");
	    	String temoldhs = oldHighscore.replace(":", "");
	    	if(Integer.parseInt(tempnt) < Integer.parseInt(temoldhs))
	    		setNewHighscore(difficulty, newTime, settings);
	    }
	}

	private void setNewHighscore(String difficulty, String time, SharedPreferences settings) {
		Toast.makeText(this, "Highscore in " + difficulty, Toast.LENGTH_LONG).show();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(difficulty, time);
        if(!editor.commit())
    		Log.e(TAG, "Couldn't set new highscore");
	}


}
