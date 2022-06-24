package massim.javaagents.plans;

import massim.javaagents.general.NextConstants;
import massim.javaagents.map.NextMapTile;

import java.util.HashSet;

public class NextPlanExploreMap extends NextPlan {

    private HashSet<NextMapTile> wantedMapTiles;

    public NextPlanExploreMap(HashSet<NextMapTile> wantedMapTiles) {
        this.agentTask = NextConstants.EAgentTask.exploreMap;
        this.wantedMapTiles = wantedMapTiles;
    }

    /**
     * Erzeugt momentan keine weiteren Subplans
     */
    @Override
    public void CreateSubPlans() {
    }

    public HashSet<NextMapTile> GetWantedMapTiles(){
        return wantedMapTiles;
    }
}
