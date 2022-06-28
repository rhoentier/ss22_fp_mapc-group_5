package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextAgentUtil;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.pathfinding.NextManhattanPath;
import massim.javaagents.percept.NextTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;


public class NextPlanSolveTask extends NextPlan {

    private NextTask task;
    // TODO Hier sollte noch berechnet werden, wie viele Punkte geholt werden könne
    private int maxPossibleProfit = 0;
    // TODO Hier sollte noch berechnet werden, wie viele Punkte pro Schritt erreicht werden können
    private float utilization = 0;
    private int carryableBlocks = 0;
    private boolean isPreconditionFulfilled = false;
    private boolean isDeadlineFulfillable = true;
    private String taskName;


    public NextPlanSolveTask(NextTask task, NextAgent agent) {
        this.agent = agent;
        this.task = task;
        this.carryableBlocks = agent.GetCarryableBlocks();
        this.agentTask = NextConstants.EAgentTask.solveTask;
        this.taskName = task.GetName();
        CheckIfPreConditionIsFulfilled();
        CreateSubPlans();
        calculateProfit();
    }

    /**
     * Berechnet Nutzen/Kosten-Quotient und den maximal erreichbaren Profit
     */
    private void calculateProfit() {
        if (!isPreconditionFulfilled) return;
        int sumOfShortestWays = 0;
        int shortestWayFromDispenserToGoalZone = 0;
        HashSet<NextMapTile> goalZones = agent.GetMap().GetGoalZones();
        HashSet<NextMapTile> dispensers = agent.GetMap().GetDispensers();
        //calculate ways from dispensers to nearestGoalZone
        for (NextMapTile block : task.GetRequiredBlocks()) {
            for (NextMapTile dispenser : dispensers) {
                if (dispenser.getThingType() != block.getThingType()) continue;
                Vector2D nearestGoalZone = NextAgentUtil.GetNearestZone(dispenser.GetPosition(), goalZones);
                int wayFromDispenserToGoalZone = NextManhattanPath.CalculatePath(dispenser.GetPosition(), nearestGoalZone).size();
                if (shortestWayFromDispenserToGoalZone == 0 || wayFromDispenserToGoalZone < shortestWayFromDispenserToGoalZone)
                    shortestWayFromDispenserToGoalZone = wayFromDispenserToGoalZone;
            }
            sumOfShortestWays += shortestWayFromDispenserToGoalZone;
        }
        if (sumOfShortestWays > 0) {
            setMaxPossibleProfit(sumOfShortestWays);
            setUtilization(sumOfShortestWays);
        }
    }

    /**
     * Berechnet den Nutzen/Kosten-Quotienten, der durch diese Aufgabe erreicht wird
     *
     * @param sumOfShortestWays Summe der Wege von Dispensern zu den GoalZones
     */
    private void setUtilization(int sumOfShortestWays) {
        utilization = (float) task.GetReward() / (float) sumOfShortestWays;
    }

    /**
     * Berechnet den maximalen Nutzen, der durch diese Aufgabe noch erreicht werden kann
     *
     * @param sumOfShortestWays Summe der Wege von Dispensern zu den GoalZones
     */
    private void setMaxPossibleProfit(int sumOfShortestWays) {
        int remainingSteps = (int) task.GetDeadline() - agent.getSimulationStatus().GetCurrentStep();
        maxPossibleProfit = (int) task.GetReward() * (remainingSteps / sumOfShortestWays);
    }

    /**
     * Versucht die Karte zu durchsuchen um eine Aufgabe zu lösen
     */
    public void FulfillPrecondition() {
        subPlans.add(0, new NextPlanExploreMap(task.GetRequiredBlocks(), agent));
    }

    /**
     * Check if the precondition is Fulfilled
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
        HashSet<String> requiredBlocks = task.GetRequiredBlocks().stream().map(NextMapTile::getThingType).collect(Collectors.toCollection(HashSet::new));
        if (!agent.GetMap().IsGoalZoneAvailable()) {
            isPreconditionFulfilled = false;
            return;
        }
        isPreconditionFulfilled = agent.GetMap().IsTaskExecutable(requiredBlocks) ? true : false;
    }

    /**
     * Erzeugt je nach Anzahl der zu tragenden Blöcke eine List mit subplans
     */
    @Override
    public void CreateSubPlans() {
        // TODO: Hier später noch implementieren, wenn mehrere Blöcke getragen werden können
        switch (carryableBlocks) {
            default -> {
                HashSet<NextMapTile> requiredBlocks = task.GetRequiredBlocks();
                for (NextMapTile block : requiredBlocks) {
                    subPlans.add(new NextPlanDispenser(block));
                    subPlans.add(new NextPlanGoalZone(block.GetPosition()));
                }
            }
        }
    }

    /**
     * @return Nutzen/Kosten-Quotient
     */
    public float GetUtilization() {
        return utilization;
    }

    public String GetTaskName() {
        return taskName;
    }

    /**
     * @return Maximaler Profit, der durch diese Aufgabe gelöst werden kann
     */
    public int GetMaxPossibleProfit() {
        return maxPossibleProfit;
    }

    public void SetPlanIsFulfilled() {
        for (Iterator<NextPlan> planIterator = subPlans.iterator(); planIterator.hasNext(); ) {
            NextPlan plan = planIterator.next();
            if (plan.IsPlanFulfilled()) continue;
            plan.SetPlanIsFulfilled();
            if (!planIterator.hasNext()) ResetAllPlans();
            return;
        }
        ResetAllPlans();
    }

    /**
     * Prüft, ob die Deadline für die Aufgabe bereits erreicht oder überschritten wurde
     *
     * @return true, falls die Deadline erreicht wurde
     */
    public boolean IsDeadlineReached() {
        if (agent.getSimulationStatus().GetCurrentStep() >= (int) task.GetDeadline()) return true;
        return false;
    }

    public void UpdateInternalBelief(){
        CheckIfPreConditionIsFulfilled();
        if (isPreconditionFulfilled){
            if (subPlans.get(0) instanceof NextPlanExploreMap) {
                subPlans.remove(0);
            }
        }
        if (subPlans.get(0) instanceof NextPlanExploreMap)
            ((NextPlanExploreMap) subPlans.get(0)).CheckPreconditionStatus();
    }
}
