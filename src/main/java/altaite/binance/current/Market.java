package altaite.binance.current;

import altaite.binance.data.SymbolPair;
import com.binance.api.client.BinanceApiRestClient;

public class Market {

	private SymbolPair pair;
	private BinanceApiRestClient rest;

	public Market(SymbolPair pair, BinanceApiRestClient rest) {
		this.pair = pair;
		this.rest = rest;
	}

	public SymbolPair getPair() {
		return pair;
	}

	public Double getPrice() {
		return Double.parseDouble(rest.getPrice(pair.toString()).getPrice());
	}

}
