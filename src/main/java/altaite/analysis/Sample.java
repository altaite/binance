package altaite.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Sample {

	private double[] a;

	public Sample(double[] input) {
		this.a = new double[input.length];
		System.arraycopy(input, 0, a, 0, input.length);
		if (a.length < 1) {
			System.err.println("Statistics empty.");
		}
	}

	public <T> Sample(T[] input, Function<T, Double> f) {
		List<Double> list = new ArrayList<>();
		int nulls = 0;
		for (int i = 0; i < input.length; i++) {
			if (input[i] == null) {
				nulls++;
			} else {
				list.add(f.apply(input[i]));
			}
		}
		//System.err.println(nulls + " of sample input is null out of " + input.length);
		this.a = new double[list.size()];
		for (int i = 0; i < a.length; i++) {
			a[i] = list.get(i);
		}
	}

	public Sample(Doubles doubles) {
		this(doubles.toArray());
	}

	public Sample head(double percent) {
		int first = (int) Math.round((a.length) * percent);
		double[] head = new double[a.length - first];
		for (int i = 0; i < head.length; i++) {
			head[i] = a[first + i];
		}
		return new Sample(head);
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

	public double getValueForPercentile(double percentile) {
		double[] b = new double[a.length];
		System.arraycopy(a, 0, b, 0, a.length);
		Arrays.sort(b);
		int i = (int) Math.round(percentile * (b.length - 1));
		return b[i];
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

	public Sample box(double min, double max) {
		double[] b = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			if (a[i] < min) {
				b[i] = min;
			} else if (max < a[i]) {
				b[i] = max;
			} else {
				b[i] = a[i];
			}
		}
		return new Sample(b);
	}

	public static void main(String[] args) {
		double[] ds = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		Sample sample = new Sample(ds);
		for (double p = 0; p <= 1.01; p += 0.1) {
			System.out.println(" " + p);
			for (double d : sample.head(p).a) {
				System.out.print(d + " ");
			}
			System.out.println("");
		}
	}
}
