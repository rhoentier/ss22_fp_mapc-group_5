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
        if(position.equals(Constants.WestPoint) && !status.getAttachedElements().contains(Constants.WestPoint)){
            return true;
        }
        if(position.equals(Constants.NorthPoint) && !status.getAttachedElements().contains(Constants.NorthPoint)){
            return true;
        }
        if(position.equals(Constants.EastPoint) && !status.getAttachedElements().contains(Constants.EastPoint)){
            return true;
        }
        if(position.equals(Constants.SouthPoint) && !status.getAttachedElements().contains(Constants.SouthPoint)){
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
}