package esis.android.sudoku.frontend;

import android.app.Activity;
import android.app.AlertDialog;
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
import esis.android.sudoku.R;
import esis.android.sudoku.backend.MyApp;


public class Welcome extends Activity{
	
	private static final String TAG = Welcome.class.getSimpleName();
	private MyApp myapp;
	private RadioGroup difficultyRadioGroup;
	private Button NewGameButton;//FIXME make the cast like in Game if it worked
	private Button LoadGameButton;
	private Button FeedbackButton;
	private Button ExitButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.welcome);
        InitRadioGroup();
        InitButtons();
    	myapp = (MyApp) getApplicationContext();
    	MyApp.setDifficulty(difficultyRadioGroup.getCheckedRadioButtonId());//Set the difficulty
    	myapp.checkForSavedGame();
    }
    
	private void InitRadioGroup() {
		
	    difficultyRadioGroup = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);

	    for(int i = 0; i < difficultyRadioGroup.getChildCount(); i++){
	    	RadioButton radioButton = (RadioButton)difficultyRadioGroup.getChildAt(i);
		    radioButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {				
			    MyApp.setDifficulty(difficultyRadioGroup.getCheckedRadioButtonId());
			}
		    });
	    }
	}

	private void InitButtons(){
	    NewGameButton = (Button) findViewById(R.id.NewGameButton);
		LoadGameButton = (Button) findViewById(R.id.LoadGameButton);
		FeedbackButton = (Button) findViewById(R.id.FeedbackButton);
		ExitButton = (Button) findViewById(R.id.ExitButton);
		
		NewGameButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
				if(MyApp.saved_game_exists){
				    deleteSavedGameWarning(v);
				}
				else{	
					CharSequence difficulty = ((RadioButton) findViewById(difficultyRadioGroup.getCheckedRadioButtonId())).getText();
				 	Toast.makeText(v.getContext(), "Creating "+difficulty+" Sudoku", Toast.LENGTH_LONG).show();
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
		    	    Toast.makeText(v.getContext(), R.string.no_game_to_load, Toast.LENGTH_SHORT).show();
		    }
		});
		FeedbackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {            	
                launchFeedbackDialog(v);
            }
		});	
		ExitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	ExitGame();
            }
		});	    
	}

	private void ExitGame(){
	    Log.d(TAG, "Exiting App");
	    this.finish();
	}
	

	private void launchFeedbackDialog(View v) {
		final SpannableString s = new SpannableString(getString(R.string.Feedback_Message));
        Linkify.addLinks(s, Linkify.EMAIL_ADDRESSES);
        
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
	
	private void deleteSavedGameWarning(View v) {
		new AlertDialog.Builder(v.getContext())
		    .setMessage("This will delete a previously saved game")
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            /* User clicked OK so start new game */  
		            MyApp.saved_game_exists = false;
		            NewGameButton.performClick();
		        }
		    })
		    .setNegativeButton("Load Instead", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            /* load the game*/
		            LoadGameButton.performClick();
		        }
		    })
		    .create().show();
	}
}