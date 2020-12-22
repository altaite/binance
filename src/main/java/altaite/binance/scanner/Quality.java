package altaite.binance.scanner;

import altaite.binance.data.SymbolPair;
import altaite.format.Format;

public class Quality implements Comparable<Quality> {

	private SymbolPair pair;
	private Double volumeA;
	private Double volumeB;
	private Double volumeC;
	private Double highestRise;

	public Quality(SymbolPair pair) {
		this.pair = pair;
	}

	public void setHighestRise(double d) {
		this.highestRise = d;
	}

	public void setVolumeA(double d) {
		this.volumeA = d;
	}

	public void setVolumeB(double d) {
		this.volumeB = d;
	}

	public void setVolumeC(double d) {
		this.volumeC = d;
	}

	public SymbolPair getPair() {
		return pair;
	}

	/*public Double getVolumeRatio() {
		return volumeRatio;
	}*/
	public Double getVolumeA() {
		return volumeA;
	}

	public Double getVolumeB() {
		return volumeB;
	}

	public Double getVolumeC() {
		return volumeC;
	}

	public Double getHighestRise() {
		return highestRise;
	}

	public void print() {
		System.out.println(pair + " "
			//+ Format.percentOneDigit(highestRise) + " "
			+ Format.oneDigit(volumeB / volumeA) + " "
			+ Format.oneDigit(volumeC / volumeA) + " "
			+ getUrl(pair));
	}

	private String getUrl(SymbolPair pair) {
		return "https://www.binance.com/en/trade/"
			+ pair.getA().toString().toUpperCase()
			+ "_"
			+ pair.getB().toString().toUpperCase()
			+ "?layout=pro";
	}

	public boolean isGood() {
		Double va  = getVolumeA();
		Double vb = getVolumeB();
		Double vc = getVolumeC();
		if (va  != null && vb != null && vc != null) {
			if (vb / va  < 0.7 && vc / va  > 2) {
				return true;
			} else {
				System.out.println((vb / va) + " --- " + (vc / va));
			}
		}
		return false;
	}

	@Override
	public int compareTo(Quality other) {
		if (other.highestRise == null && highestRise == null) {
			return 0;
		} else if (other.highestRise == null && highestRise != null) {
			return -1;
		} else if (other.highestRise != null && highestRise == null) {
			return 1;
		} else {
			return Double.compare(other.highestRise, highestRise);
		}
	}

}
