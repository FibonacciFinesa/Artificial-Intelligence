package ui;

import java.util.ArrayList;

public class Node {

    private String featureName;
    private String parentFeatureValue;
    private String mostFrequentClassLabelInSubtree;
    private ArrayList<Node> children;
    private String leafValue;

    public Node(String leafValue) {
        this.children = null;
        this.leafValue = leafValue;
    }

    public Node() {
        this.children = new ArrayList<>();
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public void setParentFeatureValue(String parentFeatureValue) {
        this.parentFeatureValue = parentFeatureValue;
    }

    public void setMostFrequentClassLabelInSubtree(String mostFrequentClassLabelInSubtree) {
        this.mostFrequentClassLabelInSubtree = mostFrequentClassLabelInSubtree;
    }

    public void appendChild(Node node) {
        children.add(node);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getParentFeatureValue() {
        return parentFeatureValue;
    }

    public String getLeafValue() {
        return leafValue;
    }

    public String getMostFrequentClassLabelInSubtree() {
        return mostFrequentClassLabelInSubtree;
    }
}
