package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.constraints.TwinMod;
import minicp.engine.constraints.TwinMoreOrEqual;
import minicp.engine.constraints.AbsoluteAboveEqualVarSub;
import minicp.engine.constraints.AllDifferentVar;
import minicp.engine.constraints.TwinLessOrEqual;
import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;

import java.util.ArrayList;
import java.util.Arrays;

import static minicp.cp.Factory.*;
import static minicp.cp.BranchingScheme.firstFail;

public class ConstraintExample2 {
    public void main() {
        
        final boolean printFiltered = false;

        Solver cp = Factory.makeSolver();

        IntVar x = makeIntVar(cp, 0, 20);
        IntVar y = makeIntVar(cp, 0, 20);

        // Use initial domains to size the grid
        int minX = x.min();
        int maxX = x.max();
        int minY = y.min();
        int maxY = y.max();

        TwinMoreOrEqual D2 = new TwinMoreOrEqual(x, y, -1, 1, -1);
        TwinMoreOrEqual D3 = new TwinMoreOrEqual(x, y, 1, -1, -1);
        TwinMoreOrEqual D = new TwinMoreOrEqual(x, y, 1, 3, 20);
        TwinMod E = new TwinMod(x, y, 3, 2, 1, 2);
        TwinLessOrEqual C = new TwinLessOrEqual(x, y, 1, 1, 21);

        ArrayList<AbstractConstraint> constraints = new ArrayList<>(Arrays.asList(D2, D3, D, E, C));
        
        // Filter the domains by propagating all constraints
        for (AbstractConstraint c : constraints) {
            cp.post(c);
        }

        if(printFiltered){
                // Store filtered values in data structures
                ArrayList<Integer> filteredX = new ArrayList<>();
                ArrayList<Integer> filteredY = new ArrayList<>();
                System.out.println("\nValues removed after constraint propagation:");
                System.out.print("x domain: {");
                for (int v = minX; v <= maxX; v++) {
                    if (!x.contains(v)) {
                    filteredX.add(v);
                    System.out.print(v + " ");
                    }
                }
                System.out.println("}");
                
                System.out.print("y domain: {");
                for (int v = minY; v <= maxY; v++) {
                    if (!y.contains(v)) {
                    filteredY.add(v);
                    System.out.print(v + " ");
                    }
                }
                System.out.println("}");
                System.out.println();
        }
        
        // ------------------------------------------------------
        // Search part: enumerate all solutions and collect stats
        // ------------------------------------------------------

        // Simple first-fail branching on (x, y)
        DFSearch dfs = makeDfs(cp, firstFail(new IntVar[]{x, y}));

        long start = System.nanoTime();
        SearchStatistics stats = dfs.solve();
        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        // Print statistics
        System.out.println();
        System.out.println("Search statistics:");
        System.out.println("  #solutions = " + stats.numberOfSolutions());
        System.out.println("  #nodes     = " + stats.numberOfNodes());
        System.out.println("  #failures  = " + stats.numberOfFailures());
        System.out.println("  raw stats  = " + stats);
        System.out.printf("Temps d'exécution de la recherche : %.3f ms%n", millis);

        System.out.println();
        System.out.println("Nb appels à propagate() :");
        for (AbstractConstraint c : constraints) {
            System.out.println(c + " : " + c.getNbPropagate());
        }

    }
    public static void main(String[] args) {
        long start = System.nanoTime();

        new ConstraintExample2().main();

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.printf("Temps d'exécution total : %.3f ms%n", millis);
    }

}
