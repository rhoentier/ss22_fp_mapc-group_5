package massim.javaagents.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import eis.iilang.Action;
import eis.iilang.Identifier;
import java.util.Collections;
import java.util.HashMap;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.ECardinals;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.percept.NextRole;
import massim.javaagents.percept.NextTask;

public final class NextAgentUtil {
	
	private static int ATTACHED_ELEMENTS = 1;

    public static Action GenerateRandomMove() {
        String[] directions = new String[]{"n", "s", "w", "e"};
        return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier(directions[GenerateRandomNumber(4)]));
    }

    public static int GenerateRandomNumber(int range) {
        Random rn = new Random();
        return rn.nextInt(range);
    }

    public static Action GenerateNorthMove() {
        return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("n"));
    }

    public static Action GenerateSouthMove() {
        return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("s"));
    }

    public static Action GenerateWestMove() {
        return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("w"));
    }

    public static Action GenerateEastMove() {
        return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier("e"));
    }
    
    public static Action GenerateMoveWithDirection(ECardinals direction) {
        return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier(direction.toString()));
    }
    
    public static Action GenerateMoveWithDirection(String direction) {
        return NextActionWrapper.CreateAction(NextConstants.EActions.move, new Identifier(direction));
    }

    /**
     * Reports, if a Thing is next to the Agent
     *
     * @param position - x-Value, y-Value of a Thing
     * @param agent - the Agent to be compared to
     * @return boolean
     */
    public static boolean NextToUsingLocalView(Vector2D position, NextAgent agent) {
        NextAgentStatus status = agent.getAgentStatus();
        if (position.equals(NextConstants.WestPoint) && !status.GetAttachedElements().contains(NextConstants.WestPoint)) {
            return true;
        }
        if (position.equals(NextConstants.NorthPoint) && !status.GetAttachedElements().contains(NextConstants.NorthPoint)) {
            return true;
        }
        if (position.equals(NextConstants.EastPoint) && !status.GetAttachedElements().contains(NextConstants.EastPoint)) {
            return true;
        }
        if (position.equals(NextConstants.SouthPoint) && !status.GetAttachedElements().contains(NextConstants.SouthPoint)) {
            return true;
        }
        return false;
    }
    
    /**
     * Reports, if a Thing is next to the Agent using Absolute coordinates
     *
     * @param position - x-Value, y-Value of a Thing
     * @param status - #source of Information
     * @return boolean
     */
    ///**
    public static boolean NextToUsingAbsoluteValues(Vector2D position, NextAgent agent) {
        Vector2D newPosition = position.clone();
        newPosition.subtract(agent.GetPosition());
        return NextToUsingLocalView(position, agent);
    }
    //*/
    
    /**
     * Change Vector Direction to Identifier for using Actions
     *
     * @param xValue - x-Value of Thing
     * @param yValue - y-Value of Thing
     * @return Identifier for the direction value of an action.
     */
    public static Identifier ChangeVector2DToIdentifier(Vector2D direction) {
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

    /**
     * Check, if Rotation from Block is possible
     * @param agent for agentstatus
     * @param direction
     * @return
     */
    public static boolean IsRotationPossible(NextAgentStatus agentStatus, String direction) {
        //HashSet<NextMapTile> visibleThings = agentStatus.GetVisibleThings();
        HashSet<NextMapTile> visibleThings = agentStatus.GetFullLocalView();

        HashSet<Vector2D> attachedElements = agentStatus.GetAttachedElements();

        Vector2D rotateTo;
        //System.out.println("Number of attached elements: " + attachedElements.size());

        for (Vector2D attachedElement : attachedElements) {

            rotateTo = attachedElement.clone();
            if (direction == "cw") {
                rotateTo.rotateCW();
            } else if (direction == "ccw") {
                rotateTo.rotateCCW();
            }

            for (NextMapTile visibleThing : visibleThings) {
                if (visibleThing.GetPosition().equals(rotateTo) && !visibleThing.IsWalkable()) {
                    return false;
                }
            }
        }

        return true;

    }

    public static boolean HasFreeSlots(NextAgentStatus agentStatus) {
        return agentStatus.GetAttachedElementsAmount() < ATTACHED_ELEMENTS;
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
        return new Action("survey", new Identifier("" + xPosition), new Identifier("" + yPosition));
    }

    /**
     * @deprecated alte Taskverarbeitung
     * @param taskList
     * @param dispenserLst
     * @param currentStep
     * @return
     */
    @Deprecated
    private static ArrayList<NextTask> EvaluatePossibleTask(HashSet<NextTask> taskList, HashSet<NextMapTile> dispenserLst, int currentStep) {
        ArrayList<NextTask> result = new ArrayList<NextTask>();
        Iterator<NextTask> it = taskList.iterator();
                
        while (it.hasNext()) {
            NextTask nextTask = it.next();
            
            if (nextTask.GetRequiredBlocks().size() == 1) { // Nur Tasks mit einem Block
                Iterator<NextMapTile> nextMapIt = nextTask.GetRequiredBlocks().iterator();
                while (nextMapIt.hasNext()) {
                    NextMapTile nextMapTile = nextMapIt.next();
                    Iterator<NextMapTile> nextDispenserIt = dispenserLst.iterator();
                    while (nextDispenserIt.hasNext()) {
                        NextMapTile nextDispenserMapTile = nextDispenserIt.next();
                        if (nextDispenserMapTile.getThingType().contains(nextMapTile.getThingType())
                                && currentStep < nextTask.GetDeadline()) {
                            result.add(nextTask);
                        }
                    }
                }
            }
        }
        return result;
    }
   
    /**
     * Creates an action for the server to change a role:
     *
     * @param roleToChangeTo - a NextRole Element, to change to
     * @return Action
     */
    public static Action GenerateRoleChangeAction(String roleToChangeTo) {
        return new Action("adopt", new Identifier(roleToChangeTo));
    }

    /**
     * Checks if an agent occupies a specified tile:
     *
     * @param zones - a Set of NextMapTiles to be evaluated.
     * @return boolean - true if in zone
     */
    public static boolean CheckIfAgentInZoneUsingLocalView(HashSet<NextMapTile> zones) {
        for (NextMapTile tile : zones) {
            if (tile.getPositionX() == 0 && tile.getPositionY() == 0) {
                return true;
            }
        }
        return false;
    }

    public static Vector2D GetDispenserFromType(HashSet<NextMapTile> dispenser, String type) {
        Vector2D result = new Vector2D();
        Iterator<NextMapTile> it = dispenser.iterator();
        while (it.hasNext()) {
            NextMapTile next = it.next();
            if (next.getThingType().contains(type)) {
                result = next.GetPosition();  // AVL - Trying a different approach
                //result = new Vector2D(next.getPositionX(), next.getPositionY());
            }
        }
        return result;
    }
    
    public static Boolean IsObstacleInPosition(HashSet<NextMapTile> list, Vector2D position)
    {
    	for (NextMapTile tile : list) {
            if (tile.GetPosition() == position && tile.getThingType().contains("obstacle")) {
                return true;
            }
        }
        return false;
    }

    private static HashSet<String> getBlockTypes(HashSet<NextMapTile> list) {
        HashSet<String> blockTypes = new HashSet<String>();
        Iterator<NextMapTile> blocksIt = list.iterator();
        while (blocksIt.hasNext()) {
            NextMapTile next = blocksIt.next();
            blockTypes.add(next.getThingType());
        }
        return blockTypes;
    }

    /**
     * Unnötige Berechnung, daher bitte GetNearestZone() verwenden
     * @param goalzones
     * @return
     */
    @Deprecated
    public static Vector2D GetNearestGoalZone(HashSet<NextMapTile> goalzones) {
        NextManhattanPath manhattanPath = new NextManhattanPath();
        ArrayList<Action> list = new ArrayList<Action>();
        Iterator<NextMapTile> it = goalzones.iterator();
        Vector2D result = new Vector2D();

        if (it.hasNext()) {
            NextMapTile next = it.next();
            list = manhattanPath.calculatePath((int) next.getPositionX(), (int) next.getPositionY());

            while (it.hasNext()) {
                next = it.next();
                ArrayList<Action> calcList = manhattanPath.calculatePath((int) next.getPositionX(), (int) next.getPositionY());
                if (calcList.size() < list.size()) {
                    list = calcList;
                    result = next.GetPosition();
                }

            }
        }
        return result;
    }

    /**
     * Unnötige Berechnung, daher bitte GetNearestZone() verwenden
     * @param roleZone
     * @return
     */
    @Deprecated
    public static Vector2D GetNearestRoleZone(HashSet<NextMapTile> roleZone) {
        NextManhattanPath manhattanPath = new NextManhattanPath();
        ArrayList<Action> list = new ArrayList<Action>();
        Iterator<NextMapTile> it = roleZone.iterator();
        Vector2D result = new Vector2D();

        NextMapTile next = it.next();
        list = manhattanPath.calculatePath((int) next.getPositionX(), (int) next.getPositionY());

        while (it.hasNext()) {
            next = it.next();
            ArrayList<Action> calcList = manhattanPath.calculatePath((int) next.getPositionX(), (int) next.getPositionY());
            if (calcList.size() < list.size()) {
                list = calcList;
                result = next.GetPosition();
            }

        }
        return result;
    }
    
    /**
     * Usefully for all Zones
     * @param zone
     * @return Vector2D Position of nearest Zone
     */
    public static Vector2D GetNearestZone(Vector2D agentPosition, HashSet<NextMapTile> zone) {
        int smallestDistance = 0;
        Iterator<NextMapTile> it = zone.iterator();
        Vector2D result = new Vector2D();

        NextMapTile next = it.next();
        smallestDistance = ManhattanDistance(agentPosition, next.GetPosition());
        result = next.GetPosition();

        while (it.hasNext()) {
            next = it.next();
            int calcDistance = ManhattanDistance(agentPosition, next.GetPosition());
            if (calcDistance < smallestDistance) {
            	smallestDistance = calcDistance;
                result = next.GetPosition();
            }
        }
        return result;
    }

    /**
     * Reasoning Algorithm to decide wich role to adapt, based on the input of
     * desiredActions:
     *
     * @param desiredActions - a Set of desiredActions, to be fulfilled by the
     * new role.
     * @param rolesList - a Set of NextRoles to to chose a role from.
     *
     * @return NextRole
     */
    public static NextRole FindNextRoleToAdapt(HashSet<NextConstants.EActions> desiredActions, HashSet<NextRole> rolesList) throws Exception {
        HashMap<Integer, HashSet<NextRole>> roleSorting = new HashMap<>();

        for (NextRole role : rolesList) {
            int rating = 0;

            // Convert the List of Actions for this role to LowerCase
            HashSet<String> roleActions = role.GetAction();
            HashSet<String> roleActionsLowerCase = new HashSet<>();
            for (String roleName : roleActions) {
                roleActionsLowerCase.add(roleName.toLowerCase());
            }

            // Compare roles and count fulfilled desiredActions 
            for (NextConstants.EActions action : desiredActions) {
                if (roleActionsLowerCase.contains(action.toString().toLowerCase())) {
                    rating += 1;
                }
                roleSorting.putIfAbsent(rating, new HashSet<>());
                roleSorting.get(rating).add(role);
            }
        }

        int maxRating = Collections.max(roleSorting.keySet());

        if (maxRating < desiredActions.size()) {
            throw new Exception("Did not find optimal RoleToAdapt.");
        }

        NextRole bestRole = null;

        // second Level comparison based on maxSpeed value
        for (NextRole roleIterator : roleSorting.get(maxRating)) {
            if (bestRole == null) {
                bestRole = roleIterator;
            }
            if (bestRole.GetSpeed().get(0) < roleIterator.GetSpeed().get(0)) {
                bestRole = roleIterator;
            }
        }

        return bestRole;
    }

    /**
     * @deprecated Neue Version vorhanden CheckIfAgentInZoneUsingLocalView
     * @param goalzones
     * @return
     */
    @Deprecated
    public static Boolean IsAgentInGoalZone(HashSet<NextMapTile> goalzones) {
        Iterator<NextMapTile> it = goalzones.iterator();

        while (it.hasNext()) {
            NextMapTile next = it.next();
            // Agent at (0,0)
            if (new Vector2D().equals(next.GetPosition())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if block in correct Position
     * @param nextAgent
     * @return
     */
    public static Boolean IsBlockInCorrectPosition(NextAgent nextAgent) {
        // TODO miri Vergleich aller Blöcke - derzeit nur mit 1
        if (nextAgent.GetActiveTask() != null) {
            HashSet<Vector2D> attachedElements = nextAgent.getAgentStatus().GetAttachedElements();
            HashSet<NextMapTile> activeTask = nextAgent.GetActiveTask().GetRequiredBlocks();

            Iterator<Vector2D> attachElementIterator = attachedElements.iterator();
            Vector2D next = attachElementIterator.next();

            Iterator<NextMapTile> activeTaskIterator = activeTask.iterator();
            NextMapTile nextActiveTask = activeTaskIterator.next();

            if (next.equals(nextActiveTask.GetPosition())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated Alte Taskverarbeitung, um Task auf aktivität zu checken
     * @param nextAgent
     * @param actualSteps
     * @return
     */
    @Deprecated
    private static Boolean IsTaskActive(NextAgent nextAgent, int actualSteps) {
        NextTask activeTask = nextAgent.GetActiveTask();
        Iterator<NextTask> taskListIt = nextAgent.getSimulationStatus().GetTasksList().iterator();
        Boolean isInTakslist = false;
        while (taskListIt.hasNext()) {
            NextTask next = taskListIt.next();
            if (next.GetName().equals(activeTask.GetName()) && actualSteps <= activeTask.GetDeadline()) {
                isInTakslist = true;
                break;
            }
        }
        return isInTakslist;
    }

    /**
     * Check correct block type
     * @param nextTask
     * @param thingType
     * @return
     */
    public static Boolean IsCorrectBlockType(NextTask nextTask, String thingType) {
    	// TODO Mehrere Blöcke implementieren
        Iterator<NextMapTile> blocksIt = nextTask.GetRequiredBlocks().iterator();
        while (blocksIt.hasNext()) {
            NextMapTile next = blocksIt.next();
            if (thingType.contains(next.getThingType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if obstacle in next step
     * @param direction
     * @param obstacle
     * @return
     */
    public static NextMapTile IsObstacleInNextStep(ECardinals direction, HashSet<NextMapTile> obstacle) {
        Vector2D newAgentPosition = new Vector2D();
        switch (direction) {
            case n:
                newAgentPosition = new Vector2D(0, -1);
                break;
            case e:
                newAgentPosition = new Vector2D(1, 0);
                break;
            case s:
                newAgentPosition = new Vector2D(0, 1);
                break;
            case w:
                newAgentPosition = new Vector2D(-1, 0);
                break;
        }

        Iterator<NextMapTile> it = obstacle.iterator();

        while (it.hasNext()) {
            NextMapTile next = it.next();
            Vector2D nextPosition = next.GetPosition();
            if (newAgentPosition.equals(nextPosition)) {
                return next;
            }
        }
        return null;
    }
    
    public Vector2D NextDirection(ECardinals direction) {
        Vector2D newPosition = new Vector2D();
        switch (direction) {
            case n:
                newPosition = new Vector2D(0, -1);
                break;
            case e:
                newPosition = new Vector2D(1, 0);
                break;
            case s:
                newPosition = new Vector2D(0, 1);
                break;
            case w:
                newPosition = new Vector2D(-1, 0);
                break;
        }

        return newPosition;
    }

    public static Boolean IsBlockBehindMe(ECardinals direction, Vector2D block) {
        Vector2D newBlockPosition = new Vector2D();
        switch (direction) { // In die Richtung, in die ich gehen mag
            case n:
                newBlockPosition = new Vector2D(0, 1);
                break;
            case e:
                newBlockPosition = new Vector2D(-1, 0);
                break;
            case s:
                newBlockPosition = new Vector2D(0, -1);
                break;
            case w:
                newBlockPosition = new Vector2D(1, 0);
                break;
        }
        Vector2D blockPosition = new Vector2D(block.x, block.y);
        return blockPosition.equals(newBlockPosition);
    }

    public static Boolean IsBlockInFrontOfMe(ECardinals direction, Vector2D block) {
        Vector2D newBlockPosition = new Vector2D();
        switch (direction) { // In die Richtung, in die ich gehen mag
            case n:
                newBlockPosition = new Vector2D(0, -1);
                break;
            case e:
                newBlockPosition = new Vector2D(1, 0);
                break;
            case s:
                newBlockPosition = new Vector2D(0, 1);
                break;
            case w:
                newBlockPosition = new Vector2D(-1, 0);
                break;
        }
        Vector2D blockPosition = new Vector2D(block.x, block.y);
        return blockPosition.equals(newBlockPosition);
    }

    /**
     * Check if next Step is possible
     * @param direction
     * @param attachedElements
     * @param obstacles
     * @return
     */
    public static Boolean IsNextStepPossible(ECardinals direction, HashSet<Vector2D> attachedElements, HashSet<NextMapTile> obstacles) {
        ArrayList<Vector2D> newAgentPositionLst = new ArrayList<Vector2D>();
        ArrayList<NextMapTile> result = new ArrayList<NextMapTile>();
        Vector2D newDirection = new Vector2D(0, 0);
        switch (direction) {
            case n:
                // **O****
                // **XA***
                // *******
                newDirection = new Vector2D(0, -1);
                newAgentPositionLst.add(newDirection); // no block			
                if (!attachedElements.isEmpty()) {
                    Iterator<Vector2D> attachedElementsIt = attachedElements.iterator();
                    while (attachedElementsIt.hasNext()) {
                        Vector2D next = attachedElementsIt.next();
                        newAgentPositionLst.add(new Vector2D(next.x + newDirection.x, next.y + newDirection.y));
                    }
                }
                break;
            case e:
                // *******
                // **XAO**
                // *******
                newDirection = new Vector2D(1, 0);
                newAgentPositionLst.add(newDirection); // no block				
                if (!attachedElements.isEmpty()) {
                    Iterator<Vector2D> attachedElementsIt = attachedElements.iterator();
                    while (attachedElementsIt.hasNext()) {
                        Vector2D next = attachedElementsIt.next();
                        newAgentPositionLst.add(new Vector2D(next.x + newDirection.x, next.y + newDirection.y));
                    }
                }
                break;
            case s:
                // *******
                // **XA***
                // **OO***
                newDirection = new Vector2D(0, 1);
                newAgentPositionLst.add(newDirection); // no block		
                if (!attachedElements.isEmpty()) {
                    Iterator<Vector2D> attachedElementsIt = attachedElements.iterator();
                    while (attachedElementsIt.hasNext()) {
                        Vector2D next = attachedElementsIt.next();
                        newAgentPositionLst.add(new Vector2D(next.x + newDirection.x, next.y + newDirection.y));
                    }
                }
                break;
            case w:
                // *******
                // *ODA***
                // *******
                newDirection = new Vector2D(-1, 0);
                newAgentPositionLst.add(newDirection); // no block					
                if (!attachedElements.isEmpty()) {
                    Iterator<Vector2D> attachedElementsIt = attachedElements.iterator();
                    while (attachedElementsIt.hasNext()) {
                        Vector2D next = attachedElementsIt.next();
                        newAgentPositionLst.add(new Vector2D(next.x + newDirection.x, next.y + newDirection.y));
                    }
                }
                break;
        }

        Iterator<NextMapTile> it = obstacles.iterator();
        while (it.hasNext()) {
            NextMapTile next = it.next();
            Vector2D nextPosition = next.GetPosition();

            for (Iterator<Vector2D> iterator = newAgentPositionLst.iterator(); iterator.hasNext();) {
                Vector2D point = iterator.next();
                if (point.equals(nextPosition)) {
                    result.add(next);
                }
            }
        }
        return result.size() > 0 ? false : true;
    }

    /**
     * get opposite direction in Vector2D
     * @param direction
     * @return
     */
    public static Vector2D GetOppositeDirectionInVector2D(ECardinals direction) {
        switch (direction) {
            case n:
                return new Vector2D(0, 1);
            case e:
                return new Vector2D(-1, 0);
            case s:
                return new Vector2D(0, -1);
            case w:
                return new Vector2D(1, 0);
        }
        return null;
    }
    
    /**
     * get opposite Direction
     * @param direction
     * @return
     */
    public static ECardinals GetOppositeDirection(ECardinals direction) {
        switch (direction) {
            case n:
                return ECardinals.s;
            case e:
                return ECardinals.w;
            case s:
                return ECardinals.n;
            case w:
                return ECardinals.e;
        }
        return null;
    }
    
    /**
     * n/s -> e/w
     * e/w -> n/s
     * @param direction
     * @return
     */
    public static ECardinals GetOtherAxis(ECardinals direction) {
        switch (direction) {
            case n:
                return ECardinals.e;
            case e:
                return ECardinals.s;
            case s:
                return ECardinals.w;
            case w:
                return ECardinals.n;
        }
        return null;
    }
    
    /**
     * Get Random Point in direction
     * @param direction
     * @param agentPosition
     * @param distance
     * @return
     */
    public static Vector2D RandomPointInDirection(String direction, Vector2D agentPosition, int distance) {
    	int x = agentPosition.x + GenerateRandomNumber(distance/2);
    	int y = agentPosition.y + GenerateRandomNumber(distance/2);
        switch (direction) {
            case "ne":
                return new Vector2D(x , y *-1);
            case "nw":
                return new Vector2D(x* -1, y*-1);
            case "se":
                return new Vector2D(x, y);
            case "sw":
                return new Vector2D(x * -1, y);
        }
        return null;
    }
    
    /**
     * Calculate Manhattan distance
     * @param origin
     * @param target
     * @return
     */
    public static int ManhattanDistance(Vector2D origin, Vector2D target) {
        return Math.abs(target.x-origin.x) + Math.abs(target.y-origin.y);
    }

	public static String RotateInWhichDirection(HashSet<Vector2D> attachedElements, HashSet<NextMapTile> requiredBlocks) {
		String result = "cw";
		if(attachedElements.size() == 1) {
			Vector2D blockPosition = attachedElements.iterator().next(); 
			if(requiredBlocks.size() == 1)
			{
				Vector2D requiredBlock = requiredBlocks.iterator().next().GetPosition();
				if(blockPosition.getRotatedCCW().equals(requiredBlock)) result = "ccw";
				else result = "cw";
			}
			else 
			{
				// TODO Mehere Blöcke
			}
		}
		else {
			// TODO Mehere attachedElements
//			Iterator attachedElementsIt = attachedElements.iterator();
//			while(attachedElementsIt.hasNext())
//			{
//				Vector2D next = attachedElementsIt.next();
//			}
		}
		return result;
	}

    public static String GetOtherRotation(String current)
    {
    	return current.equals("cw") ? "ccw" : "cw";
    }

	public static Vector2D GetNextToRotateDirection(Vector2D blockPosition, String direction) {
		Vector2D rotateTo = blockPosition.clone();
		if(direction.equals("cw")) rotateTo.rotateCW(); 
		else rotateTo.rotateCCW();
		return rotateTo;
	}
}
