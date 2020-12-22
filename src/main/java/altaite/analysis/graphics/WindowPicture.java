package altaite.analysis.graphics;

import altaite.binance.data.Candle;
import altaite.binance.data.window.Window;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class WindowPicture {

	private Window window;
	private int width = 1920, height = 1080;
	private double low, high;
	private int buyTimeIndex;
	private double sellPrice;

	public WindowPicture(Window w, int buyTimeIndex, double sellPrice) {
		this.window = w;
		this.buyTimeIndex = buyTimeIndex;
		this.sellPrice = sellPrice;
	}

	public void save(File imageFile) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		//int color = Color.WHITE.getRGB();
		Graphics g = bi.getGraphics();
		drawWindow(g);
		g.dispose();
		try {
			ImageIO.write(bi, "png", imageFile);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void drawWindow(Graphics g) {
		low = window.getLow();
		high = window.getHigh();
		for (int i = 0; i < window.size(); i++) {
			Candle c = window.get(i);

			double di = i;
			int x = (int) Math.round(2 + di / (window.size() + 6) * width);
			int lo = y(c.getLow());
			int hi = y(c.getHigh());
			int open = y(c.getOpen());
			int close = y(c.getClose());

			int smaller = open < close ? open : close;
			int bodyHeight = Math.abs(open - close);

			if (i == buyTimeIndex) {
				g.setColor(Color.GRAY);
				g.drawLine(x, 0, x, height);
				g.drawLine(0, close, width, close);
				g.drawLine(width * 9 / 10, y(sellPrice), width, y(sellPrice));
				//System.out.println(close + " " + sellPrice + " " + y(sellPrice) + " " + i + " ~~~~~~~~~~~");
			}

			if (c.getClose() > c.getOpen()) {
				g.setColor(Color.GREEN);
			} else {
				g.setColor(Color.RED);
			}
			g.fillRect(x - 1, smaller, 3, bodyHeight);
			g.drawLine(x, hi, x, lo);

		}
	}

	private int y(double price) {
		double d = high - low;
		return height - ((int) Math.round((price - low) / d * height));
	}
}
