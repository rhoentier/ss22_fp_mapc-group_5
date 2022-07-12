/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package massim.javaagents.percept;

/**
 * Container class for describing surveyed Agents.  
 * @author AVL
 */
public class NextSurveyedThing {
    // surveyed("dispenser"/"goal"/"role", distance)
        private String type;
        private int distance;

    public NextSurveyedThing(String type, int distance) {
        this.type = type;
        this.distance = distance;
    }

    public String GetType() {
        return type;
    }

    public void SetType(String type) {
        this.type = type;
    }

    public int GetDistance() {
        return distance;
    }

    public void SetDistance(int distance) {
        this.distance = distance;
    }
    
}
