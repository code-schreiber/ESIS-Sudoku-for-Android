package esis.android.sudoku.frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.MyApp;

public class Highscores extends Activity {

    private static final String TAG = Highscores.class.getSimpleName();
    private MyApp myapp;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.highscores);
	InitButtons();
	myapp = (MyApp) getApplicationContext();
	myapp.checkForSavedGame();
    }

    private void InitButtons() {
	Button button =	(Button)findViewById(R.id.ResetHighscoresButton);
	button.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
		resetHighscoresWarning(v);
	    }
	});
    }

    protected void resetHighscores() {
        // TODO Auto-generated method stub xxx XXX
//TODO delete values in file and reload TODO
	String s = "none";
	reloadHighscores(s , s, s);
    }
    protected void reloadHighscores(String easyHighscore, String mediumHighscore, String hardHighscore) {
	String text = String.format(getString(R.string.Highscore_easy), easyHighscore);
	((TextView)findViewById(R.id.Highscore_easy_view)).setText(text);
	//TODO do the other 2.
    }

    private void resetHighscoresWarning(final View v) {
	new AlertDialog.Builder(v.getContext())
		.setMessage("Are you shure you want to reset all Highscores?")
		.setPositiveButton("Yeap", 
		new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			Toast.makeText(v.getContext(), "Reseting Highscores", Toast.LENGTH_LONG).show();
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
}