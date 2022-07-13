package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class NextGroupTaskPlanner {

    private HashMap<NextAgent, NextGroupPlanSolveTask> currentPlans = new HashMap<>();
    private NextGroup group;
    private ArrayList<NextGroupPlanTask> activePlans = new ArrayList<>();

    public NextGroupTaskPlanner(NextGroup group) {
        this.group = group;
    }

    /**
     * Erzeugt für alle Agents der Gruppe den passenden Task
     */
    private void planAgentTasks() {
    }

    /**
     * Prüft, ob für eine Task noch ein Plan erzeugt werden muss und speichert die neue Task Liste
     *
     * @param newTasks
     */
    public void UpdateTasks(HashSet<NextTask> newTasks) {
        for (NextTask newTask : newTasks) {
            HashSet<String> actualTasks = activePlans.stream().map(activeTask -> activeTask.GetTask().GetName()).collect(Collectors.toCollection(HashSet::new));
            if (!actualTasks.contains(newTask.GetName())) {
                activePlans.add(new NextGroupPlanTask(group, newTask));
            }
        }
        activePlans.stream().forEach(activePlan -> activePlan.UpdateInternalBelief());
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     *
     * @return
     */
    public HashMap<NextGroupPlan, NextTask> GetDeepestPlan(NextAgent agent) {
        return null;
    }
}
