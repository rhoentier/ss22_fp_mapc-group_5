package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;

public class NextPlanRoleZone extends NextPlan{
    private Vector2D targetPosition;
    NextConstants.EAgentTask agentTask = NextConstants.EAgentTask.goToRolezone;

    public NextPlanRoleZone(Vector2D targetPosition){
        this.targetPosition = targetPosition;
    }

    @Override
    public void CreateSubPlans() {

    }
}
