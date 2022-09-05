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
    	if(range <= 0) range = 1;
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
        NextAgentStatus status = agent.GetAgentStatus();
        if (position.equals(NextConstants.WestPoint) && !status.GetAttachedElementsVector2D().contains(NextConstants.WestPoint)) {
            return true;
        }
        if (position.equals(NextConstants.NorthPoint) && !status.GetAttachedElementsVector2D().contains(NextConstants.NorthPoint)) {
            return true;
        }
        if (position.equals(NextConstants.EastPoint) && !status.GetAttachedElementsVector2D().contains(NextConstants.EastPoint)) {
            return true;
        }
        if (position.equals(NextConstants.SouthPoint) && !status.GetAttachedElementsVector2D().contains(NextConstants.SouthPoint)) {
            return true;
        }
        return false;
    }
    
    /**
     * Reports, if a Thing is next to the Agent using Absolute coordinates
     *
     * @param position - x-Value, y-Value of a Thing
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
     * @param direction - direction as a Vector2D
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

    public static boolean IsRotationPossible(NextAgentStatus agentStatus, String direction) {

        HashSet<NextMapTile> visibleThings = agentStatus.GetFullLocalView();
        HashSet<Vector2D> attachedElements = agentStatus.GetAttachedElementsVector2D();

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
                if (visibleThing.GetPosition().equals(rotateTo) && !visibleThing.IsWalkableStrict()) {
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
     * @param xPosition X-Coordinate relative to the surveing Agent
     * @param yPosition Y-Coordinate relative to the surveing Agent
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
        for(NextMapTile tile : dispenser)
        {
        	if (tile.getThingType().contains(type)) {
                result = tile.GetPosition();  // AVL - Trying a different approach
                //result = new Vector2D(next.getPositionX(), next.getPositionY());
            }
        }
        return result;
    }

    public static NextMapTile GetNearestDispenserFromType(HashSet<NextMapTile> dispenser, String type, Vector2D position) {
        NextMapTile result = null;
        int nearestDistanz = 1000;
        for(NextMapTile tile : dispenser)
        {
            if (tile.getThingType().contains(type)) {
                int distanz = ManhattanDistance(tile.GetPosition(), position);
                if (distanz < nearestDistanz){
                    nearestDistanz = distanz;
                    result = tile;
                }
            }
        }
        return result;
    }
    
    public static Boolean IsObstacleInPosition(HashSet<NextMapTile> list, Vector2D position)
    {
    	for (NextMapTile tile : list) {
            if (tile.GetPosition().equals(position) && tile.IsObstacle()) {
                return true;
            }
        }
        return false;
    }

    private static HashSet<String> getBlockTypes(HashSet<NextMapTile> list) {
        HashSet<String> blockTypes = new HashSet<String>();
        for(NextMapTile tile : list)
        {
            blockTypes.add(tile.getThingType());
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
        int smallestDistance = 1000;
        Iterator<NextMapTile> it = zone.iterator();
        Vector2D result = new Vector2D();

        for(NextMapTile zoneMapTile : zone)
        {   
        	if(zoneMapTile.IsWalkable())
        	{
	        	int calcDistance = ManhattanDistance(agentPosition, zoneMapTile.GetPosition());
	            if (calcDistance < smallestDistance) {
	            	smallestDistance = calcDistance;
	                result = zoneMapTile.GetPosition();
	            }
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
     * Correct Block position 
     * @param blockPosition
     * @param attachedElements
     * @return
     */
    public static Boolean IsBlockInPosition(Vector2D blockPosition, HashSet<Vector2D> attachedElements)
    {
    	for(Vector2D attachedElement : attachedElements)
        {
    		if(attachedElement.equals(blockPosition))
    		{
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
        if (nextAgent.GetActiveTask() != null) {
            HashSet<Vector2D> attachedElements = nextAgent.GetAgentStatus().GetAttachedElementsVector2D();
            HashSet<NextMapTile> activeTask = nextAgent.GetActiveTask().GetRequiredBlocks();

            for(Vector2D attachedElement : attachedElements)
            {
            	for(NextMapTile tile : activeTask)
            	{
            		if(attachedElement.equals(tile.GetPosition()))
            		{
            			return true;
            		}
            	}
            }
        }

        return false;
    }

    /**
     * Check if a position is free
     * @param pos target position
     * @param localView the agent's local view
     * @return if the position is free true else false
     */
    public static Boolean IsPositionFreeUsingLocalView(Vector2D pos, HashSet<NextMapTile> localView){
        for (NextMapTile field : localView){
            if (field.GetPosition().equals(pos)) return field.IsWalkable();
        }
        // pos is not in localView
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
        Iterator<NextTask> taskListIt = nextAgent.GetSimulationStatus().GetTasksList().iterator();
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
     * @param requiredBlocks
     * @param thingType
     * @return
     */
    public static Boolean IsCorrectBlockType(HashSet<NextMapTile> requiredBlocks, String thingType) {
    	// TODO Mehrere Blöcke implementieren
    	for(NextMapTile tile : requiredBlocks)
    	{
    		if (IsCorrentBlockType(tile, thingType)) {
                return true;
            }
    	}    	
        return false;
    }
    
    /**
     * 
     * @param requiredBlock
     * @param thingType
     * @return
     */
    public static Boolean IsCorrentBlockType(NextMapTile requiredBlock, String thingType)
    {
    	if(thingType.contains(requiredBlock.getThingType())) return true;
    	else return false;
    }

    /**
     * check if things in next step
     * @param direction
     * @param visibleThings
     * @return
     */
    public static NextMapTile IsThingInNextStep(ECardinals direction, HashSet<NextMapTile> visibleThings) {
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


        for (NextMapTile visibleThing : visibleThings) {
        	Vector2D nextPosition = visibleThing.GetPosition();
            if (newAgentPosition.equals(nextPosition)) {
                return visibleThing;
            }
        }
        return null;
    }
    
    public static ECardinals NextDirection(ECardinals direction) {
        switch (direction) {
            case n:
                return ECardinals.e;
            case e:
                return ECardinals.s;
            case s:
                return ECardinals.w;
            case w:
                return ECardinals.n;
            default:
            	return direction;
        }
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
     * @param direction from the opposide
     * @param attachedElements all attached Elements
     * @param localView all objects from the localView
     * @return
     */
    public static Boolean IsNextStepPossible(ECardinals direction, HashSet<Vector2D> attachedElements, HashSet<NextMapTile> localView) {
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

        for(NextMapTile tile : localView)
        {
        	for(Vector2D position : newAgentPositionLst)
        	{
        		if(position.equals(tile.GetPosition()))
        		{
        			result.add(tile);
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
     * get opposite Vector2D
     * @param vector
     * @return
     */
    public static Vector2D GetOppositeVector(Vector2D vector)
    {
    	if(vector.equals(new Vector2D(0, -1))) return new Vector2D(0, 1);
    	else if(vector.equals(new Vector2D(0, 1))) return new Vector2D(0, -1);
    	else if(vector.equals(new Vector2D(-1, 0))) return new Vector2D(1, 0);
    	else return new Vector2D(-1, 0);
    }
    
    public static ECardinals ConvertVector2DToECardinals(Vector2D vector)
    {
    	if(vector.equals(new Vector2D(0, -1))) return ECardinals.n;
    	else if(vector.equals(new Vector2D(0, 1))) return ECardinals.s;
    	else if(vector.equals(new Vector2D(-1, 0))) return ECardinals.w;
    	else return ECardinals.e;
    }
    
    public static Vector2D ConvertECardinalsToVector2D(ECardinals direction)
    {
    	if(direction.equals(ECardinals.n)) return new Vector2D(0, -1); 
    	else if(direction.equals(ECardinals.e)) return new Vector2D(1 ,0); 
    	else if(direction.equals(ECardinals.s)) return new Vector2D(0, 1); 
    	else return new Vector2D(-1, 0); 
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
        
        int baseMovementPercent = 25;
        int baseMovement = distance/200*baseMovementPercent;
        int randomNumber = GenerateRandomNumber(distance-2*baseMovement);
    	//int offsetX = distance/4 + randomNumber;
    	//int offsetY = distance/4 + (distance/2-randomNumber);
        
        int offsetX = baseMovement + randomNumber; 
    	int offsetY = distance - baseMovement - randomNumber; // basemovement + ((distance - 2*baseMovement) - randomNumber)
        
        
        switch (direction) {
            case "ne":
                offsetY *= -1;
                break;
            case "nw":
                offsetX *= -1;
                offsetY *= -1;
                break;
            case "se":
                break;
            case "sw":
                offsetX *= -1;
                break;
        }
        
        int x = agentPosition.x + offsetX;
    	int y = agentPosition.y + offsetY;
        
        return new Vector2D(x, y);
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

	/**
	 * get the other rotationdirection
	 * @param current
	 * @return
	 */
    public static String GetOtherRotation(String current)
    {
    	return current.equals("cw") ? "ccw" : "cw";
    }

    /**
     * Check rotation direction
     * @param blockPosition
     * @param direction
     * @return
     */
	public static Vector2D GetNextToRotateDirection(Vector2D blockPosition, String direction) {
		Vector2D rotateTo = blockPosition.clone();
		if(direction.equals("cw")) rotateTo.rotateCW(); 
		else rotateTo.rotateCCW();
		return rotateTo;
	}
	
	/**
	 * Check, if another Agent is in near from block
	 * @param blockPosition
	 * @param localView
	 * @return
	 */
	public static Boolean IsAnotherAgentInNearOfBlock(Vector2D blockPosition, HashSet<NextMapTile> localView)
	{
		for(NextMapTile tile : localView)
		{	
			if(tile.getThingType().contains("entity")) {
				Vector2D newBlockPosition = new Vector2D(blockPosition);
				newBlockPosition.add(new Vector2D(0, -1)); //n
				if(isAgentInNewBlockPosition(tile, newBlockPosition)) return true;
				
				newBlockPosition = new Vector2D(blockPosition);
				newBlockPosition.add(new Vector2D(1, 0)); //e
				if(isAgentInNewBlockPosition(tile, newBlockPosition)) return true;

				newBlockPosition = new Vector2D(blockPosition);
				newBlockPosition.add(new Vector2D(0, 1)); //s
				if(isAgentInNewBlockPosition(tile, newBlockPosition)) return true;

				newBlockPosition = new Vector2D(blockPosition);
				newBlockPosition.add(new Vector2D(-1, 0)); //w
				if(isAgentInNewBlockPosition(tile, newBlockPosition)) return true;

			}
		}	
		return false;
	}
	
	private static Boolean isAgentInNewBlockPosition(NextMapTile tile, Vector2D newBlockPosition)
	{
		if(!tile.GetPosition().equals(new Vector2D(0,0))
				&& tile.GetPosition().equals(newBlockPosition) 
				&& tile.getThingType().contains("entity"))
		{
			return true;
		}
		return false;
	}
	
	public static HashSet<NextAgent> getAgentsInFrontOfBlock(Vector2D agentPosition, HashSet<NextAgent> agentSet, Vector2D blockPosition)
	{
		HashSet<NextAgent> agentsInFront = new HashSet<NextAgent>();
		for(NextAgent agent : agentSet)
		{
			Vector2D newBlockPosition = new Vector2D(blockPosition).getAdded(agentPosition);
			newBlockPosition.add(new Vector2D(0, -1)); //n
			if(!newBlockPosition.equals(agentPosition) && agent.GetPosition().equals(newBlockPosition)) 
				agentsInFront.add(agent);
			
			newBlockPosition = new Vector2D(blockPosition).getAdded(agentPosition);
			newBlockPosition.add(new Vector2D(1, 0)); //e
			if(!newBlockPosition.equals(agentPosition) && agent.GetPosition().equals(newBlockPosition)) 
				agentsInFront.add(agent);

			newBlockPosition = new Vector2D(blockPosition).getAdded(agentPosition);
			newBlockPosition.add(new Vector2D(0, 1)); //s
			if(!newBlockPosition.equals(agentPosition) && agent.GetPosition().equals(newBlockPosition)) 
				agentsInFront.add(agent);

			newBlockPosition = new Vector2D(blockPosition).getAdded(agentPosition);
			newBlockPosition.add(new Vector2D(-1, 0)); //w
			if(!newBlockPosition.equals(agentPosition) && agent.GetPosition().equals(newBlockPosition)) 
				agentsInFront.add(agent);
		}
		return agentsInFront;
	}
	
    /**
     * Gets the ECardinal from thing
     *
     * @param position - x-Value, y-Value of a Thing
     * @param agent - the Agent to be compared to
     * @return boolean
     */
    public static ECardinals GetECardinalFromThing(Vector2D thingPosition) {
        if (thingPosition.equals(NextConstants.WestPoint)) {
            return ECardinals.w;
        }
        if (thingPosition.equals(NextConstants.NorthPoint)) {
            return ECardinals.n;
        }
        if (thingPosition.equals(NextConstants.EastPoint)) {
            return ECardinals.e;
        }
        if (thingPosition.equals(NextConstants.SouthPoint)) {
            return ECardinals.s;
        }
        return ECardinals.n;
    }

	public static boolean IsThisBlockAttachedToOtherAgent(Vector2D position, Vector2D agentPosition, HashSet<NextAgent> agentSet) {
		for(NextAgent agent : agentSet)
		{
			NextAgentStatus agentStatus = agent.GetAgentStatus();
			// agent has Element, the equals position and not the same Position of the Map
			if(agentStatus.GetAttachedElementsAmount() > 0 
					&& !agent.GetPosition().equals(agentPosition)
					&& agentStatus.GetAttachedElementsVector2D().iterator().next().equals(position)
					&& agentStatus.GetAttachedElementsVector2D().iterator().next().getAdded(agent.GetPosition())
						.equals(agentPosition.getAdded(position)
						)
			)
			{
				return true;
			}
		}
		return false;
	}
	
	public static HashSet<NextAgent> GetAgentsWhoThisBlockIsAttached(Vector2D position, Vector2D agentPosition, HashSet<NextAgent> agentSet) {
		HashSet<NextAgent> agents = new HashSet<NextAgent>();
		for(NextAgent agent : agentSet)
		{
			NextAgentStatus agentStatus = agent.GetAgentStatus();
			// agent has Element, the equals position and not the same Position of the Map
			if(agentStatus.GetAttachedElementsAmount() > 0 
					&& !agent.GetPosition().equals(agentPosition) // nicht ich selbst
					&& agentStatus.GetAttachedElementsVector2D().iterator().next().getAdded(agent.GetPosition()) // attachedElement + Agentenposition	
							.equals(agentPosition.getAdded(position))
			)
			{
				agents.add(agent);
			}
		}
		return agents;
	}

	// look at thing in localview
	public static Vector2D GetFirstBlockOrObstacleInLocalView(HashSet<NextMapTile> getFullLocalView) {
		for(NextMapTile tile : getFullLocalView)
		{
			if(tile.getThingType().contains("block") || tile.getThingType().contains("obstacle")) return tile.getPosition();
		}
		
		return null;
	}

	// returns the MapTile from the specific visible thing
	public static HashSet<NextMapTile> GetThingFromVisibleThings(HashSet<NextMapTile> getVisibleThings, String thing) {
		HashSet<NextMapTile> thingNextMapTiles = new HashSet<NextMapTile>();
		for(NextMapTile tile : getVisibleThings)
		{
			if(tile.getThingType().contains(thing)) thingNextMapTiles.add(tile);
		}
		return thingNextMapTiles;
	}
	
//	public static boolean IsNextToEvents(NextAgent nextAgent)
//	{
//		
//	}
}
