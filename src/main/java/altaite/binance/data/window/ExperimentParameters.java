package altaite.binance.data.window;

public class ExperimentParameters {

	private int jump = 10;
	private int independent = 50;
	private int target = 10;
	private boolean invertCandles = false;

	public int getJump() {
		return jump;
	}

	public int getWindowLength() {
		return independent + target;
	}

	public int getIndependentWindowLength() {
		return target;
	}

	public int getTargetWindowLength() {
		return target;
	}

	public double getMonthsBack() {
		return 500;
	}
}
