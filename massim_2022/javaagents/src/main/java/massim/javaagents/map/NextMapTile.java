package massim.javaagents.map;

import java.awt.Point;

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

    private Boolean isWalkable;

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
    
    public Vector2D getPosition(){
        return new Vector2D(positionX, positionY);
    }

    /**
     * Returns, if a map tile is "walkable" by an agent. Tiles which are blocked contain one of the following things:
     * entity, block, obstacle or dispenser
     *
     * @return
     */
    public Boolean isWalkable() {
        return !thingType.equals("obstacle") && !thingType.equals("entity") && !thingType.startsWith("block");
    }
    
    public Boolean IsObstacle()
    {
    	return thingType.equals("obstacle");
    }
}
