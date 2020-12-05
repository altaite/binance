package altaite.binance.app;

import altaite.binance.Account;
import altaite.binance.current.Market;
import altaite.binance.current.Markets;
import altaite.binance.data.Symbol;
import altaite.binance.data.SymbolPair;
import altaite.binance.trading.ProfitLoss;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import java.util.ArrayList;
import java.util.List;

public class Monitor {

	private final Markets markets;
	private final Account account;
	private final BinanceApiRestClient rest;

	long since = System.currentTimeMillis() - 30L * 24 * 3600 * 1000;

	public Monitor() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		rest = factory.newRestClient();
		markets = new Markets(rest);
		account = new Account(markets);
	}

	// sound alert on the last buy - sign cheaper
	// new height from zero or last alert
	// copy async from Alert
	// TODO continuous checking of price mode for single pair
	// print all at once, refresh faster
	// refresh the trades once per minute or so too, but not now (db first)
	private void sales() {
		//Symbol selected = new Symbol("btc");
		Symbol selected = new Symbol("btc");
		String[] assets = {"btc", "eth", "bnb", "xmr", "xrp", "dash"};
		String[] currencies = {"usdt"};
		boolean r = true;
		List<ProfitLoss> allPls = new ArrayList<>();

		account.printAssets();
		System.out.println(new Symbol("btc").equals(new Symbol("btc")));
		
		// separate stream and active one time info?
		// !!!!!!!!!!!!! refresh prices of all I own
		// keep printing stored report, but with new price (make trades record offline)
		//while (r) {
			for (String currency : currencies) {
				Symbol currencySymbol = new Symbol(currency);
				for (String asset : assets) {
					Symbol assetSymbol = new Symbol(asset);
					SymbolPair pair = new SymbolPair(assetSymbol, currencySymbol);
					Market m = markets.add(pair);

					List<ProfitLoss> pls = account.getProfitLosses(m, since);
					allPls.addAll(pls);
					if (assetSymbol.equals(selected)) {
						System.out.println("--- " + pair.getA() + "-" + pair.getB() + " ---");
						System.out.println(m.getPrice());
						System.out.println("-----------");
						print(pls);
						System.out.println();
					}
				}
			}
		//}
		for (ProfitLoss pl : allPls) {
			if (pl.getRelativeGain() >= 0.01) {
				print(pl);
			}
		}
		System.exit(0);
	}
	
	private void print(ProfitLoss pl) {
		System.out.println(pl.getFormatted());
	}

	private void print(List<ProfitLoss> pls) {
		for (ProfitLoss pl : pls) {
			print(pl);
		}
	}

	public void delay(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Monitor m = new Monitor();
		m.sales();
	}
}
