package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;

public class NextPlanConnect extends NextPlan{

    private final boolean mainAgent;
    private final Vector2D position;

    public NextPlanConnect(boolean mainAgent, Vector2D position){
        this.mainAgent = mainAgent;
        this.position = position;
        this.agentTask = NextConstants.EAgentActivity.connectToAgent;
    }

    public boolean IsAgentMain(){
        return mainAgent;
    }

    public Vector2D GetTargetBlockPosition(){
        return position;
    }
}
