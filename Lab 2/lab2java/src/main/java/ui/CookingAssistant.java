package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CookingAssistant {

    private ArrayList<Clause> initialClauses;
    private ArrayList<Clause> setOfSupport;
    private Clause goal;

    private class Clause {

        private HashSet<String> clauseSet;
        private Clause parent1;
        private Clause parent2;

        public Clause(HashSet<String> clauseSet) {
            this.clauseSet = clauseSet;
        }

        public void setParent1(Clause parent1) {
            this.parent1 = parent1;
        }

        public void setParent2(Clause parent2) {
            this.parent2 = parent2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Clause clause = (Clause) o;
            return clauseSet.equals(clause.clauseSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clauseSet);
        }

        @Override
        public String toString() {
            return String.join(" v ", clauseSet);
        }
    }

    private class ClausePair {

        private Clause clause1;
        private Clause clause2;

        public ClausePair(Clause clause1, Clause clause2) {
            this.clause1 = clause1;
            this.clause2 = clause2;
        }

        public Clause getClause1() {
            return clause1;
        }

        public Clause getClause2() {
            return clause2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClausePair that = (ClausePair) o;
            return clause1.equals(that.clause1) && clause2.equals(that.clause2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clause1, clause2);
        }
    }

    private void argumentsInit(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        initialClauses = new ArrayList<>();

        String line = reader.readLine();
        while (line != null) {

            if (line.startsWith("#")) {
                line = reader.readLine();
            }

            line = line.toLowerCase();
            String[] atoms = line.split(" v ");
            Clause newClause = new Clause(new HashSet<>(Arrays.asList(atoms)));

            initialClauses.add(newClause);
            line = reader.readLine();
        }

    }

    private void goalInit(Clause goalClause) {

        goal = goalClause;
        setOfSupport = new ArrayList<>();

        for(String literal : goal.clauseSet) {

            if (literal.startsWith("~")) {
                literal = literal.substring(1);
            } else {
                literal = "~" + literal;
            }

            HashSet<String> supportClause = new HashSet<>();
            supportClause.add(literal);
            setOfSupport.add(new Clause(supportClause));
        }

    }

    private void resolveRedundancy(ArrayList<Clause> listOfClauses, Clause newClause) {

        listOfClauses.removeIf(clause -> clause.clauseSet.containsAll(newClause.clauseSet));

    }

    private boolean checkValidity(Clause newClause) {

        HashSet<String> nonNegatedLiterals = new HashSet<>();
        HashSet<String> negations = new HashSet<>();

        for (String literal : newClause.clauseSet) {

            if(literal.startsWith("~")) {
                negations.add(literal.substring(1));
                continue;
            }

            nonNegatedLiterals.add(literal);
        }

        for (String literal : nonNegatedLiterals) {
            if (negations.contains(literal))
                return false;
        }

        return true;
    }


    private ArrayList<ClausePair> generateClausePairs(ArrayList<Clause> initialClauses, ArrayList<Clause> newlyDerivedClauses) {

        ArrayList<ClausePair> clausePairs = new ArrayList<>();

        for (Clause initialClause : initialClauses) {

            for (Clause newlyDerivedClause : newlyDerivedClauses) {

                ClausePair clausePair = new ClausePair(initialClause, newlyDerivedClause);
                clausePairs.add(clausePair);

            }
        }

        for (int i = 0; i < newlyDerivedClauses.size(); i++) {

            for (int j = i + 1; j < newlyDerivedClauses.size(); j++) {

                ClausePair clausePair = new ClausePair(newlyDerivedClauses.get(i), newlyDerivedClauses.get(j));
                clausePairs.add(clausePair);

            }
        }

        return clausePairs;
    }

    private ArrayList<Clause> resolve(ClausePair clausePair) {

        ArrayList<Clause> resolvents = new ArrayList<>();

        Clause clause1 = clausePair.getClause1();
        Clause clause2 = clausePair.getClause2();

        if (!checkValidity(clause1) || !checkValidity(clause2))
            return resolvents;

        // resolve to nil
        if (clause1.clauseSet.size() == 1 && clause2.clauseSet.size() == 1) {
            String literal1 = clause1.clauseSet.iterator().next();
            String literal2 = clause2.clauseSet.iterator().next();

            if ( (literal1.startsWith("~") && literal1.substring(1).equals(literal2)) ||
                    (literal2.startsWith("~") && literal2.substring(1).equals(literal1)) ) {

                HashSet<String> resolvent = new HashSet<>();
                resolvent.add("NIL");
                Clause resolventClause = new Clause(resolvent);
                resolventClause.setParent1(clause1);
                resolventClause.setParent2(clause2);
                resolvents.add(resolventClause);

                return resolvents;
            }
        }

        for (String literal1 : clause1.clauseSet) {

            for (String literal2 : clause2.clauseSet) {

                if ( (literal1.startsWith("~") && literal1.substring(1).equals(literal2)) ||  (literal2.startsWith("~") && literal2.substring(1).equals(literal1)) ) {

                    HashSet<String> copyClause1 = new HashSet<>(clause1.clauseSet);
                    copyClause1.remove(literal1);
                    HashSet<String> copyClause2 = new HashSet<>(clause2.clauseSet);
                    copyClause2.remove(literal2);
                    HashSet<String> resolvent = new HashSet<>();
                    resolvent.addAll(copyClause1);
                    resolvent.addAll(copyClause2);

                    Clause resolventClause = new Clause(resolvent);
                    resolventClause.setParent1(clause1);
                    resolventClause.setParent2(clause2);
                    resolvents.add(resolventClause);


                }
            }
        }

        return resolvents;
    }

    private void printClauseOrder(Clause nilClause) {

        if(nilClause == null)
            return;

        ArrayList<Clause> initialClausesAndSetOfSupport = new ArrayList<>();
        initialClausesAndSetOfSupport.addAll(initialClauses);
        initialClausesAndSetOfSupport.addAll(setOfSupport);

        Stack<Clause> retraceStack = new Stack<>();
        Stack<Clause> printStack = new Stack<>();

        retraceStack.push(nilClause);
        printStack.push(nilClause);
        while(!retraceStack.isEmpty()) {

            Clause clause = retraceStack.pop();

            if (!(clause.parent1 == null || clause.parent2 == null)) {
                retraceStack.push(clause.parent2);
                retraceStack.push(clause.parent1);
            }

            if (!printStack.contains(clause))
                printStack.push(clause);

        }

        ArrayList<Clause> printList = new ArrayList<>(initialClausesAndSetOfSupport);

        int index = 0;
        Iterator<Clause> initialIterator = printList.iterator();
        while(initialIterator.hasNext()) {
            Clause clause = initialIterator.next();
            if (clause.parent1 == null && clause.parent2 == null && printStack.contains(clause)) {
                System.out.println(++index + ". " + clause);
            } else {
                initialIterator.remove();
            }
        }

        System.out.println("===============");

        boolean removed = true;
        while (removed) {

            removed = false;
            Iterator<Clause> iterator = printStack.iterator();

            while (iterator.hasNext()) {

                Clause clause = iterator.next();

                if (!printList.contains(clause.parent1) || !printList.contains(clause.parent2))
                    continue;

                if (initialClausesAndSetOfSupport.contains(clause)) {
                    iterator.remove();
                    removed = true;
                    continue;
                }

                printList.add(clause);
                iterator.remove();
                removed = true;
            }
        }

        for (Clause clause : printList) {

            if(clause.parent1 == null || clause.parent2 == null)
                continue;

            int indexOfParent1 = printList.indexOf(clause.parent1) + 1;
            int indexOfParent2 = printList.indexOf(clause.parent2) + 1;

            System.out.println( (printList.indexOf(clause) + 1) + ". " + clause + " ("
                    + Math.min(indexOfParent1, indexOfParent2) + ", "
                    + Math.max(indexOfParent1, indexOfParent2) + ")");

        }

        System.out.println("===============");

    }

    public void runAssistant(String clausesPath, String inputsPath) throws IOException {

        argumentsInit(clausesPath);

        BufferedReader reader = new BufferedReader(new FileReader(inputsPath));

        String line = reader.readLine();
        while (line != null) {

            if (line.startsWith("#")) {
                line = reader.readLine();
                continue;
            }

            line = line.toLowerCase();
            System.out.println("User's command: " + line);

            if (line.contains("+")) {

                String[] clauseAndCommand = line.split(" \\+");
                String[] literals = clauseAndCommand[0].split(" v ");
                Clause inputClause = new Clause(new HashSet<>(Arrays.asList(literals)));
                initialClauses.add(inputClause);
                System.out.println("Added " + clauseAndCommand[0] + "\n");

            } else if (line.contains("-")) {

                String[] clauseAndCommand = line.split(" -");
                String[] literals = clauseAndCommand[0].split(" v ");
                Clause inputClause = new Clause(new HashSet<>(Arrays.asList(literals)));
                initialClauses.remove(inputClause);
                System.out.println("Removed " + clauseAndCommand[0] + "\n");

            } else if (line.contains("?")) {

                String[] clauseAndCommand = line.split(" \\?");
                String[] literals = clauseAndCommand[0].split(" v ");
                Clause inputClause = new Clause(new HashSet<>(Arrays.asList(literals)));
                goalInit(inputClause);
                runResolution();
                System.out.println();

            }

            line = reader.readLine();
        }

    }

    public void runResolution() {

        ArrayList<Clause> initialClausesCopy = new ArrayList<>(initialClauses);
        ArrayList<Clause> newlyDerivedClauses = new ArrayList<>(setOfSupport); // initially set-of-support
        HashSet<String> nilClauseSet = new HashSet<>();
        nilClauseSet.add("NIL");
        Clause nilClause = new Clause(nilClauseSet);

        while(true) {
            for (ClausePair clausePair : generateClausePairs(initialClausesCopy, newlyDerivedClauses)){
                ArrayList<Clause> resolvents = resolve(clausePair);

                if (resolvents.size() == 0)
                    continue;

                if (resolvents.contains(nilClause)) {
                    printClauseOrder(resolvents.get(resolvents.indexOf(nilClause)));
                    System.out.println("[CONCLUSION]: " + goal + " is true");
                    return;
                }

                for (Clause clause : resolvents) {
                    if (!newlyDerivedClauses.contains(clause) && checkValidity(clause))
                        newlyDerivedClauses.add(clause);
                }

            }

            if (initialClausesCopy.containsAll(newlyDerivedClauses)) {
                System.out.println("[CONCLUSION]: " + goal + " is unknown");
                return;
            }

            for (Clause clause : newlyDerivedClauses) {

                if (!initialClausesCopy.contains(clause)  && checkValidity(clause)) {
                    resolveRedundancy(initialClausesCopy, clause);
                    initialClausesCopy.add(clause);
                }
            }

        }
    }

}
