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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static minicp.cp.Factory.*;
import static minicp.cp.BranchingScheme.firstFail;
import minicp.engine.core.ForbiddenRegion;

public class SweepConstraintExample2 {
    public void testForbiddenRegion() {

        final boolean printForbidden = false;
        final boolean printFiltered = false;

        Solver cp = Factory.makeSolver(false);

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

        int min = Math.min(minX, minY);
        int max = Math.max(maxX, maxY);
        int n = max - min + 1;

        // Sweep-Line Algorithm for conjunction of forbidden regions
        List<ForbiddenRegion> all = new ArrayList<>();
            for (AbstractConstraint c : constraints) {
                all.addAll(c.getForbiddenRegions());
            }
            List<ForbiddenRegion> merged = unionRegions(all);
            if(printForbidden){
            System.out.println("Merged forbidden regions:");
            System.out.println(merged);}
        
        // Start with all pairs currently in domains allowed
        boolean[][] allowed = new boolean[n][n];
        for (int xv = minX; xv <= maxX; xv++) {
            if (!x.contains(xv)) continue;
            for (int yv = minY; yv <= maxY; yv++) {
                if (!y.contains(yv)) continue;
                allowed[xv - min][yv - min] = true;
            }
        }

        // Apply merged forbidden regions: mark those pairs as not allowed
        for (ForbiddenRegion r : merged) {
            int xVal = r.getEventPoint();
            if (xVal < minX || xVal > maxX) continue; // outside current x-domain range

            int fromY = Math.max(r.getInfY(), minY);
            int toY   = Math.min(r.getSupY(), maxY);

            boolean[] row = allowed[xVal - min];
            for (int yv = fromY; yv <= toY; yv++) {
                if (!y.contains(yv)) continue;
                row[yv - min] = false;
            }
        }

        // Prune x: keep only values that have at least one supporting y
        for (int xv = minX; xv <= maxX; xv++) {
            if (!x.contains(xv)) continue;

            boolean hasSupport = false;
            boolean[] row = allowed[xv - min];

            for (int yv = minY; yv <= maxY && !hasSupport; yv++) {
                if (!y.contains(yv)) continue;
                if (row[yv - min]) {
                    hasSupport = true;
                }
            }

            if (!hasSupport) {
                x.remove(xv);
            }
        }

        // Prune y: keep only values that have at least one supporting x
        for (int yv = minY; yv <= maxY; yv++) {
            if (!y.contains(yv)) continue;

            boolean hasSupport = false;
            for (int xv = minX; xv <= maxX && !hasSupport; xv++) {
                if (!x.contains(xv)) continue;
                if (allowed[xv - min][yv - min]) {
                    hasSupport = true;
                }
            }

            if (!hasSupport) {
                y.remove(yv);
            }
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

        new SweepConstraintExample2().testForbiddenRegion();

        long end = System.nanoTime();
        double millis = (end - start) / 1_000_000.0;

        System.out.printf("Temps d'exécution total : %.3f ms%n", millis);
    }


    public static List<ForbiddenRegion> unionRegions(List<ForbiddenRegion> regions) {
        // Group by X = eventPoint
        Map<Integer, List<ForbiddenRegion>> byX = new HashMap<>();
        for (ForbiddenRegion r : regions) {
            byX.computeIfAbsent(r.getEventPoint(), k -> new ArrayList<>()).add(r);
        }

        List<ForbiddenRegion> result = new ArrayList<>();

        for (Map.Entry<Integer, List<ForbiddenRegion>> e : byX.entrySet()) {
            int x = e.getKey();
            List<ForbiddenRegion> list = e.getValue();

            // Sort by y interval
            list.sort(Comparator.comparingInt(ForbiddenRegion::getInfY));

            // Merge overlapping intervals
            int start = list.get(0).getInfY();
            int end   = list.get(0).getSupY();

            for (int i = 1; i < list.size(); i++) {
                ForbiddenRegion r = list.get(i);

                if (r.getInfY() <= end + 1) {
                    // Overlapping or adjacent → extend interval
                    end = Math.max(end, r.getSupY());
                } else {
                    // Disjoint → push current one, start new
                    result.add(new ForbiddenRegion(x, start, end));
                    start = r.getInfY();
                    end   = r.getSupY();
                }
            }

            result.add(new ForbiddenRegion(x, start, end));
        }

        return result;
    }
}
