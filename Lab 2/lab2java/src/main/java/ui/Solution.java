package ui;

import java.io.IOException;

public class Solution {

	public static void main(String ... args) throws IOException {

       if (args.length == 2 && args[0].equals("resolution")) {
		   String path = args[1];
		   Resolution res = new Resolution();
		   res.runResolution(path);

	   } else if (args.length == 3 && args[0].equals("cooking")) {
		   String clausesPath = args[1];
		   String inputsPath = args[2];

		   CookingAssistant ca = new CookingAssistant();
		   ca.runAssistant(clausesPath, inputsPath);

	   }
	}

}
