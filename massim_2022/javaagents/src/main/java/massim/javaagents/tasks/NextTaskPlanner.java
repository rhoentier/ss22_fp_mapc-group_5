package massim.javaagents.tasks;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;

public class NextTaskPlanner {

    private ArrayList<NextTask> currentTasks = new ArrayList<>();
    private NextPlan currentPlan;
    private ArrayList<NextPlan> possiblePlans = new ArrayList<>();
    private NextAgent agent;


    public NextTaskPlanner(NextAgent agent) {
        this.agent = agent;
    }

    public void CreatePlanForGivenTask(NextTask newTask) {
        NextPlan newPlan = new NextPlan(NextConstants.EAgentTask.solveTask, agent, newTask);
        for (NextMapTile requiredBlock : newTask.GetRequiredBlocks()) {
            NextPlan subPlan = new NextPlan(NextConstants.EAgentTask.carryBlog, requiredBlock.getThingType(), agent, newTask);
            subPlan.SetPosition(requiredBlock.getPosition());
            subPlan.AddSubPlan(new NextPlan(NextConstants.EAgentTask.goToDispenser, requiredBlock.getThingType(), agent, newTask));
            subPlan.AddSubPlan(new NextPlan(NextConstants.EAgentTask.goToGoalzone, requiredBlock.getThingType(), agent, newTask));
            newPlan.AddSubPlan(subPlan);
        }
        newPlan.UpdatePrecondition(agent.GetMap().IsTaskExecutable(newTask.GetRequiredBlocks()));
        possiblePlans.add(newPlan);
    }

    /**
     * Check if a Task is fulfillable and returns the deepest desire of the task with the max benefit
     * if no task is fulfillable returns the desire to explore the map
     *
     * @return
     */
    public NextConstants.EAgentTask GetDeepestDesire() {
        if (currentPlan == null) {
            if (possiblePlans.isEmpty()) return NextConstants.EAgentTask.exploreMap;
            else currentPlan = findFulfillablePlan();
        } else {
            if (!currentPlan.IsPreconditionFulfilled()) {
                possiblePlans.add(currentPlan);
                currentPlan = findFulfillablePlan();
            }
        }
        if (currentPlan == null) return NextConstants.EAgentTask.exploreMap;

        for (NextPlan possiblePlan : possiblePlans) {
            if (possiblePlan.GetOwnDesire() == NextConstants.EAgentTask.solveTask)
                currentPlan = currentPlan.GetMaxBenefit() < possiblePlan.GetMaxBenefit() && possiblePlan.IsPreconditionFulfilled() ? possiblePlan : currentPlan;
        }
        return currentPlan.GetDeepestDesire();
    }

    /**
     * Find in possiblePlans a fulfillable plan or return null
     *
     * @return
     */
    private NextPlan findFulfillablePlan() {
        for (NextPlan possiblePlan : possiblePlans) {
            if (possiblePlan.IsPreconditionFulfilled()) return possiblePlan;
        }
        return null;
    }
}
