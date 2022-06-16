package massim.javaagents.tasks;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextTask;

import java.util.ArrayList;

public class NextPlan {

    private NextConstants.EAgentTask desire;
    private String target;
    private Vector2D position;
    private ArrayList<NextPlan> subPlans = new ArrayList<NextPlan>();
    private boolean isPreconditionFulfilled = false;
    private NextTask originalTask;
    private float maxBenefit;
    private NextAgent agent;

    /**
     * Create NextPlan without a specific target
     * @param desire
     */
    public NextPlan(NextConstants.EAgentTask desire, NextAgent agent, NextTask task){
        this.desire = desire;
        this.agent = agent;
        this.originalTask = task;

        maxBenefit = desire == NextConstants.EAgentTask.solveTask ? calculateMaxBenefit() : 0;
    }

    /**
     * Create NextPlan with a specific target (like b1)
     * @param desire
     * @param target
     */
    public NextPlan(NextConstants.EAgentTask desire, String target, NextAgent agent, NextTask task){
        this.desire = desire;
        this.target = target;
        this.agent = agent;
        this.originalTask = task;

        maxBenefit = desire == NextConstants.EAgentTask.solveTask ? calculateMaxBenefit() : 0;
    }

    public void AddSubPlan(NextPlan subPlan){
        subPlans.add(subPlan);
    }

    /**
     * Set Position to
     * @param position
     */
    public void SetPosition(Vector2D position){
        this.position = position;
    }

    public void UpdatePrecondition(boolean preconditionFulfillment){
        this.isPreconditionFulfilled = preconditionFulfillment;
    }

    public float GetMaxBenefit() {
        return maxBenefit;
    }

    /**
     * Get the desire of the deepest plan in the tree of plans
     * @return
     */
    public NextConstants.EAgentTask GetDeepestDesire() {
        if (!subPlans.isEmpty()) return subPlans.get(0).GetDeepestDesire();
        return desire;
    }

    /**
     * Get the desire of the called plan
     * @return
     */
    public NextConstants.EAgentTask GetOwnDesire() {
        return desire;
    }

    // TODO : Als benefit soll gespeichert werden, wie viele Einnahmen ich maximal erzielen kann
    private long calculateMaxBenefit(){
        return 0;
    }

    public boolean IsPreconditionFulfilled() {
        return isPreconditionFulfilled;
    }
}
