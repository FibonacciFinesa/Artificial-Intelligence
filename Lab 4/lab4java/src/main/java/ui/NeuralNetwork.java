package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class NeuralNetwork {

    private final double STDDEV = 0.01;
    private ArrayList<String> header;
    private String goalFeatureName;
    private ArrayList<double [][]> trainingInputs;
    private ArrayList<double [][]> testInputs;
    private ArrayList<Double> expectedTrainingOutputs;
    private ArrayList<Double> expectedTestOutputs;
    private ArrayList<WeightLayer> weightLayers;

    public ArrayList<WeightLayer> getWeightLayers() {
        return this.weightLayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeuralNetwork that = (NeuralNetwork) o;
        return weightLayers.equals(that.weightLayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weightLayers);
    }

    /**
     * Initializes the training and test datasets for the neural network.
     *
     * @param trainingFilePath file path to the dataset .txt file for training
     * @param testFilePath file path to the dataset .txt file for testing
     * @throws IOException if reading the file fails
     */
    public void initInputs(String trainingFilePath, String testFilePath) throws IOException {

        this.trainingInputs = new ArrayList<>();
        this.testInputs = new ArrayList<>();
        this.expectedTrainingOutputs = new ArrayList<>();
        this.expectedTestOutputs = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(trainingFilePath));

        String line = reader.readLine();
        String[] headerFromFile = line.split(",");
        this.header = new ArrayList<>(Arrays.asList(headerFromFile));
        this.goalFeatureName = this.header.remove(header.size() - 1);

        line = reader.readLine();
        while (line != null) {

            double [][] input = new double[1][header.size()];

            String[] splitLine = line.split(",");

            // extract the expected output of the goal feature
            double expectedOutput = Double.parseDouble(splitLine[splitLine.length - 1]);

            // extract the input values from split line for the input matrix
            for(int i = 0; i < splitLine.length - 1; i++){
                input[0][i] = Double.parseDouble(splitLine[i]);
            }

            // append to lists
            this.trainingInputs.add(input);
            this.expectedTrainingOutputs.add(expectedOutput);

            line = reader.readLine();
        }

        reader.close();

        reader = new BufferedReader(new FileReader(testFilePath));

        // skip the header line in the test file, assumes headers will match
        line = reader.readLine();

        line = reader.readLine();
        while (line != null) {

            //input will always have dimensions 1 x number of features
            double [][] input = new double[1][header.size()];

            String[] splitLine = line.split(",");

            // extract the expected output of the goal feature
            double expectedOutput = Double.parseDouble(splitLine[splitLine.length - 1]);

            // extract the input values from split line for the input matrix
            for(int i = 0; i < splitLine.length - 1; i++){
                input[0][i] = Double.parseDouble(splitLine[i]);
            }

            // append to lists
            this.testInputs.add(input);
            this.expectedTestOutputs.add(expectedOutput);

            line = reader.readLine();
        }

        reader.close();
    }

    /**
     * Initializes the hidden weight layers and the appropriate biases for the hidden layer.
     *
     * @param format the format of the neural network architecture.
     *               Example: "5s5s" will have two hidden layers of 5 neurons followed by sigmoid activation function.
     *
     * @param random Random object used to initialize the weights from a Gaussian distribution.
     */
    public void initWeightLayers(String format, Random random) {

        if(this.trainingInputs == null)
            throw new NullPointerException("Initialize the inputs to the network first");

        this.weightLayers = new ArrayList<>();

        String[] hiddenNeuronNumberPerLayer = format.split("s");

        for(int i = 0; i <= hiddenNeuronNumberPerLayer.length; i++){

            int hiddenLayerColumnNumber;

            // check if the current weights precede the final output neuron
            if(i == hiddenNeuronNumberPerLayer.length)
                hiddenLayerColumnNumber = 1;
            else
                hiddenLayerColumnNumber = Integer.parseInt(hiddenNeuronNumberPerLayer[i]);

            // initial case where the first layer of weights depends on the input dimensions
            if(i == 0){
                int firstHiddenLayerRowNumber = this.trainingInputs.get(0)[0].length;
                WeightLayer newWeightLayer = new WeightLayer(firstHiddenLayerRowNumber, hiddenLayerColumnNumber, this.STDDEV, random);
                this.weightLayers.add(newWeightLayer);
                continue;
            }

            WeightLayer priorHiddenLayer = weightLayers.get(i - 1);
            int hiddenLayerRowNumber = priorHiddenLayer.getNumberOfColumns();
            WeightLayer newWeightLayer = new WeightLayer(hiddenLayerRowNumber,hiddenLayerColumnNumber, this.STDDEV, random);
            this.weightLayers.add(newWeightLayer);
        }

    }

    /**
     * Propagates the given input through the neural network.
     *
     * @param input given input in matrix form
     * @return the result produced by the neural network
     */
    public double propagate(double[][] input) {

        if(weightLayers == null)
            throw new NullPointerException("Network weights must be initialized.");

        double[][] result = input;
        for(WeightLayer weightLayer : this.weightLayers) {
            result = multiplyMatrices(result, weightLayer.getWeights());

            // apply the activation function to all neurons except the output neuron
            if(this.weightLayers.indexOf(weightLayer) != this.weightLayers.size() - 1) {
                result = addBias(result, weightLayer.getBias());
                applySigmoidActivationFunction(result);
            }
        }

        return result[0][0];
    }

    /**
     * Propagates all training inputs through the neural network and calculates the sum square error.
     *
     * @return sum square error of the training dataset
     */
    public double train() {

        double error = 0.0;

        for(int i = 0; i < this.trainingInputs.size(); i++){

            double predictedOutput = propagate(this.trainingInputs.get(i));
            double expectedOutput = this.expectedTrainingOutputs.get(i);

            double outputDifference = expectedOutput - predictedOutput;

            error += Math.pow(outputDifference, 2);

        }

        error /= this.trainingInputs.size();

        return error;
    }

    /**
     * Propagates all test inputs through the neural network and calculates the sum square error.
     *
     * @return sum square error of the test dataset
     */
    public double test() {

        double error = 0.0;

        for(int i = 0; i < this.testInputs.size(); i++){

            double predictedOutput = propagate(this.testInputs.get(i));
            double expectedOutput = this.expectedTestOutputs.get(i);

            double outputDifference = expectedOutput - predictedOutput;

            error += Math.pow(outputDifference, 2);

        }

        error /= this.testInputs.size();

        return error;
    }

    /**
     * Multiplies two given matrices A and B and returns their product in matrix form.
     *
     * @param matrixA first matrix
     * @param matrixB second matrix
     * @throws IllegalArgumentException matrices must be dimensionally compatible for multiplication
     * @return matrix result of the multiplication
     */
    private double[][] multiplyMatrices(double[][] matrixA, double[][] matrixB) {

        int matrixARows = matrixA.length;
        int matrixACols = matrixA[0].length;
        int matrixBRows = matrixB.length;
        int matrixBCols = matrixB[0].length;

        if (matrixACols != matrixBRows)
            throw new IllegalArgumentException("Incompatible matrix dimensions for multiplication.");

        double[][] result = new double[matrixARows][matrixBCols];

        for (int i = 0; i < matrixARows; i++) {
            for (int j = 0; j < matrixBCols; j++) {

                double rowColumnDotProduct = 0.0;

                for (int k = 0; k < matrixACols; k++) {
                    rowColumnDotProduct += matrixA[i][k] * matrixB[k][j];
                }

                result[i][j] = rowColumnDotProduct;
            }
        }

        return result;
    }

    /**
     * Takes two matrices and returns their sum.
     *
     * @param matrixA first matrix
     * @param biasMatrix bias matrix
     * @throws IllegalArgumentException matrices must be dimensionally compatible for summation
     * @return resulting matrix after summation
     */
    private double[][] addBias(double[][] matrixA, double[][] biasMatrix) {

        int matrixARows = matrixA.length;
        int matrixACols = matrixA[0].length;
        int biasMatrixRows = biasMatrix.length;
        int biasMatrixCols = biasMatrix[0].length;

        if ((matrixARows != biasMatrixRows) || (matrixACols != biasMatrixCols))
            throw new IllegalArgumentException("Incompatible matrix dimensions for addition.");

        double[][] result = new double[matrixARows][matrixACols];

        for (int i = 0; i < matrixARows; i++) {
            for (int j = 0; j < matrixACols; j++) {
                result[i][j] = matrixA[i][j] + biasMatrix[i][j];
            }
        }

        return result;
    }

    /**
     * Calculates the sigmoid(x) for a given x.
     *
     * @param x variable for the sigmoid function
     * @return sigmoid(x)
     */
    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * Applies the sigmoid activation function to the given matrix elements.
     *
     * @param matrix given matrix for calculation
     * @throws NullPointerException matrix can't be null
     */
    private void applySigmoidActivationFunction(double[][] matrix) {

        if (matrix == null)
            throw new NullPointerException("The given matrix is null");

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = sigmoid(matrix[i][j]);//sigmoid.value(matrix[i][j]);
            }
        }

    }

}
