package altaite.binance;

import altaite.binance.sound.Sound;
import altaite.binance.sound.Voice;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlertRelative {

	private final String market;
	private double change;
	private Double low, high;
	private Double base;
	private boolean printed;
	private Double lastLow;
	private Double lastHigh;

	private static Voice voice = new Voice();

	AlertRelative(String market, double percentualChange) {
		this.market = market;
		this.change = percentualChange / 100;
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiWebSocketClient client = factory.newWebSocketClient();
		client.onCandlestickEvent(market, CandlestickInterval.ONE_MINUTE, response -> process(response));
	}

	private synchronized void process(CandlestickEvent e) {
		double hi = Double.parseDouble(e.getHigh());
		double lo = Double.parseDouble(e.getLow());
		lastLow = lo;
		lastHigh = hi;
		if (base == null) {
			restart(lo, hi);
		}
		if (high != null && hi >= high) {
			report(hi, ">=", high);
			//Sound.high();
			voice.speak(insertSpaces(market) + " high hail creeper");
			//System.out.println("spoke2");
			restart(lo, hi);
		} else if (low != null && lo <= low) {
			report(lo, "<=", low);
			//Sound.low();
			voice.speak(insertSpaces(market) + " low");
			//System.out.println("spoke");
			restart(lo, hi);
		}
	}

	private String insertSpaces(String in) {
		StringBuilder out = new StringBuilder();
		for (char c : in.toCharArray()) {
			out.append(c).append(" ");
		}
		return out.toString();
	}

	private void restart(double lo, double hi) {
		base = (hi + lo) / 2;
		if (change < 0) {
			low = base + change * base;
		} else {
			high = base + change * base;
		}
	}

	public Double getLastAverage() {
		if (lastHigh == null || lastLow == null) {
			return null;
		}
		return (lastLow + lastHigh) / 2;
	}

	private void report(double value1, String comparison, double value2) {
		//if (!printed) {
		System.out.println(market + " " + value1 + " " + comparison + " " + value2 + " (" + getDate()
			+ ", base: " + base + ")");
		/*System.out.println("base: " + base);
			System.out.println("low: " + low);
			System.out.println("high: " + change);
			System.out.println("change: " + change);*/
		printed = true;
		//}
	}

	private String getDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd. M yyyy");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

	public static void main(String[] args) {
		voice.speak("hail creeper");
	}
}
