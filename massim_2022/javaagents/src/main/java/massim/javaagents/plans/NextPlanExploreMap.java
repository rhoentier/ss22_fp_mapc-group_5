package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMap;
import massim.javaagents.map.NextMapTile;

import java.util.ArrayList;
import java.util.HashSet;

public class NextPlanExploreMap extends NextPlan{

    private HashSet<NextMapTile> wantedMapTiles;
    NextConstants.EAgentTask agentTask = NextConstants.EAgentTask.exploreMap;

    public NextPlanExploreMap(HashSet<NextMapTile> wantedMapTiles){
        this.wantedMapTiles = wantedMapTiles;
    }

    @Override
    public void CreateSubPlans() {
    }
}
