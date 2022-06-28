package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextPlanSurveyDispenser extends NextPlan {

    private NextMapTile wantedMapTile;

    public NextPlanSurveyDispenser(NextMapTile wantedMapTile) {
        this.agentTask = NextConstants.EAgentTask.surveyDispenser;
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
