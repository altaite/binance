package altaite.binance.trading;

import com.binance.api.client.domain.account.Trade;

public class T {

	public double commission;
	public String commissionAssett;
	public long time;
	public String symbol;
	public final double price;
	public final double qty;

	public String toString() {
		return price + " " + qty + " " + symbol + " " + time;
	}

	public T(double price, double qty) {
		this.price = price;
		this.qty = qty;
	}

	public T(Trade trade) {
		this.commission = Double.parseDouble(trade.getCommission());
		this.commissionAssett = trade.getCommissionAsset();
		this.time = trade.getTime();
		this.symbol = trade.getSymbol();
		this.price = Double.parseDouble(trade.getPrice());

		double q = Double.parseDouble(trade.getQty());
		if (trade.isBuyer()) {
			qty = q;
		} else {
			qty = -q;
		}
	}

	public boolean isSell() {
		return qty < 0;
	}
}
