

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
 * TwinMod
 */
public class TwinMod extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int mod;
    private final int cofX;
    private final int cofY;
    private final int result;

    private int nbPropagate = 0;
    private List<ForbiddenRegion> regions = new ArrayList<>();  

    public TwinMod(IntVar x, IntVar y, int mod, int cofX, int cofY, int result) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.mod = mod;
        this.cofX = cofX;
        this.cofY = cofY;
        this.result = result;
        if (result < 0)
            throw new IllegalArgumentException("result must be non-negative");
        if (mod <= 0)
            throw new IllegalArgumentException("mod must be positive");
        if (cofX == 0 && cofY == 0)
            throw new IllegalArgumentException("at least one coefficient must be non-zero");
        if (result < 0 || result >= mod)
            throw new IllegalArgumentException("result must be in [0," + (mod - 1) + "]");
        this.regions = computeForbiddenRegions();
    }

    @Override
    public void post() {
        x.propagateOnFix(this);
        y.propagateOnFix(this);
        propagate();
    }

    @Override
    public void propagate() {
        nbPropagate++;
        if(x.isFixed()) {
            int vx = x.min();
            int rhs = (result - (cofX * vx) % mod + mod) % mod;
            if(cofY == 0) {
                if(rhs != 0)
                    throw new InconsistencyException();
                this.setActive(false);
                return;
            } 
            for(int val = y.min(); val <= y.max(); val++) {
                if(y.contains(val)) {
                    if((((cofY * val) % mod) + mod) % mod != rhs) {
                        y.remove(val);
                    }
                }
            }
            this.setActive(false);
            return;
        }
        else if(y.isFixed()) {
            int vy = y.min();
            int rhs = (result - (cofY * vy) % mod + mod) % mod;
            if(cofX == 0) {
                if(rhs != 0)
                    throw new InconsistencyException();
                this.setActive(false);
                return;
            } 
            for(int val = x.min(); val <= x.max(); val++) {
                if(x.contains(val)) {
                    if((((cofX * val) % mod) + mod) % mod != rhs) {
                        x.remove(val);
                    }
                }
            }
            this.setActive(false);
        }
    }


    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if(((cofX * vx + cofY * vy) % mod + mod) % mod != result) {
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
        int minX = x.min();
        int maxX = x.max();
        int minY = y.min();
        int maxY = y.max();

        for (int xVal = minX; xVal <= maxX; xVal++) {
            if (!x.contains(xVal)) continue;
            int targetRemainder = (result - (cofX * xVal) % mod + mod) % mod;
            
            Integer rangeStart = null;
            Integer rangeEnd = null;
            
            for (int yVal = minY; yVal <= maxY; yVal++) {
                if (!y.contains(yVal)) continue;
                if (((cofY * yVal) % mod + mod) % mod != targetRemainder) {
                    // This y is forbidden for this x
                    if (rangeStart == null) {
                        rangeStart = yVal;
                    }
                    rangeEnd = yVal;
                } else {
                    // This y is allowed, close any open range
                    if (rangeStart != null) {
                        regions.add(new ForbiddenRegion(xVal, rangeStart, rangeEnd));
                        rangeStart = null;
                        rangeEnd = null;
                    }
                }
            }
            // Close any remaining open range
            if (rangeStart != null) {
                regions.add(new ForbiddenRegion(xVal, rangeStart, rangeEnd));
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
     * For X + Y ≡ 0 (mod 2), forbidden means x + y is odd
     */
    public boolean checkIfInForbiddenRegion(int x, int y) {
        return (cofX*x + cofY*y) % mod != result;  // Forbidden if sum is odd
    }
}