package massim.javaagents.map;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import eis.iilang.Identifier;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextMap {

    private NextMapTile[][] map;
    private Vector2D zeroPoint;

    public boolean foundRoleZone = false;
    public boolean foundGoalZone = false;
    private HashSet<String> foundDispensers = new HashSet<String>(); // Speichert nur die Blocktypen (b0, b1, etc) ab

    public NextMap() {
        map = new NextMapTile[20][20];
        zeroPoint = new Vector2D(0, 0);
        // ToDo: If implementation should be more efficient: Store separate list for static things instead of flags.
    }

    /**
     * Add an array of things to the map.
     *
     * @param agentPosition Current position of the agent relative to the starting position.
     * @param percept       Array of things as NextMapTile-objects.
     */
    public void AddPercept(Vector2D agentPosition, NextMapTile[] percept) {

        Vector2D mapTilePosition = new Vector2D();
        for (NextMapTile mapTile : percept) {
            mapTilePosition.set(agentPosition);
            mapTilePosition.add(mapTile.getPositionX(), mapTile.getPositionY());
            setMapTileRel(mapTilePosition, mapTile);
        }
    }

    /**
     * Print map to console with x0/y0 in top left corner. First letter of getThingType() is used for representation.
     * For example: "A": Agent, "O": Obstacle. Special character "Z" for zero point.
     */
    public void PrintMap() {
        for (int j = 0; j < map[0].length; j++) {
            for (int i = 0; i < map.length; i++) {
                if (i == zeroPoint.x && j == zeroPoint.y) {
                    System.out.print("Z" + "  ");
                } else if (map[i][j] == null) {
                    System.out.print(".  ");
                } else {
                    System.out.print(map[i][j].getThingType().charAt(0) + "  ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Merges the given map (param 1) into the existing map (this) based on position of agents and in which distance
     * they see each other.
     *
     * @param mapAgent2      NextMap of the agent2, which is seen from agent1.
     * @param positionAgent1 Position of agent1, which sees agent2
     * @param positionAgent2 Position of agent2, which is seen from agent1.
     * @param deltaView      Distance in which agent1 sees agent2
     */
    public void mergeMap(NextMap mapAgent2, Vector2D positionAgent1, Vector2D positionAgent2, Vector2D deltaView) {

        // Calculate vector from starting point of agent1 (on map1) to absolute zero on map2
        Vector2D displacementVector = new Vector2D(positionAgent1);
        displacementVector.add(deltaView);
        displacementVector.subtract(positionAgent2);
        displacementVector.subtract(mapAgent2.zeroPoint);

        for (int i = 0; i < mapAgent2.getSizeOfMap().x; i++) {
            for (int j = 0; j < mapAgent2.getSizeOfMap().y; j++) {
                Vector2D insertAt = new Vector2D(displacementVector);
                insertAt.add(i, j);
                setMapTileRel(insertAt, mapAgent2.map[i][j]);
            }
        }

        // Merge found dispensers
        foundDispensers.addAll(mapAgent2.foundDispensers);
        // Merge found zones without generating duplicates
        if (mapAgent2.foundRoleZone) foundRoleZone = true;
        if (mapAgent2.foundGoalZone) foundGoalZone = true;
    }

    /**
     * Finds the relative position of a map tile with a specific thing type
     *
     * @param thingType Thing type to be found, e.g. "dispenser"
     */
    public Vector2D getNearest(String thingType, Vector2D agentPosition) {
        double shortestDistanceSoFar = Double.POSITIVE_INFINITY;
        double currentDistance;
        Vector2D absolutePosition = null;
        Vector2D agentPositionAbs = relativeToAbsolute(agentPosition);

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
     * Transforms a relative position to an absolute position. Example with grid 10/10 and zero point at 5/5.
     * Coordinate 1/1 (relative) is transformed to 6/6 (absolute).
     *
     * @param relativeVector Relative position on the map
     * @return Absolute position on the map
     */
    private Vector2D relativeToAbsolute(Vector2D relativeVector) {
        return new Vector2D(relativeVector).getAdded(zeroPoint);
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
        return map[(int) absolutePosition.x][(int) absolutePosition.y];
    }

    /**
     * Returns a map tile at a relative position
     *
     * @param relativePosition
     * @return maptile object
     */
    private NextMapTile getMapTileRel(Vector2D relativePosition) {
        Vector2D absolutePosition = relativeToAbsolute(relativePosition);
        return getMapTileAbs(absolutePosition);
    }

    /**
     * Sets an object on the position relative to the starting position of the agent.
     *
     * @param relativePosition: Position of the map tile relative to the starting position of the agent.
     * @param maptile:          MapTile to add.
     */
    private void setMapTileRel(Vector2D relativePosition, NextMapTile maptile) {
        Vector2D absPosition = relativeToAbsolute(relativePosition);
        setMapTileAbs(absPosition, maptile);
    }

    /**
     * Sets an object on the absolute position of the map.
     *
     * @param absolutePosition: Position of the map tile absolute from the top left point.
     * @param mapTile:          MapTile to add.
     */
    private void setMapTileAbs(Vector2D absolutePosition, NextMapTile mapTile) {

        NextMapTile existingMapTile;

        // add dispenser and zones to found things
        if (mapTile != null) {
            if (mapTile.getThingType().startsWith("dispenser")) {
                if (!foundDispensers.contains(mapTile.getThingType())) foundDispensers.add((mapTile.getThingType().substring(10)));
            } else if (mapTile.getThingType().equals("goalZone")) foundGoalZone = true;
            else if (mapTile.getThingType().equals("roleZone")) foundRoleZone = true;
        }

        Vector2D offset = new Vector2D(extendArray(absolutePosition));
        absolutePosition.add(offset);

        existingMapTile = this.map[(int) absolutePosition.x][(int) absolutePosition.y];

        if (existingMapTile == null || existingMapTile.getLastVisionStep() <= existingMapTile.getLastVisionStep()) {
            this.map[(int) absolutePosition.x][(int) absolutePosition.y] = mapTile;
        }
    }

    /**
     * Calculates the most positive coordinate possible for the current map. If the map is of size 10/10 and the
     * zero point is at 5/5, the most positive coordinate is at 4/4.
     *
     * @return Vector with most positive coordinate
     */
    private Vector2D getPositiveExtend() {
        return new Vector2D(map.length - (int) zeroPoint.x - 1, map[0].length - (int) zeroPoint.y - 1);
    }

    /**
     * Calculates the most negative Coordinate possible for the current map. If the map is of size 10/10 and the
     * zero point is at 5/5, the most negative coordinate is -5/-5.
     *
     * @return Vector with most negative coordinate
     */
    private Vector2D getNegativeExtend() {
        return new Vector2D(-1 * (int) zeroPoint.x, -1 * (int) zeroPoint.y);
    }

    /**
     * Calculates the size of the map.
     *
     * @return Vector object, which represents the number of elements in x- and y-direction.
     */
    private Vector2D getSizeOfMap() {
        return new Vector2D(map.length, map[0].length);
    }

    /**
     * Extends the size of the map object either in x+, x-, y+ or y- direction if the map is too small.
     *
     * @param positionMapTile position of the map tile to be added relative to the starting position.
     */
    private Vector2D extendArray(Vector2D positionMapTile) {

        int minExtend = 1;
        Vector2D numExtend = new Vector2D(0, 0);
        Vector2D offset = new Vector2D(0, 0);
        Vector2D sizeOfMap = getSizeOfMap();

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

            NextMapTile[][] tmp = new NextMapTile[(int) sizeOfMap.x][(int) sizeOfMap.y];

            // Copy existing map to tmp map
            for (int i = 0; i < this.map.length; i++) {
                for (int j = 0; j < this.map[i].length; j++) {
                    tmp[i + (int) offset.x][j + (int) offset.y] = this.map[i][j];
                }
            }
            this.zeroPoint.add(offset.x, offset.y);
            this.map = tmp;
        }
        return offset;
    }

    /**
     * Check if the rotation cw or ccw is possible
     *
     * @param direction
     * @param attachedElements
     * @return true if the rotation is possible else otherwise
     */
    public boolean IsRotationPossible(Identifier direction, HashSet<Point> attachedElements) {
        if (direction.getValue() == "cw") {
            if (attachedElements.contains(NextConstants.NorthPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(1, 0)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }
            if (attachedElements.contains(NextConstants.EastPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(0, -1)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }
            if (attachedElements.contains(NextConstants.SouthPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(-1, 0)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }
            if (attachedElements.contains(NextConstants.WestPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(0, 1)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }

        } else {
            if (attachedElements.contains(NextConstants.NorthPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(-1, 0)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }
            if (attachedElements.contains(NextConstants.EastPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(0, 1)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }
            if (attachedElements.contains(NextConstants.SouthPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(1, 0)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }
            if (attachedElements.contains(NextConstants.WestPoint)) {
                String mapTileType = getMapTileRel(new Vector2D(0, -1)).getThingType();
                if (mapTileType == "obstacle" || mapTileType == "block" || mapTileType == "entity" || mapTileType == "dispenser")
                    return false;
            }
        }
        return true;
    }

    /**
     * Prüft, ob alle benötigten Blöcke für eine Aufgabe und eine goalZone bereits bekannt sind
     * @param requiredBlocks
     * @return
     */
    public boolean IsTaskExecutable(HashSet<String> requiredBlocks) {
        if (foundGoalZone && foundDispensers.containsAll(requiredBlocks)) return true;
        return false;
    }
}
