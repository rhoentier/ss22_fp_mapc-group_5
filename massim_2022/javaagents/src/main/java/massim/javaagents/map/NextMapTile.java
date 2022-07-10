package massim.javaagents.map;

import java.util.Objects;
import massim.javaagents.general.NextConstants.ECardinals;

/**
 * The Atomic element of a Massim Map
 *
 * ToDo: Compare Manhattan Distance Calculation isEqual toString
 *
 * Vererbung?
 *
 * @author AVL
 */
public class NextMapTile {

    private Integer positionX;
    private Integer positionY;
    private Integer lastVisionStep;
    private Boolean isAThing;
    private String thingType; // - (b1,b0,b2, Dispenser, Obstacle, zone...)

    // - Pathfinding Attributes
    private boolean open = false;
    private NextMapTile parent = null;
    private int score = 0;
    
    
    public NextMapTile() {
        this.lastVisionStep = -2;
        this.thingType = "unknown";
    }
    

    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved, String thingType) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = true;
        this.thingType = thingType;

    }

    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = false;
        this.thingType = "";
    }

    public NextMapTile(Vector2D position, Integer lastStepObserved, String thingType) {
        this.positionX = position.x;
        this.positionY = position.y;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = true;
        this.thingType = thingType;

    }
    public Boolean getIsAThing() {
        return isAThing;
    }

    public Integer getPositionX() {
        return positionX;
    }

    public Integer getPositionY() {
        return positionY;
    }

    public Vector2D GetPosition() {
        return new Vector2D(positionX, positionY);
    }

    public void SetPosition(Vector2D pos) {
        this.positionX = pos.x;
        this.positionY = pos.y;
    }

    public void MovePosition(Vector2D moveBy) {
        this.positionX += moveBy.x;
        this.positionY += moveBy.y;
    }

    public String getThingType() {
        if (isAThing) {
            return thingType;
        }
        return "Empty";
    }

    public Integer getLastVisionStep() {
        return lastVisionStep;
    }

    @Override
    public String toString() {
        if (isAThing) {
            return "[ " + this.positionX + ", " + this.positionY + " ]\n"
                    + "Last Step: " + this.lastVisionStep + "\n"
                    + "Type: " + thingType + "\n";

        }
        return "[ " + this.positionX + ", " + this.positionY + " ]\n"
                + "Last Step: " + this.lastVisionStep;
    }
    
    /**
     * Returns, if a map tile is "walkable" by an agent. Tiles which are blocked contain one of the following things:
     * entity, block, obstacle
     *
     * @return
     */
    public Boolean IsWalkable() {
        return !thingType.contains("obstacle") && !thingType.contains("entity") && !thingType.contains("unknown") && !thingType.contains("block");
    }
    
    public Boolean IsObstacle()
    {
    	return thingType.contains("obstacle");
    }
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public NextMapTile getParent() {
        return parent;
    }

    public void setParent(NextMapTile parent) {
        this.parent = parent;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setPositionX(Integer positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(Integer positionY) {
        this.positionY = positionY;
    }

    public NextMapTile Clone() {
        return new NextMapTile(this.positionX, this.positionY, this.lastVisionStep, this.thingType);
    }

    public void SetLastVisionStep(Integer lastVisionStep) {
        this.lastVisionStep = lastVisionStep;
    }

    @Override
    public NextMapTile clone() {
        return new NextMapTile(positionX, positionY, lastVisionStep, thingType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NextMapTile other = (NextMapTile) obj;
        if (!Objects.equals(this.thingType, other.thingType)) {
            return false;
        }
        if (!Objects.equals(this.positionX, other.positionX)) {
            return false;
        }
        if (!Objects.equals(this.positionY, other.positionY)) {
            return false;
        }
        return Objects.equals(this.isAThing, other.isAThing);
    }

    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.positionX);
        hash = 61 * hash + Objects.hashCode(this.positionY);
        hash = 61 * hash + Objects.hashCode(this.isAThing);
        hash = 61 * hash + Objects.hashCode(this.thingType);
        return hash;
    }
}
