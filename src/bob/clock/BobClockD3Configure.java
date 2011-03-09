package bob.clock;

import java.io.FileOutputStream;

import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;

/**
 * @author bob
 *
 */
public class BobClockD3Configure extends PreferenceActivity {

	static final String PREFS_KEY = "bob.clock.BobClockD3";
	static final String HOURS_FILE = "hours.png";
	static final String MINUTES_FILE = "minutes.png";
	static final String HOURS_COLOUR_KEY = "hours";
	static final String MINUTES_COLOUR_KEY = "minutes";
	
	protected int widgetId;
	protected ProgressDialog dialog;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(PREFS_KEY);
		addPreferencesFromResource(R.xml.settings);
		setContentView(R.layout.bob_clock_d3_configure);
		
		((Button) findViewById(R.id.configure_confirm_button)).setOnClickListener(confirmClickListener);
		((Button) findViewById(R.id.configure_cancel_button)).setOnClickListener(cancelClickListener);
        
        Bundle extras = getIntent().getExtras();
        widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
	}
	
	protected void postResult() {
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    	BobClockD3.updateAppWidget(this, appWidgetManager);
    	
    	Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
	
	protected void generateImageStrips() {
		dialog = ProgressDialog.show(this, "",
			"Generating image strips ...", true);
		new ColourStripTask().execute((Void[])null);
	}
	
	
	
	View.OnClickListener confirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	generateImageStrips();
        	
        }
    };
    
    View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
	        finish();
        }
    };
    
    protected class ColourStripTask extends AsyncTask<Void, Void, Void> {
    	
        @Override
        protected Void doInBackground(Void ...voids) {
        	final SharedPreferences preferences = BobClockD3Configure.this.getSharedPreferences(PREFS_KEY, 0);
    		final int colourHours = preferences.getInt(HOURS_COLOUR_KEY, 0xbbbdbdbd);
    		final int colourMinutes = preferences.getInt(MINUTES_COLOUR_KEY, 0xeecf6f40);
    		
        	createStrip(HOURS_FILE, colourHours);
    		createStrip(MINUTES_FILE, colourMinutes);
    		
    		return null;
        }

        @Override
        protected void onPostExecute(Void _void) {
        	dialog.dismiss();
        	postResult();
        }
        
        private void createStrip(final String filename, final int colour) {
    		Bitmap digitsBitmap = BitmapFactory.decodeResource(BobClockD3Configure.this.getResources(), R.drawable.digits);
    		int[] pixels = new int[digitsBitmap.getWidth() * digitsBitmap.getHeight()];
            digitsBitmap.getPixels(pixels, 0, digitsBitmap.getWidth(), 0, 0, digitsBitmap.getWidth(), digitsBitmap.getHeight());
            
            int colourRed = (colour & 255);
            int colourGreen = (colour >> 8) & 255;
            int colourBlue = (colour >> 16) & 255;
    		double colourAlpha = ((colour >> 24) & 255) / 255.0;
            
            for (int i=0; i < pixels.length; ++i) {
            	int p = (((pixels[i] >> 24) & 255) == 0 ? 0 : (int)(((pixels[i] >> 24) & 255) * colourAlpha)) << 24 | 
            													(colourBlue << 16) | 
            													(colourGreen << 8) | 
            													(colourRed);
            	pixels[i] = p;
            }
    		
            Bitmap colouredBitmap = Bitmap.createBitmap(pixels, digitsBitmap.getWidth(), 
            						digitsBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            
            try {
    	        FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
                colouredBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
    	     } catch (Exception e) {
                e.printStackTrace();
    	     }
    	}
    }
}
