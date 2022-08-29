package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.groupPlans.NextAgentPlan;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;

public class NextTaskHandler {

    private NextPlanSolveTask currentPlan;
    private final NextAgent agent;
    private boolean initialTask = false;


    public NextTaskHandler(NextAgent agent) {
        this.agent = agent;
    }

    public void SetInitialTask() {
        if (!initialTask) {
            initialTask = true;
            NextTask task = agent.GetSimulationStatus().GetTasksList().iterator().next();
            ArrayList<NextPlan> subPlans = new ArrayList<>();
            subPlans.add(new NextPlanDispenser(task.GetRequiredBlocks().iterator().next()));
            currentPlan = new NextPlanSolveTask(agent, new NextAgentPlan(task, subPlans));
        }
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     */
    public NextPlan GetDeepestEAgentTask() {
        if (currentPlan != null) return currentPlan.GetDeepestPlan();
        return null;
    }

    /**
     * Get the ThingType of the required block
     *
     * @return thingType is a plan is set, "Empty" else
     */
    public String GetRequiredBlockType() {
        if (currentPlan != null) {
            for (NextPlan subPlan : currentPlan.subPlans) {
                if (subPlan instanceof NextPlanDispenser)
                    return ((NextPlanDispenser) subPlan).GetDispenser().getThingType();
            }
        }
        return "Empty";
    }

    public NextTask GetCurrentTask() {
        if (currentPlan != null) return currentPlan.GetTask();
        return null;
    }

    public void SetAgentPlan(NextAgentPlan groupPlan) {
        if (groupPlan == null) return;
        if (currentPlan == null) {
            currentPlan = new NextPlanSolveTask(agent, groupPlan);
            return;
        }
        if (!groupPlan.GetTask().GetName().equals(currentPlan.GetTask().GetName()))
            currentPlan = new NextPlanSolveTask(agent, groupPlan);
    }

    public void SetReadyToConnect(){
        if (currentPlan != null){
            currentPlan.SetReadyToConnect();
        }
    }

    public void UpdateTasks() {
        if (currentPlan != null) currentPlan.UpdateInternalBelief();
    }
}
