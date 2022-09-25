package massim.javaagents.percept;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Alexander Lorenz
 */
public class NextRole {

    /*
        role(name, vision, [action1, action2, ...], [speed1, speed2, ...], clearChance, clearMaxDistance)

    name : Identifier
    vision : Numeral
    action[N] : Identifier
    speed[N] : Numeral
    clearChance : Numeral (0-1)
    clearMaxDistance : Numeral

     */
    private String name;
    private int vision;
    private HashSet<String> action;
    private ArrayList<Integer> speed;
    private float clearChance;
    private int clearMaxDistance;

    public NextRole(String name, int vision, HashSet<String> action, ArrayList<Integer> speed, float clearChance, int clearMaxDistance) {
        this.name = name;
        this.vision = vision;
        this.action = action;
        this.speed = speed;
        this.clearChance = clearChance;
        this.clearMaxDistance = clearMaxDistance;
    }

    public String GetName() {
        return name;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public int GetVision() {
        return vision;
    }

    public void SetVision(int vision) {
        this.vision = vision;
    }

    public HashSet<String> GetAction() {
        return action;
    }

    public void SetAction(HashSet<String> action) {
        this.action = action;
    }

    public ArrayList<Integer> GetSpeed() {
        return speed;
    }

    public void SetSpeed(ArrayList<Integer> speed) {
        this.speed = speed;
    }

    public float GetClearChance() {
        return clearChance;
    }

    public void SetClearChance(float clearChance) {
        this.clearChance = clearChance;
    }

    public int GetClearMaxDistance() {
        return clearMaxDistance;
    }

    public void SetClearMaxDistance(int clearMaxDistance) {
        this.clearMaxDistance = clearMaxDistance;
    }

    @Override
    public String toString() {
        return "NextRole{" + "name=" + name + ", vision=" + vision + ", action=" + action + ", speed=" + speed + ", clearChance=" + clearChance + ", clearMaxDistance=" + clearMaxDistance + '}';
    }

}
