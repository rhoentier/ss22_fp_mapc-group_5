package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.groupPlans.NextAgentPlan;
import massim.javaagents.percept.NextTask;

public class NextTaskHandler {

    private NextPlanSolveTask currentPlan;
    private final NextAgent agent;


    public NextTaskHandler(NextAgent agent) {
        this.agent = agent;
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     */
    public NextPlan GetDeepestEAgentTask() {
        if (currentPlan != null) return currentPlan.GetDeepestPlan();
        return null;
    }

    public NextTask GetCurrentTask() {
        if (currentPlan != null) return currentPlan.GetTask();
        return null;
    }

    public void SetAgentPlan(NextAgentPlan groupPlan) {
        if (currentPlan == null) {
            currentPlan = new NextPlanSolveTask(agent, groupPlan);
            return;
        }
        if (!groupPlan.GetTask().GetName().equals(currentPlan.GetTask().GetName()))
            currentPlan = new NextPlanSolveTask(agent, groupPlan);
    }

    public void UpdateTasks() {
        if (currentPlan != null)
            currentPlan.UpdateInternalBelief();
    }
}
