package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.ForbiddenRegion;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.exception.InconsistencyException;

import static minicp.cp.Factory.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * TwinLessOrEqual
 */
public class TwinLessOrEqual extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int cofX;
    private final int cofY;
    private final int result;

    private int nbPropagate = 0;
    private List<ForbiddenRegion> regions = new ArrayList<>();

    public TwinLessOrEqual(IntVar x, IntVar y, int cofX, int cofY, int result) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.cofX = cofX;
        this.cofY = cofY;
        this.result = result;
        if (cofX == 0 && cofY == 0)
            throw new IllegalArgumentException("at least one coefficient must be non-zero");
        this.regions = computeForbiddenRegions();
    }

    @Override
    public void post() {
        y.propagateOnBoundChange(this);
        x.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        nbPropagate++;
        if (cofY > 0 && cofX > 0) {
            y.removeAbove(Math.floorDiv(result - cofX * x.min(), cofY));
            x.removeAbove(Math.floorDiv(result - cofY * y.min(), cofX));
        } else if (cofY < 0 && cofX < 0) {
            y.removeBelow(Math.ceilDiv(result - cofX * x.max(), cofY));
            x.removeBelow(Math.ceilDiv(result - cofY * y.max(), cofX));
        } else if (cofY > 0 && cofX < 0) {
            y.removeAbove(Math.floorDiv(result - cofX * x.min(), cofY));
            x.removeBelow(Math.ceilDiv(result - cofY * y.max(), cofX));
        } else if (cofY < 0 && cofX > 0) {
            y.removeBelow(Math.ceilDiv(result - cofX * x.max(), cofY));
            x.removeAbove(Math.floorDiv(result - cofY * y.min(), cofX));
        }
        
    }


    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if((cofX * vx + cofY * vy) > result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        return pairs;
    }

    public int getNbPropagate() {
        return nbPropagate;
    }
    public List<ForbiddenRegion> getForbiddenRegions() {
        return regions;
    }
    
    public List<ForbiddenRegion> computeForbiddenRegions() {
        List<ForbiddenRegion> regions = new ArrayList<>();
        
        int minY = y.min();
        int maxY = y.max();
        
        // For each x value, determine which y values violate X + Y = 2
        for(int vx = x.min(); vx <= x.max(); vx++){
            if (x.contains(vx)) {
                int validY = Math.floorDiv(result - cofX * vx, cofY);  // The only y value that satisfies x + y = 2
                
                // Create forbidden region for y < validY
                if (maxY > validY) {
                    regions.add(new ForbiddenRegion(vx, Math.max(validY + 1, minY), maxY));
                }
            }
        }
        return regions;
    }
    
    /**
     * Primitive: Get first forbidden region (ordered by x, then y)
     */
    public Map.Entry<Integer, List<ForbiddenRegion>> getFirstForbiddenRegions(){        
        if (regions.isEmpty()) {
            return null;
        }
        // Sort by infX, then infY
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getEventPoint)
                              .thenComparingInt(ForbiddenRegion::getInfY));

        int starting_idx = regions.get(0).getEventPoint();
        List<ForbiddenRegion> startingRegions = new ArrayList<>();
        for (ForbiddenRegion r : regions) {
            if (r.getEventPoint() == starting_idx) {
                startingRegions.add(r);
            }
            else {
                break;
            }
        }
        return new AbstractMap.SimpleEntry<>(starting_idx, startingRegions);
    }
    
    /**
     * Primitive: Get next forbidden region after 'previous'
     */
    public ForbiddenRegion getNextForbiddenRegion(ForbiddenRegion previous) {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        int index = regions.indexOf(previous);
        if (index >= 0 && index + 1 < regions.size()) {
            return regions.get(index + 1);
        }
        
        return null;
    }
    
    /**
     * Primitive: Get last forbidden region
     */
    public ForbiddenRegion getLastForbiddenRegion() {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        if (regions.isEmpty()) {
            return null;
        }
        
        // Sort by infX, then infY
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        return regions.get(regions.size() - 1);
    }
    
    /**
     * Primitive: Get previous forbidden region before 'next'
     */
    public ForbiddenRegion getPrevForbiddenRegion(ForbiddenRegion next) {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        int index = regions.indexOf(next);
        if (index > 0) {
            return regions.get(index - 1);
        }
        
        return null;
    }
    
    /**
     * Primitive: Check if point (x,y) is in any forbidden region
     */
    public boolean checkIfInForbiddenRegion(int x, int y) {
        return (cofX * x + cofY * y) > result;
    }
}


