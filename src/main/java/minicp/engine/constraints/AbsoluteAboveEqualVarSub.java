package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

/**
 * Absolute value constraint
 */
public class AbsoluteAboveEqualVarSub extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int k;

    /**
     * Creates the constraint {@code |x| > k}.
     *
     * @param x the input variable
     * @param k the strict lower bound on |x|
     */
    public AbsoluteAboveEqualVarSub(IntVar x, IntVar y, int k) {
        super(x.getSolver());
        this.x = x;        
        this.y = y;
        this.k = k;
        if (k < 0)
            throw new IllegalArgumentException("k must be >= 0 in |x| > k");
    }


    @Override
    public void post() {
        // React whenever the domain of x changes
        x.propagateOnDomainChange(this);
        y.propagateOnDomainChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        if (x.isFixed()){
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
        if (y.isFixed()){
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
        }
    }
}