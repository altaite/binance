package altaite.binance;

import altaite.binance.sound.Sound;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Alert {

	private final String market;
	private final Double low, high;
	private boolean printed;

	Alert(String market, double low, double high) {
		this.market = market;
		this.low = low;
		this.high = high;
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiWebSocketClient client = factory.newWebSocketClient();
		client.onCandlestickEvent(market, CandlestickInterval.ONE_MINUTE, response -> process(response));
	}

	private void process(CandlestickEvent e) {
		double hi = Double.parseDouble(e.getHigh());
		double lo = Double.parseDouble(e.getLow());
		if (hi >= high) {
			report(hi, ">=", high);
			Sound.high();
		} else if (lo <= low) {
			report(lo, "<=", low);
			Sound.low();
		}
	}

	private void report(double value1, String comparison, double value2) {
		if (!printed) {
			System.out.println(market + " " + value1 + " " + comparison + " " + value2 + " (" + getDate() + ")");
			printed = true;
		}
	}

	private String getDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd. M yyyy");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

}
