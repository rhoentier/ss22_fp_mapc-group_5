package massim.javaagents.percept;

/**
 * Container class for describing surveyed agents.
 * Available after a survey action.
 *
 * @author Alexander Lorenz
 */
public class NextSurveyedAgent {

    /*
     * ########## region fields
     */

    private String name;    // name of the surveyed agent
    private String role;    // role of the surveyed agent
    private int energy;     // current available energy of the surveyed agent
 
    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     * 
     * @param name String name of the surveyed agent
     * @param role String role of the surveyed agent
     * @param energy int current available energy of the surveyed agent
     */
    public NextSurveyedAgent(String name, String role, int energy) {
        this.name = name;
        this.role = role;
        this.energy = energy;
    }
    
    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */

    /**
     * Retrieve the name of the surveyed agent
     * 
     * @return String name of the surveyed agent
     */
    public String GetName() {
        return name;
    }

    /**
     * Retrieve the role of the surveyed agent
     * 
     * @return String role of the surveyed agent
     */
    public String GetRole() {
        return role;
    }
    
    /**
     * Retrieve the energy level of the surveyed agent
     * 
     * @return int current energy
     */
    public int GetEnergy() {
        return energy;
    }
    
    /*
     * ##################### endregion public methods
     */    
}
