package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.exception.InconsistencyException;

import static minicp.cp.Factory.*;

import java.util.ArrayList;

/**
 * TwinEqual
 */
public class TwinEqual extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int cofX;
    private final int cofY;
    private final int result;

    public TwinEqual(IntVar x, IntVar y, int cofX, int cofY, int result) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.cofX = cofX;
        this.cofY = cofY;
        this.result = result;
        if (cofX == 0 && cofY == 0)
            throw new IllegalArgumentException("at least one coefficient must be non-zero");
    }

    @Override
    public void post() {
        x.propagateOnBoundChange(this);
        y.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        if (cofX > 0) {
            y.removeAbove(Math.floorDiv(result - cofX * x.min(), cofY));
            y.removeBelow(Math.ceilDiv(result - cofX * x.max(), cofY));
        } else if (cofX < 0) {
            y.removeBelow(Math.ceilDiv(result - cofX * x.max(), cofY));
            y.removeAbove(Math.floorDiv(result - cofX * x.min(), cofY));
        } 
        if (cofY > 0) {
            x.removeAbove(Math.floorDiv(result - cofY * y.min(), cofX));
            x.removeBelow(Math.ceilDiv(result - cofY * y.max(), cofX));
        } else if (cofY < 0) {
            x.removeBelow(Math.ceilDiv(result - cofY * y.max(), cofX));
            x.removeAbove(Math.floorDiv(result - cofY * y.min(), cofX));
        }
    }

    @Override
    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if((cofX * vx + cofY * vy) != result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        return pairs;
    }
}
