package minicp.engine.constraints;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.ForbiddenRegion;
import minicp.engine.core.IntVar;

public class AllDifferentVar extends AbstractConstraint {

    private final IntVar x;
    private final IntVar y;

    private int nbPropagate = 0;
    private List<ForbiddenRegion> regions = new ArrayList<>();

    public AllDifferentVar(IntVar x, IntVar y) {
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.regions = computeForbiddenRegions();
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
        return regions;
    }

    public List<ForbiddenRegion> computeForbiddenRegions() {
        List<ForbiddenRegion> regions = new ArrayList<>();
        
        int minY = y.min();
        int maxY = y.max();
        
        for(int vx = x.min(); vx <= x.max(); vx++){
            int invalidY = vx;

            int invalidYInt = (int) invalidY;
            if (maxY >= invalidYInt && invalidYInt >= minY) {
                regions.add(new ForbiddenRegion(vx, invalidYInt, invalidYInt));
            }
        }
        
        return regions;
    }
    
    /**
     * Primitive: Get first forbidden region 
     */
    public Map.Entry<Integer, List<ForbiddenRegion>> getFirstForbiddenRegions(){        
        if (regions.isEmpty()) {
            return null;
        }
        // Sort by infX, then infY
        regions.sort(Comparator.comparingInt(ForbiddenRegion::getEventPoint)
                              .thenComparingInt(ForbiddenRegion::getInfY));

        int starting_idx = regions.get(0).getEventPoint();
        List<ForbiddenRegion> startingRegions = new ArrayList<>();
        for (ForbiddenRegion r : regions) {
            if (r.getEventPoint() == starting_idx) {
                startingRegions.add(r);
            }
            else {
                break;
            }
        }
        return new AbstractMap.SimpleEntry<>(starting_idx, startingRegions);
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
