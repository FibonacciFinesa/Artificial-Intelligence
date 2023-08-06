package ui;

import java.util.Locale;

public class Solution {

	public static void main(String ... args) {

		Locale.setDefault(Locale.US);

		String trainFilePath = null;
		String testFilePath = null;
		String architecture = null;
		int populationSize = 0;
		int elitism = 0;
		double mutationProbability = 0;
		double gaussianNoiseStddev = 0;
		int iterations = 0;

		try {

			for (int i = 0; i < args.length; i+=2) {
				String arg = args[i];
				if (arg.equals("--train")) {
					trainFilePath = args[i + 1];
				} else if (arg.equals("--test")) {
					testFilePath = args[i + 1];
				} else if (arg.equals("--nn")) {
					architecture = args[i + 1];
				} else if (arg.equals("--popsize")) {
					populationSize = Integer.parseInt(args[i + 1]);
				} else if (arg.equals("--elitism")) {
					elitism = Integer.parseInt(args[i + 1]);
				} else if (arg.equals("--p")) {
					mutationProbability = Double.parseDouble(args[i + 1]);
				} else if (arg.equals("--K")) {
					gaussianNoiseStddev = Double.parseDouble(args[i + 1]);
				} else if (arg.equals("--iter")) {
					iterations = Integer.parseInt(args[i + 1]);
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		// create a new genetic neural network trainer
		GeneticNeuralNetworkTrainer gnnt = new GeneticNeuralNetworkTrainer(trainFilePath, testFilePath, architecture,
																			populationSize, elitism, mutationProbability,
																			gaussianNoiseStddev, iterations);
		// get the best network
		Chromosome bestChromosome = gnnt.runGenerationalTrainer();
		double bestChromosomeTestError = gnnt.evaluateOptimalChromosome(bestChromosome);

		System.out.printf("[Test error]: %.6f\n", bestChromosomeTestError);
	}
}
