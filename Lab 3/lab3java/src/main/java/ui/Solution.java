package ui;

import java.io.IOException;
import java.util.Locale;

public class Solution {

	public static void main(String ... args) {

		Locale.setDefault(Locale.US);

		ID3 model = new ID3();
		try {
			if (args.length == 2) {
				model.fit(args[0]);
				model.predict(args[1]);
			} else if (args.length == 3) {
				model.fit(args[0], Integer.parseInt(args[2]));
				model.predict(args[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

}
