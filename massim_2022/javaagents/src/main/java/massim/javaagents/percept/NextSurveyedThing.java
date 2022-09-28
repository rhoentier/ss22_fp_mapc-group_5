package massim.javaagents.percept;

/**
 * Container class for describing surveyed things.
 * Available after a survey action.
 * 
 * Example:
 * surveyed("dispenser"/"goal"/"role", distance)
 *
 * @author Alexander Lorenz
 */
public class NextSurveyedThing {

    /*
     * ########## region fields
     */

    private String type;        // Type of suveyed element
    private int distance;       // distance to the next instance of the specified type 

    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     *
     * @param type String type of the suveyed element
     * @param distance int distance to the next instance of the specified type 
     */
    
    public NextSurveyedThing(String type, int distance) {
        this.type = type;
        this.distance = distance;
    }
    
    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */

    /**
     * Retrieves the type of the surveyed element
     *
     * @return String type
     */
    public String GetType() {
        return type;
    }

    /**
     * Retrieves the distance to the next instance of the specified type 
     * 
     * @return int manhattan distance
     */
    public int GetDistance() {
        return distance;
    }
    
    /*
     * ##################### endregion public methods
     */
}
