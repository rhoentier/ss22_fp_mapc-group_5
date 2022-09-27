package massim.javaagents.pathfinding;

import java.util.ArrayList;

import eis.iilang.Action;
import java.util.List;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.Vector2D;

/**
 * Support methods for path finding
 *
 * @author Alexander Lorenz
 */

public class NextPathfindingUtil {


    /**
     * Calculate Distance using Manhattan or A*JPS if available
     *
     * @param groupMap NextMap containing the environmental information
     * @param startPosition Vector2D pathfinding start position 
     * @param targetPosition Vector2D pathfinding target position
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
