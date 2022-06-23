package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;

import java.util.ArrayList;

public abstract class NextPlan {
    boolean isPreconditionFulfilled = false;
    ArrayList<NextPlan> subPlans = new ArrayList<>();
    boolean isTaskFulfilled = false;
    NextConstants.EAgentTask agentTask;
    NextAgent agent;

    public boolean IsPreconditionFulfilled() {
        return isPreconditionFulfilled;
    }

    public boolean IsTaskFulfilled() {
        return isTaskFulfilled;
    }

    public abstract void CreateSubPlans();

    public NextPlan GetDeepestPlan(){
        if (subPlans.isEmpty()) return this;
        return subPlans.get(0).GetDeepestPlan();
    }
}
