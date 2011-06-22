package esis.android.sudoku.frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

/**
 * @author Sebastian Guillen
 * TODO rename this class
 */

public class Welcome extends Activity{

    private static final String TAG = Welcome.class.getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.welcome);
        InitRadioGroup();
        InitButtons();
        loadDifficulty();
    }

	private void loadDifficulty() {
		RadioGroup rg = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);
        SharedPreferences s = getSharedPreferences(MyApp.PREFERED_DIFFICULTY, MODE_WORLD_READABLE);
        rg.check(MyApp.getDifficultyID(s.getInt(MyApp.PREFERED_DIFFICULTY, 1)));        
        MyApp.setDifficulty(rg.getCheckedRadioButtonId());
	}
    
	private void InitRadioGroup() {		
	    final RadioGroup difficultyRadioGroup = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);
	    for(int i = 0; i < difficultyRadioGroup.getChildCount(); i++){
	    	RadioButton radioButton = (RadioButton)difficultyRadioGroup.getChildAt(i);
		    radioButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {				
				    MyApp.setDifficulty(difficultyRadioGroup.getCheckedRadioButtonId());
			        SharedPreferences s = getSharedPreferences(MyApp.PREFERED_DIFFICULTY, MODE_WORLD_WRITEABLE);
			        Editor e = s.edit();
			        e.putInt(MyApp.PREFERED_DIFFICULTY, MyApp.getdifficulty());
			        e.commit();
				}
		    });
	    }
	}

    private void InitButtons() {
		Button b = (Button) findViewById(R.id.NewGameButton);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			newGameButtonAction(v);
		    }
		});
		b = (Button) findViewById(R.id.LoadGameButton);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			loadgameButtonAction(v);
		    }
		});
		b = (Button) findViewById(R.id.HighscoresButton);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(Welcome.this, Highscores.class);
			Welcome.this.startActivity(intent);
		    }
		});
		b = (Button) findViewById(R.id.FeedbackButton);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			launchFeedbackDialog(v);
		    }
		});
		b = (Button) findViewById(R.id.ExitButton);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			ExitGame();
		    }
		});
    }

    private void newGameButtonAction(View v) {
		if (MyApp.saved_game_exists) {
		    deleteSavedGameWarning(v);
		} else {
		    RadioGroup rg = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);
		    CharSequence difficulty = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText();
		    Toast.makeText(v.getContext(), "Creating " + difficulty + " Sudoku", Toast.LENGTH_LONG).show();
		    Intent intent = new Intent();
		    intent.setClass(Welcome.this, Game.class);
		    Welcome.this.startActivity(intent);
		}
    }

    private void loadgameButtonAction(View v) {
	Log.d(TAG, "Loading Game");
	if (MyApp.saved_game_exists) {
	    Toast.makeText(v.getContext(), "Loading Game",
		    Toast.LENGTH_LONG).show();
	    Intent intent = new Intent();
	    intent.setClass(Welcome.this, Game.class);
	    Welcome.this.startActivity(intent);
	} else
	    Toast.makeText(v.getContext(), R.string.no_game_to_load,
		    Toast.LENGTH_SHORT).show();
    }
	
    private void ExitGame(){
	    Log.d(TAG, "Exiting App");
	    this.finish();
	}	

	private void launchFeedbackDialog(View v) {
        	final SpannableString s = new SpannableString(getString(R.string.Feedback_Message));
        	Linkify.addLinks(s, Linkify.EMAIL_ADDRESSES);
        	final Button b = (Button) findViewById(R.id.FeedbackButton);
        	final AlertDialog d = new AlertDialog.Builder(v.getContext())
                    .setIcon(R.drawable.icon)
                    .setTitle(b.getText())
                    .setMessage(s)
                    .setPositiveButton("Good to know", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	//Just go away.
                        }
                    }).create();
        	d.show();
        	// Make the textview clickable. Must be called after show()
        	((TextView)d.findViewById(android.R.id.message))
        	.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private void deleteSavedGameWarning(View v) {
		final Button b = (Button) findViewById(R.id.NewGameButton);
		final Button lb = (Button) findViewById(R.id.LoadGameButton);
		new AlertDialog.Builder(v.getContext())
		    .setMessage("This will delete a previously saved game")
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            /* User clicked OK so start new game */  
		            MyApp.saved_game_exists = false;
		            b.performClick();
		        }
		    })
		    .setNegativeButton("Load Instead", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            /* load the game*/
		            lb.performClick();
		        }
		    }).create().show();
	}
	
}