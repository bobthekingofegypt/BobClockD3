package bob.clock;

import java.io.FileInputStream;
import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

public class BobClockD3 extends AppWidgetProvider {

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		context.startService(new Intent(context, BobClockD3Service.class));
    } 
	
	@Override
    public void onEnabled(Context context) {
		context.startService(new Intent(context, BobClockD3Service.class));
    }
	
	@Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        context.stopService(new Intent(context, BobClockD3Service.class));
    }

    @Override
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, BobClockD3Service.class));
    }
	
	static void updateAppWidget(final Context context, 
								final AppWidgetManager appWidgetManager) {
		final SharedPreferences preferences = context.getSharedPreferences(BobClockD3Configure.PREFS_KEY, 0);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.bob_clock_d3);
		Bitmap builtClock = buildClock(preferences,context);
		if (builtClock == null) {
			Log.e("BobClockD3", "Failed to create clock bitmap");
			return;
		}
		remoteViews.setImageViewBitmap(R.id.clock_view, builtClock);
		boolean launchClock = preferences.getBoolean("launchclock", false);
		if (launchClock) {
			PendingIntent pendingIntent = createPendingIntent(context);
			if (pendingIntent != null) {
				remoteViews.setOnClickPendingIntent(R.id.clock_view, pendingIntent);
			}
		}
		
		ComponentName widget = new ComponentName(context, BobClockD3.class);
		
		appWidgetManager.updateAppWidget(widget, remoteViews);
	}
	
	/*
	 * Code from http://stackoverflow.com/questions/3590955/intent-to-launch-the-clock-application-on-android
	 * by frusso
	 */
	static private PendingIntent createPendingIntent(final Context context) {
		PackageManager packageManager = context.getPackageManager();
	    Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
	    
	    String clockImpls[][] = {
	            {"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
	            {"Standard Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
	            {"Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
	            {"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"},
	            {"Samsung Galaxy Alarm Clock", "com.sec.android.app.clockpackage", "com.sec.android.app.clockpackage.ClockPackage"}
	    };

	    boolean foundClockImpl = false;

	    for(int i=0; i<clockImpls.length; i++) {
	        String packageName = clockImpls[i][1];
	        String className = clockImpls[i][2];
	        try {
	            ComponentName cn = new ComponentName(packageName, className);
	            packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
	            alarmClockIntent.setComponent(cn);
	            foundClockImpl = true;
	        } catch (NameNotFoundException e) {
	        	//no-op
	        }
	    }

	    if (foundClockImpl) {
	        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, alarmClockIntent, 0);
	        return pendingIntent;
	    }

	    return null;
	}
	
	private static Bitmap buildClock(final SharedPreferences preferences, final Context context) {
		final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		final float density = displayMetrics.density;
		
		final boolean mode24 = preferences.getBoolean("mode24", false);
		final boolean lowercase = preferences.getBoolean("lowercase", false);
		
		final String[] days = context.getResources().getStringArray(R.array.days);
		final String[] months = context.getResources().getStringArray(R.array.months);
		String am = context.getResources().getString(R.string.am);
		String pm = context.getResources().getString(R.string.pm);
		
		if (lowercase) {
			am = am.toLowerCase();
			pm = pm.toLowerCase();
			for (int i=0; i<days.length; ++i) {
				days[i] = days[i].toLowerCase();
			}
			for (int i=0; i<months.length; ++i) {
				months[i] = months[i].toLowerCase();
			}
		}
		
		final int color1 = preferences.getInt(BobClockD3Configure.HOURS_COLOUR_KEY, 0x97bdbdbd);
		final int color2 = preferences.getInt(BobClockD3Configure.MINUTES_COLOUR_KEY, 0xcccf6f40);
		
		final int fontSize = (int)(13 * density);
		
		final Calendar calendar = Calendar.getInstance();
		final int ampm = calendar.get(Calendar.AM_PM);
		final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		final int monthOfYear = calendar.get(Calendar.MONTH);
		final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		final int minute = calendar.get(Calendar.MINUTE);
		
		int hourDigitOne = 0;
		int hourDigitTwo = 0;
		if (mode24) {
			final int hour = calendar.get(Calendar.HOUR_OF_DAY);
			hourDigitOne = (hour < 10 ? 0 : hour < 20 ? 1 : 2);
			hourDigitTwo = hour % 10;
		} else {
			final int hour = calendar.get(Calendar.HOUR);
			hourDigitOne = ((hour < 10 && hour != 0)  ? 0 : 1);
			hourDigitTwo = (hour == 0 ? 2 : hour % 10);
		}
		final int minuteDigitOne = (minute < 10 ? 0 : minute / 10);
		final int minuteDigitTwo = (minute < 10 ? minute : minute % 10);
		
		final int width = 160;
		final int height = 300;
		final int numberWidth = (int)(72 * density);
		final double numberHeight = (182 / 1.5) * density; 
		final int topPadding = 0;
		final int leftPadding = 10;
		final int numberGap = 2;
		
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Align.LEFT);
        
        Bitmap hourBitmap = null;
        try {
        	FileInputStream fis = context.openFileInput(BobClockD3Configure.HOURS_FILE);
        	hourBitmap = BitmapFactory.decodeStream(fis);
        	fis.close();
        } catch (Exception e) {
        	hourBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.digits);
        }

        Bitmap minuteBitmap = null;
        try {
        	FileInputStream fis = context.openFileInput(BobClockD3Configure.MINUTES_FILE);
        	minuteBitmap = BitmapFactory.decodeStream(fis);
        	fis.close();
        } catch (Exception e) {
        	minuteBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.digits);
        }
        
        Bitmap bitmap = Bitmap.createBitmap((int)(width * density), (int)(height * density), Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
		
        paint.setColor(color1);
        if (!mode24) {
			canvas.drawText((ampm == Calendar.AM) ? am : pm, leftPadding + (int)(5 * density), fontSize, paint);
        }
		
		Rect source = new Rect();
		setRectToNumber(source, hourDigitOne, numberWidth, numberHeight); 
		Rect dest = new Rect(leftPadding, 
							 topPadding + (int)(15 * density), 
							 leftPadding + numberWidth, 
							 (int)numberHeight + topPadding + (int)(15 * density));
		canvas.drawBitmap(hourBitmap, source, dest, paint);
		
		setRectToNumber(source, hourDigitTwo, numberWidth, numberHeight); 
		setRect(dest, leftPadding + numberWidth + numberGap, 
						topPadding + (int)(15 * density), 
						leftPadding + numberWidth + numberGap + numberWidth, 
						(int)(numberHeight + (int)(15 * density)));
		canvas.drawBitmap(hourBitmap, source, dest, paint);
		
		setRectToNumber(source, minuteDigitOne, numberWidth, numberHeight);
		setRect(dest, leftPadding,
					  topPadding + (int)(90 * density),
					  leftPadding + numberWidth,
					  (int) numberHeight + topPadding + (int)(90 * density));
		canvas.drawBitmap(minuteBitmap, source, dest, paint);
		
		setRectToNumber(source, minuteDigitTwo, numberWidth, numberHeight);
		setRect(dest, leftPadding + numberWidth + numberGap, 
					  topPadding + (int)(90 * density), 
					  leftPadding + numberWidth + numberGap + numberWidth, 
					  (int)(numberHeight + topPadding + (int)(90 * density)));
		canvas.drawBitmap(minuteBitmap, source, dest, paint);
		
		canvas.drawText(days[dayOfWeek], leftPadding + (int)(9 * density), topPadding + (int)(235 * density), paint);
		canvas.drawText(months[monthOfYear] + ". " + dayOfMonth, leftPadding + (int)(9 * density), topPadding + (int)(251 * density), paint);
		
        paint.setColor(color2);
		canvas.drawLine(leftPadding + (int)(5 * density), topPadding + (int)(225 * density), leftPadding + (int)(5 * density), (int)(253 * density) + topPadding, paint);
		
		return bitmap;
	}
	
	static void setRectToNumber(final Rect rect, final int number, final int numberWidth, final double numberHeight) {
		rect.left = 0;
		rect.top = (int)(numberHeight * number);
		rect.right = numberWidth;
		rect.bottom = (int)(numberHeight * (number + 1));
	}
	
	static void setRect(final Rect rect, final int left, final int top, final int right, final int bottom) {
		rect.left = left;
		rect.top = top;
		rect.right = right;
		rect.bottom = bottom;
	}
}