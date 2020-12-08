	package altaite.binance;

/**
 * All market tickers channel examples.
 *
 * It illustrates how to create a stream to obtain all market tickers.
 */
public class Alerting {

	// TODO automated from first, plus sharp movements
	private void run() {
		double c = 1;

		System.out.println("Alerting started.");
		//Alert btcusdt = new Alert("btcusdt", 15297, 15382);
		//Alert bnbbtc = new Alert("bnbbtc", 0.00185, 0.001858);
		//Alert ethbtc = new Alert("ethbtc", 0.028, 0.040);
		//Alert ethusdt = new Alert("ethusdt", 374.0, 395.5);
		//Alert xmrusdt = new Alert("xmrusdt", 120.0, 125.0);
		//Alert eosbtc = new Alert("eosbtc", 0.00024, 0.00030);
		//Alert vetbtc = new Alert("vetbtc",  0.00000125, 0.00000225);

		new AlertRelative("btcusdt", c);
		new AlertRelative("ethbtc", c);
		new AlertRelative("btcusdt", -c);
		new AlertRelative("ethbtc", -c);

		//new AlertRelative("xmrusdt", c);
		//new AlertRelative("xmrusdt", -c);

		//new AlertRelative("xrpusdt", 3);
		//new AlertRelative("xrpusdt", -3);

		new AlertRelative("bnbusdt", c);
		new AlertRelative("bnbusdt", -c);

		//new AlertRelative("bnbbtc", c);
		//new AlertRelative("bnbbtc", -c);

		new AlertRelative("linkusdt", c);
		new AlertRelative("linkusdt", -c);
		
		
		new AlertRelative("zecbtc", c);
		new AlertRelative("zecbtc", -c);
	}

	public static void main(String[] args) {
		Alerting a = new Alerting();
		a.run();
	}

}
