package minicp.engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Sweep-based pruning algorithm for conjunction of constraints
 */
 public class SweepPruner {
    
    /**
     * Prune domain of X based on conjunction of constraints
     * Returns the set of X values that should be removed
     */
    public static Set<Integer> pruneX(IntVar x, IntVar y, 
                                       List<Constraint> constraints) {
        Set<Integer> toPrune = new TreeSet<>();
        
        // Step 1: Collect all forbidden regions from all constraints
        List<ForbiddenRegion> allRegions = new ArrayList<>();
        for (Constraint constraint : constraints) {
            allRegions.addAll(constraint.getForbiddenRegions());
        }
        
        
        Collections.sort(allRegions, (r1, r2) -> Integer.compare(r1.getInfX(), r2.getInfX()));

        
        // Step 2: Process each X value in domain
        int regionsIdx = 0;
        for (int xVal = x.min(); xVal <= x.max(); xVal++) {
            IntervalSet status = new IntervalSet();
            // Update sweep line status: process all events at this x
            while (regionsIdx < allRegions.size() && allRegions.get(regionsIdx).getInfX() == xVal) {
                ForbiddenRegion region = allRegions.get(regionsIdx);
                status.add(region.getInfY(), region.getSupY());
                regionsIdx++;
            }
            
            // Check if all Y values are covered
            if (status.fullyCovers(y)) {
                toPrune.add(xVal);
            } 
        }
        
        return toPrune;
    }
    
    /**
     * Adjust minimum of X (remove values from left until a valid one is found)
     */
    public static boolean adjustMinX(IntVar x, IntVar y,
                                      List<Constraint> constraints) {
        Set<Integer> toPrune = new TreeSet<>();
        
        for (int xVal = x.min(); xVal <= x.max(); xVal++) {
            // Check if this x has any valid y
            boolean hasValidY = false;
            for (int yVal = y.min(); yVal <= y.max(); yVal++) {
                boolean forbidden = false;
                for (Constraint c : constraints) {
                    for (ForbiddenRegion r : c.getForbiddenRegions()) {
                        if (r.contains(xVal, yVal)) {
                            forbidden = true;
                            break;
                        }
                    }
                    if (forbidden) break;
                }
                if (!forbidden) {
                    hasValidY = true;
                    break;
                }
            }
            
            if (!hasValidY) {
                toPrune.add(xVal);
            } else {
                break;  // Found first valid X, stop
            }
        }
        
        for (int xVal : toPrune) {
            x.remove(xVal);
        }
        
        return !toPrune.isEmpty();
    }
    

    private static class IntervalSet {
        private final TreeMap<Integer, Integer> map = new TreeMap<>();
        private long covered = 0;  // total covered length
        
        public void add(int start, int end) {
            if (start > end) return;

            // find potential merge neighbors
            Integer left = map.floorKey(start);
            if (left != null && map.get(left) >= start - 1) {
                start = Math.min(start, left);
                end   = Math.max(end, map.get(left));
                covered -= (map.get(left) - left + 1);
                map.remove(left);
            }

            Integer right = map.ceilingKey(start);
            while (right != null && right <= end + 1) {
                end = Math.max(end, map.get(right));
                covered -= (map.get(right) - right + 1);
                map.remove(right);
                right = map.ceilingKey(start);
            }

            // insert merged
            map.put(start, end);
            covered += (end - start + 1);
        }
        
        public boolean fullyCovers(IntVar y) {
            return covered >= (y.max() - y.min() + 1);
        }
    }
}
