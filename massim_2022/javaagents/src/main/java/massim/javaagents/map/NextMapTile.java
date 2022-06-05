package massim.javaagents.map;

import java.awt.Point;

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

    public Boolean getIsAThing() {
        return isAThing;
    }

    public Integer getPositionX() {
        return positionX;
    }

    public Integer getPositionY() {
        return positionY;
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
                    + "Type: " + thingType;

        }
        return "[ " + this.positionX + ", " + this.positionY + " ]\n"
                + "Last Step: " + this.lastVisionStep;
    }

    public Point getPoint(){
        return new Point(positionX, positionY);
    }

    /**
     * Returns, if a map tile is "walkable" by an agent. Tiles which are blocked contain one of the following things:
     * entity, block, obstacle
     *
     * @return
     */
    public Boolean IsWalkable() {
        return !thingType.equals("obstacle") && !thingType.equals("entity") && !thingType.equals("unknown") && !thingType.startsWith("block");
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

    
    

}
