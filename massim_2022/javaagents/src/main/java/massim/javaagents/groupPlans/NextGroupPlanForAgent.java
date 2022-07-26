package massim.javaagents.groupPlans;

import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;

import java.util.ArrayList;


public class NextGroupPlanForAgent {

    private final NextTask task;
    ArrayList<NextPlan> subPlans;

    public NextGroupPlanForAgent(NextTask task, ArrayList<NextPlan> subPlans) {
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
