package ui;

import java.util.Random;

public class WeightLayer {

    private final int numberOfRows;
    private final int numberOfColumns;
    private final double[][] weights;
    private final double[][] bias;

    public WeightLayer(int numberOfRows, int numberOfColumns, double stddev, Random random) {
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;

        weights = new double[numberOfRows][numberOfColumns];
        bias = new double[1][numberOfColumns];

        // initializes weights
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                weights[i][j] = stddev * random.nextGaussian();
            }
        }

        // initializes bias
        for (int j = 0; j < numberOfColumns; j++) {
            bias[0][j] = stddev * random.nextGaussian();
        }
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public double[][] getWeights() {
        return weights;
    }

    public double[][] getBias() {
        return bias;
    }
}
