package esis.sudoku.android.frontend;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import esis.sudoku.android.R;
import esis.sudoku.android.backend.MyApp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class Welcome extends Activity{
	
	private static final String TAG = Welcome.class.getSimpleName();
	private MyApp myapp;
	private RadioGroup difficultyRadioGroup;
	private Button NewGameButton;
	private Button LoadGameButton;
	private Button ExitButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	//TODO (maybe) call switcher to splash view 
    	
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.welcome);
        InitRadioGroup();
        InitButtons();        
        
        //Set the difficulty
    	myapp = (MyApp) getApplicationContext();
    	setDifficulty();
    	
    	//TODO return to this view
    }
    
	private void InitRadioGroup() {
	    difficultyRadioGroup = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);
	    RadioButton EasyRadioButton = (RadioButton)findViewById(R.id.radio_easy);
	    RadioButton MediumRadioButton = (RadioButton)findViewById(R.id.radio_medium);
	    RadioButton HardRadioButton = (RadioButton)findViewById(R.id.radio_hard);
	    EasyRadioButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    setDifficulty();
		}
	    });	    
	   	MediumRadioButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    setDifficulty();
			}
		    });
	    HardRadioButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    setDifficulty();
			}
		    });	    
	}

	private void InitButtons(){
	    NewGameButton = (Button) findViewById(R.id.NewGameButton);
		LoadGameButton = (Button) findViewById(R.id.LoadGameButton);
		ExitButton = (Button) findViewById(R.id.ExitButton);
		
		NewGameButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	//XXX MyApp.dialog = ProgressDialog.show(Welcome.this, "Loadin'", "Please wait while creating your Sudoku...", true, true);
		    	Intent intent = new Intent();
		    	intent.setClass(Welcome.this, Game.class);		    	
		    	Welcome.this.startActivity(intent);
		    }
	    });
		LoadGameButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Log.d(TAG, "Loading Game");
		    	MyApp.LOAD_GAME = true;
		    	NewGameButton.performClick();
		    }
		});
		ExitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	ExitGame();
            }
		});	    
	}

	private void setDifficulty(){	    
	    switch (difficultyRadioGroup.getCheckedRadioButtonId()) {
	        case R.id.radio_easy:
	            myapp.setdifficulty(1);//Easy
	            break;
	        case R.id.radio_medium:
	            myapp.setdifficulty(2);//Medium
	            break;
	        case R.id.radio_hard:
	            myapp.setdifficulty(3);//Hard
	            break;
	        default:
	            break;
	    }
	    Log.d(TAG, "difficulty changed to "+myapp.getdifficulty());
	    
	}
	
	
	
	private void ExitGame(){
	    Log.d(TAG, "Exiting App");
	    this.finish();
	}	
}