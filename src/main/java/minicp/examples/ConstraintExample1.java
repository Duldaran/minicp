package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.constraints.TwinMod;
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

public class ConstraintExample1 {
    public void testForbiddenRegion() {

        final boolean printFiltered = true;
        Solver cp = Factory.makeSolver(false);

        IntVar x = makeIntVar(cp, 0, 4);
        IntVar y = makeIntVar(cp, 0, 4);

        // Use initial domains to size the grid
        int minX = x.min();
        int maxX = x.max();
        int minY = y.min();
        int maxY = y.max();

        AbsoluteAboveEqualVarSub B = new AbsoluteAboveEqualVarSub(x, y, 1,1,3);
        AllDifferentVar A = new AllDifferentVar(x, y);
        TwinLessOrEqual C = new TwinLessOrEqual(x, y, 1, 2, 6);
        TwinMod E = new TwinMod(x, y, 2, 1, 1, 0);

        ArrayList<AbstractConstraint> constraints = new ArrayList<>(Arrays.asList( B, A, C, E ));

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

        System.out.println();
        System.out.println("Search statistics:");
        System.out.println("  #solutions = " + stats.numberOfSolutions());
        System.out.println("  #nodes     = " + stats.numberOfNodes());
        System.out.println("  #failures  = " + stats.numberOfFailures());
        System.out.println("  raw stats  = " + stats);
        System.out.printf("Temps d'exécution de la recherche : %.3f ms%n", millis);

        System.out.println();
        System.out.println("Nb appels à propagate() :");
        System.out.println("  AllDifferentVar : " + A.getNbPropagate());
        System.out.println("  AbsoluteAboveEqualVarSub : " + B.getNbPropagate());
        System.out.println("  TwinLessOrEqual : " + C.getNbPropagate());
        System.out.println("  TwinMod : " + E.getNbPropagate());
    }

    public static void main(String[] args) {
        long start = System.nanoTime();

        new ConstraintExample1().testForbiddenRegion();

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.printf("Temps d'exécution total : %.3f ms%n", millis);
    }
}
