package massim.javaagents.agents;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public final class Constants 
{
    //--- General Directions ----
    final static Point WestPoint = new Point(-1, 0);
    final static Point EastPoint = new Point(1, 0);
    final static Point SouthPoint = new Point(0, 1);
    final static Point NorthPoint = new Point(0, -1);
    
    static enum EPriorityMap {
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
    
    // Priotiy selection to be used by Agents
    final static Map<String, Integer> PriorityMap = new HashMap<String, Integer>() {
        {
            put("submit", 1);
            put("attach", 2);
            put("request", 3);
            put("move", 4);
            put("detach", 5);
            put("rotate", 6);
            put("connect", 7);
            put("survey", 8);
            put("adopt", 9);
            put("disconnect", 10);
            put("clear", 11);
            put("skip", 100);
        }
    };
    
    static enum ECardinals {
    	n,
    	e, 
    	s,
    	w
    }
}