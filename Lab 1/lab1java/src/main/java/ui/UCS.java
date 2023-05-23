package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UCS {

    private Node startNode;
    private HashSet<String> goalStateNames;
    private HashMap<Node, TreeSet<Node>> allStates;

    private class Node implements Comparable<Node>{
        private final String name;
        private double cost;
        private Node parent;
        private TreeSet<Node> neighbours;

        public Node(String name, double cost) {
            this.name = name;
            this.cost = cost;
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
        public int compareTo(Node other) {
            if (this.cost == other.cost) {
                return this.name.compareTo(other.name);
            } else {
                return Double.compare(this.cost, other.cost);
            }
        }

    }

    private void argumentsInit(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line = reader.readLine();
        while (line.startsWith("#")) {
            line = reader.readLine();
        }

        String startNodeName = line;

        line = reader.readLine();
        while (line.startsWith("#")) {
            line = reader.readLine();
        }

        String[] goalStateNames = line.split(" ");
        this.goalStateNames = new HashSet<>();
        this.goalStateNames.addAll(Arrays.asList(goalStateNames));

        allStates = new HashMap<>();

        line = reader.readLine();
        while (line != null) {
            if(line.startsWith("#")){
                line = reader.readLine();
            }
            String[] stateAndNeighbours = line.split(": ");

            if(stateAndNeighbours.length == 1) {
                stateAndNeighbours[0] = stateAndNeighbours[0].substring(0, stateAndNeighbours[0].length() - 1);
            }

            Node newNode = new Node(stateAndNeighbours[0], 0);

            if(stateAndNeighbours.length > 1) {
                String neighboursUnsplit = stateAndNeighbours[1];
                String[] neighbours = neighboursUnsplit.split(" ");

                for (String neighbour : neighbours) {
                    String[] nameAndCost = neighbour.split(",");
                    String name = nameAndCost[0];
                    double cost = Double.parseDouble(nameAndCost[1]);

                    Node neigbourNode = new Node(name, cost);
                    newNode.appendNeighbour(neigbourNode);
                }
            }

            allStates.put(newNode, newNode.getNeighbours());
            if(newNode.getName().equals(startNodeName)) {
                startNode = new Node(newNode.getName(), newNode.getCost());
                for(Node neighbour : newNode.getNeighbours()) {
                    startNode.appendNeighbour(neighbour);
                }
            }
            line = reader.readLine();
        }

        reader.close();
    }

    public void createNodePath(LinkedList<Node> nodePath, Node node) {

        if(node.equals(startNode)) {
            nodePath.addFirst(node);
            return;
        }

        nodePath.addFirst(node);
        createNodePath(nodePath, node.getParent());
    }

    public void runAlgorithm(String filePath) throws IOException {

        argumentsInit(filePath);

        String foundSolution = "no";
        int statesVisited = -1; //on start first node will set it to 0
        int pathLength = 0;
        double totalCost = 0;
        Node finalState = null;

        PriorityQueue<Node> open = new PriorityQueue<>();
        open.add(startNode);

        HashSet<String> closedNodeNames = new HashSet<>();

        while (open.peek() != null) {
            Node currentNode = open.remove();
            closedNodeNames.add(currentNode.getName());
            statesVisited++;

            //check if currentNode is a goal state
            if (goalStateNames.contains(currentNode.getName())) {
                finalState = currentNode;
                totalCost = currentNode.getCost();
                foundSolution = "yes";
                break;
            }

            TreeSet<Node> currentNodeNeighboursTemplateSet = allStates.get(currentNode);

            if(currentNodeNeighboursTemplateSet == null)
                continue;

            for (Node neighbourTemplate : currentNodeNeighboursTemplateSet) {

                if(closedNodeNames.contains(neighbourTemplate.getName()))
                    continue;

                Node neighbour = new Node(neighbourTemplate.getName(), neighbourTemplate.getCost());
                neighbour.increaseCost(currentNode.getCost());
                neighbour.setParent(currentNode);
                open.add(neighbour);
            }
        }

        if(foundSolution.equals("no")) {
            System.out.println("[FOUND_SOLUTION]: " + foundSolution);
            return;
        }

        LinkedList<Node> nodePath = new LinkedList<>();
        createNodePath(nodePath, finalState);
        pathLength = nodePath.size();

        System.out.println("# UCS");
        System.out.println("[FOUND_SOLUTION]: " + foundSolution);
        System.out.println("[STATES_VISITED]: " + statesVisited);
        System.out.println("[PATH_LENGTH]: " + pathLength);
        System.out.println("[TOTAL_COST]: " + totalCost);
        System.out.print("[PATH]: ");

        for(int i = 0; i < nodePath.size(); i++) {
            if(i == nodePath.size() - 1) {
                System.out.print(nodePath.get(i).getName());
            } else {
                System.out.print(nodePath.get(i).getName() + " => ");
            }
        }
        return;
    }

    public double runAlgorithmFromState(String filePath, String startNodeName) throws IOException {

        argumentsInit(filePath);

        startNode = new Node(startNodeName, 0);
        double totalCost = 0;
        PriorityQueue<Node> open = new PriorityQueue<>();
        open.add(startNode);

        HashSet<String> closedNodeNames = new HashSet<>();

        while (open.peek() != null) {
            Node currentNode = open.remove();
            closedNodeNames.add(currentNode.getName());

            //check if currentNode is a goal state
            if (goalStateNames.contains(currentNode.getName())) {
                totalCost = currentNode.getCost();
                return totalCost;
            }

            TreeSet<Node> currentNodeNeighboursTemplateSet = allStates.get(currentNode);

            if(currentNodeNeighboursTemplateSet == null)
                continue;

            for (Node neighbourTemplate : currentNodeNeighboursTemplateSet) {

                if(closedNodeNames.contains(neighbourTemplate.getName()))
                    continue;

                Node neighbour = new Node(neighbourTemplate.getName(), neighbourTemplate.getCost());
                neighbour.increaseCost(currentNode.getCost());
                neighbour.setParent(currentNode);
                open.add(neighbour);
            }
        }

        return -1.0;
    }
}
