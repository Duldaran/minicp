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

public class ConstraintExample3 {
    public void main() {
        Solver cp = Factory.makeSolver();

        IntVar x = makeIntVar(cp, 0, 200);
        IntVar y = makeIntVar(cp, 0, 200);

        AbsoluteAboveEqualVarSub B = new AbsoluteAboveEqualVarSub(x, y,1,1, 50);
        TwinLessOrEqual C = new TwinLessOrEqual(x, y, 1, 1, 200);
        TwinMoreOrEqual D = new TwinMoreOrEqual(x, y, -1, 2, -1);
        TwinMoreOrEqual D2 = new TwinMoreOrEqual(x, y, 2, -1, -1);
        TwinMoreOrEqual D3 = new TwinMoreOrEqual(x, y, 3, 4, 500);
        TwinMod E = new TwinMod(x, y, 3, 1, 2, 2);

        ArrayList<AbstractConstraint> constraints = new ArrayList<>(Arrays.asList( B, C, E, D3, D2, D));
        for (AbstractConstraint c : constraints) {
            cp.post(c);
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

        new ConstraintExample3().main();

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.printf("Temps d'exécution total : %.3f ms%n", millis);
    }

}
