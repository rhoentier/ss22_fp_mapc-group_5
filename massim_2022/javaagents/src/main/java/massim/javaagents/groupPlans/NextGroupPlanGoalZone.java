package massim.javaagents.groupPlans;

import massim.javaagents.general.NextConstants;

public class NextGroupPlanGoalZone extends NextGroupPlan {

    public NextGroupPlanGoalZone() {
        this.agentTask = NextConstants.EAgentActivity.goToGoalzone;
    }

    /**
     * Ereugt keine weiteren Subplans
     */
    @Override
    public void CreateSubPlans() {

    }
}
