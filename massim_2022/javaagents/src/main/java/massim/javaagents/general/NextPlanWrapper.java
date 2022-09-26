package massim.javaagents.general;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.general.NextConstants.EAgentActivity;
import massim.javaagents.map.NextMap;

public class NextPlanWrapper {

    public static EAgentActivity GenerateNewPlan(NextAgent nextAgent) {
        NextAgentStatus nextAgentStatus = nextAgent.GetAgentStatus();
        NextMap map = nextAgent.GetMap();

        if (nextAgentStatus.GetAttachedElementsAmount() > 0) // Block available
        {
            if (map.IsGoalZoneAvailable()) // knowing endzone
            {
                return EAgentActivity.goToGoalzone;
            } else // unknown endzone
            {
                return EAgentActivity.surveyGoalZone;
            }
        } else {
            // TODO: Hier fehlt noch die Verarbeitung, ob ich zu dem Dispenser muss oder ob ich ihn für meinen Task nicht brauche
            if (map.IsDispenserAvailable()) {
                return EAgentActivity.goToDispenser;
            } else {
                return EAgentActivity.surveyDispenser;
            }
        }
    }
}
