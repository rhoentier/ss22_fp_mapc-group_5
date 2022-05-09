/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package massim.javaagents.agents;

import java.util.HashSet;

/**
 *
 * @author AVL
 */
public class NextTask {
    
    // task(name, deadline, reward, [req(x,y,type),...])
    
    private String name;
    private long deadline;

    private long reward;
    private HashSet<MapTile> requiredBlocks;
    
    public NextTask (String name, int deadline, int reward, HashSet<MapTile> requiredBlocks) {
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

    public HashSet<MapTile> GetRequiredBlocks() {
        return requiredBlocks;
    }

    public void SetRequiredBlocks(HashSet<MapTile> requiredBlocks) {
        this.requiredBlocks = requiredBlocks;
    }
    
    @Override
    public String toString() {
        return "NextTask{" + "name=" + name + ", deadline=" + deadline + ", reward=" + reward + ", requiredBlocks=" + requiredBlocks + '}';
    }
    
    
    
}
