package altaite.analysis;

import java.util.Comparator;

public class Pair implements Comparable<Pair> {

	public final double x, y;

	public Pair(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int compareTo(Pair other) {
		return Double.compare(x, other.x);
	}

	public static Comparator<Pair> getXComparator() {
		return new Comparator<Pair>() {
			@Override
			public int compare(Pair p1, Pair p2) {
				return Double.compare(p1.x, p2.x);
			}
		};
	}

	public static Comparator<Pair> getYComparator() {
		return new Comparator<Pair>() {
			@Override
			public int compare(Pair p1, Pair p2) {
				return Double.compare(p1.y, p2.y);
			}
		};
	}
}
