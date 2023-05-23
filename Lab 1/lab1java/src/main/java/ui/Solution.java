package ui;

import java.io.IOException;

public class Solution {

	public static void main(String ... args) throws IOException {

		String alg = null;
		String ss = null;
		String h = null;
		boolean checkOptimistic = false;
		boolean checkConsistent = false;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("--alg")) {
				alg = args[i + 1];
			} else if (arg.equals("--ss")) {
				ss = args[i + 1];
			} else if (arg.equals("--h")) {
				h = args[i + 1];
			} else if (arg.equals("--check-optimistic")) {
				checkOptimistic = true;
			} else if (arg.equals("--check-consistent")) {
				checkConsistent = true;
			}
		}

		if (alg != null && alg.equals("bfs")) {
			BFS bfs = new BFS();
			bfs.runAlgorithm(ss);
		} else if (alg != null && alg.equals("ucs")) {
			UCS ucs = new UCS();
			ucs.runAlgorithm(ss);
		} else if (alg != null && alg.equals("astar")) {
			ASTAR astar = new ASTAR();
			astar.runAlgorithm(ss, h);
		} else if (checkOptimistic) {
			HeuristicValidator hv = new HeuristicValidator();
			hv.checkOptimism(ss, h);
		} else if (checkConsistent) {
			HeuristicValidator hv = new HeuristicValidator();
			hv.checkConsistency(ss, h);
		}
	}

}
