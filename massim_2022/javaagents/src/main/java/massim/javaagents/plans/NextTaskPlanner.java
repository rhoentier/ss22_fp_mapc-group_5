package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;

import java.util.ArrayList;

public class NextTaskPlanner {

    private ArrayList<NextTask> currentTasks = new ArrayList<>();
    private NextPlanSolveTask currentPlan;
    private ArrayList<NextPlanSolveTask> possiblePlans = new ArrayList<>();
    private NextAgent agent;


    public NextTaskPlanner(NextAgent agent) {
        this.agent = agent;
    }

    public void CreatePlanForGivenTask(NextTask newTask) {
        possiblePlans.add(new NextPlanSolveTask(newTask, agent));
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     *
     * @return
     */
    public NextPlan GetDeepestEAgentTask() {
        // find a fulfillable plan
        // TODO Hier sollte der beste, erfüllbare Plan ausgesucht werden
        currentPlan = currentPlan != null ? currentPlan : findFulfillablePlan();

        // if no plan is fulfillable try to fulfill a plan
        if(currentPlan == null) {
            if (possiblePlans.isEmpty()) return null;
            // TODO Hier sollte der beste Plan ausgesucht werden
            currentPlan = possiblePlans.get(0);
            currentPlan.FulfillPrecondition();
            return currentPlan;
        }
        return currentPlan.GetDeepestPlan();
    }

    /**
     * Find in possiblePlans a fulfillable plan or return null
     *
     * @return
     */
    private NextPlanSolveTask findFulfillablePlan() {
        for (NextPlanSolveTask possiblePlan : possiblePlans) {
            if (possiblePlan.IsPreconditionFulfilled()) return possiblePlan;
        }
        return null;
    }

    private NextPlanSolveTask findBestFulfillablePlan(){
        return null;
    }

    private NextPlanSolveTask findBestPlan(){
        return null;
    }
}
