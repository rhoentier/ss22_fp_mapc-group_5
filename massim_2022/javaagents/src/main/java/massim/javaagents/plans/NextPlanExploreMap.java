package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

public class NextPlanExploreMap extends NextPlan {

    private HashSet<NextMapTile> wantedMapTiles;

    public NextPlanExploreMap(HashSet<NextMapTile> wantedMapTiles, NextAgent agent) {
        this.agent = agent;
        this.agentTask = NextConstants.EAgentTask.exploreMap;
        this.wantedMapTiles = wantedMapTiles;
        CreateSubPlans();
    }

    @Override
    public void CreateSubPlans() {
        if (agent.GetMap().GetGoalZones().isEmpty()) subPlans.add(new NextPlanSurveyGoalZone());
        for (NextMapTile wantedMapTile : wantedMapTiles) {
            String blockType = wantedMapTile.getThingType();
            HashSet<String> foundDispenser = agent.GetMap().GetDispensers().stream().map(mapTile -> mapTile.getThingType()).collect(Collectors.toCollection(HashSet::new));
            if (!foundDispenser.contains(blockType))
                subPlans.add(new NextPlanSurveyDispenser(wantedMapTile));
        }
    }

    public HashSet<NextMapTile> GetWantedMapTiles() {
        return wantedMapTiles;
    }

    public void CheckPreconditionStatus() {
        for(Iterator<NextPlan> subPlanIterator = subPlans.iterator(); subPlanIterator.hasNext();){
            NextPlan subPlan = subPlanIterator.next();
            if (subPlan instanceof NextPlanSurveyGoalZone){
                if (!agent.GetMap().GetDispensers().isEmpty()) subPlans.remove(subPlan);
                continue;
            }
            String blockType = ((NextPlanSurveyDispenser) subPlan).GetWantedMapTile().getThingType();
            HashSet<String> foundDispenser = agent.GetMap().GetDispensers().stream().map(mapTile -> mapTile.getThingType()).collect(Collectors.toCollection(HashSet::new));
            if (foundDispenser.contains(blockType)) subPlans.remove(subPlan);
        }
    }
}
