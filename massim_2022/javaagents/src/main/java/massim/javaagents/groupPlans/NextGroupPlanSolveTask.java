package massim.javaagents.groupPlans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.percept.NextTask;


public class NextGroupPlanSolveTask extends NextGroupPlan {

    private NextTask task;
    private int estimatedStepsToSolveTask = 0;

    private boolean isDeadlineFulfillable = true;
    private boolean isPreconditionFulfilled = false;

    private int failOffest = 2;
    private int failStatus = 0;


    public NextGroupPlanSolveTask(NextTask task) {
        this.task = task;
        this.agentTask = NextConstants.EAgentActivity.solveTask;

    }

    /**
     * Berechnet die Anzahl an Schritten, um Task zu erfüllen
     */
    private void estimateStepsToSolveTask() {

    }

    /**
     * Prüft, ob die Vorbedingung erfüllt ist
     *
     * @return true if the precondition is fulfilled
     */
    public boolean IsPreconditionFulfilled() {
        return isPreconditionFulfilled;
    }

    /**
     * prüft, ob die Vorbedingung erfüllt ist
     */
    public void CheckIfPreConditionIsFulfilled() {
    }

    /**
     * Check if the task can be submitted within the deadline
     */
    public boolean IsDeadlineFulfillable() {
        return isDeadlineFulfillable;
    }

    /**
     * prüft, ob Task noch in der zur Verfügung stehenden Zeit gelöst werden kann
     */
    public void CheckIfDeadlineIsReached(int currentStep) {
    }

    public NextTask GetTask() {
        return task;
    }

    /**
     * Erzeugt eine Liste mit subPlans - Hier werden zuerst alle Dispenser abgegangen und dann zur Zielzone
     */
    @Override
    public void CreateSubPlans() {

    }

    public void UpdateInternalBelief() {
    }
}
