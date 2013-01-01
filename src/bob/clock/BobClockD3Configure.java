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
        	postResult();
        }
        
        private void createStrip(final String filename, final int colour) {
    	// immutable, can't pass it to Canvas so make 'out' bitmap
			Bitmap digitsBitmap = BitmapFactory.decodeResource(
					BobClockD3Configure.this.getResources(), R.drawable.digits);
			Bitmap out = Bitmap.createBitmap(digitsBitmap.getWidth(),
					digitsBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(out);
			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
			c.drawBitmap(
					makeDst(digitsBitmap.getWidth(), digitsBitmap.getHeight(),
							colour), 0, 0, p);
			p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			c.drawBitmap(digitsBitmap, 0, 0, p);
			try {
				FileOutputStream fos = openFileOutput(filename,
						Context.MODE_PRIVATE);
				out.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Bitmap makeDst(int w, int h, int color) {
			Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bm);
			c.drawColor(color);
			return bm;
		}
    }
}
