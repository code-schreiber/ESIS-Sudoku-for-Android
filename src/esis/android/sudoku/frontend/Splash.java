package esis.android.sudoku.frontend;

import java.io.DataInputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.FileSystemTool;
import esis.android.sudoku.backend.MyApp;

/**
 * @author Sebastian Guillen
 * TODO Remove wait time and implement a real splash screen. Until now we wait 3 seconds to continue (or continue when tapped)
 */

public class Splash extends Activity{

    	private static final String TAG = Splash.class.getSimpleName();
	boolean alreadyStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
    	final RelativeLayout splashLayout = (RelativeLayout) findViewById(R.id.SplashLayout);
        final boolean _active = true;
        final int _splashTime = 0*1000;        
        
    	setLayoutListener(splashLayout);
    	checkForSavedGame();
        // thread for displaying the SplashScreen
        launchThread(_active, _splashTime);
    }

    private void launchThread(final boolean _active, final int _splashTime) {
	Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
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

    private void setLayoutListener(final RelativeLayout splashLayout) {
	splashLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startNextActivity();
            }
    	});
    }
    
	private void startNextActivity() {
		if (!alreadyStarted){
			alreadyStarted = true;//so this isn't called again
			finish();
			Intent intent = new Intent();
			intent.setClass(Splash.this, Menu.class);		    	
			Splash.this.startActivity(intent);
		}
	}
        

	public void checkForSavedGame() {
        	MyApp.saved_game_exists = true;
        	DataInputStream dis = FileSystemTool.openFileToLoad(getApplicationContext());
        	int readedByte = FileSystemTool.readBytes(dis);
        	if(readedByte < 1)
        	    MyApp.saved_game_exists = false;
        	FileSystemTool.closeFis(dis);
	}


}
