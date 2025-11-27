package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;

/**
 * Absolute value constraint
 */
public class Absolute extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;

/** * Creates the absolute value constraint {@code y = |x|}. 
 * 
 * @param x the input variable such that its absolut value is equal to y 
 * @param y the variable that represents the absolute value of x */

    public Absolute(IntVar x, IntVar y) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
    }

    @Override
    public void post() {
        throw new NotImplementedException("Absolute");    }

    @Override
    public void propagate() {
        throw new NotImplementedException("Absolute");
    }
}