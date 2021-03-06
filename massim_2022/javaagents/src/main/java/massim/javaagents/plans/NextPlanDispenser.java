package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextPlanDispenser extends NextPlan {
    private NextMapTile dispenser;

    public NextPlanDispenser(NextMapTile dispenser) {
        this.agentTask = NextConstants.EAgentActivity.goToDispenser;
        this.dispenser = dispenser;
    }

    /**
     * Erzeugt keine weiteren SubPlans
     */
    @Override
    public void CreateSubPlans() {

    }

    public NextMapTile GetDispenser() {
        return dispenser;
    }
}
