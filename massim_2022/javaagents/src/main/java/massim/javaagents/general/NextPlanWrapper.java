package massim.javaagents.general;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentStatus;
import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.Vector2D;

public class NextPlanWrapper {

	public static EAgentTask GenerateNewPlan(NextAgent nextAgent)
	{
		NextAgentStatus nextAgentStatus = nextAgent.GetAgentStatus();
		NextMap map = nextAgent.GetMap();

        if(nextAgentStatus.GetAttachedElementsAmount() > 0) // Block available
        {
        	if(map.IsGoalZoneAvailable()) // knowing endzone
        	{
        		return EAgentTask.goToGoalzone;
        	} 
        	else // unknown endzone
        	{
        		return EAgentTask.surveyGoalZone;
        	}
        } 
        else 
        {
        	// TODO: Hier fehlt noch die Verarbeitung, ob ich zu dem Dispenser muss oder ob ich ihn für meinen Task nicht brauche
        	if(map.IsDispenserAvailable())
        	{
        		return EAgentTask.goToDispenser;
        	} 
        	else 
        	{
        		return EAgentTask.surveyDispenser;
        	}
        }
	}
}
