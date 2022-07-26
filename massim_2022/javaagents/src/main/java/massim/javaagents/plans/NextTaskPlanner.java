package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.groupPlans.NextGroupPlanForAgent;
import massim.javaagents.percept.NextTask;

public class NextTaskPlanner {

    private NextPlanSolveTask currentPlan;
    private final NextAgent agent;
    private final NextPlanRoleZone firstRoleChange;


    public NextTaskPlanner(NextAgent agent) {
        this.agent = agent;
        firstRoleChange = new NextPlanRoleZone(agent);
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     *
     */
    public NextPlan GetDeepestEAgentTask() {
        // gibt den Rollenwechsel zurück, falls dieser noch notwendig ist
        if (!agent.GetAgentStatus().GetRole().equals("worker")) return firstRoleChange.GetDeepestPlan();
        if (currentPlan != null) return currentPlan.GetDeepestPlan();
        return null;
    }

    public NextTask GetCurrentTask() {
        if (currentPlan != null) return currentPlan.GetTask();
        return null;
    }

    public void SetGroupPlan(NextGroupPlanForAgent groupPlan) {
        currentPlan = new NextPlanSolveTask(agent, groupPlan);
    }

    public void UpdateTasks() {
        if (currentPlan != null)
            currentPlan.UpdateInternalBelief();
    }
}
