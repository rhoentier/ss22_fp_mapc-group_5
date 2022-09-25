package massim.javaagents.pathfinding;

import eis.iilang.Action;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.general.NextConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.map.Vector2D;

/**
 * Extended A* pathfinding algorithm for path generation. Optional StepMemory
 * feature: monitors, when tiles are going to be occupied
 *
 * @referenced Java version of basic A* - https://github.com/Qualia91/AStarAlg
 * @referenced Java version of JPS by Clint Mullins -
 * https://github.com/ClintFMullins/JumpPointSearch-Java/
 *
 * @author Alexander Lorenz
 */
public class NextAStarPath {

    /*
     * ########## region fields
     */
    private NextMapTile[][] map;
    private NextMapTile[][] originalMap;
    private int mapWidth;
    private int mapHeight;

    private NextMapTile currentTile;
    private int currentStep;
    private int[] targetPosition;
    private int[] localStartPoint;
    private Vector2D startpoint;

    private Boolean strictWalkable;
    private Boolean aStarJps;

    /*
     * ##################### endregion fields
     */
 /*
     * ########## region constructor.
     */
    /**
     * Shortcut for Pathfinding Processor. A*JPS, centerTheMap, strictWalkable -
     * disabled
     *
     * @param originalMap NextMapTile[][] - Array of Tiles to describe the
     * Environent
     * @param startpoint Vector2D - Position of Pathstart
     * @param target Vector2D - Position of targetpoint
     * @param currentStep int - current simulation step for StepMemory
     * @return List Collection of actions to describe the path
     */
    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, int currentStep) {
        return calculatePath(false, originalMap, startpoint, target, false, false, currentStep);
    }

    /**
     * Shortcut for Pathfinding Processor. A*JPS, strictWalkable - disabled
     *
     * @param originalMap NextMapTile[][] - Array of Tiles to describe the
     * Environent
     * @param startpoint Vector2D - Position of Pathstart
     * @param target Vector2D - Position of targetpoint
     * @param centerTheMap Boolean - true if map should be centered for optimal
 Distance calculation
     * @param currentStep int - current simulation step for StepMemory
     * @return List Collection of actions to describe the path
     */
    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, int currentStep) {
        return calculatePath(false, originalMap, startpoint, target, centerTheMap, false, currentStep);
    }

    /**
     * Shortcut for Pathfinding Processor. A*JPS - disabled
     *
     * @param originalMap NextMapTile[][] - Array of Tiles to describe the
     * Environent
     * @param startpoint Vector2D - Position of Pathstart
     * @param target Vector2D - Position of targetpoint
     * @param centerTheMap Boolean - true if map should be centered for optimal
 Distance calculation
     * @param strictWalkable Boolean - True if other agents and Blocks should be
     * considered as not Walkable (Used in local view)
     * @param currentStep int - current simulation step for StepMemory
     * @return List Collection of actions to describe the path
     */
    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, Boolean strictWalkable, int currentStep) {
        return calculatePath(false, originalMap, startpoint, target, centerTheMap, strictWalkable, currentStep);
    }

    /**
     * Shortcut for Pathfinding Processor. centerTheMap, strictWalkable -
     * disabled
     *
     * @param aStarJps Boolean - True to use A*JPS for faster calculation. Not
     * compatible with StepMemory
     * @param originalMap NextMapTile[][] - Array of Tiles to describe the
     * Environent
     * @param startpoint Vector2D - Position of Pathstart
     * @param target Vector2D - Position of targetpoint
     * @param currentStep int - current simulation step for StepMemory
     * @return List Collection of actions to describe the path
     */
    public List<Action> calculatePath(Boolean aStarJps, NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, int currentStep) {
        return calculatePath(aStarJps, originalMap, startpoint, target, false, false, currentStep);
    }

    /**
     * Shortcut for Pathfinding Processor. strictWalkable - disabled
     *
     * @param aStarJps Boolean - True to use A*JPS for faster calculation. Not
     * compatible with StepMemory
     * @param originalMap NextMapTile[][] - Array of Tiles to describe the
     * Environent
     * @param startpoint Vector2D - Position of Pathstart
     * @param target Vector2D - Position of targetpoint
     * @param centerTheMap Boolean - true if map should be centered for optimal
 Distance calculation
     * @param currentStep int - current simulation step for StepMemory
     * @return List Collection of actions to describe the path
     */
    public List<Action> calculatePath(Boolean aStarJps, NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, int currentStep) {
        return calculatePath(aStarJps, originalMap, startpoint, target, centerTheMap, false, currentStep);
    }

    /**
     * Full Pathfinding Processor, to generate path to a taget.
     *
     * @param aStarJps Boolean - True to use A*JPS for faster calculation. Not
     * compatible with StepMemory
     * @param originalMap NextMapTile[][] - Array of Tiles to describe the
     * Environent
     * @param startpoint Vector2D - Position of Pathstart
     * @param target Vector2D - Position of targetpoint
     * @param centerTheMap Boolean - true if map should be centered for optimal
 Distance calculation
     * @param strictWalkable Boolean - True if other agents and Blocks should be
     * considered as not Walkable (Used in local view)
     * @param currentStep int - current simulation step for StepMemory
     * @return List Collection of actions to describe the path
     */
    public List<Action> calculatePath(Boolean aStarJps, NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, Boolean strictWalkable, int currentStep) {

        this.aStarJps = aStarJps;
        this.originalMap = originalMap;
        this.mapWidth = originalMap.length;
        this.mapHeight = originalMap[0].length;
        this.strictWalkable = strictWalkable;
        this.currentStep = currentStep;
        this.startpoint = startpoint;

        // Guardcase - Check if the mapsize is sufficient
        if (this.mapWidth == 1 && mapHeight == 1) {
            System.out.println("Map is too small");
            return new ArrayList<>();
        }

        // Logic Gate - Checks if the map should be centered. 
        // Clone the map and adjust startPoint and targetPosition correspondingly.
        if (centerTheMap) {
            //- Centering the map 
            this.map = centerMapAroundPosition(originalMap, startpoint);
            int targetX = ((target.x + (mapWidth / 2 - startpoint.x)) % mapWidth);
            int targetY = ((target.y + (mapHeight / 2 - startpoint.y)) % mapHeight);
            this.localStartPoint = new int[]{(mapWidth / 2), (mapHeight / 2)};
            this.targetPosition = new int[]{targetX, targetY};
        } else {
            // - A Star without map shift
            this.map = NextMap.CloneMapArray(originalMap);
            int targetX = target.x;
            int targetY = target.y;
            this.localStartPoint = new int[]{startpoint.x, startpoint.y};
            this.targetPosition = new int[]{targetX, targetY};
        }

        /* - Debugging helper for live view evaluation. Place "//" in front to activate.
        System.out.println("iNPUT" + startpoint + " " + target);
        System.out.println("Map - " + mapWidthLocal + " " + mapHeightLocal);
        System.out.println("\n \n \n" + NextMap.MapToStringBuilder(map) + "\n \n \n");
        System.out.println("Output Start - " + this.localStartPoint[0] + " - " + this.localStartPoint[1]);
        System.out.println("Output Target - " + this.targetPosition[0] + " - " + this.targetPosition[1]);
        //*/
        // Guardcase - Checks if target is a viable tile.
        if (!this.map[targetPosition[0]][targetPosition[1]].IsWalkable()) {
            System.out.println("Target is NOT WALKABLE");
            return new ArrayList<>();
        }

        // reset the map for pathfinding process 
        resetAllTiles();

        return executeMainLogic();

    }

    /*
     * ##################### endregion constructor
     */
 /*
     * ########## region private methods
     */
    /**
     * Main Part of AStar calculation
     *
     * return List<Action> Collection of actions to describe the path
     */
    private List<Action> executeMainLogic() {

        // Queue of tiles to be visited. Sorted by score.
        PriorityQueue<NextMapTile> queue = new PriorityQueue<>((NextMapTile o1, NextMapTile o2) -> o1.GetScore() - o2.GetScore());

        // Initialise queue with start point
        queue.add(this.map[this.localStartPoint[0]][this.localStartPoint[1]]);

        boolean routeAvailable = false;

        //System.out.println("Map Size" + mapWidthLocal + " " + mapHeightLocal + "\n" + "X " + queue.peek().GetPositionX() + " Y " + queue.peek().GetPositionY() + " TX " + targetPosition[0] + " TY " + targetPosition[1]);
        // LogicGate - Check if Jump Point Search acceleraion is used.
        if (aStarJps) {
            // aStar JPS branch

            while (!queue.isEmpty()) {

                // retrieve an open tile from queue
                do {
                    if (queue.isEmpty()) {
                        break;
                    }
                    currentTile = queue.remove();
                    System.out.println("JPS Current Tile: " + currentTile.GetPositionX() + " - " + currentTile.GetPositionY());

                } while (!currentTile.IsOpen());

                // Set the tile to checked
                currentTile.SetOpen(false);

                //GuardCase - target was found
                if (currentTile.GetPositionX() == targetPosition[0] && currentTile.GetPositionY() == targetPosition[1]) {
                    // at the end, return path
                    routeAvailable = true;

                // System.out.println("Path Found");
                    break;
                }

                // Add relevant tiles to queue 
                queue.addAll(identifySuccessors(currentTile));

            }

        } else {
            // Classic aStar branch
            while (!queue.isEmpty()) {

                // retrieve an open tile from queue
                do {
                    if (queue.isEmpty()) {
                        break;
                    }
                    currentTile = queue.remove();
                } while (!currentTile.IsOpen());

                // Set the tile to checked
                currentTile.SetOpen(false);

                int currentX = currentTile.GetPositionX();
                int currentY = currentTile.GetPositionY();
                int currentScore = currentTile.GetScore();

                // GuardCase - target was found
                if (currentTile.GetPositionX() == targetPosition[0] && currentTile.GetPositionY() == targetPosition[1]) {
                    // at the end, return path
                    routeAvailable = true;

//                    System.out.println("Path Found");
                    break;
                }

                // loop through neighbours and get scores. Add these onto open tiles queue
                int smallestScore = 9999999;
                //Check left and right tile
                for (int x = -1; x <= 1; x += 2) {
                    int nextX = (currentX + x) % mapWidth;
                    // currentY is now nextY
                    if (validTile(nextX, currentY)) { // check if open and walkable
                        //calculate score
                        int score = getScoreOfTile(map[nextX][currentY], currentScore);
                        //check if smallest Score (shortest path)
                        if (score < smallestScore) {
                            smallestScore = score;
                        }
                        // place to queue, Set parent and score
                        NextMapTile thisTile = map[nextX][currentY];
                        thisTile.SetScore(score);
                        queue.add(thisTile);
                        thisTile.SetParent(currentTile);
                    }
                }

                //Check top and bottom tile
                for (int y = -1; y <= 1; y += 2) {
                    // currentX is now nextX
                    int nextY = (currentY + y) % mapHeight;
                    if (validTile(currentX, nextY)) {  // check if open and walkable
                        //calculate score
                        int score = getScoreOfTile(map[currentX][nextY], currentScore);
                        //check if smallest Score (shortest path)
                        if (score < smallestScore) {
                            smallestScore = score;
                        }
                        // place to queue, Set parent and score
                        NextMapTile thisTile = map[currentX][nextY];
                        thisTile.SetScore(score);
                        queue.add(thisTile);
                        thisTile.SetParent(currentTile);
                    }
                }
            }
        }

        // GuardCase - route was found
        if (routeAvailable) {

            // ---- Retrieve Path
            List<NextMapTile> path = getPath(currentTile);

            // ---- Convert Path to Vector2D Steps + Flip List
            List<Vector2D> vectorPath = convertMultiStepsToVector2D(path);

            // Block the tiles for other Agents
            blockUsedTiles(vectorPath);

            // ---- Convert Vector2D Steps to Actions
            List<Action> actionPath = convertVectorToAction(vectorPath);

            return actionPath;

        }

        // no path found, return empty List
        System.out.println("No Path Found");
        return new ArrayList<>();

    }

    /**
     * Prepare all mapTiles for calculation Set to open, score = 0 and parent =
     * 0
     */
    private void resetAllTiles() {
        for (NextMapTile[] tile : map) {
            for (int col = 0; col < map[0].length; col++) {
                if (tile[col] != null) {
                    tile[col].SetOpen(true);
                    tile[col].SetParent(null);
                    tile[col].SetScore(0);
                }
            }
        }
    }

    /**
     * Check if a tile is open, walkable and inside the map
     *
     * @param nextX int - x position of a tile
     * @param nextY int - y position of a tile
     * @return boolean True if valid
     */
    private boolean validTile(int nextX, int nextY) {
        if (nextX >= 0 && nextX < mapWidth) {
            if (nextY >= 0 && nextY < mapHeight) {
                if (this.strictWalkable) {
                    return map[nextX][nextY].IsOpen() && map[nextX][nextY].IsWalkableStrict() && map[nextX][nextY] != null;
                } else {
                    return map[nextX][nextY].IsOpen() && map[nextX][nextY].IsWalkable() && map[nextX][nextY] != null;
                }
            }
        }
        return false;
    }

    /**
     * Calculate the score for the tile
     *
     * @param tile NextMapTile to calculace the score for
     * @param currentScore from ParentTile
     * @return int Score to arrive the tile
     */
    private int getScoreOfTile(NextMapTile tile, int currentScore) {
        // Distance to target
        int guessScoreLeft = distanceScoreAway(tile);
        // evaluation of Score for current tile
        int extraMovementCost = 0;
        // LogicGate - use IsStrictWalkable to check for Blocks and Agents
        // StepMemory is used for calculation
        if (this.strictWalkable) {
            // unknown, obstacles, Blocks and Agents
            if (!tile.IsWalkableStrict(this.currentStep + currentScore)) {
                // Optional: dig action can be implemented here. 
                // +1 for Digger +3 for default, worker etc.
                extraMovementCost += 1000;
            }
        } else {
            // unknown, obstacles
            if (!tile.IsWalkable(this.currentStep + currentScore)) {
                // Optional: dig action can be implemented here. 
                // +1 for Digger +3 for default, worker etc.
                extraMovementCost += 1000;
            }
        }
        int movementScore = currentScore + 1;
        return guessScoreLeft + movementScore + extraMovementCost;
    }

    /**
     * Calculate the Manhattan Distance from a tile to target
     *
     * @param currentTile NextMapTile tile to calculate from
     * @return int Distance
     */
    private int distanceScoreAway(NextMapTile currentTile) {
        return Math.abs(targetPosition[0] - currentTile.GetPositionX()) + Math.abs(targetPosition[1] - currentTile.GetPositionY());
    }

    /**
     * Recursive call to Retrieve the reversed Path to target
     *
     * @param currentTile NextMapTile the targetTile to calculate the path to.
     * @return List<NextMapTile> List of NextMapTiles
     */
    private List<NextMapTile> getPath(NextMapTile currentTile) {

        List<NextMapTile> path = new ArrayList<>();
        while (currentTile != null) {
            path.add(currentTile);
            //recursive call
            currentTile = currentTile.GetParent();
        }
        return path;
    }

    /**
     * Convert a List of NextMapTiles to a List of Vector2D entries
     *
     * @param path List<NextMapTile> Source list of a path to convert
     * @return List<Vector2D> Path to target represented as a List of Vector2D
     * entries
     */
    private List<Vector2D> convertToVector2D(List<NextMapTile> path) {

        List<Vector2D> processedList = new ArrayList<>();
        if (!path.isEmpty()) {
            //Process all entries and Reverse List
            for (int i = path.size() - 1; i > 0; i--) {
                NextMapTile actualStep = path.get(i);
                NextMapTile previousStep = path.get(i - 1);

                // find the delta between current and previous tile
                int xValue = previousStep.GetPositionX() - actualStep.GetPositionX();
                int yValue = previousStep.GetPositionY() - actualStep.GetPositionY();

                // Add the entry to the processedList
                processedList.add(new Vector2D(xValue, yValue));
            }
        }

        return processedList;
    }

    /**
     * Convert the multistep Vector2D entries to multiple steps with length of 1
     *
     * @param path List<Vector2D> Entries to Process
     * @return List<Vector2D> List of Vector2D entries with length of 1
     */
    private List<Vector2D> convertMultiStepsToVector2D(List<NextMapTile> path) {
        // Reference Point to check for valid tile
        Vector2D position = new Vector2D(localStartPoint[0], localStartPoint[1]);
        // New list to return
        List<Vector2D> processedList = new ArrayList<>();
        if (!path.isEmpty()) {
            //Process all entries and Reverse List
            for (int i = path.size() - 1; i > 0; i--) {
                NextMapTile actualStep = path.get(i);
                NextMapTile previousStep = path.get(i - 1);

                // find the delta between current and previous tile
                int xValue = previousStep.GetPositionX() - actualStep.GetPositionX();
                int yValue = previousStep.GetPositionY() - actualStep.GetPositionY();

                //Get Direction
                Vector2D direction = Vector2D.CalculateNormalisedDirection(actualStep.GetPosition(), previousStep.GetPosition());
                // Get number of steps
                int steps = Math.max(Math.abs(xValue), Math.abs(yValue)); // The values are equal or one is zero
                // Add direction to processedList x-Times
                for (int j = 0; j < steps; j++) {

                    if (direction.x != 0 && direction.y != 0) {
                        //Diagonal movement
                        //Check if tile is acessible
                        if (validTile(position.x, position.y + direction.y)) {
                            processedList.add(new Vector2D(0, direction.y));
                            processedList.add(new Vector2D(direction.x, 0));
                        } else {
                            processedList.add(new Vector2D(direction.x, 0));
                            processedList.add(new Vector2D(0, direction.y));
                        }

                    } else {
                        //Orthogonal movement
                        processedList.add(new Vector2D(direction.x, direction.y));
                    }
                    // Update the inbetween position
                    position.Add(direction);
                }

            }
        }

        return processedList;
    }

    /**
     * convert List of Vector2D entries to a List of Actions
     *
     * @param vectorList List<Vector2D> Path to target as a List of Vector2D
     * entries with length of 1
     * @return List<Action> Path to target as a List of Actions
     */
    private List<Action> convertVectorToAction(List<Vector2D> vectorList) {
        List<Action> processedActions = new ArrayList<>();
        for (Vector2D target : vectorList) {
            if (target.equals(NextConstants.WestPoint)) {
                processedActions.add(NextAgentUtil.GenerateWestMove());
            }
            if (target.equals(NextConstants.NorthPoint)) {
                processedActions.add(NextAgentUtil.GenerateNorthMove());
            }
            if (target.equals(NextConstants.EastPoint)) {
                processedActions.add(NextAgentUtil.GenerateEastMove());
            }
            if (target.equals(NextConstants.SouthPoint)) {
                processedActions.add(NextAgentUtil.GenerateSouthMove());
            }
        }
        return processedActions;
    }

    /**
     * StepMemory feature: Block the tiles at a given time StepMemory
     * represents, when tiles are going to be occupied
     *
     * @param vectorPath List<Vector2D> Path to block the tiles for
     */
    private void blockUsedTiles(List<Vector2D> vectorPath) {
        int offset_x = 0;
        int offset_y = 0;
        if (currentStep != -1) { // -1 is used for Distance calculation
            for (int i = 0; i < vectorPath.size(); i++) {
                // Update position
                offset_x += vectorPath.get(i).x;
                offset_y += vectorPath.get(i).y;

                // calculate position on map taking the possibility to go over the edge into account
                int xOnMap = (startpoint.x + offset_x + mapWidth) % mapWidth;
                int yOnMap = (startpoint.y + offset_y + mapHeight) % mapHeight;
                // Block the step
                originalMap[xOnMap][yOnMap].BlockAtStep(this.currentStep + i + 1);
            }
        }
    }

    // JPS Methods -------------------
    /**
     * Identify successor tiles by performing jumps.
     *
     * @param baseTile NextMapTile tile to be evaluated
     * @return ArrayList<NextMapTile> List of tiles to Add to queue
     */
    private ArrayList<NextMapTile> identifySuccessors(NextMapTile baseTile) {
        // empty sucessors List to be returned
        ArrayList<NextMapTile> successors = new ArrayList<>();

        // retrieve all relevant neighbors
        Vector2D[] neighbors = getNeighborsPrune(baseTile);
        Vector2D temporalPosition;

        //for each of these neighbors
        for (Vector2D neighbor : neighbors) {
            if (neighbor == null) {
                continue;
            }
            // perform a jump, and retrieve potential successors
            temporalPosition = jump(neighbor, baseTile.GetPosition());
            // check if returned value is relevant 
            if (temporalPosition.x != -1) {
                // Calculate the ng part of the score value
                int ng = (int) temporalPosition.Distance(startpoint) + baseTile.GetScore();
                NextMapTile temporalTile = map[temporalPosition.x][temporalPosition.y];
                if (temporalTile.IsOpen() || temporalTile.GetScore() > ng) {
                    temporalTile.SetParent(baseTile);
                    temporalTile.SetScore(ng);
                    // Add to List
                    successors.add(temporalTile);
                }
            }
        }
        return successors;
    }

    /**
     * Retrieve all relevant neighbors of a tile, based on the parent location
     * in relation to the given node.
     *
     * @param baseTile NextMapTile tile to be evaluated, which has a parent
     * @return Vector2D[] list of tile that will be jumped
     */
    private Vector2D[] getNeighborsPrune(NextMapTile baseTile) {
        // empty List of possible Neigbors
        Vector2D[] neighbors = new Vector2D[5];
        Vector2D position = baseTile.GetPosition();
        // Parent position
        NextMapTile parent = baseTile.GetParent();
        //directed pruning: can ignore most neighbors, unless forced
        if (parent != null) {
            // retrieve the normalized direction of travel
            Vector2D direction = Vector2D.CalculateNormalisedDirection(parent.GetPosition(), position);

            if (direction.x != 0 && direction.y != 0) {
                // search in diagonal direction
                if (validTile(position.x, position.y + direction.y)) {
                    neighbors[0] = new Vector2D(position.x, position.y + direction.y);
                }

                if (validTile(position.x + direction.x, position.y)) {
                    neighbors[1] = new Vector2D(position.x + direction.x, position.y);
                }

                if (validTile(position.x, position.y + direction.y)
                        || validTile(position.x + direction.x, position.y)) {
                    neighbors[2] = new Vector2D(position.x + direction.x, position.y + direction.y);
                }

                // forced neighbour perpendicular to direction
                if (!validTile(position.x - direction.x, position.y)
                        && validTile(position.x, position.y + direction.y)) {
                    neighbors[3] = new Vector2D(position.x - direction.x, position.y + direction.y);
                }

                if (!validTile(position.x, position.y - direction.y)
                        && validTile(position.x + direction.x, position.y)) {
                    neighbors[4] = new Vector2D(position.x + direction.x, position.y - direction.y);
                }

            } else {
                // search in orthogonal direction

                if (direction.x == 0) {
                    //vertical direction
                    if (validTile(position.x, position.y + direction.y)) {
                        neighbors[0] = new Vector2D(position.x, position.y + direction.y);

                        if (!validTile(position.x + 1, position.y)) {
                            neighbors[1] = new Vector2D(position.x + 1, position.y + direction.y);
                        }

                        if (!validTile(position.x - 1, position.y)) {
                            neighbors[2] = new Vector2D(position.x - 1, position.y + direction.y);
                        }

                    }

                } else {
                    //horizontal direction
                    if (validTile(position.x + direction.x, position.y)) {
                        neighbors[0] = new Vector2D(position.x + direction.x, position.y);

                        if (!validTile(position.x, position.y + 1)) {
                            neighbors[1] = new Vector2D(position.x + direction.x, position.y + 1);
                        }

                        if (!validTile(position.x, position.y - 1)) {
                            neighbors[2] = new Vector2D(position.x + direction.x, position.y - 1);
                        }

                    }

                }

            }
        } else {
            // Special Case - Starttile, no parent
            // return all neighbors
            return getAllNeighbors(position);
        }
        // return pruned neighbors
        return neighbors;
    }

    /**
     * The Jump method recursevly searches in the direction from parentTile to
     * currentTile. It returns its current position in three situations:
     *
     * 1) The current node is the end node. (endX, endY) 2) The current node is
     * a forced neighbor. 3) The current node is an intermediate step to a node
     * that satisfies either 1) or 2)
     *
     * @param currentTile Vector2D tile to evaluate
     * @param parentTile Vector2D parent of the current tile
     * @return Vector2D a tile, that satisfies the described conditions or
     * Vector2D{-1,-1} to check if failed.
     */
    private Vector2D jump(Vector2D currentTile, Vector2D parentTile) {
        // default return vaules
        Vector2D jumpPointXDirection;
        Vector2D jumpPointYDirection;
        // Calculate the direction of movement.
        Vector2D direction = Vector2D.CalculateNormalisedDirection(parentTile, currentTile);

        // GuardCase - Check if tile is Walkable
        if (!validTile(currentTile.x, currentTile.y)) {
            return new Vector2D(-1, -1);
        }

        // GuardCase - Check if tile is targetTile
        if (currentTile.x == targetPosition[0] && currentTile.y == targetPosition[1]) {
            return currentTile;
        }

        if (direction.x != 0 && direction.y != 0) {
            // Diagonal movement, check for forced neighbors on diagonals
            // check the perpendicular diagonals, if we find a forced neighbor, we are on a jump point, return the current position
            if ((validTile(currentTile.x - direction.x, currentTile.y + direction.y)
                    && !validTile(currentTile.x - direction.x, currentTile.y))
                    || (validTile(currentTile.x + direction.x, currentTile.y - direction.y)
                    && !validTile(currentTile.x, currentTile.y - direction.y))) {
                return currentTile;
            }
        } else {
            // horizontal/vertical movement
            if (direction.x != 0) {
                // horizontal movement
                if ((validTile(currentTile.x + direction.x, currentTile.y + 1) // checking for forced neighbors
                        && !validTile(currentTile.x, currentTile.y + 1))
                        || (validTile(currentTile.x + direction.x, currentTile.y - 1)
                        && !validTile(currentTile.x + direction.x, currentTile.y - 1))) {
                    return currentTile;
                }
            } else {
                // vertical movement
                if ((validTile(currentTile.x + 1, currentTile.y + direction.y) // checking for forced neighbors
                        && !validTile(currentTile.x + 1, currentTile.y))
                        || (validTile(currentTile.x - 1, currentTile.y - direction.y)
                        && !validTile(currentTile.x - 1, currentTile.y))) {
                    return currentTile;
                }
            }

        }

        // Additional Checking for horizontal/vertical jump points in case of diagonal movement
        if (direction.x != 0 && direction.y != 0) {
            jumpPointXDirection = jump(new Vector2D(currentTile.x + direction.x, currentTile.y), currentTile);
            jumpPointYDirection = jump(new Vector2D(currentTile.x, currentTile.y + direction.y), currentTile);
            if (jumpPointXDirection.x != -1 || jumpPointYDirection.x != -1) {
                return currentTile;
            }

        }

        //moving diagonally, one of the vertical/horizontal neighbors must be open
        if (validTile(currentTile.x + direction.x, currentTile.y) || validTile(currentTile.x, currentTile.y + direction.y)) {
            return jump(new Vector2D(currentTile.x + direction.x, currentTile.y + direction.y), currentTile);
        } else {
            //moving diagonally but blocked by two touching corners of obstacles
            return new Vector2D(-1, -1);
        }
    }

    /**
     * Retrieve all neighbors from a tile
     *
     * @param basePoint Vector2D tile to be evaluated
     * @return Vector2D[] array of neighbor tiles
     */
    private Vector2D[] getAllNeighbors(Vector2D basePoint) {
        //Array to be returned
        Vector2D[] neighbors = new Vector2D[8];

        // variables to check if diagonal tile is accessible
        boolean diagonal0 = false;
        boolean diagonal1 = false;
        boolean diagonal2 = false;
        boolean diagonal3 = false;

        //Check North
        if (validTile(basePoint.x, basePoint.y - 1)) {
            neighbors[0] = new Vector2D(basePoint.x, basePoint.y - 1);
            diagonal0 = true;
            diagonal1 = true;
        }

        //Check East
        if (validTile(basePoint.x + 1, basePoint.y)) {
            neighbors[1] = new Vector2D(basePoint.x + 1, basePoint.y);
            diagonal1 = true;
            diagonal2 = true;
        }

        //Check South
        if (validTile(basePoint.x, basePoint.y + 1)) {
            neighbors[2] = new Vector2D(basePoint.x, basePoint.y + 1);
            diagonal2 = true;
            diagonal3 = true;
        }

        //Check West
        if (validTile(basePoint.x - 1, basePoint.y)) {
            neighbors[3] = new Vector2D(basePoint.x - 1, basePoint.y);
            diagonal3 = true;
            diagonal0 = true;
        }

        //Check NorthWest
        if (diagonal0 && validTile(basePoint.x - 1, basePoint.y - 1)) {
            neighbors[4] = new Vector2D(basePoint.x - 1, basePoint.y - 1);
        }

        //Check NorthEast
        if (diagonal1 && validTile(basePoint.x + 1, basePoint.y - 1)) {
            neighbors[5] = new Vector2D(basePoint.x + 1, basePoint.y - 1);
        }

        //Check SouthEast
        if (diagonal2 && validTile(basePoint.x + 1, basePoint.y + 1)) {
            neighbors[6] = new Vector2D(basePoint.x + 1, basePoint.y + 1);
        }

        //Check SouthWest
        if (diagonal3 && validTile(basePoint.x - 1, basePoint.y + 1)) {
            neighbors[7] = new Vector2D(basePoint.x - 1, basePoint.y + 1);
        }

        return neighbors;

    }

    /**
     * Centers the map around the startpoint
     *
     * @param mapOld NextMapTile[][] original map array
     * @param position Vector2D position to center around
     * @return NextMapTile[][] centered map array
     */
    private NextMapTile[][] centerMapAroundPosition(NextMapTile[][] mapOld, Vector2D position) {
        // GuardCase - Check if MapSize is sufficient
        if (mapOld.length == 1 && mapOld[0].length == 1) {
            return mapOld;
        }

        // retrieve map dimensions
        int mapWidthLocal = mapOld.length;
        int mapHeightLocal = mapOld[0].length;

        // Calculate map offset
        int xOffset = (int) position.x - ((int) (mapWidthLocal / 2));
        int yOffset = (int) position.y - ((int) (mapHeightLocal / 2));

        // new Map to return
        NextMapTile[][] tempMap = new NextMapTile[mapWidthLocal][mapHeightLocal];

        //Transfer all mapTiles to the new map
        for (int y = 0; y < mapHeightLocal; y++) {
            for (int x = 0; x < mapWidthLocal; x++) {
                int newX = (x - xOffset) % mapWidthLocal;
                int newY = (y - yOffset) % mapHeightLocal;
                tempMap[newX][newY] = new NextMapTile(
                        newX,
                        newY,
                        mapOld[x][y].GetLastVisionStep(),
                        mapOld[x][y].GetThingType(),
                        mapOld[x][y].GetStepMemory());
            }
        }

        return tempMap;
    }

    /*
     * ##################### endregion private methods
     */
}
