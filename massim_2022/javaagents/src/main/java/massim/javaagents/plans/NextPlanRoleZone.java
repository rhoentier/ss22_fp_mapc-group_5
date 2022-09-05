package massim.javaagents.plans;

import massim.javaagents.agents.NextAgent;
import massim.javaagents.general.NextConstants;

public class NextPlanRoleZone extends NextPlan {

    String role;

    public NextPlanRoleZone(NextAgent agent, String role) {
        this.agent = agent;
        this.agentTask = NextConstants.EAgentActivity.goToRolezone;
        CreateSubPlans();
        this.role = role;
    }

    /**
     * Erzeugt keine weiteren SubPlans
     */
    public void CreateSubPlans() {
        if(agent.GetMap().GetRoleZones().isEmpty())
            subPlans.add(new NextPlanSurveyRandom());
    }

    /**
     * Get the wanted role
     *
     * @return name of the role
     */
    public String GetRole() {
        return role;
    }

    /**
     * Gibt zurück, ob noch eine RoleZone gefunden werden muss, oder ob direkt dorthin gegangen werden kann
     *
     * @return NextPlan um Rolle zu wechseln
     */
    @Override
    public NextPlan GetDeepestPlan() {
        if (!agent.GetMap().GetRoleZones().isEmpty()) {
            return this;
        } else return subPlans.get(0);
    }
}
