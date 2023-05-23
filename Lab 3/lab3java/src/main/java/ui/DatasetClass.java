package ui;

import java.util.TreeMap;

public class DatasetClass {

    private TreeMap<String, String> featureNameFeatureValueMap;
    private String classLabelValue;

    public DatasetClass(String classLabelValue) {
        this.featureNameFeatureValueMap = new TreeMap<>();
        this.classLabelValue = classLabelValue;
    }

    public TreeMap<String, String> getFeatureNameFeatureValueMap() {
        return featureNameFeatureValueMap;
    }

    public String getClassLabelValue() {
        return classLabelValue;
    }

    public void putFeatureNameFeatureValue(String featureName, String featureValue) {
        this.featureNameFeatureValueMap.put(featureName, featureValue);
    }

    public void putAllFeatureNamesFeatureValues(TreeMap<String, String> sourceFeatureNameFeatureValueMap) {
        this.featureNameFeatureValueMap.putAll(sourceFeatureNameFeatureValueMap);
    }
}
