package altaite.analysis;

public class Sample {

	private double[] a;

	public Sample(double[] a) {
		this.a = a;
		if (a.length < 1) {
			System.err.println("Statistics empty.");
		}
	}

	public Sample(Doubles doubles) {
		this(doubles.toArray());
	}

	public double min() {
		double min = Double.POSITIVE_INFINITY;
		for (double v : a) {
			if (v < min) {
				min = v;
			}
		}
		return min;
	}

	public double max() {
		double max = Double.NEGATIVE_INFINITY;
		for (double v : a) {
			if (v > max) {
				max = v;
			}
		}
		return max;
	}

	public double average() {
		double avg = 0;
		for (double v : a) {
			avg += v;
		}
		return avg / a.length;
	}

}
