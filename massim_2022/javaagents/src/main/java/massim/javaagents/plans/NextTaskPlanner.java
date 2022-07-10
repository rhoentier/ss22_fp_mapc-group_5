package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

public class NextTaskPlanner {

    private NextPlanSolveTask currentPlan;
    private ArrayList<NextPlanSolveTask> possiblePlans = new ArrayList<>();
    private NextAgent agent;
    private NextPlanRoleZone firstRoleChange;


    public NextTaskPlanner(NextAgent agent) {
        this.agent = agent;
        firstRoleChange = new NextPlanRoleZone(agent);
    }

    /**
     * Erzeugt für eine neue Task einen Plan inklusive subplans
     *
     * @param newTask
     */
    private void createPlanForGivenTask(NextTask newTask) {
        possiblePlans.add(new NextPlanSolveTask(newTask, agent));
    }

    /**
     * Prüft, ob für eine Task noch ein Plan erzeugt werden muss und speichert die neue Task Liste
     *
     * @param newTasks
     */
    public void UpdateTasks(HashSet<NextTask> newTasks) {
        for (NextTask newTask : newTasks) {
            HashSet<String> actualTasks = possiblePlans.stream().map(possiblePlan -> possiblePlan.GetTaskName()).collect(Collectors.toCollection(HashSet::new));
            if (!actualTasks.contains(newTask.GetName())) {
                createPlanForGivenTask(newTask);
            }
        }
        possiblePlans.stream().forEach(possiblePlan -> possiblePlan.UpdateInternalBelief());
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     *
     * @return
     */
    public NextPlan GetDeepestEAgentTask() {
        // gibt den Rollenwechsel zurück, falls dieser noch notwendig ist
        if (!agent.GetAgentStatus().GetRole().equals("worker")) return firstRoleChange.GetDeepestPlan();

        // find a fulfillable plan
        currentPlan = findBestFulfillablePlan();

        // if no plan is fulfillable try to fulfill a plan
        if (currentPlan == null) {
            if (possiblePlans.isEmpty()) return null;
            currentPlan = findBestPlan();
            if (currentPlan == null) return null;
            currentPlan.FulfillPrecondition();
        }
        return currentPlan.GetDeepestPlan();
    }

    /**
     * Find in possiblePlans a fulfillable plan or return null
     *
     * @return
     */
    private NextPlanSolveTask findFulfillablePlan() {
        for (NextPlanSolveTask possiblePlan : possiblePlans) {
            if (possiblePlan.IsPreconditionFulfilled() && possiblePlan.IsDeadlineFulfillable()) return possiblePlan;
        }
        return null;
    }

    /**
     * findet den besten (Nutzen/Kosten) Plan, der bereits ausgeführt werden kann oder null,
     * wenn kein Plan erfüllbar ist
     *
     * @return NextPlanSolveTask, der alle Vorbedingungen erfüllt
     */
    private NextPlanSolveTask findBestFulfillablePlan() {
        NextPlanSolveTask bestPlan = null;
        for (NextPlanSolveTask possiblePlan : possiblePlans) {
            if (possiblePlan.IsPreconditionFulfilled() && possiblePlan.IsDeadlineFulfillable()) {
                if (bestPlan == null || bestPlan.GetUtilization() < possiblePlan.GetUtilization())
                    bestPlan = possiblePlan;
            }
        }
        return bestPlan;
    }

    /**
     * findet den besten (Nutzen/Kosten) Plan, egal, ob dieser erfüllbar ist oder null, falls es keine Task mehr gibt
     *
     * @return NextPlanSolveTask, der alle Vorbedingungen erfüllt
     */
    private NextPlanSolveTask findBestPlan() {
        NextPlanSolveTask bestPlan = null;
        for (NextPlanSolveTask possiblePlan : possiblePlans) {
            if (possiblePlan.IsDeadlineFulfillable())
                if (bestPlan == null || bestPlan.GetUtilization() < possiblePlan.GetUtilization())
                    bestPlan = possiblePlan;
        }
        return bestPlan;
    }

    public NextTask GetCurrentTask() {
        if (currentPlan != null) return currentPlan.GetTask();
        return null;
    }
}
