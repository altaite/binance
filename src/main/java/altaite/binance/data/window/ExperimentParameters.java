package altaite.binance.data.window;

public class ExperimentParameters {

	private int minutes = 500;
	private double independent = 0.6;
	private boolean invertCandles = false;

	public int getJump() {
		//return 10;
		return 500;
		//return (int) Math.round(minutes * target);
	}

	public int getWindowLength() {
		return minutes;
	}

	public int getIndependentWindowLength() {
		return (int) Math.round(minutes * independent);
	}

	public int getTargetWindowLength() {
		return minutes - getIndependentWindowLength();
	}

	public double getMonthsBack() {
		return 500;
	}
}
