package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

public class NextGroupPlanExploreMap extends NextGroupPlan {

    private HashSet<NextMapTile> wantedMapTiles;

    public NextGroupPlanExploreMap(HashSet<NextMapTile> wantedMapTiles, NextGroup group) {
        this.group = group;
        this.agentTask = NextConstants.EAgentActivity.exploreMap;
        this.wantedMapTiles = wantedMapTiles;
        CreateSubPlans();
    }

    @Override
    public void CreateSubPlans() {
        if (group.GetGroupMap().GetGoalZones().isEmpty()) subPlans.add(new NextGroupPlanSurveyGoalZone());
        for (NextMapTile wantedMapTile : wantedMapTiles) {
            String blockType = wantedMapTile.getThingType();
            HashSet<String> foundDispenser = group.GetGroupMap().GetDispensers().stream().map(mapTile -> mapTile.getThingType()).collect(Collectors.toCollection(HashSet::new));
            if (!foundDispenser.contains(blockType))
                subPlans.add(new NextGroupPlanSurveyDispenser(wantedMapTile));
        }
    }

    public HashSet<NextMapTile> GetWantedMapTiles() {
        return wantedMapTiles;
    }

    public void CheckPreconditionStatus() {
        for (Iterator<NextGroupPlan> subPlanIterator = subPlans.iterator(); subPlanIterator.hasNext(); ) {
            NextGroupPlan subPlan = subPlanIterator.next();
            if (subPlan instanceof NextGroupPlanSurveyGoalZone) {
                if (!group.GetGroupMap().GetGoalZones().isEmpty()) subPlanIterator.remove();
                continue;
            }
            String blockType = ((NextGroupPlanSurveyDispenser) subPlan).GetWantedMapTile().getThingType();
            HashSet<String> foundDispenser = group.GetGroupMap().GetDispensers().stream().map(mapTile -> mapTile.getThingType()).collect(Collectors.toCollection(HashSet::new));
            if (foundDispenser.contains(blockType)) subPlanIterator.remove();
        }
    }
}
