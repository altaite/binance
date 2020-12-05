package altaite.binance.trading;

import altaite.binance.data.SymbolPair;
import altaite.format.Format;
import java.util.ArrayList;
import java.util.List;

public class Sequence {

	private SymbolPair pair;
	private T[] trades;
	private double dolarFilter;

	public Sequence(SymbolPair pair, T[] trades) {
		this.pair = pair;
		this.dolarFilter = dolarFilter;
		this.trades = trades;
		//printTrades();
		//System.out.println("");
		/*double total = 0;
		int count = 0;
		for (T t : trades) {
			double c = t.commission;
			if (t.commissionAssett.equals("BNB")) {
				total += c;
				count++;
			}
		}
		System.out.println("total commission " + total + " BNB");
		System.out.println("count: " + count);*/

		// sum and print all commisions
	}

	/*public double getProfit(double currentPrice, double toSell) {
		double[] changes = getQtys();

		// eliminate old buy-sells
		for (int i = 0; i < trades.length; i++) {
			T t = trades[i];
			if (t.isSell()) {
				eliminateSell(changes, i);
			}
		}

		// first, compute profitability of past ones
		// for each sell, delete amount from oldest buys
		// from oldest remaining buys, compute gain if sold, accumulate
		// subtract fee at transaction from money earned
		// print unaccounted money?
		return 0;
	}*/
	// TODO Search for starting rallies, fast increases over all
	// all possible windows: measure maximum deviation
	// for all much smaller windows: if growth is bigger than dev and big (1 %)
	// TODO split screen, 4 windows with subminute, minute, 15 min, day
	// keys will buy 10, 20, 40, 100
	// sell 10, all highest profit bracket
	public List<ProfitLoss> summary(Double currentPrice) {
		if (currentPrice == null) {
			System.out.println("Waiting for current price.");
			return null;
		}
		double[] changes = getQtys();
		T old = null;
		for (T t : trades) {
			if (old != null) {
				if (old.time > t.time) {
					throw new RuntimeException();
				}
			}
			old = t;
		}
		for (int i = 0; i < changes.length; i++) {
			if (changes[i] < 0) {
				eradicate(changes, i);
				assert wasEradicated(changes, i);
			}
		}
		return printSummary(changes, currentPrice);
	}

	private List<ProfitLoss> printSummary(double[] changes, double currentPrice) {
		List<ProfitLoss> pl = new ArrayList<>();
		for (int i = 0; i < changes.length; i++) {
			if (changes[i] > 0) { // not eradicated (canceled by closest sells or buys)
				pl.add(new ProfitLoss(pair, trades[i].price, changes[i], currentPrice));
			}
		}
		return pl;
	}

	// pair all nearest buy - sell, remove amount (qty) from changes on both side
	private void eradicate(double[] changes, int from) {
		if (from == 0) {
			changes[from] = 0;
			return;
		}
		for (int i = from - 1; i >= 0; i--) {
			assert changes[i] >= 0 : changes[i];
			if (changes[i] < -changes[from]) {
				changes[from] += changes[i];
				changes[i] = 0;
			} else {
				changes[i] += changes[from];
				changes[from] = 0;
				return;
			}
			if (i == 0) {
				changes[from] = 0;
			}
		}
	}

	private boolean wasEradicated(double[] changes, int from) {
		for (int i = 0; i <= from; i++) {
			if (changes[i] < 0) {
				System.err.println("eradication fail: " + from + " " + i);
				return false;
			}
		}
		return true;
	}

	// simple version: just sum in usd
	// ????
	private double[] getQtys() {
		double[] qtys = new double[trades.length];
		for (int i = 0; i < trades.length; i++) {
			qtys[i] = trades[i].qty;
		}
		return qtys;
	}

	private double eliminateSell(double[] changes, int index) {
		double remains = -changes[index]; // to pair with buys
		double totalProfit = 0;
		double sellPrice = trades[index].price;
		for (int i = 0; i < index; i++) {
			double amount;
			double buyPrice = trades[i].price;
			if (remains < changes[i]) {
				amount = remains;
				changes[i] -= amount;
				remains = 0;
			} else {
				amount = changes[i];
				remains -= amount;
				changes[i] = 0;
			}
			double profit = (sellPrice - buyPrice) * amount;
			if (profit != 0) {
				System.out.println("profit: " + profit);
			}
			totalProfit += profit;
		}
		//if (remains > 0) {
		//	throw new RuntimeException("starting amount nonzero");
		//}
		changes[index] = 0;
		System.out.println("total: " + totalProfit);
		return totalProfit;
	}

	private void printTrades() {
		for (T t : trades) {
			System.out.println(t);
		}
	}
}
