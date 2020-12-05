package altaite.binance.current;

import altaite.binance.data.Symbol;
import altaite.binance.data.SymbolPair;
import com.binance.api.client.BinanceApiRestClient;
import java.util.HashMap;
import java.util.Map;

public class Markets {

	private final BinanceApiRestClient rest;
	private Map<SymbolPair, Market> markets = new HashMap<>();

	public Markets(BinanceApiRestClient rest) {
		this.rest = rest;
	}

	public Market add(SymbolPair pair) {
		Market ms = new Market(pair, rest);
		markets.put(pair, ms);
		return ms;
	}

	public Market get(SymbolPair pair) {
		return markets.get(pair);
	}

	public double getUsdPrice(Symbol symbol) {
		Symbol usdt = new Symbol("usdt");
		return Double.parseDouble(rest.getPrice(new SymbolPair(symbol, usdt).toString()).getPrice());
	}
}
