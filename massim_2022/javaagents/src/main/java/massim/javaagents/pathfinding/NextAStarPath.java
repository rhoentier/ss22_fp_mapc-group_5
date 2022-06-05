package massim.javaagents.pathfinding;

import eis.iilang.Action;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.general.NextConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import massim.javaagents.map.Vector2D;

/**
 * Basic A* pathfinding algorithm Based on https://github.com/Qualia91/AStarAlg
 *
 * @author AVL
 */
public class NextAStarPath {

    private NextMapTile[][] map;
    private int mapWidth;
    private int mapHeight;

    private NextMapTile currentTile;
    private int[] targetPosition;

    public List<NextMapTile> calculatePath(NextMapTile[][] map, Vector2D startpoint, Vector2D target) {

//          public List<Action> calculatePath(NextMapTile[][] map, Vector2D startpoint, Vector2D target) {

        this.mapWidth = map.length;
        this.mapHeight = map[0].length;
        
        this.map = NextMap.CenterMapAroundPosition(map, startpoint);
        
        int targetX = (int)target.x  + (int)startpoint.x - ((int)(mapWidth / 2));
        int targetY = (int)target.y  + (int)startpoint.y - ((int)(mapHeight / 2));
        
        this.targetPosition = new int[]{(int) targetX % (mapWidth-1), (int) targetX % (mapHeight-1)};

        //fillAllTiles(); // - Not needed anymore. handled in Map
        resetAllTiles();

        PriorityQueue<NextMapTile> queue = new PriorityQueue<>(new Comparator<NextMapTile>() {
            @Override
            public int compare(NextMapTile o1, NextMapTile o2) {
                return o1.getScore() - o2.getScore();
            }
        }
        );

        queue.add(map[(int) (mapWidth / 2)][(int) (mapHeight / 2)]);

        boolean routeAvailable = false;

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
                break;
            }
            // loop through neighbours and get scores. add these onto temp open list
            int smallestScore = 9999999;
            for (int x = -1; x <= 1; x += 2) {
                int nextX = currentX + x;
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
                }
            }

            for (int y = -1; y <= 1; y += 2) {
                // currentX is now nextX
                int nextY = currentY + y;
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

        if (routeAvailable) {
            return getPath(currentTile);
        }
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
                return map[nextX][nextY].isOpen() && map[nextX][nextY].IsWalkable() && map[nextX][nextY] != null;
            }
        }
        return false;
    }

    private int getScoreOfTile(NextMapTile tile, int currentScore) {
        int guessScoreLeft = distanceScoreAway(tile);
        int extraMovementCost = 0;
        if (!tile.IsWalkable()) {

            // We can implement Dig Action here. +1 for Digger +3 for default, worker etc.
            extraMovementCost += 1000;
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
}
