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

    public NextTask(String name, int deadline, int reward, HashSet<NextMapTile> requiredBlocks) {
        this.name = name;
        this.deadline = deadline;
        this.reward = reward;
        this.requiredBlocks = requiredBlocks;
    }

    public String GetName() {
        return name;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public long GetDeadline() {
        return deadline;
    }

    public void SetDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long GetReward() {
        return reward;
    }

    public void SetReward(long reward) {
        this.reward = reward;
    }

    public HashSet<NextMapTile> GetRequiredBlocks() {
        return requiredBlocks;
    }

    public void SetRequiredBlocks(HashSet<NextMapTile> requiredBlocks) {
        this.requiredBlocks = requiredBlocks;
    }

    @Override
    public String toString() {
        return "NextTask{" + "name=" + name + ", deadline=" + deadline + ", reward=" + reward + ", requiredBlocks=" + requiredBlocks + '}';
    }

}
