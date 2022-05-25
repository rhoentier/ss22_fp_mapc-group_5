package massim.javaagents.agents;

import java.awt.Point;
import java.util.Random;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.general.NextConstants;
import massim.javaagents.pathfinding.NextRandomPath;
import massim.javaagents.pathfinding.NextSpiralPath;

public final class AgentUtil{

	static NextRandomPath nextRandomPath = new NextRandomPath();
	static NextSpiralPath nextSimplePath = new NextSpiralPath();
	static public Action GenerateMove(NextConstants.EPathFinding pathfinding) 
	{
		Action action = null;
		switch(pathfinding) 
		{
			case simple: 
				action = nextSimplePath.GenerateNextMove();
				break;
			case random:
				action = nextRandomPath.GenerateNextMove();
			    break;
			case aStar:
				break;
			default:
				break;
		}
		return action;
	}

    /**
     * Reports, if a Thing is next to the Agent
     *
     * @param xValue - x-Value of Thing
     * @param yValue - y-Value of Thing
     * @return boolean
     */
    static boolean NextTo(Point position, NextAgentStatus status) {
        if(position.equals(NextConstants.WestPoint) && !status.GetAttachedElements().contains(NextConstants.WestPoint)){
            return true;
        }
        if(position.equals(NextConstants.NorthPoint) && !status.GetAttachedElements().contains(NextConstants.NorthPoint)){
            return true;
        }
        if(position.equals(NextConstants.EastPoint) && !status.GetAttachedElements().contains(NextConstants.EastPoint)){
            return true;
        }
        if(position.equals(NextConstants.SouthPoint) && !status.GetAttachedElements().contains(NextConstants.SouthPoint)){
            return true;
        }        
        return false;
    }
    
    /**
     * Returns the direction for an action
     *
     * @param xValue - x-Value of Thing
     * @param yValue - y-Value of Thing
     * @return Identifier for the direction value of an action.
     */
    static Identifier GetDirection(Point direction) {
        if (direction.equals(NextConstants.WestPoint)) {
            return new Identifier(NextConstants.ECardinals.w.toString());
        }

        if (direction.equals(NextConstants.SouthPoint)) {
            return new Identifier(NextConstants.ECardinals.s.toString());
        }

        if (direction.equals(NextConstants.EastPoint)) {
            return new Identifier(NextConstants.ECardinals.e.toString());
        }

        if (direction.equals(NextConstants.NorthPoint)) {
            return new Identifier(NextConstants.ECardinals.n.toString());
        }

        return null;
    }

    static boolean hasFreeSlots(NextAgentStatus agentStatus) {
        return agentStatus.GetAttachedElementsAmount() <= 2;
    }

    /**
     * Creates an action to localise the distance to the next target:  
     *
     * @param type of Target. "dispenser", "goal", "role"
     * @return Action
     */
    static Action GenerateSurveyThingAction(String type) {
        return new Action("survey", new Identifier(type));
    }
    /**
     * Creates an action to survey a remote agent:  
     *
     * @param String X-Coordinate relative to the surveing Agent
     * @param String Y-Coordinate relative to the surveing Agent
     * @return Action
     */
    static Action GenerateSurveyAgentAction(int xPosition, int yPosition) {
        return new Action("survey", new Identifier( "" + xPosition ),new Identifier( "" + yPosition));
    }
}