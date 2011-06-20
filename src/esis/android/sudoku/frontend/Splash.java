package esis.android.sudoku.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import esis.android.sudoku.R;
import esis.android.sudoku.backend.MyApp;

public class Splash extends Activity{

	boolean alreadyStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
    	final RelativeLayout splashLayout = (RelativeLayout) findViewById(R.id.SplashLayout);
        final boolean _active = true;
        //TODO Remove wait time and implement a real splash screen. Until now we wait 3 seconds to continue (or continue when tapped)
        final int _splashTime = 3*1000;
        
        
    	splashLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startNextActivity();
            }
    	});
    	
    	MyApp myapp = (MyApp) getApplicationContext();
    	myapp.checkForSavedGame();
    	
        // thread for displaying the SplashScreen
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
                    //TODO go forth?
                } finally {
                    startNextActivity();
                }
            }
        };
        splashTread.start();

    }
    
	private void startNextActivity() {
		if (!alreadyStarted){
			alreadyStarted = true;//so this isn't called again
			finish();
			Intent intent = new Intent();
			intent.setClass(Splash.this, Welcome.class);		    	
			Splash.this.startActivity(intent);
		}
	}

    
}
