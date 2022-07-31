package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextPlanSurveyDispenser extends NextPlan {

    private NextMapTile wantedMapTile;

    public NextPlanSurveyDispenser(NextMapTile wantedMapTile) {
        this.agentTask = NextConstants.EAgentActivity.surveyDispenser;
        this.wantedMapTile = wantedMapTile;
    }

    public NextMapTile GetWantedMapTile() {
        return wantedMapTile;
    }
}
