package altaite.binance.data;

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
