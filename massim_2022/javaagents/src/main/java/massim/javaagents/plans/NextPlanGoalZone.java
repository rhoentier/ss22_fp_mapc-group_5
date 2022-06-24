package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;

public class NextPlanGoalZone extends NextPlan {

    private Vector2D targetPosition;

    public NextPlanGoalZone(Vector2D targetPosition) {
        this.agentTask = NextConstants.EAgentTask.goToGoalzone;
        this.targetPosition = targetPosition;
    }


    /**
     * Ereugt keine weiteren Subplans
     */
    @Override
    public void CreateSubPlans() {

    }

    private Vector2D GetTargetPosition() {
        return targetPosition;
    }
}
