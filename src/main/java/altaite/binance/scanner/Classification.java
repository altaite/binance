package altaite.binance.scanner;

public class Classification {

	public final double probability;
	public boolean isHigh;
	public final double low;
	public final double high;

	public Classification(double probability, boolean isHigh, double low, double high) {
		this.probability = probability;
		this.isHigh = isHigh;
		this.low = low;
		this.high = high;
	}
}
