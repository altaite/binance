package altaite.time;

import java.util.Calendar;
import java.util.Date;

public class Time {

	public static String format(long timestamp) {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		Date date = cal.getTime();
		return date.toString();
	}

	public static long monthsToMilliseconds(double months) {
		return Math.round(months * 31L * 24 * 3600 * 1000);
	}

	public static long getMilliseconds(int day, int month, int year) {
		Calendar c = new Calendar.Builder().setDate(year, month, day).build();
		Date d = c.getTime();
		long milliseconds = d.getTime();
		return milliseconds;
	}
}
