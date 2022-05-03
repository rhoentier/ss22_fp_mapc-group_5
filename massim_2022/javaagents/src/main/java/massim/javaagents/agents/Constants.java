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
    
    static enum ECardinals {
    	n,
    	e, 
    	s,
    	w
    }
}