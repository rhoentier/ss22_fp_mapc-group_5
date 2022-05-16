package massim.javaagents.general;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public final class NextConstants 
{
    //--- General Directions ----
    public final static Point WestPoint = new Point(-1, 0);
    public final static Point EastPoint = new Point(1, 0);
    public final static Point SouthPoint = new Point(0, 1);
    public final static Point NorthPoint = new Point(0, -1);
    
    /**
     * Enum for Priority Map
     * @author miwo
     *
     */
    public static enum EPriorityMap {
    	SUBMIT,
    	ATTACH,
    	REQUEST,
    	MOVE,
    	DETACH,
    	ROTATE,
    	CONNECT,
    	SURVEY,
    	ADOPT,
    	DISCONNECT,
    	CLEAR,
    	SKIPT,
    }
    
    /**
     * Priotiy selection to be used by Agents
     */
    public final static Map<String, Integer> PriorityMap = new HashMap<String, Integer>() {
        {
            put("submit", 10);
            put("attach", 20);
            put("request", 30);
            put("dodge", 39);
            put("move", 40);
            put("detach", 50);
            put("rotate", 60);
            put("connect", 70);
            put("survey", 80);
            put("adopt", 90);
            put("disconnect", 100);
            put("clear", 110);
            put("skip", 1000);
        }
    };
    
    /**
     * Enum for Cardinals
     * @author miwo
     *
     */
    public static enum ECardinals {
    	n,
    	e, 
    	s,
    	w
    }
    
    /**
     * Enum for Percepts
     * @author miwo
     *
     */
    public static enum EPercepts {
    	simStart,
    	name,
    	team,
    	teamSize,
    	steps,
    	role,
    	simEnd,
    	ranking,
    	score,
    	bye,
    	requestAction,
    	actionID,
    	timestamp,
    	deadline,
    	step,
    	lastAction,
    	lastActionResult,
    	lastActionParams,
    	thing,
    	task,
    	attached,
    	energy,
    	deactivated,
    	roleZone,
    	goalZone,
    	violation,
    	norm,
    	hit,
    	surveyed;
    }

	/**
	 * Enum for Actions
	 * @author rhoentier
	 *
	 */
	public static enum EActions {
		skip,
		move,
		attach,
		detach,
		rotate,
		connect,
		disconnect,
		request,
		submit,
		clear,
		adopt,
		survey,

	}
}