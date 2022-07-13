package massim.javaagents.groupPlans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.agents.NextGroup;
import massim.javaagents.general.NextConstants;

public class NextGroupPlanRoleZone extends NextGroupPlan {
    public NextGroupPlanRoleZone(NextGroup group) {
        this.group = group;
        this.agentTask = NextConstants.EAgentActivity.goToRolezone;
        CreateSubPlans();
    }

    /**
     * Erzeugt keine weiteren SubPlans
     */
    @Override
    public void CreateSubPlans() {
        subPlans.add(new NextGroupPlanSurveyRoleZone());
    }


    /**
     * Gibt zurück, ob noch eine RoleZone gefunden werden muss, oder ob direkt dorthin gegangen werden kann
     * @return NextPlan um Rolle zu wechseln
     */
    @Override
    public NextGroupPlan GetDeepestPlan(){
        if (group.GetGroupMap().GetRoleZones().isEmpty()) {
            return this;
        } else return subPlans.get(0);
    }
}
