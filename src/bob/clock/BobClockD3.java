package bob.clock;

import java.util.Calendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;

public class BobClockD3 extends AppWidgetProvider {

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.bob_clock_d3);
		remoteViews.setImageViewBitmap(R.id.clock_view, buildClock(context));
		
		ComponentName widget = new ComponentName(context, BobClockD3.class);
		appWidgetManager.updateAppWidget(widget, remoteViews);
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
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.bob_clock_d3);
		remoteViews.setImageViewBitmap(R.id.clock_view, buildClock(context));
		
		ComponentName widget = new ComponentName(context, BobClockD3.class);
		appWidgetManager.updateAppWidget(widget, remoteViews);
	}
	
	private static Bitmap buildClock(final Context context) {
		final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		final float density = displayMetrics.density;
		final SharedPreferences preferences = context.getSharedPreferences(BobClockD3Configure.PREFS_KEY, 0);
		final boolean mode24 = preferences.getBoolean(BobClockD3Configure.TWENTY_FOUR_HOUR_MODE, false);
		
		final String[] days = context.getResources().getStringArray(R.array.days);
		final String[] months = context.getResources().getStringArray(R.array.months);
		
		final int color1 = context.getResources().getColor(R.color.hour_colour);
		final int color2 = context.getResources().getColor(R.color.minutes_colour);
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
    		if (ampm == Calendar.AM) {
    			hourDigitOne = (hour < 10 ? 0 : 1);
    			hourDigitTwo = (hour < 10 ? hour : hour - 10);
    		} else {
    			hourDigitOne = ((hour < 10 && hour != 0)  ? 0 : 1);
    			hourDigitTwo = (hour == 0 ? 2 : hour % 10);
    		}
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
        
        Bitmap hourBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.digitshours);
        Bitmap minuteBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.digitsminutes);
		
        Bitmap bitmap = Bitmap.createBitmap((int)(width * density), (int)(height * density), Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
		
        paint.setColor(color1);
        if (!mode24) {
			canvas.drawText((ampm == Calendar.AM) ? "AM" : "PM", leftPadding + (int)(5 * density), fontSize, paint);
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