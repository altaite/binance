package altaite.binance.data;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import java.util.Calendar;
import java.util.Date;

import java.util.List;

/**
 * Examples on how to get market data information such as the latest price of a symbol, etc., in an async way.
 */
public class History {

	//private Sticks sticks = new Sticks();

	private static long getMilliseconds(int day, int month, int year) {
		//SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
		Calendar c = new Calendar.Builder().setDate(year, month, day).build();
		Date d = c.getTime();
		long milliseconds = d.getTime();
		return milliseconds;
	}

	private String longToDate(long l) {
		Date d = new Date(l);
		return d.toString();
	}

	private void process(List<Candlestick> response) {
		for (Candlestick cs : response) {
			//System.out.println(cs);
			//sticks.add(cs);
		}
		System.out.println("retrieved " + response.size());
	}

	private List<Long> getTimePoints(long start, long end, long candleStickInterval, int bufferSize) {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiAsyncRestClient client = factory.newAsyncRestClient();

		System.out.println(start);
		System.out.println(end);
		System.out.println(candleStickInterval * bufferSize);
		int count = 0;
		for (long t = start; t <= end; t += candleStickInterval * bufferSize) {
			System.out.println("retrieving " + t);
			client.getCandlestickBars("BTCUSDT", CandlestickInterval.ONE_MINUTE, 1000,
				t, t + candleStickInterval * bufferSize,
				(List<Candlestick> response) -> process(response));
			count++;
		}
		System.out.println("total " + count);
		return null;
	}

	private void run() {
		long start = getMilliseconds(1, 9, 2017);
		long end = getMilliseconds(1, 10, 2017);
		long minuteInMilliseconds = 60 * 1000;
		getTimePoints(start, end, minuteInMilliseconds, 1000);

	}

	private void run2() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiAsyncRestClient client = factory.newAsyncRestClient();

		/**
		 * Kline/candlestick bars for a symbol. Klines are uniquely identified by their open time.
		 *
		 * @param symbol symbol to aggregate (mandatory)
		 * @param interval candlestick interval (mandatory)
		 * @param limit Default 500; max 1000 (optional)
		 * @param startTime Timestamp in ms to get candlestick bars from INCLUSIVE (optional).
		 * @param endTime Timestamp in ms to get candlestick bars until INCLUSIVE (optional).
		 * @param callback the callback that handles the response containing a candlestick bar for the given symbol and
		 * interval
		 */
		//void getCandlestickBars(String symbol, CandlestickInterval interval, Integer limit, Long startTime, Long endTime, BinanceApiCallback<List<Candlestick>> callback);
		long startTime = getMilliseconds(1, 9, 2017);
		long endTime = getMilliseconds(1, 1, 2020);
		client.getCandlestickBars("BTCUSDT", CandlestickInterval.ONE_MINUTE, 1000, startTime, endTime,
			(List<Candlestick> response) -> process(response));

		/*	// Getting depth of a symbol (async)
		client.getOrderBook("NEOETH", 10, (OrderBook response) -> {
			System.out.println(response.getBids());
		});

		// Getting latest price of a symbol (async)
		client.get24HrPriceStatistics("NEOETH", (TickerStatistics response) -> {
			System.out.println(response);
		});

		// Getting all latest prices (async)
		client.getAllPrices((List<TickerPrice> response) -> {
			System.out.println(response);
		});

		// Getting agg trades (async)
		client.getAggTrades("NEOETH", (List<AggTrade> response) -> System.out.println(response));

		// Weekly candlestick bars for a symbol
		client.getCandlestickBars("NEOETH", CandlestickInterval.WEEKLY,
			(List<Candlestick> response) -> System.out.println(response));

		// Book tickers (async)
		client.getBookTickers(response -> System.out.println(response));

		// Exception handling
		try {
			client.getOrderBook("UNKNOWN", 10, response -> System.out.println(response));
		} catch (BinanceApiException e) {
			System.out.println(e.getError().getCode()); // -1121
			System.out.println(e.getError().getMsg());  // Invalid symbol
		}*/
	}

	public static void main(String[] args) {
		History history = new History();
		history.run();
	}
}
