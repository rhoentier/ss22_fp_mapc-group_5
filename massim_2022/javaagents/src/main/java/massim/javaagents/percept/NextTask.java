package massim.javaagents.percept;

import java.util.HashSet;

import massim.javaagents.map.NextMapTile;

/**
 * Description of a task recieved from the server
 * 
 * example:
 * task(name, deadline, reward, [req(x,y,type),...])
 *
 * @author Alexander Lorenz
 */

public class NextTask {
    
    /*
     * ########## region fields
     */

    private String name;                            // identification of the task
    private int deadline;                          // last step for the task to be submitted
    private int reward;                            // possible reward after completing the task            
    private HashSet<NextMapTile> requiredBlocks;    // description of blocks and orientation required for completion
     
    /*
     * ##################### endregion fields
     */

    /**
     * ########## region constructor.
     *
     * @param name String identification of the task
     * @param deadline int last step for the task to be submitted
     * @param reward int possible reward after completing the task
     * @param requiredBlocks NextMapTile HashSet description of required blocks
     */
    public NextTask(String name, int deadline, int reward, HashSet<NextMapTile> requiredBlocks) {
        this.name = name;
        this.deadline = deadline;
        this.reward = reward;
        this.requiredBlocks = requiredBlocks;
    }
    
    /*
     * ##################### endregion constructor
     */
    
    /*
     * ########## region public methods
     */

    /**
     * Retrieves the identification of the task
     *
     * @return String name of te task
     */
    public String GetName() {
        return name;
    }

    /**
     * Retrieves the last step for the task to be successfully submitted
     * 
     * @return int deadline as a step
     */
    public int GetDeadline() {
        return deadline;
    }

    /**
     * Retrieves the possible reward for the successfull submission
     * 
     * @return int achieveable points
     */
    public int GetReward() {
        return reward;
    }

    /**
     * Returns collection of blocks in correct orientation for submussion
     *
     * @return NextMapTile HashSet description of blocks to submit
     */
    public HashSet<NextMapTile> GetRequiredBlocks() {
        return requiredBlocks;
    }

    /**
     * This implementation returns a small part of the stored values as string
     * 
     * @return String formatted for representation
     */
    @Override
    public String toString() {
        return "NextTask{" + "name=" + name + ", deadline=" + deadline + ", reward=" + reward + '}';
    }
    
    /*
     * ##################### endregion public methods
     */

}
