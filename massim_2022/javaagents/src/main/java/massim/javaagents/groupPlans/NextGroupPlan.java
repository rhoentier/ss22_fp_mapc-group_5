package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.general.NextConstants;
import massim.javaagents.plans.NextPlan;

import java.util.ArrayList;

public abstract class NextGroupPlan {

    NextGroup group;
    ArrayList<NextPlan> subPlans = new ArrayList<>();
    boolean isPlanFulfilled = false;
    NextConstants.EAgentActivity agentTask;

    /**
     * Check if the plan is already fulfilled
     *
     * @return true if the plan is already fulfilled
     */
    public boolean IsPlanFulfilled() {
        return isPlanFulfilled;
    }

    /**
     * set if a plan is fulfilled
     */
    public void SetPlanIsFulfilled(boolean status) {
        this.isPlanFulfilled = status;
    }

     /**
     * DeepFirstSearch to get the current plan to execute
     *
     * @return plan to execute
     */
    public NextPlan GetDeepestPlan() {
        for (NextPlan plan : subPlans) {
            if (plan.IsPlanFulfilled()) continue;
            return plan.GetDeepestPlan();
        }
        return null;
    }

    /**
     * Set plan and sub plans to not fulfilled
     */
    public void ResetAllPlans() {
        for (NextPlan plan : subPlans) {
            plan.ResetAllPlans();
        }
        isPlanFulfilled = false;
    }

    /**
     * Get the EAgentTask of this type of plan
     *
     * @return EAgentTask
     */
    public NextConstants.EAgentActivity GetAgentTask() {
        return agentTask;
    }
}
