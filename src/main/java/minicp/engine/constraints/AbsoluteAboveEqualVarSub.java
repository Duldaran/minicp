package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

import java.util.ArrayList;


/**
 * Absolute value constraint
 * |x - y| >= k   (pairs with |x - y| < k are forbidden)
 */
public class AbsoluteAboveEqualVarSub extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int k;

    /**
     * Creates the constraint |x - y| >= k
     *
     * @param x the first variable
     * @param y the second variable
     * @param k the strict lower bound on |x - y|
     */
    public AbsoluteAboveEqualVarSub(IntVar x, IntVar y, int k) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.k = k;
        if (k < 0)
            throw new IllegalArgumentException("k must be >= 0 in |x - y| >= k");
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
            for (int vx = x.min(); vx <= x.max(); vx++) {
                if (x.contains(vx)) {
                    for (int vy = y.min(); vy <= y.max(); vy++) {
                        if (y.contains(vy)) {
                            if (Math.abs(vx - vy) < k) {
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
                            if (Math.abs(vx - vy) < k) {
                                x.remove(vx);
                            }
                        }
                    }
                }
            }
            this.setActive(false);
        }
    }

    @Override
    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();

        for (int vx = x.min(); vx <= x.max(); vx++) {
            if (x.contains(vx)) {
                for (int vy = y.min(); vy <= y.max(); vy++) {
                    if (y.contains(vy)) {
                        if (Math.abs(vx - vy) < k) {
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
                        if (Math.abs(vx - vy) < k) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }

        return pairs;
    }
}
