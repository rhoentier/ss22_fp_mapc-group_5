package massim.javaagents.map;

import java.util.HashSet;
import java.util.Objects;
import massim.javaagents.general.NextConstants.ECardinals;

/**
 * The Atomic element of a Massim Map
 *
 * @author Alexander Lorenz, Sebastian
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
    
    private HashSet<Integer> stepMemory = new HashSet<>(); 
        
    public NextMapTile() {
        this.lastVisionStep = -2;
        this.thingType = "unknown";
    }
    

    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved, String thingType, HashSet<Integer> stepMemory) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = true;
        this.thingType = thingType;
        this.stepMemory = stepMemory;

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

    public Vector2D getPosition(){
        return new Vector2D(positionX,positionY);
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

    public void Subtract(Vector2D v) {
        this.positionX -= v.x;
        this.positionY -= v.y;
    }

    public void ModPosition(Vector2D mod) {
        Vector2D pos = new Vector2D(this.positionX, this.positionY);
        pos.mod(mod);
        this.positionX = pos.x;
        this.positionY = pos.y;
    }

    public String getThingType() {
        if (isAThing) {
            return thingType;
        }
        return "Empty";
    }

    public void SetThingType(String thing) {
        this.thingType = thing;
        this.isAThing = true;
    }

    public Integer getLastVisionStep() {
        return lastVisionStep;
    }

    @Override
    public String toString() {
        if (isAThing) {
            return "\n[ " + this.positionX + ", " + this.positionY + " ]"
                    + "Last Step: " + this.lastVisionStep + " "
                    + "Type: " + thingType + " ";

        }
        return "\n[ " + this.positionX + ", " + this.positionY + " ]"
                + "Last Step: " + this.lastVisionStep;
    }
    
    /**
     * Returns, if a map tile is "walkable" by an agent. Tiles which are blocked contain one of the following things:
     * obstacle, unknown
     *
     * @return
     */
    public Boolean IsWalkable() {
        //return !thingType.contains("obstacle") && !thingType.contains("entity") && !thingType.contains("unknown") && !thingType.contains("block");
        return !thingType.contains("obstacle") && !thingType.contains("unknown");
    }
    
    /**
     * Returns, if a map tile is "walkable" by an agent at a specific step. Tiles which are blocked contain one of the following things:
     * obstacle, unknown
     *
     * @return
     */
    public Boolean IsWalkable(Integer step) {
        return IsWalkable() && !this.stepMemory.contains(step);
    }
    
    
    /**
     * Returns, if a map tile is "walkable" by an agent. Tiles which are blocked contain one of the following things:
     * entity, block, obstacle, unknown
     *
     * @return Boolean
     */
    public Boolean IsWalkableStrict() {
        return !thingType.contains("obstacle") && !thingType.contains("entity") && !thingType.contains("unknown") && !thingType.contains("block");
    }
    
    /**
    * Returns, if a map tile is "walkable" by an agent at a specific step. Tiles which are blocked contain one of the following things:
    * obstacle, unknown
    *
    * @return Boolean  
    */
    public Boolean IsWalkableStrict(Integer step) {
        return IsWalkableStrict() && !this.stepMemory.contains(step);
    }
    
    public Boolean IsObstacle()
    {
    	return thingType.contains("obstacle");
    }
    
    public Boolean IsDispenser()
    {
    	return thingType.contains("dispenser");
    }
    
    public Boolean IsBlock()
    {
    	return thingType.contains("block");
    }
    
    public Boolean IsEntity()
    {
    	return thingType.contains("entity");
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
        return new NextMapTile(this.positionX, this.positionY, this.lastVisionStep, this.thingType,this.stepMemory);
    }

    public void SetLastVisionStep(Integer lastVisionStep) {
        this.lastVisionStep = lastVisionStep;
    }

    @Override
    public NextMapTile clone() {
        return new NextMapTile(positionX, positionY, lastVisionStep, thingType, stepMemory);
    }
    
    public void BlockAtStep (int step) {
        this.stepMemory.add(step);
    }
    
    public void ReleaseAtStep ( int step) {
        this.stepMemory.remove(step);
    }

    public boolean CheckAtStep (int step) {
        return this.stepMemory.contains(step);
    }
    
    public String ReportBlockedSteps (){
        StringBuilder intListe = new StringBuilder();
        for (int value : stepMemory) {
            intListe.append(" " + value);
        }
        return intListe.toString();
    }
    
    public HashSet GetStepMemory(){
        return stepMemory;
    }

    public void SetStepMemory(HashSet<Integer> stepMemory) {this.stepMemory = stepMemory;}

    public void AddToStepMemory(HashSet<Integer> stepMemory) {this.stepMemory.addAll(stepMemory);}
    /*
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
    */

    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.positionX);
        hash = 61 * hash + Objects.hashCode(this.positionY);
        hash = 61 * hash + Objects.hashCode(this.isAThing);
        hash = 61 * hash + Objects.hashCode(this.thingType);
        return hash;
    }


    @Override
    public boolean equals (Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;

        NextMapTile maptile = (NextMapTile) o;

        return Objects.equals(positionX, maptile.positionX)
                && Objects.equals(positionY, maptile.positionY)
                && Objects.equals(isAThing, maptile.isAThing)
                && Objects.equals(thingType, maptile.thingType);
    }


}
