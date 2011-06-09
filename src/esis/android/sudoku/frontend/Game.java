package esis.android.sudoku.frontend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import esis.android.sudoku.backend.BackendSudoku;
import esis.android.sudoku.backend.MyApp;
import esis.android.sudoku.R;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class Game extends Activity {
	private static final String TAG = Game.class.getSimpleName();
	private EditText guiText;
  	private final int SIZE = BackendSudoku.SIZE;
	private BackendSudoku backendsudoku;
	
	private Button CheckButton;
	private Button ResetButton;
	private Button HelpButton;
	private Button PauseButton;
	private Button saveButton;
//	private Chronometer chrono;
//	private String timepaused;//FIXME
//	private ProgressDialog dialog;
	
	/** Called when the activity is first created. */
	@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.game);
            
//            chrono = (Chronometer) findViewById(R.id.chronometer);        
    
            InitButtons(); 
    
    		NewGame();
        }
    	
	private void NewGame(){
		MyApp myapp = (MyApp) getApplication();
		backendsudoku = new BackendSudoku();
		if (!MyApp.saved_game_exists){
			Log.d(TAG, "Creating grid..............");
			backendsudoku.create_game(myapp.getdifficulty());
			Log.d(TAG, "Grid Created...............");
	
			if(+myapp.getdifficulty()!=1 && +myapp.getdifficulty()!=2 && +myapp.getdifficulty()!=3)
				Log.e(TAG, "Difficulty not set.");
			Log.d(TAG, "New Game Called with difficulty "+myapp.getdifficulty());
	
			ResetGame();
		}
		else{//Load previously saved game
			loadGame();//copy user grid to GUI
		}
		
    	CheckButton.setEnabled(true);
    	HelpButton.setEnabled(true);
    	ResetButton.setEnabled(true);
    	PauseButton.setEnabled(true);
    }

        private void PauseGame()
    	{
    		boolean action;
    		final View layout = findViewById(R.id.SudokuGridLayout);
    		if(layout.getVisibility() == View.VISIBLE){    			
    			Log.d(TAG, "Pausing Game");				
    			action = false;//pause
//timepaused = chrono.getText().toString();
//chrono.stop();
    			PauseButton.setText("Resume");
    			layout.setVisibility(View.GONE);//INVISIBLE
    		}
    		else{
    			Log.d(TAG, "Resuming Game");
    			action = true;//Resume
//chrono.setBase(timepaused);
//chrono.start();
    			PauseButton.setText("Pause");    		
    			layout.setVisibility(View.VISIBLE);//VISIBLE
    		}
    			  
    		CheckButton.setEnabled(action);
    		HelpButton.setEnabled(action);
    		ResetButton.setEnabled(action);

    	}

	private void ResetGame(){
	    	Log.d(TAG, "Reset Game Called");	
        	copyGrid();
//chrono.setBase(0L);//FIXME Reset chronometer
//chrono.run();
	}

	private void HelpGame() {
	    if(sudokuIsComplete()){
		Toast.makeText(this, R.string.no_help_needed, Toast.LENGTH_SHORT).show();
	    	return;
	    }
	    //Look for an empty place, a better implementation would know which places are empty, and would take one randomly	    
	    Random rand = new Random();
	    int row;
	    int column;
	    while(true){
		    row = rand.nextInt(SIZE);
		    column = rand.nextInt(SIZE);
		    guiText = (EditText) findViewById(getEditTextId(row,column));	   
		    if(guiText.getText().toString().equals("")){
			    guiText.setText(Integer.toString(backendsudoku.solved_grid[row][column]));
			    return;
		    }
	    }
	}			

	private void saveGame() {

	    		Toast.makeText(this, "Saving Game", Toast.LENGTH_SHORT).show();

			try {

				FileOutputStream fos;//XXX maybe do it global in myapp


				fos = openFileOutput(MyApp.SUDOKU_SAVED_FILE, Context.MODE_PRIVATE);
				MyApp.dos = new DataOutputStream(fos);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int row = 0; row < SIZE; ++row)
        	    		for(int column = 0; column < SIZE; ++column){
        	    			       	        		        	    		    
        	        		try {
        	        		
        	        		//Write cell from solved
					MyApp.dos.writeByte(backendsudoku.solved_grid[row][column]);
					
					//Write cell from unsolved
					MyApp.dos.writeByte(backendsudoku.unsolved_grid[row][column]);
					
					//Write cells entered from user

    					guiText = (EditText) findViewById(getEditTextId(row,column));
    					if (guiText.isFocusable() && guiText.isEnabled() && !guiText.getText().toString().equals(""))




    	        		    		MyApp.dos.writeByte(Integer.parseInt(guiText.getText().toString()));
    					else
		        			MyApp.dos.writeByte(0);//Cell was either empty or given






	    		    
	        		}catch (IOException e) {
	    				// TODO Auto-generated catch block
        	    				e.printStackTrace();
        	    			}


        	    		}

	    	
			try {
				MyApp.dos.close();//closes fos also

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			MyApp.saved_game_exists = true;//set flag to load this saved game the next time a game starts
	}

	private void loadGame() {

		FileInputStream fis = null;
		int[][] user_entered_numbers = new int[SIZE][SIZE];

		try {
			fis = openFileInput(MyApp.SUDOKU_SAVED_FILE);


		} catch (FileNotFoundException e) {
    	    Toast.makeText(this, "No game to load", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		for(int row = 0; row < SIZE; ++row)
        		for(int column = 0; column < SIZE; ++column){
        		    try {
    	        		//Read cell from solved
            		    	backendsudoku.solved_grid[row][column] = fis.read();    				
    				//Read cell from unsolved
            		    	backendsudoku.unsolved_grid[row][column] = fis.read();            		    	
    				//Read cells entered from user
            		    	user_entered_numbers[row][column] = fis.read();
    				 













        		    }catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
        	    	    }
        		}



    	
		try {
			fis.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		copyGrid();//write the unsolved_grid in the GUI		
		for(int row = 0; row < SIZE; ++row)//write user entered numbers in the GUI
        		for(int column = 0; column < SIZE; ++column)
                		if (user_entered_numbers[row][column] != 0){
                		    	guiText = (EditText) findViewById(getEditTextId(row,column));
                		 	guiText.setText(Integer.toString(user_entered_numbers[row][column]));
                		}   
	}	
	
	private void InitButtons() {
		
		CheckButton = (Button) findViewById(R.id.CheckButton);
		ResetButton = (Button) findViewById(R.id.ResetButton);
		HelpButton = (Button) findViewById(R.id.HelpButton);
		PauseButton = (Button) findViewById(R.id.PauseButton);
		saveButton = (Button) findViewById(R.id.SaveButton);
		
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
	
	CheckButton.setEnabled(false);
	HelpButton.setEnabled(false); 
	ResetButton.setEnabled(false);
	PauseButton.setEnabled(false);
		
	}

	private void copyGrid(){//TODO BIIIIG idea: make this function also help the loading of a game! (parameter: backendsudoku.unsolved_grid or the one loaded!)
    	Log.d(TAG, "CopyGrid Called");
    	for(int row = 0; row < SIZE; ++row){
    		for(int column = 0; column < SIZE; ++column){
		        guiText = (EditText) findViewById(getEditTextId(row,column));
			
		        if(backendsudoku.unsolved_grid[row][column]!=0){
	    	        	guiText.setEnabled(false);
	    	        	guiText.setFocusable(false);
	    	        	guiText.setText(Integer.toString(backendsudoku.unsolved_grid[row][column]));
	    	        	guiText.setTextColor(getResources().getColor(color.primary_text_dark));
				}
				else{
				    guiText.setText("");
				    guiText.setEnabled(true);
				    guiText.setFocusable(true);
				    guiText.setTextColor(getResources().getColor(R.color.solid_black));
				}
    		}
    	}
    }
	
	private int getEditTextId(int row, int column) {
		TableLayout tl = (TableLayout) findViewById(R.id.SudokuGridLayout);
		return ((TableRow) tl.getChildAt(row)).getChildAt(column).getId();
	}

	private void CheckGrid(){

	boolean action = true;//Unchecking
	if(CheckButton.getText().equals(" Check "))
		action = false;//Checking	//FIXME inverse logic duh!
		
	check(action);

	
	if(sudokuIsComplete())
		if(sudokuIsCorrect())
			gameWon();
		else
			keepPlaying(action);
}
	
	private void check(boolean action){
		Log.d(TAG, "check called with action "+action);
		String text;
		
		if (action == false)
			text = "Uncheck";
		else
			text = " Check ";

    	CheckButton.setText(text);
    	HelpButton.setEnabled(action);
    	ResetButton.setEnabled(action);
    	PauseButton.setEnabled(action);

    	for(int row = 0; row < SIZE; ++row)
    	    for(int column = 0; column < SIZE; ++column){
        		guiText = (EditText) findViewById(getEditTextId(row,column));
        		if (guiText.isEnabled()){
        			//guiText.setFocusable(action);//TODO BUG but no ideas
        			if (!guiText.getText().toString().equals("")) {        		
	            		if (action == true)
	            		    guiText.setTextColor(getResources().getColor(R.color.solid_black));//TODO make cell normal again (black?)
	            		else
	            		    markMistakes(row, column);
        			}
        		}
    	    }
	}

	private void markMistakes(int row, int column) {
	    if (guiText.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
	    	guiText.setTextColor(getResources().getColor(R.color.solid_blue));//good //TODO IDEA: only mark errors.
	    else
	    	guiText.setTextColor(getResources().getColor(R.color.solid_red));//make red
	}

	private boolean sudokuIsComplete(){
	    for(int row = 0; row < SIZE; ++row)
		for(int column = 0; column < SIZE; ++column){
		    guiText = (EditText) findViewById(getEditTextId(row,column));
		    	if(guiText.getText().toString().equals(""))
		    	    return false;
        	}
        	return true;
	}

	private boolean sudokuIsCorrect(){
	for(int row = 0; row < SIZE; ++row)
		for(int column = 0; column < SIZE; ++column){
				guiText = (EditText) findViewById(getEditTextId(row,column));
				if(!guiText.getText().toString().equals(Integer.toString(backendsudoku.solved_grid[row][column])))
				    return false;
	}
	return true;
}

	private void keepPlaying(boolean action){
		if (!action)
			Toast.makeText(this, R.string.keepPlayingText, Toast.LENGTH_LONG).show();
	}

	private void gameWon(){
	
//chrono.stop();
	    MyApp.saved_game_exists = false;//no chance to load an already won game
       	check(true);//uncheck the game in background  

	    new AlertDialog.Builder(this)
	    	.setIcon(R.drawable.icon)
            .setTitle("Congrats, You Won!")
            .setMessage("Tap OK if you want to play again or Menu to exit the game")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK so do some stuff */ 
                	NewGame();            		
                }
            })
            .setNegativeButton("Menu", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked Cancel so do some stuff */
                	Game.this.finish();
                }
            })
            .create().show();
	}
	
	
}
	
/*	
void windowInit(){

mainwidget->setName("Sudoku Spiel - SEB4");
mainwidget->name("Sudoku Software Project - WS 2010/2011");
mainwidget->setWindowIcon(QIcon("icon.png"));
mainwidget->setStatusTip(QApplication::translate("MainWidget", "ESIS - Sudoku Game", 0, QApplication::UnicodeUTF8));
mainwidget->setWindowTitle("ESIS - Sudoku Game");
    
QFrame* frame = new QFrame(mainwidget);
frame->setObjectName(QString::fromUtf8("BG"));
frame->setGeometry(QRect(6, 26, 415, 415));
frame->setStyleSheet("background-color:#ccfaf1");
}
*/        
