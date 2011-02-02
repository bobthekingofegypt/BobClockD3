/*
 * Copyright (c) Rapid Mobile Media Ltd 2004 - 2010
 * (see http://www.rapid-mobile.com/eula.txt)
 */
package bob.clock;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * @author bob
 *
 */
public class BobClockD3Service extends Service {
	
	private BroadcastReceiver broadcastReceiver;
	
    @Override
    public void onCreate() {
	    broadcastReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	        	BobClockD3.updateAppWidget(context, appWidgetManager);
	        }
	    };
		
	    IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
	    registerReceiver(broadcastReceiver, intentFilter);
	}

    @Override
    public IBinder onBind(Intent intent) {
	    return null;
    }
    
    @Override
    public void onDestroy() {
    	unregisterReceiver(broadcastReceiver);
    }
    
    
}
