package altaite.binance.data.window;

public class ExperimentParameters {

	private int jump = 1; // TODO 60
	private int featureN = 200;//getALength() + getBLength() + getCLength() + getDLength();//200;
	private int targetN = 60;//10;
	public int trainSamples = 10000;
	public int testSamples = 10000;

	public void setTargetN(int n) {
		System.out.println("Setting targetN " + n);
		this.targetN = n;
	}

	public int getALength() {
		return 60 * 5 * 1;
	}

	public int getBLength() {
		return 60;
	}

	public int getCLength() {
		return 4;
	}

	public int getDLength() {
		return targetN;
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

	public double getMediumRangeMonthsBack() {
		return 1;
	}

	public String getExperimentDescription() {
		return "_" + targetN;
	}
}
