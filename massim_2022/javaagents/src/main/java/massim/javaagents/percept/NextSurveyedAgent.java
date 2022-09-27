package massim.javaagents.percept;

/**
 * Container class for describing surveyed Agents.
 *
 * @author Alexander Lorenz
 */
public class NextSurveyedAgent {

    private String name;
    private String role;
    private int energy;

    /**
     *
     * @param name
     * @param role
     * @param energy
     */
    public NextSurveyedAgent(String name, String role, int energy) {
        this.name = name;
        this.role = role;
        this.energy = energy;
    }

    /**
     *
     * @return
     */
    public String GetName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void SetName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String GetRole() {
        return role;
    }

    /**
     *
     * @param role
     */
    public void SetRole(String role) {
        this.role = role;
    }

    /**
     *
     * @return
     */
    public int GetEnergy() {
        return energy;
    }

    /**
     *
     * @param energy
     */
    public void SetEnergy(int energy) {
        this.energy = energy;
    }
}
