package esis.android.sudoku.frontend;


import java.io.DataInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.BackendSudoku;
import esis.android.sudoku.backend.FileSystemTool;
import esis.android.sudoku.backend.MyApp;
import esis.android.sudoku.backend.MyChronometer;
import esis.android.sudoku.backend.MyPopup;

/**
 * The class Game
 * @author Sebastian Guillen
 * TODO 's: 
 * reorder f()'s, consider refactoring
 * Sign for 22 years (2033)
 * Your application must be signed with a cryptographic private key whose validity period ends after 22 October 2033.
 * bug when winning toast: you have errors
 */

public class Game extends Activity {

	private static final String TAG = Game.class.getSimpleName();
	private final int SIZE = BackendSudoku.SIZE;
	private BackendSudoku backendsudoku;
	private MyPopup popup;
	private TableLayout nineButtonsLayout;
	private int triesCounter = 0;
	private int removedNrs = 0;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		initButtons();
		initCells();
	    initPopupWindow();
		NewGame();
	}	

	@Override
	public void onBackPressed() {
		tryToSaveGame(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(findViewById(R.id.SudokuGridLayout).getVisibility() != View.VISIBLE)
			PauseGame();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(findViewById(R.id.SudokuGridLayout).getVisibility() == View.VISIBLE)
			PauseGame();
		super.onSaveInstanceState(outState);
	}
	
	/**	Release focus when number is typed in (listener to all cells) */
	private void initCells() {
		final int nrOfGuilines = 2;	
		final TableLayout sudokuGridLayout = (TableLayout)findViewById(R.id.SudokuGridLayout);
		sudokuGridLayout.setBackgroundResource(R.color.lines_color);
		populateSudokuGrid(nrOfGuilines, sudokuGridLayout);
	}
	
	private void initPopupWindow() {	
		nineButtonsLayout = new TableLayout(getApplicationContext());
		nineButtonsLayout.setBackgroundResource(android.R.drawable.alert_dark_frame);
		add9ButtonstoView();	
		popup = new MyPopup(findViewById(android.R.id.content).getRootView());//get root view from current activity
		popup.setContentView(nineButtonsLayout);
	}
	/**
	 * Fills the grid with cells and lines.
	 * @param nrOfGuilines
	 * @param sudokuGridLayout
	 * @param normalParams
	 * @param horizontalLineParams
	 * @param verticaLineParams
	 */
	private void populateSudokuGrid(final int nrOfGuilines, final TableLayout sudokuGridLayout) {// TODO i don like this refactoring
		int guilinethickness = 5;	
		LinearLayout.LayoutParams normalParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1);
		LinearLayout.LayoutParams horizontalLineParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, guilinethickness);
		LinearLayout.LayoutParams verticaLineParams = new LinearLayout.LayoutParams(guilinethickness, LayoutParams.FILL_PARENT);
		//sudokuGridLayout.setWeightSum(SIZE + nrOfGuilines);//TODO works?
		for (int row = 0; row < SIZE + nrOfGuilines; row++) {
			if (indexIsCell(row)) {
				final LinearLayout oneRow = addRowToGrid(sudokuGridLayout, normalParams, row);
				for (int column = 0; column < SIZE + nrOfGuilines; ++column) {
					if (indexIsCell(column)) {
						addCellToGrid(normalParams, oneRow, column);
					} else {
						addLineToGrid(oneRow, verticaLineParams, column);
					}
				}
			} else {
				addLineToGrid(sudokuGridLayout, horizontalLineParams, row);
			}

		}
	}
	/**
	 * @param sudokuGridLayout
	 * @param normalParams
	 * @param row
	 * @return
	 */
	private LinearLayout addRowToGrid(final TableLayout sudokuGridLayout, LinearLayout.LayoutParams normalParams, int row) {
		sudokuGridLayout.addView(new LinearLayout(sudokuGridLayout.getContext()), row, normalParams);			
		final LinearLayout oneRow = (LinearLayout)sudokuGridLayout.getChildAt(row);
		//TODO oneRow.setWeightSum(SIZE + nrOfGuilines);
		return oneRow;
	}
	/**
	 * @param normalParams
	 * @param oneRow
	 * @param column
	 */
	private void addCellToGrid(LinearLayout.LayoutParams normalParams,
			final LinearLayout oneRow, int column) {
		oneRow.addView(new Button(oneRow.getContext()), column, normalParams);
		final Button guiCell = (Button) oneRow.getChildAt(column);
		guiCell.setBackgroundResource(R.drawable.cell);
		setCellListeners(guiCell);
	}
	
	/**
	 * Set listeners for all popup buttons
	 * @param parent the text we will change.
	 */
	private void setListeners(final View parent) {
		for (View  v : nineButtonsLayout.getTouchables()) {// For all buttons
			final Button b = ((Button)v);
			b.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
			    	 ((TextView)parent).setText(b.getText());
			    	 popup.dismiss();		    	 
			    }
			});
		}
	}

	/**
	 * @param guiCell
	 */
	private void setCellListeners(final Button guiCell) {
		guiCell.setOnClickListener(new OnClickListener() {				
			public void onClick(View v) {
				showPopup(v);					
			}
		});
		guiCell.setOnLongClickListener(new OnLongClickListener() {							
			public boolean onLongClick(View v) {
				if (((TextView) v).length() != 0){
					eraseCell((TextView) v);
					return true;
				}
				return false;
			}
		});
		guiCell.addTextChangedListener(new TextWatcher() {
		    public void afterTextChanged(Editable s) {
		    	guiTextChanged(guiCell);
		    }
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				 /* Nothing */}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				 /* Nothing */}
		});
	}

	/**
	 * @param parent
	 * @param lineParams
	 * @param i
	 */
	private void addLineToGrid(LinearLayout parent, LinearLayout.LayoutParams lineParams, int i) {
		int guiLineColor = getResources().getColor(R.color.lines_color);
		Button b = new Button(parent.getContext());
		parent.addView(b, i, lineParams);
		b.setBackgroundColor(guiLineColor);
		b.setFocusable(false);
	}
	
	/**
	 * TODO f() erklärung
	 * @param i
	 */
	private boolean indexIsCell(int i) {
		if(i != 3 && i != 7)
			return true;
		return false;
	}
	protected void eraseCell(TextView v) {
			v.setText("");
			Toast.makeText(this, "Erased", Toast.LENGTH_SHORT).show();//TODO change message	
	}
	
	private void showPopup(final View parent) {		
		setListeners(parent);
		int[] whereToBeShown = getXandY(parent);		
		popup.showAtLocation(parent, Gravity.NO_GRAVITY, whereToBeShown[0], whereToBeShown[1]);// Launch!
	}
	/**
	 * Get position where popup must be shown (Align centers)
	 * @param locationOfParent
	 */
	private int[] getXandY(View parent) {
		int[] locationOfParent = new int[2];
		parent.getLocationInWindow(locationOfParent);
		nineButtonsLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);//Measure before calling getMeasured___()
		int x = (int)((locationOfParent[0] + (float)parent.getMeasuredWidth()/2)-((float)nineButtonsLayout.getMeasuredWidth()/2));
		int y = (int)((locationOfParent[1] + (float)parent.getMeasuredHeight()/2)-((float)nineButtonsLayout.getMeasuredHeight()/2));
		Rect rectgle = new Rect();
		Window window = getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
		int statusBarHeight = rectgle.top;
		if(y < statusBarHeight)//Show popup under the statusbar
			y = statusBarHeight;
		
		int[] centerOfParent = new int[2];
		centerOfParent[0] = x;
		centerOfParent[1] = y;
		return centerOfParent;
	}
	/**
	 * 	Create all 9 Buttons
	 */
	private void add9ButtonstoView() {
		final int three = SIZE/3;
		Context c = nineButtonsLayout.getContext();
		for (int row=0; row<three; row++){    		
			nineButtonsLayout.addView(new LinearLayout(c),row);
	    	for (int column=0; column<three; column++){
	    		ViewGroup v = (ViewGroup) nineButtonsLayout.getChildAt(row);
	    		final Button b = new Button(v.getContext());
	    		b.setBackgroundResource(R.drawable.popupbutton);	
				b.setTextColor(Color.WHITE);
				b.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD);
	    		int text = row * three + column + 1;//Calculate 1-9 from row and column
				b.setText(String.valueOf(text));
	    		v.addView(b, column);
	    	}
		}
	}
	private void NewGame() {
		backendsudoku = new BackendSudoku();
		if (MyApp.saved_game_exists) {// Load previously saved game
			loadGame();// copy user grid to GUI
		} else {
			backendsudoku.create_game(getDifficulty());
		    removedNrs = backendsudoku.quantityRemoved;
			Log.d(TAG, "New Game Called with difficulty " + getDifficulty());
			ResetGame();
		}
		((Button) findViewById(R.id.CheckButton)).setEnabled(true);
		enableOrDisableHelpResetPause(true);
		((MyChronometer) findViewById(R.id.chronometer)).start();
	}

	private void saveGame() {		
		Toast.makeText(this, R.string.saving_game, Toast.LENGTH_SHORT).show();
		//Open file
		long base = ((MyChronometer) findViewById(R.id.chronometer)).getBase();
		FileSystemTool.openFileToSave(getApplicationContext(), base, getDifficulty(), triesCounter, removedNrs);
		int[][] guiCells = new int[SIZE][SIZE];
		copyGuiCellsToArray(guiCells);
		FileSystemTool.writeGameToFile(backendsudoku.solved_grid, backendsudoku.unsolved_grid, guiCells);
	}

	private void loadGame() {
		int[][] user_entered_numbers = new int[SIZE][SIZE];
	
		DataInputStream dis = null;
		dis = FileSystemTool.openFileToLoad(getApplicationContext());
		int loadedDifficulty = FileSystemTool.readBytes(dis);
		loadDifficulty(loadedDifficulty);
		long savedTime = FileSystemTool.getsavedTime(dis);
		int tempTries = FileSystemTool.readBytes(dis);
		//Load the deleted numbers
		removedNrs = FileSystemTool.readBytes(dis);
		loadData(dis, user_entered_numbers);
		FileSystemTool.closeFis(dis);
	
		// write the unsolved grid in the GUI
		copyGrid();		
		// write user entered numbers in the GUI
		copyUserNumbersToGui(user_entered_numbers);	
		//Load the tries after they were affected in copy
		triesCounter = tempTries;
		//Start Chronometer from saved time
		((MyChronometer) findViewById(R.id.chronometer)).setBase(savedTime);
		((MyChronometer) findViewById(R.id.chronometer)).start();
	}

	private int getDifficulty() {
	    SharedPreferences s = getSharedPreferences(MyApp.PREFERED_DIFFICULTY, MODE_WORLD_READABLE);
	    return s.getInt(MyApp.PREFERED_DIFFICULTY, MyApp.EASY);
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
		triesCounter = 0;
		((MyChronometer) findViewById(R.id.chronometer)).reset();
		((MyChronometer) findViewById(R.id.chronometer)).start();
	}

	private void help() {
		if (sudokuIsComplete())
			Toast.makeText(this, R.string.no_help_needed, Toast.LENGTH_SHORT).show();
		else
			showOneSolutionNumber();
	}
	
	/**		 
	 * Look for an empty place, a better implementation would know which 
	 * places are empty, and would take one randomly
	 */
	private void showOneSolutionNumber() {
		TableLayout sudokuGridLayout = (TableLayout) findViewById(R.id.SudokuGridLayout);
		Random rand = new Random();
		int row;
		int column;
		while (true) {//FIXME danger of infinite loop
			row = rand.nextInt(SIZE);
			column = rand.nextInt(SIZE);
			Button cellText = getCell(sudokuGridLayout, row, column);
			if (cellText.getText().toString().equals("")) {
				cellText.requestFocus();
				cellText.setText(Integer.toString(backendsudoku.solved_grid[row][column]));
				((Button) findViewById(R.id.HelpButton)).requestFocus();
				return;
			}
		}
	}

	private void copyGuiCellsToArray(int[][] guiCells) {
		TableLayout sudokuGridLayout = (TableLayout) findViewById(R.id.SudokuGridLayout);
	    for (int row = 0; row < SIZE; ++row)
	    	for (int column = 0; column < SIZE; ++column){
	    		Button cellText = getCell(sudokuGridLayout, row, column);				
	    	    if (cellText.isFocusable() && cellText.isEnabled() && !cellText.getText().toString().equals(""))
	    	    	guiCells[row][column] = Integer.parseInt(cellText.getText().toString());
	    	    else
	    	    	guiCells[row][column] = 0;
	    	}
	}

	private void loadDifficulty(int loadedDifficulty) {	    
	    SharedPreferences s = getSharedPreferences(MyApp.PREFERED_DIFFICULTY, MODE_WORLD_WRITEABLE);
	    Editor e = s.edit();
	    e.putInt(MyApp.PREFERED_DIFFICULTY, loadedDifficulty);
	    e.commit();
	}

	private void copyUserNumbersToGui(int[][] user_entered_numbers) {
		TableLayout sudokuGridLayout = (TableLayout) findViewById(R.id.SudokuGridLayout);
	    for (int row = 0; row < SIZE; ++row)
	        for (int column = 0; column < SIZE; ++column)
	    	if (user_entered_numbers[row][column] != 0) {
	    	    Button cellText = getCell(sudokuGridLayout, row, column);
	    	    setTextofCell(cellText, Integer.toString(user_entered_numbers[row][column]));
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

	private void initButtons() {    	
		Button button;

		button = (Button) findViewById(R.id.CheckButton);
		button.setBackgroundResource(R.drawable.button);
		button.setEnabled(false);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CheckGrid();
			}
		});
		button = (Button) findViewById(R.id.ResetButton);
		button.setBackgroundResource(R.drawable.button);
		button.setEnabled(false);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ResetGame();
			}
		});
		button = (Button) findViewById(R.id.PauseButton);
		button.setBackgroundResource(R.drawable.button);
		button.setEnabled(false);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PauseGame();
			}
		});
		button = (Button) findViewById(R.id.SaveButton);
		button.setBackgroundResource(R.drawable.button);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				tryToSaveGame(false);
			}
		});
    	SharedPreferences sp = getSharedPreferences(MyApp.HELP_ACTIVATED, MODE_WORLD_READABLE);
		button = (Button) findViewById(R.id.HelpButton);
    	if(sp.getBoolean(MyApp.HELP_ACTIVATED, false)){
    		button.setBackgroundResource(R.drawable.button);
    		button.setEnabled(false);
    		button.setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				help();
    			}
    		});
    	}
    	else{
    		button.setVisibility(View.GONE);
    	}

	}

	private void copyGrid() {
		TableLayout sudokuGridLayout = (TableLayout) findViewById(R.id.SudokuGridLayout);
		for (int row = 0; row < SIZE; ++row) {
			for (int column = 0; column < SIZE; ++column) {
			    Button cellText = getCell(sudokuGridLayout, row, column);
				int backendCellNumber = backendsudoku.unsolved_grid[row][column];
				if (backendCellNumber != 0)
					setGivenCell(cellText, backendCellNumber);
				else
					setTextofCell(cellText, "");
			}
		}
	}

	private void setTextofCell(Button cell, String text) {
		cell.setText(text);
		cell.setEnabled(true);
		cell.setFocusable(true);
		cell.setTextColor(getResources().getColor(R.color.solid_black));
	}
	
	private void setGivenCell(Button cell, int backendCellNumber) {
		cell.setText(Integer.toString(backendCellNumber));
		cell.setEnabled(false);
		cell.setFocusable(false);
		cell.setTextColor(getResources().getColor(color.primary_text_dark));
	}

	private Button getCell(TableLayout sudokuGrid, int row, int column) {
		row = evadeGuiLines(row);
		column = evadeGuiLines(column);
		return (Button) ((LinearLayout) sudokuGrid.getChildAt(row)).getChildAt(column);
	}
	
	/**
	 * This makes sure we don't return the dividing gui lines.
	 * It is supposed to only be called by getCellId().
	 * Info: Gui lines are: 3,7 [index 0]
	 * @param position
	 * @return the right position
	 */
	private int evadeGuiLines(int position) {
		if (position > 5)
			position +=2;
		else if (position > 2)
			position++;
		return position;
	}

	private void CheckGrid() {
		boolean checking = false;// Unchecking
		if (((Button) findViewById(R.id.CheckButton)).getText().equals("Check"))
			checking = true;// Checking
		check(checking);
	}

	private void checkIfWon() {
		if (sudokuIsComplete())
			if (sudokuIsCorrect())
				gameWon();
			else
				keepPlaying();
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

		checkUserCells(action);
	}

	private void checkUserCells(boolean action) {
		TableLayout sudokuGridLayout = (TableLayout) findViewById(R.id.SudokuGridLayout);
	    for (int row = 0; row < SIZE; ++row)
	        for (int column = 0; column < SIZE; ++column) {
	        	Button guiCell = getCell(sudokuGridLayout, row, column);
	    		if (guiCell.isFocusable()) {// if it is a user cell
	    		    guiCell.setEnabled(!action);
	    		    if (!guiCell.getText().toString().equals("")) {
		    			if (!action)
		    			    guiCell.setTextColor(getResources().getColor(R.color.solid_black));
		    			else
		    			    markMistakes(guiCell, row, column);
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
		if (!guiText.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
		    guiText.setTextColor(getResources().getColor(R.color.solid_red));// make mistakes red
	}

	private boolean sudokuIsComplete() {
		TableLayout sudokuGridLayout = (TableLayout) findViewById(R.id.SudokuGridLayout);
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
			    Button guiCell = getCell(sudokuGridLayout, row, column);
				if (guiCell.getText().toString().equals(""))
					return false;
			}
		return true;
	}

	private boolean sudokuIsCorrect() {
		TableLayout sudokuGridLayout = (TableLayout) findViewById(R.id.SudokuGridLayout);
		for (int row = 0; row < SIZE; ++row)
			for (int column = 0; column < SIZE; ++column) {
				Button guiCell = getCell(sudokuGridLayout, row, column);
				if (!guiCell.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
					return false;
			}
		return true;
	}

	private void keepPlaying() {
	    Toast.makeText(this, R.string.keepPlayingText, Toast.LENGTH_LONG).show();
	}

	private void gameWon() {
		Log.d(TAG, "Game Won");
		MyChronometer c = ((MyChronometer) findViewById(R.id.chronometer));
		c.stop();
		check(false);// uncheck the game in background
		showWonMessage();
		saveHighscore(c.getText().toString());
	}

	private void showWonMessage() {
	    AlertDialog d = new AlertDialog.Builder(this)
	    	.setIcon(R.drawable.icon)
	    	.setTitle(R.string.Won_Title)
	    	.setMessage(R.string.Won_Mesage)
	    	.setPositiveButton(MyApp.getPositiveText(), new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int whichButton) {
	    		/* User clicked OK so do some stuff */
	    		NewGame();
	    	    }
	    	}).setNegativeButton("Menu", new DialogInterface.OnClickListener() {
	    		    public void onClick(DialogInterface dialog,
	    			    int whichButton) {
	    			/* User clicked Cancel so do some stuff */
	    			Game.this.finish();
	    		    }
	    	}).setCancelable(false).create();
	    d.show();
	    setButtonsBackground(d);
	}

    private void saveHighscore(String newTime) {
    	MyApp ma = (MyApp) getApplication();
    	String difName = ma.getDifficultyString(getDifficulty());
    
    	SharedPreferences sp = getSharedPreferences(MyApp.HIGHSCORES, MODE_WORLD_WRITEABLE);
    	String oldHscore = sp.getString(difName, Highscores.defValue);
    	Log.d(TAG, "saving highscore with diff " + difName + " and time "
    		+ newTime + " and old hs " + oldHscore);
    
    	/* Save the time if its was faster than old highscore. */
    	if (oldHscore.equals(Highscores.defValue))
    	    setNewHighscore(difName, newTime, sp);
    	else {
    		String newinInt = newTime.replace(":", "");
    		int start = 5;//where to start deleting date
    		if(oldHscore.length() > 17)
    			start += 3;//3 char: ":HH"
    	    String oldinInt = oldHscore.replace(oldHscore.substring(start),"").replace(":", "");
    	    if (Integer.parseInt(newinInt) < Integer.parseInt(oldinInt))
    		setNewHighscore(difName, newTime, sp);
    	}
    }

	private void setNewHighscore(String difficulty, String time, SharedPreferences settings) {
		Toast.makeText(this, "Highscore in " + difficulty, Toast.LENGTH_LONG).show();
	    DateFormat dateFormat = new SimpleDateFormat(" - "+getString(R.string.preferred_date_format));
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(difficulty, time + dateFormat.format(new Date()));
	        if(!editor.commit())
	    		Log.e(TAG, "Couldn't set new highscore");
	}

	private void guiTextChanged(final Button guiCell) {
		if(guiCell.length() != 0){
			triesCounter++;
			if (triesCounter >= removedNrs)
				checkIfWon();
		}
		else
			triesCounter--;
		Log.d(TAG, "tried "+triesCounter+" times from "+removedNrs);
	}
	
	private void askForOverwrittingPermission(final boolean saveAndQuit) {
		String date = FileSystemTool.getSavedGamesDate(this);
		String msg = getString(R.string.delete_saved_game) + " from " + date + "?";
		if (saveAndQuit)
			msg = getString(R.string.save_this_game) + " and " + msg;
		else
			msg = msg.replace("de", "De");
		AlertDialog d = new AlertDialog.Builder(this)
		    .setMessage(msg)
		    .setPositiveButton(MyApp.getPositiveText(), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	saveGame();
		        	if(saveAndQuit)
		        		exitGame();
		        }
		    })
		    .setNegativeButton("No!", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	if(saveAndQuit)
		        		exitGame();
		        }
		    }).create();
		d.show();
		setButtonsBackground(d);
	}

	/**
	 * @param alertDialog
	 */
	private void setButtonsBackground(AlertDialog alertDialog) {
		Button b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		if (b != null)
			b.setBackgroundResource(R.drawable.button);
		b = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		if (b != null)
			b.setBackgroundResource(R.drawable.button);
		b = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
		if (b != null)
			b.setBackgroundResource(R.drawable.button);
	}

	private void tryToSaveGame(boolean saveAndQuit) {
		if (MyApp.saved_game_exists){
			if(!savedGameisExactlyCurrentGame())
				askForOverwrittingPermission(saveAndQuit);
		}			
		else{
			saveGame();
        	if(saveAndQuit)
        		exitGame();
		}
	}
	
    private boolean savedGameisExactlyCurrentGame() {
		// Implement comparing saved & gui games
		return false;
	}
    
	private void exitGame(){
	    Log.d(TAG, "Exiting Game");
	    this.finish();
	}	

}
