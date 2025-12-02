package minicp.engine.constraints;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.ForbiddenRegion;
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

    public List<ForbiddenRegion> getForbiddenRegions() {
        List<ForbiddenRegion> regions = new ArrayList<>();
        
        int minX = x.min();
        int maxX = x.max();
        int minY = y.min();
        int maxY = y.max();
        
        for(int vx = x.min(); vx <= x.max(); vx++){
            int invalidY = vx;

            int invalidYInt = (int) invalidY;
            if (maxY >= invalidYInt && invalidYInt >= minY) {
                regions.add(new ForbiddenRegion(vx, vx,  invalidYInt , invalidYInt));
            }
        }
        
        return regions;
    }
    
    /**
     * Primitive: Get first forbidden region (ordered by x, then y)
     */
    public ForbiddenRegion getFirstForbiddenRegion() {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        if (regions.isEmpty()) {
            return null;
        }
        
        // Sort by infX, then infY
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        return regions.get(0);
    }
    
    /**
     * Primitive: Get next forbidden region after 'previous'
     */
    public ForbiddenRegion getNextForbiddenRegion(ForbiddenRegion previous) {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        int index = regions.indexOf(previous);
        if (index >= 0 && index + 1 < regions.size()) {
            return regions.get(index + 1);
        }
        
        return null;
    }
    
    /**
     * Primitive: Get last forbidden region
     */
    public ForbiddenRegion getLastForbiddenRegion() {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        if (regions.isEmpty()) {
            return null;
        }
        
        // Sort by infX, then infY
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        return regions.get(regions.size() - 1);
    }
    
    /**
     * Primitive: Get previous forbidden region before 'next'
     */
    public ForbiddenRegion getPrevForbiddenRegion(ForbiddenRegion next) {
        List<ForbiddenRegion> regions = getForbiddenRegions();
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getInfX)
                              .thenComparingInt(ForbiddenRegion::getInfY));
        
        int index = regions.indexOf(next);
        if (index > 0) {
            return regions.get(index - 1);
        }
        
        return null;
    }
    
    /**
     * Primitive: Check if point (x,y) is in any forbidden region
     */
    public boolean checkIfInForbiddenRegion(int x, int y) {
        return (x - y) == 0;
    }
}
