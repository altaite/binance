package altaite.binance.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketTimeFrame {

	private String marketSymbol;
	private Date start, end;
	private long interval;

	public MarketTimeFrame(String marketSymbol, Date start, Date end) {
		this.marketSymbol = marketSymbol;
		this.start = start;
		this.end = end;
	}

	// return start times of each batch
	public List<Long> getBatches(int batchSize) {
		List<Long> times = new ArrayList<>();
		return times;
	}

}
