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
 * |x - y| >= k   (pairs with |x - y| < k are forbidden)
 */
public class AbsoluteAboveEqualVarSub extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int cofX;
    private final int cofY;
    private final int result;
    
    private int nbPropagate = 0;

    /**
     * Creates the constraint |x - y| >= k
     *
     * @param x the first variable
     * @param y the second variable
     * @param k the strict lower bound on |x - y|
     */
    public AbsoluteAboveEqualVarSub(IntVar x, IntVar y, int cofX, int cofY, int result) {
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
        nbPropagate++;
        if (x.isFixed()) {
            for (int vx = x.min(); vx <= x.max(); vx++) {
                if (x.contains(vx)) {
                    for (int vy = y.min(); vy <= y.max(); vy++) {
                        if (y.contains(vy)) {
                            if (Math.abs(vx*cofX - vy*cofY) < result) {
                                y.remove(vy);
                            }
                        }
                    }
                }
            }
        }
        if (y.isFixed()) {
            for (int vy = y.min(); vy <= y.max(); vy++) {
                if (y.contains(vy)) {
                    for (int vx = x.min(); vx <= x.max(); vx++) {
                        if (x.contains(vx)) {
                            if (Math.abs(vx*cofX - vy*cofY) < result) {
                                x.remove(vx);
                            }
                        }
                    }
                }
            }
            this.setActive(false);
        }
    }
    public int getNbPropagate() {
        return nbPropagate;
    }
    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();

        for (int vx = x.min(); vx <= x.max(); vx++) {
            if (x.contains(vx)) {
                for (int vy = y.min(); vy <= y.max(); vy++) {
                    if (y.contains(vy)) {
                        if (Math.abs(vx*cofX - vy*cofY) < result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        

        return pairs;
    }

    public List<ForbiddenRegion> getForbiddenRegionStart() {
        int maxX = Math.floorDiv(cofY*y.max() + result, cofX);
        int minX = Math.ceilDiv(cofY*y.min() - result, cofX);

        ForbiddenRegion region = new ForbiddenRegion(Math.max(minX, x.min()), Math.min(maxX, x.max()), y.min(), y.max());
        return List.of(region);
    }

    public List<ForbiddenRegion> getForbiddenRegions(int x) {
        List<ForbiddenRegion> regions = new ArrayList<>();
        
        int minY = y.min();
        int maxY = y.max();
    
        int minDisallowedY = Math.ceilDiv(cofX*x - result, cofY);
        int maxDisallowedY = Math.floorDiv(cofX*x + result, cofY);

        int lowerBound = Math.max(minDisallowedY, minY);
        int upperBound = Math.min(maxDisallowedY, maxY);
        if (lowerBound <= upperBound) {
            regions.add(new ForbiddenRegion(x, x, lowerBound, upperBound));
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
        return Math.abs(x*cofX - y*cofY) < result;
    }
}