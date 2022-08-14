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
 * Extended A* pathfinding algorithm using concepts from
 *
 * https://github.com/Qualia91/AStarAlg
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

    private Boolean centerTheMap;
    private Boolean strictWalkable;
    private Boolean aStarJps;

    /*
     * ##################### endregion fields
     */
 /*
     * ########## region constructor.
     */
    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, int currentStep) {
        return calculatePath(false, originalMap, startpoint, target, false, false, currentStep);
    }

    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, int currentStep) {
        return calculatePath(false, originalMap, startpoint, target, centerTheMap, false, currentStep);
    }

    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, Boolean strictWalkable, int currentStep) {
        return calculatePath(false, originalMap, startpoint, target, centerTheMap, false, currentStep);
    }

    public List<Action> calculatePath(Boolean aStarJps, NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, int currentStep) {
        return calculatePath(aStarJps, originalMap, startpoint, target, false, false, currentStep);
    }

    public List<Action> calculatePath(Boolean aStarJps, NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, int currentStep) {
        return calculatePath(aStarJps, originalMap, startpoint, target, centerTheMap, false, currentStep);
    }

    public List<Action> calculatePath(Boolean aStarJps, NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, Boolean strictWalkable, int currentStep) {

        this.aStarJps = aStarJps;
        this.originalMap = originalMap;
        this.mapWidth = originalMap.length;
        this.mapHeight = originalMap[0].length;
        this.centerTheMap = centerTheMap;
        this.strictWalkable = strictWalkable;
        this.currentStep = currentStep;
        this.startpoint = startpoint;

        if (this.mapWidth == 1 && mapHeight == 1) {
            System.out.println("Map is too small");
            return new ArrayList<>();
        }

        if (centerTheMap) {
            //- Centering the map 
            this.map = NextMap.CenterMapAroundPosition(originalMap, startpoint);
            int targetX = ((target.x - startpoint.x + ((mapWidth / 2))) % mapWidth); //- 1;
            int targetY = ((target.y - startpoint.y + ((mapHeight / 2))) % mapHeight); //- 1;
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

        ///* - Debugging helper
        System.out.println("iNPUT" + startpoint + " " + target);
        System.out.println("Map - " + mapWidth + " " + mapHeight);
        System.out.println("\n \n \n" + NextMap.MapToStringBuilder(map) + "\n \n \n");
        System.out.println("Output Start - " + this.localStartPoint[0] + " - " +this.localStartPoint[1] );
        System.out.println("Output Target - " + this.targetPosition[0] + " - " +this.targetPosition[1] );
        //*/
        if (!this.map[targetPosition[0]][targetPosition[1]].IsWalkable()) {
            System.out.println("Target is NOT WALKABLE");
            return new ArrayList<>();
        }

        //fillAllTiles(); // - Not needed anymore. handled in NextMap
        resetAllTiles();

        return executeMainLogic();

    }

    /*
     * ##################### endregion constructor
     */

    /*
     * ########## region public methods
     */
    
     /*
     * ##################### endregion public methods
     */
    
    // ------------------------------------------------------------------------
    
    /*
     * ########## region private methods
     */
    private List<Action> executeMainLogic() {

        PriorityQueue<NextMapTile> queue = new PriorityQueue<>(new Comparator<NextMapTile>() {
            @Override
            public int compare(NextMapTile o1, NextMapTile o2) {
                return o1.getScore() - o2.getScore();
            }
        });

        queue.add(this.map[this.localStartPoint[0]][this.localStartPoint[1]]);

        boolean routeAvailable = false;

        //System.out.println("Map Size" + mapWidth + " " + mapHeight + "\n" + "X " + queue.peek().getPositionX() + " Y " + queue.peek().getPositionY() + " TX " + targetPosition[0] + " TY " + targetPosition[1]);
        if (aStarJps) {

            // aStar JPS 
            while (!queue.isEmpty()) {

                do {
                    if (queue.isEmpty()) {
                        break;
                    }
                    currentTile = queue.remove();
                    System.out.println("JPS Current Tile: " + currentTile.getPositionX() + " - " + currentTile.getPositionY());

                } while (!currentTile.isOpen());

                currentTile.setOpen(false);

                if (currentTile.getPositionX() == targetPosition[0] && currentTile.getPositionY() == targetPosition[1]) {
                    // at the end, return path
                    routeAvailable = true;

                    System.out.println("Path Found");
                    break;
                }
                
                queue.addAll(identifySuccessors(currentTile));
                System.out.println("Queue: " + queue);

            }

        } else {

            // Classic aStar 
            while (!queue.isEmpty()) {

                do {
                    if (queue.isEmpty()) {
                        break;
                    }
                    currentTile = queue.remove();
                } while (!currentTile.isOpen());

                currentTile.setOpen(false);

                int currentX = currentTile.getPositionX();
                int currentY = currentTile.getPositionY();
                int currentScore = currentTile.getScore();

                if (currentTile.getPositionX() == targetPosition[0] && currentTile.getPositionY() == targetPosition[1]) {
                    // at the end, return path
                    routeAvailable = true;

                    System.out.println("Path Found");
                    break;
                }

                // loop through neighbours and get scores. add these onto temp open list
                int smallestScore = 9999999;
                for (int x = -1; x <= 1; x += 2) {
                    int nextX = (currentX + x) % mapWidth;
                    // currentY is now nextY
                    if (validTile(nextX, currentY)) {
                        int score = getScoreOfTile(map[nextX][currentY], currentScore);
                        if (score < smallestScore) {
                            smallestScore = score;
                        }
                        NextMapTile thisTile = map[nextX][currentY];
                        thisTile.setScore(score);
                        queue.add(thisTile);
                        thisTile.setParent(currentTile);
                        //System.out.println("Trggered-2");

                    }
                }

                for (int y = -1; y <= 1; y += 2) {
                    // currentX is now nextX
                    int nextY = (currentY + y) % mapHeight;
                    if (validTile(currentX, nextY)) {
                        int score = getScoreOfTile(map[currentX][nextY], currentScore);
                        if (score < smallestScore) {
                            smallestScore = score;
                        }
                        NextMapTile thisTile = map[currentX][nextY];
                        thisTile.setScore(score);
                        queue.add(thisTile);
                        thisTile.setParent(currentTile);
                    }
                }
            }
        }

        if (routeAvailable) {

            //System.out.println("Convert to TargetData");
            //System.out.println("Start:" + localStartPoint[0] +" " + localStartPoint[1]);
            // ---- Retrieve Path
            List<NextMapTile> path = getPath(currentTile);
            //System.out.println("pfadlänge - " + path.size());
            //System.out.println("pfadlänge - " + path);

            // ---- Convert Path to Vector2D Steps + Flip List
            List<Vector2D> vectorPath = convertMultiStepsToVector2D(path);
            //System.out.println("pfadlänge2 - " + vectorPath.size());
            //System.out.println("pfadlänge2 - " + vectorPath);

            // Block the tiles for other Agents
            blockUsedTiles(vectorPath);

            // ---- Convert Vector2D Steps to Actions
            List<Action> actionPath = convertVectorToAction(vectorPath);
            //System.out.println("pfadlänge3 - " + actionPath.size());

            return actionPath;

        }

        System.out.println("No Path Found");
        return new ArrayList<>();

    }


   
    private void resetAllTiles() {
        for (NextMapTile[] tile : map) {
            for (int col = 0; col < map[0].length; col++) {
                if (tile[col] != null) {
                    tile[col].setOpen(true);
                    tile[col].setParent(null);
                    tile[col].setScore(0);
                }
            }
        }
    }

    private boolean validTile(int nextX, int nextY) {
        if (nextX >= 0 && nextX < mapWidth) {
            if (nextY >= 0 && nextY < mapHeight) {
                if (this.strictWalkable) {
                    return map[nextX][nextY].isOpen() && map[nextX][nextY].IsWalkableStrict() && map[nextX][nextY] != null;
                } else {
                    return map[nextX][nextY].isOpen() && map[nextX][nextY].IsWalkable() && map[nextX][nextY] != null;
                }
            }
        }
        return false;
    }

    private int getScoreOfTile(NextMapTile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);
        int extraMovementCost = 0;
        if (this.strictWalkable) {
            if (!tile.IsWalkableStrict(this.currentStep + currentScore)) {
                // We can implement Dig Action here. +1 for Digger +3 for default, worker etc.
                extraMovementCost += 1000;
            }
        } else {
            if (!tile.IsWalkable(this.currentStep + currentScore)) {
                // We can implement Dig Action here. +1 for Digger +3 for default, worker etc.
                extraMovementCost += 1000;
            }
        }
        int movementScore = currentScore + 1;
        return guessScoreLeft + movementScore + extraMovementCost;
    }

    private int distanceScoreAway(NextMapTile currentTile) {
        return Math.abs(targetPosition[0] - currentTile.getPositionX()) + Math.abs(targetPosition[1] - currentTile.getPositionY());
    }

    private List<NextMapTile> getPath(NextMapTile currentTile) {

        List<NextMapTile> path = new ArrayList<>();
        while (currentTile != null) {
            path.add(currentTile);
            currentTile = currentTile.getParent();
        }
        return path;
    }

    private List<Vector2D> convertToVector2D(List<NextMapTile> path) {
        System.out.println("convertToVector2D triggered ");

        List<Vector2D> processedList = new ArrayList<>();
        if (path.size() > 0) {
            for (int i = path.size() - 1; i > 0; i--) {
                NextMapTile actualStep = path.get(i);
                NextMapTile previousStep = path.get(i - 1);

                int xValue = previousStep.getPositionX() - actualStep.getPositionX();
                int yValue = previousStep.getPositionY() - actualStep.getPositionY();

                processedList.add(new Vector2D(xValue, yValue));
            }
        }

        return processedList;
    }

    private List<Vector2D> convertMultiStepsToVector2D(List<NextMapTile> path) {
        System.out.println("convertMultiStepsToVector2D triggered ");

        List<Vector2D> processedList = new ArrayList<>();
        if (path.size() > 0) {
            for (int i = path.size() - 1; i > 0; i--) {
                NextMapTile actualStep = path.get(i);
                NextMapTile previousStep = path.get(i - 1);

                int xValue = previousStep.getPositionX() - actualStep.getPositionX();
                int yValue = previousStep.getPositionY() - actualStep.getPositionY();

                System.out.println("Step: X " + xValue + " Y " + yValue);

                int offset_x = 0;
                int offset_y = 0;

                //Get Direction
                Vector2D direction = Vector2D.calculateNormalisedDirection(actualStep.GetPosition(), previousStep.GetPosition());

                // Get number of steps
                int steps = Math.max(Math.abs(xValue), Math.abs(yValue)); // The values are equal or one is zero

                // Add direction x-Times
                for (int j = 0; j < steps; j++) {

                    offset_x += direction.x;
                    offset_y += direction.y;
                    if (direction.x != 0 && direction.y != 0) {
                        //Diagonal movement

                        int xSubValue = previousStep.GetPosition().x + offset_x;
                        int ySubValue = previousStep.GetPosition().y + offset_y;

                        if (validTile(xSubValue, ySubValue + direction.y)) {
                            processedList.add(new Vector2D(0, direction.y));
                            processedList.add(new Vector2D(direction.x, 0));
                        } else {
                            processedList.add(new Vector2D(direction.x, 0));
                            processedList.add(new Vector2D(0, direction.y));
                        }

                    } else {
                        //orthogonal movement
                        processedList.add(new Vector2D(direction.x, direction.y));
                    }

                }

            }
        }

        for (Vector2D element : processedList) {
            System.out.println("orthogonal processedList: " + element);
        }
        return processedList;
    }

    private List<Action> convertVectorToAction(List<Vector2D> vectorList) {
        List<Action> processedActions = new ArrayList<>();
        for (Vector2D target : vectorList) {
            if (target.equals(NextConstants.WestPoint)) {
                //if ( target.x ==  NextConstants.WestPoint.x && (int) target.y == NextConstants.WestPoint.y) {
                processedActions.add(NextAgentUtil.GenerateWestMove());
            }
            if (target.equals(NextConstants.NorthPoint)) {
                //if ((int) target.x == NextConstants.NorthPoint.x && (int) target.y == NextConstants.NorthPoint.y) {
                processedActions.add(NextAgentUtil.GenerateNorthMove());
            }
            if (target.equals(NextConstants.EastPoint)) {
                //if ((int) target.x == NextConstants.EastPoint.x && (int) target.y == NextConstants.EastPoint.y) {
                processedActions.add(NextAgentUtil.GenerateEastMove());
            }
            if (target.equals(NextConstants.SouthPoint)) {
                //if ((int) target.x == NextConstants.SouthPoint.x && (int) target.y == NextConstants.SouthPoint.y) {
                processedActions.add(NextAgentUtil.GenerateSouthMove());
            }
        }
        return processedActions;
    }

    private void blockUsedTiles(List<Vector2D> vectorPath) {

        //currentStep
        //this.startpoint
        int offset_x = 0;
        int offset_y = 0;
        if (currentStep != -1) {
            for (int i = 0; i < vectorPath.size(); i++) {
                offset_x += vectorPath.get(i).x;
                offset_y += vectorPath.get(i).y;

                originalMap[startpoint.x + offset_x][startpoint.y + offset_y].BlockAtStep(this.currentStep + i + 1);
                System.out.println("Blockcheck " + (this.currentStep + i + 1) + " Is " + originalMap[startpoint.x + offset_x][startpoint.y + offset_y].CheckAtStep(this.currentStep + i + 1));
            }
        }
    }

    // JPS Methods -------------------
    private ArrayList<NextMapTile> identifySuccessors(NextMapTile baseTile) {
        //System.out.println("identifySuccessors triggered");
        ArrayList<NextMapTile> successors = new ArrayList<>();  // empty sucessors List to be returned
        Vector2D[] neighbors = getNeighborsPrune(baseTile);  // retrieve all neighbors

        Vector2D temporalPosition = new Vector2D();
        for (int i = 0; i < neighbors.length; i++) { //for each of these neighbors
            if (neighbors[i] == null) {
                continue;
            }

            temporalPosition = jump(neighbors[i], baseTile.GetPosition());
            //System.out.println("Temporal Position" + temporalPosition);
            if (temporalPosition.x != -1) {
                int ng = (int) temporalPosition.distance(startpoint) + baseTile.getScore();
                NextMapTile temporalTile = map[temporalPosition.x][temporalPosition.y];
                if (temporalTile.isOpen() || temporalTile.getScore() > ng) {
                    temporalTile.setParent(baseTile);
                    temporalTile.setScore(ng);
                    successors.add(temporalTile);
                }

            }

        }
        //System.out.println("Successors : " + successors);
        return successors;
    }

    private Vector2D[] getNeighborsPrune(NextMapTile baseTile) {
        //System.out.println("baseTile: " + baseTile.GetPosition());

        //System.out.println("getNeighborsPrune triggered");
        Vector2D[] neighbors = new Vector2D[5];
        Vector2D position = baseTile.GetPosition();
        NextMapTile parent = baseTile.getParent();
        if (parent != null) {
            // retrieve the direction of travel
            Vector2D direction = Vector2D.calculateNormalisedDirection(parent.GetPosition(), position);

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

                // 90° to direction,forced neighbour
                if (!validTile(position.x - direction.x, position.y)
                        && validTile(position.x, position.y + direction.y)) {
                    neighbors[3] = new Vector2D(position.x - direction.x, position.y + direction.y);
                }

                if (!validTile(position.x, position.y - direction.y)
                        && validTile(position.x + direction.x, position.y)) {
                    neighbors[4] = new Vector2D(position.x + direction.x, position.y - direction.y);
                }

            } else {
                // search orthogonal

                if (direction.x == 0) {
                    //vertical
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
                    //horizontal
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
            // System.out.println("No Parent Triggered");
            // return all neighbors
            return getAllNeighbors(position);
        }
        return neighbors;
    }

    private Vector2D jump(Vector2D currentNode, Vector2D parentNode) {
        Vector2D jumpPointXDirection = new Vector2D(-1, -1);
        Vector2D jumpPointYDirection = new Vector2D(-1, -1);
        Vector2D direction = Vector2D.calculateNormalisedDirection(parentNode, currentNode);

        if (!validTile(currentNode.x, currentNode.y)) {
            return new Vector2D(-1, -1);
        }

        if (currentNode.x == targetPosition[0] && currentNode.y == targetPosition[1]) {
            return currentNode;
        }

        if (direction.x != 0 && direction.y != 0) { // Diagonal movement, check for forced neighbors on diagonals
            // check the 90° diagonals, if we find a forced neighbor, we are on a jump point, return the current position
            if ((validTile(currentNode.x - direction.x, currentNode.y + direction.y)
                    && !validTile(currentNode.x - direction.x, currentNode.y))
                    || (validTile(currentNode.x + direction.x, currentNode.y - direction.y)
                    && !validTile(currentNode.x, currentNode.y - direction.y))) {
                return currentNode;
            }
        } else { // horizontal/vertical movement
            if (direction.x != 0) {// horizontal movement
                if ((validTile(currentNode.x + direction.x, currentNode.y + 1) // checking for forced neighbors
                        && !validTile(currentNode.x, currentNode.y + 1))
                        || (validTile(currentNode.x + direction.x, currentNode.y - 1)
                        && !validTile(currentNode.x + direction.x, currentNode.y - 1))) {
                    return currentNode;
                }
            } else { // vertical movement
                if ((validTile(currentNode.x + 1, currentNode.y + direction.y) // checking for forced neighbors
                        && !validTile(currentNode.x + 1, currentNode.y))
                        || (validTile(currentNode.x - 1, currentNode.y - direction.y)
                        && !validTile(currentNode.x - 1, currentNode.y))) {
                    return currentNode;
                }
            }

        }

        if (direction.x != 0 && direction.y != 0) { // Additional Checking for horizontal/vertical jump points in case of diagonal movement
            jumpPointXDirection = jump(new Vector2D(currentNode.x + direction.x, currentNode.y), currentNode);
            jumpPointYDirection = jump(new Vector2D(currentNode.x, currentNode.y + direction.y), currentNode);
            if (jumpPointXDirection.x != -1 || jumpPointYDirection.x != -1) {
                return currentNode;
            }

        }

        if (validTile(currentNode.x + direction.x, currentNode.y) || validTile(currentNode.x, currentNode.y + direction.y)) {  //moving diagonally, one of the vertical/horizontal neighbors must be open
            return jump( new Vector2D(currentNode.x+direction.x,currentNode.y + direction.y), currentNode);
        } else { //moving diagonally but blocked by two touching corners of obstacles
            return new Vector2D(-1, -1);
        }
    }

    private Vector2D[] getAllNeighbors(Vector2D basePoint) {
        //System.out.println("getAllNeighbors triggered");
        Vector2D[] neighbors = new Vector2D[8];
        boolean diagonal0 = false; // Check if diagonal tile is accessible
        boolean diagonal1 = false;
        boolean diagonal2 = false;
        boolean diagonal3 = false;

        if (validTile(basePoint.x, basePoint.y - 1)) {
            neighbors[0] = new Vector2D(basePoint.x, basePoint.y - 1);
            diagonal0 = true;
            diagonal1 = true;
        }

        if (validTile(basePoint.x + 1, basePoint.y)) {
            neighbors[1] = new Vector2D(basePoint.x + 1, basePoint.y);
            diagonal1 = true;
            diagonal2 = true;
        }

        if (validTile(basePoint.x, basePoint.y + 1)) {
            neighbors[2] = new Vector2D(basePoint.x, basePoint.y + 1);
            diagonal2 = true;
            diagonal3 = true;
        }

        if (validTile(basePoint.x - 1, basePoint.y)) {
            neighbors[3] = new Vector2D(basePoint.x - 1, basePoint.y);
            diagonal3 = true;
            diagonal0 = true;
        }

        if (diagonal0 && validTile(basePoint.x - 1, basePoint.y - 1)) {
            neighbors[4] = new Vector2D(basePoint.x - 1, basePoint.y - 1);
        }

        if (diagonal1 && validTile(basePoint.x + 1, basePoint.y - 1)) {
            neighbors[5] = new Vector2D(basePoint.x + 1, basePoint.y - 1);
        }

        if (diagonal2 && validTile(basePoint.x + 1, basePoint.y + 1)) {
            neighbors[6] = new Vector2D(basePoint.x + 1, basePoint.y + 1);
        }

        if (diagonal3 && validTile(basePoint.x - 1, basePoint.y + 1)) {
            neighbors[7] = new Vector2D(basePoint.x - 1, basePoint.y + 1);
        }

        return neighbors;

    }
    /*
     * ##################### endregion private methods
     */
}
