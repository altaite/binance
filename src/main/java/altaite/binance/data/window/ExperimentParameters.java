package altaite.binance.data.window;

public class ExperimentParameters {

	public int minutes = 500;
	public double target = 0.8;
	public boolean invertCandles = false;

	public int getJump() {
		//return 10;
		return 50;
		//return (int) Math.round(minutes * target);
	}
}
