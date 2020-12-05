package altaite.binance.trading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SequenceTest {

	private void test() {

		List<T> ts = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(new File("d:/t/data/market/bitcoin/xmr_text.txt")))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				T t = new T(Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
				ts.add(t);
			}
			T[] trades = new T[ts.size()];
			ts.toArray(trades);

		/*	Sequence s = new Sequence(trades);
			
			s.summary(trades[trades.length - 1].price);*/
			
			
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void main(String[] args) {
		SequenceTest t = new SequenceTest();
		t.test();
	}

}
