package massim.javaagents.map;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;

import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.ECardinals;

public class NextMap {

    private NextMapTile[][] map;
    private HashSet<String> excludeThingTypes;

    private NextGroup group;
    private Vector2D simulationMapSize;

    private HashSet<NextMapTile> dispensers = new HashSet<>();
    private HashSet<NextMapTile> goalZones = new HashSet<>();
    private HashSet<NextMapTile> roleZones = new HashSet<>();
    private HashSet<String> availableDispensers = new HashSet<String>(); // Speichert nur die Blocktypen (b0, b1, etc) ab

    public NextMap(NextGroup group) {
        map = new NextMapTile[1][1];
        map[0][0] = new NextMapTile(0, 0, 0, "unknown");
        excludeThingTypes = new HashSet<>(Arrays.asList("entity", "block"));
        this.group = group;
        simulationMapSize = new Vector2D(-1, -1);
    }

    public NextMap(NextAgent agent) {
        map = new NextMapTile[1][1];
        map[0][0] = new NextMapTile(0, 0, 0, "unknown");
        excludeThingTypes = new HashSet<>(Arrays.asList("entity", "block"));
        simulationMapSize = new Vector2D(-1, -1);
    }

    /**
     * Add an array of things to the map.
     * @param agent
     * @param percept Array of things as NextMapTiles. Position within NextMapTile is local (rel. to the agents position)
     */
    public void AddPercept(NextAgent agent, HashSet<NextMapTile> percept) {
        NextMapTile clonedMaptile;
        for (NextMapTile maptile : percept) {
            clonedMaptile = maptile.clone();
            clonedMaptile.SetPosition(clonedMaptile.GetPosition().getAdded(agent.GetPosition()));
            clonedMaptile.ModPosition(simulationMapSize);
            setMapTile(clonedMaptile);
        }
    }

    /**
     * Write map to file with 0/0 in top left corner. First letter of getThingType()
     * is used for representation. For example: agent -> a, obstacle -> o, dispenser -> d
     *
     * @param filename File for export
     */
    public void WriteToFile(String filename, int step) {

        Vector2D size = GetSizeOfMap();
        System.out.println(size);
        int x = size.x;
        int y = size.y;
        String[][] stringMap = new String[x][y];

        for (int j = 0; j < map[0].length; j++) {
            for (int i = 0; i < map.length; i++) {
                stringMap[i][j] = "";
            }
        }

        Vector2D pos;
        for(NextAgent agent : group.GetAgents()) {
            pos = agent.GetPosition();
            if (isOnMap(pos)){
                stringMap[pos.x][pos.y] += "A";
            }
        }

        for(NextMapTile maptile : roleZones) {
            pos = maptile.GetPosition();
            stringMap[pos.x][pos.y] += maptile.getThingType().charAt(0);
        }

        for(NextMapTile maptile : goalZones) {
            pos = maptile.GetPosition();
            stringMap[pos.x][pos.y] += maptile.getThingType().charAt(0);
        }

        for(NextMapTile maptile : dispensers) {
            pos = maptile.GetPosition();
            stringMap[pos.x][pos.y] += maptile.getThingType().charAt(0);
        }

        for (int j = 0; j < map[0].length; j++) {
            for (int i = 0; i < map.length; i++) {
                stringMap[i][j] += map[i][j].getThingType().charAt(0);
            }
        }

        StringBuilder outputString = new StringBuilder();

        outputString.append("Step: " + step + "\n");

        String tmpString;
        for (int j = 0; j < map[0].length; j++) {
            for (int i = 0; i < map.length; i++) {
                tmpString = stringMap[i][j];
                int rfill = 4 - tmpString.length();
                for (int f = 0; f < rfill; f++) {tmpString += " ";}
                outputString.append(tmpString);
            }
            outputString.append("\n");
        }

        FileWriter myWriter;
        try {
            myWriter = new FileWriter(filename);
            myWriter.write(outputString.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a given position is on the map (x/y not negative and not greater than current size)
     * @param pos Position to check
     * @return true/false
     */
    private boolean isOnMap(Vector2D pos) {
        Vector2D size = GetSizeOfMap();
        if(pos.x >= 0 && pos.y >= 0 && pos.x < size.x && pos.y < size.y){
            return true;
        }
        return false;
    }

    /**
     * Returns the size of the map.
     *
     * @return Vector object, which represents the number of elements in x- and y-direction.
     */
    public Vector2D GetSizeOfMap() {
        return new Vector2D(map.length, map[0].length);
    }

    /**
     * Returns all dispensers found so far as NextMapTiles. X/Y of each maptile is the position on the map
     * @return Dispensers
     */
    public HashSet<NextMapTile> GetDispensers() {return dispensers;}

    /**
     * Returns all RoleZones found so far as NextMapTiles. X/Y of each maptile is the position on the map
     * @return RoleZones
     */
    public HashSet<NextMapTile> GetRoleZones() {return roleZones;}

    public HashSet<NextMapTile> GetGoalZones() {return goalZones;}

    /**
     * Returns a map tile at a position on the map with 0/0 in top left corner
     * @param position
     * @return maptile object
     */
    public NextMapTile GetMapTile(Vector2D position) {
        return map[position.x][position.y];
    }

    /**
     * Sets an object on the absolute position of the map.
     *
     * @param maptile: MapTile to add.
     */
    public Vector2D setMapTile(NextMapTile maptile) {

        Vector2D offset = new Vector2D(extendArray(this, maptile.GetPosition()));
        group.MoveAllAgents(offset);
        maptile.MovePosition(offset);

        // Check if type of maptile is part of the exclude list. If yes, set flag addMaptile to false
        boolean addMaptile = true;
        for (String e : excludeThingTypes) {
            if (maptile.getThingType().startsWith(e))
                addMaptile = false;
        }

        // Only add maptile if: flag addMapTile is true AND (existingMapTile is null OR existingMapTile is older)
        if (addMaptile) {
            switch (maptile.getThingType().substring(0, 4)) {
                case "disp":
                    dispensers.add(maptile);
                    availableDispensers.add((maptile.getThingType().substring(10)));
                    break;
                case "goal":
                    goalZones.add(maptile);
                    break;
                case "role":
                    roleZones.add(maptile);
                    break;
                case "free":
                    this.map[maptile.getPositionX()][maptile.getPositionY()] = maptile;
                    removeRoleZone(maptile.GetPosition());
                    removeGoalZone(maptile.GetPosition());
                    break;
                default:
                    this.map[maptile.getPositionX()][maptile.getPositionY()] = maptile;
            }
        }

        return offset;
    }

    /**
     * Removes a roleZone at a specific position from the list of roleZones
     * @param pos Position
     */
    private void removeRoleZone(Vector2D pos) {
        this.roleZones.remove(new NextMapTile(pos.x, pos.y, 0, "roleZone"));
    }

    /**
     * Removes a goalZone at a specific position from the list of goalZones
     * @param pos Position
     */
    private void removeGoalZone(Vector2D pos) {
        this.goalZones.remove(new NextMapTile(pos.x, pos.y, 0, "goalZone"));
    }

    /**
     * Moves all Maptiles within a HashSet by an offset. With HashSets it is necessary to copy/paste all elements
     * instead of just executing a function for each element.
     * @param hashSet HashSet to be shifted
     * @param offset Offset by which the data is to be moved
     * @return The new Hashset with the moved Maptiles
     */
    private static HashSet<NextMapTile> moveMaptilesInHashset(HashSet<NextMapTile> hashSet, Vector2D offset) {
        HashSet<NextMapTile> newHashSet = new HashSet<>();
        for ( NextMapTile tile : hashSet ) {
            NextMapTile newTile = tile.Clone();
            newTile.MovePosition(offset);
            newHashSet.add(newTile);
        }

        return newHashSet;
    }

    private static HashSet<NextMapTile> modMaptilesInHashset(HashSet<NextMapTile> hashSet, Vector2D mod) {
        HashSet<NextMapTile> newHashSet = new HashSet<>();
        for ( NextMapTile tile : hashSet ) {
            NextMapTile newTile = tile.Clone();
            newTile.ModPosition(mod);
            newHashSet.add(newTile);
        }

        return newHashSet;
    }

    /**
     * Extends the size of the map object either in x+, x-, y+ or y- direction if the map is too small.
     *
     * @param positionMapTile position of the map tile to be added relative to the upper left corner
     */
    private static Vector2D extendArray(NextMap nextMap, Vector2D positionMapTile) {

        int minExtend = 1; // ToDo: Should be extended in the future for higher efficiency. 1 is good for debugging.
        Vector2D numExtend = new Vector2D(0, 0);
        Vector2D offset = new Vector2D(0, 0);
        Vector2D sizeOfMap = nextMap.GetSizeOfMap();

        // Find if extension is needed in x-direction
        if (positionMapTile.x >= nextMap.map.length) {
            numExtend.x = Math.max(positionMapTile.x - nextMap.map.length + 1, minExtend);
        } else if (positionMapTile.x < 0) {
            numExtend.x = Math.max(-1 * positionMapTile.x, minExtend);
            offset.x = numExtend.x;
        }

        // Find if extension is needed in y-direction
        if (positionMapTile.y >= nextMap.map[0].length) {
            numExtend.y = Math.max(positionMapTile.y - nextMap.map[0].length + 1, minExtend);
        } else if (positionMapTile.y < 0) {
            numExtend.y = Math.max(-1 * positionMapTile.y, minExtend);
            offset.y = numExtend.y;
        }

        if (!numExtend.equals(new Vector2D(0, 0))) {
            sizeOfMap.add(numExtend);

            // Create extended map + fill with "unknown" maptiles
            NextMapTile[][] tmp = new NextMapTile[sizeOfMap.x][sizeOfMap.y];
            for (int i = 0; i < tmp.length; i++) {
                for (int j = 0; j < tmp[i].length; j++) {
                    tmp[i][j] = new NextMapTile(i, j, 0, "unknown");
                }
            }

            // Copy existing map to extended map and replace existing map
            NextMapTile newMapTile;
            for (int i = 0; i < nextMap.map.length; i++) {
                for (int j = 0; j < nextMap.map[i].length; j++) {
                    newMapTile = nextMap.map[i][j].Clone();
                    newMapTile.MovePosition(offset);
                    tmp[i + offset.x][j + offset.y] = newMapTile;
                }
            }
            nextMap.map = tmp;

            // Move dispensers, goalZones and roleZones
            nextMap.dispensers = moveMaptilesInHashset(nextMap.dispensers, offset);
            nextMap.roleZones = moveMaptilesInHashset(nextMap.roleZones, offset);
            nextMap.goalZones = moveMaptilesInHashset(nextMap.goalZones, offset);
        }
        return offset;
    }

    /**
     * Prüft, ob alle benötigten Blöcke für eine Aufgabe und eine goalZone
     * bereits bekannt sind
     *
     * @param requiredBlocks
     * @return
     */
    public boolean IsTaskExecutable(HashSet<String> requiredBlocks) {
        if (IsGoalZoneAvailable() && availableDispensers.containsAll(requiredBlocks)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the map with coordinates 0/0 in upper left corner
     * @return Map
     */
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
    
    public Boolean IsGoalZoneAvailable() {
    	return !goalZones.isEmpty();
    }
    
    public Boolean IsRoleZoneAvailable() {
        return !roleZones.isEmpty();
    }
    
    public Boolean IsDispenserAvailable() {
        return !dispensers.isEmpty();
    }
    
    //------------- TEST
    
    public static void UpdateMap(NextAgent agent) {

        NextMap map = agent.GetMap();
        Vector2D position = agent.GetPosition();

        if (agent.GetAgentStatus().GetLastAction().equals("move") && agent.GetAgentStatus().GetLastActionResult().equals("success")) {

            Vector2D lastStep = new Vector2D(0, 0);

            switch (agent.GetAgentStatus().GetLastActionParams()) {
                case "[n]":
                    lastStep = new Vector2D(0, -1);
                    break;
                case "[e]":
                    lastStep = new Vector2D(1, 0);
                    break;
                case "[s]":
                    lastStep = new Vector2D(0, 1);
                    break;
                case "[w]":
                    lastStep = new Vector2D(-1, 0);
                    break;
            }


            agent.MovePosition(lastStep);
            agent.ModPosition();

            // 1. Add all maptiles of view as "free"
            HashSet<NextMapTile> view = new HashSet<>();

            int vision = agent.GetAgentStatus().GetCurrentRole().GetVision();

            HashSet<Vector2D> vectorsInView = generateVectorsInView(vision, false);
            for (Vector2D v : vectorsInView) {
                view.add(new NextMapTile(v, agent.GetSimulationStatus().GetCurrentStep(), "free"));
            }
            map.AddPercept(agent, view);

            // 2. Add things, which are visible but not attached to the agent (overwrites maptiles from step 1)
            HashSet<NextMapTile> visibleNotAttachedThings = new HashSet<>();

            for (NextMapTile thing : agent.GetAgentStatus().GetVisibleThings()) {
                if (!agent.GetAgentStatus().GetAttachedElementsVector2D().contains(thing.GetPosition())) {
                    visibleNotAttachedThings.add(thing);
                }
            }
            map.AddPercept(agent, visibleNotAttachedThings);

            // 3. Add obstacles within view (overwrites maptiles from steps 1 and 2)
            map.AddPercept(agent, agent.GetAgentStatus().GetObstacles());

            // 4. Add goal and role zones
            map.AddPercept(agent, agent.GetAgentStatus().GetGoalZones());
            map.AddPercept(agent, agent.GetAgentStatus().GetRoleZones());

            // Only for debugging
/*

            map.WriteToFile("map_" + agent.GetGroup().getGroupID() + ".txt", agent.GetSimulationStatus().GetCurrentStep());
            try {
                Thread.sleep(0); // Wait for 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // */
        }
    }

    /**
     *
     * @param vision
     * @param includeCenter
     * @return
     */
    private static HashSet<Vector2D> generateVectorsInView (int vision, boolean includeCenter){
        HashSet<Vector2D> view = new HashSet<>();
        for (int i = -1 * vision; i <= vision; i++) {
            for (int j = -1 * vision; j <= vision; j++) {
                if (Math.abs(i) + Math.abs(j) <= vision) {
                    if (i != 0 || j != 0 || includeCenter) {
                        view.add(new Vector2D(i, j));
                    }
                }
            }
        }
        return view;
    }

    /**
     * Joins one Map into another map
     * @param mapToKeep The map in which the other map is joined / merged
     * @param mapToAdd The map to be joined/merged into the other map
     * @param offset Offset of both maps according to their upper left corner
     * @return
     */
    public static NextMap JoinMap ( NextMap mapToKeep, NextMap mapToAdd, Vector2D offset) {

        NextMapTile newMapTile;
        Vector2D sizeMapToAdd = mapToAdd.GetSizeOfMap();

        for (int i = 0; i < sizeMapToAdd.x; i++) {
            for (int j = 0; j < sizeMapToAdd.y; j++) {
                newMapTile = mapToAdd.map[i][j].Clone();
                newMapTile.Subtract(offset);
                Vector2D mapMoved = mapToKeep.setMapTile(newMapTile);
                offset.subtract(mapMoved);
            }
        }
        return mapToKeep;
    }
    
    public void SetSimulationMapHeight(int MapHeight) {
        this.simulationMapSize.y = MapHeight;
        this.resizeMap();
    }

    public int GetSimulationMapHeight() {
        return this.simulationMapSize.y;
    }

    public void SetSimulationMapWidth(int MapWidth) {
        this.simulationMapSize.x = MapWidth;
        this.resizeMap();
    }

    public int GetSimulationMapWidth() {
        return this.simulationMapSize.x;
    }

    public Vector2D GetSimulationMapSize() {
        return this.simulationMapSize;
    }
    
    private void resizeMap(){

        // Calc new mapsize
        Vector2D currentMapSize = GetSizeOfMap();
        Vector2D newMapSize = new Vector2D(currentMapSize.x, currentMapSize.y);
        if (simulationMapSize.x > 0) newMapSize.x = simulationMapSize.x;
        if (simulationMapSize.y > 0) newMapSize.y = simulationMapSize.y;

        // Create map with reduced width/height + fill with "unknown" maptiles
        NextMapTile[][] tmp = new NextMapTile[newMapSize.x][newMapSize.y];
        for (int i = 0; i < newMapSize.x; i++) {
            for (int j = 0; j < newMapSize.y; j++) {
                tmp[i][j] = new NextMapTile(i, j, 0, "unknown");
            }
        }

        // Copy existing tiles to new map (with mod)
        for (int i = 0; i < currentMapSize.x; i++) {
            for (int j = 0; j < currentMapSize.y; j++) {
                Vector2D newPos = new Vector2D(i, j);
                newPos.mod(simulationMapSize);

                if (tmp[newPos.x][newPos.y] == null) tmp[newPos.x][newPos.y] = new NextMapTile(newPos.x, newPos.y, 0, "unknown");
                if (map[i][j].getLastVisionStep() > tmp[newPos.x][newPos.y].getLastVisionStep()) tmp[newPos.x][newPos.y] = map[i][j];
            }
        }

        dispensers = modMaptilesInHashset(dispensers, simulationMapSize);
        goalZones = modMaptilesInHashset(goalZones, simulationMapSize);
        roleZones = modMaptilesInHashset(roleZones, simulationMapSize);

        group.ModAllAgents(newMapSize);

        this.map = tmp;
    }
}
