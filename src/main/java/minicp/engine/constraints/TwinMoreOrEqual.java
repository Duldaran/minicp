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

    private int nbPropagate = 0;
    private List<ForbiddenRegion> regions = new ArrayList<>();

    public TwinMoreOrEqual(IntVar x, IntVar y, int cofX, int cofY, int result) {
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


    public List<ForbiddenRegion> computeForbiddenRegions() {

        List<ForbiddenRegion> regions = new ArrayList<>();
        int yMin = y.min();
        int yMax = y.max();

        for (int xVal = x.min(); xVal <= x.max(); xVal++) {
            if (!x.contains(xVal)) continue;

            if (cofY > 0) {
                int num = result - cofX * xVal;
                int yMinAllowed = ceilDiv(num, cofY); // smallest y that satisfies

                int forbiddenLow = yMin;
                int forbiddenHigh = yMinAllowed - 1;

                if (forbiddenLow <= forbiddenHigh) {
                    // Intersect with current y-domain
                    forbiddenLow  = Math.max(forbiddenLow, yMin);
                    forbiddenHigh = Math.min(forbiddenHigh, yMax);
                    if (forbiddenLow <= forbiddenHigh) {
                        regions.add(new ForbiddenRegion(xVal, forbiddenLow, forbiddenHigh));
                    }
                }
            }
            else if (cofY < 0) {
                int num = result - cofX * xVal;
                int yMaxAllowed = Math.floorDiv(num, cofY); // largest y that satisfies

                int forbiddenLow = yMaxAllowed + 1;
                int forbiddenHigh = yMax;

                if (forbiddenLow <= forbiddenHigh) {
                    forbiddenLow  = Math.max(forbiddenLow, yMin);
                    forbiddenHigh = Math.min(forbiddenHigh, yMax);
                    if (forbiddenLow <= forbiddenHigh) {
                        regions.add(new ForbiddenRegion(xVal, forbiddenLow, forbiddenHigh));
                    }
                }
            }
            else { // cofY == 0
                if (cofX * xVal < result) {
                    // whole column is forbidden
                    regions.add(new ForbiddenRegion(xVal, yMin, yMax));
                }
                // else: all y allowed for that x → no region
            }
        }

        return regions;
    }

    // helper:
    static int ceilDiv(int num, int den) {
        return -Math.floorDiv(-num, den);
    }

    

    public int getNbPropagate() {
        return nbPropagate;
    }
    public List<ForbiddenRegion> getForbiddenRegions() {
        return regions;
    }
}
