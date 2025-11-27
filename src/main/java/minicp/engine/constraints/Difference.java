package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

public class Difference extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final IntVar z;

    // Enforces z = x - y
    public Difference(IntVar x, IntVar y, IntVar z) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        x.propagateOnDomainChange(this);
        y.propagateOnDomainChange(this);
        z.propagateOnDomainChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        // z = x - y  <=>  x = z + y
        z.removeBelow(x.min() - y.max());
        z.removeAbove(x.max() - y.min());

        x.removeBelow(z.min() + y.min());
        x.removeAbove(z.max() + y.max());

        y.removeBelow(x.min() - z.max());
        y.removeAbove(x.max() - z.min());
    }
}
