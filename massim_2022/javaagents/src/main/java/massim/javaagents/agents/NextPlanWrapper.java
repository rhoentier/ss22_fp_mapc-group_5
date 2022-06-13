package massim.javaagents.agents;

import java.util.ArrayList;

import massim.javaagents.general.NextConstants.EAgentTask;
import massim.javaagents.percept.NextTask;

public class NextPlanWrapper {

	public static EAgentTask GenerateNewPlan(NextAgent nextAgent)
	{
		NextAgentStatus nextAgentStatus = nextAgent.getAgentStatus();
		NextSimulationStatus nextSimulationStatus = nextAgent.getSimulationStatus();
		EAgentTask oldTask = nextAgent.GetAgentTask();
		
        if(nextAgentStatus.GetAttachedElementsAmount() > 0) // Block available
        {
        	if(!nextAgent.GetMap().GetMapTiles("goalZone", nextAgent.GetPosition()).isEmpty()) // knowing endzone
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
        	if(!nextAgent.GetMap().GetMapTiles("dispenser", nextAgent.GetPosition()).isEmpty())
        	{
        		return EAgentTask.goToDispenser;
        	} 
        	else 
        	{
        		return EAgentTask.exploreMap;
        	}
        }
	}
}
