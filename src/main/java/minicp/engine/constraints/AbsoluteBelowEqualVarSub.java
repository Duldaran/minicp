package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.ForbiddenRegion;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Absolute value constraint
 * Enforces |x - y| <= k  (pairs with |x - y| > k are forbidden)
 */
public class AbsoluteBelowEqualVarSub extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int cofX;
    private final int cofY;
    private final int result;

    /**
     * Creates the constraint |x - y| <= k.
     *
     * @param x first variable
     * @param y second variable
     * @param k the maximum allowed distance |x - y|
     */
    public AbsoluteBelowEqualVarSub(IntVar x, IntVar y, int cofX, int cofY, int result) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.cofX = cofX;
        this.cofY = cofY;
        this.result = result;
        if (cofX == 0 && cofY == 0)
            throw new IllegalArgumentException("at least one coefficient must be non-zero");
        if (cofX < 0 || cofY < 0)
            throw new IllegalArgumentException("coefficients must be positive");
        if (result < 0)
            throw new IllegalArgumentException("result must be non-negative");
    }

    @Override
    public void post() {
        // React whenever the domain of x or y changes
        x.propagateOnDomainChange(this);
        y.propagateOnDomainChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        if (x.isFixed()) {
            for (int vx = x.min()*cofX; vx <= x.max()*cofX; vx++) {
                if (x.contains(vx)) {
                    for (int vy = y.min()*cofY; vy <= y.max()*cofY; vy++) {
                        if (y.contains(vy)) {
                            if (Math.abs(vx*cofX - vy*cofY) > result) {
                                y.remove(vy);
                            }
                        }
                    }
                }
            }
        }
        if (y.isFixed()) {
            for (int vy = y.min()*cofY; vy <= y.max()*cofY; vy++) {
                if (y.contains(vy)) {
                    for (int vx = x.min()*cofX; vx <= x.max()*cofX; vx++) {
                        if (x.contains(vx)) {
                            if (Math.abs(vx*cofX - vy*cofY) > result) {
                                x.remove(vx);
                            }
                        }
                    }
                }
            }
        }
    }

    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();

        for (int vx = x.min(); vx <= x.max(); vx++) {
            if (x.contains(vx)) {
                for (int vy = y.min(); vy <= y.max(); vy++) {
                    if (y.contains(vy)) {
                        if (Math.abs(vx*cofX - vy*cofY) > result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        for (int vy = y.min(); vy <= y.max(); vy++) {
            if (y.contains(vy)) {
                for (int vx = x.min(); vx <= x.max(); vx++) {
                    if (x.contains(vx)) {
                        if (Math.abs(vx*cofX - vy*cofY) > result) {
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
        
        for (int x = minX; x <= maxX; x++) {
            // Allowed range: x - 2 <= y <= x + 2
            int minAllowedY = Math.ceilDiv(cofX*x - result, cofY);
            int maxAllowedY = Math.floorDiv(cofX*x + result, cofY);
            
            // Lower forbidden region: y < x - 2
            if (minY < minAllowedY) {
                int upperBound = Math.min(minAllowedY - 1, maxY);
                if (minY <= upperBound) {
                    regions.add(new ForbiddenRegion(x, x, minY, upperBound));
                }
            }
            
            // Upper forbidden region: y > x + 2
            if (maxY > maxAllowedY) {
                int lowerBound = Math.max(maxAllowedY + 1, minY);
                if (lowerBound <= maxY) {
                    regions.add(new ForbiddenRegion(x, x, lowerBound, maxY));
                }
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
        return Math.abs(x*cofX - y*cofY) > result;
    }
}