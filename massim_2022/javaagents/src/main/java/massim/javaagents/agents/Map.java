import java.awt.*;

public class Map {

    private MapTile[][] map;
    private Point zeroPoint;

    public Map() {
        map = new MapTile[20][20];
        zeroPoint = new Point(0, 0);
        // ToDo: If implementation should be more efficient: Either add flags or separate list for static things.
    }

    /**
     * Add an array of things to the map.
     * @param agentPosition Current position of the agent relative to the starting position.
     * @param percept Array of things as MapTile-objects.
     */
    public void AddPercept(Point agentPosition, MapTile[] percept) {

        Point mapTilePosition = new Point();
        for (MapTile mapTile : percept) {
            mapTilePosition.setLocation(agentPosition);
            mapTilePosition.translate(mapTile.getPositionX(), mapTile.getPositionY());
            extendArray(mapTilePosition);
            addMapTile(mapTilePosition, mapTile);
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
     * Adds a MapTile-object to the map.
     * @param position: Position of the map tile relative to the starting position of the agent.
     * @param maptile: MapTile to add.
     */
    private void addMapTile(Point position, MapTile maptile) {
        this.map[this.zeroPoint.x + position.x][this.zeroPoint.y + position.y] = maptile;
    }

    /**
     * Removes a MapTile-object from the map.
     * @param position: Position of the map tile relative to the starting position of the agent.
     */
    private void removeMapTile(Point position) {
        this.map[this.zeroPoint.x + position.x][this.zeroPoint.y + position.y] = null;
    }

    /**
     * Calculates the most positive coordinate possible for the current map. If the map is of size 10/10 and the
     * zero point is at 5/5, the most positive coordinate is at 4/4.
     * @return Point with most positive coordinate
     */
    private Point getPositiveExtend() {
        return new Point (map.length-zeroPoint.x-1, map[0].length-zeroPoint.y-1);
    }

    /**
     * Calculates the most negative Coordinate possible for the current map. If the map is of size 10/10 and the
     * zero point is at 5/5, the most negative coordinate is -5/-5.
     * @return Point with most negative coordinate
     */
    private Point getNegativeExtend() {
        return new Point (-1 * zeroPoint.x, -1 * zeroPoint.y);
    }

    /**
     * Returns the size of the map.
     * @return Point object, which represents the number of elements in x- and y-direction.
     */
    private Point getSizeOfMap() {
        return new Point (map.length, map[0].length);
    }
    /**
     * Extends the size of the map object either in x+, x-, y+ or y- direction if the map is too small.
     * @param positionMapTile position of the map tile to be added relative to the starting position.
     */
    private void extendArray(Point positionMapTile) {

        int offsetX = 0;
        int offsetY = 0;
        int minExtend = 1;
        int numExtend;

        Point maxNegExt = getNegativeExtend();
        Point maxPosExt = getPositiveExtend();
        Point sizeOfMap = getSizeOfMap();

        if (positionMapTile.x > maxPosExt.x) {            // x+
            numExtend = Math.max(positionMapTile.x - maxPosExt.x, minExtend);
            sizeOfMap.x += numExtend;
        } else if (positionMapTile.x < maxNegExt.x) {     // x-
            numExtend = Math.max(maxNegExt.x - positionMapTile.x, minExtend);
            sizeOfMap.x += numExtend;
            offsetX = numExtend;
        } else if (positionMapTile.y > maxPosExt.y) {     // y+
            numExtend = Math.max(positionMapTile.y - maxPosExt.y, minExtend);
            sizeOfMap.y += numExtend;
        } else if (positionMapTile.y < maxNegExt.y) {     // y-
            numExtend = Math.max(maxNegExt.y - positionMapTile.y, minExtend);
            sizeOfMap.y += numExtend;
            offsetY = numExtend;
        } else {
            return;
        }

        MapTile[][] tmp = new MapTile[sizeOfMap.x][sizeOfMap.y];

        for (int i = 0; i < this.map.length; i++) {
            for (int j = 0; j < this.map[i].length; j++) {
                tmp[i + offsetX][j + offsetY] = this.map[i][j];
            }
        }

        this.zeroPoint.translate(offsetX, offsetY);
        this.map = tmp;
    }

}
