package massim.javaagents.map;

import java.util.HashSet;
import java.util.Objects;

/**
 * The Atomic element of a MASSim Map.
 * Describes a cell, can have different types, stores last visited step, stores occupancy at a specific step
 *
 * @author Alexander Lorenz, Sebastian Loder
 */
public class NextMapTile {
    /*
     * ########## region fields
     */
    
    private Integer positionX;              // X position on a map 
    private Integer positionY;              // Y position on a map
    private Integer lastVisionStep;         // last step, the tile was observed
    private Boolean isAThing;               // true if ist a thing
    private String thingType;               // precise specification of a thing, e.g b1,b0,b2, Dispenser, Obstacle, zone...

    // - Pathfinding Attributes
    private boolean open = false;           // used in pathfinding to evaluate, if tile was already evaluated 
    private NextMapTile parent = null;      // used in pathfinding to store the parent tile
    private int score = 0;                  // used in pathfinding to store the distance score

    private HashSet<Integer> stepMemory = new HashSet<>();

    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     */
    
    /**
     * Specifies the most basic tile to fill the map
     */
    public NextMapTile() {
        this.lastVisionStep = -2;
        this.thingType = "unknown";
    }

    /**
     * Specifies a tile of type thing with stepMemory
     * 
     * @param positionX int X position on the map
     * @param positionY int Y position on the map
     * @param lastStepObserved int last time observed by an agent
     * @param thingType String type of thing 
     * @param stepMemory Integer HashSet collection of steps, the tile is occupied by an agent
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
     * Specifies a tile of type thing without stepMemory
     *
     * @param positionX int X position on the map
     * @param positionY int Y position on the map
     * @param lastStepObserved int last time observed by an agent
     * @param thingType String type of thing 
     */
    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved, String thingType) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = true;
        this.thingType = thingType;

    }

    /**
     * Specifies a free tile without stepMemory
     * 
     * @param positionX int X position on the map
     * @param positionY int Y position on the map
     * @param lastStepObserved int last time observed by an agent
     */
    public NextMapTile(Integer positionX, Integer positionY, Integer lastStepObserved) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.lastVisionStep = lastStepObserved;
        this.isAThing = false;
        this.thingType = "";
    }

    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */

    /**
     * Checks if a tile is a thing
     *
     * @return boolean true if a thing
     */
    public Boolean IsAThing() {
        return isAThing;
    }
    
    /**
     * Retrieves the X position of a tile
     * 
     * @return Integer X coordinate
     */
    public Integer GetPositionX() {
        return positionX;
    }
    
    /**
     * Retrieves the Y position of a tile
     * 
     * @return Integer Y coordinate
     */
    public Integer GetPositionY() {
        return positionY;
    }

    /**
     * @Deprecated replaced by {@link #GetPosition()}
     * Retrieves the position of a tile as Vector2D
     * 
     * @return Vector2D coordinates
     */
    @Deprecated
    public Vector2D getPosition() {
        return new Vector2D(positionX, positionY);
    }
    
    /**
     * Retrieves the position of a tile as Vector2D
     * 
     * @return Vector2D coordinates
     */
    public Vector2D GetPosition() {
        return new Vector2D(positionX, positionY);
    }

    /**
     * Specifies the position of a tile
     * @param pos Vector2D new position
     */
    public void SetPosition(Vector2D pos) {
        this.positionX = pos.x;
        this.positionY = pos.y;
    }

    /**
     * Adjusts the position of a tile
     * 
     * @param moveBy Vector2D specified offset
     */
    public void MovePosition(Vector2D moveBy) {
        this.positionX += moveBy.x;
        this.positionY += moveBy.y;
    }

    /**
     * Performs a modulus operation on the position of a tile. 
     * 
     * @param mod Vector2D input value
     */
    public void ModPosition(Vector2D mod) {
        Vector2D pos = new Vector2D(this.positionX, this.positionY);
        pos.Mod(mod);
        this.positionX = pos.x;
        this.positionY = pos.y;
    }

    /**
     * Retrieve the type of the tile
     * 
     * @return String type identification
     */
    public String GetThingType() {
        if (isAThing) {
            return thingType;
        }
        return "Empty";
    }

    /**
     * Specifies/Updates the type of a tile
     * 
     * @param thing String type identification
     */
    public void SetThingType(String thing) {
        this.thingType = thing;
        this.isAThing = true;
    }

    /**
     * Returns the last time the tile was observed 
     *
     * @return Integer step of last observation
     */
    public Integer GetLastVisionStep() {
        return lastVisionStep;
    }

    /**
     * Returns, if a map tile is "walkable" by an agent. 
     * Blocked tiles contain one of the following things: obstacle, unknown
     *
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkable() {
        //return !thingType.contains("obstacle") && !thingType.contains("entity") && !thingType.contains("unknown") && !thingType.contains("block");
        return !thingType.contains("obstacle") && !thingType.contains("unknown");
    }

    /**
     * Returns, if a map tile is "walkable" by an agent at a specific step.
     * Blocked tiles contain one of the following things: obstacle, unknown
     *
     * @param step int value to check at
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkable(Integer step) {
        return IsWalkable() && !this.stepMemory.contains(step);
    }

    /**
     * Returns, if a map tile is "walkable" by an agent. 
     * Blocked tiles contain one of the following things: entity, block, obstacle, unknown
     *
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkableStrict() {
        return !thingType.contains("obstacle") && !thingType.contains("entity") && !thingType.contains("unknown") && !thingType.contains("block");
    }

    /**
     * Returns, if a map tile is "walkable" by an agent at a specific step.
     * Blocked tiles contain one of the following things: entity, block, obstacle, unknown
     *
     * @param step int value to check at
     * @return Boolean true if not obstructed
     */
    public Boolean IsWalkableStrict(Integer step) {
        return IsWalkableStrict() && !this.stepMemory.contains(step);
    }

    /**
     * Checks if tile contains an obstacle
     *
     * @return boolean true if an obstacle
     */
    public Boolean IsObstacle() {
        return thingType.contains("obstacle");
    }

    /**
     * Checks if tile contains a dispenser
     *
     * @return boolean true if a dispenser
     */
    public Boolean IsDispenser() {
        return thingType.contains("dispenser");
    }

    /**
     * Checks if tile contains a block
     *
     * @return boolean true if a block
     */
    public Boolean IsBlock() {
        return thingType.contains("block");
    }

    /**
     * Checks if tile contains an entity
     *
     * @return boolean true if an entity
     */
    public Boolean IsEntity() {
        return thingType.contains("entity");
    }

    /**
     * Part of pathfinding search
     * Checks if tile was already processed
     * 
     * @return boolean false if already processed
     */
    public boolean IsOpen() {
        return open;
    }

    /**
     * Part of pathfinding search
     * Specifies the status, if a tile was already processed
     * 
     * @param open boolean true if not processed
     */
    public void SetOpen(boolean open) {
        this.open = open;
    }

    /**
     * Part of pathfinding search
     * Retrieve the parent tile for path calculation
     * 
     * @return NextMapTile of fastest predecessor to this tile
     */
    public NextMapTile GetParent() {
        return parent;
    }

    /**
     * Part of pathfinding search
     * Specifies the parent tile for path calculation
     * 
     * @param parent NextMapTile of fastest predecessor to this tile
     */
    public void SetParent(NextMapTile parent) {
        this.parent = parent;
    }

    /**
     * Part of pathfinding search
     * Retrieves the cost for reaching the tile
     *
     * @return int cost for reaching the tile
     */
    public int GetScore() {
        return score;
    }

    /**
     * Part of pathfinding search
     * Specifies the cost for reaching the tile
     *
     * @param score int cost for reaching the tile
     */
    public void SetScore(int score) {
        this.score = score;
    }
    

    /**
     * Part of stepMemory concept
     * Occupies a tile at a specific timestamp
     * 
     * @param step int time at which tile is blocked
     */
    public void BlockAtStep(int step) {
        this.stepMemory.add(step);
    }

    /**
     * Part of stepMemory concept
     * Frees a tile at a specific timestamp
     *
     * @param step int time at which tile is released
     */
    public void ReleaseAtStep(int step) {
        this.stepMemory.remove(step);
    }

    /**
     * Part of stepMemory concept
     * Checks if a tile is occupied at a specific timestamp
     *
     * @param step int specific timestamp to check
     * @return boolean true if occupied
     */
    public boolean CheckAtStep(int step) {
        return this.stepMemory.contains(step);
    }

    /**
     * Part of stepMemory concept
     * Returns a report of steps, when the tile is blocked
     * 
     * @return String representation of blocked Steps
     */
    public String ReportBlockedSteps() {
        StringBuilder intListe = new StringBuilder();
        for (int value : stepMemory) {
            intListe.append(" ").append(value);
        }
        return intListe.toString();
    }

    /**
     * Part of stepMemory concept
     * Retrieves a collection of steps, when the tile is blocked
     * Used in Map consolidation
     *
     * @return integer HashSet containing the stepMemory
     */
    public HashSet GetStepMemory() {
        return stepMemory;
    }

    /**
     * Part of stepMemory concept
     * Overwrites the collection of steps, when the tile is blocked
     * Used in Map consolidation
     *
     * @param stepMemory integer HashSet containing the stepMemory
     */
    public void SetStepMemory(HashSet<Integer> stepMemory) {
        this.stepMemory = stepMemory;
    }

    /**
     * Part of stepMemory concept
     * Adds the collection of steps to local stepMemory
     * Used in Map consolidation
     *
     * @param stepMemory integer HashSet containing the stepMemory
     */
    public void AddToStepMemory(HashSet<Integer> stepMemory) {
        this.stepMemory.addAll(stepMemory);
    }

    /** 
     * Specifies the last observed time
     *
     * @param lastVisionStep Integer time when observed last
     */
    public void SetLastVisionStep(Integer lastVisionStep) {
        this.lastVisionStep = lastVisionStep;
    }

    /**
     * Create a copy of a tile for further manipulation
     * 
     * @return NextMapTile containing a clone of the current tile
     */
    public NextMapTile Clone() {
        return new NextMapTile(this.positionX, this.positionY, this.lastVisionStep, this.thingType, this.stepMemory);
    }


    /**
     * This implementation calculates the hash based on position and type of a tile
     *
     * @return int hash value
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
     * This implementation compares two tiles based on position and type
     * 
     * @param o Object to compare to
     * @return boolean true if equal
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
    
    /**
     * This implementation returns the most relevant values as a string
     * 
     * @return String formatted for representation
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
    
    /*
     * ##################### endregion public methods
     */

}
