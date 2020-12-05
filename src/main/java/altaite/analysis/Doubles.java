package altaite.analysis;

import java.util.ArrayList;
import java.util.List;

public class Doubles {

	private List<Double> doubles = new ArrayList<>();

	public void add(double d) {
		doubles.add(d);
	}

	public double[] toArray() {
		double[] a = new double[doubles.size()];
		for (int i = 0; i < a.length; i++) {
			a[i] = doubles.get(i);
		}
		return a;
	}
}
