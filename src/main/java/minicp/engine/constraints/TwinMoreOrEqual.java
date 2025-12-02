package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.ForbiddenRegion;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.exception.InconsistencyException;

import static minicp.cp.Factory.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TwinLessOrEqual
 */
public class TwinMoreOrEqual extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int cofX;
    private final int cofY;
    private final int result;

    public TwinMoreOrEqual(IntVar x, IntVar y, int cofX, int cofY, int result) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.cofX = cofX;
        this.cofY = cofY;
        this.result = result;
        if (cofX == 0 && cofY == 0)
            throw new IllegalArgumentException("at least one coefficient must be non-zero");
    }

    @Override
    public void post() {

        y.propagateOnBoundChange(this);
        x.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        if(cofX > 0 && cofY > 0) {
            x.removeBelow(Math.ceilDiv(result - cofY * y.max(), cofX));
            y.removeBelow(Math.ceilDiv(result - cofX * x.max(), cofY));
        }
        else if(cofX < 0 && cofY < 0) {
            x.removeAbove(Math.floorDiv(result - cofY * y.min(), cofX));
            y.removeAbove(Math.floorDiv(result - cofX * x.min(), cofY));
        } 
        else if (cofX > 0 && cofY < 0) {
            x.removeBelow(Math.ceilDiv(result - cofY * y.min(), cofX));
            y.removeAbove(Math.floorDiv(result - cofX * x.max(), cofY));
        } 
        else if (cofX < 0 && cofY > 0) {
            x.removeAbove(Math.floorDiv(result - cofY * y.max(), cofX));
            y.removeBelow(Math.ceilDiv(result - cofX * x.min(), cofY));
        }
        
    }


    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if((cofX * vx + cofY * vy) < result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        return pairs;
    }


    public List<ForbiddenRegion> getForbiddenRegions() {
        List<ForbiddenRegion> regions = new ArrayList<>();
        
        int minX = x.min();
        int maxX = x.max();
        int minY = y.min();
        int maxY = y.max();
        
        // For each x value, determine which y values violate X + Y = 2
        for(int vx = x.min(); vx <= x.max(); vx++){
            int validY = Math.ceilDiv(result - cofX * vx, cofY);  // The only y value that satisfies x + y = 2
            
            // Create forbidden region for y < validY
            if (minY < validY) {
                regions.add(new ForbiddenRegion(vx, vx, minY, Math.min(validY - 1, maxY)));
            }
        }
        
        return regions;
    }
    
    /**
     * Primitive: Get first forbidden region (ordered by x, then y)
     */
    public ForbiddenRegion getFirstForbiddenRegion() {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        if (regions.isEmpty()) {
            return null;
        }
        
        // Sort by infX, then infY
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        return regions.get(0);
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
        return (cofX * x + cofY * y) < result;
    }
}
