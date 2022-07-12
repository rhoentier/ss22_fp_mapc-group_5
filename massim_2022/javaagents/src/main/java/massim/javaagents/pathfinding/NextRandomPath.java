package massim.javaagents.pathfinding;

import java.util.Random;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.general.NextConstants.*;

/**
 * Wegfindung nach dem Random Konzept
 * @author miwo
 *
 */
public class NextRandomPath implements INextPath {

	/**
	 * generates the next random Move
	 */
	@Override
	public Action GenerateNextMove() {
		
		Random rn = new Random();
	    String[] directions = new String[]
	    {
	    	ECardinals.n.toString(), 
	    	ECardinals.s.toString(),
	    	ECardinals.w.toString(),
	    	ECardinals.e.toString()
	    };
	    return new Action("move", new Identifier(directions[rn.nextInt(4)]));
	}
}
