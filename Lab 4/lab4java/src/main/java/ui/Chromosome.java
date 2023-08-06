package ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Chromosome implements Comparable<Chromosome> {

    private Random random;
    private String trainFilePath;
    private String testFilePath;
    private String architecture;

    private NeuralNetwork nn;
    private double error;
    private double fitness;

    public Chromosome(String trainFilePath, String testFilePath, String architecture) {

        try {

            this.random = new Random();
            this.trainFilePath = trainFilePath;
            this.testFilePath = testFilePath;
            this.architecture = architecture;
            this.nn = new NeuralNetwork();
            this.nn.initInputs(trainFilePath, testFilePath);
            this.nn.initWeightLayers(architecture, this.random);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public double getError() {
        return this.error;
    }

    public double getFitness() {
        return this.fitness;
    }

    public ArrayList<WeightLayer> getChromosomeWeights() {
        return this.nn.getWeightLayers();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chromosome that = (Chromosome) o;
        return Double.compare(that.fitness, fitness) == 0 && nn.equals(that.nn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nn, fitness);
    }

    @Override
    public int compareTo(Chromosome other) {
        return Double.compare(this.fitness, other.fitness);
    }

    /**
     * Calculates and updates the fitness of the chromosome on the given training dataset.
     *
     * @return the fitness quality of the chromosome
     */
    public double calculateAndUpdateFitness() {

        if (this.nn == null) {
            throw new NullPointerException("No neural network initialized.");
        }

        this.error = nn.train();
        this.fitness = 1.0 / (1.0 + this.error);

        return this.fitness;
    }

    public double evaluateNeuralNetwork() {
        return nn.test();
    }
}
