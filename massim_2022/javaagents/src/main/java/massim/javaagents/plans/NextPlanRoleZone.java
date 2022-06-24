package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;

public class NextPlanRoleZone extends NextPlan {
    public NextPlanRoleZone(Vector2D targetPosition) {
        this.agentTask = NextConstants.EAgentTask.goToRolezone;
    }

    /**
     * Erzeugt keine weiteren SubPlans
     */
    @Override
    public void CreateSubPlans() {

    }
}
