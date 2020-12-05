package altaite.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;

public class Sample2 {

	private List<Pair> data = new ArrayList<>();
	private String SEPARATOR = ",";

	public Sample2() {

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

	public double areaUnderCurve() {
		sortByX();
		double area = 0;
		for (int i = 0; i < data.size() - 1; i++) {
			Pair a = data.get(i);
			Pair b = data.get(i + 1);
			area += (b.x - a.x) * (a.y + b.y) / 2;
		}
		return area;
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

	public final void add(double x, double y) {
		data.add(new Pair(x, y));
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

	public void sortByX() {
		Collections.sort(data, Pair.getXComparator());
	}

	public void sortByY() {
		Collections.sort(data, Pair.getYComparator());
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

	private List<Pair> percentile(double percentile) {
		sortByX();
		List<Pair> subset = new ArrayList<>();
		int size = (int) Math.ceil(data.size() * percentile);
		for (int i = 0; i < size; i++) {
			subset.add(data.get(i));
		}
		return subset;
	}

}
