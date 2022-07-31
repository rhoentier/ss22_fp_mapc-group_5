package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;

public class NextPlanGoalZone extends NextPlan {

    public NextPlanGoalZone() {
        this.agentTask = NextConstants.EAgentActivity.goToGoalzone;
    }
}
