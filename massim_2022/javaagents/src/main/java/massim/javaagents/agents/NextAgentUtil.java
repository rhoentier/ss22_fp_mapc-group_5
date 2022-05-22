package massim.javaagents.agents;

import java.awt.Point;
import java.util.Random;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.general.NextConstants;

public final class NextAgentUtil{

	public static Action generateRandomMove()
	{
	    Random rn = new Random();
	    String[] directions = new String[]{"n", "s", "w", "e"};
	    return new Action("move", new Identifier(directions[rn.nextInt(4)]));
	}

    /**
     * Reports, if a Thing is next to the Agent
     *
     * @param position - x-Value, y-Value of a Thing
     * @param status - #source of Information
     * @return boolean
     */
    public static boolean NextTo(Point position, NextAgentStatus status) {
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
    public static Identifier GetDirection(Point direction) {
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
    public static Action GenerateSurveyThingAction(String type) {
        return new Action("survey", new Identifier(type));
    }
    /**
     * Creates an action to survey a remote agent:  
     *
     * @param String X-Coordinate relative to the surveing Agent
     * @param String Y-Coordinate relative to the surveing Agent
     * @return Action
     */
    public static Action GenerateSurveyAgentAction(int xPosition, int yPosition) {
        return new Action("survey", new Identifier( "" + xPosition ),new Identifier( "" + yPosition));
    }
}