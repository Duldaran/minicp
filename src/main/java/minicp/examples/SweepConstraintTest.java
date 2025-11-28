package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.constraints.TwinMod;
import minicp.engine.constraints.AbsoluteAboveEqualVarSub;
import minicp.engine.constraints.AllDifferentVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;

import java.util.ArrayList;

import static minicp.cp.Factory.*;
import static minicp.cp.BranchingScheme.firstFail;

public class SweepConstraintTest {
    public void testForbiddenRegion() {
        Solver cp = Factory.makeSolver(false);

        IntVar x = makeIntVar(cp, 0, 10);
        IntVar y = makeIntVar(cp, 0, 10);

        // Create and post the concrete TwinMod constraint so we can call getForbiddenPairs()
        //TwinMod c = new TwinMod(x, y, 2, 1, 1, 0);
        //AbsoluteAboveEqualVarSub c = new AbsoluteAboveEqualVarSub(x, y, 4);
        AllDifferentVar c = new AllDifferentVar(x, y);
        cp.post(c);

        // Use current domains to size the grid
        int minX = x.min();
        int maxX = x.max();
        int minY = y.min();
        int maxY = y.max();

        int min = Math.min(minX, minY);
        int max = Math.max(maxX, maxY);

        int n = max - min + 1;

        // Grid to record which (x,y) pairs are allowed by the constraint
        final boolean[][] allowed = new boolean[n][n];

        // Start with everything allowed
        for (int xv = min; xv <= max; xv++) {
            for (int yv = min; yv <= max; yv++) {
                allowed[xv - min][yv - min] = true;
            }
        }

        // Get forbidden pairs directly from the constraint
        ArrayList<Integer[]> forbiddenPairs = c.getForbiddenPairs();
        for (Integer[] pair : forbiddenPairs) {
            int xv = pair[0];
            int yv = pair[1];
            if (xv >= min && xv <= max && yv >= min && yv <= max) {
                allowed[xv - min][yv - min] = false;
            }
        }

        // Print header
        System.out.println("   ");
        System.out.println("Allowed region for constraint(x, y)");
        System.out.println("O = allowed (solution), X = forbidden (no solution)");
        System.out.println();

        // y-axis labels
        System.out.print("    y=");
        for (int yv = min; yv <= max; yv++) {
            System.out.print(" " + yv);
        }
        System.out.println();

        // separator line
        System.out.print("   ");
        for (int i = 0; i < 2 * n + 1; i++) {
            System.out.print("-");
        }
        System.out.println();

        // Print grid: rows = x, columns = y
        for (int xv = min; xv <= max; xv++) {
            System.out.print("x=" + xv + " | ");
            for (int yv = min; yv <= max; yv++) {
                char cPlot = allowed[xv - min][yv - min] ? 'O' : 'X';
                System.out.print(cPlot + " ");
            }
            System.out.println();
        }
        System.out.print("   ");
        System.out.println();

        // ------------------------------------------------------
        // Search part: enumerate all solutions and collect stats
        // ------------------------------------------------------

        // Simple first-fail branching on (x, y)
        DFSearch dfs = makeDfs(cp, firstFail(new IntVar[]{x, y}));

        // Optional: print each solution
        dfs.onSolution(() -> {
            System.out.println("Solution found: x=" + x.min() + ", y=" + y.min());
        });

        // Run the full search and get statistics
        SearchStatistics stats = dfs.solve();

        // Print statistics
        System.out.println();
        System.out.println("Search statistics:");
        System.out.println("  #solutions = " + stats.numberOfSolutions());
        System.out.println("  #nodes     = " + stats.numberOfNodes());
        System.out.println("  #failures  = " + stats.numberOfFailures());
        System.out.println("  raw stats  = " + stats);
    }

    public static void main(String[] args) {
        new SweepConstraintTest().testForbiddenRegion();
    }
}
