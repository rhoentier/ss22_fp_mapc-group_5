package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextConstants;
import massim.javaagents.groupPlans.NextAgentPlan;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;


public class NextPlanSolveTask extends NextPlan {

    private boolean isPreconditionFulfilled = false;
    private final NextTask task;

    public NextPlanSolveTask(NextAgent agent, NextAgentPlan groupPlan) {
        this.agent = agent;
        this.task = groupPlan.GetTask();
        this.agentTask = NextConstants.EAgentActivity.solveTask;
        CheckIfPreConditionIsFulfilled();
        subPlans = groupPlan.GetSubPlans();
    }

    /**
     * Versucht die Karte zu durchsuchen, um eine Aufgabe zu lösen
     */
    private void fulfillPrecondition() {
        subPlans.add(0, new NextPlanExploreMap(task.GetRequiredBlocks(), agent));
    }

    /**
     * Prüft, ob die Vorbedingung erfüllt ist
     */
    public void CheckIfPreConditionIsFulfilled() {

        if (!subPlans.isEmpty()) {
            if (!agent.GetAgentStatus().GetCurrentRole().GetName().equals("worker") && !subPlans.get(0).GetAgentTask().equals(NextConstants.EAgentActivity.exploreMap))
                subPlans.add(0, new NextPlanRoleZone(agent, "worker"));
        }

        HashSet<String> requiredBlocks = task.GetRequiredBlocks().stream().map(NextMapTile::getThingType).collect(Collectors.toCollection(HashSet::new));
        isPreconditionFulfilled = agent.GetMap().IsTaskExecutable(requiredBlocks);
        if (!isPreconditionFulfilled) fulfillPrecondition();
    }

    /**
     * Prüft, ob der Task vollständig erfüllt ist und setzt ggf. die subPlans zurück
     */
    public boolean CheckIfTaskIsFulfilled() {
        if (agent.GetAgentStatus().GetLastAction().equals("submit") && agent.GetAgentStatus().GetLastActionResult().equals("success")) {
            for (NextPlan subplan : subPlans) {
                subplan.SetPlanIsFulfilled(false);
            }
            return true;
        }
        return false;
    }

    public NextTask GetTask() {
        return task;
    }

    public void UpdateInternalBelief() {
        CheckIfPreConditionIsFulfilled();
        if (CheckIfTaskIsFulfilled()) return;
        for (Iterator<NextPlan> subPlanIterator = subPlans.iterator(); subPlanIterator.hasNext(); ) {
            NextPlan subPlan = subPlanIterator.next();
            if (subPlan instanceof NextPlanRoleZone) {
                if (agent.GetAgentStatus().GetCurrentRole().GetName().equals("worker")) subPlanIterator.remove();
                continue;
            }
            if (subPlan instanceof NextPlanExploreMap) {
                if (isPreconditionFulfilled) subPlanIterator.remove();
                else ((NextPlanExploreMap) subPlan).CheckPreconditionStatus();
                continue;
            }
            if (subPlan instanceof NextPlanDispenser) {
                // prüft, welche Blöcke momentan attached sind
                HashSet<NextMapTile> visibleThings = agent.GetAgentStatus().GetVisibleThings();
                HashSet<Vector2D> attachedElements = agent.GetAgentStatus().GetAttachedElementsVector2D();
                ArrayList<String> attachedBlockTypes = new ArrayList<>();
                for (Vector2D attachedElement : attachedElements) {
                    for (NextMapTile visibleThing : visibleThings) {
                        if (attachedElement.equals(visibleThing.GetPosition())) {
                            if (visibleThing.getThingType().contains("block")) {
                                attachedBlockTypes.add(visibleThing.getThingType().substring(visibleThing.getThingType().length() - 2));
                            }
                        }
                    }
                }
                //prüft, ob Blöcke momentan attached sind und stellt subPlans (goToDispenser) auf fertig
                subPlan.SetPlanIsFulfilled(attachedBlockTypes.contains(((NextPlanDispenser) subPlan).GetDispenser().getThingType()));
            }
            if (subPlan instanceof NextPlanGoalZone) {
                if (NextAgentUtil.CheckIfAgentInZoneUsingLocalView(agent.GetAgentStatus().GetGoalZones())) {
                    if (subPlanIterator.hasNext()) subPlan.SetPlanIsFulfilled(true);
                }
            }
        }
    }
}
