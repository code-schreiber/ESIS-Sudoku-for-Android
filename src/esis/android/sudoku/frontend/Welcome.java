package esis.android.sudoku.frontend;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import esis.android.sudoku.backend.MyApp;
import esis.android.sudoku.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class Welcome extends Activity{
	
	private static final String TAG = Welcome.class.getSimpleName();
	private MyApp myapp;
	private RadioGroup difficultyRadioGroup;
	private Button NewGameButton;
	private Button LoadGameButton;
	private Button FeedbackButton;
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
    	
    	//TODO return to this view (see previous TODO)
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
		FeedbackButton = (Button) findViewById(R.id.FeedbackButton);
		ExitButton = (Button) findViewById(R.id.ExitButton);
		
		NewGameButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			if(MyApp.saved_game_exists){
			    new AlertDialog.Builder(v.getContext())
		            .setMessage("This will delete a previously saved game")
		            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                    /* User clicked OK so start new game */  
		                    MyApp.saved_game_exists = false;
		                    NewGameButton.performClick();
		                }
		            })
		            .setNegativeButton("Load Game Instead", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                    /* load the game*/
		                    LoadGameButton.performClick();
		                }
		            })
		            .create().show();
			}
			else{
	                    //XXX MyApp.dialog = ProgressDialog.show(Welcome.this, "Loadin'", "Please wait while creating your Sudoku...", true, true);
        		    Intent intent = new Intent();
        		    intent.setClass(Welcome.this, Game.class);		    	
        		    Welcome.this.startActivity(intent);
			}

		    }
	    });
		LoadGameButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	Log.d(TAG, "Loading Game");
		    	if(MyApp.saved_game_exists){
		    	    Toast.makeText(v.getContext(), "Loading Game", Toast.LENGTH_LONG).show();
			    	Intent intent = new Intent();
			    	intent.setClass(Welcome.this, Game.class);		    	
			    	Welcome.this.startActivity(intent);
		    	}
		    	else
		    	    Toast.makeText(v.getContext(), "No game to load", Toast.LENGTH_SHORT).show();
		    }
		});
		FeedbackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
                final SpannableString s = new SpannableString("Any comments or ideas are welcome.\nIf you want to share some piece of mind please send an Email to\nsebasguillen@gmail.com.\nWe would really apreciate it.");
                Linkify.addLinks(s, Linkify.ALL);
                
                final AlertDialog d = new AlertDialog.Builder(v.getContext())
    	    	.setIcon(R.drawable.icon)
                .setTitle(FeedbackButton.getText())
                .setMessage(s)
                .setPositiveButton("Good to know", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	//Just go away.
                    }
                }).create();
                
                d.show();
                
            	// Make the textview clickable. Must be called after show()
                ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
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