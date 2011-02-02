package bob.clock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author bob
 *
 */
public class BobClockD3Configure extends Activity {
	
	static final String PREFS_KEY = "bob.clock.BobClockD3";
	static final String TWENTY_FOUR_HOUR_MODE = "24 hour mode";
	static final String SHOW_CLOCK = "show clock";
	
	protected int widgetId;
	
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bob_clock_d3_configure);
        
        ((Button) findViewById(R.id.confirm_button_12)).setOnClickListener(confirm12ClickListener);
        ((Button) findViewById(R.id.confirm_button_24)).setOnClickListener(confirm24ClickListener);
        
        Bundle extras = getIntent().getExtras();
        widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
    }
    
    protected void postResult() {
    	Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
    
    protected void set24HourMode(final boolean mode24) {
    	SharedPreferences.Editor prefs = getSharedPreferences(PREFS_KEY, 0).edit();
    	prefs.putBoolean(TWENTY_FOUR_HOUR_MODE, mode24);
    	prefs.commit();
    }
    
    View.OnClickListener confirm12ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	set24HourMode(false);
        	postResult();
        }
    };
    
    View.OnClickListener confirm24ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	set24HourMode(true);
        	postResult();
        }
    };
}
