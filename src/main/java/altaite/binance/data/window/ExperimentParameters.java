package altaite.binance.data.window;

public class ExperimentParameters {

	private int jump = 1;
	private int featureN = 100;
	private int targetN = 10;
	public int trainSamples = 2000;
	public int testSamples = 30000;
	private boolean invertCandles = false;

	public void setTargetN(int n) {
		System.out.println("Setting targetN " + n);
		this.targetN = n;
	}

	public int getJump() {
		return jump;
	}

	public int getWindowN() {
		return featureN + targetN;
	}

	public int getTargetN() {
		return targetN;
	}

	public int getFeatureN() {
		return featureN;
	}

	public double getMonthsBack() {
		return 500;
	}

	public String getExperimentDescription() {
		return "_" + targetN;
	}
}
