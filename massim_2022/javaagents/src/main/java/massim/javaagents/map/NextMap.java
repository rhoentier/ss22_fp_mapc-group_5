package massim.javaagents.map;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;

import java.util.HashSet;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;

public class NextMap {

    private HashSet<NextMapTile> maplist = new HashSet<>();
    private HashSet<String> excludeThingTypes = new HashSet<>(Arrays.asList("entity", "block", "unknown"));;
    private NextGroup group;
    private Vector2D simulationMapSize;
    private HashSet<NextMapTile> dispensers = new HashSet<>();
    private HashSet<NextMapTile> goalZones = new HashSet<>();
    private HashSet<NextMapTile> roleZones = new HashSet<>();
    private HashSet<String> availableDispensers = new HashSet<String>(); // Speichert nur die Blocktypen (b0, b1, etc) ab

    public NextMap(NextGroup group) {
        this.group = group;
        simulationMapSize = new Vector2D(-1, -1);
    }

    public NextMap(NextAgent agent) {
        simulationMapSize = new Vector2D(-1, -1);
    }

    /**
     * Add an array of things to the map.
     *
     * @param agent
     * @param percept Array of things as NextMapTiles. Position within
     * NextMapTile is local (rel. to the agents position)
     */
    public void AddPercept(NextAgent agent, HashSet<NextMapTile> percept) {
        NextMapTile clonedMaptile;
        for (NextMapTile maptile : percept) {
            clonedMaptile = maptile.clone();
            clonedMaptile.SetPosition(clonedMaptile.GetPosition().getAdded(agent.GetPosition()));
            clonedMaptile.ModPosition(simulationMapSize);
            setMapTile(clonedMaptile);
        }
        shiftToZero();
    }

    /**
     * Write map to file with 0/0 in top left corner. First letter of
     * getThingType() is used for representation. For example: agent -> a,
     * obstacle -> o, dispenser -> d
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
                stringMap[pos.x][pos.y] += "A";
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
     * Returns the size of the map.
     *
     * @return Vector object, which represents the number of elements in x- and
     * y-direction.
     */
    public Vector2D GetSizeOfMap() {
        Vector2D size;
        size = posExtendInHashset(maplist);
        size = size.getMax(posExtendInHashset(dispensers));
        size = size.getMax(posExtendInHashset(goalZones));
        size = size.getMax(posExtendInHashset(roleZones));
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

    public HashSet<NextMapTile> GetGoalZones() {
        return goalZones;
    }

    /**
     * Sets an object on the map.
     * Returns a map tile at a position on the map with 0/0 in top left corner
     *
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

        Vector2D offset = new Vector2D(extendArray(this, maptile.GetPosition()));
        group.MoveAllAgents(offset);
        maptile.MovePosition(offset);

        NextMapTile existingMapTile = this.map[maptile.getPositionX()][maptile.getPositionY()];

        // Check if type of maptile is part of the exclude list. If yes, set flag addMaptile to false
        boolean addMaptile = true;
        for (String e : excludeThingTypes) {
            if (maptile.getThingType().startsWith(e)) {
                addMaptile = false;
            }
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
                    if (existingMapTile == null || existingMapTile.getLastVisionStep() < maptile.getLastVisionStep()) {
                        this.map[maptile.getPositionX()][maptile.getPositionY()] = maptile;
                    }
                        removeRoleZone(maptile.GetPosition());
                        removeGoalZone(maptile.GetPosition());
                        removeDispenser(maptile.GetPosition());
                    break;
                default:
                    if (existingMapTile == null || existingMapTile.getLastVisionStep() < maptile.getLastVisionStep()) {

                        this.map[maptile.getPositionX()][maptile.getPositionY()] = maptile;
                    }
            }
        }
        return offset;
    }

    /**
     * Removes a roleZone at a specific position from the list of roleZones
     *
     * @param pos Position
     */
    private void removeRoleZone(Vector2D pos) {
        this.roleZones.remove(new NextMapTile(pos.x, pos.y, 0, "roleZone"));
    }

    /**
     * Removes a roleZone at a specific position from the list of roleZones
     *
     * @param pos Position
     */
    private void removeDispenser(Vector2D pos) {
        this.dispensers.remove(new NextMapTile(pos.x, pos.y, 0, "dispenser"));
    }

    /**
     * Removes a goalZone at a specific position from the list of goalZones
     *
     * @param pos Position
     */
    private void removeGoalZone(Vector2D pos) {
        this.goalZones.remove(new NextMapTile(pos.x, pos.y, 0, "goalZone"));
    }

    /**
     * Moves all Maptiles within a HashSet by an offset. With HashSets it is
     * necessary to copy/paste all elements instead of just executing a function
     * for each element.
     *
     * @param hashSet HashSet to be shifted
     * @param offset Offset by which the data is to be moved
     * @return The new Hashset with the moved Maptiles
     */
    private static HashSet<NextMapTile> moveMaptilesInHashset(HashSet<NextMapTile> hashSet, Vector2D offset) {
        HashSet<NextMapTile> newHashSet = new HashSet<>();
        for (NextMapTile tile : hashSet) {
            NextMapTile newTile = tile.Clone();
            newTile.MovePosition(offset);
            newHashSet.add(newTile);
        }

        return newHashSet;
    }

    private static HashSet<NextMapTile> modMaptilesInHashset(HashSet<NextMapTile> hashSet, Vector2D mod) {
        HashSet<NextMapTile> newHashSet = new HashSet<>();
        for (NextMapTile tile : hashSet) {
            NextMapTile newTile = tile.Clone();
            newTile.ModPosition(mod);
            newHashSet.add(newTile);
        }

        return newHashSet;
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
     *
     * @return Map
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
                        mapOld[oldX][oldY].getThingType());
            }
        }
        return tempMap;
    }

    /**
     * Creates a copy of the map and transforms the maptiles to absolte position
     *
     * @param mapOld
     * @return Returns an copy of the map with NextMapTiles using absolute
     * coordinates.
     */
    public static NextMapTile[][] copyAbsoluteMap(NextMapTile[][] mapOld) {
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
                        mapOld[x][y].getThingType());
            }
        }
        return tempMap;
    }

    public HashSet<Vector2D> GetDispenserPositions() {
        HashSet<Vector2D> werte = new HashSet<>();
        for (NextMapTile disp : dispensers) {
            werte.add(new Vector2D(disp.getPositionX(), disp.getPositionY()));
        }
        return werte;
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

    public Boolean IsGoalZoneAvailable() {
        return !goalZones.isEmpty();
    }

    public Boolean IsRoleZoneAvailable() {
        return !roleZones.isEmpty();
    }

    public Boolean IsDispenserAvailable() {
        return !dispensers.isEmpty();
    }

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


            map.WriteToFile("map_" + agent.GetGroup().getGroupID() + ".txt", agent.GetSimulationStatus().GetCurrentStep());
/*            try {
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

    private static Vector2D negExtendInHashset(HashSet<NextMapTile> hashSet) {
        Vector2D ext = new Vector2D(0, 0);
        for (NextMapTile maptile : hashSet) {
            ext = ext.getMin(maptile.GetPosition());
        }
        return ext;
    }

    private static Vector2D posExtendInHashset(HashSet<NextMapTile> hashSet) {
        Vector2D ext = new Vector2D(0, 0);
        for (NextMapTile maptile : hashSet) {
            ext = ext.getMax(maptile.GetPosition());
        }
        return ext;
    }

    private void shiftToZero() {
        Vector2D offset;

        offset = negExtendInHashset(maplist);
        offset = offset.getMin(negExtendInHashset(dispensers));
        offset = offset.getMin(negExtendInHashset(goalZones));
        offset = offset.getMin(negExtendInHashset(roleZones));

        offset.reverse();

        maplist = moveMaptilesInHashset(maplist, offset);
        dispensers = moveMaptilesInHashset(dispensers, offset);
        goalZones = moveMaptilesInHashset(goalZones, offset);
        roleZones = moveMaptilesInHashset(roleZones, offset);

        group.MoveAllAgents(offset);
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

        NextMapTile newMapTile;
        Vector2D sizeMapToAdd = mapToAdd.GetSizeOfMap();

        moveMaptilesInHashset(mapToAdd.maplist, offset.getReversed());
        moveMaptilesInHashset(mapToAdd.dispensers, offset.getReversed());
        moveMaptilesInHashset(mapToAdd.goalZones, offset.getReversed());
        moveMaptilesInHashset(mapToAdd.roleZones, offset.getReversed());

        mapToKeep.maplist.addAll(mapToAdd.maplist);
        mapToKeep.dispensers.addAll(mapToAdd.dispensers);
        mapToKeep.goalZones.addAll(mapToAdd.goalZones);
        mapToKeep.roleZones.addAll(mapToAdd.roleZones);

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

        dispensers = modMaptilesInHashset(dispensers, simulationMapSize);
        goalZones = modMaptilesInHashset(goalZones, simulationMapSize);
        roleZones = modMaptilesInHashset(roleZones, simulationMapSize);
        maplist = modMaptilesInHashset(maplist, simulationMapSize);

        group.ModAllAgents(newMapSize);

    }
}
