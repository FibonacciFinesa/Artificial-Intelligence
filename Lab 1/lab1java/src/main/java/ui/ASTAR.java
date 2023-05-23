package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ASTAR {

    private Node startNode;
    private HashSet<String> goalStateNames;
    private HashMap<Node, TreeSet<Node>> allStates;
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
        public int compareTo(Node other) {
            if ((this.cost + this.heuristic) == (other.cost + other.heuristic)) {
                return this.name.compareTo(other.name);
            } else {
                return Double.compare((this.cost + this.heuristic), (other.cost + other.heuristic));
            }
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

            Node newNode = new Node(stateAndNeighbours[0], 0, heuristicsMap.get(stateAndNeighbours[0]));

            if(stateAndNeighbours.length > 1) {
                String neighboursUnsplit = stateAndNeighbours[1];
                String[] neighbours = neighboursUnsplit.split(" ");

                for (String neighbour : neighbours) {
                    String[] nameAndCost = neighbour.split(",");
                    String name = nameAndCost[0];
                    double cost = Double.parseDouble(nameAndCost[1]);

                    Node neigbourNode = new Node(name, cost, heuristicsMap.get(name));
                    newNode.appendNeighbour(neigbourNode);
                }
            }

            allStates.put(newNode, newNode.getNeighbours());
            if(newNode.getName().equals(startNodeName)) {
                startNode = new Node(newNode.getName(), newNode.getCost(), newNode.getHeuristic());
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

    public void runAlgorithm(String filePathStateSpace, String filePathHeuristics) throws IOException {

        setHeuristicsMap(filePathHeuristics);
        argumentsInit(filePathStateSpace);

        String foundSolution = "no";
        int statesVisited = -1; //on start first node will set it to 0
        int pathLength = 0;
        double totalCost = 0;
        Node finalState = null;

        PriorityQueue<Node> open = new PriorityQueue<>();
        open.add(startNode);

        HashSet<Node> closed = new HashSet<>();

        while (open.peek() != null) {
            Node currentNode = open.remove();
            closed.add(currentNode);
            statesVisited++;

            //check if currentNode is a goal state
            if (goalStateNames.contains(currentNode.getName())) {
                finalState = currentNode;
                totalCost = currentNode.getCost();
                foundSolution = "yes";
                break;
            }

            TreeSet<Node> currentNodeNeighboursTemplateSet = allStates.get(currentNode);

            if (currentNodeNeighboursTemplateSet == null)
                continue;

            for (Node neighbourTemplate : currentNodeNeighboursTemplateSet) {

                neighbourTemplate.increaseCost(currentNode.getCost());

                if (closed.contains(neighbourTemplate)) {

                    HashSet<Node> closedCopy = new HashSet<>(closed);
                    Iterator<Node> iterator = closedCopy.iterator();
                    while (iterator.hasNext()) {
                        Node element = iterator.next();
                        if (element.equals(neighbourTemplate)) {

                            if(element.getCost() < neighbourTemplate.getCost()) {
                                break;
                            } else {
                                closed.remove(element);
                                Node neighbour = new Node(neighbourTemplate.getName(), neighbourTemplate.getCost(), heuristicsMap.get(neighbourTemplate.getName()));
                                neighbour.setParent(currentNode);
                                open.add(neighbour);
                                break;
                            }
                        }
                    }

                } else if (open.contains(neighbourTemplate)) {

                    PriorityQueue<Node> openCopy = new PriorityQueue<>(open);
                    Iterator<Node> iterator = openCopy.iterator();
                    while (iterator.hasNext()) {
                        Node element = iterator.next();
                        if (element.equals(neighbourTemplate)) {

                            if(element.getCost() < neighbourTemplate.getCost()) {
                                break;
                            } else {
                                open.remove(element);
                                Node neighbour = new Node(neighbourTemplate.getName(), neighbourTemplate.getCost(), heuristicsMap.get(neighbourTemplate.getName()));
                                neighbour.setParent(currentNode);
                                open.add(neighbour);
                                break;
                            }
                        }
                    }

                } else {

                    Node neighbour = new Node(neighbourTemplate.getName(), neighbourTemplate.getCost(), heuristicsMap.get(neighbourTemplate.getName()));
                    neighbour.setParent(currentNode);
                    open.add(neighbour);

                }

            }
        }

        if(foundSolution.equals("no")) {
            System.out.println("[FOUND_SOLUTION]: " + foundSolution);
            return;
        }

        LinkedList<Node> nodePath = new LinkedList<>();
        createNodePath(nodePath, finalState);
        pathLength = nodePath.size();

        System.out.println("# A-STAR " + filePathHeuristics);
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

}
