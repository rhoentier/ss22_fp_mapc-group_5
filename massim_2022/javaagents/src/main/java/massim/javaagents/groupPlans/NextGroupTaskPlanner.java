package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;
import massim.javaagents.plans.NextPlanDispenser;
import massim.javaagents.plans.NextPlanGoalZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class NextGroupTaskPlanner {

    private final HashMap<NextAgent, NextGroupPlanForAgent> currentPlans = new HashMap<>();
    private final NextGroup group;
    private final HashSet<NextGroupPlanTask> activePlans = new HashSet<>();

    public NextGroupTaskPlanner(NextGroup group) {
        this.group = group;
    }

    /**
     * Erzeugt für alle Agents der Gruppe den passenden Task
     */
    private void planAgentTasks() {
        HashSet<NextAgent> agents = group.GetAgents();
        if (agents.size() == 1) {
            for (NextAgent agent : agents) {
                currentPlans.put(agent, getBestTaskForOneAgent());
            }
        }
    }

    private NextGroupPlanForAgent getBestTaskForOneAgent() {
        NextGroupPlanTask bestPlan = null;
        for (NextGroupPlanTask possiblePlan : activePlans) {
            if (possiblePlan.IsPreconditionFulfilled() && possiblePlan.IsDeadlineFulfillable() && possiblePlan.GetTask().GetRequiredBlocks().size() == 1) {
                if (bestPlan == null || bestPlan.GetUtilization() < possiblePlan.GetUtilization())
                    bestPlan = possiblePlan;
            }
            if (bestPlan == null && possiblePlan.GetTask().GetRequiredBlocks().size() == 1)
                bestPlan = possiblePlan;
        }
        return createPlanForSingleAgent(bestPlan);
    }

    private NextGroupPlanForAgent createPlanForSingleAgent(NextGroupPlanTask plan) {
        ArrayList<NextPlan> subPlans = new ArrayList<>();
        HashSet<NextMapTile> requiredBlocks = plan.GetTask().GetRequiredBlocks();
        for (NextMapTile block : requiredBlocks) {
            subPlans.add(new NextPlanDispenser(block));
        }
        subPlans.add(new NextPlanGoalZone());
        return new NextGroupPlanForAgent(plan.GetTask(), subPlans);
    }

    /**
     * Prüft, ob für eine Task noch ein Plan erzeugt werden muss und speichert die neue Task Liste
     */
    public void UpdateTasks(HashSet<NextTask> newTasks) {
        for (NextTask newTask : newTasks) {
            HashSet<String> actualTasks = activePlans.stream().map(activeTask -> activeTask.GetTask().GetName()).collect(Collectors.toCollection(HashSet::new));
            if (!actualTasks.contains(newTask.GetName())) {
                activePlans.add(new NextGroupPlanTask(group, newTask));
            }
        }
        activePlans.forEach(NextGroupPlanTask::UpdateInternalBelief);
        planAgentTasks();
    }

    /**
     * Get the plan for one specific agent
     */
    public NextGroupPlanForAgent GetPlan(NextAgent agent) {
        return currentPlans.get(agent);
    }

    public void SetMaxAttemptsAreReached(NextTask task) {
        for (NextGroupPlanTask plan : activePlans) {
            if (plan.GetTask().GetName().equals(task.GetName()))
                plan.SetMaxAttemptsAreReached();
        }
    }

}
