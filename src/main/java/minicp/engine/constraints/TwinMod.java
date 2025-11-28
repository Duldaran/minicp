

package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.exception.InconsistencyException;

import static minicp.cp.Factory.*;

import java.util.ArrayList;

/**
 * TwinMod
 */
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
        if (result < 0)
            throw new IllegalArgumentException("result must be non-negative");
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
        if(x.isFixed()) {
            int vx = x.min();
            int rhs = (result - (cofX * vx) % mod + mod) % mod;
            if(cofY == 0) {
                if(rhs != 0)
                    throw new InconsistencyException();
                this.setActive(false);
                return;
            } 
            for(int val = y.min(); val <= y.max(); val++) {
                if(y.contains(val)) {
                    if((((cofY * val) % mod) + mod) % mod != rhs) {
                        y.remove(val);
                    }
                }
            }
            this.setActive(false);
            return;
        }
        else if(y.isFixed()) {
            int vy = y.min();
            int rhs = (result - (cofY * vy) % mod + mod) % mod;
            if(cofX == 0) {
                if(rhs != 0)
                    throw new InconsistencyException();
                this.setActive(false);
                return;
            } 
            for(int val = x.min(); val <= x.max(); val++) {
                if(x.contains(val)) {
                    if((((cofX * val) % mod) + mod) % mod != rhs) {
                        x.remove(val);
                    }
                }
            }
            this.setActive(false);
        }
    }

    @Override
    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for(int vx = x.min(); vx <= x.max(); vx++) {
            if(x.contains(vx)) {
                for(int vy = y.min(); vy <= y.max(); vy++) {
                    if(y.contains(vy)) {
                        if(((cofX * vx + cofY * vy) % mod + mod) % mod != result) {
                            pairs.add(new Integer[]{vx, vy});
                        }
                    }
                }
            }
        }
        return pairs;
    }
}