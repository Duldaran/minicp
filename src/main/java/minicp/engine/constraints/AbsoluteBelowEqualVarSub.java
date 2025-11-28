package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

import java.util.ArrayList;

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
}