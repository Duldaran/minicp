package minicp.engine.core;

import java.util.Objects;

/**
 * Represents a rectangular forbidden region for constraint propagation
 */
public class ForbiddenRegion {
    private final int eventPoint;
    private final int infY;
    private final int supY;
    
    public ForbiddenRegion(int eventPoint, int infY, int supY) {
        this.eventPoint = eventPoint;
        this.infY = infY;
        this.supY = supY;
    }
    
    public int getEventPoint() { return eventPoint; }
    public int getInfY() { return infY; }
    public int getSupY() { return supY; }
    
    /**
     * Check if point (x,y) is contained in this forbidden region
     */
    public boolean contains(int x, int y) {
        return (eventPoint == x && infY <= y && y <= supY);
    }
    
    @Override
    public String toString() {
        return String.format("ForbiddenRegion(x:%d with borders [%d,%d])", 
                           eventPoint, infY, supY);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ForbiddenRegion)) return false;
        ForbiddenRegion other = (ForbiddenRegion) obj;
        return eventPoint == other.eventPoint &&
               infY == other.infY && supY == other.supY;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventPoint, infY, supY);
    }
}
