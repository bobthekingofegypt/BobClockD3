package bob.clock;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

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

	private final static String DATE_FORMAT_DEFAULT = "1";
	private final static String DATE_FORMAT_ALTERNATIVE = "2";
	
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
		final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEEEE~|~MMMMM~|~dd~|~a");
		final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		final float density = displayMetrics.density;
		
		final boolean mode24 = preferences.getBoolean("mode24", false);
		final boolean lowercase = preferences.getBoolean("lowercase", false);
		
		final Calendar calendar = Calendar.getInstance();
		final Date date = calendar.getTime();
		final String formattedDateString = dateFormatter.format(date);
		StringTokenizer stringTokeniser = new StringTokenizer(formattedDateString, "~|~");
		String dayString = stringTokeniser.nextToken();
		String monthString = stringTokeniser.nextToken();
		String dayOfMonthString = stringTokeniser.nextToken();
		String ampmString = stringTokeniser.nextToken();
		
		final String dateLayout = preferences.getString("datelayout", DATE_FORMAT_DEFAULT);
		String dateString = null;
		if (dateLayout.equals(DATE_FORMAT_DEFAULT)) {
			dateString = monthString + ". " + dayOfMonthString;
		} else {
			dateString = dayOfMonthString + ". " +monthString;
		}
		
		if (lowercase) {
			ampmString = ampmString.toLowerCase();
			dayString = dayString.toLowerCase();
			dateString = dateString.toLowerCase();
		} else {
			ampmString = ampmString.toUpperCase();
			dayString = dayString.toUpperCase();
			dateString = dateString.toUpperCase();
		}
		
		final int color1 = preferences.getInt(BobClockD3Configure.HOURS_COLOUR_KEY, 0x97bdbdbd);
		final int color2 = preferences.getInt(BobClockD3Configure.MINUTES_COLOUR_KEY, 0xcccf6f40);
		
		final int fontSizePreference = Integer.parseInt(preferences.getString("fontsize", "13"));
		final int fontSize = (int)(fontSizePreference * density);
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
			canvas.drawText(ampmString, leftPadding + (int)(5 * density), fontSize, paint);
        }
        
        Rect rect = new Rect();
        paint.getTextBounds(ampmString, 0, ampmString.length(), rect);
        int ampmHeight = (int)((rect.height() + 2) * density);
		
		Rect source = new Rect();
		setRectToNumber(source, hourDigitOne, numberWidth, numberHeight); 
		Rect dest = new Rect(leftPadding, 
							 topPadding + ampmHeight, 
							 leftPadding + numberWidth, 
							 (int)numberHeight + topPadding + ampmHeight);
		canvas.drawBitmap(hourBitmap, source, dest, paint);
		
		setRectToNumber(source, hourDigitTwo, numberWidth, numberHeight); 
		setRect(dest, leftPadding + numberWidth + numberGap, 
						topPadding + ampmHeight, 
						leftPadding + numberWidth + numberGap + numberWidth, 
						(int)(numberHeight +  ampmHeight));
		canvas.drawBitmap(hourBitmap, source, dest, paint);
		
		setRectToNumber(source, minuteDigitOne, numberWidth, numberHeight);
		setRect(dest, leftPadding,
					  topPadding + ampmHeight + (int)(75 * density),
					  leftPadding + numberWidth,
					  (int) numberHeight + topPadding + ampmHeight + (int)(75 * density));
		canvas.drawBitmap(minuteBitmap, source, dest, paint);
		
		setRectToNumber(source, minuteDigitTwo, numberWidth, numberHeight);
		setRect(dest, leftPadding + numberWidth + numberGap, 
					  topPadding + ampmHeight + (int)(75 * density), 
					  leftPadding + numberWidth + numberGap + numberWidth, 
					  (int)(numberHeight + topPadding + ampmHeight + (int)(75 * density)));
		canvas.drawBitmap(minuteBitmap, source, dest, paint);
		
		canvas.drawText(dayString, leftPadding + (int)(9 * density), topPadding + ampmHeight + (int)(220 * density), paint);
		canvas.drawText(dateString, leftPadding + (int)(9 * density), topPadding + ampmHeight + ampmHeight + (int)(220 * density), paint);
		
        paint.setColor(color2);
		canvas.drawLine(leftPadding + (int)(5 * density), 
						topPadding + ampmHeight + (int)(210 * density), 
						leftPadding + (int)(5 * density), 
						ampmHeight + ampmHeight + (int)(220 * density) + topPadding, 
						paint);
		
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