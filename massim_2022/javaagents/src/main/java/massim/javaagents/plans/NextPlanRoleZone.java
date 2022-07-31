package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;

public class NextPlanRoleZone extends NextPlan {
    public NextPlanRoleZone(NextAgent agent) {
        this.agent = agent;
        this.agentTask = NextConstants.EAgentActivity.goToRolezone;
        CreateSubPlans();
    }

    /**
     * Erzeugt keine weiteren SubPlans
     */
    public void CreateSubPlans() {
        if(agent.GetMap().GetRoleZones().isEmpty())
            subPlans.add(new NextPlanSurveyRoleZone());
    }


    /**
     * Gibt zurück, ob noch eine RoleZone gefunden werden muss, oder ob direkt dorthin gegangen werden kann
     * @return NextPlan um Rolle zu wechseln
     */
    @Override
    public NextPlan GetDeepestPlan(){
        if (!agent.GetMap().GetRoleZones().isEmpty()) {
            return this;
        } else return subPlans.get(0);
    }
}
