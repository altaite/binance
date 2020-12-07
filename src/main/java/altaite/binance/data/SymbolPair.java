package altaite.binance.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SymbolPair {

	private Symbol a, b;

	public SymbolPair(Symbol a, Symbol b) {
		this.a = a;
		this.b = b;
	}

	public SymbolPair(String as, String bs) {
		this(new Symbol(as), new Symbol(bs));
	}

	public static SymbolPair create(String s, String[] currencySymbols) {
		List<String> hits = new ArrayList<>();
		String pair = s.toUpperCase();
		for (String symbol : currencySymbols) {
			if (pair.contains(symbol)) {
				hits.add(symbol);
			}
		}
		if (hits.size() != 2) {
			throw new RuntimeException(s + " " + hits.size());
		}
		String a = hits.get(0);
		String b = hits.get(1);
		if (s.equals(a + b)) {
			return new SymbolPair(a, b);
		} else if (s.equals(b + a)) {
			return new SymbolPair(b, a);
		} else {
			throw new RuntimeException(s + " " + a + " " + b);
		}
	}

	public Symbol getA() {
		return a;
	}

	public Symbol getB() {
		return b;
	}

	@Override
	public String toString() {
		return a.toString() + b.toString();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash + Objects.hashCode(this.a);
		hash = 89 * hash + Objects.hashCode(this.b);
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		SymbolPair other = (SymbolPair) o;
		return a.equals(other.a) && b.equals(other.b);
	}
}
