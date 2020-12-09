package altaite.analysis;

import java.io.Serializable;
import java.util.Comparator;

public class PairInt implements Comparable<PairInt>, Serializable {

	public final int x, y;

	public PairInt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int compareTo(PairInt other) {
		return Double.compare(x, other.x);
	}

	public static Comparator<PairInt> getXComparator() {
		return (PairInt p1, PairInt p2) -> Integer.compare(p1.x, p2.x);
	}

	public static Comparator<PairInt> getYComparator() {
		return (PairInt p1, PairInt p2) -> Integer.compare(p1.y, p2.y);
	}
}
