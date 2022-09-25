package massim.javaagents.pathfinding;

import java.util.ArrayList;

import eis.iilang.Action;
import java.util.List;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.Vector2D;

public class NextPathfindingUtil {

    public static ArrayList<Action> GenerateExploreActions() {
        ArrayList<Action> steps;

        // detect randomPoint to go
        NextManhattanPath manhattanPath = new NextManhattanPath();
        steps = manhattanPath.CalculatePath(NextAgentUtil.GenerateRandomNumber(21) - 10, NextAgentUtil.GenerateRandomNumber(21) - 10);

        return steps;
    }

    /**
     * Calculate Distance using Manhattan or A*JPS if available
     *
     * @param startPosition Vector2D start position of the pathfindung
     * @param targetPosition Vector2D target position of the pathfinding
     * @return int calculated distance
     */
    public static int CalculateDistance(NextMap groupMap, Vector2D startPosition, Vector2D targetPosition) {

        List<Action> steps = new ArrayList<>();

        if (groupMap.GetMapTile(startPosition).IsWalkable() && groupMap.GetMapTile(targetPosition).IsWalkable()) {
            NextAStarPath aStarPathfinder = new NextAStarPath();
            steps = aStarPathfinder.CalculatePath(Boolean.TRUE, groupMap.GetMapArray(), startPosition, targetPosition, -1);
        }

        if (steps.isEmpty()) {
            steps = NextManhattanPath.CalculatePath(startPosition, targetPosition);
        }

        return steps.size();
    }
}
