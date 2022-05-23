package massim.javaagents.intention;

import eis.iilang.Action;
import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextActionWrapper;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class NextIntention {

    private NextAgent nextAgent;
    HashSet<Action> possibleActions;

    public NextIntention(NextAgent nextAgent){
        this.nextAgent = nextAgent;
        possibleActions = new HashSet<Action>();
    }

    /**
     * Selects the next Action based on the priorityMap
     *
     * @return Action
     */
    public Action SelectNextAction() {

        Action nextAction = NextActionWrapper.CreateAction(NextConstants.EActions.SKIP);

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
        possibleActions.add(NextAgentUtil.generateRandomMove());

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
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.REQUEST, NextAgentUtil.GetDirection(position)));
                    }
                }

                if (visibleThing.getThingType().contains("block")) {
                    if (nextAgent.getStatus().GetAttachedElementsAmount() < 2) {
                        possibleActions.add(NextActionWrapper.CreateAction(NextConstants.EActions.ATTACH, NextAgentUtil.GetDirection(position)));
                    }
                }
            }

        }
    }
}
