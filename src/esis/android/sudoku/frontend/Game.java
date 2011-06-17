package esis.android.sudoku.frontend;


import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
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

	private Button CheckButton;
	private Button ResetButton;
	private Button HelpButton;
	private Button PauseButton;
	private Button saveButton;
	private MyChronometer chronometer;

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

		CheckButton.setEnabled(true);
		HelpButton.setEnabled(true);
		ResetButton.setEnabled(true);
		PauseButton.setEnabled(true);

		chronometer.start();
	}

	private void PauseGame() {
		boolean action;
		final View sudokuGridLayout = findViewById(R.id.SudokuGridLayout);
		
		if (sudokuGridLayout.getVisibility() == View.VISIBLE) {
			Log.d(TAG, "Pausing Game");
			action = false;// pause
			chronometer.pause();
			PauseButton.setText("Resume");
			sudokuGridLayout.setVisibility(View.GONE);// INVISIBLE
		} else {
			Log.d(TAG, "Resuming Game");
			action = true;// Resume
			chronometer.resume();
			PauseButton.setText("Pause");
			sudokuGridLayout.setVisibility(View.VISIBLE);// VISIBLE
		}

		CheckButton.setEnabled(action);
		HelpButton.setEnabled(action);
		ResetButton.setEnabled(action);

	}

	private void ResetGame() {
		copyGrid();
		chronometer.reset();
	}

	private void HelpGame() {
		if (sudokuIsComplete()) {
			Toast.makeText(this, R.string.no_help_needed, Toast.LENGTH_SHORT)
					.show();
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

		//Save Chronometer's time
		MyApp.saveTime(chronometer.getBase());
		// set flag to load this saved game the next time a game starts
		MyApp.saved_game_exists = true;
		try {
			MyApp.fos = openFileOutput(MyApp.SUDOKU_SAVED_FILE, Context.MODE_PRIVATE);
			MyApp.dos = new DataOutputStream(MyApp.fos);
		} catch (FileNotFoundException e) {
			MyApp.saved_game_exists = false;
			Log.e(TAG, e.getMessage());//TODO Make all exceptions log to console
		}
		
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column){
				guiText = (EditText) findViewById(getEditTextId(row, column));
				int userCell = 0;
				if (guiText.isFocusable() && guiText.isEnabled() && !guiText.getText().toString().equals(""))
					userCell = Integer.parseInt(guiText.getText().toString());
				FileSystemTool.writeBytes(backendsudoku.solved_grid[row][column], backendsudoku.unsolved_grid[row][column], userCell );
			}
		FileSystemTool.closeDos();
		
	}

	private void loadGame() {

		FileInputStream fis = null;
		int[][] user_entered_numbers = new int[SIZE][SIZE];
		
		try {
			fis = openFileInput(MyApp.SUDOKU_SAVED_FILE);
	
		} catch (FileNotFoundException e) {
			Toast.makeText(this, R.string.no_game_to_load, Toast.LENGTH_SHORT).show();
			Log.e(TAG, e.getMessage());
		}
		

		
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
				// Read cell from solved
				backendsudoku.solved_grid[row][column] = FileSystemTool.readBytes(fis);
				// Read cell from unsolved
				backendsudoku.unsolved_grid[row][column] = FileSystemTool.readBytes(fis);
				// Read cells entered from user
				user_entered_numbers[row][column] = FileSystemTool.readBytes(fis);
			}	
		
		FileSystemTool.closeFis(fis);
		
		// write the unsolved_grid in the GUI
		copyGrid();
		
		// write user entered numbers in the GUI
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column)
				if (user_entered_numbers[row][column] != 0) {
					guiText = (EditText) findViewById(getEditTextId(row, column));
					guiText.setText(Integer.toString(user_entered_numbers[row][column]));
				}
		
		//Start Chronometer from saved time
		chronometer.setBase(MyApp.getsavedTime());
		chronometer.start();
		
	}

	private void InitButtons() {

		CheckButton = (Button) findViewById(R.id.CheckButton);
		ResetButton = (Button) findViewById(R.id.ResetButton);
		HelpButton = (Button) findViewById(R.id.HelpButton);
		PauseButton = (Button) findViewById(R.id.PauseButton);
		saveButton = (Button) findViewById(R.id.SaveButton);
		
		chronometer = (MyChronometer) findViewById(R.id.chronometer);

		CheckButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CheckGrid();
			}
		});
		HelpButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				HelpGame();
			}
		});
		ResetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ResetGame();
			}
		});
		PauseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PauseGame();
			}
		});
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveGame();
			}
		});

		CheckButton.setEnabled(false);//TODO make a group for this buttons to enable & desable
		HelpButton.setEnabled(false);
		ResetButton.setEnabled(false);
		PauseButton.setEnabled(false);

	}

	private void copyGrid() {
		for (int row = 0; row < SIZE; ++row) {
			for (int column = 0; column < SIZE; ++column) {
				guiText = (EditText) findViewById(getEditTextId(row, column));
				int backendCellNumber = backendsudoku.unsolved_grid[row][column];
				if (backendCellNumber != 0)// TODO make new class?
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
		if (CheckButton.getText().equals(" Check "))// TODO make icons?
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

		CheckButton.setText(text);
		HelpButton.setEnabled(!action);
		ResetButton.setEnabled(!action);
		PauseButton.setEnabled(!action);
		saveButton.setEnabled(!action);

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

		chronometer.stop();
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
	}

}
