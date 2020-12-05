package altaite.binance.trading;

import altaite.binance.data.SymbolPair;
import altaite.format.Format;

public class ProfitLoss {

	private final SymbolPair pair;
	private final double buyPrice;
	private final double qty;
	private final double sellPrice;
	// derived values
	private double gain;
	private double relativeGain;
	private double fee;

	public ProfitLoss(SymbolPair pair, double buyPrice, double qty, double sellPrice) {
		this.pair = pair;
		this.buyPrice = buyPrice;
		this.qty = qty;
		this.sellPrice = sellPrice;
		compute();
	}

	public double getRelativeGain() {
		return relativeGain;
	}
	
	public double getFee() {
		return fee;
	}

	public String getFormatted() {
		return Format.digits(relativeGain * 100, 2, 6) + " % "
			+ Format.digits(gain, 1, 5) + " $ "
			+ Format.digits(qty, 6, 15) + " "
			+ Format.digits(buyPrice, 6, 15) + " " + pair + 
			" (" + fee + ")";
	}

	private void compute() {
		double currencyInvested = buyPrice * qty; // base = second symbol in pair (dollars, etc).
		double buyFee = 0.00075 * currencyInvested;
		double currencyReturned = sellPrice * qty;
		double sellFee = 0.00075 * currencyReturned;
		gain = currencyReturned - currencyInvested - buyFee - sellFee;
		relativeGain = gain / currencyInvested;
		fee = buyFee + sellFee;
	}
}
