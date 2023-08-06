package ui;

import java.util.*;

public class GeneticNeuralNetworkTrainer {

    private final String trainFilePath;
    private final String testFilePath;
    private final String architecture;
    private final int populationSize;
    private final int elitism;
    private final double mutationProbability;
    private final double gaussianNoiseStddev;
    private final int iterations;

    private final Random random;
    private ArrayList<Chromosome> initialPopulation;

    public GeneticNeuralNetworkTrainer(String trainFilePath, String testFilePath, String architecture,
                                       int populationSize, int elitism, double mutationProbability,
                                       double gaussianNoiseStddev, int iterations) {

        this.trainFilePath = trainFilePath;
        this.testFilePath = testFilePath;
        this.architecture = architecture;
        this.populationSize = populationSize;
        this.elitism = elitism;
        this.mutationProbability = mutationProbability;
        this.gaussianNoiseStddev = gaussianNoiseStddev;
        this.iterations = iterations;

        this.random = new Random();

        createNewInitialPopulation();

    }

    /**
     * Creates a new population of chromosomes based on the hyperparameters of the GeneticNeuralNetworkTrainer object.
     *
     */
    private void createNewInitialPopulation() {

        this.initialPopulation = new ArrayList<>();

        for (int i = 0; i < this.populationSize; i++) {

            Chromosome newChromosome = new Chromosome(this.trainFilePath, this.testFilePath, this.architecture);
            this.initialPopulation.add(newChromosome);

        }
    }

    /**
     * Prints the error of the best chromosome for the given iteration.
     *
     * @param iteration iteration number
     * @param population list of chromosomes
     */
    public void printIterationResult(int iteration, ArrayList<Chromosome> population) {

        population.sort(Chromosome::compareTo);
        Collections.reverse(population);
        Chromosome bestChromosome = population.get(0);
        System.out.printf("[Train error @%d]: %.6f\n", iteration, bestChromosome.getError());

    }

    /**
     * Evaluates the performance of the best chromosome on the test dataset calculating sum square error.
     *
     * @param chromosome the chromosome for evaluation
     * @return the sum square error on the hyperparameter test dataset
     */
    public double evaluateOptimalChromosome(Chromosome chromosome) {
        return chromosome.evaluateNeuralNetwork();
    }

    /**
     * Runs the generational trainer and optimizes the neural network architectures of the population.
     * Uses the hyperparameters of the GeneticNeuralNetworkTrainer object.
     *
     * @return returns the chromosome with the neural network which achieved the smallest error on the training dataset.
     */
    public Chromosome runGenerationalTrainer() {

        // Generation 0. is based on the initial population
        ArrayList<Chromosome> parentPopulation = new ArrayList<>(this.initialPopulation);

        // evaluates the current population updating fitness
        parentPopulation.forEach(Chromosome::calculateAndUpdateFitness);

        for (int iteration = 1; iteration <= this.iterations; iteration++) {

            ArrayList<Chromosome> newPopulation = new ArrayList<>();

            // sorts the population in reverse order based on their fitness, the best fitness first
            parentPopulation.sort(Chromosome::compareTo);
            Collections.reverse(parentPopulation);

            // keep best parent chromosomes from prior generation to the next generation
            for (int i = 0; i < this.elitism; i++) {
                newPopulation.add(parentPopulation.get(i));
            }

            // map used to keep track of paired parents, promotes diversity
            HashMap<Chromosome, Chromosome> parentPairingMap = new HashMap<>();

            while (newPopulation.size() < parentPopulation.size()) {

                ArrayList<Chromosome> parentSelectionList = new ArrayList<>(parentPopulation);
                Chromosome firstParent = selectParentsFromPopulation(parentSelectionList);
                parentSelectionList.remove(firstParent); // comment this line if you allow a parent to mix with itself
                Chromosome secondParent = selectParentsFromPopulation(parentSelectionList);

                // if the parents have already been paired, skip the current pairing
                if ( (parentPairingMap.containsKey(firstParent) && parentPairingMap.get(firstParent).equals(secondParent)) ||
                        (parentPairingMap.containsKey(secondParent) && parentPairingMap.get(secondParent).equals(firstParent))) {
                    continue;
                }

                parentPairingMap.put(firstParent, secondParent);

                //create new child from parent chromosomes
                Chromosome child = crossoverParents(firstParent, secondParent);

                // apply mutations to the child
                applyMutationToChromosome(child);

                newPopulation.add(child);

            }

            // evaluate the new generation population
            newPopulation.forEach(Chromosome::calculateAndUpdateFitness);
            // the new population becomes the parent population for next iteration
            parentPopulation = newPopulation;

            if (iteration % 2000 == 0)
                printIterationResult(iteration, parentPopulation);
        }

        // get the best chromosome
        parentPopulation.sort(Chromosome::compareTo);
        Collections.reverse(parentPopulation);
        Chromosome bestChromosome = parentPopulation.get(0);

        return bestChromosome;
    }

    /**
     * Selects a parent chromosome from the given population using roulette wheel mechanism.
     * The selection is fitness proportional.
     *
     * @param population list of chromosomes for choosing the parent
     * @return returns the chosen parent chromosome
     */
    private Chromosome selectParentsFromPopulation(ArrayList<Chromosome> population) {

        double fitnessSum = 0;
        for (Chromosome chromosome : population) {
            fitnessSum += chromosome.getFitness();
        }

        // chooses a random double number between 0 and fitnessSum
        Random random = new Random();
        double pointer = random.nextDouble() * fitnessSum;

        double area = 0.0;
        for (Chromosome chromosome : population) {
            area += Math.abs(chromosome.getFitness());
            if (pointer <= area)
                return chromosome;
        }

        return null;
    }

    /**
     * Crosses over genetic material of the parents creating a child chromosome.
     * The neural network weights of the child are calculated as arithmetic sum of the parent weights.
     *
     * @param firstParent first parent chromosome
     * @param secondParent second parent chromosome
     * @return returns the child chromosome
     */
    private Chromosome crossoverParents(Chromosome firstParent, Chromosome secondParent) {

        Chromosome child = new Chromosome(this.trainFilePath, this.testFilePath, this.architecture);

        ArrayList<WeightLayer> firstParentWeightLayers = firstParent.getChromosomeWeights();
        ArrayList<WeightLayer> secondParentWeightLayers = secondParent.getChromosomeWeights();
        ArrayList<WeightLayer> childWeightLayers = child.getChromosomeWeights();

        for (int weightLayerIndex = 0; weightLayerIndex < firstParentWeightLayers.size(); weightLayerIndex++) {

            WeightLayer firstParentLayer = firstParentWeightLayers.get(weightLayerIndex);
            double[][] firstParentWeights = firstParentLayer.getWeights();

            WeightLayer secondParentLayer = secondParentWeightLayers.get(weightLayerIndex);
            double[][] secondParentWeights = secondParentLayer.getWeights();

            WeightLayer childLayer = childWeightLayers.get(weightLayerIndex);
            double[][] childWeights = childLayer.getWeights();

            // crossover weights
            for (int i = 0; i < childWeights.length; i++) {
                for (int j = 0; j < childWeights[0].length; j++) {
                    childWeights[i][j] = (firstParentWeights[i][j] + secondParentWeights[i][j]) / 2.0;
                }
            }

            // crossover bias
            double [][] firstParentBias = firstParentLayer.getBias();
            double [][] secondParentBias = secondParentLayer.getBias();
            double [][] childBias = childLayer.getBias();

            for (int i = 0; i < childBias.length; i++) {
                for (int j = 0; j < childBias[0].length; j++) {
                    childBias[i][j] = (firstParentBias[i][j] + secondParentBias[i][j]) / 2.0;
                }
            }
        }

        return child;
    }

    /**
     * Mutates the chromosome with given hyperparameter probability by adding Gaussian noise
     * with hyperparameter standard deviation.
     *
     * @param chromosome the chromosome for mutation
     */
    private void applyMutationToChromosome(Chromosome chromosome) {

        Random newRandom = new Random();
        ArrayList<WeightLayer> childWeightLayers = chromosome.getChromosomeWeights();

        for (WeightLayer weightLayer : childWeightLayers) {

            double[][] weights = weightLayer.getWeights();

            for (int i = 0; i < weights.length; i++) {
                for (int j = 0; j < weights[0].length; j++) {

                    double randomValue = newRandom.nextDouble();
                    if (randomValue < this.mutationProbability) {
                        weights[i][j] += this.gaussianNoiseStddev * newRandom.nextGaussian();
                    }

                }
            }

            double[][] bias = weightLayer.getBias();
            for (int i = 0; i < bias.length; i++) {
                for (int j = 0; j < bias[0].length; j++) {
                    double randomValue = newRandom.nextDouble();
                    if (randomValue < this.mutationProbability) {
                        bias[i][j] += this.gaussianNoiseStddev * newRandom.nextGaussian();
                    }
                }
            }
        }
    }
}
