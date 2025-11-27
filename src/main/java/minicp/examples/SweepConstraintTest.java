package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;


public class SweepConstraintTest {
    public void testForbiddenRegion() {
        Solver cp = Factory.makeSolver(false);

        IntVar x = makeIntVar(cp, 0, 4);
        IntVar y = makeIntVar(cp, 0, 4);

        IntVar D = makeIntVar(cp, -4, 4);
        cp.post(difference(x, y, D));

        // Post your constraint here
        // cp.post(allDifferentVar(x, y));
        //cp.post(absoluteAboveEqual(D, 2));
        // cp.post(absoluteBelowEqual(D, 2));
        cp.post(absoluteAboveEqualVarSub(x, y, 3));
        
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

        // Simple search over (x, y)
        DFSearch search = makeDfs(cp, firstFail(x, y));

        search.onSolution(() -> {
            int xv = x.min();
            int yv = y.min();
            allowed[xv - min][yv - min] = true;
        });

        search.solve();

        // Print header
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
                char c = allowed[xv - min][yv - min] ? 'O' : 'X';
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new SweepConstraintTest().testForbiddenRegion();
    }
}
