package esis.android.sudoku.frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import esis.android.sudoku.backend.FileSystemTool;
import esis.android.sudoku.backend.MyApp;

/**
 * The class Menu
 * @author Sebastian Guillen
 * 
 */

public class Menu extends Activity{

    private static final String TAG = Menu.class.getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       	setContentView(R.layout.menu);
        InitRadioGroup();
        InitButtons();
        setListenerAndReloadDifficulty();
    }

    private void setListenerAndReloadDifficulty() {
	OnSharedPreferenceChangeListener li = new OnSharedPreferenceChangeListener() {
	    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		reloadGuiDifficulty(sp, key);
	    }
	};
	SharedPreferences settings = getSharedPreferences(MyApp.PREFERED_DIFFICULTY, MODE_WORLD_READABLE);
	settings.registerOnSharedPreferenceChangeListener(li);
	reloadGuiDifficulty(settings, MyApp.PREFERED_DIFFICULTY);
    }
	
    private void reloadGuiDifficulty(SharedPreferences sp, String key) {
    	RadioGroup rg = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);
        int id = sp.getInt(key, MyApp.EASY);
        rg.check(MyApp.getDifficultyID(id));
    	Log.d(TAG, "Difficulty reloaded "+key+", now: "+id);
    }
	    
	private void InitRadioGroup() {		
	    final RadioGroup difficultyRadioGroup = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);
	    for(int i = 0; i < difficultyRadioGroup.getChildCount(); i++){
	    	RadioButton radioButton = (RadioButton)difficultyRadioGroup.getChildAt(i);
	    	//FIXME radioButton.setBackgroundResource(R.drawable.radiobutton);
		    radioButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {				
				    SharedPreferences s = getSharedPreferences(MyApp.PREFERED_DIFFICULTY, MODE_WORLD_WRITEABLE);
				    Editor e = s.edit();
				    e.putInt(MyApp.PREFERED_DIFFICULTY, MyApp.getDifficulty(difficultyRadioGroup.getCheckedRadioButtonId()));
				    e.commit();
				}
		    });
	    }
	}

    private void InitButtons() {
		Button b = (Button) findViewById(R.id.NewGameButton);
		b.setBackgroundResource(R.drawable.button);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			newGameButtonAction(v);
		    }
		});
		b = (Button) findViewById(R.id.LoadGameButton);
		b.setBackgroundResource(R.drawable.button);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			loadgameButtonAction(v);
		    }
		});
		b = (Button) findViewById(R.id.HighscoresButton);
		b.setBackgroundResource(R.drawable.button);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(Menu.this, Highscores.class);
			Menu.this.startActivity(intent);
		    }
		});
		b = (Button) findViewById(R.id.FeedbackButton);
		b.setBackgroundResource(R.drawable.button);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			launchFeedbackDialog(v);
		    }
		});
		b = (Button) findViewById(R.id.ExitButton);
		b.setBackgroundResource(R.drawable.button);
		b.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			exitApp();
		    }
		});
    }

    private void newGameButtonAction(View v) {
		if (MyApp.saved_game_exists) {
		    deleteSavedGameWarning(v);
		} else {		    
		    final Context c = v.getContext();
		    runOnUiThread(new Runnable() 
	        {                
	            public void run() {
	                //TODO Your toast code here
	            	RadioGroup rg = (RadioGroup) findViewById(R.id.DifficultyRadioGroup);
				    final CharSequence difficulty = ((RadioButton) findViewById(rg.getCheckedRadioButtonId())).getText();
					Toast.makeText(c, "Creating " + difficulty + " Sudoku", Toast.LENGTH_LONG).show();
	            }
	        });            
		    startGameActivity();
		}
    }

    private void loadgameButtonAction(View v) {
	Log.d(TAG, "Loading Game");
	if (MyApp.saved_game_exists) {
	    Toast.makeText(v.getContext(), "Loading Game", Toast.LENGTH_LONG).show();
	    startGameActivity();
	} else
	    Toast.makeText(v.getContext(), R.string.no_game_to_load, Toast.LENGTH_SHORT).show();
    }

	private void startGameActivity() {
		Intent intent = new Intent();
	    intent.setClass(Menu.this, Game.class);
	    Menu.this.startActivity(intent);
	}
	
    private void exitApp(){
	    Log.d(TAG, "Exiting App");
	    this.finish();
	}	

	private void launchFeedbackDialog(View v) {
			Resources res = getResources();
			String s = getStringformArray(res.getStringArray(R.array.Feedback_Message));
        	final SpannableString ss = new SpannableString(s);
        	Linkify.addLinks(ss, Linkify.EMAIL_ADDRESSES);
        	final Button b = (Button) findViewById(R.id.FeedbackButton);
        	final AlertDialog d = new AlertDialog.Builder(v.getContext())
                    .setIcon(R.drawable.icon)
                    .setTitle(b.getText())
                    .setMessage(ss)
                    .setPositiveButton(MyApp.getPositiveText(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	sendFeedbackEmail();                        	
                        }
                    })
                    .setNegativeButton(getString(R.string.i_dont_care), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	//Just go away.
                        }
                    }).create();
        	d.show();        	
        	d.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.button);
        	d.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.button);
        	// Make the textview clickable. Must be called after show()
        	((TextView)d.findViewById(android.R.id.message))
        	.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private String getStringformArray(String[] array) {
		String s = "";
		for (String i : array) {
			Log.d(TAG, "concadenate"+i);
			s += i;
		}
		return s;
	}

	private void deleteSavedGameWarning(View v) {
		final Button b = (Button) findViewById(R.id.NewGameButton);
		final Button lb = (Button) findViewById(R.id.LoadGameButton);
		final String date = FileSystemTool.getSavedGamesDate(this);
		AlertDialog d = new AlertDialog.Builder(v.getContext())
		    .setMessage("This will " + getString(R.string.delete_saved_game) +
		    			" from " + date)
		    .setPositiveButton(MyApp.getPositiveText(), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            /* User clicked OK so start new game and delete last saved file*/  
		            FileSystemTool.deleteSavedFile(getApplicationContext());
		            b.performClick();
		        }
		    })
		    .setNegativeButton("Load Instead", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            /* load the game*/
		            lb.performClick();
		        }
		    }).create();
		d.show();
		d.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.cell);
		d.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.cell);
	}

	/**
	 *  TODO populate email for feedback 
	 *	http://thedevelopersinfo.wordpress.com/2009/10/22/email-sending-in-android/
	 */
	private void sendFeedbackEmail() {		
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		String[] recipients = new String[]{getString(R.string.app_email), "",};
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Comments or ideas about "+getString(R.string.app_name)+":\n");
		emailIntent.setType("text/plain");
		startActivity(Intent.createChooser(emailIntent, "Send feedback"));
	}
	
}