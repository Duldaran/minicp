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

public class SweepConstraintExample2_naive {
    public void main() {
        Solver cp = Factory.makeSolver();
        
        final boolean printForbidden = false;

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

        ArrayList<AbstractConstraint> constraints = new ArrayList<>(Arrays.asList(D, D2, D3, E, C));
        // Filter the domains by propagating all constraints
        for (AbstractConstraint c : constraints) {
            cp.post(c);
        }

        // Store filtered values in data structures
        ArrayList<Integer> filteredX = new ArrayList<>();
        ArrayList<Integer> filteredY = new ArrayList<>();

        for (int v = minX; v <= maxX; v++) {
            if (!x.contains(v)) {
            filteredX.add(v);
            }
        }
        
        for (int v = minY; v <= maxY; v++) {
            if (!y.contains(v)) {
            filteredY.add(v);
            }
        }

        int min = Math.min(minX, minY);
        int max = Math.max(maxX, maxY);

        int n = max - min + 1;

        final boolean[][] allowed = new boolean[n][n];
        // Start with everything allowed
        for (int xv = min; xv <= max; xv++) {
            if(filteredX.contains(xv))
                continue;
            Arrays.fill(allowed[xv - min], true);
            for(int filtered : filteredY) {
                allowed[xv - min][filtered - min] = false;
            }
        }

        for (AbstractConstraint c : constraints) {
            ArrayList<Integer[]> forbiddenPairs = c.getForbiddenPairs();
            if(printForbidden) {
                final boolean[][] temp_allowed = new boolean[n][n];
                for (int xv = min; xv <= max; xv++) {
                    if(filteredX.contains(xv))
                        continue;
                    Arrays.fill(temp_allowed[xv - min], true);
                    for(int filtered : filteredY) {
                        temp_allowed[xv - min][filtered - min] = false;
                    }
                }
                for (Integer[] pair : forbiddenPairs) {
                    int xv = pair[0];
                    int yv = pair[1];
                    if (xv >= min && xv <= max && yv >= min && yv <= max) {
                        temp_allowed[xv - min][yv - min] = false;
                    }
                }
            }
            for (Integer[] pair : forbiddenPairs) {
                int xv = pair[0];
                int yv = pair[1];
                if (xv >= min && xv <= max && yv >= min && yv <= max) {
                    allowed[xv - min][yv - min] = false;
                }
            }
        }

        ArrayList<Integer> newFilteredX = new ArrayList<>();
        ArrayList<Integer> newFilteredY = new ArrayList<>();

        for(int xv = min; xv <= max; xv++) {
            boolean allForbidden = true;
            for (int yv = min; yv <= max; yv++) {
                if (allowed[xv - min][yv - min]) {
                    allForbidden = false;
                    break;
                }
            }
            if (allForbidden && !filteredX.contains(xv)) {
                newFilteredX.add(xv);
            }
        }

        for(int yv = min; yv <= max; yv++) {
            boolean allForbidden = true;
            for (int xv = min; xv <= max; xv++) {
                if (allowed[xv - min][yv - min]) {
                    allForbidden = false;
                    break;
                }
            }
            if (allForbidden && !filteredY.contains(yv)) {
                newFilteredY.add(yv);
            }
        }

        if(printForbidden) {
            // Print header
            System.out.println("   ");
            System.out.println("Allowed region for sweep line (x, y)");
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
        }

        filteredX.addAll(newFilteredX);
        filteredY.addAll(newFilteredY);

        for (int xv : newFilteredX) {
            System.out.print(xv + " ");
            x.remove(xv);
        }
        for (int yv : newFilteredY) {
            System.out.print(yv + " ");
            y.remove(yv);
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

        new SweepConstraintExample2_naive().main();

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.printf("Temps d'exécution total : %.3f ms%n", millis);
    }


}
