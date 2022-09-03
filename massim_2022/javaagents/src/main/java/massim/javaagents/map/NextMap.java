package massim.javaagents.map;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;

/**
 * Interne Karte zum Speichern bisher wahrgenommener Dinge wie z.B. Hindernisse, Dispenser, Goal- oder Role-Zones.
 * @author Sebastian Loder
 *
 */
public class NextMap {

    private HashSet<NextMapTile> maplist = new HashSet<>();
    private HashSet<String> excludeThingTypes = new HashSet<>(Arrays.asList("entity", "block", "unknown"));;
    private NextGroup group;
    private Vector2D simulationMapSize = new Vector2D(-1, -1);;
    private HashSet<NextMapTile> dispensers = new HashSet<>();
    private HashSet<NextMapTile> goalZones = new HashSet<>();
    private HashSet<NextMapTile> roleZones = new HashSet<>();
    private HashSet<String> availableDispensers = new HashSet<String>(); // Speichert nur die Blocktypen (b0, b1, etc) ab

    /**
     * Creates a new Map
     *
     * @param group Group which uses the map
     */
    public NextMap(NextGroup group) {
        this.group = group;
    }

    // ToDo: Check if constructor is still needed
    /**
     * Creates a new Map
     *
     * @param agent Agent
     */
    public NextMap(NextAgent agent) {
    }

    /**
     * Add multiple things to the map.
     *
     * @param agentPosition Position of the agent relative to the upper left corner
     * @param percept HashSet of NextMapTiles with local position (relative to the agents position)
     */
    public void AddPercept(Vector2D agentPosition, HashSet<NextMapTile> percept) {
        for (NextMapTile maptile : percept) {
            NextMapTile clonedMaptile = maptile.clone();
            clonedMaptile.MovePosition(agentPosition);
            setMapTile(clonedMaptile);
        }
    }

    // ToDo: Update method from maptiles-in-hashset
    /**
     * Write map to txt file with 0/0 in top left corner. First letter of
     * getThingType() is used for representation, e.g. obstacle -> o.
     *
     * @param filename File for export
     */
    public void WriteToFile(String filename, int step) {

        NextMapTile[][] mapArray = GetMapArray();
        Vector2D size = GetSizeOfMap();

        String[][] stringMap = new String[size.x][size.y];

        for (int j = 0; j < mapArray[0].length; j++) {
            for (int i = 0; i < mapArray.length; i++) {
                stringMap[i][j] = "";
            }
        }

        Vector2D pos;
        for (NextAgent agent : group.GetAgents()) {
            pos = agent.GetPosition();

            if (IsOnMap(pos)){
                stringMap[pos.x][pos.y] += "a";
            }
        }

        for (NextMapTile maptile : roleZones) {
            pos = maptile.GetPosition();
            stringMap[pos.x][pos.y] += maptile.getThingType().charAt(0);
        }

        for (NextMapTile maptile : goalZones) {
            pos = maptile.GetPosition();
            stringMap[pos.x][pos.y] += maptile.getThingType().charAt(0);
        }

        for (NextMapTile maptile : dispensers) {
            pos = maptile.GetPosition();
            stringMap[pos.x][pos.y] += maptile.getThingType().charAt(0);
        }

        for (int j = 0; j < mapArray[0].length; j++) {
            for (int i = 0; i < mapArray.length; i++) {
                stringMap[i][j] += mapArray[i][j].getThingType().charAt(0);
            }
        }

        StringBuilder outputString = new StringBuilder();

        outputString.append("Step: " + step + "\n");

        String tmpString;
        for (int j = 0; j < mapArray[0].length; j++) {
            for (int i = 0; i < mapArray.length; i++) {
                tmpString = stringMap[i][j];
                int rfill = 4 - tmpString.length();
                for (int f = 0; f < rfill; f++) {
                    tmpString += " ";
                }
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
     * Checks if a given position is on the map (x/y not negative and not
     * greater than current size)
     *
     * @param pos Position to check
     * @return true/false
     */
    public boolean IsOnMap(Vector2D pos) {
        Vector2D size = GetSizeOfMap();
        if (pos.x >= 0 && pos.y >= 0 && pos.x < size.x && pos.y < size.y) {
            return true;
        }
        return false;
    }

    /**
     * Returns a Vector2D-object with the highest map-value in x- and y-direction.
     *
     * @return
     */
    public Vector2D GetSizeOfMap() {

        // Build HashSet with all positions on the map (maptiles + agents)
        HashSet<Vector2D> positions = new HashSet<>();
        positions.addAll(group.GetAgentPositions());
        positions.addAll(Vector2D.extractPositionsFromMapTiles(maplist));
        positions.addAll(Vector2D.extractPositionsFromMapTiles(dispensers));
        positions.addAll(Vector2D.extractPositionsFromMapTiles(goalZones));
        positions.addAll(Vector2D.extractPositionsFromMapTiles(roleZones));

        // Calculate max. value
        Vector2D size = Vector2D.getMax(positions);
        size.add(1, 1);

        return size;
    }

    /**
     * Returns all dispensers found so far as NextMapTiles. X/Y of each maptile
     * is the position on the map
     *
     * @return Dispensers
     */
    public HashSet<NextMapTile> GetDispensers() {
        return dispensers;
    }

    /**
     * Returns all RoleZones found so far as NextMapTiles. X/Y of each maptile
     * is the position on the map
     *
     * @return RoleZones
     */
    public HashSet<NextMapTile> GetRoleZones() {
        return roleZones;
    }

    /**
     * Returns all GoalZones found so far as NextMapTiles. X/Y of each maptile
     * is the position on the map
     *
     * @return GoalZones
     */
    public HashSet<NextMapTile> GetGoalZones() {
        return goalZones;
    }

    /**
     * Returns a map tile at a given position (possible values are obstacle, free or unknown)
     * Dispensers, GoalZones and RoleZones are not considered!
     *
     * @param position
     * @return maptile object
     */
    public NextMapTile GetMapTile(Vector2D position) {
        for (NextMapTile maptile : maplist) {
            if (maptile.GetPosition().equals(position)) {
                return maptile;
            }
        }
        return new NextMapTile(position, 0, "unknown");
    }

    /**
     * Sets an object on the absolute position of the map.
     *
     * @param maptile: NextMapTile to add.
     */
    public void setMapTile(NextMapTile maptile) {

        // Check if type of maptile is part of the exclude list. If yes, set flag addMaptile to false
        for (String e : excludeThingTypes) {
            if (maptile.getThingType().startsWith(e)) {
                return;
            }
        }

        // Transfer StepMemory to new MapTile
        NextMapTile existingMapTile = GetMapTile(maptile.GetPosition());
        if (existingMapTile != null) {
            maptile.AddToStepMemory(existingMapTile.GetStepMemory());
        }

        // Only add maptile if: existingMapTile is null OR existingMapTile is older
        Vector2D maptilePosition = maptile.GetPosition();
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
                removeMapTile(roleZones, maptilePosition);
                removeMapTile(goalZones, maptilePosition);
                removeMapTile(dispensers, maptilePosition);
                removeMapTile(maplist, maptilePosition);
                maplist.add(maptile);
                break;
            case "obst":
                removeMapTile(maplist, maptilePosition);
                maplist.add(maptile);
                break;
        }
    }

    /**
     * Removes a maptile of a given hashSet at a specific position
     * @param hashSet
     * @param pos
     */
    private void removeMapTile(HashSet<NextMapTile> hashSet, Vector2D pos) {
        Predicate<NextMapTile> condition = maptile -> maptile.GetPosition().equals(pos);
        hashSet.removeIf(condition);
    }

    /**
     * Move all maptiles of a specific hashSet
     *
     * @param hashSet hashSet with the MapTile objects
     * @param offset offset by which the maptiles should be be moved
     */
    private HashSet<NextMapTile> moveMaptiles(HashSet<NextMapTile> hashSet, Vector2D offset) {
        HashSet<NextMapTile> newHashSet = new HashSet<>();
        for (NextMapTile tile : hashSet) {
            NextMapTile newTile = tile.Clone();
            newTile.MovePosition(offset);
            newHashSet.add(newTile);
        }
        return newHashSet;
    }

    /**
     * Move all maptiles of all thing types
     *
     * @param offset offset by which the maptiles should be moved
     */
    private void moveMapTiles(Vector2D offset) {
        maplist = moveMaptiles(maplist, offset);
        dispensers = moveMaptiles(dispensers, offset);
        goalZones = moveMaptiles(goalZones, offset);
        roleZones = moveMaptiles(roleZones, offset);
    }

    /**
     * Modulo all maptiles of a specific hashSet
     *
     * @param hashSet hashSet with the MapTile objects
     * @param mod modulo value to be applied
     */
    private HashSet<NextMapTile> modMapTiles(HashSet<NextMapTile> hashSet, Vector2D mod) {
        HashSet<NextMapTile> newHashSet = new HashSet<>();
        for (NextMapTile tile : hashSet) {
            NextMapTile newTile = tile.Clone();
            newTile.ModPosition(mod);
            newHashSet.add(newTile);
        }
        return newHashSet;
    }

    /**
     * Modulo all maptiles of all thing types
     *
     * @param mod modulo value to be applied
     */
    private void modMapTiles(Vector2D mod) {
        maplist = modMapTiles(maplist, mod);
        dispensers =  modMapTiles(dispensers, mod);
        goalZones = modMapTiles(goalZones, mod);
        roleZones = modMapTiles(roleZones, mod);
    }

    /**
     * Checks if a given list of block types is already known (... or the dispensers to get the blocks from).
     * Only results in true, if min 1 goalZone is known, too.
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
     * Returns the map with unknown, free or obstacle - maptiles (dispenser, goalZones, roleZones are not considered)
     *
     * @return map object
     */
    public NextMapTile[][] GetMapArray() {
        Vector2D size = GetSizeOfMap();

        NextMapTile[][] mapArray = new NextMapTile[size.x][size.y];

        // Fill with "unknown" tiles
        for (int i = 0; i < size.x; i++) {
            for (int j = 0; j < size.y; j++) {
                mapArray[i][j] = new NextMapTile(i, j, 0, "unknown");
            }
        }

        // Fill with tiles from maplist
        for (NextMapTile maptile : maplist) {
            mapArray[maptile.getPositionX()][maptile.getPositionY()] = maptile.clone();
        }

        return mapArray;
    }
    

    // ToDo: Wird das noch benötigt?
    public static NextMapTile[][] CenterMapAroundPosition(NextMapTile[][] mapOld, Vector2D position) {
        if (mapOld.length == 1 && mapOld[0].length == 1) {
            return mapOld;
        }

        int mapWidth = mapOld.length;
        int mapHeight = mapOld[0].length;
        int xOffset = (int) position.x - ((int) (mapWidth / 2));
        int yOffset = (int) position.y - ((int) (mapHeight / 2));
        NextMapTile[][] tempMap = new NextMapTile[mapWidth][mapHeight];

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int oldX = (x - xOffset + mapWidth) % (mapWidth - 1);
                int oldY = (y - yOffset + mapHeight) % (mapHeight - 1);
                tempMap[x][y] = new NextMapTile(
                        x,
                        y,
                        mapOld[oldX][oldY].getLastVisionStep(),
                        mapOld[oldX][oldY].getThingType(),
                        mapOld[oldX][oldY].GetStepMemory());
            }
        }
        return tempMap;
    }

    /**
     * Creates a copy of the map
     *
     * @param mapOld
     * @return Returns an copy of the map
     */
    public static NextMapTile[][] CloneMapArray(NextMapTile[][] mapOld) {

        if (mapOld.length == 1 && mapOld[0].length == 1) {
            return mapOld;
        }

        int mapWidth = mapOld.length;
        int mapHeight = mapOld[0].length;
        NextMapTile[][] tempMap = new NextMapTile[mapWidth][mapHeight];

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                tempMap[x][y] = new NextMapTile(
                        x,
                        y,
                        mapOld[x][y].getLastVisionStep(),
                        mapOld[x][y].getThingType(),
                        mapOld[x][y].GetStepMemory());
            }
        }
        return tempMap;
    }

    /**
     * Return the positions of dispensers
     * @return
     */
    public HashSet<Vector2D> GetDispenserPositions() {
        return Vector2D.extractPositionsFromMapTiles(dispensers);
    }

    /**
     * Return the positions of goalZones
     * @return
     */
    public HashSet<Vector2D> GetGoalZonePositions() {
        return Vector2D.extractPositionsFromMapTiles(goalZones);
    }

    /**
     * Return the positions of roleZones
     * @return
     */
    public HashSet<Vector2D> GetRoleZonePositions() {
        return Vector2D.extractPositionsFromMapTiles(roleZones);
    }

    public String MapToStringBuilder() {
        return MapToStringBuilder(GetMapArray());
    }

    public static String MapToStringBuilder(NextMapTile[][] map) {
        return MapToStringBuilder(map, new HashSet<>(), new HashSet<>());
    }

    public static String MapToStringBuilder(NextMapTile[][] map, HashSet<Vector2D> agents, HashSet<Vector2D> dispenser) {
        StringBuilder stringForReturn = new StringBuilder();

        for (int y = 0; y < map[0].length; y++) {
            StringBuilder subString = new StringBuilder();

            for (int x = 0; x < map.length; x++) {
                if (dispenser.contains(new Vector2D(x, y))) {
                    subString.append("D");
                    continue;
                }
                if (agents.contains(new Vector2D(x, y))) {
                    subString.append("A");
                    continue;
                }

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

    /**
     * Returns true if a goalZone was found so far, otherwise false
     * @return
     */
    public Boolean IsGoalZoneAvailable() {
        return !goalZones.isEmpty();
    }

    /**
     * Returns true if a roleZone was found so far, otherwise false
     * @return
     */
    public Boolean IsRoleZoneAvailable() {
        return !roleZones.isEmpty();
    }

    /**
     * Returns true if a dispenser was found so far, otherwise false
     * @return
     */
    public Boolean IsDispenserAvailable() {
        return !dispensers.isEmpty();
    }

    /**
     * Updates the group map with the information of a single agent in each step.
     * The update includes the movement of the agent as well as the received percept.
     *
     * @param agent Agent for which the update should be executed
     */
    public static void UpdateMap(NextAgent agent) {

        NextMap map = agent.GetMap();

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

            Vector2D agentPosition = agent.GetPositionRef();

            // 1. Add all maptiles of view as "free"
            HashSet<NextMapTile> view = new HashSet<>();
            int vision = agent.GetAgentStatus().GetCurrentRole().GetVision();
            HashSet<Vector2D> vectorsInView = generateVectorsInView(vision, false);
            for (Vector2D v : vectorsInView) {
                view.add(new NextMapTile(v, agent.GetSimulationStatus().GetCurrentStep(), "free"));
            }
            map.AddPercept(agentPosition, view);

            // 2. Add things, which are visible but not attached to the agent (overwrites maptiles from step 1)
            HashSet<NextMapTile> visibleNotAttachedThings = new HashSet<>();
            for (NextMapTile thing : agent.GetAgentStatus().GetVisibleThings()) {
                if (!agent.GetAgentStatus().GetAttachedElementsVector2D().contains(thing.GetPosition())) {
                    visibleNotAttachedThings.add(thing);
                }
            }
            map.AddPercept(agentPosition, visibleNotAttachedThings);

            // 3. Add obstacles, goalZones and roleZones within view (overwrites maptiles from steps 1 and 2)
            map.AddPercept(agentPosition, agent.GetAgentStatus().GetObstacles());
            map.AddPercept(agentPosition, agent.GetAgentStatus().GetGoalZones());
            map.AddPercept(agentPosition, agent.GetAgentStatus().GetRoleZones());

            // Shift map to zero (important before WriteToFile()!)
            map.shiftToZero();

            // Only for debugging

            // map.WriteToFile("map_" + agent.GetGroup().GetGroupID() + ".txt", agent.GetSimulationStatus().GetCurrentStep());

        } else if (agent.GetAgentStatus().GetLastAction().contains("move") && !agent.GetAgentStatus().GetLastActionResult().equals("success")) {
            agent.clearAgentStepMemory();
        } else if (agent.GetAgentStatus().GetLastAction().contains("clear")) {
            agent.clearAgentStepMemory();
        }
        
    }

    /**
     * Generates a HashSet of Vector2D with all positions inside the view (diamond-shape)
     * @param vision vision range
     * @param includeCenter boolean to define if 0/0 should be included as Vector2D
     * @return
     */
    private static HashSet<Vector2D> generateVectorsInView(int vision, boolean includeCenter) {
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
     * Analyze the most negative value in x- and y-direction. Shift all maptiles and agents with this offset to be
     * at minimum at position 0/0.
     */
    public void shiftToZero() {

        // Build HashSet with all positions on the map (maptiles + agents)
        HashSet<Vector2D> positions = new HashSet<>();
        positions.addAll(group.GetAgentPositions());
        positions.addAll(Vector2D.extractPositionsFromMapTiles(maplist));
        positions.addAll(Vector2D.extractPositionsFromMapTiles(dispensers));
        positions.addAll(Vector2D.extractPositionsFromMapTiles(goalZones));
        positions.addAll(Vector2D.extractPositionsFromMapTiles(roleZones));

        // Calculate min. value
        Vector2D offset = Vector2D.getMin(positions);

        offset.reverse();

        // Move
        moveMapTiles(offset);
        group.MoveAllAgents(offset);

        // Mod
        modMapTiles(simulationMapSize);
        group.ModAllAgents(simulationMapSize);

    }

    /**
     * Analyze the hashSet of dispensers and update the list of available dispenser types.
     */
    private void updateAvailableDispensers() {
        for (NextMapTile maptile : dispensers) {
            availableDispensers.add((maptile.getThingType().substring(10)));
        }
    }

    private static HashSet<NextMapTile> mergeLists(HashSet<NextMapTile> hashSet1, HashSet<NextMapTile> hashSet2) {

        HashMap<Vector2D, NextMapTile> mergedHashMap = new HashMap<>();

        // Add all maptiles from hashSet1 to hashMap
        for (NextMapTile maptile : hashSet1) {
            mergedHashMap.put(maptile.GetPosition(), maptile);
        }

        // Add maptiles from hashSet2, if newer or not there yet
        for (NextMapTile maptile : hashSet2) {
            Vector2D pos = maptile.GetPosition();
            if (mergedHashMap.containsKey(pos)) {
                // If already there, compare maptile and merge
                NextMapTile existingMapTile = mergedHashMap.get(pos);
                NextMapTile newMapTile = maptile.clone();

                // If maptile from hashSet2 is newer, replace the existing one from hashSet1
                if (maptile.getLastVisionStep() > existingMapTile.getLastVisionStep()){
                    newMapTile.AddToStepMemory(existingMapTile.GetStepMemory());
                    mergedHashMap.put(pos, newMapTile);
                } else {
                    existingMapTile.AddToStepMemory(newMapTile.GetStepMemory());
                    mergedHashMap.put(pos, existingMapTile);
                }
            } else {
                // If not yet there, just add the maptile
                mergedHashMap.put(pos, maptile);
            }
        }

        // Transfer from HashMap to HashSet
        HashSet<NextMapTile> mergedHashSet = new HashSet<>();
        for (NextMapTile maptile : mergedHashMap.values()) {
            mergedHashSet.add(maptile);
        }

        return mergedHashSet;
    }

    /**
     * Joins one Map into another map
     *
     * @param mapToKeep The map in which the other map is joined / merged
     * @param mapToAdd The map to be joined/merged into the other map
     * @param offset Offset of both maps according to their upper left corner
     * @return
     */
    public static NextMap JoinMap(NextMap mapToKeep, NextMap mapToAdd, Vector2D offset) {

        mapToAdd.moveMapTiles(offset.getReversed());

        mapToKeep.maplist = NextMap.mergeLists(mapToAdd.maplist, mapToKeep.maplist);
        mapToKeep.dispensers = NextMap.mergeLists(mapToAdd.dispensers, mapToKeep.dispensers);
        mapToKeep.goalZones = NextMap.mergeLists(mapToAdd.goalZones, mapToKeep.goalZones);
        mapToKeep.roleZones = NextMap.mergeLists(mapToAdd.roleZones, mapToKeep.roleZones);

        mapToKeep.updateAvailableDispensers();

        mapToKeep.shiftToZero();

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

    private void resizeMap() {

        // Calc new mapsize
        Vector2D currentMapSize = GetSizeOfMap();
        Vector2D newMapSize = new Vector2D(currentMapSize.x, currentMapSize.y);
        if (simulationMapSize.x > 0) {
            newMapSize.x = simulationMapSize.x;
        }
        if (simulationMapSize.y > 0) {
            newMapSize.y = simulationMapSize.y;
        }

        modMapTiles(simulationMapSize);

        group.ModAllAgents(newMapSize);

    }
}
