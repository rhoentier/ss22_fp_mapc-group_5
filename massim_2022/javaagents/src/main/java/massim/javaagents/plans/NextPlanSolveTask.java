package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextConstants;
import massim.javaagents.groupPlans.NextAgentPlan;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;


public class NextPlanSolveTask extends NextPlan {

    private boolean isPreconditionFulfilled = false;
    private boolean readyToConnect = false;
    private final NextTask task;

    public NextPlanSolveTask(NextAgent agent, NextAgentPlan groupPlan) {
        this.agent = agent;
        this.task = groupPlan.GetTask();
        this.agentTask = NextConstants.EAgentActivity.solveTask;
        subPlans = groupPlan.GetSubPlans();
        UpdateInternalBelief();
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
            if (!agent.GetAgentStatus().GetCurrentRole().GetName().equals("worker") && !subPlans.get(0).GetAgentTask()
                    .equals(NextConstants.EAgentActivity.exploreMap))
                subPlans.add(0, new NextPlanRoleZone(agent, "worker"));
        }

        HashSet<String> requiredBlocks = task.GetRequiredBlocks().stream().map(NextMapTile::GetThingType)
                .collect(Collectors.toCollection(HashSet::new));
        isPreconditionFulfilled = agent.GetMap().IsTaskExecutable(requiredBlocks);
        if (!isPreconditionFulfilled) fulfillPrecondition();
    }

    public void SetReadyToConnect() {
        readyToConnect = true;
    }

    /**
     * Prüft, ob der Task vollständig erfüllt ist und setzt ggf. die subPlans zurück
     */
    public boolean CheckIfTaskIsFulfilled() {
        if (agent.GetAgentStatus().GetLastAction().equals("submit") && agent.GetAgentStatus().GetLastActionResult()
                .equals("success")) {
            for (NextPlan subplan : subPlans) {
                subplan.SetPlanIsFulfilled(false);
            }
            readyToConnect = false;

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
                if (agent.GetAgentStatus().GetAttachedElementsAmount() == 0) {
                    subPlan.SetPlanIsFulfilled(false);
                    continue;
                }
                // prüft, welche Blöcke momentan attached sind
                HashSet<NextMapTile> attachedElements = agent.GetAgentStatus().GetAttachedElementsNextMapTiles();
                ArrayList<String> attachedBlockTypes = new ArrayList<>();
                for (NextMapTile attachedElement : attachedElements) {
                    if (attachedElement.GetThingType().contains("block")) {
                        attachedBlockTypes.add(
                                attachedElement.GetThingType().substring(attachedElement.GetThingType().length() - 2));
                    }
                }
                //prüft, ob Blöcke momentan attached sind und stellt subPlans (goToDispenser) auf fertig
                if (attachedBlockTypes.contains(((NextPlanDispenser) subPlan).GetDispenser().GetThingType()))
                    subPlan.SetPlanIsFulfilled(true);
                continue;
            }
            if (subPlan instanceof NextPlanGoalZone) {
                if (readyToConnect && subPlanIterator.hasNext()) {
                    subPlan.SetPlanIsFulfilled(true);
                    continue;
                }
                if (subPlanIterator.hasNext()) subPlan.SetPlanIsFulfilled(
                        NextAgentUtil.CheckIfAgentInZoneUsingLocalView(agent.GetAgentStatus().GetGoalZones()));
                continue;
            }
            if (subPlan instanceof NextPlanConnect) {
                if (agent.GetAgentStatus().GetLastAction().contains("connect") && agent.GetAgentStatus()
                        .GetLastActionResult().contains("success")) {
                    ((NextPlanConnect) subPlan).SetAgentConnection(true);
                }
            }
        }
    }
}
