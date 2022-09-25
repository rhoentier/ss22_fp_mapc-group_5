package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

public class NextPlanExploreMap extends NextPlan {

    private final HashSet<NextMapTile> wantedMapTiles;

    public NextPlanExploreMap(HashSet<NextMapTile> wantedMapTiles, NextAgent agent) {
        this.agent = agent;
        this.agentTask = NextConstants.EAgentActivity.exploreMap;
        this.wantedMapTiles = wantedMapTiles;
        CreateSubPlans();
    }

    public void CreateSubPlans() {
        if (agent.GetMap().GetRoleZones().isEmpty()) subPlans.add(new NextPlanSurveyRandom());
        if (!agent.GetAgentStatus().GetCurrentRole().GetAction().contains("survey"))
            subPlans.add(new NextPlanRoleZone(agent, "explorer"));
        if (agent.GetMap().GetGoalZones().isEmpty()) subPlans.add(new NextPlanSurveyGoalZone());
        for (NextMapTile wantedMapTile : wantedMapTiles) {
            String blockType = wantedMapTile.GetThingType();
            HashSet<String> foundDispenser = agent.GetMap().GetDispensers().stream().map(NextMapTile::GetThingType).collect(Collectors.toCollection(HashSet::new));
            if (!foundDispenser.contains(blockType)) subPlans.add(new NextPlanSurveyDispenser(wantedMapTile));
        }
    }

    public void CheckPreconditionStatus() {
        for (Iterator<NextPlan> subPlanIterator = subPlans.iterator(); subPlanIterator.hasNext(); ) {
            NextPlan subPlan = subPlanIterator.next();
            if (subPlan instanceof NextPlanSurveyRandom) {
                if (!agent.GetMap().GetRoleZones().isEmpty()) {
                    subPlanIterator.remove();
                }
                continue;
            }
            if (subPlan instanceof NextPlanRoleZone) {
                if (agent.GetAgentStatus().GetCurrentRole().GetAction().contains("survey")) {
                    subPlanIterator.remove();
                }
                continue;
            }
            if (subPlan instanceof NextPlanSurveyGoalZone) {
                if (!agent.GetMap().GetGoalZones().isEmpty()) subPlanIterator.remove();
                continue;
            }
            if (subPlan instanceof NextPlanSurveyDispenser) {
                String blockType = ((NextPlanSurveyDispenser) subPlan).GetWantedMapTile().GetThingType();
                HashSet<String> foundDispenser = agent.GetMap().GetDispensers().stream().map(NextMapTile::GetThingType).collect(Collectors.toCollection(HashSet::new));
                if (foundDispenser.contains(blockType)) subPlanIterator.remove();
            }
        }
    }
}
