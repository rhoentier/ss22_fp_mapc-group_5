package massim.javaagents.groupPlans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextGroupPlanSurveyDispenser extends NextGroupPlan {

    private NextMapTile wantedMapTile;

    public NextGroupPlanSurveyDispenser(NextMapTile wantedMapTile) {
        this.agentTask = NextConstants.EAgentActivity.surveyDispenser;
        this.wantedMapTile = wantedMapTile;
    }

    /**
     * Erzeugt momentan keine weiteren Subplans
     */
    @Override
    public void CreateSubPlans() {

    }

    public NextMapTile GetWantedMapTile() {
        return wantedMapTile;
    }
}
