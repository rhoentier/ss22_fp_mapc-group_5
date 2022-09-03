package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;

import java.util.ArrayList;
import java.util.HashSet;


public class NextAgentPlan {

    private final NextTask task;
    ArrayList<NextPlan> subPlans;

    public NextAgentPlan(NextTask task, ArrayList<NextPlan> subPlans) {
        this.task = task;
        this.subPlans = subPlans;
    }
    public NextTask GetTask() {
        return task;
    }

    public ArrayList<NextPlan> GetSubPlans() {
        return subPlans;
    }
}
