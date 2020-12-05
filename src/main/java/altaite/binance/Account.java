package altaite.binance;

import altaite.binance.current.Market;
import altaite.binance.current.Markets;
import altaite.binance.data.Symbol;
import altaite.binance.trading.ProfitLoss;
import altaite.binance.trading.Sequence;
import altaite.binance.trading.T;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.exception.BinanceApiException;
import java.util.ArrayList;

import java.util.List;

public class Account {

	private BinanceApiRestClient client;
	private Markets markets;

	public Account(Markets markets) {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("Pb4ZxISDGQPwc9BLWU0mhvgDyATq1USv7gBeuK8Y6L0xr69gyMqXt9SyT6oMrhAi",
			"4A2flXocutqEBv8sWVRR55bav18L7cYmT1G3RJUdTON3E3wSnYDFwlwm5xaFtLTO");
		client = factory.newRestClient();
		this.markets = markets;
	}

	public void printAssets() {
		for (AssetBalance ab : client.getAccount().getBalances()) {
			double free = Double.parseDouble(ab.getFree());
			if (free > 0) {
				Symbol symbol = new Symbol(ab.getAsset());
				Double inUsd = null;
				try {
					double price = markets.getUsdPrice(symbol);
					inUsd = price * free;
				} catch (BinanceApiException ex) {
				}
				System.out.println(symbol + " " + free + " " + inUsd);
			}
		}
	}

	public List<ProfitLoss> getProfitLosses(Market market, long since) {
		List<Trade> trades = client.getMyTrades(market.getPair().toString());
		List<T> ts = new ArrayList<>();

		for (int i = 0; i < trades.size(); i++) {
			Trade trade = trades.get(i);
			if (trade.getTime() >= since) {
				ts.add(new T(trade));
			}
			//System.out.println(ts[i].price + " " + ts[i].qty);
		}

		T[] a = new T[ts.size()];
		ts.toArray(a);
		//System.out.println();
		Sequence s = new Sequence(market.getPair(), a);
		return s.summary(market.getPrice());
	}

}
