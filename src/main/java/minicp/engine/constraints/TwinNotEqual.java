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
public class TwinNotEqual extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int cofX;
    private final int cofY;
    private final int result;

    public TwinNotEqual(IntVar x, IntVar y, int cofX, int cofY, int result) {
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
        x.propagateOnFix(this);
        y.propagateOnFix(this);
        propagate();
    }

    @Override
    public void propagate() {
        if(x.isFixed()) {
            int vx = x.min();
            int bannedY = result - cofX * vx;
            if (cofY == 0 && bannedY == 0) {
                throw new InconsistencyException();
            }
            if (cofY != 0 && bannedY % cofY == 0) {
                int valueToRemove = bannedY / cofY;
                if (y.contains(valueToRemove)) {
                    y.remove(valueToRemove);
                }
            }
        }
        else if(y.isFixed()) {
            int vy = y.min();
            int bannedX = result - cofY * vy;
            if (cofX == 0 && bannedX == 0) {
                throw new InconsistencyException();
            }
            if (cofX != 0 && bannedX % cofX == 0) {
                int valueToRemove = bannedX / cofX;
                if (x.contains(valueToRemove)) {
                    x.remove(valueToRemove);
                }
            }
        }

    }

    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if((cofX * vx + cofY * vy) == result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        return pairs;
    }
}
