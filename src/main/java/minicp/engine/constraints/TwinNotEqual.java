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
 * TwinEqual
 */
public class TwinNotEqual extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int cofX;
    private final int cofY;
    private final int result;

    public TwinNotEqual(IntVar x, IntVar y, int cofX, int cofY, int result) {
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
        x.propagateOnFix(this);
        y.propagateOnFix(this);
        propagate();
    }

    @Override
    public void propagate() {
        if(x.isFixed()) {
            int vx = x.min();
            int bannedY = result - cofX * vx;
            if (cofY == 0 && bannedY == 0) {
                throw new InconsistencyException();
            }
            if (cofY != 0 && bannedY % cofY == 0) {
                int valueToRemove = bannedY / cofY;
                if (y.contains(valueToRemove)) {
                    y.remove(valueToRemove);
                }
            }
        }
        else if(y.isFixed()) {
            int vy = y.min();
            int bannedX = result - cofY * vy;
            if (cofX == 0 && bannedX == 0) {
                throw new InconsistencyException();
            }
            if (cofX != 0 && bannedX % cofX == 0) {
                int valueToRemove = bannedX / cofX;
                if (x.contains(valueToRemove)) {
                    x.remove(valueToRemove);
                }
            }
        }

    }


    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if((cofX * vx + cofY * vy) == result) {
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
            double invalidY = (double)(result - cofX * vx) / cofY;  // The only y value that satisfies x + y = 2
            if (invalidY % 1 != 0) {
                // invalidY is not an integer, so no y values are forbidden for this x
                continue;
            }
            int invalidYInt = (int) invalidY;
            // Create forbidden region for y < validY
            if (maxY >= invalidYInt && invalidYInt >= minY) {
                regions.add(new ForbiddenRegion(vx, vx,  invalidYInt , invalidYInt));
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
        return (cofX * x + cofY * y) == result;
    }
}
