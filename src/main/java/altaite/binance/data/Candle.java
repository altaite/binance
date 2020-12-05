package altaite.binance.data;

import com.binance.api.client.domain.market.Candlestick;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Candle {

	private long openTime;

	private double open;

	private double high;

	private double low;

	private double close;

	private double volume;

	private long closeTime;

	private double quoteAssetVolume;

	private long numberOfTrades;

	private double takerBuyBaseAssetVolume;

	private double takerBuyQuoteAssetVolume;

	private static boolean inversionWarningDisplayed = false;

	public void invert() {
		open *= -1;
		close *= -1;
		high *= -1;
		low *= -1;
		if (!inversionWarningDisplayed) {
			System.out.println("INVERSION OF CANDLES, BEWARE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			inversionWarningDisplayed = true;
		}
	}

	public Candle(Candlestick b) {
		openTime = encode(b.getOpenTime());
		open = sd(b.getOpen());
		high = sd(b.getHigh());
		low = sd(b.getLow());
		close = sd(b.getClose());
		volume = sd(b.getVolume());
		closeTime = encode(b.getCloseTime());
		quoteAssetVolume = sd(b.getQuoteAssetVolume());
		numberOfTrades = encode(b.getNumberOfTrades());
		takerBuyBaseAssetVolume = sd(b.getTakerBuyBaseAssetVolume());
		takerBuyQuoteAssetVolume = sd(b.getTakerBuyQuoteAssetVolume());
	}

	public Candle(DataInputStream dis) throws IOException {
		openTime = dis.readLong();
		open = dis.readDouble();
		high = dis.readDouble();
		low = dis.readDouble();
		close = dis.readDouble();
		volume = dis.readDouble();
		closeTime = dis.readLong();
		quoteAssetVolume = dis.readDouble();
		numberOfTrades = dis.readLong();
		takerBuyBaseAssetVolume = dis.readDouble();
		takerBuyQuoteAssetVolume = dis.readDouble();
	}

	public void write(DataOutputStream dos) {
		try {
			dos.writeLong(openTime);
			dos.writeDouble(open);
			dos.writeDouble(high);
			dos.writeDouble(low);
			dos.writeDouble(close);
			dos.writeDouble(volume);
			dos.writeLong(closeTime);
			dos.writeDouble(quoteAssetVolume);
			dos.writeLong(numberOfTrades);
			dos.writeDouble(takerBuyBaseAssetVolume);
			dos.writeDouble(takerBuyQuoteAssetVolume);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void print() {
		System.out.println("***");
		System.out.println(openTime);
		System.out.println(open);
		System.out.println(high);
		System.out.println(low);
		System.out.println(close);
		System.out.println(volume);
		System.out.println(closeTime);
		System.out.println(quoteAssetVolume);
		System.out.println(numberOfTrades);
		System.out.println(takerBuyBaseAssetVolume);
		System.out.println(takerBuyQuoteAssetVolume);
		System.out.println("///");
	}

	private long encode(Long l) {
		if (l == null) {
			return Long.MIN_VALUE;
		} else {
			return l;
		}
	}

	private double sd(String s) {
		return Double.parseDouble(s);
	}

	public long getOpenTime() {
		return openTime;
	}

	public double getOpen() {
		return open;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public double getClose() {
		return close;
	}

	public double getVolume() {
		return volume;
	}

	public long getCloseTime() {
		return closeTime;
	}

	public double getQuoteAssetVolume() {
		return quoteAssetVolume;
	}

	public long getNumberOfTrades() {
		return numberOfTrades;
	}

	public double getTakerBuyBaseAssetVolume() {
		return takerBuyBaseAssetVolume;
	}

	public double getTakerBuyQuoteAssetVolume() {
		return takerBuyQuoteAssetVolume;
	}
}
