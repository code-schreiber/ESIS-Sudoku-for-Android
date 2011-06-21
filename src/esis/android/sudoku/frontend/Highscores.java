package esis.android.sudoku.frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.MyApp;

/**
 * @author Sebastian Guillen
 *
 */
public class Highscores extends Activity {

    private static final String TAG = Highscores.class.getSimpleName();
    private static final String Easy_key = "Easy";
    private static final String Medium_key = "Medium";
    private static final String Hard_key = "Hard";
    /** Value to return if preference does not exist. */
    public static final String defValue = "none";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.highscores);
	InitButtons();
	restorePreferences();  
    }
    
    private void InitButtons() {
	Button button =	(Button)findViewById(R.id.ResetHighscoresButton);
	button.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		resetHighscoresWarning(v);
	    }
	});
    }

    private void resetHighscores() {
        SharedPreferences settings = getPreferences(MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Easy_key, defValue);
        editor.putString(Medium_key, defValue);
        editor.putString(Hard_key, defValue);
        editor.commit();
    }
    
    private void updateGuiHighscore(String key, String value) {
        int fromWhom = 0;
        if (key == Easy_key)
        	fromWhom = R.id.Highscore_easy_view;
        else if (key == Medium_key)
        	fromWhom = R.id.Highscore_medium_view;
        else if (key == Hard_key)
        	fromWhom = R.id.Highscore_hard_view;
    	
	String text = String.format(getString(fromWhom), key, value);//BUG: returning false!
	Log.d(TAG, text);
	Log.d(TAG, getString(fromWhom));
	Log.d(TAG, key);
	Log.d(TAG, value);
	
	((TextView)findViewById(fromWhom)).setText(text);//TODO does this errase the format?
    }

    private void resetHighscoresWarning(final View v) {
	new AlertDialog.Builder(v.getContext())
		.setMessage("Are you shure you want to reset all Highscores?")
		.setPositiveButton("Yeap", 
		new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			Toast.makeText(v.getContext(), "Highscores Reseted", Toast.LENGTH_SHORT).show();
			resetHighscores();
		    }
		})
		.setNegativeButton("mmh..", 
		new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			/* do nothing */
		    }
		}).create().show();
    }
    
    public class MyListener implements OnSharedPreferenceChangeListener{

		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			
		}
    }
	
    private void restorePreferences() {
    	OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
    	    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    		updateGuiHighscore(key, sharedPreferences.getString(key, Highscores.defValue));
    	    }
    	};
    	SharedPreferences settings = getSharedPreferences(MyApp.HIGHSCORES_FILE, MODE_WORLD_READABLE);
    	settings.registerOnSharedPreferenceChangeListener(listener);

    }

    
}