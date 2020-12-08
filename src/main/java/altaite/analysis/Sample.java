package altaite.analysis;

import java.util.Arrays;

public class Sample {

	private double[] a;

	public Sample(double[] inputArray) {
		this.a = new double[inputArray.length];
		System.arraycopy(inputArray, 0, a, 0, inputArray.length);
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

	public double percentile(double value) {
		double[] b = new double[a.length];
		System.arraycopy(a, 0, b, 0, a.length);
		Arrays.sort(b);
		if (value < b[0]) {
			return 0;
		}
		for (int i = 0; i < b.length; i++) {
			if (b[i] == value) {
				return ((double) i) / b.length;
			}
			if (i < b.length - 1 && b[i] <= value && value < b[i + 1]) {
				double diff = b[i + 1] - b[i];
				double w2 = (value - b[i]) / diff;
				double w1 = (b[i + 1] - value) / diff;
				double p1 = ((double) i) / b.length;
				double p2 = ((double) (i + 1)) / b.length;
				return w1 * p1 + w2 * p2;
			}
		}
		return 1;
	}

	private static void test() {
		double[] a = {50, 4, 3, -10, 2};
		Sample sample = new Sample(a);
		for (double d = 0; d < 6; d += 0.5) {
			System.out.println(d + " " + sample.percentile(d));
		}
		System.out.println("");
		Arrays.sort(a);
		for (double d : a) {
			double p = sample.percentile(d);
			System.out.println(d + " " + p);
		}
		System.out.println();
		System.out.println(sample.percentile(-10));
		System.out.println(sample.percentile(-9));
		System.out.println(sample.percentile(1));
		System.out.println(sample.percentile(2));
	}

}
