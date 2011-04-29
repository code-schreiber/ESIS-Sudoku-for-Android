package esis.sudoku.android.frontend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import esis.sudoku.android.R;
import esis.sudoku.android.backend.BackendSudoku;
import esis.sudoku.android.backend.MyApp;
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
	private Button gotoMenuButton;
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
		if (!MyApp.LOAD_GAME){
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

	private void gotoMenu(){
		Toast.makeText(this, "Saving your Game", Toast.LENGTH_SHORT).show();		
		saveGame();				
	    this.finish();
	}

	private void saveGame() {


			try {
				FileOutputStream fos_numbers;
				FileOutputStream fos_cells;//XXX maybe do it global in myapp
				fos_numbers = openFileOutput(MyApp.NUMBERS_FILE, Context.MODE_PRIVATE);
				MyApp.dos_numbers = new DataOutputStream(fos_numbers);
				fos_cells = openFileOutput(MyApp.CELLTYPE_FILE, Context.MODE_PRIVATE);
				MyApp.dos_cells = new DataOutputStream(fos_cells);
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int row = 0; row < SIZE; ++row)
	    		for(int column = 0; column < SIZE; ++column){
	    			
	    		    guiText = (EditText) findViewById(getEditTextId(row,column));	      			
	        		
	    		    
	        		try {
	        			//Write the number
						MyApp.dos_numbers.writeChars(guiText.getText().toString());

		        		//Write if it was a given cell or not
		    		    if (guiText.isFocusable() && guiText.isEnabled())
							MyApp.dos_cells.writeBoolean(!MyApp.GIVEN);						
						else
		        			MyApp.dos_cells.writeBoolean(MyApp.GIVEN);  
	    		    
	        		}catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}

	        	}
	    	
			try {
				MyApp.dos_numbers.close();//closes fos also
				MyApp.dos_cells.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			MyApp.LOAD_GAME = true;//set flag to load this saved game the next time a game starts
	}

	private void loadGame() {

		FileInputStream fis_numbers = null;
		FileInputStream fis_cells = null;		
		try {
			fis_numbers = openFileInput(MyApp.NUMBERS_FILE);
			fis_cells = openFileInput(MyApp.CELLTYPE_FILE);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(int row = 0; row < SIZE; ++row)
    		for(int column = 0; column < SIZE; ++column){
    			
    		    guiText = (EditText) findViewById(getEditTextId(row,column));	      			
    			try {  		
		    	//Write the number
				guiText.setText(fis_numbers.read());

        		//Write if it was a given cell or not
    		    if (fis_cells.read() == 0){//if not given
					guiText.setFocusable(true);
					guiText.setEnabled(true);
    		    }
				//TODO do i need an else?

        		}catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}

        	}
    	
		try {
			fis_numbers.close();
			fis_cells.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	private void InitButtons() {
		
		CheckButton = (Button) findViewById(R.id.CheckButton);
		ResetButton = (Button) findViewById(R.id.ResetButton);
		HelpButton = (Button) findViewById(R.id.HelpButton);
		PauseButton = (Button) findViewById(R.id.PauseButton);
		gotoMenuButton = (Button) findViewById(R.id.gotoMenuButton);
		
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
		gotoMenuButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
	    	gotoMenu();
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

	if(CheckButton.getText().equals(" Check "))
		check(false);//Checking	
	else		
		check(true);//Unchecking
	
	if(sudokuIsComplete())
		if(sudokuIsCorrect())
			gameWon();
		else
			keepPlaying();
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
        			//guiText.setFocusable(action);
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
	    	guiText.setTextColor(getResources().getColor(R.color.solid_green));//make green
	    else
	    	guiText.setTextColor(getResources().getColor(R.color.solid_red));//make red
	}

	private boolean sudokuIsComplete(){
	    for(int row = 0; row < SIZE; ++row)
		for(int column = 0; column < SIZE; ++column){
		    guiText = (EditText) findViewById(getEditTextId(row,column));
		    	if(guiText.getText().toString().equals(""))//TODO does this work?
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

	private void keepPlaying(){

//	msgBox.setIconPixmap(QPixmap("icon.png")); TODO maybe TODO

		Toast.makeText(this, R.string.keepPlayingText, Toast.LENGTH_LONG).show();
	}

	private void gameWon(){
	
//chrono.stop();
	
	    new AlertDialog.Builder(this)
	    	.setIcon(R.drawable.icon)
            .setTitle("Congrats!")
            .setMessage("Tap OK if you want to play again or Menu to exit the game")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked OK so do some stuff */
                	check(true);
                	NewGame();            		
                }
            })
            .setNegativeButton("Menu", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked Cancel so do some stuff */
                	gotoMenu();
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
