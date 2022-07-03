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
    //private NextAgent agent;
    private HashSet<String> excludeThingTypes;

    private NextGroup group;

    private HashSet<NextMapTile> dispensers = new HashSet<>();
    private HashSet<NextMapTile> goalZones = new HashSet<>();
    private HashSet<NextMapTile> roleZones = new HashSet<>();

    private boolean foundDispenser = false;
    private boolean foundRoleZone = false;
    private boolean foundGoalZone = false;

    private HashSet<String> availableDispensers = new HashSet<String>(); // Speichert nur die Blocktypen (b0, b1, etc) ab

    public NextMap(NextGroup group) {
        map = new NextMapTile[1][1];
        map[0][0] = new NextMapTile(0, 0, 0, "unknown");
        excludeThingTypes = new HashSet<>(Arrays.asList("entity", "block"));
        this.group = group;
    }

    public NextMap(NextAgent agent) {
        map = new NextMapTile[1][1];
        map[0][0] = new NextMapTile(0, 0, 0, "unknown");

    //    this.agent = agent;

        excludeThingTypes = new HashSet<>(Arrays.asList("entity", "block"));
    }

    /**
     * Add an array of things to the map.
     * @param agentPosition Current position of the agent (absolute)
     * @param percept Array of things as NextMapTiles. Position within NextMapTile is local (rel. to the agents position)
     */
    public void AddPercept(Vector2D agentPosition, HashSet<NextMapTile> percept) {
        NextMapTile clonedMaptile;
        for (NextMapTile maptile : percept) {
            clonedMaptile = maptile.clone();
            clonedMaptile.SetPosition(agentPosition.getAdded(clonedMaptile.GetPosition()));
            setMapTile(clonedMaptile);
        }
    }

    /**
     * Write map to file with 0/0 in top left corner. First letter of getThingType()
     * is used for representation. For example: agent -> a, obstacle -> o, dispenser -> d
     *
     * @param filename File for export
     */
    public void WriteToFile(String filename) {
        StringBuilder strMap = new StringBuilder();
        String pos;
        for (int j = 0; j < map[0].length; j++) {
            for (int i = 0; i < map.length; i++) {
                //pos = map[i][j].getPositionX().toString();
                //pos += "/" + map[i][j].getPositionY().toString() + " ";
                strMap.append(map[i][j].getThingType().charAt(0)).append(" "); //.append(pos);
            }
            strMap.append("\n");
        }

        FileWriter myWriter;
        try {
            myWriter = new FileWriter(filename);
            myWriter.write(strMap.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void setMapTile(NextMapTile maptile) {

        NextMapTile existingMapTile;

        Vector2D moveArray = new Vector2D(extendArray(maptile.GetPosition()));

        group.MoveAllAgents(moveArray);
        maptile.MovePosition(moveArray);

        existingMapTile = GetMapTile(maptile.GetPosition());

        // Check if type of maptile is part of the exlude list. If yes, set flag addMaptile to false
        boolean addMaptile = true;
        for (String e : excludeThingTypes) {
            if (maptile.getThingType().startsWith(e))
                addMaptile = false;
        }

        // Only add maptile if: flag addMapTile is true AND (existingMapTile is null OR existingMapTile is older)

        if (addMaptile && (existingMapTile == null || existingMapTile.getLastVisionStep() <= maptile.getLastVisionStep())) {
            this.map[maptile.getPositionX()][maptile.getPositionY()] = maptile;

            switch (maptile.getThingType().substring(0,4)) {
                case "disp":
                    addTo(dispensers, maptile);
                    foundDispenser = true;
                    availableDispensers.add((maptile.getThingType().substring(10)));
                    break;
                case "goal":
                    addTo(goalZones, maptile);
                    foundGoalZone = true;
                    break;
                case "role":
                    addTo(roleZones, maptile);
                    foundRoleZone = true;
                    break;
            }
        }
    }

    /**
     * Helper Function to add dispensers, goalZones and role Zones to their list
     * @param hashSet HashSet to which the maptile should be added
     * @param maptile maptile to be added
     */
    private void addTo(HashSet<NextMapTile> hashSet, NextMapTile maptile) {
        for (NextMapTile mt : hashSet) {
            if (maptile.GetPosition().equals(mt.GetPosition())) {
                return;
            }
        }
        hashSet.add(maptile.Clone());
    }

    /**
     * Extends the size of the map object either in x+, x-, y+ or y- direction if the map is too small.
     *
     * @param positionMapTile position of the map tile to be added relative to the upper left corner
     */
    private Vector2D extendArray(Vector2D positionMapTile) {

        int minExtend = 1; // ToDo: Should be extended in the future for higher efficiency. 1 is good for debugging.
        Vector2D numExtend = new Vector2D(0, 0);
        Vector2D offset = new Vector2D(0, 0);
        Vector2D sizeOfMap = GetSizeOfMap();

        // Find if extension is needed in x-direction
        if (positionMapTile.x >= map.length) {
            numExtend.x = Math.max(positionMapTile.x - map.length + 1, minExtend);
        } else if (positionMapTile.x < 0) {
            numExtend.x = Math.max(-1 * positionMapTile.x, minExtend);
            offset.x = numExtend.x;
        }

        // Find if extension is needed in y-direction
        if (positionMapTile.y >= map[0].length) {
            numExtend.y = Math.max(positionMapTile.y - map[0].length + 1, minExtend);
        } else if (positionMapTile.y < 0) {
            numExtend.y = Math.max(-1 * positionMapTile.y, minExtend);
            offset.y = numExtend.y;
        }

        if (!numExtend.equals(new Vector2D(0, 0))) {
            sizeOfMap.add(numExtend);

            // Create extended array + fill with "unknown" maptiles
            NextMapTile[][] tmp = new NextMapTile[sizeOfMap.x][sizeOfMap.y];
            for (int i = 0; i < tmp.length; i++) {
                for (int j = 0; j < tmp[i].length; j++) {
                    // Note: X/Y are normally relative to the agents position. Here, they are set to 0.
                    tmp[i][j] = new NextMapTile(i, j, 0, "unknown");
                }
            }

            // Copy existing map to tmp map
            NextMapTile newMapTile;
            for (int i = 0; i < this.map.length; i++) {
                for (int j = 0; j < this.map[i].length; j++) {
                    newMapTile = this.map[i][j].Clone();
                    newMapTile.MovePosition(offset);
                    tmp[i + offset.x][j + offset.y] = newMapTile;
                }
            }

            // Replace existing map
            this.map = tmp;

            // Move existing dispensers, goalZones and roleZones
            for (NextMapTile disp : dispensers) {
                disp.MovePosition(offset);
            }
            for (NextMapTile goal : goalZones) {
                goal.MovePosition(offset);
            }
            for (NextMapTile role : roleZones) {
                role.MovePosition(offset);
            }
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
        if (foundGoalZone && availableDispensers.containsAll(requiredBlocks)) {
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
        //return foundGoalZone;
    }
    
    public Boolean IsRoleZoneAvailable() {
        return !roleZones.isEmpty();
    	//return foundRoleZone;
    }
    
    public Boolean IsDispenserAvailable() {
        return !dispensers.isEmpty();
        //System.out.println("IsDispenserAvailable: \n" + foundDispenser);
    	//return foundDispenser;
    }
    
    //------------- TEST
    
    public static void UpdateMap(NextAgent agent) {

        Vector2D position = agent.GetGroup().GetAgentPosition(agent);
        NextMap map = agent.GetGroup().GetGroupMap();
        
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

            agent.MovePosition(lastStep); // actually moves the position within the group

            // 1. Add all maptiles of view as "free"
            HashSet<NextMapTile> view = new HashSet<>();

            int vision = agent.GetAgentStatus().GetCurrentRole().GetVision();

            for (int i = -1 * vision; i <= vision; i++) {
                for (int j = -1 * vision; j <= vision; j++) {
                    if (Math.abs(i) + Math.abs(j) <= vision) {
                    	// TODO Der Teil muss kluger ersetzt werden
                    	Iterator<NextMapTile> goalZoneIt = agent.GetAgentStatus().GetGoalZones().iterator();
                    	while(goalZoneIt.hasNext()) {
                    		NextMapTile next = goalZoneIt.next();
                    		if(i == next.getPositionX() && j == next.getPositionY()) {
                                view.add(new NextMapTile(i, j, agent.GetSimulationStatus().GetCurrentStep(), "goalZone"));
                            }
                        }
                        Iterator<NextMapTile> roleZoneIt = agent.GetAgentStatus().GetRoleZones().iterator();
                        while (roleZoneIt.hasNext()) {
                            NextMapTile next = roleZoneIt.next();
                            if (i == next.getPositionX() && j == next.getPositionY()) {
                                view.add(new NextMapTile(i, j, agent.GetSimulationStatus().GetCurrentStep(), "roleZone"));
                            }
                        }
                        view.add(new NextMapTile(i, j, agent.GetSimulationStatus().GetCurrentStep(), "free"));
                    }
                }
            }
            map.AddPercept(position, view);

            // 2. Add things, which are visible but not attached to the agent (overwrites maptiles from step 1)
            HashSet<NextMapTile> visibleNotAttachedThings = new HashSet<>();

            for (NextMapTile thing : agent.GetAgentStatus().GetVisibleThings()) {
                if (!agent.GetAgentStatus().GetAttachedElements().contains(thing.GetPosition())) {
                    visibleNotAttachedThings.add(thing);
                }
            }
            map.AddPercept(position, visibleNotAttachedThings);

            // 3. Add obstacles within view (overwrites maptiles from steps 1 and 2)
            map.AddPercept(position, agent.GetAgentStatus().GetObstacles());

            // Only for debugging
            /*
            map.WriteToFile("map_" + agent.GetGroup().getGroupID() + ".txt");

            try {
                Thread.sleep(0); // Wait for 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
             */
        }
    }
    
    public static NextMap JoinMap ( NextMap mapToKeep, NextMap mapToAdd, Vector2D offset) {

        // Not yet implemented
        return mapToKeep;
    }
}
