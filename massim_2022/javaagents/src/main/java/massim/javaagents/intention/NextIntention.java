package massim.javaagents.intention;

import eis.iilang.Action;
import massim.javaagents.agents.AgentUtil;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.general.NextConstants.EPathFinding;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.pathfinding.PathfindingConfig;

import java.awt.*;
import java.util.ArrayList;

public class NextIntention {

    private NextAgent nextAgent;
    ArrayList<Action> possibleActions;

    public NextIntention(NextAgent nextAgent){
        this.nextAgent = nextAgent;
        possibleActions = new ArrayList<Action>();
    }

    /**
     * Selects the next Action based on the priorityMap
     *
     * @return Action
     */
    public Action SelectNextAction() {

        Action nextAction = NextActionWrapper.CreateAction(NextConstants.EActions.skip);
        

        //Compares each action based on the value
        if (possibleActions != null) {
            for (Action action : possibleActions) {
                if (NextConstants.PriorityMap.get(action.getName()) < NextConstants.PriorityMap.get(nextAction.getName())) {
                    nextAction = action;
                }
            }
        }
        return nextAction;
    }

    public void GeneratePossibleActions() {
        possibleActions.clear();
    	Action nextMove = AgentUtil.GenerateMove(PathfindingConfig.GetAlgorithm());
    	if(nextMove != null) 
    	{
    		possibleActions.add(nextMove);
        }

        // Localises the distance to the next target:  "dispenser", "goal", "role"
        possibleActions.add(NextAgentUtil.GenerateSurveyThingAction("dispenser"));

        // Survey a specific field with an agent. Get Name, Role, Energy
        // Attributes x-Position, y-Position relative to the Agent
        possibleActions.add(NextAgentUtil.GenerateSurveyAgentAction(0, 0));

        //Special case: Interaction with an adjacent element.
        for (NextMapTile visibleThing : nextAgent.getStatus().GetVision()) {

            Point position = visibleThing.getPoint();

            if (NextAgentUtil.NextTo(position, nextAgent.getStatus())) {

                if (visibleThing.getThingType().contains("dispenser")) {

                    if (nextAgent.getStatus().GetAttachedElementsAmount() < 2) {
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.request, NextAgentUtil.GetDirection(position)));
                    }
                }

                if (visibleThing.getThingType().contains("block")) {
                    if (nextAgent.getStatus().GetAttachedElementsAmount() < 2) {
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.attach, NextAgentUtil.GetDirection(position)));
                    }
                }
            }

        }
    }
}
