package altaite.format;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Format {

	public static String digits(double d, int digits, int width) {
		return String.format("%" + width + "." + digits + "f", d);
	}

	public static String date(long stamp) {
		Date date = new Date(stamp);
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String dateText = df2.format(date);
		return dateText;
	}

	public static void main(String[] args) {
		System.out.println(digits(111.1234, 1, 5));
	}

}
