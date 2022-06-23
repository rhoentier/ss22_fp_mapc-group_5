package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.percept.NextTask;

import java.util.HashSet;

public class NextPlanSolveTask extends NextPlan {

    private NextTask task;
    // TODO Hier sollte noch berechnet werden, wie viele Punkte geholt werden könne
    private int maxPossibleProfit;
    // TODO Hier sollte noch berechnet werden, wie viele Punkte pro Schritt erreicht werden können
    private float utilization;
    private int carryableBlocks;
    NextConstants.EAgentTask agentTask = NextConstants.EAgentTask.solveTask;

    public NextPlanSolveTask(NextTask task, NextAgent agent) {
        this.agent = agent;
        this.task = task;
        this.carryableBlocks = agent.GetCarryableBlocks();
        CreateSubPlans();
    }

    public void FulfillPrecondition(){
        subPlans.add(0, new NextPlanExploreMap(task.GetRequiredBlocks()));
    }

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
}
