package esis.sudoku.android.frontend;

import esis.sudoku.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class Splash extends Activity{
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.splash);
        //setProgressBarIndeterminateVisibility(mToggleIndeterminate);        
    }
}
