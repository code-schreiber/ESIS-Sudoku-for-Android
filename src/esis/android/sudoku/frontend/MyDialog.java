package esis.android.sudoku.frontend;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.WindowManager;
import android.widget.Button;
import esis.android.sudoku.R;

public class MyDialog {
	
	/**
	 * 
	 * @param alertDialog
	 */
	public static void showCustomisedDialog(AlertDialog alertDialog, String title, String msg) {
	    alertDialog.setTitle(title);
	    alertDialog.setMessage(msg);
	    alertDialog.show();
	    
	    WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();  
	    lp.dimAmount = 0.0f;  
	    alertDialog.getWindow().setAttributes(lp);  
	    alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	    
		Button b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		checkandset(b);
		b = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		checkandset(b);
		b = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
		checkandset(b);
	}
	

	public static void showCustomisedDialog(AlertDialog d, String msg) {
		showCustomisedDialog(d, "", msg);
	}

	/**
	 * @param b
	 */
	private static void checkandset(Button b) {
		if (b != null)
			b.setBackgroundResource(R.drawable.button);
	}

}
