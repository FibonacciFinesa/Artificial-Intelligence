package ui;

import java.util.HashMap;
import java.util.TreeSet;

public class ConfusionMatrix {

    private final int[][] matrix;
    private final HashMap<String, Integer> classLabelIndexMap;

    public ConfusionMatrix(TreeSet<String> classLabelValues) {
        this.matrix = new int[classLabelValues.size()][classLabelValues.size()];
        this.classLabelIndexMap = new HashMap<>();

        int index = 0;
        for (String value : classLabelValues) {
            this.classLabelIndexMap.put(value, index++);
        }
    }

    public void printMatrix() {

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if(j == 0)
                    System.out.print(matrix[i][j]);
                else
                    System.out.print(" " + matrix[i][j]);
            }
            System.out.println();
        }

    }

    public void updateMatrix(String realClassLabel, String predictedClassLabel) {

        int indexOfRealClassLabel = classLabelIndexMap.get(realClassLabel);
        int indexOfpredictedClassLabel = classLabelIndexMap.get(predictedClassLabel);

        matrix[indexOfRealClassLabel][indexOfpredictedClassLabel]++;

    }
}
