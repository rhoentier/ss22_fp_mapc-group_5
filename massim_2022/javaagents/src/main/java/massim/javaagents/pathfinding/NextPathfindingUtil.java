package massim.javaagents.pathfinding;

import java.util.ArrayList;

import eis.iilang.Action;
import eis.iilang.Identifier;
import java.util.List;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.Vector2D;


public class NextPathfindingUtil {
	
	public static ArrayList<Action> GenerateExploreActions()
	{
		ArrayList<Action> steps = new ArrayList<>();
		
		// detect randomPoint to go
		NextManhattanPath manhattanPath = new NextManhattanPath();
		steps = manhattanPath.calculatePath(NextAgentUtil.GenerateRandomNumber(21)-10,NextAgentUtil.GenerateRandomNumber(21)-10);	
		
		// Zum testen
//		steps.add(new Action("move", new Identifier("w")));
//		steps.add(new Action("move", new Identifier("w")));
//		steps.add(new Action("move", new Identifier("w")));
//		steps.add(new Action("move", new Identifier("w")));
//		steps.add(new Action("move", new Identifier("w")));
//		steps.add(new Action("move", new Identifier("w")));
		System.out.println("--- Neuer Weg entdeckt");
		return steps;		
	}
        
    /**
     * Calculate Distance using Manhattan or A*JPS if available 
     * 
     * @param startPosition Vector2D start position of the pathfindung
     * @param targetPosition Vector2D target position of the pathfinding
     * @return int calculated distance
     */
    public static int calculateDistance(NextMap groupMap, Vector2D startPosition, Vector2D targetPosition) {

        int distance = 0;
        List<Action> steps = new ArrayList<>();
        
        if(groupMap.GetMapTile(startPosition).IsWalkable() && groupMap.GetMapTile(targetPosition).IsWalkable()) {
            NextAStarPath aStarPathfinder = new NextAStarPath();
            steps = aStarPathfinder.calculatePath(Boolean.TRUE, groupMap.GetMapArray(), startPosition, targetPosition, -1);
        } else {
            steps = NextManhattanPath.CalculatePath(startPosition, targetPosition);
        }
        distance = steps.size();

        return distance;
    }
}
