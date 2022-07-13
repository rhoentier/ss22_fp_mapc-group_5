package massim.javaagents.groupPlans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextGroupPlanDispenser extends NextGroupPlan {
    private NextMapTile dispenser;

    public NextGroupPlanDispenser(NextMapTile dispenser) {
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
