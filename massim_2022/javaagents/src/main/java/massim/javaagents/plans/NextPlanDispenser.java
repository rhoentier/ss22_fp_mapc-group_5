package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextPlanDispenser extends NextPlan{
    private NextMapTile dispenser;
    NextConstants.EAgentTask agentTask = NextConstants.EAgentTask.goToDispenser;

    public NextPlanDispenser(NextMapTile dispenser){
        this.dispenser = dispenser;
    }

    @Override
    public void CreateSubPlans() {

    }
}
