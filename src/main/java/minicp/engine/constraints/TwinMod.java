package minicp.engine.constraints;

import java.util.ArrayList;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.InconsistencyException;

public class TwinMod extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;
    private final int mod;
    private final int cofX;
    private final int cofY;
    private final int result;

    public TwinMod(IntVar x, IntVar y, int mod, int cofX, int cofY, int result) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.mod = mod;
        this.cofX = cofX;
        this.cofY = cofY;
        this.result = result;
        if (mod <= 0)
            throw new IllegalArgumentException("mod must be positive");
        if (cofX == 0 && cofY == 0)
            throw new IllegalArgumentException("at least one coefficient must be non-zero");
        if (result < 0 || result >= mod)
            throw new IllegalArgumentException("result must be in [0," + (mod - 1) + "]");
    }

    @Override
    public void post() {
        x.propagateOnFix(this);
        y.propagateOnFix(this);
        propagate();
    }

    @Override
    public void propagate() {

        // Case 1: x is fixed -> prune y
        if (x.isFixed()) {
            int vx = x.min();
            int rhs = (result - (cofX * vx) % mod + mod) % mod;

            for (int vy = y.min(); vy <= y.max(); vy++) {
                if (y.contains(vy)) {
                    if ((cofY * vy) % mod != rhs) {
                        y.remove(vy);
                    }
                }
            }
            setActive(false);
        }

        // Case 2: y is fixed -> prune x
        else if (y.isFixed()) {
            int vy = y.min();
            int rhs = (result - (cofY * vy) % mod + mod) % mod;

            for (int vx = x.min(); vx <= x.max(); vx++) {
                if (x.contains(vx)) {
                    if ((cofX * vx) % mod != rhs) {
                        x.remove(vx);
                    }
                }
            }
            setActive(false);
        }
    }

    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if((cofX * vx + cofY * vy) % mod != result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        return pairs;
    } 
}
