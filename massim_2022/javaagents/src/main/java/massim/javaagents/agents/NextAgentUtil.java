package massim.javaagents.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.percept.NextTask;

public final class NextAgentUtil{

	public static Action GenerateRandomMove()
	{
	    String[] directions = new String[]{"n", "s", "w", "e"};
	    return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier(directions[GenerateRandomNumber(4)]));
	}

        public static int GenerateRandomNumber(int range) {
            Random rn = new Random();
	    return rn.nextInt(range);
        }
        public static Action GenerateNorthMove()
	{
            return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("n"));
	}
        public static Action GenerateSouthMove()
	{
            return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("s"));
	}
        public static Action GenerateWestMove()
	{
            return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("w"));
	}
        public static Action GenerateEastMove()
	{
            return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("e"));
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
    
    public static ArrayList<NextTask> EvaluatePossibleTask(HashSet<NextTask> taskList, HashSet<NextMapTile> dispenserLst, int actualSteps)
    {
    	ArrayList<NextTask> result = new ArrayList<NextTask>();
    	Iterator<NextTask> it = taskList.iterator();
    	
    	while(it.hasNext())
    	{
    		NextTask nextTask = it.next();
    		if(nextTask.GetRequiredBlocks().size() == 1) { // Nur Tasks mit einem Block
	    		Iterator<NextMapTile> nextMapIt = nextTask.GetRequiredBlocks().iterator();
	    		while (nextMapIt.hasNext()) {
	    			NextMapTile nextMapTile = nextMapIt.next();
	    			Iterator<NextMapTile> nextDispenserIt = dispenserLst.iterator();
	    			while(nextDispenserIt.hasNext())
	    			{
	    				NextMapTile nextDispenserMapTile = nextDispenserIt.next();
	    				if(nextDispenserMapTile.getThingType().contains(nextMapTile.getThingType())
	    						&& actualSteps < nextTask.GetDeadline())
	    				{
	    					result.add(nextTask);	    					
	    				}
	    			}
	    		}
    		}
    	}
    	return result;
    }
    
    public static Vector2D GetDispenserFromType(HashSet<NextMapTile> dispenser, String type)
    {
    	Vector2D result = new Vector2D();
    	Iterator<NextMapTile> it = dispenser.iterator();
    	while(it.hasNext())
    	{
    		NextMapTile next = it.next();
    		if(next.getThingType().contains(type))
    		{
    			result = new Vector2D(next.getPositionX(), next.getPositionY());
    		}
    	}
    	return result;
    }
    
    public static ArrayList<Action> GetNearestGoalZone(HashSet<NextMapTile> goalzones)
    {
    	// TODO hier noch schauen, dass er in die Goalzone läuft
    	NextManhattanPath manhattanPath = new NextManhattanPath();
    	ArrayList<Action> list = new ArrayList<Action>();
    	Iterator<NextMapTile> it = goalzones.iterator();
    	
    	NextMapTile next = it.next();    		
		list = manhattanPath.calculatePath((int) next.getPositionX(), (int)next.getPositionY());
		
    	while(it.hasNext())
    	{
    		it.next();
    		ArrayList<Action> calcList = manhattanPath.calculatePath((int) next.getPositionX(), (int)next.getPositionY());
    		if(calcList.size() < list.size())
    		{
    			list = calcList;
    		}

    	}
		return list;
    }
    
    public static Boolean IsAgentInGoalZone(HashSet<NextMapTile> goalzones)
    {
    	Iterator<NextMapTile> it = goalzones.iterator();
    	
    	while(it.hasNext())
    	{
    		NextMapTile next = it.next();    
    		// Agent at (0,0)
    		if(new Vector2D().equals(next.getPosition())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static Boolean IsBlockInCorrectPosition(NextAgent nextAgent)
    {
    	// TODO miri Vergleich aller Blöcke - derzeit nur mit 1
    	if(nextAgent.GetActiveTask() != null) {
	    	HashSet<Point> attachedElements = nextAgent.getStatus().GetAttachedElements();
	    	HashSet<NextMapTile> activeTask = nextAgent.GetActiveTask().GetRequiredBlocks();
	    	    	
	    	Iterator<Point> attachElementIterator = attachedElements.iterator();
	    	Point next = attachElementIterator.next();
	    	
	    	Iterator<NextMapTile> activeTaskIterator = activeTask.iterator();
	    	NextMapTile nextActiveTask = activeTaskIterator.next();
	    	
	    	if(next.equals(nextActiveTask.getPoint()))
	    	{
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
    public static Boolean IsTaskActive(NextTask activeTask, int actualSteps)
    {
    	if(actualSteps <= activeTask.GetDeadline())
    		return true;
    	return false;
    }
    
    public static Boolean IsCorrectBlockType(NextTask nextTask, String thingType)
    {
    	Iterator<NextMapTile> blocksIt = nextTask.GetRequiredBlocks().iterator();
    	while(blocksIt.hasNext())
    	{
    		NextMapTile next = blocksIt.next();
    		if(thingType.contains(next.getThingType()))
    			return true;
    	}
    	return false;
    }
}