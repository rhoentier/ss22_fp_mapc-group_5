package massim.javaagents.percept;

import java.util.ArrayList;
import java.util.HashSet;

    /**
     *  Description of a role that can be adapted by an agent  
     * 
     *  Example:
     *  role(name, vision, [action1, action2, ...], [speed1, speed2, ...], clearChance, clearMaxDistance)
     *
     *  name : Identifier
     *  vision : Numeral
     *  action[N] : Identifier
     *  speed[N] : Numeral
     *  clearChance : Numeral (0-1)
     *  clearMaxDistance : Numeral
     *
     * @author Alexander Lorenz
     */

public class NextRole {

   /*
    * ########## region fields
    */
    
    private String name;                    // Name of the role
    private int vision;                     // distance of the vision
    private HashSet<String> action;         // possible actions to be peformed by the agent
    private ArrayList<Integer> speed;       // number of steps an agent can take. 
                                            // Numeral position corresponds to attached blocks 
    private float clearChance;              // Probability to clear a tile
    private int clearMaxDistance;           // maximum clearing distance
     
    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     * 
     * @param name String Name of the role
     * @param vision int distance of the vision
     * @param action String HashSet possible actions to be peformed by the agent
     * @param speed Integer ArrayList containing number of steps an agent can take.
     * @param clearChance float probability to clear a tile
     * @param clearMaxDistance int clearMaxDistance
     */
    public NextRole(String name, int vision, HashSet<String> action, ArrayList<Integer> speed, float clearChance, int clearMaxDistance) {
        this.name = name;
        this.vision = vision;
        this.action = action;
        this.speed = speed;
        this.clearChance = clearChance;
        this.clearMaxDistance = clearMaxDistance;
    }
    
    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */
    
    /**
     * Retrieves the name of the role
     * 
     * @return String rolename
     */
    public String GetName() {
        return name;
    }

    /**
     * Retrieves the range of vision based on manhattan distance
     * 
     * @return int distance in tiles
     */
    public int GetVision() {
        return vision;
    }

    /**
     * Retrieves the collection of actions, possible to be performed by the agent.
     * 
     * @return String HashSet containing possible actions
     */
    public HashSet<String> GetAction() {
        return action;
    }

    /**
     * Retrieves the possible amount of jumps, that can be performed by the agent in a step
     * The position of the number corresponds to the number of attached blocks.
     * 
     * @return Integer ArrayList containing the number of jumps.
     */
    public ArrayList<Integer> GetSpeed() {
        return speed;
    }

    /**
     * Retrieves the probability for a cell to be cleared
     * 
     * @return float specific probability
     */
    public float GetClearChance() {
        return clearChance;
    }

    /**
     * Retrieves the maximum distance to perform a clear action
     * 
     * @return int distance
     */
    public int GetClearMaxDistance() {
        return clearMaxDistance;
    }

    /**
     * This implementation returns a small part of the stored values as string
     * 
     * @return String formatted for representation
     */
    @Override
    public String toString() {
        return "NextRole{" + "name=" + name + ", vision=" + vision + ", action=" + action + '}';
    }
    
    /*
     * ##################### endregion public methods
     */

}
