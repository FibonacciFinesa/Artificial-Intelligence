package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ID3 {

    private final int INITIAL_DEPTH = 1; // determines the printed value representing root depth value of the tree
    private ArrayList<Feature> header;  // doesn't include the class label feature
    private Feature classLabelFeature;
    private ArrayList<DatasetClass> datasetClassesCollection; // collection of all dataset classes from the training file
    private Node root;

    /**
     * Initialises all necessary parameters from a given dataset .csv file.
     *
     * @param filePath file path of the training dataset used to fit the decision tree
     * @throws IOException
     */
    private void treeInit(String filePath) throws IOException {

        header = new ArrayList<>();
        datasetClassesCollection = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String headerString = reader.readLine();
        String[] headerStringValues = headerString.split(",");
        for (String hsv : headerStringValues) {
            Feature newFeature = new Feature(hsv);
            header.add(newFeature);
        }

        String line = reader.readLine();
        while (line != null) {

            String[] featureEntryValues = line.split(",");
            ArrayList<String> featureEntry = new ArrayList<>(List.of(featureEntryValues)); // get feature values from dataset line
            String classLabelValue = featureEntry.get(featureEntry.size() - 1); // get the class label value
            DatasetClass dse = new DatasetClass(classLabelValue);

            for (int i = 0; i < header.size(); i++) {
                Feature feature = header.get(i);
                feature.addFeatureValue(featureEntry.get(i));

                // only true for all feature names except the class label feature
                if (i != header.size() - 1)
                    dse.putFeatureNameFeatureValue(feature.getFeatureName(), featureEntry.get(i));

            }

            datasetClassesCollection.add(dse);

            line = reader.readLine();
        }

        classLabelFeature = header.remove(header.size() - 1);
        reader.close();
    }

    /**
     * Fits the decision tree using the ID3 algorithm.
     *
     * @param filePath file path of the training dataset used to fit the decision tree
     * @throws IOException
     */
    public void fit(String filePath) throws IOException {

        treeInit(filePath);

        root = runID3Builder(datasetClassesCollection, datasetClassesCollection, header, classLabelFeature.getFeatureValues());
        System.out.println("[BRANCHES]:");
        printBranches(INITIAL_DEPTH, root, "");
    }

    /**
     * Fits the decision tree using the ID3 algorithm with a limited depth of the tree.
     *
     * @param filePath file path of the training dataset used to fit the decision tree
     * @param limit depth limit of the tree (0 equals only one leaf node)
     * @throws IOException
     */
    public void fit(String filePath, int limit) throws IOException {

        treeInit(filePath);

        root = runID3BuilderWithDepthLimit(datasetClassesCollection, datasetClassesCollection, header, classLabelFeature.getFeatureValues(), limit);
        System.out.println("[BRANCHES]:");
        printBranches(INITIAL_DEPTH, root, "");
    }

    /**
     * Predicts class label values using a fitted decision tree, calculates model accuracy and prints a confusion matrix.
     * Method can be used only when a tree has already been fitted.
     *
     * @param filePath file path of the test dataset used to test the fitted decision tree and predict class label values
     * @throws IOException
     */
    public void predict(String filePath) throws IOException {

        if (root == null) {
            System.out.println("Unable to predict, no tree available");
            return;
        }

        System.out.print("[PREDICTIONS]:");

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        ArrayList<Feature> predictionHeader = new ArrayList<>(); // header made from the feature names of the test file
        HashMap<String, String> featureNameFeatureValueMap = new HashMap<>();
        int correctPredictions = 0;
        int totalPredictions = 0;
        TreeSet<String> confusionMatrixClassLabels = getClassLabelValuesFromFile(filePath);
        ConfusionMatrix confusionMatrix = new ConfusionMatrix(confusionMatrixClassLabels); // confusion matrix initialisation

        String headerString = reader.readLine();
        String[] headerStringValues = headerString.split(",");
        for (String hsv : headerStringValues) {
            Feature newFeature = new Feature(hsv);
            predictionHeader.add(newFeature);
        }

        // remove class label
        predictionHeader.remove(predictionHeader.size() - 1);

        String line = reader.readLine();
        while (line != null) {

            String[] featureEntryValues = line.split(",");
            ArrayList<String> featureEntryValuesList = new ArrayList<>(List.of(featureEntryValues));

            // remove class label value from featureEntryValuesList
            String testFileClassLabel = featureEntryValuesList.remove(featureEntryValuesList.size() - 1);

            for (int i = 0; i < predictionHeader.size(); i++) {
                String featureNameFromFile = predictionHeader.get(i).getFeatureName();
                String featureValueFromFile = featureEntryValuesList.get(i);
                featureNameFeatureValueMap.put(featureNameFromFile, featureValueFromFile);
            }

            String prediction = predictionRecursion(root, featureNameFeatureValueMap);

            if (prediction.equals(testFileClassLabel))
                correctPredictions++;

            totalPredictions++;

            confusionMatrix.updateMatrix(testFileClassLabel, prediction);

            System.out.print(" " + prediction);
            line = reader.readLine();
        }

        reader.close();

        System.out.println();
        double accuracy = (double) correctPredictions / totalPredictions;
        System.out.printf("[ACCURACY]: %.5f\n", accuracy);
        System.out.println("[CONFUSION_MATRIX]:");
        confusionMatrix.printMatrix();
    }

    /**
     * Makes a tree set of all class label values appearing in a dataset .csv file.
     *
     * @param filePath file path of a dataset .csv file
     * @return tree set of all class label values appearing in a dataset .csv file
     * @throws IOException
     */
    private TreeSet<String> getClassLabelValuesFromFile(String filePath) throws IOException {

        TreeSet<String> classLabels = new TreeSet<>();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        // skip header
        String line = reader.readLine();

        line = reader.readLine();
        while (line != null) {

            String[] featureEntryValues = line.split(",");
            ArrayList<String> featureEntryValuesList = new ArrayList<>(List.of(featureEntryValues));
            String testFileClassLabel = featureEntryValuesList.remove(featureEntryValuesList.size() - 1);
            classLabels.add(testFileClassLabel);

            line = reader.readLine();
        }

        reader.close();
        return classLabels;
    }

    /**
     * Recursively traverses the decision tree and makes a prediction from a given dataset class.
     *
     * @param node currently expanded node, if the node is a leaf returns the leaf value
     * @param featureNameFeatureValueMapOfDatasetClass map of feature names and their corresponding values for a dataset class
     * @return class label prediction
     */
    private String predictionRecursion(Node node, HashMap<String, String> featureNameFeatureValueMapOfDatasetClass) {

        String nodeFeatureName = node.getFeatureName();
        String featureForPrediction = featureNameFeatureValueMapOfDatasetClass.get(nodeFeatureName);

        ArrayList<Node> children = node.getChildren();

        // only true for leaf nodes
        if(children == null) {
            return node.getLeafValue();
        }

        // transverse the tree through correct child
        for (Node child : node.getChildren()) {

            if (!child.getParentFeatureValue().equals(featureForPrediction))
                continue;

            return predictionRecursion(child, featureNameFeatureValueMapOfDatasetClass);
        }

        // the tree cannot be transversed further, return the most frequent class label value in subtree
        return node.getMostFrequentClassLabelInSubtree();
    }

    /**
     * Recursive implementation of the ID3 algorithm used to build a decision tree.
     *
     * @param datasetClassesCollection collection of all dataset classes corresponding to the currently created node
     * @param parentDatasetClassesCollection collection of all dataset classes corresponding to the parent node of currently created node
     * @param header list of feature names
     * @param classLabelValues set of all possible class label values
     * @return created subtree of the decision tree
     */
    private Node runID3Builder(ArrayList<DatasetClass> datasetClassesCollection, ArrayList<DatasetClass> parentDatasetClassesCollection, ArrayList<Feature> header, TreeSet<String> classLabelValues) {

        // border case 1
        if (datasetClassesCollection.isEmpty()) {
            String mostFrequentLabel = mostFrequentClassLabelValueInDatasetClassesCollection(classLabelValues, parentDatasetClassesCollection);
            return new Node(mostFrequentLabel) ;
        }

        String mostFrequentLabel = mostFrequentClassLabelValueInDatasetClassesCollection(classLabelValues, datasetClassesCollection);
        // border case 2
        if (header.isEmpty() || datasetClassesCollectionContainsOnlyClassLabelValue(datasetClassesCollection, mostFrequentLabel)) {
            return new Node(mostFrequentLabel);
        }

        // most discriminatory feature
        Feature mostDiscriminatoryFeature = mostDiscriminatoryFeatureFromFeatureCollection(datasetClassesCollection, header, classLabelValues);

        Node subtreeNode = new Node();
        subtreeNode.setFeatureName(mostDiscriminatoryFeature.getFeatureName());
        subtreeNode.setMostFrequentClassLabelInSubtree(mostFrequentLabel);
        TreeSet<String> featureValues = header.get(header.indexOf(mostDiscriminatoryFeature)).getFeatureValues();

        for (String featureValue : featureValues) {

            ArrayList<DatasetClass> reducedFeaturesCollection = keepClassesInCollectionWithFeatureValue(datasetClassesCollection, mostDiscriminatoryFeature.getFeatureName(), featureValue);
            ArrayList<Feature> reducedHeader = removeFeatureFromFeatureCollection(mostDiscriminatoryFeature, header);
            Node newNode = runID3Builder(reducedFeaturesCollection, datasetClassesCollection,  reducedHeader, classLabelValues);
            newNode.setParentFeatureValue(featureValue);
            subtreeNode.appendChild(newNode);

        }

        return subtreeNode;
    }

    /**
     * Recursive implementation of the ID3 algorithm used to build a decision tree with given depth limit.
     *
     * @param datasetClassesCollection collection of all dataset classes corresponding to the currently created node
     * @param parentDatasetClassesCollection collection of all dataset classes corresponding to the parent node of currently created node
     * @param header list of feature names
     * @param classLabelValues set of all possible class label values
     * @param limit depth limit of the created tree
     * @return created subtree of the decision tree
     */
    private Node runID3BuilderWithDepthLimit(ArrayList<DatasetClass> datasetClassesCollection, ArrayList<DatasetClass> parentDatasetClassesCollection, ArrayList<Feature> header, TreeSet<String> classLabelValues, int limit) {

        // border case 1
        if (datasetClassesCollection.isEmpty()) {
            String mostFrequentLabel = mostFrequentClassLabelValueInDatasetClassesCollection(classLabelValues, parentDatasetClassesCollection);
            return new Node(mostFrequentLabel) ;
        }

        String mostFrequentLabel = mostFrequentClassLabelValueInDatasetClassesCollection(classLabelValues, datasetClassesCollection);
        // border case 2
        if (header.isEmpty() || datasetClassesCollectionContainsOnlyClassLabelValue(datasetClassesCollection, mostFrequentLabel)) {
            return new Node(mostFrequentLabel);
        }

        // border case 3
        if (limit == 0) {
            return new Node(mostFrequentLabel);
        }

        // most discriminatory feature
        Feature mostDiscriminatoryFeature = mostDiscriminatoryFeatureFromFeatureCollection(datasetClassesCollection, header, classLabelValues);

        Node subtreeNode = new Node();
        subtreeNode.setFeatureName(mostDiscriminatoryFeature.getFeatureName());
        subtreeNode.setMostFrequentClassLabelInSubtree(mostFrequentLabel);
        TreeSet<String> featureValues = header.get(header.indexOf(mostDiscriminatoryFeature)).getFeatureValues();

        for (String featureValue : featureValues) {

            ArrayList<DatasetClass> reducedFeaturesCollection = keepClassesInCollectionWithFeatureValue(datasetClassesCollection, mostDiscriminatoryFeature.getFeatureName(), featureValue);
            ArrayList<Feature> reducedHeader = removeFeatureFromFeatureCollection(mostDiscriminatoryFeature, header);
            Node newNode = runID3BuilderWithDepthLimit(reducedFeaturesCollection, datasetClassesCollection,  reducedHeader, classLabelValues, limit - 1);
            newNode.setParentFeatureValue(featureValue);
            subtreeNode.appendChild(newNode);

        }

        return subtreeNode;
    }

    /**
     * Recursive function that prints the branches of a tree in DFS fashion.
     * The root depth is determined by the depth value. The depth value doesn't change the start of the transversal,
     * but rather the printed value.
     *
     * @param depth current depth of the printed node
     * @param node node for printing
     * @param path current path from ancestor node to current node
     */
    private void printBranches(int depth, Node node, String path) {

        ArrayList<Node> children = node.getChildren();

        if (children == null) {
            System.out.println(path + node.getLeafValue());
            return;
        }

        path += depth + ":" + node.getFeatureName() + "=";

        for(Node child : children) {
            path += child.getParentFeatureValue() + " ";
            printBranches(depth + 1, child, path);
            path = path.substring(0, path.length() - child.getParentFeatureValue().length() - 1);
        }
    }

    /**
     * Iterates the collection of all dataset classes and returns
     * the most frequent class label value inside the collection.
     * If the number of two class label values is the same, returns alphabetically smaller class label value.
     *
     * @param classLabelValues set of all possible class label values
     * @param datasetClassesCollection collection of all dataset classes
     * @return most frequent class label value in dataset classes collection
     */
    private String mostFrequentClassLabelValueInDatasetClassesCollection(TreeSet<String> classLabelValues, ArrayList<DatasetClass> datasetClassesCollection) {

        // guess that the most frequent class label value is first
        String mostFrequentClassLabelValue = classLabelValues.first();
        int mostFrequentClassLabelValueOccurrence = 0;
        for (DatasetClass dsc : datasetClassesCollection) {
            if (dsc.getClassLabelValue().equals(mostFrequentClassLabelValue))
                mostFrequentClassLabelValueOccurrence++;
        }

        for (String reviewingLabel : classLabelValues) {

            if (reviewingLabel.equals(mostFrequentClassLabelValue))
                continue;

            int reviewingClassLabelValueOccurrence = 0;
            for (DatasetClass dsc : datasetClassesCollection) {

                if (dsc.getClassLabelValue().equals(reviewingLabel))
                    reviewingClassLabelValueOccurrence++;

            }

            if (reviewingClassLabelValueOccurrence > mostFrequentClassLabelValueOccurrence) {

                mostFrequentClassLabelValue = reviewingLabel;
                mostFrequentClassLabelValueOccurrence = reviewingClassLabelValueOccurrence;

            } else if (reviewingClassLabelValueOccurrence == mostFrequentClassLabelValueOccurrence && reviewingLabel.compareTo(mostFrequentClassLabelValue) < 0) {

                mostFrequentClassLabelValue = reviewingLabel;

            }

        }

        return mostFrequentClassLabelValue;
    }

    /**
     * Checks whether the given dataset classes collection contains only classes with given class label value.
     *
     * @param datasetClassesCollection collection of all dataset classes
     * @param classLabelValue given class label value for filtering
     * @return true if the collection contains only given class label value, otherwise false
     */
    private boolean datasetClassesCollectionContainsOnlyClassLabelValue(ArrayList<DatasetClass> datasetClassesCollection, String classLabelValue) {

        for (DatasetClass dsc : datasetClassesCollection) {

            if (!dsc.getClassLabelValue().equals(classLabelValue))
                return false;

        }

        return true;
    }

    /**
     * Creates a new feature collection consisting of all features from given original feature collection
     * without the given feature.
     *
     * @param featureToRemove given feature for removal in the collection
     * @param featureCollection collection of all features for filtering
     * @return new reduced feature collection without the given feature for removal
     */
    private ArrayList<Feature> removeFeatureFromFeatureCollection(Feature featureToRemove, ArrayList<Feature> featureCollection) {

        ArrayList<Feature> reducedHeader = new ArrayList<>();

        for (Feature f : featureCollection) {

            if (!f.equals(featureToRemove)) {
                reducedHeader.add(f);
            }

        }

        return reducedHeader;
    }

    /**
     * Reduces the collection of dataset classes to only contain classes with a
     * given feature value for a given feature name.
     *
     * @param datasetClassesCollection collection of all dataset classes for reduction
     * @param featureName given feature name for observing
     * @param featureValue given feature value for keeping the dataset class with it
     * @return reduced dataset class collection with only dataset classes with given feature value
     */
    private ArrayList<DatasetClass> keepClassesInCollectionWithFeatureValue(ArrayList<DatasetClass> datasetClassesCollection, String featureName, String featureValue) {

        ArrayList<DatasetClass> reducedDatasetClassesCollection = new ArrayList<>();

        for (DatasetClass dsc : datasetClassesCollection) {
            if (dsc.getFeatureNameFeatureValueMap().get(featureName).equals(featureValue)) {

                DatasetClass newDsc = new DatasetClass(dsc.getClassLabelValue());
                newDsc.putAllFeatureNamesFeatureValues(dsc.getFeatureNameFeatureValueMap());
                reducedDatasetClassesCollection.add(newDsc);

            }
        }

        return reducedDatasetClassesCollection;
    }

    /**
     * Calculates the entropy of a given dataset classes collection.
     *
     * @param datasetClassesCollection collection of all dataset classes for entropy calculation
     * @param classLabelValues set of all possible class label values
     * @return entropy value of a given dataset classes collection
     */
    private Double calculateEntropy(ArrayList<DatasetClass> datasetClassesCollection, TreeSet<String> classLabelValues) {

        double entropy = 0;

        if (datasetClassesCollection.size() == 0)
            return (double) 0;

        for(String classLabelValue : classLabelValues) {

            int occurrence = frequencyOfClassLabelValueInDatasetClassesCollection(classLabelValue, datasetClassesCollection);

            if (occurrence == 0)
                continue;

            int totalClasses = datasetClassesCollection.size();
            double fraction = (double) occurrence / totalClasses;
            entropy += fraction * (Math.log(fraction) / Math.log(2));

        }

        return -entropy;
    }

    /**
     * Calculates the number of occurrences of a given class label value in a given dataset classes collection.
     *
     * @param classLabelValue class label value for occurrence calculation
     * @param datasetClassesCollection collection of all dataset classes for occurrence calculation
     * @return the number of occurrences of a given class label in a given dataset classes collection
     */
    private int frequencyOfClassLabelValueInDatasetClassesCollection(String classLabelValue, ArrayList<DatasetClass> datasetClassesCollection) {

        int frequency = 0;

        for (DatasetClass dsc : datasetClassesCollection) {
            if (dsc.getClassLabelValue().equals(classLabelValue))
                frequency++;
        }

        return frequency;
    }

    /**
     * Calculates information gain (IG) of all features and returns the most discriminatory feature with maximal
     * information gain.
     * If the information gain of two features is the same, returns the alphabetically smaller feature.
     *
     * @param datasetClassesCollection collection of all dataset classes used in information gain calculation
     * @param featureNameList list of all possible feature names
     * @param classLabelValues set of all possible class label values
     * @return the most discriminatory feature
     */
    private Feature mostDiscriminatoryFeatureFromFeatureCollection(ArrayList<DatasetClass> datasetClassesCollection, ArrayList<Feature> featureNameList, TreeSet<String> classLabelValues) {

        // guess that the most discriminatory feature from collection is first
        Feature mostDiscriminatoryFeature = featureNameList.get(0);
        double featuresCollectionEntropy = calculateEntropy(datasetClassesCollection, classLabelValues);
        double maxInformationGain = featuresCollectionEntropy;

        for (String fv : mostDiscriminatoryFeature.getFeatureValues()) {

            ArrayList<DatasetClass> reducedFeatureCollection = keepClassesInCollectionWithFeatureValue(datasetClassesCollection, mostDiscriminatoryFeature.getFeatureName(), fv);
            Double reducedFeatureCollectionEntropy = calculateEntropy(reducedFeatureCollection, classLabelValues);
            maxInformationGain -= ((double) reducedFeatureCollection.size() / datasetClassesCollection.size()) * reducedFeatureCollectionEntropy;
        }

        for (int i = 1; i < featureNameList.size(); i++) {

            Feature reviewedDiscriminatoryFeature = featureNameList.get(i);
            double reviewedInformationGain = featuresCollectionEntropy;

            for (String fv : reviewedDiscriminatoryFeature.getFeatureValues()) {

                ArrayList<DatasetClass> reducedFeatureCollection = keepClassesInCollectionWithFeatureValue(datasetClassesCollection, reviewedDiscriminatoryFeature.getFeatureName(), fv);
                Double reducedFeatureCollectionEntropy = calculateEntropy(reducedFeatureCollection, classLabelValues);
                reviewedInformationGain -= ((double) reducedFeatureCollection.size() / datasetClassesCollection.size()) * reducedFeatureCollectionEntropy;
            }

            if (reviewedInformationGain > maxInformationGain) {

                mostDiscriminatoryFeature = reviewedDiscriminatoryFeature;
                maxInformationGain = reviewedInformationGain;

            } else if ( reviewedInformationGain == maxInformationGain && reviewedDiscriminatoryFeature.getFeatureName().compareTo(mostDiscriminatoryFeature.getFeatureName()) < 0) {

                mostDiscriminatoryFeature = reviewedDiscriminatoryFeature;

            }

        }

        return mostDiscriminatoryFeature;
    }

}
