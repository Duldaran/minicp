package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

/**
 * Absolute value constraint
 */
public class AbsoluteAboveEqual extends AbstractConstraint {

    private final IntVar x;
    private final int k;

    /**
     * Creates the constraint {@code |x| > k}.
     *
     * @param x the input variable
     * @param k the strict lower bound on |x|
     */
    public AbsoluteAboveEqual(IntVar x, int k) {
        super(x.getSolver());
        this.x = x;
        this.k = k;
        if (k < 0)
            throw new IllegalArgumentException("k must be >= 0 in |x| > k");
    }


    @Override
    public void post() {
        // React whenever the domain of x changes
        x.propagateOnDomainChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        // Remove all values vx with |vx| < k
        for (int vx = x.min(); vx <= x.max(); vx++) {
            if (x.contains(vx)) {
                if (Math.abs(vx) < k) {
                    x.remove(vx);
                }
            }
        }

        // If domain is empty, this will have thrown already;
        // if x is fixed, check the condition explicitly:
        if (x.isFixed() && Math.abs(x.min()) < k) {
            throw new InconsistencyException();
        }

        setActive(false);
    }
}