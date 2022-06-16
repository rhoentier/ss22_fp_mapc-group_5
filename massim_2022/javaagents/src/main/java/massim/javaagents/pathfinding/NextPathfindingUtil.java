package massim.javaagents.pathfinding;

import java.util.ArrayList;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.agents.NextAgentUtil;

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
}
