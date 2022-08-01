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
 * Basic A* pathfinding algorithm Based on https://github.com/Qualia91/AStarAlg
 *
 * @author Alexander Lorenz
 */
public class NextAStarPath {

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

    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, int currentStep) {
        return calculatePath(originalMap, startpoint, target, false, false, currentStep);
    }
    
    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, int currentStep){
        return calculatePath(originalMap, startpoint, target, centerTheMap , false, currentStep);
    }

    public List<Action> calculatePath(NextMapTile[][] originalMap, Vector2D startpoint, Vector2D target, Boolean centerTheMap, Boolean strictWalkable, int currentStep) {

        this.originalMap = originalMap;
        this.mapWidth = originalMap.length;
        this.mapHeight = originalMap[0].length;
        this.centerTheMap = centerTheMap;
        this.strictWalkable = strictWalkable;
        this.currentStep = currentStep;
        this.startpoint = startpoint;

        if (this.mapWidth == 1 && mapHeight == 1) {
            System.out.println("Map is to small");
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
        
        /* - Debugging helper
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

        PriorityQueue<NextMapTile> queue = new PriorityQueue<>(new Comparator<NextMapTile>() {
            @Override
            public int compare(NextMapTile o1, NextMapTile o2) {
                return o1.getScore() - o2.getScore();
            }
        });

        queue.add(this.map[this.localStartPoint[0]][this.localStartPoint[1]]);

        boolean routeAvailable = false;

        //System.out.println("Map Size" + mapWidth + " " + mapHeight + "\n" + "X " + queue.peek().getPositionX() + " Y " + queue.peek().getPositionY() + " TX " + targetPosition[0] + " TY " + targetPosition[1]);
        while (!queue.isEmpty()) {

            do {
                if (queue.isEmpty()) {
                    break;
                }
                currentTile = queue.remove();
                //System.out.println("Current Tile: " + currentTile.getPositionX() + " - " + currentTile.getPositionY());
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
                    //System.out.println("Trggered-3");
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
            List<Vector2D> vectorPath = convertToVector2D(path);
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
                if(this.strictWalkable){
                    return map[nextX][nextY].isOpen() && map[nextX][nextY].IsWalkableStrict() && map[nextX][nextY] != null;
                } else{
                    return map[nextX][nextY].isOpen() && map[nextX][nextY].IsWalkable() && map[nextX][nextY] != null;
                }
            }
        }
        return false;
    }

    private int getScoreOfTile(NextMapTile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);
        int extraMovementCost = 0;
        if(this.strictWalkable){
            if (!tile.IsWalkableStrict(this.currentStep+currentScore)) {
                // We can implement Dig Action here. +1 for Digger +3 for default, worker etc.
                extraMovementCost += 1000;
            }
        }else{
            if (!tile.IsWalkable(this.currentStep+currentScore)) {
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
        
        for (int i = 0; i < vectorPath.size(); i++) {
            offset_x += vectorPath.get(i).x;
            offset_y += vectorPath.get(i).y;
            
            originalMap[startpoint.x+offset_x][startpoint.y+offset_y].BlockAtStep(this.currentStep+i+1);
            System.out.println("Blockcheck " + (this.currentStep+i+1) + " Is " + originalMap[startpoint.x+offset_x][startpoint.y+offset_y].CheckAtStep(this.currentStep+i+1)     );
        }
        
        
    }
}
