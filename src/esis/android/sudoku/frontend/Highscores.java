package esis.android.sudoku.frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.MyApp;

/**
 * @author Sebastian Guillen
 * 
 */

public class Highscores extends Activity {

    private static final String TAG = Highscores.class.getSimpleName();
    private static String Easy_key = "";
    private static String Medium_key = "";
    private static String Hard_key = "";
    /** Value to return if preference does not exist. */
    public static final String defValue = "none";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.highscores);
		InitButtons();
		updateGui();
		setListener();
    }

	private void InitButtons() {
		Button button =	(Button)findViewById(R.id.ResetHighscoresButton);
		button.setBackgroundResource(R.drawable.cell);
		button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	showWarning(v);
		    }
		});
		button = (Button)findViewById(R.id.ToggleHelpButton);
		//FIXME ((ToggleButton)button).setBackgroundResource(R.drawable.radiobutton);
    	SharedPreferences sp = getSharedPreferences(MyApp.HELP_ACTIVATED, MODE_WORLD_READABLE);
    	((ToggleButton)button).setChecked(sp.getBoolean(MyApp.HELP_ACTIVATED, false));
		button.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	SharedPreferences sp = getSharedPreferences(MyApp.HELP_ACTIVATED, MODE_WORLD_WRITEABLE);
		    	Editor e = sp.edit();
		    	e.putBoolean(MyApp.HELP_ACTIVATED, ((ToggleButton)v).isChecked());
		    	e.commit();
		    }
		});		
	}

	private void setListener() {
		OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
		    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		    	updateGuiHighscore(key);
		    }
		};
		SharedPreferences settings = getSharedPreferences(MyApp.HIGHSCORES, MODE_WORLD_READABLE);
		settings.registerOnSharedPreferenceChangeListener(listener);
	}

	private void updateGui() {
		Easy_key = getString(R.string.Easy);
		Medium_key = getString(R.string.Medium);
		Hard_key = getString(R.string.Hard);	
		updateGuiHighscore(Easy_key);
		updateGuiHighscore(Medium_key);
		updateGuiHighscore(Hard_key);
	}
    
    private void resetHighscores() {
    	Log.d(TAG, "Reseting Highscores ");
        SharedPreferences settings = getSharedPreferences(MyApp.HIGHSCORES, MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Easy_key, defValue);
        editor.putString(Medium_key, defValue);
        editor.putString(Hard_key, defValue);
        if(!editor.commit())
        		Log.e(TAG, "Couldn't reset highscores");
    }
    
    private void updateGuiHighscore(String key) {
        int fromWhom = 0;
        if (key == Easy_key)
        	fromWhom = R.id.Highscore_easy_view;
        else if (key == Medium_key)
        	fromWhom = R.id.Highscore_medium_view;
        else if (key == Hard_key)
        	fromWhom = R.id.Highscore_hard_view;

	    SharedPreferences settings = getSharedPreferences(MyApp.HIGHSCORES, MODE_WORLD_READABLE);
	    String value = settings.getString(key, defValue);
	    String format = getString(R.string.Highscore_format);
	    String text = String.format(format, key, value);
	    ((TextView)findViewById(fromWhom)).setText(text);
	    Log.d(TAG, "Updated "+ key + " with " + text);
    }

    private void showWarning(final View v) {
		AlertDialog d = new AlertDialog.Builder(v.getContext())
			.setPositiveButton(MyApp.getPositiveText(),
			new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int whichButton) {
				Toast.makeText(v.getContext(), "Highscores Cleared", Toast.LENGTH_SHORT).show();
				resetHighscores();
			    }
			})			
			.setNegativeButton(getString(R.string.i_dont_care), 
			new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			/* do nothing */
		    }
		}).create();
		MyDialog.showCustomisedDialog(d, "All your hard work will be worth nothing");
    }

    
}