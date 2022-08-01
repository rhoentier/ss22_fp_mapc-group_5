package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;

import java.util.HashSet;

public class NextPlanConnect extends NextPlan{

    private final boolean mainAgent;
    private final Vector2D position;
    HashSet<NextAgent> involvedAgents = new HashSet<>();

    public NextPlanConnect(boolean mainAgent, Vector2D position, HashSet<NextAgent> involvedAgents){
        this.mainAgent = mainAgent;
        this.position = position;
        this.agentTask = NextConstants.EAgentActivity.connectToAgent;
        this.involvedAgents = involvedAgents;
    }

    public HashSet<NextAgent> GetInvolvedAgents(){
        return involvedAgents;
    }

    public boolean IsAgentMain(){
        return mainAgent;
    }

    public Vector2D GetTargetBlockPosition(){
        return position;
    }
}
