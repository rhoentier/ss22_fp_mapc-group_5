package massim.javaagents.map;

import java.awt.*;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.Objects;

import eis.iilang.Identifier;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.ECardinals;

public class NextMap {

    private NextMapTile[][] map;
    private Vector2D zeroPoint;

    private HashSet<NextAgent> agents;
    private HashSet<String> excludeThingTypes;
    public Boolean foundDispenser = false;
    public boolean foundRoleZone = false;
    public boolean foundGoalZone = false;
    private HashSet<String> foundDispensers = new HashSet<String>(); // Speichert nur die Blocktypen (b0, b1, etc) ab

    public NextMap(NextAgent agent) {
        map = new NextMapTile[1][1];
        map[0][0] = new NextMapTile(0, 0, 0, "unknown");

        // Create new list of agents; Add this agent to list
        agents = new HashSet<>();
        agents.add(agent);

        zeroPoint = new Vector2D(0, 0);
        excludeThingTypes = new HashSet<>(Arrays.asList("entity", "block"));
        // ToDo: If implementation should be more efficient: Store separate list for static things instead of flags.
    }

    /**
     * Add an array of things to the map.
     *
     * @param agentPosition Current position of the agent relative to the
     * starting position.
     * @param percept Array of things as NextMapTile-objects.
     */
    public void AddPercept(Vector2D agentPosition, HashSet<NextMapTile> percept) {

        Vector2D mapTilePosition = new Vector2D();
        for (NextMapTile mapTile : percept) {
            mapTilePosition.set(agentPosition);
            mapTilePosition.add(mapTile.getPositionX(), mapTile.getPositionY());
            setMapTileRel(mapTilePosition, mapTile);
        }
    }

    /**
     * Print map to console with x0/y0 in top left corner. First letter of
     * getThingType() is used for representation. For example: "A": Agent, "O":
     * Obstacle. Special character "Z" for zero point.
     */
    public void WriteToFile(String filename) {
        String strMap = "";
        for (int j = 0; j < map[0].length; j++) {
            for (int i = 0; i < map.length; i++) {
                strMap += map[i][j].getThingType().charAt(0) + "  ";
            }
            strMap += "\n";
        }

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(filename);
            myWriter.write(strMap);
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Merges the given map (param 1) into the existing map (this) based on
     * position of agents and in which distance they see each other.
     *
     * @param mapAgent2 NextMap of the agent2, which is seen from agent1.
     * @param positionAgent1 Position of agent1, which sees agent2
     * @param positionAgent2 Position of agent2, which is seen from agent1.
     * @param deltaView Distance in which agent1 sees agent2
     */
    public void MergeMap(NextMap mapAgent2, Vector2D positionAgent1, Vector2D positionAgent2, Vector2D deltaView) {

        // Calculate vector from starting point of agent1 (on map1) to absolute zero on map2
        Vector2D displacementVector = new Vector2D(positionAgent1);
        displacementVector.add(deltaView);
        displacementVector.subtract(positionAgent2);
        displacementVector.subtract(mapAgent2.zeroPoint);

        for (int i = 0; i < mapAgent2.GetSizeOfMap().x; i++) {
            for (int j = 0; j < mapAgent2.GetSizeOfMap().y; j++) {
                Vector2D insertAt = new Vector2D(displacementVector);
                insertAt.add(i, j);
                setMapTileRel(insertAt, mapAgent2.map[i][j]);
            }
        }

        // Merge found dispensers
        foundDispensers.addAll(mapAgent2.foundDispensers);
        // Merge found zones without generating duplicates
        if (mapAgent2.foundRoleZone) {
            foundRoleZone = true;
        }
        if (mapAgent2.foundGoalZone) {
            foundGoalZone = true;
        }
    }

    /**
     * Finds the relative position of a map tile with a specific thing type
     *
     * @param thingType Thing type to be found, e.g. "dispenser"
     * @return Relative position of the nearest tile
     */
    public Vector2D GetNearest(String thingType, Vector2D agentPosition) {
        double shortestDistanceSoFar = Double.POSITIVE_INFINITY;
        double currentDistance;
        Vector2D absolutePosition = null;
        Vector2D agentPositionAbs = RelativeToAbsolute(agentPosition);

        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                if (Objects.equals(getMapTileAbs(new Vector2D(i, j)).getThingType(), thingType)) {
                    currentDistance = agentPositionAbs.distance(new Vector2D(i, j));
                    if (currentDistance < shortestDistanceSoFar) {
                        shortestDistanceSoFar = currentDistance;
                        absolutePosition = new Vector2D(i, j);
                    }
                }
            }
        }
        return absoluteToRelative(absolutePosition);
    }

    /**
     * Calculates the size of the map.
     *
     * @return Vector object, which represents the number of elements in x- and y-direction.
     */
    public Vector2D GetSizeOfMap() {
        return new Vector2D(map.length, map[0].length);
    }

    /**
     * Transforms a relative position to an absolute position. Example with grid 10/10 and zero point at 5/5.
     * Coordinate 1/1 (relative) is transformed to 6/6 (absolute).
     *
     * @param relativeVector Relative position on the map
     * @return Absolute position on the map
     */
    public Vector2D RelativeToAbsolute(Vector2D relativeVector) {
        return new Vector2D(relativeVector).getAdded(zeroPoint);
    }

    /**
     * Returns the map with coordinates 0/0 in upper left corner
     *
     * @return Internal Map
     */
    public NextMapTile[][] GetMap() {
       return map;
    }

    /**
     * Get a list of all maptile objects of a specific type.
     * @param thingType Filter for specific thing types, e.g. "dispenser" or "obstacle"
     * @param position Relative position of the agent (stored in NextAgentStatus)
     * @return HashSet of Maptiles
     */
    public HashSet<NextMapTile> GetMapTiles(String thingType, Vector2D position) {

        // ToDo: Store things directly in list as entity of map. Way more efficient and easier to handle than below.
        updateXY(position);

        HashSet<NextMapTile> maptileList = new HashSet<>();

        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                if (map[i][j].getThingType().startsWith(thingType)) {
                    maptileList.add(map[i][j]);
                }
            }
        }
        return maptileList;
    }

    /**
     * Transforms an absolute position to a relative position. Example with grid 10/10 and zero point at 5/5.
     * Coordinate 1/1 (absolute) is transformed to -4/-4 (relative).
     *
     * @param absoluteVector Absolute position on the map
     * @return Relative position on the map
     */
    private Vector2D absoluteToRelative(Vector2D absoluteVector) {
        return new Vector2D(absoluteVector).getSubtracted(zeroPoint);
    }

    /**
     * Returns a map tile at an absolute position
     *
     * @param absolutePosition
     * @return maptile object
     */
    private NextMapTile getMapTileAbs(Vector2D absolutePosition) {
        return map[absolutePosition.x][absolutePosition.y];
    }

    /**
     * Returns a map tile at a relative position
     *
     * @param relativePosition
     * @return maptile object
     */
    private NextMapTile getMapTileRel(Vector2D relativePosition) {
        Vector2D absolutePosition = RelativeToAbsolute(relativePosition);
        return getMapTileAbs(absolutePosition);
    }

    /**
     * Sets an object on the position relative to the starting position of the
     * agent.
     *
     * @param relativePosition: Position of the map tile relative to the
     * starting position of the agent.
     * @param maptile: MapTile to add.
     */
    private void setMapTileRel(Vector2D relativePosition, NextMapTile maptile) {
        Vector2D absPosition = RelativeToAbsolute(relativePosition);
        setMapTileAbs(absPosition, maptile);
    }

    /**
     * Sets an object on the absolute position of the map.
     *
     * @param absolutePosition: Position of the map tile absolute from the top
     * left point.
     * @param maptile: MapTile to add.
     */
    private void setMapTileAbs(Vector2D absolutePosition, NextMapTile maptile) {

        NextMapTile existingMapTile;

        // add dispenser and zones to found things
        if (maptile != null) {
            if (maptile.getThingType().startsWith("dispenser")) {
                if (!foundDispensers.contains(maptile.getThingType())) {
                    foundDispensers.add((maptile.getThingType().substring(10)));
                }
            } else if (maptile.getThingType().equals("goalZone")) {
                foundGoalZone = true;
            } else if (maptile.getThingType().equals("roleZone")) {
                foundRoleZone = true;
            }
        }

        Vector2D offset = new Vector2D(extendArray(absolutePosition));
        absolutePosition.add(offset);

        existingMapTile = this.map[absolutePosition.x][absolutePosition.y];

        // Check if type of maptile is part of the exlude list. If yes, set flag addMaptile to false
        boolean addMaptile = true;
        for (String e : excludeThingTypes) {
            if (maptile.getThingType().startsWith(e))
                addMaptile = false;
        }

        // Only add maptile if: flag addMapTile is true AND (existingMapTile is null OR existingMapTile is older)
        if (addMaptile && (existingMapTile == null || existingMapTile.getLastVisionStep() <= maptile.getLastVisionStep())) {
                this.map[absolutePosition.x][absolutePosition.y] = maptile;
        }
    }

    /**
     * Calculates the most positive coordinate possible for the current map. If
     * the map is of size 10/10 and the zero point is at 5/5, the most positive
     * coordinate is at 4/4.
     *
     * @return Vector with most positive coordinate
     */
    private Vector2D getPositiveExtend() {
        return new Vector2D(map.length - zeroPoint.x - 1, map[0].length - zeroPoint.y - 1);
    }

    /**
     * Calculates the most negative Coordinate possible for the current map. If
     * the map is of size 10/10 and the zero point is at 5/5, the most negative
     * coordinate is -5/-5.
     *
     * @return Vector with most negative coordinate
     */
    private Vector2D getNegativeExtend() {
        return zeroPoint.getReversed();
    }

    /**
     * Calculates the size of the map.
     *
     * @return Vector object, which represents the number of elements in x- and
     * y-direction.
     */
    public Vector2D getSizeOfMap() {
        return new Vector2D(map.length, map[0].length);
    }

    /**
     * Extends the size of the map object either in x+, x-, y+ or y- direction
     * if the map is too small.
     *
     * @param positionMapTile position of the map tile to be added relative to
     * the starting position.
     */
    private Vector2D extendArray(Vector2D positionMapTile) {

        int minExtend = 1; // ToDo: Should be extended in the future for higher efficiency. 1 is good for debugging.
        Vector2D numExtend = new Vector2D(0, 0);
        Vector2D offset = new Vector2D(0, 0);
        Vector2D sizeOfMap = GetSizeOfMap();

        if (positionMapTile.x >= map.length) {
            numExtend.x = Math.max(positionMapTile.x - map.length + 1, minExtend);
        } else if (positionMapTile.x < 0) {
            numExtend.x = Math.max(Math.abs(positionMapTile.x), minExtend);
            offset.x = numExtend.x;
        }

        if (positionMapTile.y >= map[0].length) {
            numExtend.y = Math.max(positionMapTile.y - map[0].length + 1, minExtend);
        } else if (positionMapTile.y < 0) {
            numExtend.y = Math.max(Math.abs(positionMapTile.y), minExtend);
            offset.y = numExtend.y;
        }

        if (numExtend.getLength() > 0) {
            sizeOfMap.add(numExtend);

            // Create extended array + fill with "unknown" maptiles
            NextMapTile[][] tmp = new NextMapTile[sizeOfMap.x][sizeOfMap.y];
            for (int i = 0; i < tmp.length; i++) {
                for (int j = 0; j < tmp[i].length; j++) {
                    // Note: X/Y are normally relative to the agents position. Here, they are set to 0.
                    tmp[i][j] = new NextMapTile(0, 0, 0, "unknown");
                }
            }

            // Copy existing map to tmp map
            for (int i = 0; i < this.map.length; i++) {
                for (int j = 0; j < this.map[i].length; j++) {
                    tmp[i + offset.x][j + offset.y] = this.map[i][j];
                }
            }
            this.zeroPoint.add(offset.x, offset.y);
            this.map = tmp;
        }
        return offset;
    }

    /** Updates x/y position of each map tile relative to the position of the agent
     *
     *  @param position relative position of the agent
     */
    private void updateXY(Vector2D position) {

        Vector2D absAgentPos = RelativeToAbsolute(position);
        int absX = absAgentPos.x;
        int absY = absAgentPos.y;

        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                this.map[i][j].setPosition(new Vector2D(i-absX, j-absY));
            }
        }
    }

    /**
     * Check if the rotation cw or ccw is possible. Note: North/South is swapped
     * in massim. For example if the North tile (bottom) is rotated in
     * cw-direction, it leads to West tile (left). For further explanation, see
     * also:
     * <a href="https://github.com/rhoentier/ss22_fp_mapc-group_5/pull/43#discussion_r878838495">https://github.com/rhoentier/ss22_fp_mapc-group_5/pull/43#discussion_r878838495</a>
     *
     * @param direction
     * @param attachedElements
     * @return true if the rotation is possible else otherwise
     */
    public boolean IsRotationPossible(Identifier direction, Vector2D position, HashSet<Point> attachedElements) {
        // ToDo: For the future, extend functionality if multiple blocks are attached in one direction
        if (direction.getValue().equals("cw")) {
            if (attachedElements.contains(NextConstants.NorthPoint)) {
                if (!getMapTileRel(position.getAdded(1, 0)).IsWalkable()) {
                      return false;
                }
            }
            if (attachedElements.contains(NextConstants.EastPoint)) {
                if (!getMapTileRel(position.getAdded(0, 1)).IsWalkable()) {
                    return false;
                }
            }
            if (attachedElements.contains(NextConstants.SouthPoint)) {
                if (!getMapTileRel(position.getAdded(-1, 0)).IsWalkable()) {
                    return false;
                }
            }
            if (attachedElements.contains(NextConstants.WestPoint)) {
                if (!getMapTileRel(position.getAdded(0, -1)).IsWalkable()) {
                    return false;
                }
            }
        } else {
            if (attachedElements.contains(NextConstants.NorthPoint)) {
                if (!getMapTileRel(position.getAdded(-1, 0)).IsWalkable()) {
                    return false;
                }
            }
            if (attachedElements.contains(NextConstants.EastPoint)) {
                if (!getMapTileRel(position.getAdded(0, -1)).IsWalkable()) {
                    return false;
                }
            }
            if (attachedElements.contains(NextConstants.SouthPoint)) {
                if (!getMapTileRel(position.getAdded(1, 0)).IsWalkable()) {
                    return false;
                }
            }
            if (attachedElements.contains(NextConstants.WestPoint)) {
                if (!getMapTileRel(position.getAdded(0, 1)).IsWalkable()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Prüft, ob alle benötigten Blöcke für eine Aufgabe und eine goalZone
     * bereits bekannt sind
     *
     * @param requiredBlocks
     * @return
     */
    public boolean IsTaskExecutable(HashSet<String> requiredBlocks) {
        if (foundGoalZone && foundDispensers.containsAll(requiredBlocks)) {
            return true;
        }
        return false;
    }

    public NextMapTile[][] GetMapArray() {
        return map.clone();
    }
    
    public static NextMapTile[][] CenterMapAroundPosition(NextMapTile[][] mapOld, Vector2D position) {
        if(mapOld.length == 1 && mapOld[0].length == 1 ) {
            return mapOld;      
        }
        
        int mapWidth = mapOld.length;
        int mapHeight = mapOld[0].length;
        int xOffset = (int)position.x  - ((int)(mapWidth / 2));
        int yOffset = (int)position.y - ((int)(mapHeight / 2)); 
        NextMapTile[][] tempMap = new NextMapTile[mapWidth][ mapHeight];
        
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int oldX = (x-xOffset+mapWidth)%(mapWidth-1);
                int oldY = (y-yOffset+mapHeight)%(mapHeight-1);
                tempMap[x][y] = new NextMapTile(
                        x,
                        y,
                        mapOld[oldX][oldY].getLastVisionStep(),
                        mapOld[oldX][oldY].getThingType());
            }
        }
        return tempMap;
    }
    
    /**
     * Creates a copy of the map and transforms the maptiles to absolte position 
     * 
     * @param mapOld
     * @return Returns an copy of the map with NextMapTiles using absolute coordinates.
     */
    public static NextMapTile[][] copyAbsoluteMap(NextMapTile[][] mapOld) {
        if(mapOld.length == 1 && mapOld[0].length == 1 ) {
            return mapOld;      
        }
        
        int mapWidth = mapOld.length;
        int mapHeight = mapOld[0].length;
        NextMapTile[][] tempMap = new NextMapTile[mapWidth][ mapHeight];
        
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                tempMap[x][y] = new NextMapTile(
                        x,
                        y,
                        mapOld[x][y].getLastVisionStep(),
                        mapOld[x][y].getThingType());
            }
        }
        return tempMap;
    }
    
    
    public String MapToStringBuilder() {
        return MapToStringBuilder(this.map);
    }
        

    public static String MapToStringBuilder( NextMapTile[][] map) {
        StringBuilder stringForReturn = new StringBuilder();

        for (int y = 0; y < map[0].length; y++) {
            StringBuilder subString = new StringBuilder();

            for (int x = 0; x < map.length; x++) {
                if (map[x][y] != null) {
                    if (map[x][y].IsWalkable() != null) {
                        if (map[x][y].IsWalkable()) {
                            subString.append("_");
                        } else {
                            subString.append("X");
                        }
                    }
                } else {
                    subString.append("#");
                }
            }
            
            stringForReturn.append(subString + "\n");
        }
        return "NextMap:" + "\n" + stringForReturn;
    }

    public Boolean ContainsPoint(Vector2D target) {
        int xPosition = target.x;
        int yPosition = target.y;
        
        if( xPosition >= 0 && xPosition < map.length ) {
            if( yPosition >= 0 && yPosition < map[0].length ){
                return true;
            }
        }
    
        return false;
    }
}
