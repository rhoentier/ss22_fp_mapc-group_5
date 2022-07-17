package massim.javaagents.groupPlans;

import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;

import java.util.ArrayList;


public class NextGroupPlanSolveTask{

    private final NextTask task;
    ArrayList<NextPlan> subPlans;

    public NextGroupPlanSolveTask(NextTask task, ArrayList<NextPlan> subPlans) {
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
