package esis.android.sudoku.frontend;

import java.io.DataInputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.FileSystemTool;
import esis.android.sudoku.backend.MyApp;

/**
 * @author Sebastian Guillen
 * TODO Remove wait time and implement a real splash screen. Until now we wait some seconds to continue (or till tapped)
 */

public class Splash extends Activity{

    	private static final String TAG = Splash.class.getSimpleName();
    	//FIXME private static boolean alreadyStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        appendVersion();

    	RelativeLayout splashLayout = 
    	    (RelativeLayout) findViewById(R.id.SplashLayout);    	

        //TODO Eula.showEulaRequireAcceptance(this);

    	setLayoutListener(splashLayout);
    	checkForSavedGame();
    	
        final boolean _active = true;
        final int _splashTimeinSeconds = 1; 
        launchThread(_active, _splashTimeinSeconds);
    }

    private void appendVersion() {
	String versionCode = "";
	try {
	    versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
	} catch (NameNotFoundException e) {
	    Log.e(TAG, e.getMessage());
	}        
	if (versionCode != "")
	    versionCode = "version " + versionCode + " | ";
	
	TextView tv = (TextView) findViewById(R.id.SplashText);
        tv.setText(versionCode + tv.getText());
    }
    
	@Override
	public void onBackPressed() {
		//FIXME alreadyStarted = true;
		super.onBackPressed();
	}

	/* thread for displaying the SplashScreen */
    private void launchThread(final boolean _active, final int _splashTime) {
	Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime*1000)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {
                    Log.d(TAG, "Interupted: "+ e.getMessage());
                } finally {
                    startNextActivity();
                }
            }
        };
        splashTread.start();
    }

    private void setLayoutListener(RelativeLayout splashLayout) {
	splashLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startNextActivity();
            }
    	});
    }
    
	private void startNextActivity() {
		//FIXME if (!alreadyStarted){
		//FIXME alreadyStarted = true;//so this isn't called again
			finish();
			Intent intent = new Intent();
			intent.setClass(Splash.this, Menu.class);
			Splash.this.startActivity(intent);
			//FIXME }
	}
        
	public void checkForSavedGame() {
        	MyApp.saved_game_exists = true;
        	DataInputStream dis = FileSystemTool.openFileToLoad(getApplicationContext());
        	if (dis != null){
	        	int readedByte = FileSystemTool.readBytes(dis);
	        	if(readedByte < 1)
	        	    MyApp.saved_game_exists = false;
	        	FileSystemTool.closeFis(dis);
        	}
        	Log.d(TAG, "Saved game found: "+ MyApp.saved_game_exists);
	}


}
