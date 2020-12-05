package altaite.format;

public class Format {

	public static String digits(double d, int digits, int width) {
		return String.format("%" + width + "." + digits + "f", d);
	}

	public static void main(String[] args) {
		System.out.println(digits(111.1234, 1, 5));
	}
}
