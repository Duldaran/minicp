package minicp.engine.core;

import java.util.Objects;

/**
 * Represents a rectangular forbidden region for constraint propagation
 */
public class ForbiddenRegion {
    private final int infX;
    private final int supX;
    private final int infY;
    private final int supY;
    
    public ForbiddenRegion(int infX, int supX, int infY, int supY) {
        this.infX = infX;
        this.supX = supX;
        this.infY = infY;
        this.supY = supY;
    }
    
    public int getInfX() { return infX; }
    public int getSupX() { return supX; }
    public int getInfY() { return infY; }
    public int getSupY() { return supY; }
    
    /**
     * Check if point (x,y) is contained in this forbidden region
     */
    public boolean contains(int x, int y) {
        return (infX <= x && x <= supX && infY <= y && y <= supY);
    }
    
    @Override
    public String toString() {
        return String.format("ForbiddenRegion([%d,%d] x [%d,%d])", 
                           infX, supX, infY, supY);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ForbiddenRegion)) return false;
        ForbiddenRegion other = (ForbiddenRegion) obj;
        return infX == other.infX && supX == other.supX &&
               infY == other.infY && supY == other.supY;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(infX, supX, infY, supY);
    }
}
