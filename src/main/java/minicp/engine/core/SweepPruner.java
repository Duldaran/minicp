package minicp.engine.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        
        List<Event> events = new ArrayList<>();
        for (Constraint c : constraints) {
            List<ForbiddenRegion> regionsX = c.getForbiddenRegionStart();
            for (ForbiddenRegion region : regionsX) {
                events.add(new Event(region.getInfX(), Type.START, c));
                events.add(new Event(region.getSupX() + 1, Type.END, c));
            }
        }
        events.sort(Comparator.comparingInt(e -> e.x));
        int eventIndex = 0;

        List<Constraint> activeConstraints = new ArrayList<>();
        for (int xVal = x.min(); xVal <= x.max(); xVal++) {
            while (eventIndex < events.size() && events.get(eventIndex).x == xVal) {
                Event e = events.get(eventIndex);
                if (e.type == Type.START) {
                    activeConstraints.add(e.getConstraint());
                } else {
                    activeConstraints.remove(e.getConstraint());
                }
                eventIndex++;
            }
            List<Interval> status = new ArrayList<>();
            for (Constraint c : activeConstraints) {
                List<ForbiddenRegion> regions = c.getForbiddenRegions(xVal);
                for (ForbiddenRegion r : regions) {
                    status.add(new Interval(r.getInfY(), r.getSupY()));
                }
            }
            
            if (fullyCovers(y, status)) {
                toPrune.add(xVal);
            } 
        }
        
        return toPrune;
    }
    
    private static class Event {
        int x;
        Type type;
        Constraint constraint;
        
        Event(int x, Type type, Constraint constraint) {
            this.x = x;
            this.type = type;
            this.constraint = constraint;
        }

        public Constraint getConstraint() {
            return constraint;
        }
    }

    private enum Type {
        START,
        END
    }
    

    private static boolean fullyCovers(IntVar y, List<Interval> segs) {
        if (segs.isEmpty()) return false;
        segs.sort((a,b) -> Integer.compare(a.y1, b.y1));

        int current = y.min();
        for (Interval s : segs) {
            if (s.y1 > current) 
                return false;             
            current = Math.max(current, s.y2 + 1);
            if (current > y.max())
                return true;              
        }
        return current > y.max();
    }

    
}
