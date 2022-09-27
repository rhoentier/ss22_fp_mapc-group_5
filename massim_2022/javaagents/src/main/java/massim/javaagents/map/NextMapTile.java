package massim.javaagents.map;

import java.util.HashSet;
import java.util.Objects;
import massim.javaagents.general.NextConstants.ECardinals;

/**
 * The Atomic element of a MASSim Map
 *
 * @author Alexander Lorenz, Sebastian Loder
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

    /**
     *
     */
    public NextMapTile() {
        this.lastVisionStep = -2;
        this.thingType = "unknown";
    }

    /**
     *
     * @param positionX
     * @param positionY
     * @param lastStepObserved
     * @param thingType
     * @param stepMemory
     */
    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved, String thingType, HashSet<Integer> stepMemory) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = true;
        this.thingType = thingType;
        this.stepMemory = stepMemory;

    }

    /**
     *
     * @param positionX
     * @param positionY
     * @param lastStepObserved
     * @param thingType
     */
    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved, String thingType) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = true;
        this.thingType = thingType;

    }

    /**
     *
     * @param positionX
     * @param positionY
     * @param lastStepObserved
     */
    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = false;
        this.thingType = "";
    }

    /**
     *
     * @param position
     * @param lastStepObserved
     * @param thingType
     */
    public NextMapTile(Vector2D position, Integer lastStepObserved, String thingType) {
        this.positionX = position.x;
        this.positionY = position.y;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = true;
        this.thingType = thingType;

    }

    /**
     *
     * @return
     */
    public Boolean GetIsAThing() {
        return isAThing;
    }

    /**
     *
     * @return
     */
    public Vector2D getPosition() {
        return new Vector2D(positionX, positionY);
    }

    /**
     *
     * @return
     */
    public Integer GetPositionX() {
        return positionX;
    }

    /**
     *
     * @return
     */
    public Integer GetPositionY() {
        return positionY;
    }

    /**
     *
     * @return
     */
    public Vector2D GetPosition() {
        return new Vector2D(positionX, positionY);
    }

    /**
     *
     * @param pos
     */
    public void SetPosition(Vector2D pos) {
        this.positionX = pos.x;
        this.positionY = pos.y;
    }

    /**
     *
     * @param moveBy
     */
    public void MovePosition(Vector2D moveBy) {
        this.positionX += moveBy.x;
        this.positionY += moveBy.y;
    }

    /**
     *
     * @param v
     */
    public void Subtract(Vector2D v) {
        this.positionX -= v.x;
        this.positionY -= v.y;
    }

    /**
     *
     * @param mod
     */
    public void ModPosition(Vector2D mod) {
        Vector2D pos = new Vector2D(this.positionX, this.positionY);
        pos.Mod(mod);
        this.positionX = pos.x;
        this.positionY = pos.y;
    }

    /**
     *
     * @return
     */
    public String GetThingType() {
        if (isAThing) {
            return thingType;
        }
        return "Empty";
    }

    /**
     *
     * @param thing
     */
    public void SetThingType(String thing) {
        this.thingType = thing;
        this.isAThing = true;
    }

    /**
     *
     * @return
     */
    public Integer GetLastVisionStep() {
        return lastVisionStep;
    }

    /**
     *
     * @return
     */
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
     * Returns, if a map tile is "walkable" by an agent. Tiles which are blocked
     * contain one of the following things: obstacle, unknown
     *
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkable() {
        //return !thingType.contains("obstacle") && !thingType.contains("entity") && !thingType.contains("unknown") && !thingType.contains("block");
        return !thingType.contains("obstacle") && !thingType.contains("unknown");
    }

    /**
     * Returns, if a map tile is "walkable" by an agent at a specific step.
     * Tiles which are blocked contain one of the following things: obstacle, unknown
     *
     * @param step int value to check at
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkable(Integer step) {
        return IsWalkable() && !this.stepMemory.contains(step);
    }

    /**
     * Returns, if a map tile is "walkable" by an agent. 
     * Tiles which are blocked contain one of the following things: entity, block, obstacle, unknown
     *
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkableStrict() {
        return !thingType.contains("obstacle") && !thingType.contains("entity") && !thingType.contains("unknown") && !thingType.contains("block");
    }

    /**
     * Returns, if a map tile is "walkable" by an agent at a specific step.
     * Tiles which are blocked contain one of the following things: entity, block, obstacle, unknown
     *
     * @param step int value to check at
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkableStrict(Integer step) {
        return IsWalkableStrict() && !this.stepMemory.contains(step);
    }

    /**
     *
     * @return
     */
    public Boolean IsObstacle() {
        return thingType.contains("obstacle");
    }

    /**
     *
     * @return
     */
    public Boolean IsDispenser() {
        return thingType.contains("dispenser");
    }

    /**
     *
     * @return
     */
    public Boolean IsBlock() {
        return thingType.contains("block");
    }

    /**
     *
     * @return
     */
    public Boolean IsEntity() {
        return thingType.contains("entity");
    }

    /**
     *
     * @return
     */
    public boolean IsOpen() {
        return open;
    }

    /**
     *
     * @param open
     */
    public void SetOpen(boolean open) {
        this.open = open;
    }

    /**
     *
     * @return
     */
    public NextMapTile GetParent() {
        return parent;
    }

    /**
     *
     * @param parent
     */
    public void SetParent(NextMapTile parent) {
        this.parent = parent;
    }

    /**
     *
     * @return
     */
    public int GetScore() {
        return score;
    }

    /**
     *
     * @param score
     */
    public void SetScore(int score) {
        this.score = score;
    }

    /**
     *
     * @param positionX
     */
    public void SetPositionX(Integer positionX) {
        this.positionX = positionX;
    }

    /**
     *
     * @param positionY
     */
    public void SetPositionY(Integer positionY) {
        this.positionY = positionY;
    }

    /**
     *
     * @return
     */
    public NextMapTile Clone() {
        return new NextMapTile(this.positionX, this.positionY, this.lastVisionStep, this.thingType, this.stepMemory);
    }

    /**
     *
     * @param lastVisionStep
     */
    public void SetLastVisionStep(Integer lastVisionStep) {
        this.lastVisionStep = lastVisionStep;
    }

    /**
     *
     * @return
     */
    @Override
    public NextMapTile clone() {
        return new NextMapTile(positionX, positionY, lastVisionStep, thingType, stepMemory);
    }

    /**
     *
     * @param step
     */
    public void BlockAtStep(int step) {
        this.stepMemory.add(step);
    }

    /**
     *
     * @param step
     */
    public void ReleaseAtStep(int step) {
        this.stepMemory.remove(step);
    }

    /**
     *
     * @param step
     * @return
     */
    public boolean CheckAtStep(int step) {
        return this.stepMemory.contains(step);
    }

    /**
     *
     * @return
     */
    public String ReportBlockedSteps() {
        StringBuilder intListe = new StringBuilder();
        for (int value : stepMemory) {
            intListe.append(" " + value);
        }
        return intListe.toString();
    }

    /**
     *
     * @return
     */
    public HashSet GetStepMemory() {
        return stepMemory;
    }

    /**
     *
     * @param stepMemory
     */
    public void SetStepMemory(HashSet<Integer> stepMemory) {
        this.stepMemory = stepMemory;
    }

    /**
     *
     * @param stepMemory
     */
    public void AddToStepMemory(HashSet<Integer> stepMemory) {
        this.stepMemory.addAll(stepMemory);
    }

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

    /**
     *
     * @return
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

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        NextMapTile maptile = (NextMapTile) o;

        return Objects.equals(positionX, maptile.positionX)
                && Objects.equals(positionY, maptile.positionY)
                && Objects.equals(isAThing, maptile.isAThing)
                && Objects.equals(thingType, maptile.thingType);
    }

}
