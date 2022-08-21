package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.map.NextMapTile;
import massim.javaagents.map.Vector2D;
import massim.javaagents.percept.NextTask;
import massim.javaagents.plans.NextPlan;
import massim.javaagents.plans.NextPlanConnect;
import massim.javaagents.plans.NextPlanDispenser;
import massim.javaagents.plans.NextPlanGoalZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class NextTaskPlanner {

    private final ArrayList<NextAgent> agents = new ArrayList<>();
    private final HashMap<NextAgent, NextAgentPlan> currentPlans = new HashMap<>();
    private final NextGroup group;
    private final HashSet<NextGroupPlan> activePlans = new HashSet<>();

    public NextTaskPlanner(NextGroup group) {
        this.group = group;
    }

    /**
     * Erzeugt für alle Agents der Gruppe den passenden Task
     */
    private void planAgentTasks() {
        int size = agents.size();
        if (size < 4) getBestPlanForAgents(agents);
        else {
            int groupsOfFour = size / 4;
            for (int i = 0; i < groupsOfFour; i++) {
                getBestPlanForAgents(new ArrayList<>(agents.subList((4 * i), 4 + (4 * i))));
            }
            int mod = size % 4;
//            System.out.println("mod " + mod + " -- groups of four " + groupsOfFour);
            switch (mod) {
                case 1 -> getBestPlanForAgents(new ArrayList<>(agents.subList((4 * groupsOfFour), 1 + (4 * groupsOfFour))));
                case 2 -> getBestPlanForAgents(new ArrayList<>(agents.subList((4 * groupsOfFour), 2 + (4 * groupsOfFour))));
                case 3 -> getBestPlanForAgents(new ArrayList<>(agents.subList((4 * groupsOfFour), 3 + (4 * groupsOfFour))));
            }
        }
    }

    private void getBestPlanForAgents(ArrayList<NextAgent> agents) {
        boolean foundPlanForAll = false;
        NextGroupPlan bestPlan = null;
        for (NextGroupPlan possiblePlan : activePlans) {
            if (possiblePlan.IsPreconditionFulfilled() && possiblePlan.IsDeadlineFulfillable() && possiblePlan.GetTask().GetRequiredBlocks().size() == agents.size()) {
                if (bestPlan == null || bestPlan.GetUtilization() <= possiblePlan.GetUtilization()) {
                    foundPlanForAll = true;
                    bestPlan = possiblePlan;
                }
            }
        }

        // Es wurde ein Task mit der richtigen Anzahl an Blöcken gefunden
        if (foundPlanForAll) {
            switch (agents.size()) {
                case 1 -> createPlanForSingleAgent(bestPlan, agents);
                case 2 -> createPlanForTwoAgents(bestPlan, agents);
                case 3 -> createPlanForThreeAgents(bestPlan, agents);
                case 4 -> createPlanForFourAgents(bestPlan, agents);
            }
            return;
        }

        // Keine Tasks mit Blöcken in der richtigen Anzahl versuche Tasks mit weniger Blöcken abzuarbeiten
        switch (agents.size()) {
            case 1:
                for (NextGroupPlan plan : activePlans) {
                    // Fallback, falls es keine Tasks gibt, für die alle Vorbedingungen erfüllt sind
                    if (plan.IsDeadlineFulfillable() && plan.GetTask().GetRequiredBlocks().size() == 1) {
                        createPlanForSingleAgent(plan, agents);
                        break;
                    }
                }
                break;
            case 2:
                getBestPlanForAgents(new ArrayList<>(agents.subList(0, 1)));
                getBestPlanForAgents(new ArrayList<>(agents.subList(1, 2)));
                break;
            case 3:
                getBestPlanForAgents(new ArrayList<>(agents.subList(0, 2)));
                getBestPlanForAgents(new ArrayList<>(agents.subList(2, 3)));
                break;
            case 4:
                getBestPlanForAgents(new ArrayList<>(agents.subList(0, 2)));
                getBestPlanForAgents(new ArrayList<>(agents.subList(2, 4)));
                break;
        }
    }

    private void createPlanForSingleAgent(NextGroupPlan plan, ArrayList<NextAgent> agent) {
        ArrayList<NextPlan> subPlans = new ArrayList<>();
        HashSet<NextMapTile> requiredBlocks = plan.GetTask().GetRequiredBlocks();
        for (NextMapTile block : requiredBlocks) {
            subPlans.add(new NextPlanDispenser(block));
        }
        subPlans.add(new NextPlanGoalZone());
        currentPlans.put(agent.get(0), new NextAgentPlan(plan.GetTask(), subPlans));
    }

    // TODO: Kann bestimmt verallgemeinert werden
    private void createPlanForTwoAgents(NextGroupPlan plan, ArrayList<NextAgent> agents) {
        // TODO: Test, ob das hier klappt
        Vector2D topBlock = new Vector2D(0, 1);

        HashSet<NextMapTile> requiredBlocks = plan.GetTask().GetRequiredBlocks();
        HashMap<NextAgent, HashMap<NextMapTile, Integer>> stepsToDispenser = new HashMap<>();

        // Count estimated steps for every agent to the dispenser
        for (NextAgent agent : agents) {
            stepsToDispenser.put(agent, agent.GetDispenserDistances(requiredBlocks));
        }

        // Reformat for easier access
        ArrayList<NextMapTile> blockArray = new ArrayList<>(requiredBlocks.stream().toList());
        ArrayList<NextAgent> agentArray = new ArrayList<>(agents.stream().toList());

        int[] estimatedStepsInPartition = {0, 0};

        estimatedStepsInPartition[0] = estimatedStepsInPartition[0] + stepsToDispenser.get(agentArray.get(0)).get(blockArray.get(0));
        estimatedStepsInPartition[1] = estimatedStepsInPartition[1] + stepsToDispenser.get(agentArray.get(0)).get(blockArray.get(1));

        estimatedStepsInPartition[0] = estimatedStepsInPartition[0] + stepsToDispenser.get(agentArray.get(1)).get(blockArray.get(1));
        estimatedStepsInPartition[1] = estimatedStepsInPartition[1] + stepsToDispenser.get(agentArray.get(1)).get(blockArray.get(0));

        ArrayList<NextPlan> subPlans = new ArrayList<>();
        boolean main = blockArray.get(0).GetPosition().equals(topBlock);
        HashSet<NextAgent> involvedAgents;

        if (estimatedStepsInPartition[0] <= estimatedStepsInPartition[1]) {
            subPlans.add(new NextPlanDispenser(blockArray.get(0)));
            subPlans.add(new NextPlanGoalZone());

            involvedAgents = new HashSet<>();
            involvedAgents.add(agentArray.get(1));
            subPlans.add(new NextPlanConnect(main, blockArray.get(0).GetPosition(), involvedAgents));

            currentPlans.put(agentArray.get(0), new NextAgentPlan(plan.GetTask(), subPlans));

            subPlans = new ArrayList<>();
            subPlans.add(new NextPlanDispenser(blockArray.get(1)));
            subPlans.add(new NextPlanGoalZone());

            involvedAgents = new HashSet<>();
            involvedAgents.add(agentArray.get(0));
            subPlans.add(new NextPlanConnect(!main, blockArray.get(1).GetPosition(), involvedAgents));
        } else {
            subPlans.add(new NextPlanDispenser(blockArray.get(1)));
            subPlans.add(new NextPlanGoalZone());

            involvedAgents = new HashSet<>();
            involvedAgents.add(agentArray.get(1));
            subPlans.add(new NextPlanConnect(!main, blockArray.get(1).GetPosition(), involvedAgents));

            currentPlans.put(agentArray.get(0), new NextAgentPlan(plan.GetTask(), subPlans));

            subPlans = new ArrayList<>();
            subPlans.add(new NextPlanDispenser(blockArray.get(0)));
            subPlans.add(new NextPlanGoalZone());

            involvedAgents = new HashSet<>();
            involvedAgents.add(agentArray.get(0));
            subPlans.add(new NextPlanConnect(main, blockArray.get(0).GetPosition(), involvedAgents));
        }
        currentPlans.put(agentArray.get(1), new NextAgentPlan(plan.GetTask(), subPlans));
    }

    // TODO: Für 3 Tasks aktivieren
    private void createPlanForThreeAgents(NextGroupPlan plan, ArrayList<NextAgent> agents) {
        getBestPlanForAgents(new ArrayList<>(agents.subList(0, 2)));
        getBestPlanForAgents(new ArrayList<>(agents.subList(2, 3)));
    }

    // TODO Für 4 Tasks aktivieren
    private void createPlanForFourAgents(NextGroupPlan plan, ArrayList<NextAgent> agents) {
        getBestPlanForAgents(new ArrayList<>(agents.subList(0, 2)));
        getBestPlanForAgents(new ArrayList<>(agents.subList(2, 4)));
    }

    /**
     * Prüft, ob für einen Task noch ein Plan erzeugt werden muss und speichert die neue Taskliste
     */
    public void UpdateTasksAndAgents(HashSet<NextTask> newTasks) {
        for (NextTask newTask : newTasks) {
            HashSet<String> actualTasks = activePlans.stream().map(activeTask -> activeTask.GetTask().GetName()).collect(Collectors.toCollection(HashSet::new));
            if (!actualTasks.contains(newTask.GetName())) {
                activePlans.add(new NextGroupPlan(group, newTask));
            }
        }
        activePlans.forEach(NextGroupPlan::UpdateInternalBelief);

        for (NextAgent agent : group.GetAgents()) {
            if (!agents.contains(agent)) agents.add(agent);
        }
        planAgentTasks();
    }

    /**
     * Get the plan for one specific agent
     */
    public NextAgentPlan GetPlan(NextAgent agent) {
        return currentPlans.get(agent);
    }

    public void SetMaxAttemptsAreReached(NextTask task) {
        for (NextGroupPlan plan : activePlans) {
            if (plan.GetTask().GetName().equals(task.GetName())) plan.SetMaxAttemptsAreReached();
        }
    }
}
