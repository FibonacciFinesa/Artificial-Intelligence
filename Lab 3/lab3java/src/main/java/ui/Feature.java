package ui;

import java.util.Objects;
import java.util.TreeSet;

public class Feature {

    private String featureName;
    private TreeSet<String> featureValues;

    public Feature(String featureName) {
        this.featureName = featureName;
        this.featureValues = new TreeSet<>();
    }

    public void addFeatureValue(String value) {
        featureValues.add(value);
    }

    public String getFeatureName() {
        return featureName;
    }

    public TreeSet<String> getFeatureValues() {
        return featureValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return featureName.equals(feature.featureName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureName);
    }


}
