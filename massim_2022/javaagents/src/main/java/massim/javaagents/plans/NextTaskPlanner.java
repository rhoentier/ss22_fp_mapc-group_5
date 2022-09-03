package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class NextTaskPlanner {

    private NextPlanSolveTask currentPlan;
    private final ArrayList<NextPlanSolveTask> possiblePlans = new ArrayList<>();
    private final NextAgent agent;

    public NextTaskPlanner(NextAgent agent) {
        this.agent = agent;
    }

    /**
     * Erzeugt für einen neuen Task einen Plan inklusive subplans
     *
     * @param newTask Die neue Aufgabe
     */
    private void createPlanForGivenTask(NextTask newTask) {
        possiblePlans.add(new NextPlanSolveTask(newTask, agent));
    }

    /**
     * Prüft, ob für einen Task noch ein Plan erzeugt werden muss und speichert die neue Taskliste
     *
     * @param newTasks Neue Tasks, die vom Server übermittelt wurden
     */
    public void UpdateTasks(HashSet<NextTask> newTasks) {
        for (NextTask newTask : newTasks) {
            HashSet<String> actualTasks = possiblePlans.stream().map(NextPlanSolveTask::GetTaskName).collect(Collectors.toCollection(HashSet::new));
            if (!actualTasks.contains(newTask.GetName())) {
                createPlanForGivenTask(newTask);
            }
        }
        possiblePlans.forEach(NextPlanSolveTask::UpdateInternalBelief);
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     *
     * @return Aktueller Plan, der ausgeführt werden soll
     */
    public NextPlan GetDeepestEAgentTask() {
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
     * findet den besten (Nutzen/Kosten) Plan, egal, ob dieser erfüllbar ist oder null, falls es kein Task mehr gibt
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
