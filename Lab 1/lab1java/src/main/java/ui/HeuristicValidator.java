package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class HeuristicValidator {

    private TreeSet<String> allStateNames;
    private TreeMap<Node, TreeSet<Node>> allStates;
    private HashMap<String, Double> heuristicsMap;

    private class Node implements Comparable<Node>{
        private final String name;
        private double cost;
        private double heuristic;
        private Node parent;
        private TreeSet<Node> neighbours;

        public Node(String name, double cost, double heuristic) {
            this.name = name;
            this.cost = cost;
            this.heuristic = heuristic;
            this.parent = null;
            this.neighbours = new TreeSet<>();
        }

        public String getName() {
            return name;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public void increaseCost(double cost) {
            this.cost += cost;
        }

        public double getHeuristic() {
            return heuristic;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public TreeSet<Node> getNeighbours() {
            return neighbours;
        }

        public void appendNeighbour(Node neighbour) {
            this.neighbours.add(neighbour);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return name.equals(node.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public int compareTo(Node otherNode) {
            return this.name.compareTo(otherNode.getName());
        }

    }

    private void setHeuristicsMap(String filePath) throws IOException {

        heuristicsMap = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line = reader.readLine();
        while (line != null) {

            if (line.startsWith("#")) {
                line = reader.readLine();
            }

            String[] stateAndHeuristic = line.split(": ");
            heuristicsMap.put(stateAndHeuristic[0], Double.parseDouble(stateAndHeuristic[1]));
            line = reader.readLine();
        }

    }

    private void argumentsInit(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        allStateNames = new TreeSet<>();
        allStates = new TreeMap<>();

        String line = reader.readLine();
        while (line.startsWith("#")) {
            line = reader.readLine();
        }

        String startNodeName = line;

        line = reader.readLine();
        while (line.startsWith("#")) {
            line = reader.readLine();
        }

        line = reader.readLine();
        while (line != null) {

            if(line.startsWith("#")){
                line = reader.readLine();
            }

            String[] stateAndNeighbours = line.split(": ");

            if(stateAndNeighbours.length == 1) {
                stateAndNeighbours[0] = stateAndNeighbours[0].substring(0, stateAndNeighbours[0].length() - 1);
            }

            String newNodeName = stateAndNeighbours[0];
            allStateNames.add(newNodeName);

            TreeSet<Node> neighboursSet = new TreeSet<>();
            if(stateAndNeighbours.length > 1) {
                String neighboursUnsplit = stateAndNeighbours[1];
                String[] neighbours = neighboursUnsplit.split(" ");

                for (String neighbour : neighbours) {
                    String[] nameAndCost = neighbour.split(",");
                    String name = nameAndCost[0];
                    double cost = Double.parseDouble(nameAndCost[1]);

                    Node neigbourNode = new Node(name, cost, heuristicsMap.get(name));
                    neighboursSet.add(neigbourNode);
                }
            }

            Node newNode = new Node(newNodeName, 0, heuristicsMap.get(newNodeName));
            allStates.put(newNode, neighboursSet);
            line = reader.readLine();
        }
        reader.close();
    }

    public void checkOptimism(String filePathStateSpace, String filePathHeuristics) throws IOException {

        setHeuristicsMap(filePathHeuristics);
        argumentsInit(filePathStateSpace);

        UCS ucs = new UCS();
        boolean error = false;

        System.out.println("# HEURISTIC-OPTIMISTIC " + filePathHeuristics);

        for(String stateName : allStateNames) {
            double totalCost = ucs.runAlgorithmFromState(filePathStateSpace, stateName);
            double heuristic = heuristicsMap.get(stateName);
            if(heuristic <= totalCost) {
                System.out.println("[CONDITION]: [OK] h(" + stateName + ") <= h*: " + String.format(Locale.US,"%.1f", heuristic) + " <= " + String.format(Locale.US,"%.1f", totalCost));
            } else {
                System.out.println("[CONDITION]: [ERR] h(" + stateName + ") <= h*: " + String.format(Locale.US,"%.1f", heuristic) + " <= " + String.format(Locale.US,"%.1f", totalCost));
                error = true;
            }
        }

        System.out.println("[CONCLUSION]: Heuristic is" + (error ? " not " : " ") + "optimistic.");

    }

    public void checkConsistency(String filePathStateSpace, String filePathHeuristics) throws IOException {

        setHeuristicsMap(filePathHeuristics);
        argumentsInit(filePathStateSpace);

        boolean error = false;

        System.out.println("# HEURISTIC-CONSISTENT " + filePathHeuristics);

        for(Map.Entry<Node, TreeSet<Node>> entry : allStates.entrySet()) {
            Node currentNode = entry.getKey();
            TreeSet<Node> neighbours = entry.getValue();
            for(Node neighbour : neighbours) {

                if(currentNode.getHeuristic() <= neighbour.getHeuristic() + neighbour.getCost()) {
                    System.out.println("[CONDITION]: [OK] h(" + currentNode.getName() + ") <= h(" + neighbour.getName() + ") + c: " + String.format(Locale.US,"%.1f", currentNode.getHeuristic()) + " <= " + String.format(Locale.US,"%.1f", neighbour.getHeuristic()) + " + " + String.format(Locale.US,"%.1f", neighbour.getCost()));
                } else {
                    System.out.println("[CONDITION]: [ERR] h(" + currentNode.getName() + ") <= h(" + neighbour.getName() + ") + c: " + String.format(Locale.US,"%.1f", currentNode.getHeuristic()) + " <= " + String.format(Locale.US,"%.1f", neighbour.getHeuristic()) + " + " + String.format(Locale.US,"%.1f", neighbour.getCost()));
                    error = true;
                }
            }
        }

        System.out.println("[CONCLUSION]: Heuristic is" + (error ? " not " : " ") + "consistent.");

    }

}
