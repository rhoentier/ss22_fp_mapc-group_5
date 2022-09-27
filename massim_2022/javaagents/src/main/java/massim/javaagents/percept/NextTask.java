package massim.javaagents.percept;

import java.util.HashSet;
import java.util.Iterator;

import massim.javaagents.map.NextMapTile;

/**
 * Task(name, deadline, reward, [req(x,y,type),...])
 *
 * @author Alexander Lorenz
 */
public class NextTask {

    private String name;
    private long deadline;

    private long reward;
    private HashSet<NextMapTile> requiredBlocks;

    /**
     *
     * @param name
     * @param deadline
     * @param reward
     * @param requiredBlocks
     */
    public NextTask(String name, int deadline, int reward, HashSet<NextMapTile> requiredBlocks) {
        this.name = name;
        this.deadline = deadline;
        this.reward = reward;
        this.requiredBlocks = requiredBlocks;
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
    public long GetDeadline() {
        return deadline;
    }

    /**
     *
     * @param deadline
     */
    public void SetDeadline(long deadline) {
        this.deadline = deadline;
    }

    /**
     *
     * @return
     */
    public long GetReward() {
        return reward;
    }

    /**
     *
     * @param reward
     */
    public void SetReward(long reward) {
        this.reward = reward;
    }

    /**
     *
     * @return
     */
    public HashSet<NextMapTile> GetRequiredBlocks() {
        return requiredBlocks;
    }

    /**
     *
     * @param requiredBlocks
     */
    public void SetRequiredBlocks(HashSet<NextMapTile> requiredBlocks) {
        this.requiredBlocks = requiredBlocks;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "NextTask{" + "name=" + name + ", deadline=" + deadline + ", reward=" + reward + ", requiredBlocks=" + requiredBlocks + '}';
    }

}
