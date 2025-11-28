package minicp.engine.constraints;

import java.util.ArrayList;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;

public class AllDifferentVar extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;

    private int nbPropagate = 0;

    public AllDifferentVar(IntVar x, IntVar y) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
    }

    @Override
    public void post() {
        x.propagateOnFix(this);
        y.propagateOnFix(this);
        propagate();
    }

    @Override
    public void propagate() {
        nbPropagate++;

        if (x.isFixed()) {
            int vx = x.min();
            if (y.contains(vx)) {
                y.remove(vx);
            }
        }
        if (y.isFixed()) {
            int vy = y.min();
            if (x.contains(vy)) {
                x.remove(vy);
            }
        }

    }

    // Pour ton affichage de régions interdites
    public ArrayList<Integer[]> getForbiddenPairs() {
        ArrayList<Integer[]> pairs = new ArrayList<>();
        for (int vx = x.min(); vx <= x.max(); vx++) {
            if (x.contains(vx)) {
                for (int vy = y.min(); vy <= y.max(); vy++) {
                    if (y.contains(vy) && vx == vy) {
                        pairs.add(new Integer[]{vx, vy});
                    }
                }
            }
        }
        return pairs;
    }

    public int getNbPropagate() {
        return nbPropagate;
    }
}
