package altaite.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;

public class Sample2 implements Serializable {

	private List<Pair> data = new ArrayList<>();
	private String SEPARATOR = ",";

	public Sample2() {
	}

	public Sample2(List<Pair> pairs) {
		this.data = pairs;
	}

	public Sample2(File f) {
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, SEPARATOR);
				double x = Double.parseDouble(st.nextToken());
				double y = Double.parseDouble(st.nextToken());
				add(x, y);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Sample2(Sample2 origin, Function<Pair, Boolean> filter) {
		for (Pair p : origin.getPairs()) {
			if (filter.apply(p)) {
				add(p.x, p.y);
			}
		}
	}

	public Sample2 createSortedByX() {
		List<Pair> list = new ArrayList<>();
		list.addAll(data);
		Collections.sort(list, Pair.getXComparator());
		Sample2 s = new Sample2(list);
		return s;
	}

	public Sample2 createSortedByY() {
		List<Pair> list = new ArrayList<>();
		list.addAll(data);
		Collections.sort(list, Pair.getYComparator());
		Sample2 s = new Sample2(list);
		return s;
	}

	public Sample2 subsample(int howMany) {
		Sample2 s = new Sample2();
		boolean[] sample = Sampling.sample(1, size(), howMany);
		for (int i = 0; i < size(); i++) {
			if (sample[i]) {
				s.add(get(i));
			}
		}
		return s;
	}

	public Sample2 filter(Function<Pair, Boolean> f) {
		Sample2 s = new Sample2();
		for (Pair p : data) {
			if (f.apply(p)) {
				s.add(p);
			}
		}
		return s;
	}

	public final void add(double x, double y) {
		data.add(new Pair(x, y));
	}

	public final void add(Pair p) {
		data.add(p);
	}

	public Pair get(int i) {
		return data.get(i);
	}

	public List<Pair> getPairs() {
		return data;
	}

	public int size() {
		return data.size();
	}

	public double correlation() {
		return MathUtil.correlation(getXs(), getYs());
	}

	public double[] getXs() {
		double[] xs = new double[data.size()];
		for (int i = 0; i < data.size(); i++) {
			xs[i] = data.get(i).x;
		}
		return xs;
	}

	public double[] getYs() {
		double[] xs = new double[data.size()];
		for (int i = 0; i < data.size(); i++) {
			xs[i] = data.get(i).y;
		}
		return xs;
	}

	public void toCsv(File f) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (Pair p : data) {
				bw.write(Double.toString(p.x));
				bw.write(SEPARATOR);
				bw.write(Double.toString(p.y));
				bw.write(SEPARATOR);
				bw.write("\n");
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Sample2 box(double xa, double ya, double xb, double yb) {
		Sample2 s = new Sample2();
		for (Pair p : data) {
			double x = p.x;
			double y = p.y;
			if (x < xa) {
				x = xa;
			} else if (x > xb) {
				x = xb;
			}
			if (y < ya) {
				y = ya;
			} else if (y > yb) {
				y = yb;
			}
			s.add(x, y);
		}
		return s;
	}

	public Pair getHighestY() {
		Pair highest = null;
		for (Pair p : data) {
			if (highest == null || p.y > highest.y) {
				highest = p;
			}
		}
		return highest;
	}
}
