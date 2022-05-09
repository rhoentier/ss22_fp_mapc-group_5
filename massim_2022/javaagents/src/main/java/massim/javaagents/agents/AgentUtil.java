package massim.javaagents.agents;

import java.awt.Point;
import java.util.Random;

import eis.iilang.Action;
import eis.iilang.Identifier;

public final class AgentUtil{

	static Action generateRandomMove() 
	{
	    Random rn = new Random();
	    String[] directions = new String[]{"n", "s", "w", "e"};
	    return new Action("move", new Identifier(directions[rn.nextInt(4)]));
	}

    /**
     * Reports, if a Thing is next to the Agent
     *
     * @param xValue - x-Value of Thing
     * @param yValue - y-Value of Thing
     * @return boolean
     */
    static boolean NextTo(Point position, AgentStatus status) {
        if(position.equals(Constants.WestPoint) && !status.GetAttachedElements().contains(Constants.WestPoint)){
            return true;
        }
        if(position.equals(Constants.NorthPoint) && !status.GetAttachedElements().contains(Constants.NorthPoint)){
            return true;
        }
        if(position.equals(Constants.EastPoint) && !status.GetAttachedElements().contains(Constants.EastPoint)){
            return true;
        }
        if(position.equals(Constants.SouthPoint) && !status.GetAttachedElements().contains(Constants.SouthPoint)){
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
        if (direction.equals(Constants.WestPoint)) {
            return new Identifier(Constants.ECardinals.w.toString());
        }

        if (direction.equals(Constants.SouthPoint)) {
            return new Identifier(Constants.ECardinals.s.toString());
        }

        if (direction.equals(Constants.EastPoint)) {
            return new Identifier(Constants.ECardinals.e.toString());
        }

        if (direction.equals(Constants.NorthPoint)) {
            return new Identifier(Constants.ECardinals.n.toString());
        }

        return null;
    }

    static boolean hasFreeSlots(AgentStatus agentStatus) {
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