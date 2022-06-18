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

    /**
     * Reports, if a Thing is next to the Agent
     *
     * @param position - x-Value, y-Value of a Thing
     * @param status - #source of Information
     * @return boolean
     */
    public static boolean NextTo(Vector2D position, NextAgentStatus status) {
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
     * Returns the direction for an action
     *
     * @param xValue - x-Value of Thing
     * @param yValue - y-Value of Thing
     * @return Identifier for the direction value of an action.
     */
    public static Identifier GetDirection(Vector2D direction) {
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
        return new Action("survey", new Identifier("" + xPosition), new Identifier("" + yPosition));
    }

    public static ArrayList<NextTask> EvaluatePossibleTask(HashSet<NextTask> taskList, HashSet<NextMapTile> dispenserLst, int actualSteps) {
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
                                && actualSteps < nextTask.GetDeadline()) {
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
    public static Action GenerateRoleChangeAction(NextRole roleToChangeTo) {
        return new Action("adopt", new Identifier(roleToChangeTo.GetName()));
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
                result = new Vector2D(next.getPositionX(), next.getPositionY());
            }
        }
        return result;
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

    //For testing only
    public static NextMapTile GetNearestGoalZoneMapTile(HashSet<NextMapTile> goalzones) {
        NextManhattanPath manhattanPath = new NextManhattanPath();
        ArrayList<Action> list = new ArrayList<Action>();
        Iterator<NextMapTile> it = goalzones.iterator();
        NextMapTile nearestMapTile = null;

        NextMapTile next = it.next();
        list = manhattanPath.calculatePath((int) next.getPositionX(), (int) next.getPositionY());
        nearestMapTile = next;

        while (it.hasNext()) {
            next = it.next();
            ArrayList<Action> calcList = manhattanPath.calculatePath((int) next.getPositionX(), (int) next.getPositionY());
            if (calcList.size() < list.size()) {
                list = calcList;
                nearestMapTile = next;
            }

        }
        return nearestMapTile;
    }

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
                    result = next.getPosition();
                }

            }
        }
        return result;
    }

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
                result = next.getPosition();
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

    public static Boolean IsAgentInGoalZone(HashSet<NextMapTile> goalzones) {
        Iterator<NextMapTile> it = goalzones.iterator();

        while (it.hasNext()) {
            NextMapTile next = it.next();
            // Agent at (0,0)
            if (new Vector2D().equals(next.getPosition())) {
                return true;
            }
        }
        return false;
    }

    public static Boolean IsBlockInCorrectPosition(NextAgent nextAgent) {
        // TODO miri Vergleich aller Blöcke - derzeit nur mit 1
        if (nextAgent.GetActiveTask() != null) {
            HashSet<Vector2D> attachedElements = nextAgent.getAgentStatus().GetAttachedElements();
            HashSet<NextMapTile> activeTask = nextAgent.GetActiveTask().GetRequiredBlocks();

            Iterator<Vector2D> attachElementIterator = attachedElements.iterator();
            Vector2D next = attachElementIterator.next();

            Iterator<NextMapTile> activeTaskIterator = activeTask.iterator();
            NextMapTile nextActiveTask = activeTaskIterator.next();

            if (next.equals(nextActiveTask.getPoint())) {
                return true;
            }
        }

        return false;
    }

    public static Boolean IsTaskActive(NextAgent nextAgent, int actualSteps) {
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

    public static Boolean IsCorrectBlockType(NextTask nextTask, String thingType) {
        Iterator<NextMapTile> blocksIt = nextTask.GetRequiredBlocks().iterator();
        while (blocksIt.hasNext()) {
            NextMapTile next = blocksIt.next();
            if (thingType.contains(next.getThingType())) {
                return true;
            }
        }
        return false;
    }

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
            Vector2D nextPosition = next.getPosition();
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
                //newAgentPosition = new Vector2D(0, -1);
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
                //newAgentPosition = new Vector2D(1, 0);
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
                //newAgentPosition = new Vector2D(0, 1);
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
                //newAgentPosition = new Vector2D(-1, 0);
                break;
        }

//		newAgentPositionLstArrayList.add(new Vector2D());
        Iterator<NextMapTile> it = obstacles.iterator();
        while (it.hasNext()) {
            NextMapTile next = it.next();
            Vector2D nextPosition = next.getPosition();

            for (Iterator<Vector2D> iterator = newAgentPositionLst.iterator(); iterator.hasNext();) {
                Vector2D point = iterator.next();
                if (point.equals(nextPosition)) {
                    result.add(next);
                }
            }
//    		if(newAgentPositionLstArrayList.equals(nextPosition)) {
//    			return next;
//    		}
        }
        return result.size() > 0 ? false : true;
        //return result;
    }

    public static Vector2D GetOppositeDirection(ECardinals direction) {
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
    
    public static int ManhattanDistance(Vector2D origin, Vector2D target) {
        return Math.abs(target.x-origin.x) + Math.abs(target.y-origin.y);
    }
}
