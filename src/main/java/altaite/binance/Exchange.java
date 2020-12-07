package altaite.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import global.io.LineFile;
import java.util.HashSet;
import java.util.Set;

public class Exchange {

	private BinanceApiRestClient rest = BinanceApiClientFactory.newInstance().newRestClient();

	private void getSymbols() {
		ExchangeInfo info = rest.getExchangeInfo();
		System.out.println(info.toString());

		System.out.println(info.getSymbols().size());
		Set<String> set = new HashSet<>();
		for (SymbolInfo si : info.getSymbols()) {
			System.out.println(si.getSymbol());
			System.out.println(si.getBaseAsset());
			String other = si.getSymbol().replace(si.getBaseAsset(), "");
			set.add(si.getBaseAsset());
			set.add(other);
			System.out.println();
		}
		LineFile lf = new LineFile("d:/t/data/binance/currency_symbols.txt");
		for (String s : set) {
			lf.writeLine(s);
		}
	}

	public static void main(String[] args) {
		Exchange e = new Exchange();
		e.getSymbols();
	}
}
