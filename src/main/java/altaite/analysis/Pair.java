package altaite.analysis;

import java.io.Serializable;
import java.util.Comparator;

public class Pair implements Comparable<Pair>, Serializable {

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
		return (Pair p1, Pair p2) -> Double.compare(p1.x, p2.x);
	}

	public static Comparator<Pair> getYComparator() {
		return (Pair p1, Pair p2) -> Double.compare(p1.y, p2.y);
	}
}
