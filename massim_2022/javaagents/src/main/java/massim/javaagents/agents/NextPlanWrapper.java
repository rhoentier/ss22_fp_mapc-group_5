package massim.javaagents.agents;

import java.util.ArrayList;

import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.percept.NextTask;

public class NextPlanWrapper {

	public static EAgentTask GenerateNewPlan(NextAgent nextAgent)
	{
		NextAgentStatus nextAgentStatus = nextAgent.getStatus();
		NextSimulationStatus nextSimulationStatus = nextAgent.getSimulationStatus();
		EAgentTask oldTask = nextAgent.GetAgentTask();
		
        if(nextAgentStatus.GetAttachedElementsAmount() > 0) // Block available
        {
        	// TODO miri GetGoalZones 
        	if(!nextAgentStatus.GetMap().GetMapTiles("goalZone", nextAgentStatus.GetPosition()).isEmpty()) // knowing endzone
        	{
        		return EAgentTask.goToEndzone;
        	} 
        	else // unknown endzone
        	{
        		return EAgentTask.exploreMap;
        	}
        } 
        else 
        {
        	// Kenn ich einen Dispenser vom aktuellen Tasktyp
        	/// TODO miri GetDispenserFromType(type)
        	if(!nextAgentStatus.GetMap().GetMapTiles("dispenser", nextAgentStatus.GetPosition()).isEmpty())
        	{
        		return EAgentTask.goToDispenser;
        	} 
        	else 
        	{
        		return EAgentTask.exploreMap;
        	}
        }
	}
	
	private void resetAfterInactiveTask(NextAgent nextAgent)
	{
		nextAgent.SetActiveTask(null);
		
	}
}
