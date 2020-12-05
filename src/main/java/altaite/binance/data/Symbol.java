package altaite.binance.data;

import java.util.Objects;

public class Symbol {

	private String s;

	public Symbol(String s) {
		this.s = s.toUpperCase();
	}

	@Override
	public String toString() {
		return s;
	}

	@Override
	public boolean equals(Object o) {
		Symbol other = (Symbol) o;
		return s.equals(other.s);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + Objects.hashCode(this.s);
		return hash;
	}
}
