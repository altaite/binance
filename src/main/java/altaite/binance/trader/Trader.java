package altaite.binance.trader;

import altaite.learn.Instance;
import altaite.learn.model.Model;

public class Trader {
	// start evaluation: single fixed threshold for buy or sell, compute money
	// LATER: learn how much to buy, thresholds or linear to prediction 
	// buy, sell: simple threshold on price prediction
	// optimize both on data, all options, maximize total profit
	// alternative: ML? enough examples?

	// !!!!!!!!!!!!!!!!! build certainty profile, buy/sell based on that and wishes of investor
	// use math here, simple statistics
	// >>>>>>>>>>>>>do this first >>> #### for each bracket of price prediction: compute distribution!, then use this to evaluate how much should be transacted (expectation maximization)
	// some function for skewing buys and sells when out of optimum (0 or desired level btc : usdt)
	// split criterion (how much to buy 2 * p - 1): treat each level of win/loss amount as different bet
	// stop loss and expectancy, trade profit?
	// check speed of big rises and falls
	// does high price expectation help find good spikes on all markets? !!!!!!!!!!!!!!!!!!!
	// think what to do with possible leftover money
	// or tell them deadline? decrease exposure? exporuse is a good parameter
	
	private Model model;

	public Trader(Model model) {
		this.model = model;
	}

	
	public double trade(Instance instance) {
		model.predict(instance);
		return 0;
	}
}
