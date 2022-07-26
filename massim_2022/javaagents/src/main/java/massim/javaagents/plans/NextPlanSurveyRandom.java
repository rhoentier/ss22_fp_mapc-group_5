package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

public class NextPlanSurveyRandom extends NextPlan {

    public NextPlanSurveyRandom() {
        this.agentTask = NextConstants.EAgentActivity.surveyRandom;
    }

    /**
     * Erzeugt momentan keine weiteren Subplans
     */
    @Override
    public void CreateSubPlans() {

    }
}
