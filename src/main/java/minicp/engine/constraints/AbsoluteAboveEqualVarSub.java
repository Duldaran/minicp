package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.ForbiddenRegion;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.sun.tools.javap.resources.version;


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
    private List<ForbiddenRegion> regions = new ArrayList<>();

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
        this.regions = computeForbiddenRegions();
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
    public List<ForbiddenRegion> getForbiddenRegions() {
        return regions;
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
    
    public List<ForbiddenRegion> computeForbiddenRegions() {
        List<ForbiddenRegion> regions = new ArrayList<>();
        int minY = y.min();
        int maxY = y.max();
        for (int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                int minDisallowedY = Math.ceilDiv(cofX*vx - (result-1), cofY);
                int maxDisallowedY = Math.floorDiv(cofX*vx + (result-1), cofY);
                int lowerBound = Math.max(minDisallowedY, minY);
                int upperBound = Math.min(maxDisallowedY, maxY);
                if (lowerBound <= upperBound) {
                    regions.add(new ForbiddenRegion(vx, lowerBound, upperBound));
                }
            }
        }
        return regions;
    }
}